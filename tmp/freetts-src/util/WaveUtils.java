/*     */ package com.sun.speech.freetts.util;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ public class WaveUtils
/*     */ {
/*  18 */   private static int[] exp_lut2 = { 0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };
/*     */   private static final boolean ZEROTRAP = true;
/*     */   private static final int CLIP = 32625;
/*     */   private static final int BIAS = 132;
/*  39 */   private static final int[] expLut = { 0, 132, 396, 924, 1980, 4092, 8316, 16764 };
/*     */ 
/*     */   public static final byte shortToUlaw(short sampleData)
/*     */   {
/*  52 */     int sample = sampleData;
/*     */ 
/*  54 */     int sign = sample >> 8 & 0x80;
/*  55 */     if (sign != 0) {
/*  56 */       sample = -sample;
/*     */     }
/*  58 */     if (sample > 32625) {
/*  59 */       sample = 32625;
/*     */     }
/*  61 */     sample += 132;
/*  62 */     int exponent = exp_lut2[(sample >> 7 & 0xFF)];
/*  63 */     int mantissa = sample >> exponent + 3 & 0xF;
/*  64 */     int ulawByte = (sign | exponent << 4 | mantissa) ^ 0xFFFFFFFF;
/*     */ 
/*  67 */     if (ulawByte == 0) {
/*  68 */       ulawByte = 2;
/*     */     }
/*     */ 
/*  72 */     return (byte)(ulawByte - 128 & 0xFF);
/*     */   }
/*     */ 
/*     */   public static final short ulawToShort(short uByte)
/*     */   {
/*  97 */     int ulawByte = uByte;
/*     */ 
/*  99 */     ulawByte ^= -1;
/* 100 */     int sign = ulawByte & 0x80;
/* 101 */     int exponent = ulawByte >> 4 & 0x7;
/* 102 */     int mantissa = ulawByte & 0xF;
/* 103 */     int sample = expLut[exponent] + (mantissa << exponent + 3);
/* 104 */     if (sign != 0) {
/* 105 */       sample = -sample;
/*     */     }
/*     */ 
/* 108 */     return (short)sample;
/*     */   }
/*     */ 
/*     */   public static final short bytesToShort(byte hiByte, byte loByte)
/*     */   {
/* 120 */     int result = 0xFF & hiByte;
/* 121 */     result <<= 8;
/* 122 */     result |= 0xFF & loByte;
/* 123 */     return (short)result;
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 132 */     for (int i = 0; i < 256; ++i)
/* 133 */       System.out.println("" + i + "=" + ulawToShort((short)i));
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.util.WaveUtils
 * JD-Core Version:    0.5.4
 */