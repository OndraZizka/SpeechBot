/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import java.util.HashSet;
/*     */ import java.util.Vector;
/*     */ 
/*     */ class UniqueVector
/*     */ {
/*     */   private HashSet elementSet;
/*     */   private Vector elementVector;
/*     */ 
/*     */   public UniqueVector()
/*     */   {
/* 696 */     this.elementSet = new HashSet();
/* 697 */     this.elementVector = new Vector();
/*     */   }
/*     */ 
/*     */   public void add(Object o)
/*     */   {
/* 708 */     if (!contains(o)) {
/* 709 */       this.elementSet.add(o);
/* 710 */       this.elementVector.add(o);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addVector(UniqueVector v)
/*     */   {
/* 722 */     for (int i = 0; i < v.size(); ++i)
/* 723 */       add(v.get(i));
/*     */   }
/*     */ 
/*     */   public void addArray(Object[] a)
/*     */   {
/* 735 */     for (int i = 0; i < a.length; ++i)
/* 736 */       add(a[i]);
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/* 746 */     return this.elementVector.size();
/*     */   }
/*     */ 
/*     */   public boolean contains(Object o)
/*     */   {
/* 760 */     return this.elementSet.contains(o);
/*     */   }
/*     */ 
/*     */   public Object get(int index)
/*     */   {
/* 772 */     return this.elementVector.get(index);
/*     */   }
/*     */ 
/*     */   public Object[] toArray()
/*     */   {
/* 782 */     return this.elementVector.toArray();
/*     */   }
/*     */ 
/*     */   public Object[] toArray(Object[] a)
/*     */   {
/* 792 */     return this.elementVector.toArray(a);
/*     */   }
/*     */ 
/*     */   public Collection elements()
/*     */   {
/* 800 */     return this.elementVector;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.UniqueVector
 * JD-Core Version:    0.5.4
 */