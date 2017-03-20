/*     */ package com.sun.speech.freetts.util;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class BulkTimer
/*     */ {
/*  24 */   public static final BulkTimer LOAD = new BulkTimer();
/*     */   private static final String SELF = "OverallTime";
/*     */   private boolean verbose;
/*     */   private Map timers;
/*     */ 
/*     */   public BulkTimer()
/*     */   {
/*  35 */     this.verbose = false;
/*  36 */     this.timers = new LinkedHashMap();
/*     */   }
/*     */ 
/*     */   public void start(String name)
/*     */   {
/*  47 */     getTimer(name).start();
/*     */   }
/*     */ 
/*     */   public void stop(String name)
/*     */   {
/*  56 */     getTimer(name).stop(this.verbose);
/*     */   }
/*     */ 
/*     */   public void start()
/*     */   {
/*  68 */     getTimer("OverallTime").start();
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/*  76 */     getTimer("OverallTime").stop(this.verbose);
/*     */   }
/*     */ 
/*     */   public void setVerbose(boolean verbose)
/*     */   {
/*  85 */     this.verbose = verbose;
/*     */   }
/*     */ 
/*     */   public boolean isVerbose()
/*     */   {
/*  95 */     return this.verbose;
/*     */   }
/*     */ 
/*     */   public Timer getTimer(String name)
/*     */   {
/* 106 */     if (!this.timers.containsKey(name)) {
/* 107 */       this.timers.put(name, new Timer(name));
/*     */     }
/* 109 */     return (Timer)this.timers.get(name);
/*     */   }
/*     */ 
/*     */   public void show(String title)
/*     */   {
/* 118 */     long overall = getTimer("OverallTime").getCurrentTime();
/* 119 */     Collection values = this.timers.values();
/* 120 */     Timer.showTimesShortTitle(title);
/* 121 */     for (Iterator i = values.iterator(); i.hasNext(); ) {
/* 122 */       Timer t = (Timer)i.next();
/* 123 */       t.showTimes(overall);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.util.BulkTimer
 * JD-Core Version:    0.5.4
 */