/*     */ package com.sun.speech.freetts.diphone;
/*     */ 
/*     */ import com.sun.speech.freetts.relp.Sample;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.nio.ByteBuffer;
/*     */ 
/*     */ public class Diphone
/*     */ {
/*     */   protected static final int MAGIC = 262988031;
/*     */   protected static final int ALIAS_MAGIC = 195800832;
/*     */   protected static final int NAME_LENGTH = 8;
/*     */   private String name;
/*     */   private int midPoint;
/*     */   private Sample[] samples;
/*     */   private int unitSizePart1;
/*     */   private int unitSizePart2;
/*     */ 
/*     */   public Diphone(String name, Sample[] samples, int midPoint)
/*     */   {
/*  43 */     this.name = name;
/*  44 */     this.midPoint = midPoint;
/*  45 */     this.samples = samples;
/*  46 */     this.unitSizePart1 = 0;
/*  47 */     this.unitSizePart2 = 0;
/*     */ 
/*  49 */     for (int i = 0; i < midPoint; ++i) {
/*  50 */       this.unitSizePart1 += samples[i].getResidualSize();
/*     */     }
/*  52 */     for (int i = midPoint; i < samples.length; ++i)
/*  53 */       this.unitSizePart2 += samples[i].getResidualSize();
/*     */   }
/*     */ 
/*     */   protected Diphone(String name)
/*     */   {
/*  64 */     this.name = name;
/*  65 */     this.midPoint = 0;
/*  66 */     this.samples = null;
/*  67 */     this.unitSizePart1 = 0;
/*  68 */     this.unitSizePart2 = 0;
/*     */   }
/*     */ 
/*     */   public Sample[] getSamples()
/*     */   {
/*  77 */     return this.samples;
/*     */   }
/*     */ 
/*     */   public Sample getSamples(int which)
/*     */   {
/*  88 */     return this.samples[which];
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/*  97 */     return this.name;
/*     */   }
/*     */ 
/*     */   public int getMidPoint()
/*     */   {
/* 108 */     return this.midPoint;
/*     */   }
/*     */ 
/*     */   public int getPbPositionMillis()
/*     */   {
/* 118 */     return getMidPoint();
/*     */   }
/*     */ 
/*     */   public Sample nearestSample(float uIndex, int unitPart)
/*     */   {
/* 132 */     int iSize = 0;
/*     */ 
/* 134 */     int start = (unitPart == 1) ? 0 : this.midPoint;
/* 135 */     int end = (unitPart == 1) ? this.midPoint : this.samples.length;
/*     */ 
/* 137 */     for (int i = start; i < end; ++i) {
/* 138 */       int nSize = iSize + this.samples[i].getResidualSize();
/*     */ 
/* 140 */       if (Math.abs(uIndex - iSize) < Math.abs(uIndex - nSize))
/*     */       {
/* 142 */         return this.samples[i];
/*     */       }
/* 144 */       iSize = nSize;
/*     */     }
/* 146 */     return this.samples[(end - 1)];
/*     */   }
/*     */ 
/*     */   public int getUnitSize(int unitPart)
/*     */   {
/* 158 */     if (unitPart == 1) {
/* 159 */       return this.unitSizePart1;
/*     */     }
/* 161 */     return this.unitSizePart2;
/*     */   }
/*     */ 
/*     */   public void dump()
/*     */   {
/* 169 */     System.out.println("Diphone: " + this.name);
/* 170 */     System.out.println("    MP : " + this.midPoint);
/* 171 */     for (int i = 0; i < this.samples.length; ++i)
/* 172 */       this.samples[i].dump();
/*     */   }
/*     */ 
/*     */   public void dumpBinary(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 184 */     char[] nameArray = (this.name + "        ").toCharArray();
/*     */ 
/* 186 */     bb.putInt(262988031);
/* 187 */     for (int i = 0; i < 8; ++i) {
/* 188 */       bb.putChar(nameArray[i]);
/*     */     }
/* 190 */     bb.putInt(this.midPoint);
/* 191 */     bb.putInt(this.samples.length);
/*     */ 
/* 193 */     for (int i = 0; i < this.samples.length; ++i)
/* 194 */       this.samples[i].dumpBinary(bb);
/*     */   }
/*     */ 
/*     */   public void dumpBinary(DataOutputStream os)
/*     */     throws IOException
/*     */   {
/* 206 */     char[] nameArray = (this.name + "        ").toCharArray();
/*     */ 
/* 208 */     os.writeInt(262988031);
/* 209 */     for (int i = 0; i < 8; ++i) {
/* 210 */       os.writeChar(nameArray[i]);
/*     */     }
/* 212 */     os.writeInt(this.midPoint);
/* 213 */     os.writeInt(this.samples.length);
/*     */ 
/* 215 */     for (int i = 0; i < this.samples.length; ++i)
/* 216 */       this.samples[i].dumpBinary(os);
/*     */   }
/*     */ 
/*     */   boolean compare(Diphone other)
/*     */   {
/* 230 */     if (!this.name.equals(other.getName())) {
/* 231 */       return false;
/*     */     }
/*     */ 
/* 234 */     if (this.midPoint != other.getMidPoint()) {
/* 235 */       return false;
/*     */     }
/*     */ 
/* 238 */     if (this.samples.length != other.getSamples().length) {
/* 239 */       return false;
/*     */     }
/*     */ 
/* 242 */     for (int i = 0; i < this.samples.length; ++i) {
/* 243 */       if (!this.samples[i].compare(other.getSamples(i))) {
/* 244 */         return false;
/*     */       }
/*     */     }
/* 247 */     return true;
/*     */   }
/*     */ 
/*     */   public static Diphone loadBinary(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 260 */     StringBuffer sb = new StringBuffer();
/*     */ 
/* 265 */     int magic = bb.getInt();
/* 266 */     if (magic == 195800832) {
/* 267 */       for (int i = 0; i < 8; ++i) {
/* 268 */         char c = bb.getChar();
/* 269 */         if (!Character.isWhitespace(c)) {
/* 270 */           sb.append(c);
/*     */         }
/*     */       }
/* 273 */       String name = sb.toString().trim();
/* 274 */       sb.setLength(0);
/* 275 */       for (int i = 0; i < 8; ++i) {
/* 276 */         char c = bb.getChar();
/* 277 */         if (!Character.isWhitespace(c)) {
/* 278 */           sb.append(c);
/*     */         }
/*     */       }
/* 281 */       String origName = sb.toString().trim();
/* 282 */       return new AliasDiphone(name, origName);
/* 283 */     }if (magic != 262988031) {
/* 284 */       throw new Error("Bad magic number in diphone");
/*     */     }
/*     */ 
/* 287 */     for (int i = 0; i < 8; ++i) {
/* 288 */       char c = bb.getChar();
/* 289 */       if (!Character.isWhitespace(c)) {
/* 290 */         sb.append(c);
/*     */       }
/*     */     }
/*     */ 
/* 294 */     int midPoint = bb.getInt();
/* 295 */     int numSamples = bb.getInt();
/*     */ 
/* 297 */     Sample[] samples = new Sample[numSamples];
/* 298 */     for (int i = 0; i < numSamples; ++i) {
/* 299 */       samples[i] = Sample.loadBinary(bb);
/*     */     }
/* 301 */     return new Diphone(sb.toString().trim(), samples, midPoint);
/*     */   }
/*     */ 
/*     */   public static Diphone loadBinary(DataInputStream dis)
/*     */     throws IOException
/*     */   {
/* 314 */     StringBuffer sb = new StringBuffer();
/*     */ 
/* 319 */     int magic = dis.readInt();
/* 320 */     if (magic == 195800832) {
/* 321 */       for (int i = 0; i < 8; ++i) {
/* 322 */         char c = dis.readChar();
/* 323 */         if (!Character.isWhitespace(c)) {
/* 324 */           sb.append(c);
/*     */         }
/*     */       }
/* 327 */       String name = sb.toString().trim();
/* 328 */       sb.setLength(0);
/* 329 */       for (int i = 0; i < 8; ++i) {
/* 330 */         char c = dis.readChar();
/* 331 */         if (!Character.isWhitespace(c)) {
/* 332 */           sb.append(c);
/*     */         }
/*     */       }
/* 335 */       String origName = sb.toString().trim();
/* 336 */       return new AliasDiphone(name, origName);
/* 337 */     }if (magic != 262988031) {
/* 338 */       throw new Error("Bad magic number in diphone");
/*     */     }
/*     */ 
/* 341 */     for (int i = 0; i < 8; ++i) {
/* 342 */       char c = dis.readChar();
/* 343 */       if (!Character.isWhitespace(c)) {
/* 344 */         sb.append(c);
/*     */       }
/*     */     }
/*     */ 
/* 348 */     int midPoint = dis.readInt();
/* 349 */     int numSamples = dis.readInt();
/*     */ 
/* 351 */     Sample[] samples = new Sample[numSamples];
/* 352 */     for (int i = 0; i < numSamples; ++i) {
/* 353 */       samples[i] = Sample.loadBinary(dis);
/*     */     }
/* 355 */     return new Diphone(sb.toString().trim(), samples, midPoint);
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.diphone.Diphone
 * JD-Core Version:    0.5.4
 */