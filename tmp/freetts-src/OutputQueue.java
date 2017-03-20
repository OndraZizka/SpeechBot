/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.util.LinkedList;
/*     */ 
/*     */ public class OutputQueue
/*     */ {
/*  22 */   private LinkedList list = new LinkedList();
/*     */   private int size;
/*     */   private static final int DEFAULT_SIZE = 5;
/*  25 */   private volatile boolean closed = false;
/*     */ 
/*     */   public OutputQueue(int size)
/*     */   {
/*  33 */     this.size = size;
/*     */   }
/*     */ 
/*     */   public OutputQueue()
/*     */   {
/*  40 */     this(5);
/*     */   }
/*     */ 
/*     */   public synchronized void post(Utterance utterance)
/*     */   {
/*  52 */     if (this.closed) {
/*  53 */       throw new IllegalStateException("output queue closed");
/*     */     }
/*     */ 
/*  56 */     while (this.list.size() >= this.size)
/*     */       try {
/*  58 */         super.wait();
/*     */       }
/*     */       catch (InterruptedException ie)
/*     */       {
/*     */       }
/*  63 */     this.list.add(utterance);
/*  64 */     super.notify();
/*     */   }
/*     */ 
/*     */   public synchronized void close()
/*     */   {
/*  72 */     this.closed = true;
/*  73 */     this.list.add(null);
/*  74 */     super.notify();
/*     */   }
/*     */ 
/*     */   public boolean isClosed()
/*     */   {
/*  84 */     return this.closed;
/*     */   }
/*     */ 
/*     */   public synchronized Utterance pend()
/*     */   {
/*  94 */     Utterance utterance = null;
/*  95 */     while (this.list.size() == 0) {
/*     */       try {
/*  97 */         super.wait();
/*     */       } catch (InterruptedException ie) {
/*  99 */         return null;
/*     */       }
/*     */     }
/* 102 */     utterance = (Utterance)this.list.removeFirst();
/* 103 */     super.notify();
/* 104 */     return utterance;
/*     */   }
/*     */ 
/*     */   public synchronized void removeAll()
/*     */   {
/* 111 */     this.list.clear();
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.OutputQueue
 * JD-Core Version:    0.5.4
 */