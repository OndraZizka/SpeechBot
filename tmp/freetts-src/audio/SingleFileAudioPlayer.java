/*     */ package com.sun.speech.freetts.audio;
/*     */ 
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.io.SequenceInputStream;
/*     */ import java.util.Vector;
/*     */ import javax.sound.sampled.AudioFileFormat.Type;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ import javax.sound.sampled.AudioInputStream;
/*     */ import javax.sound.sampled.AudioSystem;
/*     */ 
/*     */ public class SingleFileAudioPlayer
/*     */   implements AudioPlayer
/*     */ {
/*  30 */   private AudioFormat currentFormat = null;
/*     */   private String baseName;
/*     */   private byte[] outputData;
/*  33 */   private int curIndex = 0;
/*  34 */   private int totBytes = 0;
/*     */   private AudioFileFormat.Type outputType;
/*     */   private Vector outputList;
/*     */ 
/*     */   public SingleFileAudioPlayer(String baseName, AudioFileFormat.Type type)
/*     */   {
/*  47 */     this.baseName = (baseName + "." + type.getExtension());
/*  48 */     this.outputType = type;
/*     */ 
/*  50 */     this.outputList = new Vector();
/*     */   }
/*     */ 
/*     */   public SingleFileAudioPlayer()
/*     */   {
/*  61 */     this(Utilities.getProperty("com.sun.speech.freetts.AudioPlayer.baseName", "freetts"), AudioFileFormat.Type.WAVE);
/*     */   }
/*     */ 
/*     */   public synchronized void setAudioFormat(AudioFormat format)
/*     */   {
/*  75 */     this.currentFormat = format;
/*     */   }
/*     */ 
/*     */   public AudioFormat getAudioFormat()
/*     */   {
/*  85 */     return this.currentFormat;
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
/*     */   public synchronized void cancel()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void reset()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void startFirstSampleTimer()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void close()
/*     */   {
/*     */     try
/*     */     {
/* 129 */       File file = new File(this.baseName);
/* 130 */       InputStream is = new SequenceInputStream(this.outputList.elements());
/* 131 */       AudioInputStream ais = new AudioInputStream(is, this.currentFormat, this.totBytes / this.currentFormat.getFrameSize());
/*     */ 
/* 138 */       System.out.println("Wrote synthesized speech to " + this.baseName);
/* 139 */       AudioSystem.write(ais, this.outputType, file);
/*     */     } catch (IOException ioe) {
/* 141 */       System.err.println("Can't write audio to " + this.baseName);
/*     */     } catch (IllegalArgumentException iae) {
/* 143 */       System.err.println("Can't write audio type " + this.outputType);
/*     */     }
/*     */   }
/*     */ 
/*     */   public float getVolume()
/*     */   {
/* 154 */     return 1.0F;
/*     */   }
/*     */ 
/*     */   public void setVolume(float volume)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void begin(int size)
/*     */   {
/* 175 */     this.outputData = new byte[size];
/* 176 */     this.curIndex = 0;
/*     */   }
/*     */ 
/*     */   public boolean end()
/*     */   {
/* 188 */     this.outputList.add(new ByteArrayInputStream(this.outputData));
/* 189 */     this.totBytes += this.outputData.length;
/* 190 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean drain()
/*     */   {
/* 201 */     return true;
/*     */   }
/*     */ 
/*     */   public synchronized long getTime()
/*     */   {
/* 210 */     return -1L;
/*     */   }
/*     */ 
/*     */   public synchronized void resetTime()
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] audioData)
/*     */   {
/* 231 */     return write(audioData, 0, audioData.length);
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] bytes, int offset, int size)
/*     */   {
/* 245 */     System.arraycopy(bytes, offset, this.outputData, this.curIndex, size);
/* 246 */     this.curIndex += size;
/* 247 */     return true;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 256 */     return "FileAudioPlayer";
/*     */   }
/*     */ 
/*     */   public void showMetrics()
/*     */   {
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.audio.SingleFileAudioPlayer
 * JD-Core Version:    0.5.4
 */