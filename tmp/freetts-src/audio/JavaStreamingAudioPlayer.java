/*     */ package com.sun.speech.freetts.audio;
/*     */ 
/*     */ import com.sun.speech.freetts.util.BulkTimer;
/*     */ import com.sun.speech.freetts.util.Timer;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.PrintStream;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioSystem;
/*     */ import javax.sound.sampled.DataLine.Info;
/*     */ import javax.sound.sampled.FloatControl;
/*     */ import javax.sound.sampled.FloatControl.Type;
/*     */ import javax.sound.sampled.LineEvent;
/*     */ import javax.sound.sampled.LineEvent.Type;
/*     */ import javax.sound.sampled.LineListener;
/*     */ import javax.sound.sampled.LineUnavailableException;
/*     */ import javax.sound.sampled.SourceDataLine;
/*     */ 
/*     */ public class JavaStreamingAudioPlayer
/*     */   implements AudioPlayer
/*     */ {
/*     */   private volatile boolean paused;
/*  75 */   private volatile boolean done = false;
/*  76 */   private volatile boolean cancelled = false;
/*     */   private SourceDataLine line;
/*  79 */   private float volume = 1.0F;
/*  80 */   private long timeOffset = 0L;
/*  81 */   private BulkTimer timer = new BulkTimer();
/*     */ 
/*  84 */   private AudioFormat defaultFormat = new AudioFormat(8000.0F, 16, 1, true, true);
/*     */ 
/*  86 */   private AudioFormat currentFormat = this.defaultFormat;
/*     */ 
/*  88 */   private boolean debug = false;
/*  89 */   private boolean audioMetrics = false;
/*  90 */   private boolean firstSample = true;
/*     */   private long cancelDelay;
/*     */   private long drainDelay;
/*     */   private long openFailDelayMs;
/*     */   private long totalOpenFailDelayMs;
/*  97 */   private Object openLock = new Object();
/*  98 */   private Object lineLock = new Object();
/*     */ 
/* 104 */   private static final int AUDIO_BUFFER_SIZE = Utilities.getInteger("com.sun.speech.freetts.audio.AudioPlayer.bufferSize", 8192).intValue();
/*     */ 
/* 111 */   private static final int BYTES_PER_WRITE = Utilities.getInteger("com.sun.speech.freetts.audio.AudioPlayer.bytesPerWrite", 160).intValue();
/*     */ 
/*     */   public JavaStreamingAudioPlayer()
/*     */   {
/* 119 */     this.debug = Utilities.getBoolean("com.sun.speech.freetts.audio.AudioPlayer.debug");
/*     */ 
/* 121 */     this.cancelDelay = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.cancelDelay", 0L).longValue();
/*     */ 
/* 124 */     this.drainDelay = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.drainDelay", 150L).longValue();
/*     */ 
/* 127 */     this.openFailDelayMs = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.openFailDelayMs", 0L).longValue();
/*     */ 
/* 130 */     this.totalOpenFailDelayMs = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.totalOpenFailDelayMs", 0L).longValue();
/*     */ 
/* 133 */     this.audioMetrics = Utilities.getBoolean("com.sun.speech.freetts.audio.AudioPlayer.showAudioMetrics");
/*     */ 
/* 136 */     this.line = null;
/* 137 */     setPaused(false);
/*     */   }
/*     */ 
/*     */   public synchronized void setAudioFormat(AudioFormat format)
/*     */   {
/* 149 */     this.currentFormat = format;
/* 150 */     debugPrint("AF changed to " + format);
/*     */   }
/*     */ 
/*     */   public AudioFormat getAudioFormat()
/*     */   {
/* 160 */     return this.currentFormat;
/*     */   }
/*     */ 
/*     */   public void startFirstSampleTimer()
/*     */   {
/* 167 */     this.timer.start("firstAudio");
/* 168 */     this.firstSample = true;
/*     */   }
/*     */ 
/*     */   private synchronized void openLine(AudioFormat format)
/*     */   {
/* 181 */     synchronized (this.lineLock) {
/* 182 */       if (this.line != null) {
/* 183 */         this.line.close();
/* 184 */         this.line = null;
/*     */       }
/*     */     }
/* 187 */     DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
/*     */     int i;
/* 189 */     int i = 0;
/* 190 */     long totalDelayMs = 0L;
/*     */     do
/*     */       try
/*     */       {
/* 194 */         this.line = ((SourceDataLine)AudioSystem.getLine(info));
/* 195 */         this.line.addLineListener(new JavaStreamLineListener(null));
/*     */ 
/* 197 */         synchronized (this.openLock) {
/* 198 */           this.line.open(format, AUDIO_BUFFER_SIZE);
/*     */           try {
/* 200 */             this.openLock.wait();
/*     */           } catch (InterruptedException ie) {
/* 202 */             ie.printStackTrace();
/*     */           }
/* 204 */           i = 1;
/*     */         }
/*     */       } catch (LineUnavailableException lue) {
/* 207 */         System.err.println("LINE UNAVAILABLE: Format is " + this.currentFormat);
/*     */         try
/*     */         {
/* 210 */           Thread.sleep(this.openFailDelayMs);
/* 211 */           totalDelayMs += this.openFailDelayMs;
/*     */         } catch (InterruptedException ie) {
/* 213 */           ie.printStackTrace();
/*     */         }
/*     */       }
/* 216 */     while ((i == 0) && (totalDelayMs < this.totalOpenFailDelayMs));
/*     */ 
/* 218 */     if (i != 0) {
/* 219 */       setVolume(this.line, this.volume);
/* 220 */       resetTime();
/* 221 */       if ((isPaused()) && (this.line.isRunning()))
/* 222 */         this.line.stop();
/*     */       else
/* 224 */         this.line.start();
/*     */     }
/*     */     else {
/* 227 */       if (this.line != null) {
/* 228 */         this.line.close();
/*     */       }
/* 230 */       this.line = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void pause()
/*     */   {
/* 239 */     if (!isPaused()) {
/* 240 */       setPaused(true);
/* 241 */       if (this.line != null)
/* 242 */         this.line.stop();
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void resume()
/*     */   {
/* 251 */     if (isPaused()) {
/* 252 */       setPaused(false);
/* 253 */       if ((!isCancelled()) && (this.line != null)) {
/* 254 */         this.line.start();
/* 255 */         super.notify();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cancel()
/*     */   {
/* 272 */     debugPrint("cancelling...");
/*     */ 
/* 274 */     if (this.audioMetrics) {
/* 275 */       this.timer.start("audioCancel");
/*     */     }
/*     */ 
/* 278 */     if (this.cancelDelay > 0L) {
/*     */       try {
/* 280 */         Thread.sleep(this.cancelDelay);
/*     */       } catch (InterruptedException ie) {
/* 282 */         ie.printStackTrace();
/*     */       }
/*     */     }
/*     */ 
/* 286 */     synchronized (this.lineLock) {
/* 287 */       if ((this.line != null) && (this.line.isRunning())) {
/* 288 */         this.line.stop();
/* 289 */         this.line.flush();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 294 */     synchronized (this) {
/* 295 */       this.cancelled = true;
/* 296 */       super.notify();
/*     */     }
/*     */ 
/* 299 */     if (this.audioMetrics) {
/* 300 */       this.timer.stop("audioCancel");
/* 301 */       Timer.showTimesShortTitle("");
/* 302 */       this.timer.getTimer("audioCancel").showTimesShort(0L);
/*     */     }
/*     */ 
/* 305 */     debugPrint("...cancelled");
/*     */   }
/*     */ 
/*     */   public synchronized void reset()
/*     */   {
/* 314 */     this.timer.start("audioOut");
/* 315 */     if (this.line != null) {
/* 316 */       waitResume();
/* 317 */       if ((isCancelled()) && (!isDone())) {
/* 318 */         this.cancelled = false;
/* 319 */         this.line.start();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void close()
/*     */   {
/* 328 */     this.done = true;
/* 329 */     if ((this.line != null) && (this.line.isOpen())) {
/* 330 */       this.line.close();
/* 331 */       this.line = null;
/* 332 */       super.notify();
/*     */     }
/*     */   }
/*     */ 
/*     */   public float getVolume()
/*     */   {
/* 343 */     return this.volume;
/*     */   }
/*     */ 
/*     */   public void setVolume(float volume)
/*     */   {
/* 352 */     if (volume > 1.0F) {
/* 353 */       volume = 1.0F;
/*     */     }
/* 355 */     if (volume < 0.0F) {
/* 356 */       volume = 0.0F;
/*     */     }
/* 358 */     this.volume = volume;
/*     */   }
/*     */ 
/*     */   private void setPaused(boolean state)
/*     */   {
/* 367 */     this.paused = state;
/*     */   }
/*     */ 
/*     */   private boolean isPaused()
/*     */   {
/* 376 */     return this.paused;
/*     */   }
/*     */ 
/*     */   private void setVolume(SourceDataLine line, float vol)
/*     */   {
/* 386 */     if ((line == null) || (!line.isControlSupported(FloatControl.Type.MASTER_GAIN)))
/*     */       return;
/* 388 */     FloatControl volumeControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
/*     */ 
/* 390 */     float range = volumeControl.getMaximum() - volumeControl.getMinimum();
/*     */ 
/* 392 */     volumeControl.setValue(vol * range + volumeControl.getMinimum());
/*     */   }
/*     */ 
/*     */   public void begin(int size)
/*     */   {
/* 409 */     debugPrint("opening Stream...");
/* 410 */     openLine(this.currentFormat);
/* 411 */     reset();
/* 412 */     debugPrint("...Stream opened");
/*     */   }
/*     */ 
/*     */   public synchronized boolean end()
/*     */   {
/* 424 */     if (this.line != null) {
/* 425 */       drain();
/* 426 */       synchronized (this.lineLock) {
/* 427 */         this.line.close();
/* 428 */         this.line = null;
/*     */       }
/* 430 */       super.notify();
/* 431 */       debugPrint("ended stream...");
/*     */     }
/* 433 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean drain()
/*     */   {
/* 451 */     if (this.line != null) {
/* 452 */       debugPrint("started draining...");
/* 453 */       if (this.line.isOpen()) {
/* 454 */         this.line.drain();
/* 455 */         if (this.drainDelay > 0L)
/*     */           try {
/* 457 */             Thread.sleep(this.drainDelay);
/*     */           }
/*     */           catch (InterruptedException ie) {
/*     */           }
/*     */       }
/* 462 */       debugPrint("...finished draining");
/*     */     }
/* 464 */     this.timer.stop("audioOut");
/*     */ 
/* 466 */     return !isCancelled();
/*     */   }
/*     */ 
/*     */   public synchronized long getTime()
/*     */   {
/* 475 */     return (this.line.getMicrosecondPosition() - this.timeOffset) / 1000L;
/*     */   }
/*     */ 
/*     */   public synchronized void resetTime()
/*     */   {
/* 483 */     this.timeOffset = this.line.getMicrosecondPosition();
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] audioData)
/*     */   {
/* 497 */     return write(audioData, 0, audioData.length);
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] bytes, int offset, int size)
/*     */   {
/* 511 */     if (this.line == null) {
/* 512 */       return false;
/*     */     }
/*     */ 
/* 515 */     int bytesRemaining = size;
/* 516 */     int curIndex = offset;
/*     */ 
/* 518 */     if (this.firstSample) {
/* 519 */       this.firstSample = false;
/* 520 */       this.timer.stop("firstAudio");
/* 521 */       if (this.audioMetrics) {
/* 522 */         Timer.showTimesShortTitle("");
/* 523 */         this.timer.getTimer("firstAudio").showTimesShort(0L);
/*     */       }
/*     */     }
/* 526 */     debugPrint(" au write " + bytesRemaining + " pos " + this.line.getMicrosecondPosition() + " avail " + this.line.available() + " bsz " + this.line.getBufferSize());
/*     */ 
/* 531 */     while ((bytesRemaining > 0) && (!isCancelled()))
/*     */     {
/* 533 */       if (!waitResume()) {
/* 534 */         return false;
/*     */       }
/*     */ 
/* 537 */       debugPrint("   queueing cur " + curIndex + " br " + bytesRemaining);
/*     */ 
/* 540 */       synchronized (this.lineLock) {
/* 541 */         int bytesWritten = this.line.write(bytes, curIndex, Math.min(BYTES_PER_WRITE, bytesRemaining));
/*     */ 
/* 545 */         if (bytesWritten != bytesWritten) {
/* 546 */           debugPrint("RETRY! bw" + bytesWritten + " br " + bytesRemaining);
/*     */         }
/*     */ 
/* 550 */         curIndex += bytesWritten;
/* 551 */         bytesRemaining -= bytesWritten;
/*     */       }
/*     */       int bytesWritten;
/* 554 */       debugPrint("   wrote  cur " + curIndex + " br " + bytesRemaining + " bw " + bytesWritten);
/*     */     }
/*     */ 
/* 559 */     return (!isCancelled()) && (!isDone());
/*     */   }
/*     */ 
/*     */   private synchronized boolean waitResume()
/*     */   {
/* 572 */     while ((isPaused()) && (!isCancelled()) && (!isDone()))
/*     */       try {
/* 574 */         debugPrint("   paused waiting ");
/* 575 */         super.wait();
/*     */       }
/*     */       catch (InterruptedException ie)
/*     */       {
/*     */       }
/* 580 */     return (!isCancelled()) && (!isDone());
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 590 */     return "JavaStreamingAudioPlayer";
/*     */   }
/*     */ 
/*     */   private void debugPrint(String msg)
/*     */   {
/* 600 */     if (this.debug)
/* 601 */       System.out.println(toString() + ": " + msg);
/*     */   }
/*     */ 
/*     */   public void showMetrics()
/*     */   {
/* 609 */     this.timer.show("JavaStreamingAudioPlayer");
/*     */   }
/*     */ 
/*     */   private synchronized boolean isCancelled()
/*     */   {
/* 620 */     return this.cancelled;
/*     */   }
/*     */ 
/*     */   private synchronized boolean isDone()
/*     */   {
/* 631 */     return this.done;
/*     */   }
/*     */ 
/*     */   private class JavaStreamLineListener
/*     */     implements LineListener
/*     */   {
/*     */     private final JavaStreamingAudioPlayer this$0;
/*     */ 
/*     */     private JavaStreamLineListener()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void update(LineEvent event)
/*     */     {
/* 646 */       if (event.getType().equals(LineEvent.Type.OPEN))
/* 647 */         synchronized (this.this$0.openLock) {
/* 648 */           this.this$0.openLock.notifyAll();
/*     */         }
/*     */     }
/*     */ 
/*     */     JavaStreamLineListener(JavaStreamingAudioPlayer.1 x1)
/*     */     {
/* 637 */       this(x0);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.audio.JavaStreamingAudioPlayer
 * JD-Core Version:    0.5.4
 */