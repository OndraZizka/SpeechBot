/*     */ package com.sun.speech.freetts.diphone;
/*     */ 
/*     */ import com.sun.speech.freetts.Unit;
/*     */ import com.sun.speech.freetts.relp.Sample;
/*     */ 
/*     */ class DiphoneUnit
/*     */   implements Unit
/*     */ {
/*     */   private Diphone diphone;
/*     */   private int unitPart;
/*     */ 
/*     */   public DiphoneUnit(Diphone diphone, int unitPart)
/*     */   {
/* 187 */     this.diphone = diphone;
/* 188 */     this.unitPart = unitPart;
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 197 */     return this.diphone.getName();
/*     */   }
/*     */ 
/*     */   public int getSize()
/*     */   {
/* 206 */     return this.diphone.getUnitSize(this.unitPart);
/*     */   }
/*     */ 
/*     */   public Sample getNearestSample(float index)
/*     */   {
/* 217 */     return this.diphone.nearestSample(index, this.unitPart);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 226 */     return getName();
/*     */   }
/*     */ 
/*     */   public void dump()
/*     */   {
/* 234 */     this.diphone.dump();
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.diphone.DiphoneUnit
 * JD-Core Version:    0.5.4
 */