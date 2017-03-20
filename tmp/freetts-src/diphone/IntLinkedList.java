/*     */ package com.sun.speech.freetts.diphone;
/*     */ 
/*     */ class IntLinkedList
/*     */ {
/* 137 */   private IntListNode head = null;
/* 138 */   private IntListNode tail = null;
/* 139 */   private IntListNode iterator = null;
/*     */ 
/*     */   public IntLinkedList()
/*     */   {
/* 145 */     this.head = null;
/* 146 */     this.tail = null;
/* 147 */     this.iterator = null;
/*     */   }
/*     */ 
/*     */   public void add(int val)
/*     */   {
/* 156 */     IntListNode node = new IntListNode(val);
/* 157 */     if (this.head == null) {
/* 158 */       this.head = node;
/* 159 */       this.tail = node;
/*     */     } else {
/* 161 */       this.tail.next = node;
/* 162 */       this.tail = node;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void resetIterator()
/*     */   {
/* 170 */     this.iterator = this.head;
/*     */   }
/*     */ 
/*     */   public int nextInt()
/*     */   {
/* 182 */     int val = this.iterator.val;
/* 183 */     if (this.iterator != null) {
/* 184 */       this.iterator = this.iterator.next;
/*     */     }
/* 186 */     return val;
/*     */   }
/*     */ 
/*     */   public boolean hasNext()
/*     */   {
/* 196 */     return this.iterator != null;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.diphone.IntLinkedList
 * JD-Core Version:    0.5.4
 */