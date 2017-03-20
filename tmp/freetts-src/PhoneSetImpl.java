/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.net.URL;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.NoSuchElementException;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public class PhoneSetImpl
/*     */   implements PhoneSet
/*     */ {
/*  43 */   private int lineCount = 0;
/*     */   private Map phonesetMap;
/*     */ 
/*     */   public PhoneSetImpl(URL url)
/*     */     throws IOException
/*     */   {
/*  62 */     this.phonesetMap = new HashMap();
/*  63 */     BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
/*     */ 
/*  65 */     String line = reader.readLine();
/*  66 */     this.lineCount += 1;
/*  67 */     while (line != null) {
/*  68 */       if (!line.startsWith("***")) {
/*  69 */         parseAndAdd(line);
/*     */       }
/*  71 */       line = reader.readLine();
/*     */     }
/*  73 */     reader.close();
/*     */   }
/*     */ 
/*     */   private void parseAndAdd(String line)
/*     */   {
/*  82 */     StringTokenizer tokenizer = new StringTokenizer(line, " ");
/*     */     try {
/*  84 */       String phoneme = tokenizer.nextToken();
/*  85 */       String feature = tokenizer.nextToken();
/*  86 */       String value = tokenizer.nextToken();
/*  87 */       this.phonesetMap.put(getKey(phoneme, feature), value);
/*     */     } catch (NoSuchElementException nse) {
/*  89 */       throw new Error("part of speech data in bad format at line " + this.lineCount);
/*     */     }
/*     */   }
/*     */ 
/*     */   private String getKey(String phoneme, String feature)
/*     */   {
/* 104 */     return phoneme + feature;
/*     */   }
/*     */ 
/*     */   public String getPhoneFeature(String phone, String featureName)
/*     */   {
/* 116 */     return (String)this.phonesetMap.get(getKey(phone, featureName));
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.PhoneSetImpl
 * JD-Core Version:    0.5.4
 */