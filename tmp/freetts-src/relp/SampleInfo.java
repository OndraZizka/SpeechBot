/*     */ package com.sun.speech.freetts.relp;
/*     */ 
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.nio.ByteBuffer;
/*     */ 
/*     */ public class SampleInfo
/*     */ {
/*     */   public static final String UTT_NAME = "SampleInfo";
/*     */   private final int sampleRate;
/*     */   private final int numberOfChannels;
/*     */   private final int residualFold;
/*     */   private final float coeffMin;
/*     */   private final float coeffRange;
/*     */   private final float postEmphasis;
/*     */ 
/*     */   public SampleInfo(int sampleRate, int numberOfChannels, int residualFold, float coeffMin, float coeffRange, float postEmphasis)
/*     */   {
/*  47 */     this.sampleRate = sampleRate;
/*  48 */     this.numberOfChannels = numberOfChannels;
/*  49 */     this.residualFold = residualFold;
/*  50 */     this.coeffMin = coeffMin;
/*  51 */     this.coeffRange = coeffRange;
/*  52 */     this.postEmphasis = postEmphasis;
/*     */   }
/*     */ 
/*     */   public SampleInfo(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/*  63 */     this.numberOfChannels = bb.getInt();
/*  64 */     this.sampleRate = bb.getInt();
/*  65 */     this.coeffMin = bb.getFloat();
/*  66 */     this.coeffRange = bb.getFloat();
/*  67 */     this.postEmphasis = bb.getFloat();
/*  68 */     this.residualFold = bb.getInt();
/*     */   }
/*     */ 
/*     */   public SampleInfo(DataInputStream is)
/*     */     throws IOException
/*     */   {
/*  79 */     this.numberOfChannels = is.readInt();
/*  80 */     this.sampleRate = is.readInt();
/*  81 */     this.coeffMin = is.readFloat();
/*  82 */     this.coeffRange = is.readFloat();
/*  83 */     this.postEmphasis = is.readFloat();
/*  84 */     this.residualFold = is.readInt();
/*     */   }
/*     */ 
/*     */   public final int getSampleRate()
/*     */   {
/*  93 */     return this.sampleRate;
/*     */   }
/*     */ 
/*     */   public final int getNumberOfChannels()
/*     */   {
/* 102 */     return this.numberOfChannels;
/*     */   }
/*     */ 
/*     */   public final int getResidualFold()
/*     */   {
/* 111 */     return this.residualFold;
/*     */   }
/*     */ 
/*     */   public final float getCoeffMin()
/*     */   {
/* 120 */     return this.coeffMin;
/*     */   }
/*     */ 
/*     */   public final float getCoeffRange()
/*     */   {
/* 129 */     return this.coeffRange;
/*     */   }
/*     */ 
/*     */   public final float getPostEmphasis()
/*     */   {
/* 138 */     return this.postEmphasis;
/*     */   }
/*     */ 
/*     */   public void dumpBinary(DataOutputStream os)
/*     */     throws IOException
/*     */   {
/* 151 */     os.writeInt(this.numberOfChannels);
/* 152 */     os.writeInt(this.sampleRate);
/* 153 */     os.writeFloat(this.coeffMin);
/* 154 */     os.writeFloat(this.coeffRange);
/* 155 */     os.writeFloat(this.postEmphasis);
/* 156 */     os.writeInt(this.residualFold);
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.relp.SampleInfo
 * JD-Core Version:    0.5.4
 */