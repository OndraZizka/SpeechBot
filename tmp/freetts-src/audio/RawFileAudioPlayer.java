/*     */ package com.sun.speech.freetts.audio;
/*     */ 
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ 
/*     */ public class RawFileAudioPlayer
/*     */   implements AudioPlayer
/*     */ {
/*     */   private AudioFormat audioFormat;
/*     */   private float volume;
/*     */   private BufferedOutputStream os;
/*     */   private String path;
/*     */ 
/*     */   public RawFileAudioPlayer()
/*     */     throws IOException
/*     */   {
/*  37 */     this(Utilities.getProperty("com.sun.speech.freetts.AudioPlayer.baseName", "freetts") + ".raw");
/*     */   }
/*     */ 
/*     */   public RawFileAudioPlayer(String path)
/*     */     throws IOException
/*     */   {
/*  46 */     this.path = path;
/*  47 */     this.os = new BufferedOutputStream(new FileOutputStream(path));
/*     */   }
/*     */ 
/*     */   public void setAudioFormat(AudioFormat format)
/*     */   {
/*  57 */     this.audioFormat = format;
/*     */   }
/*     */ 
/*     */   public AudioFormat getAudioFormat()
/*     */   {
/*  66 */     return this.audioFormat;
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
/*     */   }
/*     */ 
/*     */   public void resume()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*     */     try
/*     */     {
/* 108 */       this.os.flush();
/* 109 */       this.os.close();
/* 110 */       System.out.println("Wrote synthesized speech to " + this.path);
/*     */     } catch (IOException ioe) {
/* 112 */       ioe.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public float getVolume()
/*     */   {
/* 123 */     return this.volume;
/*     */   }
/*     */ 
/*     */   public void setVolume(float volume)
/*     */   {
/* 132 */     this.volume = volume;
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] audioData)
/*     */   {
/* 145 */     return write(audioData, 0, audioData.length);
/*     */   }
/*     */ 
/*     */   public void begin(int size)
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean end()
/*     */   {
/* 163 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] bytes, int offset, int size)
/*     */   {
/*     */     try
/*     */     {
/* 178 */       this.os.write(bytes, offset, size);
/*     */     } catch (IOException ioe) {
/* 180 */       return false;
/*     */     }
/* 182 */     return true;
/*     */   }
/*     */ 
/*     */   public void startFirstSampleTimer()
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean drain()
/*     */   {
/* 198 */     return true;
/*     */   }
/*     */ 
/*     */   public long getTime()
/*     */   {
/* 208 */     return -1L;
/*     */   }
/*     */ 
/*     */   public void resetTime()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showMetrics()
/*     */   {
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.audio.RawFileAudioPlayer
 * JD-Core Version:    0.5.4
 */