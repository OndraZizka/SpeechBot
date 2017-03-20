/*     */ package com.sun.speech.freetts.diphone;
/*     */ 
/*     */ import com.sun.speech.freetts.relp.Sample;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.nio.ByteBuffer;
/*     */ 
/*     */ public class AliasDiphone extends Diphone
/*     */ {
/*     */   private String originalName;
/*     */   private Diphone original;
/*     */ 
/*     */   public AliasDiphone(String name, String originalName)
/*     */   {
/*  29 */     super(name);
/*  30 */     this.originalName = originalName;
/*     */   }
/*     */ 
/*     */   public String getOriginalName()
/*     */   {
/*  38 */     return this.originalName;
/*     */   }
/*     */ 
/*     */   public void setOriginalDiphone(Diphone original)
/*     */   {
/*  51 */     if (!this.originalName.equals(original.getName())) {
/*  52 */       throw new IllegalArgumentException("The diphone to register (" + original.getName() + ") does not match the original name (" + this.originalName + ")");
/*     */     }
/*     */ 
/*  55 */     this.original = original;
/*     */   }
/*     */ 
/*     */   public Sample[] getSamples()
/*     */   {
/*  64 */     return this.original.getSamples();
/*     */   }
/*     */ 
/*     */   public Sample getSamples(int which)
/*     */   {
/*  75 */     return this.original.getSamples(which);
/*     */   }
/*     */ 
/*     */   public int getMidPoint()
/*     */   {
/*  85 */     return this.original.getMidPoint();
/*     */   }
/*     */ 
/*     */   public int getPbPositionMillis()
/*     */   {
/*  95 */     return this.original.getPbPositionMillis();
/*     */   }
/*     */ 
/*     */   public Sample nearestSample(float uIndex, int unitPart)
/*     */   {
/* 109 */     return this.original.nearestSample(uIndex, unitPart);
/*     */   }
/*     */ 
/*     */   public int getUnitSize(int unitPart)
/*     */   {
/* 121 */     return this.original.getUnitSize(unitPart);
/*     */   }
/*     */ 
/*     */   public void dump()
/*     */   {
/* 128 */     System.out.println("AliasDiphone: " + getName() + " aliased to " + this.original.getName());
/*     */   }
/*     */ 
/*     */   public void dumpBinary(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 139 */     char[] nameArray = (getName() + "        ").toCharArray();
/* 140 */     char[] origNameArray = (this.original.getName() + "        ").toCharArray();
/*     */ 
/* 142 */     bb.putInt(195800832);
/* 143 */     for (int i = 0; i < 8; ++i) {
/* 144 */       bb.putChar(nameArray[i]);
/*     */     }
/* 146 */     for (int i = 0; i < 8; ++i)
/* 147 */       bb.putChar(origNameArray[i]);
/*     */   }
/*     */ 
/*     */   public void dumpBinary(DataOutputStream os)
/*     */     throws IOException
/*     */   {
/* 159 */     char[] nameArray = (getName() + "        ").toCharArray();
/* 160 */     char[] origNameArray = (this.original.getName() + "        ").toCharArray();
/*     */ 
/* 162 */     os.writeInt(195800832);
/* 163 */     for (int i = 0; i < 8; ++i) {
/* 164 */       os.writeChar(nameArray[i]);
/*     */     }
/* 166 */     for (int i = 0; i < 8; ++i)
/* 167 */       os.writeChar(origNameArray[i]);
/*     */   }
/*     */ 
/*     */   boolean compare(Diphone other)
/*     */   {
/* 184 */     return this.original.compare(other);
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.diphone.AliasDiphone
 * JD-Core Version:    0.5.4
 */