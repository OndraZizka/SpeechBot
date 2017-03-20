/*     */ package com.sun.speech.freetts.relp;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.util.NoSuchElementException;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public class SampleSet
/*     */ {
/*     */   private Sample[] samples;
/*     */   private SampleInfo sampleInfo;
/*     */ 
/*     */   public SampleSet(StringTokenizer tok, BufferedReader reader)
/*     */   {
/*     */     try
/*     */     {
/*  44 */       int numSamples = Integer.parseInt(tok.nextToken());
/*  45 */       int numChannels = Integer.parseInt(tok.nextToken());
/*  46 */       int sampleRate = Integer.parseInt(tok.nextToken());
/*  47 */       float coeffMin = Float.parseFloat(tok.nextToken());
/*  48 */       float coeffRange = Float.parseFloat(tok.nextToken());
/*  49 */       float postEmphasis = Float.parseFloat(tok.nextToken());
/*  50 */       int residualFold = Integer.parseInt(tok.nextToken());
/*     */ 
/*  52 */       this.samples = new Sample[numSamples];
/*  53 */       this.sampleInfo = new SampleInfo(sampleRate, numChannels, residualFold, coeffMin, coeffRange, postEmphasis);
/*     */ 
/*  56 */       for (int i = 0; i < numSamples; ++i)
/*  57 */         this.samples[i] = new Sample(reader, numChannels);
/*     */     }
/*     */     catch (NoSuchElementException nse) {
/*  60 */       throw new Error("Parsing sample error " + nse.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   public SampleSet(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/*  73 */     this.sampleInfo = new SampleInfo(bb);
/*  74 */     int numSamples = bb.getInt();
/*  75 */     this.samples = new Sample[numSamples];
/*  76 */     for (int i = 0; i < numSamples; ++i)
/*  77 */       this.samples[i] = Sample.loadBinary(bb);
/*     */   }
/*     */ 
/*     */   public SampleSet(DataInputStream is)
/*     */     throws IOException
/*     */   {
/*  90 */     this.sampleInfo = new SampleInfo(is);
/*  91 */     int numSamples = is.readInt();
/*  92 */     this.samples = new Sample[numSamples];
/*  93 */     for (int i = 0; i < numSamples; ++i)
/*  94 */       this.samples[i] = Sample.loadBinary(is);
/*     */   }
/*     */ 
/*     */   public void dumpBinary(DataOutputStream os)
/*     */     throws IOException
/*     */   {
/* 106 */     this.sampleInfo.dumpBinary(os);
/* 107 */     os.writeInt(this.samples.length);
/* 108 */     for (int i = 0; i < this.samples.length; ++i)
/* 109 */       this.samples[i].dumpBinary(os);
/*     */   }
/*     */ 
/*     */   public Sample getSample(int index)
/*     */   {
/* 122 */     return this.samples[index];
/*     */   }
/*     */ 
/*     */   public SampleInfo getSampleInfo()
/*     */   {
/* 131 */     return this.sampleInfo;
/*     */   }
/*     */ 
/*     */   public int getUnitSize(int start, int end)
/*     */   {
/* 145 */     int size = 0;
/*     */ 
/* 147 */     for (int i = start; i < end; ++i) {
/* 148 */       size += getFrameSize(i);
/*     */     }
/* 150 */     return size;
/*     */   }
/*     */ 
/*     */   public int getFrameSize(int frame)
/*     */   {
/* 162 */     return this.samples[frame].getResidualSize();
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.relp.SampleSet
 * JD-Core Version:    0.5.4
 */