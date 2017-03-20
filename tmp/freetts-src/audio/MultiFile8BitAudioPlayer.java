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
/*     */ public class MultiFile8BitAudioPlayer
/*     */   implements AudioPlayer
/*     */ {
/*  29 */   private AudioFormat currentFormat = new AudioFormat(8000.0F, 8, 1, false, false);
/*     */ 
/*  32 */   private int fileCount = 0;
/*     */   private String baseName;
/*     */   private byte[] outputData;
/*  35 */   private int curIndex = 0;
/*     */   private AudioFileFormat.Type outputType;
/*     */ 
/*     */   public MultiFile8BitAudioPlayer()
/*     */   {
/*  47 */     this(Utilities.getProperty("com.sun.speech.freetts.AudioPlayer.baseName", "freetts"), AudioFileFormat.Type.WAVE);
/*     */   }
/*     */ 
/*     */   public MultiFile8BitAudioPlayer(String baseName, AudioFileFormat.Type type)
/*     */   {
/*  61 */     this.baseName = baseName;
/*  62 */     this.outputType = type;
/*     */   }
/*     */ 
/*     */   public synchronized void setAudioFormat(AudioFormat format)
/*     */   {
/*     */   }
/*     */ 
/*     */   public AudioFormat getAudioFormat()
/*     */   {
/*  84 */     return this.currentFormat;
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
/* 133 */     return 1.0F;
/*     */   }
/*     */ 
/*     */   public void setVolume(float volume)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void begin(int size)
/*     */   {
/* 152 */     this.outputData = new byte[size / 2];
/* 153 */     this.curIndex = 0;
/*     */   }
/*     */ 
/*     */   public boolean end()
/*     */   {
/* 165 */     ByteArrayInputStream bais = new ByteArrayInputStream(this.outputData);
/* 166 */     AudioInputStream ais = new AudioInputStream(bais, this.currentFormat, this.outputData.length / this.currentFormat.getFrameSize());
/*     */ 
/* 169 */     String name = this.baseName;
/* 170 */     name = name + this.fileCount;
/* 171 */     name = name + "." + this.outputType.getExtension();
/* 172 */     File file = new File(name);
/*     */     try {
/* 174 */       AudioSystem.write(ais, this.outputType, file);
/* 175 */       System.out.println("Wrote synthesized speech to " + name);
/*     */     } catch (IOException ioe) {
/* 177 */       System.err.println("Can't write audio to " + file);
/* 178 */       return false;
/*     */     } catch (IllegalArgumentException iae) {
/* 180 */       System.err.println("Can't write audio type " + this.outputType);
/* 181 */       return false;
/*     */     }
/* 183 */     this.fileCount += 1;
/* 184 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean drain()
/*     */   {
/* 195 */     return true;
/*     */   }
/*     */ 
/*     */   public synchronized long getTime()
/*     */   {
/* 204 */     return -1L;
/*     */   }
/*     */ 
/*     */   public synchronized void resetTime()
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] audioData)
/*     */   {
/* 225 */     return write(audioData, 0, audioData.length);
/*     */   }
/*     */ 
/*     */   public boolean write(byte[] bytes, int offset, int size)
/*     */   {
/* 240 */     bytes = convert16To8Bits(bytes);
/* 241 */     size /= 2;
/* 242 */     System.arraycopy(bytes, offset, this.outputData, this.curIndex, size);
/* 243 */     this.curIndex += size;
/* 244 */     return true;
/*     */   }
/*     */ 
/*     */   private static byte[] convert16To8Bits(byte[] samples16Bit)
/*     */   {
/* 257 */     byte[] samples8Bit = new byte[samples16Bit.length / 2];
/* 258 */     int i = 0; for (int j = 0; i < samples16Bit.length; ++j) {
/* 259 */       int sample = 0xFF & samples16Bit[i];
/* 260 */       samples8Bit[j] = (byte)(sample + 128);
/*     */ 
/* 258 */       i += 2;
/*     */     }
/*     */ 
/* 262 */     return samples8Bit;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 272 */     return "MultiFile8BitAudioPlayer";
/*     */   }
/*     */ 
/*     */   public void showMetrics()
/*     */   {
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.audio.MultiFile8BitAudioPlayer
 * JD-Core Version:    0.5.4
 */