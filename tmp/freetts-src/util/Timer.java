/*     */ package com.sun.speech.freetts.util;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.text.DecimalFormat;
/*     */ 
/*     */ public class Timer
/*     */ {
/*  16 */   private static final DecimalFormat timeFormatter = new DecimalFormat("###0.0000");
/*     */ 
/*  18 */   private static final DecimalFormat percentFormatter = new DecimalFormat("###0.00%");
/*     */   private String name;
/*     */   private long startTime;
/*     */   private long curTime;
/*     */   private long count;
/*     */   private double sum;
/*  25 */   private long minTime = 9223372036854775807L;
/*  26 */   private long maxTime = 0L;
/*     */   private boolean notReliable;
/*     */ 
/*     */   public Timer(String name)
/*     */   {
/*  35 */     this.name = name;
/*  36 */     reset();
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/*  43 */     this.startTime = 0L;
/*  44 */     this.count = 0L;
/*  45 */     this.sum = 0.0D;
/*  46 */     this.minTime = 9223372036854775807L;
/*  47 */     this.maxTime = 0L;
/*  48 */     this.notReliable = false;
/*     */   }
/*     */ 
/*     */   public void start()
/*     */   {
/*  55 */     if (this.startTime != 0L) {
/*  56 */       this.notReliable = true;
/*     */     }
/*     */ 
/*  59 */     this.startTime = System.currentTimeMillis();
/*     */   }
/*     */ 
/*     */   public long getCurrentTime()
/*     */   {
/*  69 */     return this.curTime;
/*     */   }
/*     */ 
/*     */   public void stop(boolean verbose)
/*     */   {
/*  79 */     if (this.startTime == 0L) {
/*  80 */       this.notReliable = true;
/*     */     }
/*  82 */     this.curTime = (System.currentTimeMillis() - this.startTime);
/*  83 */     this.startTime = 0L;
/*  84 */     if (this.curTime > this.maxTime) {
/*  85 */       this.maxTime = this.curTime;
/*     */     }
/*  87 */     if (this.curTime < this.minTime) {
/*  88 */       this.minTime = this.curTime;
/*     */     }
/*  90 */     this.count += 1L;
/*  91 */     this.sum += this.curTime;
/*  92 */     if (verbose)
/*  93 */       showTimesShort(0L);
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 101 */     stop(false);
/*     */   }
/*     */ 
/*     */   private String fmtTime(long time)
/*     */   {
/* 112 */     return fmtTime(time / 1000.0D);
/*     */   }
/*     */ 
/*     */   private String fmtTime(double time)
/*     */   {
/* 123 */     return Utilities.pad(timeFormatter.format(time) + "s", 10);
/*     */   }
/*     */ 
/*     */   public void showTimesLong(long overall)
/*     */   {
/* 134 */     System.out.println(" Timer:    " + this.name);
/* 135 */     System.out.println(" Count:    " + this.count);
/*     */ 
/* 137 */     if (this.notReliable) {
/* 138 */       System.out.println(" Not reliable");
/*     */     }
/* 140 */     else if (this.count == 1L) {
/* 141 */       System.out.println(" Cur Time: " + fmtTime(this.curTime));
/* 142 */     } else if (this.count > 1L) {
/* 143 */       System.out.println(" Min Time: " + fmtTime(this.minTime));
/* 144 */       System.out.println(" Max Time: " + fmtTime(this.maxTime));
/* 145 */       System.out.println(" Avg Time: " + fmtTime(this.sum / this.count / 1000.0D));
/*     */ 
/* 147 */       System.out.println(" Tot Time: " + fmtTime(this.sum / 1000.0D));
/* 148 */       if (overall != 0L) {
/* 149 */         System.out.println(" Percent:  " + percentFormatter.format(this.sum / overall));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 154 */     System.out.println();
/*     */   }
/*     */ 
/*     */   public static void showTimesShortTitle(String title)
/*     */   {
/* 164 */     String titleBar = "# ----------------------------- " + title + "----------------------------------------------------------- ";
/*     */ 
/* 167 */     System.out.println(Utilities.pad(titleBar, 78));
/* 168 */     System.out.print(Utilities.pad("# Name", 15) + " ");
/* 169 */     System.out.print(Utilities.pad("Count", 6));
/* 170 */     System.out.print(Utilities.pad("CurTime", 10));
/* 171 */     System.out.print(Utilities.pad("MinTime", 10));
/* 172 */     System.out.print(Utilities.pad("MaxTime", 10));
/* 173 */     System.out.print(Utilities.pad("AvgTime", 10));
/* 174 */     System.out.print(Utilities.pad("TotTime", 10));
/* 175 */     System.out.print(Utilities.pad("% Total", 8));
/* 176 */     System.out.println();
/*     */   }
/*     */ 
/*     */   public void showTimesShort(long overall)
/*     */   {
/* 186 */     double avgTime = 0.0D;
/* 187 */     double overallPercent = 0.0D;
/*     */ 
/* 195 */     if (this.count == 0L) {
/* 196 */       return;
/*     */     }
/*     */ 
/* 199 */     if (this.count > 0L) {
/* 200 */       avgTime = this.sum / this.count / 1000.0D;
/*     */     }
/*     */ 
/* 203 */     if (overall != 0L) {
/* 204 */       overallPercent = this.sum / overall;
/*     */     }
/*     */ 
/* 207 */     if (this.notReliable) {
/* 208 */       System.out.print(Utilities.pad(this.name, 15) + " ");
/* 209 */       System.out.println("Not reliable.");
/*     */     } else {
/* 211 */       System.out.print(Utilities.pad(this.name, 15) + " ");
/* 212 */       System.out.print(Utilities.pad("" + this.count, 6));
/* 213 */       System.out.print(fmtTime(this.curTime));
/* 214 */       System.out.print(fmtTime(this.minTime));
/* 215 */       System.out.print(fmtTime(this.maxTime));
/* 216 */       System.out.print(fmtTime(avgTime));
/* 217 */       System.out.print(fmtTime(this.sum / 1000.0D));
/* 218 */       System.out.print(percentFormatter.format(overallPercent));
/* 219 */       System.out.println();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void showTimes(long overall)
/*     */   {
/* 231 */     showTimesShort(overall);
/*     */   }
/*     */ 
/*     */   public void showTimes()
/*     */   {
/* 240 */     showTimesShort(0L);
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 250 */     Timer timer = new Timer("testTimer");
/* 251 */     Timer overallTimer = new Timer("overall");
/* 252 */     timer.showTimes(0L);
/*     */ 
/* 254 */     overallTimer.start();
/*     */ 
/* 256 */     for (int i = 0; i < 5; ++i) {
/* 257 */       timer.start();
/*     */       try {
/* 259 */         Thread.sleep(i * 1000L);
/*     */       } catch (InterruptedException e) {
/*     */       }
/* 262 */       timer.stop(true);
/*     */     }
/* 264 */     overallTimer.stop();
/* 265 */     timer.showTimes(overallTimer.getCurrentTime());
/* 266 */     overallTimer.showTimes();
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.util.Timer
 * JD-Core Version:    0.5.4
 */