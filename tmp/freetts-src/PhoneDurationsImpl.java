/*    */ package com.sun.speech.freetts;
/*    */ 
/*    */ import java.io.BufferedReader;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStreamReader;
/*    */ import java.net.URL;
/*    */ import java.util.HashMap;
/*    */ import java.util.StringTokenizer;
/*    */ 
/*    */ public class PhoneDurationsImpl
/*    */   implements PhoneDurations
/*    */ {
/*    */   private HashMap phoneDurations;
/*    */ 
/*    */   public PhoneDurationsImpl(URL url)
/*    */     throws IOException
/*    */   {
/* 54 */     this.phoneDurations = new HashMap();
/* 55 */     BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
/*    */ 
/* 57 */     String line = reader.readLine();
/* 58 */     while (line != null) {
/* 59 */       if (!line.startsWith("***")) {
/* 60 */         parseAndAdd(line);
/*    */       }
/* 62 */       line = reader.readLine();
/*    */     }
/* 64 */     reader.close();
/*    */   }
/*    */ 
/*    */   private void parseAndAdd(String line)
/*    */   {
/* 74 */     StringTokenizer tokenizer = new StringTokenizer(line, " ");
/* 75 */     String phone = tokenizer.nextToken();
/* 76 */     float mean = Float.parseFloat(tokenizer.nextToken());
/* 77 */     float stddev = Float.parseFloat(tokenizer.nextToken());
/* 78 */     this.phoneDurations.put(phone, new PhoneDuration(mean, stddev));
/*    */   }
/*    */ 
/*    */   public PhoneDuration getPhoneDuration(String phone)
/*    */   {
/* 90 */     return (PhoneDuration)this.phoneDurations.get(phone);
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.PhoneDurationsImpl
 * JD-Core Version:    0.5.4
 */