/*     */ package com.sun.speech.freetts.audio;
/*     */ 
/*     */ import com.sun.speech.freetts.util.BulkTimer;
/*     */ import com.sun.speech.freetts.util.Timer;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.IOException;
/*     */ import java.io.PipedInputStream;
/*     */ import java.io.PipedOutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javax.sound.sampled.AudioSystem;
/*     */ import javax.sound.sampled.Clip;
/*     */ import javax.sound.sampled.DataLine.Info;
/*     */ import javax.sound.sampled.FloatControl;
/*     */ import javax.sound.sampled.FloatControl.Type;
/*     */ import javax.sound.sampled.LineEvent;
/*     */ import javax.sound.sampled.LineEvent.Type;
/*     */ import javax.sound.sampled.LineListener;
/*     */ import javax.sound.sampled.LineUnavailableException;
/*     */ 
/*     */ public class JavaClipAudioPlayer
/*     */   implements AudioPlayer
/*     */ {
/*  41 */   private static final Logger LOGGER = Logger.getLogger(JavaClipAudioPlayer.class.getName());
/*     */   private volatile boolean paused;
/*  45 */   private volatile boolean cancelled = false;
/*     */   private volatile Clip currentClip;
/*  49 */   private float volume = 1.0F;
/*  50 */   private boolean audioMetrics = false;
/*  51 */   private final BulkTimer timer = new BulkTimer();
/*     */ 
/*  53 */   private AudioFormat defaultFormat = new AudioFormat(8000.0F, 16, 1, true, true);
/*     */ 
/*  55 */   private AudioFormat currentFormat = this.defaultFormat;
/*  56 */   private boolean firstSample = true;
/*  57 */   private boolean firstPlay = true;
/*  58 */   private int curIndex = 0;
/*     */   private final PipedOutputStream outputData;
/*     */   private AudioInputStream audioInput;
/*     */   private final LineListener lineListener;
/*     */   private long drainDelay;
/*     */   private long openFailDelayMs;
/*     */   private long totalOpenFailDelayMs;
/*     */ 
/*     */   public JavaClipAudioPlayer()
/*     */   {
/*  74 */     this.drainDelay = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.drainDelay", 150L).longValue();
/*     */ 
/*  77 */     this.openFailDelayMs = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.openFailDelayMs", 0L).longValue();
/*     */ 
/*  80 */     this.totalOpenFailDelayMs = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.totalOpenFailDelayMs", 0L).longValue();
/*     */ 
/*  83 */     this.audioMetrics = Utilities.getBoolean("com.sun.speech.freetts.audio.AudioPlayer.showAudioMetrics");
/*     */ 
/*  85 */     setPaused(false);
/*  86 */     this.outputData = new PipedOutputStream();
/*  87 */     this.lineListener = new JavaClipLineListener(null);
/*     */   }
/*     */ 
/*     */   public synchronized void setAudioFormat(AudioFormat format)
/*     */   {
/*  99 */     if (this.currentFormat.matches(format)) {
/* 100 */       return;
/*     */     }
/* 102 */     this.currentFormat = format;
/*     */ 
/* 104 */     if (this.currentClip != null)
/* 105 */       this.currentClip = null;
/*     */   }
/*     */ 
/*     */   public AudioFormat getAudioFormat()
/*     */   {
/* 115 */     return this.currentFormat;
/*     */   }
/*     */ 
/*     */   public void pause()
/*     */   {
/* 125 */     if (!this.paused) {
/* 126 */       setPaused(true);
/* 127 */       if (this.currentClip != null) {
/* 128 */         this.currentClip.stop();
/*     */       }
/* 130 */       synchronized (this) {
/* 131 */         super.notifyAll();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void resume()
/*     */   {
/* 141 */     if (this.paused) {
/* 142 */       setPaused(false);
/* 143 */       if (this.currentClip != null) {
/* 144 */         this.currentClip.start();
/*     */       }
/* 146 */       super.notifyAll();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cancel()
/*     */   {
/* 155 */     if (this.audioMetrics) {
/* 156 */       this.timer.start("audioCancel");
/*     */     }
/* 158 */     if (this.currentClip != null) {
/* 159 */       this.currentClip.stop();
/* 160 */       this.currentClip.close();
/*     */     }
/* 162 */     synchronized (this) {
/* 163 */       this.cancelled = true;
/* 164 */       this.paused = false;
/* 165 */       super.notifyAll();
/*     */     }
/* 167 */     if (this.audioMetrics) {
/* 168 */       this.timer.stop("audioCancel");
/* 169 */       Timer.showTimesShortTitle("");
/* 170 */       this.timer.getTimer("audioCancel").showTimesShort(0L);
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void reset()
/*     */   {
/* 180 */     this.timer.start("speakableOut");
/*     */   }
/*     */ 
/*     */   public boolean drain()
/*     */   {
/* 190 */     this.timer.stop("speakableOut");
/* 191 */     return true;
/*     */   }
/*     */ 
/*     */   public synchronized void close()
/*     */   {
/* 206 */     if (this.currentClip != null) {
/* 207 */       this.currentClip.drain();
/* 208 */       if (this.drainDelay > 0L)
/*     */         try {
/* 210 */           Thread.sleep(this.drainDelay);
/*     */         }
/*     */         catch (InterruptedException e) {
/*     */         }
/* 214 */       this.currentClip.close();
/*     */     }
/* 216 */     super.notifyAll();
/*     */   }
/*     */ 
/*     */   public float getVolume()
/*     */   {
/* 224 */     return this.volume;
/*     */   }
/*     */ 
/*     */   public void setVolume(float volume)
/*     */   {
/* 232 */     if (volume > 1.0F) {
/* 233 */       volume = 1.0F;
/*     */     }
/* 235 */     if (volume < 0.0F) {
/* 236 */       volume = 0.0F;
/*     */     }
/* 238 */     this.volume = volume;
/* 239 */     if (this.currentClip != null)
/* 240 */       setVolume(this.currentClip, volume);
/*     */   }
/*     */ 
/*     */   private void setPaused(boolean state)
/*     */   {
/* 249 */     this.paused = state;
/*     */   }
/*     */ 
/*     */   private void setVolume(Clip clip, float vol)
/*     */   {
/* 260 */     if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
/* 261 */       FloatControl volumeControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
/*     */ 
/* 263 */       float range = volumeControl.getMaximum() - volumeControl.getMinimum();
/*     */ 
/* 265 */       volumeControl.setValue(vol * range + volumeControl.getMinimum());
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized long getTime()
/*     */   {
/* 280 */     return -1L;
/*     */   }
/*     */ 
/*     */   public synchronized void resetTime()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void begin(int size)
/*     */   {
/* 299 */     this.timer.start("utteranceOutput");
/* 300 */     this.cancelled = false;
/* 301 */     this.curIndex = 0;
/*     */     try
/*     */     {
/* 304 */       PipedInputStream in = new PipedInputStream(this.outputData);
/* 305 */       this.audioInput = new AudioInputStream(in, this.currentFormat, size);
/*     */     } catch (IOException e) {
/* 307 */       LOGGER.warning(e.getLocalizedMessage());
/*     */     }
/* 309 */     while ((this.paused) && (!this.cancelled)) {
/*     */       try {
/* 311 */         super.wait();
/*     */       } catch (InterruptedException ie) {
/* 313 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 317 */     this.timer.start("clipGeneration");
/*     */ 
/* 319 */     boolean opened = false;
/* 320 */     long totalDelayMs = 0L;
/*     */     do
/*     */     {
/*     */       try
/*     */       {
/* 325 */         this.currentClip = getClip();
/* 326 */         this.currentClip.open(this.audioInput);
/* 327 */         opened = true;
/*     */       } catch (LineUnavailableException lue) {
/* 329 */         System.err.println("LINE UNAVAILABLE: Format is " + this.currentFormat);
/*     */         try
/*     */         {
/* 332 */           Thread.sleep(this.openFailDelayMs);
/* 333 */           totalDelayMs += this.openFailDelayMs;
/*     */         } catch (InterruptedException ie) {
/* 335 */           ie.printStackTrace();
/*     */         }
/*     */       } catch (IOException e) {
/* 338 */         LOGGER.warning(e.getLocalizedMessage());
/*     */       }
/*     */     }
/* 340 */     while ((!opened) && (totalDelayMs < this.totalOpenFailDelayMs));
/*     */ 
/* 342 */     if (!opened) {
/* 343 */       close();
/*     */     } else {
/* 345 */       setVolume(this.currentClip, this.volume);
/* 346 */       if ((this.audioMetrics) && (this.firstPlay)) {
/* 347 */         this.firstPlay = false;
/* 348 */         this.timer.stop("firstPlay");
/* 349 */         this.timer.getTimer("firstPlay");
/* 350 */         Timer.showTimesShortTitle("");
/* 351 */         this.timer.getTimer("firstPlay").showTimesShort(0L);
/*     */       }
/* 353 */       this.currentClip.start();
/*     */     }
/*     */   }
/*     */ 
/*     */   private Clip getClip()
/*     */     throws LineUnavailableException
/*     */   {
/* 364 */     if (this.currentClip == null) {
/* 365 */       if (LOGGER.isLoggable(Level.FINE)) {
/* 366 */         LOGGER.fine("creating new clip");
/*     */       }
/* 368 */       DataLine.Info info = new DataLine.Info(Clip.class, this.currentFormat);
/*     */       try {
/* 370 */         this.currentClip = ((Clip)AudioSystem.getLine(info));
/* 371 */         this.currentClip.addLineListener(this.lineListener);
/*     */       } catch (SecurityException e) {
/* 373 */         throw new LineUnavailableException(e.getLocalizedMessage());
/*     */       } catch (IllegalArgumentException e) {
/* 375 */         throw new LineUnavailableException(e.getLocalizedMessage());
/*     */       }
/*     */     }
/* 378 */     return this.currentClip;
/*     */   }
/*     */ 
/*     */   public synchronized boolean end()
/*     */   {
/* 389 */     boolean ok = true;
/*     */ 
/* 391 */     if (this.cancelled) {
/* 392 */       return false;
/*     */     }
/*     */ 
/* 395 */     if ((this.currentClip == null) || (!this.currentClip.isOpen())) {
/* 396 */       close();
/* 397 */       ok = false;
/*     */     } else {
/* 399 */       setVolume(this.currentClip, this.volume);
/* 400 */       if ((this.audioMetrics) && (this.firstPlay)) {
/* 401 */         this.firstPlay = false;
/* 402 */         this.timer.stop("firstPlay");
/* 403 */         this.timer.getTimer("firstPlay");
/* 404 */         Timer.showTimesShortTitle("");
/* 405 */         this.timer.getTimer("firstPlay").showTimesShort(0L);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 410 */         while ((this.currentClip != null) && (((this.currentClip.isRunning()) || (this.paused))) && (!this.cancelled))
/* 411 */           super.wait();
/*     */       }
/*     */       catch (InterruptedException ie) {
/* 414 */         ok = false;
/*     */       }
/* 416 */       close();
/*     */     }
/*     */ 
/* 419 */     this.timer.stop("clipGeneration");
/* 420 */     this.timer.stop("utteranceOutput");
/* 421 */     ok &= !this.cancelled;
/* 422 */     return ok;
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] audioData)
/*     */   {
/* 435 */     return write(audioData, 0, audioData.length);
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] bytes, int offset, int size)
/*     */   {
/* 449 */     if (this.firstSample) {
/* 450 */       this.firstSample = false;
/* 451 */       this.timer.stop("firstAudio");
/* 452 */       if (this.audioMetrics) {
/* 453 */         Timer.showTimesShortTitle("");
/* 454 */         this.timer.getTimer("firstAudio").showTimesShort(0L);
/*     */       }
/*     */     }
/*     */     try {
/* 458 */       this.outputData.write(bytes, offset, size);
/*     */     } catch (IOException e) {
/* 460 */       LOGGER.warning(e.getLocalizedMessage());
/* 461 */       return false;
/*     */     }
/* 463 */     this.curIndex += size;
/* 464 */     return true;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 474 */     return "JavaClipAudioPlayer";
/*     */   }
/*     */ 
/*     */   public void showMetrics()
/*     */   {
/* 482 */     this.timer.show(toString());
/*     */   }
/*     */ 
/*     */   public void startFirstSampleTimer()
/*     */   {
/* 489 */     this.timer.start("firstAudio");
/* 490 */     this.firstSample = true;
/* 491 */     if (this.audioMetrics) {
/* 492 */       this.timer.start("firstPlay");
/* 493 */       this.firstPlay = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   private class JavaClipLineListener
/*     */     implements LineListener
/*     */   {
/*     */     private final JavaClipAudioPlayer this$0;
/*     */ 
/*     */     private JavaClipLineListener()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void update(LineEvent event)
/*     */     {
/* 510 */       if (event.getType().equals(LineEvent.Type.START)) {
/* 511 */         if (JavaClipAudioPlayer.LOGGER.isLoggable(Level.FINE))
/* 512 */           JavaClipAudioPlayer.LOGGER.fine(super.toString() + ": EVENT START");
/*     */       }
/* 514 */       else if (event.getType().equals(LineEvent.Type.STOP)) {
/* 515 */         if (JavaClipAudioPlayer.LOGGER.isLoggable(Level.FINE)) {
/* 516 */           JavaClipAudioPlayer.LOGGER.fine(super.toString() + ": EVENT STOP");
/*     */         }
/* 518 */         synchronized (this.this$0) {
/* 519 */           this.this$0.notifyAll();
/*     */         }
/* 521 */       } else if (event.getType().equals(LineEvent.Type.OPEN)) {
/* 522 */         if (JavaClipAudioPlayer.LOGGER.isLoggable(Level.FINE))
/* 523 */           JavaClipAudioPlayer.LOGGER.fine(super.toString() + ": EVENT OPEN");
/*     */       } else {
/* 525 */         if (!event.getType().equals(LineEvent.Type.CLOSE)) {
/*     */           return;
/*     */         }
/*     */ 
/* 529 */         if (JavaClipAudioPlayer.LOGGER.isLoggable(Level.FINE)) {
/* 530 */           JavaClipAudioPlayer.LOGGER.fine(super.toString() + ": EVENT CLOSE");
/*     */         }
/* 532 */         synchronized (this.this$0) {
/* 533 */           this.this$0.notifyAll();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*     */     JavaClipLineListener(JavaClipAudioPlayer.1 x1)
/*     */     {
/* 501 */       this(x0);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.audio.JavaClipAudioPlayer
 * JD-Core Version:    0.5.4
 */