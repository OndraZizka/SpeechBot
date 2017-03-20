/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import com.sun.speech.freetts.audio.AudioPlayer;
/*     */ import com.sun.speech.freetts.audio.JavaClipAudioPlayer;
/*     */ import com.sun.speech.freetts.audio.MultiFileAudioPlayer;
/*     */ import com.sun.speech.freetts.audio.NullAudioPlayer;
/*     */ import com.sun.speech.freetts.audio.RawFileAudioPlayer;
/*     */ import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.net.URL;
/*     */ import java.util.logging.ConsoleHandler;
/*     */ import java.util.logging.Handler;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.sound.sampled.AudioFileFormat.Type;
/*     */ import javax.sound.sampled.AudioSystem;
/*     */ 
/*     */ public class FreeTTS
/*     */ {
/*  40 */   private static final Logger LOGGER = Logger.getLogger(FreeTTS.class.getName());
/*     */   public static final String VERSION = "FreeTTS 1.2.2";
/*     */   private Voice voice;
/*  46 */   private static AudioPlayer audioPlayer = null;
/*  47 */   private boolean silent = false;
/*  48 */   private String audioFile = null;
/*  49 */   private boolean multiAudio = false;
/*  50 */   private boolean streamingAudio = false;
/*  51 */   private InputMode inputMode = InputMode.INTERACTIVE;
/*     */ 
/*     */   public FreeTTS()
/*     */   {
/*  57 */     VoiceManager voiceManager = VoiceManager.getInstance();
/*  58 */     voiceManager.getVoice("kevin16");
/*     */   }
/*     */ 
/*     */   public FreeTTS(Voice voice)
/*     */   {
/*  68 */     this.voice = voice;
/*     */   }
/*     */ 
/*     */   public void startup()
/*     */   {
/*  76 */     this.voice.allocate();
/*  77 */     if (!getSilentMode()) {
/*  78 */       if (this.audioFile != null) {
/*  79 */         AudioFileFormat.Type type = getAudioType(this.audioFile);
/*  80 */         if (type != null)
/*  81 */           if (this.multiAudio) {
/*  82 */             audioPlayer = new MultiFileAudioPlayer(getBasename(this.audioFile), type);
/*     */           }
/*     */           else
/*  85 */             audioPlayer = new SingleFileAudioPlayer(getBasename(this.audioFile), type);
/*     */         else {
/*     */           try
/*     */           {
/*  89 */             audioPlayer = new RawFileAudioPlayer(this.audioFile);
/*     */           } catch (IOException ioe) {
/*  91 */             System.out.println("Can't open " + this.audioFile + " " + ioe);
/*     */           }
/*     */         }
/*     */       }
/*  95 */       else if (!this.streamingAudio) {
/*  96 */         audioPlayer = new JavaClipAudioPlayer();
/*     */       } else {
/*     */         try {
/*  99 */           audioPlayer = this.voice.getDefaultAudioPlayer();
/*     */         } catch (InstantiationException e) {
/* 101 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 106 */     if (audioPlayer == null) {
/* 107 */       audioPlayer = new NullAudioPlayer();
/*     */     }
/*     */ 
/* 114 */     this.voice.setAudioPlayer(audioPlayer);
/*     */   }
/*     */ 
/*     */   private AudioFileFormat.Type getAudioType(String file)
/*     */   {
/* 126 */     AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
/* 127 */     String extension = getExtension(file);
/*     */ 
/* 129 */     for (int i = 0; i < types.length; ++i) {
/* 130 */       if (types[i].getExtension().equals(extension)) {
/* 131 */         return types[i];
/*     */       }
/*     */     }
/* 134 */     return null;
/*     */   }
/*     */ 
/*     */   private static String getExtension(String path)
/*     */   {
/* 146 */     int index = path.lastIndexOf(".");
/* 147 */     if (index == -1) {
/* 148 */       return null;
/*     */     }
/* 150 */     return path.substring(index + 1);
/*     */   }
/*     */ 
/*     */   private static String getBasename(String path)
/*     */   {
/* 163 */     int index = path.lastIndexOf(".");
/* 164 */     if (index == -1) {
/* 165 */       return path;
/*     */     }
/* 167 */     return path.substring(0, index);
/*     */   }
/*     */ 
/*     */   public void shutdown()
/*     */   {
/* 175 */     audioPlayer.close();
/* 176 */     this.voice.deallocate();
/*     */   }
/*     */ 
/*     */   public boolean textToSpeech(String text)
/*     */   {
/* 189 */     return this.voice.speak(text);
/*     */   }
/*     */ 
/*     */   private boolean batchTextToSpeech(String text)
/*     */   {
/* 203 */     this.voice.startBatch();
/* 204 */     boolean ok = textToSpeech(text);
/* 205 */     this.voice.endBatch();
/* 206 */     return ok;
/*     */   }
/*     */ 
/*     */   private boolean lineToSpeech(String path)
/*     */   {
/* 214 */     boolean ok = true;
/* 215 */     this.voice.startBatch();
/*     */     try {
/* 217 */       BufferedReader reader = new BufferedReader(new FileReader(path));
/*     */ 
/* 220 */       while (((line = reader.readLine()) != null) && (ok))
/*     */       {
/*     */         String line;
/* 221 */         ok = textToSpeech(line);
/*     */       }
/* 223 */       reader.close();
/*     */     } catch (IOException ioe) {
/* 225 */       LOGGER.severe("can't read " + path);
/* 226 */       throw new Error(ioe);
/*     */     }
/* 228 */     this.voice.endBatch();
/*     */ 
/* 230 */     return ok;
/*     */   }
/*     */ 
/*     */   protected Voice getVoice()
/*     */   {
/* 240 */     return this.voice;
/*     */   }
/*     */ 
/*     */   public boolean streamToSpeech(InputStream is)
/*     */   {
/* 251 */     this.voice.startBatch();
/* 252 */     boolean ok = this.voice.speak(is);
/* 253 */     this.voice.endBatch();
/* 254 */     return ok;
/*     */   }
/*     */ 
/*     */   public boolean urlToSpeech(String urlPath)
/*     */   {
/* 266 */     boolean ok = false;
/*     */     try {
/* 268 */       URL url = new URL(urlPath);
/* 269 */       InputStream is = url.openStream();
/* 270 */       ok = streamToSpeech(is);
/*     */     } catch (IOException ioe) {
/* 272 */       System.err.println("Can't read data from " + urlPath);
/*     */     }
/* 274 */     return ok;
/*     */   }
/*     */ 
/*     */   public boolean fileToSpeech(String filePath)
/*     */   {
/* 286 */     boolean ok = false;
/*     */     try {
/* 288 */       InputStream is = new FileInputStream(filePath);
/* 289 */       ok = streamToSpeech(is);
/*     */     } catch (IOException ioe) {
/* 291 */       System.err.println("Can't read data from " + filePath);
/*     */     }
/* 293 */     return ok;
/*     */   }
/*     */ 
/*     */   public void setSilentMode(boolean silent)
/*     */   {
/* 303 */     this.silent = silent;
/*     */   }
/*     */ 
/*     */   public boolean getSilentMode()
/*     */   {
/* 314 */     return this.silent;
/*     */   }
/*     */ 
/*     */   public void setInputMode(InputMode inputMode)
/*     */   {
/* 324 */     this.inputMode = inputMode;
/*     */   }
/*     */ 
/*     */   public InputMode getInputMode()
/*     */   {
/* 335 */     return this.inputMode;
/*     */   }
/*     */ 
/*     */   public void setAudioFile(String audioFile)
/*     */   {
/* 345 */     this.audioFile = audioFile;
/*     */   }
/*     */ 
/*     */   public void setMultiAudio(boolean multiAudio)
/*     */   {
/* 356 */     this.multiAudio = multiAudio;
/*     */   }
/*     */ 
/*     */   public void setStreamingAudio(boolean streamingAudio)
/*     */   {
/* 366 */     this.streamingAudio = streamingAudio;
/*     */   }
/*     */ 
/*     */   static void usage(String voices)
/*     */   {
/* 373 */     System.out.println("FreeTTS 1.2.2");
/* 374 */     System.out.println("Usage:");
/* 375 */     System.out.println("    -detailedMetrics: turn on detailed metrics");
/* 376 */     System.out.println("    -dumpAudio file : dump audio to file ");
/* 377 */     System.out.println("    -dumpAudioTypes : dump the possible output types");
/*     */ 
/* 379 */     System.out.println("    -dumpMultiAudio file : dump audio to file ");
/* 380 */     System.out.println("    -dumpRelations  : dump the relations ");
/* 381 */     System.out.println("    -dumpUtterance  : dump the final utterance");
/* 382 */     System.out.println("    -dumpASCII file : dump the final wave to file as ASCII");
/*     */ 
/* 384 */     System.out.println("    -file file      : speak text from given file");
/* 385 */     System.out.println("    -lines file     : render lines from a file");
/* 386 */     System.out.println("    -help           : shows usage information");
/* 387 */     System.out.println("    -voiceInfo      : print detailed voice info");
/* 388 */     System.out.println("    -metrics        : turn on metrics");
/* 389 */     System.out.println("    -run  name      : sets the name of the run");
/* 390 */     System.out.println("    -silent         : don't say anything");
/* 391 */     System.out.println("    -streaming      : use streaming audio player");
/* 392 */     System.out.println("    -text say me    : speak given text");
/* 393 */     System.out.println("    -url path       : speak text from given URL");
/* 394 */     System.out.println("    -verbose        : verbose output");
/* 395 */     System.out.println("    -version        : shows version number");
/* 396 */     System.out.println("    -voice VOICE    : " + voices);
/*     */   }
/*     */ 
/*     */   private static void interactiveMode(FreeTTS freetts)
/*     */   {
/*     */     try
/*     */     {
/* 411 */       BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
/* 412 */       System.out.print("Enter text: ");
/* 413 */       System.out.flush();
/* 414 */       String text = reader.readLine();
/* 415 */       if ((text == null) || (text.length() == 0)) {
/* 416 */         freetts.shutdown();
/* 417 */         System.exit(0);
/*     */       } else {
/* 419 */         freetts.batchTextToSpeech(text);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   private static void dumpAudioTypes()
/*     */   {
/* 430 */     AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
/*     */ 
/* 432 */     for (int i = 0; i < types.length; ++i)
/* 433 */       System.out.println(types[i].getExtension());
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 442 */     String text = null;
/* 443 */     String inFile = null;
/* 444 */     boolean dumpAudioTypes = false;
/* 445 */     Voice voice = null;
/*     */ 
/* 447 */     VoiceManager voiceManager = VoiceManager.getInstance();
/* 448 */     String voices = voiceManager.toString();
/*     */ 
/* 451 */     for (int i = 0; i < args.length; ++i) {
/* 452 */       if (args[i].equals("-voice")) {
/* 453 */         if (++i < args.length) {
/* 454 */           String voiceName = args[i];
/* 455 */           if (voiceManager.contains(voiceName)) {
/* 456 */             voice = voiceManager.getVoice(voiceName);
/*     */           } else {
/* 458 */             System.out.println("Invalid voice: " + voiceName);
/* 459 */             System.out.println("  Valid voices are " + voices);
/* 460 */             System.exit(1); } break;
/*     */         }
/*     */ 
/* 463 */         usage(voices);
/* 464 */         System.exit(1);
/*     */ 
/* 466 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 470 */     if (voice == null) {
/* 471 */       voice = voiceManager.getVoice("kevin16");
/*     */     }
/*     */ 
/* 474 */     if (voice == null) {
/* 475 */       throw new Error("The specified voice is not defined");
/*     */     }
/* 477 */     FreeTTS freetts = new FreeTTS(voice);
/*     */ 
/* 479 */     for (int i = 0; i < args.length; ++i) {
/* 480 */       if (args[i].equals("-metrics")) {
/* 481 */         voice.setMetrics(true);
/* 482 */       } else if (args[i].equals("-detailedMetrics")) {
/* 483 */         voice.setDetailedMetrics(true);
/* 484 */       } else if (args[i].equals("-silent")) {
/* 485 */         freetts.setSilentMode(true);
/* 486 */       } else if (args[i].equals("-streaming")) {
/* 487 */         freetts.setStreamingAudio(true);
/* 488 */       } else if (args[i].equals("-verbose")) {
/* 489 */         Handler handler = new ConsoleHandler();
/* 490 */         handler.setLevel(Level.ALL);
/* 491 */         Logger.getLogger("com.sun").addHandler(handler);
/* 492 */         Logger.getLogger("com.sun").setLevel(Level.ALL);
/* 493 */       } else if (args[i].equals("-dumpUtterance")) {
/* 494 */         voice.setDumpUtterance(true);
/* 495 */       } else if (args[i].equals("-dumpAudioTypes")) {
/* 496 */         dumpAudioTypes = true;
/* 497 */       } else if (args[i].equals("-dumpRelations")) {
/* 498 */         voice.setDumpRelations(true);
/* 499 */       } else if (args[i].equals("-dumpASCII")) {
/* 500 */         if (++i < args.length)
/* 501 */           voice.setWaveDumpFile(args[i]);
/*     */         else
/* 503 */           usage(voices);
/*     */       }
/* 505 */       else if (args[i].equals("-dumpAudio")) {
/* 506 */         if (++i < args.length)
/* 507 */           freetts.setAudioFile(args[i]);
/*     */         else
/* 509 */           usage(voices);
/*     */       }
/* 511 */       else if (args[i].equals("-dumpMultiAudio")) {
/* 512 */         if (++i < args.length) {
/* 513 */           freetts.setAudioFile(args[i]);
/* 514 */           freetts.setMultiAudio(true);
/*     */         } else {
/* 516 */           usage(voices);
/*     */         }
/* 518 */       } else if (args[i].equals("-version")) {
/* 519 */         System.out.println("FreeTTS 1.2.2");
/* 520 */       } else if (args[i].equals("-voice"))
/*     */       {
/* 522 */         ++i;
/* 523 */       } else if (args[i].equals("-help")) {
/* 524 */         usage(voices);
/* 525 */         System.exit(0);
/* 526 */       } else if (args[i].equals("-voiceInfo")) {
/* 527 */         System.out.println(VoiceManager.getInstance().getVoiceInfo());
/* 528 */         System.exit(0); } else {
/* 529 */         if (args[i].equals("-text")) {
/* 530 */           freetts.setInputMode(InputMode.TEXT);
/*     */ 
/* 532 */           StringBuffer sb = new StringBuffer();
/* 533 */           for (int j = i + 1; j < args.length; ++j) {
/* 534 */             sb.append(args[j]);
/* 535 */             sb.append(" ");
/*     */           }
/* 537 */           text = sb.toString();
/* 538 */           break;
/* 539 */         }if (args[i].equals("-file"))
/* 540 */           if (++i < args.length) {
/* 541 */             inFile = args[i];
/* 542 */             freetts.setInputMode(InputMode.FILE);
/*     */           } else {
/* 544 */             usage(voices);
/*     */           }
/* 546 */         else if (args[i].equals("-lines"))
/* 547 */           if (++i < args.length) {
/* 548 */             inFile = args[i];
/* 549 */             freetts.setInputMode(InputMode.LINES);
/*     */           } else {
/* 551 */             usage(voices);
/*     */           }
/* 553 */         else if (args[i].equals("-url"))
/* 554 */           if (++i < args.length) {
/* 555 */             inFile = args[i];
/* 556 */             freetts.setInputMode(InputMode.URL);
/*     */           } else {
/* 558 */             usage(voices);
/*     */           }
/* 560 */         else if (args[i].equals("-run")) {
/* 561 */           if (++i < args.length)
/* 562 */             voice.setRunTitle(args[i]);
/*     */           else
/* 564 */             usage(voices);
/*     */         }
/*     */         else {
/* 567 */           System.out.println("Unknown option:" + args[i]);
/*     */         }
/*     */       }
/*     */     }
/* 571 */     if (dumpAudioTypes) {
/* 572 */       dumpAudioTypes();
/*     */     }
/*     */ 
/* 575 */     freetts.startup();
/*     */ 
/* 577 */     if (freetts.getInputMode() == InputMode.TEXT)
/* 578 */       freetts.batchTextToSpeech(text);
/* 579 */     else if (freetts.getInputMode() == InputMode.FILE)
/* 580 */       freetts.fileToSpeech(inFile);
/* 581 */     else if (freetts.getInputMode() == InputMode.URL)
/* 582 */       freetts.urlToSpeech(inFile);
/* 583 */     else if (freetts.getInputMode() == InputMode.LINES)
/* 584 */       freetts.lineToSpeech(inFile);
/*     */     else {
/* 586 */       interactiveMode(freetts);
/*     */     }
/*     */ 
/* 589 */     if ((freetts.getVoice().isMetrics()) && (!freetts.getSilentMode()));
/* 595 */     freetts.shutdown();
/* 596 */     System.exit(0);
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.FreeTTS
 * JD-Core Version:    0.5.4
 */