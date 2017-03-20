/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.net.URL;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.NoSuchElementException;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public class PartOfSpeechImpl
/*     */   implements PartOfSpeech
/*     */ {
/*  42 */   private int lineCount = 0;
/*     */   private Map partOfSpeechMap;
/*     */   private String defaultPartOfSpeech;
/*     */ 
/*     */   public PartOfSpeechImpl(URL url, String defaultPartOfSpeech)
/*     */     throws IOException
/*     */   {
/*  68 */     this.partOfSpeechMap = new HashMap();
/*  69 */     this.defaultPartOfSpeech = defaultPartOfSpeech;
/*  70 */     BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
/*     */ 
/*  72 */     String line = reader.readLine();
/*  73 */     this.lineCount += 1;
/*  74 */     while (line != null) {
/*  75 */       if (!line.startsWith("***")) {
/*  76 */         parseAndAdd(line);
/*     */       }
/*  78 */       line = reader.readLine();
/*     */     }
/*  80 */     reader.close();
/*     */   }
/*     */ 
/*     */   private void parseAndAdd(String line)
/*     */   {
/*  89 */     StringTokenizer tokenizer = new StringTokenizer(line, " ");
/*     */     try {
/*  91 */       String word = tokenizer.nextToken();
/*  92 */       String pos = tokenizer.nextToken();
/*  93 */       this.partOfSpeechMap.put(word, pos);
/*     */     } catch (NoSuchElementException nse) {
/*  95 */       System.err.println("part of speech data in bad format at line " + this.lineCount);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getPartOfSpeech(String word)
/*     */   {
/* 110 */     String pos = (String)this.partOfSpeechMap.get(word);
/* 111 */     if (pos == null) {
/* 112 */       pos = this.defaultPartOfSpeech;
/*     */     }
/* 114 */     return pos;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.PartOfSpeechImpl
 * JD-Core Version:    0.5.4
 */