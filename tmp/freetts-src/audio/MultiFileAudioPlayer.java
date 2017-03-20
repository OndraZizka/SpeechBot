/*     */ package com.sun.speech.freetts.audio;
/*     */ 
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import javax.sound.sampled.AudioFileFormat.Type;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javax.sound.sampled.AudioSystem;
/*     */ 
/*     */ public class MultiFileAudioPlayer
/*     */   implements AudioPlayer
/*     */ {
/*  27 */   private AudioFormat currentFormat = null;
/*  28 */   private int fileCount = 0;
/*     */   private String baseName;
/*     */   private byte[] outputData;
/*  31 */   private int curIndex = 0;
/*     */   private AudioFileFormat.Type outputType;
/*     */ 
/*     */   public MultiFileAudioPlayer()
/*     */   {
/*  42 */     this(Utilities.getProperty("com.sun.speech.freetts.AudioPlayer.baseName", "freetts"), AudioFileFormat.Type.WAVE);
/*     */   }
/*     */ 
/*     */   public MultiFileAudioPlayer(String baseName, AudioFileFormat.Type type)
/*     */   {
/*  55 */     this.baseName = baseName;
/*  56 */     this.outputType = type;
/*     */   }
/*     */ 
/*     */   public synchronized void setAudioFormat(AudioFormat format)
/*     */   {
/*  68 */     this.currentFormat = format;
/*     */   }
/*     */ 
/*     */   public AudioFormat getAudioFormat()
/*     */   {
/*  78 */     return this.currentFormat;
/*     */   }
/*     */ 
/*     */   public void pause()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void resume()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void startFirstSampleTimer()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void cancel()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void reset()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void close()
/*     */   {
/*     */   }
/*     */ 
/*     */   public float getVolume()
/*     */   {
/* 127 */     return 1.0F;
/*     */   }
/*     */ 
/*     */   public void setVolume(float volume)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void begin(int size)
/*     */   {
/* 148 */     this.outputData = new byte[size];
/* 149 */     this.curIndex = 0;
/*     */   }
/*     */ 
/*     */   public boolean end()
/*     */   {
/* 161 */     ByteArrayInputStream bais = new ByteArrayInputStream(this.outputData);
/* 162 */     AudioInputStream ais = new AudioInputStream(bais, this.currentFormat, this.outputData.length / this.currentFormat.getFrameSize());
/*     */ 
/* 164 */     String name = this.baseName;
/* 165 */     name = name + this.fileCount;
/* 166 */     name = name + "." + this.outputType.getExtension();
/* 167 */     File file = new File(name);
/*     */     try {
/* 169 */       AudioSystem.write(ais, this.outputType, file);
/* 170 */       System.out.println("Wrote synthesized speech to " + name);
/*     */     } catch (IOException ioe) {
/* 172 */       System.err.println("Can't write audio to " + file);
/* 173 */       return false;
/*     */     } catch (IllegalArgumentException iae) {
/* 175 */       System.err.println("Can't write audio type " + this.outputType);
/* 176 */       return false;
/*     */     }
/* 178 */     this.fileCount += 1;
/* 179 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean drain()
/*     */   {
/* 190 */     return true;
/*     */   }
/*     */ 
/*     */   public synchronized long getTime()
/*     */   {
/* 199 */     return -1L;
/*     */   }
/*     */ 
/*     */   public synchronized void resetTime()
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] audioData)
/*     */   {
/* 220 */     return write(audioData, 0, audioData.length);
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] bytes, int offset, int size)
/*     */   {
/* 234 */     System.arraycopy(bytes, offset, this.outputData, this.curIndex, size);
/* 235 */     this.curIndex += size;
/* 236 */     return true;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 245 */     return "FileAudioPlayer";
/*     */   }
/*     */ 
/*     */   public void showMetrics()
/*     */   {
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.audio.MultiFileAudioPlayer
 * JD-Core Version:    0.5.4
 */