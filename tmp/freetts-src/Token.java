/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ public class Token
/*     */ {
/*     */   private String token;
/*     */   private String whitespace;
/*     */   private String prepunctuation;
/*     */   private String postpunctuation;
/*     */   private int position;
/*     */   private int lineNumber;
/*     */ 
/*     */   public Token()
/*     */   {
/*  18 */     this.token = null;
/*  19 */     this.whitespace = null;
/*  20 */     this.prepunctuation = null;
/*  21 */     this.postpunctuation = null;
/*  22 */     this.position = 0;
/*  23 */     this.lineNumber = 0;
/*     */   }
/*     */ 
/*     */   public String getWhitespace()
/*     */   {
/*  32 */     return this.whitespace;
/*     */   }
/*     */ 
/*     */   public String getPrepunctuation()
/*     */   {
/*  42 */     return this.prepunctuation;
/*     */   }
/*     */ 
/*     */   public String getPostpunctuation()
/*     */   {
/*  52 */     return this.postpunctuation;
/*     */   }
/*     */ 
/*     */   public int getPosition()
/*     */   {
/*  61 */     return this.position;
/*     */   }
/*     */ 
/*     */   public int getLineNumber()
/*     */   {
/*  70 */     return this.lineNumber;
/*     */   }
/*     */ 
/*     */   public void setWhitespace(String whitespace)
/*     */   {
/*  79 */     this.whitespace = whitespace;
/*     */   }
/*     */ 
/*     */   public void setPrepunctuation(String prepunctuation)
/*     */   {
/*  88 */     this.prepunctuation = prepunctuation;
/*     */   }
/*     */ 
/*     */   public void setPostpunctuation(String postpunctuation)
/*     */   {
/*  97 */     this.postpunctuation = postpunctuation;
/*     */   }
/*     */ 
/*     */   public void setPosition(int position)
/*     */   {
/* 106 */     this.position = position;
/*     */   }
/*     */ 
/*     */   public void setLineNumber(int lineNumber)
/*     */   {
/* 115 */     this.lineNumber = lineNumber;
/*     */   }
/*     */ 
/*     */   public String getWord()
/*     */   {
/* 124 */     return this.token;
/*     */   }
/*     */ 
/*     */   public void setWord(String word)
/*     */   {
/* 133 */     this.token = word;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 142 */     StringBuffer fullToken = new StringBuffer();
/*     */ 
/* 144 */     if (this.whitespace != null) {
/* 145 */       fullToken.append(this.whitespace);
/*     */     }
/* 147 */     if (this.prepunctuation != null) {
/* 148 */       fullToken.append(this.prepunctuation);
/*     */     }
/* 150 */     if (this.token != null) {
/* 151 */       fullToken.append(this.token);
/*     */     }
/* 153 */     if (this.postpunctuation != null) {
/* 154 */       fullToken.append(this.postpunctuation);
/*     */     }
/* 156 */     return fullToken.toString();
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Token
 * JD-Core Version:    0.5.4
 */