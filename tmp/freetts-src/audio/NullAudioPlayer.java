/*     */ package com.sun.speech.freetts.audio;
/*     */ 
/*     */ import com.sun.speech.freetts.util.BulkTimer;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ 
/*     */ public class NullAudioPlayer
/*     */   implements AudioPlayer
/*     */ {
/*  23 */   private float volume = 1.0F;
/*     */   private AudioFormat audioFormat;
/*  25 */   private static final boolean TRACE = Utilities.getBoolean("com.sun.speech.freetts.audio.trace");
/*     */ 
/*  27 */   private boolean firstSound = true;
/*  28 */   private int totalBytes = 0;
/*  29 */   private int totalWrites = 0;
/*  30 */   private BulkTimer timer = new BulkTimer();
/*     */ 
/*     */   public void setAudioFormat(AudioFormat format)
/*     */   {
/*  46 */     this.audioFormat = format;
/*     */   }
/*     */ 
/*     */   public AudioFormat getAudioFormat()
/*     */   {
/*  55 */     return this.audioFormat;
/*     */   }
/*     */ 
/*     */   public void cancel()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void pause()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/*  80 */     this.timer.start("AudioOutput");
/*     */   }
/*     */ 
/*     */   public void resume()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*     */   }
/*     */ 
/*     */   public float getVolume()
/*     */   {
/* 106 */     return this.volume;
/*     */   }
/*     */ 
/*     */   public void setVolume(float volume)
/*     */   {
/* 115 */     this.volume = volume;
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] audioData)
/*     */   {
/* 128 */     return write(audioData, 0, audioData.length);
/*     */   }
/*     */ 
/*     */   public void begin(int size)
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean end()
/*     */   {
/* 146 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] bytes, int offset, int size)
/*     */   {
/* 160 */     this.totalBytes += size;
/* 161 */     this.totalWrites += 1;
/* 162 */     if (this.firstSound) {
/* 163 */       this.timer.stop("AudioFirstSound");
/* 164 */       this.firstSound = false;
/* 165 */       if (TRACE) {
/* 166 */         this.timer.show("Null Trace");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 172 */     return true;
/*     */   }
/*     */ 
/*     */   public void startFirstSampleTimer()
/*     */   {
/* 179 */     this.firstSound = true;
/* 180 */     this.timer.start("AudioFirstSound");
/*     */   }
/*     */ 
/*     */   public boolean drain()
/*     */   {
/* 190 */     this.timer.stop("AudioOutput");
/* 191 */     return true;
/*     */   }
/*     */ 
/*     */   public long getTime()
/*     */   {
/* 201 */     return -1L;
/*     */   }
/*     */ 
/*     */   public void resetTime()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showMetrics()
/*     */   {
/* 215 */     this.timer.show("NullAudioPlayer");
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.audio.NullAudioPlayer
 * JD-Core Version:    0.5.4
 */