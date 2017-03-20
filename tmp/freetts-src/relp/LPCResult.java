/*     */ package com.sun.speech.freetts.relp;
/*     */ 
/*     */ import com.sun.speech.freetts.FreeTTSSpeakable;
/*     */ import com.sun.speech.freetts.Utterance;
/*     */ import com.sun.speech.freetts.audio.AudioPlayer;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import com.sun.speech.freetts.util.WaveUtils;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.Writer;
/*     */ import java.text.DecimalFormat;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ 
/*     */ public class LPCResult
/*     */ {
/*     */   private static final double POST_EMPHASIS = 0.0D;
/*  36 */   private int frameSize = 10;
/*  37 */   private int numberOfFrames = 0;
/*     */ 
/*  39 */   private short[][] frames = (short[][])null;
/*  40 */   private int[] times = null;
/*  41 */   private int[] sizes = null;
/*     */ 
/*  47 */   private byte[] residuals = null;
/*     */   private int numberOfChannels;
/*     */   private int sampleRate;
/*     */   private int residualFold;
/*     */   private float lpcMinimum;
/*     */   private float lpcRange;
/*  56 */   private static final int MAX_SAMPLE_SIZE = Utilities.getInteger("com.sun.speech.freetts.LpcResult.maxSamples", 1024).intValue();
/*     */ 
/*  63 */   private static final float[] residualToFloatMap = new float[256];
/*     */ 
/*     */   public LPCResult()
/*     */   {
/*  74 */     this.residualFold = 1;
/*     */   }
/*     */ 
/*     */   public void resizeFrames(int numberOfFrames)
/*     */   {
/*  83 */     this.times = new int[numberOfFrames];
/*  84 */     this.frames = new short[numberOfFrames][];
/*  85 */     this.sizes = new int[numberOfFrames];
/*  86 */     this.numberOfFrames = numberOfFrames;
/*     */   }
/*     */ 
/*     */   public void resizeResiduals(int numberOfSamples)
/*     */   {
/*  96 */     this.residuals = new byte[numberOfSamples];
/*     */   }
/*     */ 
/*     */   public void setValues(int numberOfChannels, int sampleRate, int residualFold, float lpcMin, float lpcRange)
/*     */   {
/* 111 */     this.numberOfChannels = numberOfChannels;
/* 112 */     this.sampleRate = sampleRate;
/* 113 */     this.lpcMinimum = lpcMin;
/* 114 */     this.lpcRange = lpcRange;
/*     */   }
/*     */ 
/*     */   public int getFrameShift(int frameIndex)
/*     */   {
/* 128 */     if ((0 <= frameIndex) && (frameIndex < this.times.length)) {
/* 129 */       if (frameIndex > 0) {
/* 130 */         return this.times[frameIndex] - this.times[(frameIndex - 1)];
/*     */       }
/* 132 */       return this.times[frameIndex];
/*     */     }
/*     */ 
/* 135 */     return 0;
/*     */   }
/*     */ 
/*     */   public int getFrameSize()
/*     */   {
/* 145 */     return this.frameSize;
/*     */   }
/*     */ 
/*     */   public short[] getFrame(int index)
/*     */   {
/* 156 */     return this.frames[index];
/*     */   }
/*     */ 
/*     */   public int[] getTimes()
/*     */   {
/* 165 */     return this.times;
/*     */   }
/*     */ 
/*     */   public int getNumberOfFrames()
/*     */   {
/* 174 */     return this.numberOfFrames;
/*     */   }
/*     */ 
/*     */   public int getNumberOfChannels()
/*     */   {
/* 183 */     return this.numberOfChannels;
/*     */   }
/*     */ 
/*     */   public float getLPCMin()
/*     */   {
/* 192 */     return this.lpcMinimum;
/*     */   }
/*     */ 
/*     */   public float getLPCRange()
/*     */   {
/* 201 */     return this.lpcRange;
/*     */   }
/*     */ 
/*     */   public int getNumberOfSamples()
/*     */   {
/* 210 */     if (this.residuals == null) {
/* 211 */       return 0;
/*     */     }
/* 213 */     return this.residuals.length;
/*     */   }
/*     */ 
/*     */   public int getSampleRate()
/*     */   {
/* 223 */     return this.sampleRate;
/*     */   }
/*     */ 
/*     */   public int[] getResidualSizes()
/*     */   {
/* 232 */     return this.sizes;
/*     */   }
/*     */ 
/*     */   public byte[] getResiduals()
/*     */   {
/* 241 */     return this.residuals;
/*     */   }
/*     */ 
/*     */   public void setFrameSize(int frameSize)
/*     */   {
/* 250 */     this.frameSize = frameSize;
/*     */   }
/*     */ 
/*     */   public void setNumberOfFrames(int numberFrames)
/*     */   {
/* 259 */     this.numberOfFrames = numberFrames;
/*     */   }
/*     */ 
/*     */   public void setFrame(int index, short[] newFrames)
/*     */   {
/* 269 */     this.frames[index] = newFrames;
/*     */   }
/*     */ 
/*     */   public void setTimes(int[] times)
/*     */   {
/* 278 */     this.times = times;
/*     */   }
/*     */ 
/*     */   public void setNumberOfChannels(int numberOfChannels)
/*     */   {
/* 287 */     this.numberOfChannels = numberOfChannels;
/*     */   }
/*     */ 
/*     */   public void setLPCMin(float min)
/*     */   {
/* 296 */     this.lpcMinimum = min;
/*     */   }
/*     */ 
/*     */   public void setLPCRange(float range)
/*     */   {
/* 305 */     this.lpcRange = range;
/*     */   }
/*     */ 
/*     */   public void setSampleRate(int rate)
/*     */   {
/* 314 */     this.sampleRate = rate;
/*     */   }
/*     */ 
/*     */   public void setResidualSizes(int[] sizes)
/*     */   {
/* 323 */     for (int i = 0; (i < this.sizes.length) && (i < sizes.length); ++i)
/* 324 */       this.sizes[i] = sizes[i];
/*     */   }
/*     */ 
/*     */   public void copyResiduals(byte[] source, int targetPosition, int targetSize)
/*     */   {
/* 339 */     int unitSize = source.length;
/* 340 */     if (unitSize < targetSize) {
/* 341 */       int targetStart = (targetSize - unitSize) / 2;
/* 342 */       System.arraycopy(source, 0, this.residuals, targetPosition + targetStart, source.length);
/*     */     }
/*     */     else
/*     */     {
/* 346 */       int sourcePosition = (unitSize - targetSize) / 2;
/* 347 */       System.arraycopy(source, sourcePosition, this.residuals, targetPosition, targetSize);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void copyResidualsPulse(byte[] source, int targetPosition, int targetSize)
/*     */   {
/* 363 */     int unitSize = source.length;
/* 364 */     short sample = (short)(source[0] + 128);
/* 365 */     if (unitSize < targetSize)
/* 366 */       this.residuals[((targetSize - unitSize) / 2)] = WaveUtils.shortToUlaw(sample);
/*     */     else
/* 368 */       this.residuals[((unitSize - targetSize) / 2)] = WaveUtils.shortToUlaw(sample);
/*     */   }
/*     */ 
/*     */   private static final byte hibyte(int val)
/*     */   {
/* 381 */     return (byte)(val >>> 8);
/*     */   }
/*     */ 
/*     */   private static final byte lobyte(int val)
/*     */   {
/* 393 */     return (byte)(val & 0xFF);
/*     */   }
/*     */ 
/*     */   public boolean playWave(AudioPlayer player, Utterance utterance)
/*     */   {
/* 403 */     return playWaveSamples(player, utterance.getSpeakable(), getNumberOfSamples() * 2);
/*     */   }
/*     */ 
/*     */   public byte[] getWaveSamples()
/*     */   {
/* 410 */     return getWaveSamples(2 * getNumberOfSamples(), null);
/*     */   }
/*     */ 
/*     */   private byte[] getWaveSamples(int numberSamples, Utterance utterance)
/*     */   {
/* 425 */     int numberChannels = getNumberOfChannels();
/*     */ 
/* 427 */     float pp = 0.0F;
/*     */ 
/* 429 */     byte[] samples = new byte[numberSamples];
/* 430 */     byte[] residuals = getResiduals();
/* 431 */     int[] residualSizes = getResidualSizes();
/*     */ 
/* 433 */     FloatList outBuffer = FloatList.createList(numberChannels + 1);
/* 434 */     FloatList lpcCoefficients = FloatList.createList(numberChannels);
/*     */ 
/* 436 */     double multiplier = getLPCRange() / 65535.0D;
/* 437 */     int s = 0;
/*     */ 
/* 440 */     int r = 0; for (int i = 0; i < this.numberOfFrames; ++i)
/*     */     {
/* 443 */       short[] frame = getFrame(i);
/*     */ 
/* 445 */       FloatList lpcCoeffs = lpcCoefficients;
/* 446 */       for (int k = 0; k < numberChannels; ++k) {
/* 447 */         lpcCoeffs.value = ((float)((frame[k] + 32768.0D) * multiplier) + this.lpcMinimum);
/*     */ 
/* 449 */         lpcCoeffs = lpcCoeffs.next;
/*     */       }
/*     */ 
/* 452 */       int pmSizeSamples = residualSizes[i];
/*     */ 
/* 456 */       for (int j = 0; j < pmSizeSamples; ++r)
/*     */       {
/* 458 */         FloatList backBuffer = outBuffer.prev;
/* 459 */         float ob = residualToFloatMap[(residuals[r] + 128)];
/*     */ 
/* 461 */         lpcCoeffs = lpcCoefficients;
/*     */         do {
/* 463 */           ob += lpcCoeffs.value * backBuffer.value;
/* 464 */           backBuffer = backBuffer.prev;
/* 465 */           lpcCoeffs = lpcCoeffs.next;
/* 466 */         }while (lpcCoeffs != lpcCoefficients);
/*     */ 
/* 468 */         int sample = (int)(ob + pp * 0.0D);
/* 469 */         samples[(s++)] = hibyte(sample);
/* 470 */         samples[(s++)] = lobyte(sample);
/*     */ 
/* 473 */         outBuffer.value = (pp = ob);
/* 474 */         outBuffer = outBuffer.next;
/*     */ 
/* 456 */         ++j;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 477 */     return samples;
/*     */   }
/*     */ 
/*     */   private boolean playWaveSamples(AudioPlayer player, FreeTTSSpeakable speakable, int numberSamples)
/*     */   {
/* 489 */     boolean ok = true;
/* 490 */     int numberChannels = getNumberOfChannels();
/*     */ 
/* 492 */     float pp = 0.0F;
/*     */ 
/* 494 */     byte[] samples = new byte[MAX_SAMPLE_SIZE];
/* 495 */     byte[] residuals = getResiduals();
/* 496 */     int[] residualSizes = getResidualSizes();
/*     */ 
/* 498 */     FloatList outBuffer = FloatList.createList(numberChannels + 1);
/* 499 */     FloatList lpcCoefficients = FloatList.createList(numberChannels);
/*     */ 
/* 501 */     double multiplier = getLPCRange() / 65535.0D;
/* 502 */     int s = 0;
/*     */ 
/* 505 */     player.begin(numberSamples);
/* 506 */     int r = 0; int i = 0;
/*     */ 
/* 508 */     for (; ((ok &= !speakable.isCompleted())) && (i < this.numberOfFrames); ++i)
/*     */     {
/* 511 */       short[] frame = getFrame(i);
/*     */ 
/* 513 */       FloatList lpcCoeffs = lpcCoefficients;
/* 514 */       for (int k = 0; k < numberChannels; ++k) {
/* 515 */         lpcCoeffs.value = ((float)((frame[k] + 32768.0D) * multiplier) + this.lpcMinimum);
/*     */ 
/* 517 */         lpcCoeffs = lpcCoeffs.next;
/*     */       }
/*     */ 
/* 520 */       int pmSizeSamples = residualSizes[i];
/*     */ 
/* 524 */       for (int j = 0; j < pmSizeSamples; ++r)
/*     */       {
/* 526 */         FloatList backBuffer = outBuffer.prev;
/* 527 */         float ob = residualToFloatMap[(residuals[r] + 128)];
/*     */ 
/* 529 */         lpcCoeffs = lpcCoefficients;
/*     */         do {
/* 531 */           ob += lpcCoeffs.value * backBuffer.value;
/* 532 */           backBuffer = backBuffer.prev;
/* 533 */           lpcCoeffs = lpcCoeffs.next;
/* 534 */         }while (lpcCoeffs != lpcCoefficients);
/*     */ 
/* 536 */         int sample = (int)(ob + pp * 0.0D);
/* 537 */         samples[(s++)] = hibyte(sample);
/* 538 */         samples[(s++)] = lobyte(sample);
/*     */ 
/* 540 */         if (s >= MAX_SAMPLE_SIZE) {
/* 541 */           if (((ok &= !speakable.isCompleted())) && (!player.write(samples)))
/*     */           {
/* 543 */             ok = false;
/*     */           }
/* 545 */           s = 0;
/*     */         }
/*     */ 
/* 548 */         outBuffer.value = (pp = ob);
/* 549 */         outBuffer = outBuffer.next;
/*     */ 
/* 524 */         ++j;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 554 */     if (((ok &= !speakable.isCompleted())) && (s > 0)) {
/* 555 */       ok = player.write(samples, 0, s);
/* 556 */       s = 0;
/*     */     }
/*     */ 
/* 560 */     if ((ok &= !speakable.isCompleted())) {
/* 561 */       ok = player.end();
/*     */     }
/*     */ 
/* 564 */     return ok;
/*     */   }
/*     */ 
/*     */   public void dump()
/*     */   {
/* 571 */     dump(new OutputStreamWriter(System.out));
/*     */   }
/*     */ 
/*     */   public void dump(Writer writer)
/*     */   {
/* 580 */     DecimalFormat numberFormat = new DecimalFormat();
/* 581 */     numberFormat.setMaximumFractionDigits(6);
/* 582 */     numberFormat.setMinimumFractionDigits(6);
/* 583 */     PrintWriter pw = new PrintWriter(new BufferedWriter(writer));
/*     */ 
/* 585 */     if (getNumberOfFrames() == 0) {
/* 586 */       pw.println("# ========== LPCResult ==========");
/* 587 */       pw.println("# Num_of_Frames: " + getNumberOfFrames());
/* 588 */       pw.flush();
/* 589 */       return;
/*     */     }
/* 591 */     pw.println("========== LPCResult ==========");
/* 592 */     pw.println("Num_of_Frames: " + getNumberOfFrames());
/* 593 */     pw.println("Num_of_Channels: " + getNumberOfChannels());
/* 594 */     pw.println("Num_of_Samples: " + getNumberOfSamples());
/* 595 */     pw.println("Sample_Rate: " + this.sampleRate);
/* 596 */     pw.println("LPC_Minimum: " + numberFormat.format(this.lpcMinimum));
/* 597 */     pw.println("LPC_Range: " + numberFormat.format(this.lpcRange));
/* 598 */     pw.println("Residual_Fold: " + this.residualFold);
/* 599 */     pw.println("Post_Emphasis: " + numberFormat.format(0.0D));
/*     */ 
/* 602 */     pw.print("Times:\n");
/* 603 */     for (int i = 0; i < getNumberOfFrames(); ++i) {
/* 604 */       pw.print(this.times[i] + " ");
/*     */     }
/* 606 */     pw.print("\nFrames: ");
/* 607 */     for (i = 0; i < getNumberOfFrames(); ++i)
/*     */     {
/* 609 */       short[] frame = getFrame(i);
/* 610 */       for (int j = 0; j < frame.length; ++j) {
/* 611 */         pw.print(frame[j] + 32768 + "\n");
/*     */       }
/*     */     }
/* 614 */     pw.print("\nSizes: ");
/* 615 */     for (i = 0; i < getNumberOfFrames(); ++i) {
/* 616 */       pw.print(this.sizes[i] + " ");
/*     */     }
/* 618 */     pw.print("\nResiduals: ");
/* 619 */     for (i = 0; i < getNumberOfSamples(); ++i) {
/* 620 */       if (this.residuals[i] == 0)
/* 621 */         pw.print(255);
/*     */       else {
/* 623 */         pw.print(this.residuals[i] + 128);
/*     */       }
/* 625 */       pw.print("\n");
/* 626 */       pw.flush();
/*     */     }
/* 628 */     pw.flush();
/*     */   }
/*     */ 
/*     */   public void dumpASCII()
/*     */   {
/* 636 */     dumpASCII(new OutputStreamWriter(System.out));
/*     */   }
/*     */ 
/*     */   public void dumpASCII(String path)
/*     */     throws IOException
/*     */   {
/* 647 */     Writer writer = new FileWriter(path, true);
/* 648 */     getWave().dump(writer);
/*     */   }
/*     */ 
/*     */   private Wave getWave()
/*     */   {
/* 658 */     AudioFormat audioFormat = new AudioFormat(getSampleRate(), 16, 1, true, true);
/*     */ 
/* 662 */     return new Wave(audioFormat, getWaveSamples(getNumberOfSamples() * 2, null));
/*     */   }
/*     */ 
/*     */   public void dumpASCII(Writer writer)
/*     */   {
/* 672 */     Wave wave = getWave();
/* 673 */     wave.dump(writer);
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  66 */     for (short i = 0; i < residualToFloatMap.length; i = (short)(i + 1)) {
/*  67 */       residualToFloatMap[i] = WaveUtils.ulawToShort(i);
/*     */     }
/*  69 */     residualToFloatMap[''] = WaveUtils.ulawToShort(255);
/*     */   }
/*     */ 
/*     */   private static class Wave
/*     */   {
/*     */     public static final int DEFAULT_SAMPLE_SIZE_IN_BITS = 16;
/*     */     public static final boolean DEFAULT_SIGNED = true;
/*     */     public static final boolean DEFAULT_BIG_ENDIAN = false;
/* 700 */     private byte[] samples = null;
/* 701 */     private AudioFormat audioFormat = null;
/*     */ 
/*     */     Wave(AudioFormat audioFormat, byte[] samples)
/*     */     {
/* 710 */       this.audioFormat = audioFormat;
/* 711 */       this.samples = samples;
/*     */     }
/*     */ 
/*     */     public void dump(Writer writer)
/*     */     {
/* 720 */       PrintWriter pw = new PrintWriter(new BufferedWriter(writer));
/* 721 */       pw.println("#========== Wave ==========");
/* 722 */       pw.println("#Type: NULL");
/* 723 */       pw.println("#Sample_Rate: " + (int)this.audioFormat.getSampleRate());
/* 724 */       pw.println("#Num_of_Samples: " + this.samples.length / 2);
/* 725 */       pw.println("#Num_of_Channels: " + this.audioFormat.getChannels());
/* 726 */       if (this.samples != null) {
/* 727 */         for (int i = 0; i < this.samples.length; i += 2) {
/* 728 */           pw.println(WaveUtils.bytesToShort(this.samples[i], this.samples[(i + 1)]));
/*     */         }
/*     */       }
/*     */ 
/* 732 */       pw.flush();
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.relp.LPCResult
 * JD-Core Version:    0.5.4
 */