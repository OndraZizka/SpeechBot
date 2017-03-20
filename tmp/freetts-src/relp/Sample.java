/*     */ package com.sun.speech.freetts.relp;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.util.NoSuchElementException;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public class Sample
/*     */ {
/*     */   private final short[] frameData;
/*     */   private final byte[] residualData;
/*     */   private final int residualSize;
/*     */ 
/*     */   public Sample(short[] frameData, byte[] residualData)
/*     */   {
/*  38 */     this.frameData = frameData;
/*  39 */     this.residualData = residualData;
/*  40 */     this.residualSize = 0;
/*     */   }
/*     */ 
/*     */   public Sample(short[] frameData, byte[] residualData, int residualSize)
/*     */   {
/*  50 */     this.frameData = frameData;
/*  51 */     this.residualData = residualData;
/*  52 */     this.residualSize = residualSize;
/*     */   }
/*     */ 
/*     */   public Sample(BufferedReader reader, int numChannels)
/*     */   {
/*     */     try
/*     */     {
/*  63 */       String line = reader.readLine();
/*     */ 
/*  65 */       StringTokenizer tok = new StringTokenizer(line);
/*  66 */       if (!tok.nextToken().equals("FRAME")) {
/*  67 */         throw new Error("frame Parsing sample error");
/*     */       }
/*     */ 
/*  70 */       this.frameData = new short[numChannels];
/*     */ 
/*  72 */       for (int i = 0; i < numChannels; ++i) {
/*  73 */         int svalue = Integer.parseInt(tok.nextToken()) - 32768;
/*     */ 
/*  75 */         if ((svalue < -32768) || (svalue > 32767)) {
/*  76 */           throw new Error("data out of short range");
/*     */         }
/*  78 */         this.frameData[i] = (short)svalue;
/*     */       }
/*     */ 
/*  81 */       line = reader.readLine();
/*  82 */       tok = new StringTokenizer(line);
/*  83 */       if (!tok.nextToken().equals("RESIDUAL")) {
/*  84 */         throw new Error("residual Parsing sample error");
/*     */       }
/*     */ 
/*  87 */       this.residualSize = Integer.parseInt(tok.nextToken());
/*  88 */       this.residualData = new byte[this.residualSize];
/*     */ 
/*  90 */       for (int i = 0; i < this.residualSize; ++i) {
/*  91 */         int bvalue = Integer.parseInt(tok.nextToken()) - 128;
/*     */ 
/*  93 */         if ((bvalue < -128) || (bvalue > 127)) {
/*  94 */           throw new Error("data out of byte range");
/*     */         }
/*  96 */         this.residualData[i] = (byte)bvalue;
/*     */       }
/*     */     } catch (NoSuchElementException nse) {
/*  99 */       throw new Error("Parsing sample error " + nse.getMessage());
/*     */     } catch (IOException ioe) {
/* 101 */       throw new Error("IO error while parsing sample" + ioe.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   public short[] getFrameData()
/*     */   {
/* 111 */     return this.frameData;
/*     */   }
/*     */ 
/*     */   public byte[] getResidualData()
/*     */   {
/* 120 */     return this.residualData;
/*     */   }
/*     */ 
/*     */   public int getResidualSize()
/*     */   {
/* 129 */     return this.residualSize;
/*     */   }
/*     */ 
/*     */   public int getResidualData(int which)
/*     */   {
/* 142 */     return this.residualData[which] + 128;
/*     */   }
/*     */ 
/*     */   public int getFrameData(int which)
/*     */   {
/* 154 */     return this.frameData[which] + 32768;
/*     */   }
/*     */ 
/*     */   public void dump()
/*     */   {
/* 162 */     System.out.println(" FD Count: " + getFrameData().length);
/* 163 */     for (int i = 0; i < getFrameData().length; ++i) {
/* 164 */       System.out.print(" " + getFrameData(i));
/*     */     }
/* 166 */     System.out.println();
/* 167 */     System.out.println(" RD Count: " + getResidualSize());
/*     */ 
/* 169 */     for (int i = 0; i < getResidualData().length; ++i) {
/* 170 */       System.out.print(" " + getResidualData(i));
/*     */     }
/* 172 */     System.out.println();
/*     */   }
/*     */ 
/*     */   public void dumpBinary(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 183 */     bb.putInt(this.frameData.length);
/* 184 */     for (int i = 0; i < this.frameData.length; ++i) {
/* 185 */       bb.putShort(this.frameData[i]);
/*     */     }
/* 187 */     bb.putInt(this.residualData.length);
/* 188 */     bb.put(this.residualData);
/*     */   }
/*     */ 
/*     */   public void dumpBinary(DataOutputStream os)
/*     */     throws IOException
/*     */   {
/* 199 */     os.writeInt(this.frameData.length);
/* 200 */     for (int i = 0; i < this.frameData.length; ++i) {
/* 201 */       os.writeShort(this.frameData[i]);
/*     */     }
/* 203 */     os.writeInt(this.residualData.length);
/* 204 */     for (int i = 0; i < this.residualData.length; ++i)
/* 205 */       os.writeByte(this.residualData[i]);
/*     */   }
/*     */ 
/*     */   public static Sample loadBinary(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 217 */     int frameDataSize = bb.getInt();
/*     */ 
/* 219 */     short[] frameData = new short[frameDataSize];
/*     */ 
/* 221 */     for (int i = 0; i < frameData.length; ++i) {
/* 222 */       frameData[i] = bb.getShort();
/*     */     }
/*     */ 
/* 225 */     int residualDataSize = bb.getInt();
/* 226 */     byte[] residualData = new byte[residualDataSize];
/*     */ 
/* 228 */     for (int i = 0; i < residualData.length; ++i) {
/* 229 */       residualData[i] = bb.get();
/*     */     }
/*     */ 
/* 232 */     return new Sample(frameData, residualData, residualDataSize);
/*     */   }
/*     */ 
/*     */   public static Sample loadBinary(DataInputStream dis)
/*     */     throws IOException
/*     */   {
/* 244 */     int frameDataSize = dis.readInt();
/*     */ 
/* 246 */     short[] frameData = new short[frameDataSize];
/*     */ 
/* 248 */     for (int i = 0; i < frameData.length; ++i) {
/* 249 */       frameData[i] = dis.readShort();
/*     */     }
/*     */ 
/* 252 */     int residualDataSize = dis.readInt();
/* 253 */     byte[] residualData = new byte[residualDataSize];
/*     */ 
/* 255 */     for (int i = 0; i < residualData.length; ++i) {
/* 256 */       residualData[i] = dis.readByte();
/*     */     }
/*     */ 
/* 259 */     return new Sample(frameData, residualData, residualDataSize);
/*     */   }
/*     */ 
/*     */   public boolean compare(Sample other)
/*     */   {
/* 273 */     if (this.frameData.length != other.getFrameData().length) {
/* 274 */       return false;
/*     */     }
/*     */ 
/* 277 */     for (int i = 0; i < this.frameData.length; ++i) {
/* 278 */       if (this.frameData[i] != other.frameData[i]) {
/* 279 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 283 */     if (this.residualData.length != other.residualData.length) {
/* 284 */       return false;
/*     */     }
/*     */ 
/* 287 */     for (int i = 0; i < this.residualData.length; ++i) {
/* 288 */       if (this.residualData[i] != other.residualData[i]) {
/* 289 */         return false;
/*     */       }
/*     */     }
/* 292 */     return true;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.relp.Sample
 * JD-Core Version:    0.5.4
 */