/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.io.InputStream;
/*     */ import java.io.PrintStream;
/*     */ import org.w3c.dom.Document;
/*     */ 
/*     */ public class FreeTTSSpeakableImpl
/*     */   implements FreeTTSSpeakable
/*     */ {
/*     */   private Document doc;
/*     */   private String text;
/*     */   private InputStream inputStream;
/*  23 */   volatile boolean completed = false;
/*  24 */   volatile boolean cancelled = false;
/*     */ 
/*     */   public FreeTTSSpeakableImpl(String text)
/*     */   {
/*  32 */     this.text = text;
/*     */   }
/*     */ 
/*     */   public FreeTTSSpeakableImpl(Document doc)
/*     */   {
/*  41 */     this.doc = doc;
/*     */   }
/*     */ 
/*     */   public FreeTTSSpeakableImpl(InputStream is)
/*     */   {
/*  50 */     this.inputStream = is;
/*     */   }
/*     */ 
/*     */   public void started()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void completed()
/*     */   {
/*  63 */     this.completed = true;
/*  64 */     super.notifyAll();
/*     */   }
/*     */ 
/*     */   public synchronized void cancelled()
/*     */   {
/*  71 */     this.completed = true;
/*  72 */     this.cancelled = true;
/*  73 */     super.notifyAll();
/*     */   }
/*     */ 
/*     */   public synchronized boolean isCompleted()
/*     */   {
/*  83 */     return this.completed;
/*     */   }
/*     */ 
/*     */   public synchronized boolean waitCompleted()
/*     */   {
/*  93 */     while (!this.completed) {
/*     */       try {
/*  95 */         super.wait();
/*     */       } catch (InterruptedException ie) {
/*  97 */         System.err.println("FreeTTSSpeakableImpl:Wait interrupted");
/*  98 */         return false;
/*     */       }
/*     */     }
/* 101 */     return !this.cancelled;
/*     */   }
/*     */ 
/*     */   public boolean isPlainText()
/*     */   {
/* 111 */     return this.text != null;
/*     */   }
/*     */ 
/*     */   public String getText()
/*     */   {
/* 120 */     return this.text;
/*     */   }
/*     */ 
/*     */   public Document getDocument()
/*     */   {
/* 129 */     return this.doc;
/*     */   }
/*     */ 
/*     */   public boolean isStream()
/*     */   {
/* 138 */     return this.inputStream != null;
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream()
/*     */   {
/* 147 */     return this.inputStream;
/*     */   }
/*     */ 
/*     */   public boolean isDocument()
/*     */   {
/* 157 */     return this.doc != null;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.FreeTTSSpeakableImpl
 * JD-Core Version:    0.5.4
 */