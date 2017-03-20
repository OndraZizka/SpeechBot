/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.util.Calendar;
/*     */ import java.util.GregorianCalendar;
/*     */ import java.util.logging.ConsoleHandler;
/*     */ import java.util.logging.Handler;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class FreeTTSTime extends FreeTTS
/*     */ {
/*     */   private static final String VERSION = "FreeTTSTime Version 1.1, August  1, 2003";
/*     */ 
/*     */   public FreeTTSTime()
/*     */   {
/*  39 */     super(VoiceManager.getInstance().getVoice("alan"));
/*     */   }
/*     */ 
/*     */   public FreeTTSTime(Voice voice)
/*     */   {
/*  48 */     super(voice);
/*     */   }
/*     */ 
/*     */   public static void usage()
/*     */   {
/*  56 */     System.out.println("FreeTTSTime Version 1.1, August  1, 2003");
/*  57 */     System.out.println("Usage:");
/*  58 */     System.out.println("    -dumpASCII file : dump the final wave to file");
/*  59 */     System.out.println("    -dumpAudio file : dump audio to file ");
/*  60 */     System.out.println("    -help           : shows usage information");
/*  61 */     System.out.println("    -detailedMetrics: turn on detailed metrics");
/*  62 */     System.out.println("    -dumpRelations  : dump the relations ");
/*  63 */     System.out.println("    -dumpUtterance  : dump the final utterance");
/*  64 */     System.out.println("    -metrics        : turn on metrics");
/*  65 */     System.out.println("    -run  name      : sets the name of the run");
/*  66 */     System.out.println("    -silent         : don't say anything");
/*  67 */     System.out.println("    -verbose        : verbose output");
/*  68 */     System.out.println("    -version        : shows version number");
/*  69 */     System.out.println("    -timeTest       : runs a lengthy time test");
/*  70 */     System.out.println("    -iter count     : run for count iterations");
/*  71 */     System.out.println("    -time XX:XX     : speak the given time");
/*  72 */     System.out.println("    -time now       : speak the current time");
/*  73 */     System.out.println("    -period secs    : period of iter");
/*  74 */     System.out.println("    -clockMode      : tells time every 5 mins");
/*  75 */     System.out.println("    -voice VOICE    : " + VoiceManager.getInstance().toString());
/*     */ 
/*  77 */     System.exit(0);
/*     */   }
/*     */ 
/*     */   private static void interactiveMode(FreeTTSTime freetts)
/*     */   {
/*     */     try
/*     */     {
/*  92 */       BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
/*     */ 
/*  94 */       System.out.print("Enter time: ");
/*  95 */       System.out.flush();
/*  96 */       String time = reader.readLine();
/*  97 */       if ((time == null) || (time.length() == 0) || (time.equals("quit")))
/*     */       {
/*  99 */         freetts.shutdown();
/* 100 */         System.exit(0);
/*     */       } else {
/* 102 */         freetts.getVoice().startBatch();
/* 103 */         freetts.safeTimeToSpeech(time);
/* 104 */         freetts.getVoice().endBatch();
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   private static String timeApprox(int hour, int min)
/*     */   {
/* 123 */     int mm = min % 5;
/*     */ 
/* 125 */     if ((mm == 0) || (mm == 4))
/* 126 */       return "exactly";
/* 127 */     if (mm == 1)
/* 128 */       return "just after";
/* 129 */     if (mm == 2) {
/* 130 */       return "a little after";
/*     */     }
/* 132 */     return "almost";
/*     */   }
/*     */ 
/*     */   private static String timeMin(int hour, int min)
/*     */   {
/* 148 */     int mm = min / 5;
/* 149 */     if (min % 5 > 2) {
/* 150 */       ++mm;
/*     */     }
/* 152 */     mm *= 5;
/* 153 */     if (mm > 55) {
/* 154 */       mm = 0;
/*     */     }
/*     */ 
/* 157 */     if (mm == 0)
/* 158 */       return "";
/* 159 */     if (mm == 5)
/* 160 */       return "five past";
/* 161 */     if (mm == 10)
/* 162 */       return "ten past";
/* 163 */     if (mm == 15)
/* 164 */       return "quarter past";
/* 165 */     if (mm == 20)
/* 166 */       return "twenty past";
/* 167 */     if (mm == 25)
/* 168 */       return "twenty-five past";
/* 169 */     if (mm == 30)
/* 170 */       return "half past";
/* 171 */     if (mm == 35)
/* 172 */       return "twenty-five to";
/* 173 */     if (mm == 40)
/* 174 */       return "twenty to";
/* 175 */     if (mm == 45)
/* 176 */       return "quarter to";
/* 177 */     if (mm == 50)
/* 178 */       return "ten to";
/* 179 */     if (mm == 55) {
/* 180 */       return "five to";
/*     */     }
/* 182 */     return "five to";
/*     */   }
/*     */ 
/*     */   private static String timeHour(int hour, int min)
/*     */   {
/* 198 */     int hh = hour;
/* 199 */     if (min > 32) {
/* 200 */       ++hh;
/*     */     }
/* 202 */     if (hh == 24) {
/* 203 */       hh = 0;
/*     */     }
/* 205 */     if (hh > 12) {
/* 206 */       hh -= 12;
/*     */     }
/*     */ 
/* 209 */     if (hh == 0)
/* 210 */       return "midnight";
/* 211 */     if (hh == 1)
/* 212 */       return "one";
/* 213 */     if (hh == 2)
/* 214 */       return "two";
/* 215 */     if (hh == 3)
/* 216 */       return "three";
/* 217 */     if (hh == 4)
/* 218 */       return "four";
/* 219 */     if (hh == 5)
/* 220 */       return "five";
/* 221 */     if (hh == 6)
/* 222 */       return "six";
/* 223 */     if (hh == 7)
/* 224 */       return "seven";
/* 225 */     if (hh == 8)
/* 226 */       return "eight";
/* 227 */     if (hh == 9)
/* 228 */       return "nine";
/* 229 */     if (hh == 10)
/* 230 */       return "ten";
/* 231 */     if (hh == 11)
/* 232 */       return "eleven";
/* 233 */     if (hh == 12) {
/* 234 */       return "twelve";
/*     */     }
/* 236 */     return "twelve";
/*     */   }
/*     */ 
/*     */   private static String timeOfDay(int hour, int min)
/*     */   {
/* 249 */     int hh = hour;
/*     */ 
/* 251 */     if (min > 58) {
/* 252 */       ++hh;
/*     */     }
/* 254 */     if (hh == 24)
/* 255 */       return "";
/* 256 */     if (hh > 17)
/* 257 */       return "in the evening";
/* 258 */     if (hh > 11) {
/* 259 */       return "in the afternoon";
/*     */     }
/* 261 */     return "in the morning";
/*     */   }
/*     */ 
/*     */   public static String timeToString(String time)
/*     */   {
/* 274 */     String theTime = null;
/* 275 */     if (Pattern.matches("[012][0-9]:[0-5][0-9]", time)) {
/* 276 */       int hour = Integer.parseInt(time.substring(0, 2));
/* 277 */       int min = Integer.parseInt(time.substring(3));
/*     */ 
/* 279 */       theTime = timeToString(hour, min);
/*     */     }
/* 281 */     return theTime;
/*     */   }
/*     */ 
/*     */   public static String timeToString(int hour, int min)
/*     */   {
/* 293 */     String theTime = "The time is now, " + timeApprox(hour, min) + " " + timeMin(hour, min) + " " + timeHour(hour, min) + ", " + timeOfDay(hour, min) + ".";
/*     */ 
/* 298 */     return theTime;
/*     */   }
/*     */ 
/*     */   public void timeToSpeech(String time)
/*     */   {
/* 313 */     String theTime = timeToString(time);
/* 314 */     if (theTime != null)
/* 315 */       textToSpeech(theTime);
/*     */     else
/* 317 */       throw new IllegalArgumentException("Bad time format");
/*     */   }
/*     */ 
/*     */   public void timeToSpeech(int hour, int min)
/*     */   {
/* 328 */     if ((hour < 0) || (hour > 23)) {
/* 329 */       throw new IllegalArgumentException("Bad time format: hour");
/*     */     }
/*     */ 
/* 332 */     if ((min < 0) || (min > 59)) {
/* 333 */       throw new IllegalArgumentException("Bad time format: min");
/*     */     }
/* 335 */     String theTime = timeToString(hour, min);
/* 336 */     textToSpeech(theTime);
/*     */   }
/*     */ 
/*     */   public void safeTimeToSpeech(String time)
/*     */   {
/*     */     try
/*     */     {
/* 348 */       if (time.equals("now"))
/* 349 */         speakNow();
/*     */       else
/* 351 */         timeToSpeech(time);
/*     */     }
/*     */     catch (IllegalArgumentException iae) {
/* 354 */       System.err.println("Bad time format");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void speakNow()
/*     */   {
/* 362 */     Calendar cal = new GregorianCalendar();
/* 363 */     int hour = cal.get(11);
/* 364 */     int min = cal.get(12);
/* 365 */     timeToSpeech(hour, min);
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 373 */     String time = null;
/* 374 */     int iterations = 1;
/* 375 */     int delay = 0;
/*     */ 
/* 377 */     boolean setMetrics = false;
/* 378 */     boolean setDetailedMetrics = false;
/* 379 */     boolean setVerbose = false;
/* 380 */     boolean setDumpUtterance = false;
/* 381 */     boolean setDumpRelations = false;
/* 382 */     String waveDumpFile = null;
/* 383 */     String runTitle = null;
/*     */ 
/* 385 */     boolean setSilentMode = false;
/* 386 */     String audioFile = null;
/* 387 */     boolean setInputMode = false;
/*     */ 
/* 389 */     String voiceName = null;
/*     */ 
/* 391 */     for (int i = 0; i < args.length; ++i) {
/* 392 */       if (args[i].equals("-metrics")) {
/* 393 */         setMetrics = true;
/* 394 */       } else if (args[i].equals("-detailedMetrics")) {
/* 395 */         setDetailedMetrics = true;
/* 396 */       } else if (args[i].equals("-silent")) {
/* 397 */         setSilentMode = true;
/* 398 */       } else if (args[i].equals("-period")) {
/* 399 */         if (++i >= args.length) continue;
/*     */         try {
/* 401 */           delay = Integer.parseInt(args[i]);
/*     */         } catch (NumberFormatException nfe) {
/* 403 */           System.out.println("Bad clock period");
/* 404 */           usage();
/*     */         }
/*     */       }
/* 407 */       else if (args[i].equals("-verbose")) {
/* 408 */         setVerbose = true;
/* 409 */       } else if (args[i].equals("-dumpUtterance")) {
/* 410 */         setDumpUtterance = true;
/* 411 */       } else if (args[i].equals("-dumpRelations")) {
/* 412 */         setDumpRelations = true;
/* 413 */       } else if (args[i].equals("-clockMode")) {
/* 414 */         iterations = 2147483647;
/* 415 */         delay = 300;
/* 416 */       } else if (args[i].equals("-timeTest")) {
/* 417 */         iterations = 100;
/* 418 */       } else if (args[i].equals("-dumpAudio")) {
/* 419 */         if (++i < args.length)
/* 420 */           audioFile = args[i];
/*     */         else
/* 422 */           usage();
/*     */       }
/* 424 */       else if (args[i].equals("-iter")) {
/* 425 */         if (++i < args.length)
/*     */           try {
/* 427 */             iterations = Integer.parseInt(args[i]);
/*     */           } catch (NumberFormatException nfe) {
/* 429 */             System.out.println("Bad iteration format");
/* 430 */             usage();
/*     */           }
/*     */         else
/* 433 */           usage();
/*     */       }
/* 435 */       else if (args[i].equals("-dumpASCII")) {
/* 436 */         if (++i < args.length)
/* 437 */           waveDumpFile = args[i];
/*     */         else
/* 439 */           usage();
/*     */       }
/* 441 */       else if (args[i].equals("-version")) {
/* 442 */         System.out.println("FreeTTSTime Version 1.1, August  1, 2003");
/* 443 */       } else if (args[i].equals("-help")) {
/* 444 */         usage();
/* 445 */       } else if (args[i].equals("-time")) {
/* 446 */         setInputMode = true;
/* 447 */         if (++i < args.length)
/* 448 */           time = args[i];
/*     */         else
/* 450 */           usage();
/*     */       }
/* 452 */       else if (args[i].equals("-run")) {
/* 453 */         if (++i < args.length)
/* 454 */           runTitle = args[i];
/*     */         else
/* 456 */           usage();
/*     */       }
/* 458 */       else if (args[i].equals("-voice")) {
/* 459 */         if (++i < args.length)
/* 460 */           voiceName = args[i];
/*     */         else
/* 462 */           usage();
/*     */       }
/*     */       else {
/* 465 */         System.out.println("Unknown option:" + args[i]);
/*     */       }
/*     */     }
/*     */ 
/* 469 */     if (voiceName == null) {
/* 470 */       voiceName = "alan";
/*     */     }
/*     */ 
/* 473 */     FreeTTSTime freetts = new FreeTTSTime(VoiceManager.getInstance().getVoice(voiceName));
/*     */ 
/* 475 */     Voice voice = freetts.getVoice();
/*     */ 
/* 477 */     if (setMetrics) {
/* 478 */       voice.setMetrics(true);
/*     */     }
/*     */ 
/* 481 */     if (setDetailedMetrics) {
/* 482 */       voice.setDetailedMetrics(true);
/*     */     }
/*     */ 
/* 485 */     if (setVerbose) {
/* 486 */       Handler handler = new ConsoleHandler();
/* 487 */       handler.setLevel(Level.ALL);
/* 488 */       Logger.getLogger("com.sun").addHandler(handler);
/* 489 */       Logger.getLogger("com.sun").setLevel(Level.ALL);
/*     */     }
/*     */ 
/* 492 */     if (setDumpUtterance) {
/* 493 */       voice.setDumpUtterance(true);
/*     */     }
/*     */ 
/* 496 */     if (setDumpRelations) {
/* 497 */       voice.setDumpRelations(true);
/*     */     }
/*     */ 
/* 500 */     if (waveDumpFile != null) {
/* 501 */       voice.setWaveDumpFile(waveDumpFile);
/*     */     }
/*     */ 
/* 504 */     if (runTitle != null) {
/* 505 */       voice.setRunTitle(runTitle);
/*     */     }
/*     */ 
/* 508 */     if (setSilentMode) {
/* 509 */       freetts.setSilentMode(true);
/*     */     }
/*     */ 
/* 512 */     if (audioFile != null) {
/* 513 */       freetts.setAudioFile(audioFile);
/*     */     }
/*     */ 
/* 516 */     if (setInputMode) {
/* 517 */       freetts.setInputMode(InputMode.TEXT);
/*     */     }
/*     */ 
/* 521 */     freetts.startup();
/*     */ 
/* 523 */     if (time != null) {
/* 524 */       freetts.getVoice().startBatch();
/* 525 */       for (int i = 0; i < iterations; ++i) {
/* 526 */         freetts.safeTimeToSpeech(time);
/*     */         try {
/* 528 */           Thread.sleep(delay * 1000L);
/*     */         } catch (InterruptedException ie) {
/* 530 */           break label798:
/*     */         }
/*     */       }
/* 533 */       label798: freetts.getVoice().endBatch();
/*     */     } else {
/* 535 */       interactiveMode(freetts);
/*     */     }
/* 537 */     freetts.shutdown();
/* 538 */     System.exit(0);
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.FreeTTSTime
 * JD-Core Version:    0.5.4
 */