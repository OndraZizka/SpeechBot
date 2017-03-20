/*     */ package com.sun.speech.freetts.relp;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ class FloatList
/*     */ {
/*     */   float value;
/*     */   FloatList next;
/*     */   FloatList prev;
/*     */ 
/*     */   FloatList()
/*     */   {
/* 755 */     this.value = 0.0F;
/* 756 */     this.next = null;
/* 757 */     this.prev = null;
/*     */   }
/*     */ 
/*     */   static FloatList createList(int size)
/*     */   {
/* 768 */     FloatList prev = null;
/* 769 */     FloatList first = null;
/*     */ 
/* 771 */     for (int i = 0; i < size; ++i) {
/* 772 */       FloatList cur = new FloatList();
/* 773 */       cur.prev = prev;
/* 774 */       if (prev == null)
/* 775 */         first = cur;
/*     */       else {
/* 777 */         prev.next = cur;
/*     */       }
/* 779 */       prev = cur;
/*     */     }
/* 781 */     first.prev = prev;
/* 782 */     prev.next = first;
/*     */ 
/* 784 */     return first;
/*     */   }
/*     */ 
/*     */   static void dump(String title, FloatList list)
/*     */   {
/* 794 */     System.out.println(title);
/*     */ 
/* 796 */     FloatList cur = list;
/*     */     do {
/* 798 */       System.out.println("Item: " + cur.value);
/* 799 */       cur = cur.next;
/* 800 */     }while (cur != list);
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.relp.FloatList
 * JD-Core Version:    0.5.4
 */