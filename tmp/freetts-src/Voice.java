/*      */ package com.sun.speech.freetts;
/*      */ 
/*      */ import com.sun.speech.freetts.audio.AudioPlayer;
/*      */ import com.sun.speech.freetts.lexicon.Lexicon;
/*      */ import com.sun.speech.freetts.relp.LPCResult;
/*      */ import com.sun.speech.freetts.util.BulkTimer;
/*      */ import com.sun.speech.freetts.util.Utilities;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.PrintStream;
/*      */ import java.io.PrintWriter;
/*      */ import java.io.Reader;
/*      */ import java.net.URL;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collections;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Locale;
/*      */ import java.util.Map;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */ import org.w3c.dom.Document;
/*      */ import org.w3c.dom.Node;
/*      */ import org.w3c.dom.Text;
/*      */ 
/*      */ public abstract class Voice
/*      */   implements UtteranceProcessor, Dumpable
/*      */ {
/*   83 */   private static final Logger LOGGER = Logger.getLogger(Voice.class.getName());
/*      */   public static final String DATABASE_NAME = "databaseName";
/*      */   private List utteranceProcessors;
/*      */   private Map featureProcessors;
/*      */   private FeatureSetImpl features;
/*   95 */   private boolean metrics = false;
/*   96 */   private boolean detailedMetrics = false;
/*   97 */   private boolean dumpUtterance = false;
/*   98 */   private boolean dumpRelations = false;
/*   99 */   private String runTitle = "unnamed run";
/*  100 */   private Lexicon lexicon = null;
/*  101 */   private AudioPlayer defaultAudioPlayer = null;
/*  102 */   private AudioPlayer audioPlayer = null;
/*      */   private UtteranceProcessor audioOutput;
/*  104 */   private OutputQueue outputQueue = null;
/*  105 */   private String waveDumpFile = null;
/*  106 */   private BulkTimer runTimer = new BulkTimer();
/*  107 */   private BulkTimer threadTimer = new BulkTimer();
/*  108 */   private boolean externalOutputQueue = false;
/*  109 */   private boolean externalAudioPlayer = false;
/*      */ 
/*  112 */   private float nominalRate = 150.0F;
/*  113 */   private float pitch = 100.0F;
/*  114 */   private float range = 10.0F;
/*  115 */   private float pitchShift = 1.0F;
/*  116 */   private float volume = 0.8F;
/*  117 */   private float durationStretch = 1.0F;
/*      */ 
/*  119 */   private boolean loaded = false;
/*      */ 
/*  121 */   private String name = "default_name";
/*  122 */   private Age age = Age.DONT_CARE;
/*  123 */   private Gender gender = Gender.DONT_CARE;
/*  124 */   private String description = "default description";
/*  125 */   private Locale locale = Locale.getDefault();
/*  126 */   private String domain = "general";
/*  127 */   private String style = "standard";
/*  128 */   private String organization = "unknown";
/*      */   public static final String PROP_PREFIX = "com.sun.speech.freetts.voice.";
/*      */   public static final String FEATURE_SILENCE = "silence";
/*      */   public static final String FEATURE_JOIN_TYPE = "join_type";
/*      */   public static final String DEFAULT_AUDIO_PLAYER = "com.sun.speech.freetts.voice.defaultAudioPlayer";
/*      */   public static final String DEFAULT_AUDIO_PLAYER_DEFAULT = "com.sun.speech.freetts.audio.JavaStreamingAudioPlayer";
/*      */ 
/*      */   public Voice()
/*      */   {
/*  175 */     this.utteranceProcessors = Collections.synchronizedList(new ArrayList());
/*  176 */     this.features = new FeatureSetImpl();
/*  177 */     this.featureProcessors = new HashMap();
/*      */     try
/*      */     {
/*  180 */       this.nominalRate = Float.parseFloat(Utilities.getProperty("com.sun.speech.freetts.voice.speakingRate", "150"));
/*      */ 
/*  182 */       this.pitch = Float.parseFloat(Utilities.getProperty("com.sun.speech.freetts.voice.pitch", "100"));
/*      */ 
/*  184 */       this.range = Float.parseFloat(Utilities.getProperty("com.sun.speech.freetts.voice.range", "10"));
/*      */ 
/*  186 */       this.volume = Float.parseFloat(Utilities.getProperty("com.sun.speech.freetts.voice.volume", "1.0"));
/*      */     }
/*      */     catch (SecurityException se)
/*      */     {
/*      */     }
/*  191 */     this.outputQueue = null;
/*  192 */     this.audioPlayer = null;
/*  193 */     this.defaultAudioPlayer = null;
/*      */   }
/*      */ 
/*      */   public Voice(String name, Gender gender, Age age, String description, Locale locale, String domain, String organization)
/*      */   {
/*  216 */     setName(name);
/*  217 */     setGender(gender);
/*  218 */     setAge(age);
/*  219 */     setDescription(description);
/*  220 */     setLocale(locale);
/*  221 */     setDomain(domain);
/*  222 */     setOrganization(organization);
/*      */   }
/*      */ 
/*      */   public boolean speak(String text)
/*      */   {
/*  235 */     return speak(new FreeTTSSpeakableImpl(text));
/*      */   }
/*      */ 
/*      */   public boolean speak(Document doc)
/*      */   {
/*  248 */     return speak(new FreeTTSSpeakableImpl(doc));
/*      */   }
/*      */ 
/*      */   public boolean speak(InputStream inputStream)
/*      */   {
/*  261 */     return speak(new FreeTTSSpeakableImpl(inputStream));
/*      */   }
/*      */ 
/*      */   public boolean speak(FreeTTSSpeakable speakable)
/*      */   {
/*  276 */     if (LOGGER.isLoggable(Level.FINE)) {
/*  277 */       LOGGER.fine("speak(FreeTTSSpeakable) called");
/*      */     }
/*  279 */     boolean ok = true;
/*  280 */     boolean posted = false;
/*      */ 
/*  282 */     getAudioPlayer().startFirstSampleTimer();
/*      */ 
/*  284 */     Iterator i = tokenize(speakable);
/*  285 */     while ((!speakable.isCompleted()) && (i.hasNext())) {
/*      */       try {
/*  287 */         Utterance utterance = (Utterance)i.next();
/*  288 */         if (utterance != null) {
/*  289 */           processUtterance(utterance);
/*  290 */           posted = true;
/*      */         }
/*      */       } catch (ProcessException pe) {
/*  293 */         ok = false;
/*      */       }
/*      */     }
/*  296 */     if ((ok) && (posted)) {
/*  297 */       this.runTimer.start("WaitAudio");
/*  298 */       ok = speakable.waitCompleted();
/*  299 */       this.runTimer.stop("WaitAudio");
/*      */     }
/*  301 */     if (LOGGER.isLoggable(Level.FINE)) {
/*  302 */       LOGGER.fine("speak(FreeTTSSpeakable) completed");
/*      */     }
/*  304 */     return ok;
/*      */   }
/*      */ 
/*      */   /** @deprecated */
/*      */   public void load()
/*      */   {
/*  312 */     allocate();
/*      */   }
/*      */ 
/*      */   public void allocate()
/*      */   {
/*  323 */     if (isLoaded()) {
/*  324 */       return;
/*      */     }
/*  326 */     BulkTimer.LOAD.start();
/*      */ 
/*  329 */     if (!this.lexicon.isLoaded()) {
/*      */       try {
/*  331 */         this.lexicon.load();
/*      */       } catch (IOException ioe) {
/*  333 */         LOGGER.severe("Can't load voice " + ioe);
/*  334 */         throw new Error(ioe);
/*      */       }
/*      */     }
/*      */     try
/*      */     {
/*  339 */       this.audioOutput = getAudioOutput();
/*      */     } catch (IOException ioe) {
/*  341 */       LOGGER.severe("Can't load audio output handler for voice " + ioe);
/*  342 */       throw new Error(ioe);
/*      */     }
/*  344 */     if (this.outputQueue == null)
/*  345 */       this.outputQueue = createOutputThread();
/*      */     try
/*      */     {
/*  348 */       loader();
/*      */     } catch (IOException ioe) {
/*  350 */       LOGGER.severe("Can't load voice " + ioe);
/*  351 */       throw new Error(ioe);
/*      */     }
/*  353 */     BulkTimer.LOAD.stop();
/*  354 */     if (isMetrics()) {
/*  355 */       BulkTimer.LOAD.show("loading " + toString() + " for " + getRunTitle());
/*      */     }
/*      */ 
/*  358 */     setLoaded(true);
/*      */   }
/*      */ 
/*      */   public boolean isLoaded()
/*      */   {
/*  369 */     return this.loaded;
/*      */   }
/*      */ 
/*      */   protected void setLoaded(boolean loaded)
/*      */   {
/*  379 */     this.loaded = loaded;
/*      */   }
/*      */ 
/*      */   public void processUtterance(Utterance u)
/*      */     throws ProcessException
/*      */   {
/*  396 */     if (this.utteranceProcessors == null) {
/*  397 */       return;
/*      */     }
/*  399 */     if (u == null) {
/*  400 */       throw new ProcessException("Utterance is null.");
/*      */     }
/*      */ 
/*  403 */     this.runTimer.start("processing");
/*  404 */     UtteranceProcessor[] processors = new Dumpable[this.utteranceProcessors.size()];
/*  405 */     processors = (UtteranceProcessor[])this.utteranceProcessors.toArray(processors);
/*      */ 
/*  408 */     if (LOGGER.isLoggable(Level.FINE))
/*  409 */       LOGGER.fine("Processing Utterance: " + u.getString("input_text"));
/*      */     try
/*      */     {
/*  412 */       for (int i = 0; (i < processors.length) && (!u.getSpeakable().isCompleted()); )
/*      */       {
/*  414 */         runProcessor(processors[i], u, this.runTimer);
/*      */ 
/*  413 */         ++i;
/*      */       }
/*      */ 
/*  416 */       if (!u.getSpeakable().isCompleted())
/*  417 */         if (this.outputQueue == null) {
/*  418 */           if (LOGGER.isLoggable(Level.FINE)) {
/*  419 */             LOGGER.fine("To AudioOutput");
/*      */           }
/*  421 */           outputUtterance(u, this.runTimer);
/*      */         } else {
/*  423 */           this.runTimer.start("..post");
/*  424 */           this.outputQueue.post(u);
/*  425 */           this.runTimer.stop("..post");
/*      */         }
/*      */     }
/*      */     catch (ProcessException pe) {
/*  429 */       System.err.println("Processing Utterance: " + pe);
/*      */     } catch (Exception e) {
/*  431 */       System.err.println("Trouble while processing utterance " + e);
/*  432 */       e.printStackTrace();
/*  433 */       u.getSpeakable().cancelled();
/*      */     }
/*      */ 
/*  436 */     if (LOGGER.isLoggable(Level.FINE)) {
/*  437 */       LOGGER.fine("Done Processing Utterance: " + u.getString("input_text"));
/*      */     }
/*      */ 
/*  440 */     this.runTimer.stop("processing");
/*      */ 
/*  442 */     if (this.dumpUtterance) {
/*  443 */       u.dump("Utterance");
/*      */     }
/*  445 */     if (this.dumpRelations) {
/*  446 */       u.dumpRelations("Utterance");
/*      */     }
/*      */ 
/*  449 */     dumpASCII(u);
/*      */   }
/*      */ 
/*      */   private void dumpASCII(Utterance utterance)
/*      */   {
/*  459 */     if (this.waveDumpFile != null) {
/*  460 */       LPCResult lpcResult = (LPCResult)utterance.getObject("target_lpcres");
/*      */       try
/*      */       {
/*  463 */         if (this.waveDumpFile.equals("-"))
/*  464 */           lpcResult.dumpASCII();
/*      */         else
/*  466 */           lpcResult.dumpASCII(this.waveDumpFile);
/*      */       }
/*      */       catch (IOException ioe) {
/*  469 */         LOGGER.severe("Can't dump file to " + this.waveDumpFile + " " + ioe);
/*  470 */         throw new Error(ioe);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static OutputQueue createOutputThread()
/*      */   {
/*  484 */     OutputQueue queue = new OutputQueue();
/*  485 */     Thread t = new Thread(queue) { private final OutputQueue val$queue;
/*      */ 
/*  487 */       public void run() { Utterance utterance = null;
/*      */         do {
/*  489 */           utterance = this.val$queue.pend();
/*  490 */           if (utterance != null) {
/*  491 */             Voice voice = utterance.getVoice();
/*  492 */             if (Voice.LOGGER.isLoggable(Level.FINE)) {
/*  493 */               Voice.LOGGER.fine("OUT: " + utterance.getString("input_text"));
/*      */             }
/*      */ 
/*  496 */             voice.outputUtterance(utterance, voice.threadTimer);
/*      */           }
/*      */         }
/*  498 */         while (utterance != null); }
/*      */ 
/*      */     };
/*  501 */     t.setDaemon(true);
/*  502 */     t.start();
/*  503 */     return queue;
/*      */   }
/*      */ 
/*      */   private boolean outputUtterance(Utterance utterance, BulkTimer timer)
/*      */   {
/*  521 */     boolean ok = true;
/*  522 */     FreeTTSSpeakable speakable = utterance.getSpeakable();
/*      */ 
/*  524 */     if (!speakable.isCompleted()) {
/*  525 */       if (utterance.isFirst()) {
/*  526 */         getAudioPlayer().reset();
/*  527 */         speakable.started();
/*  528 */         if (LOGGER.isLoggable(Level.FINE)) {
/*  529 */           LOGGER.fine(" --- started ---");
/*      */         }
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/*  535 */         if (!speakable.isCompleted())
/*  536 */           runProcessor(this.audioOutput, utterance, timer);
/*      */         else
/*  538 */           ok = false;
/*      */       }
/*      */       catch (ProcessException pe) {
/*  541 */         ok = false;
/*      */       }
/*  543 */       if ((ok) && (utterance.isLast())) {
/*  544 */         getAudioPlayer().drain();
/*  545 */         speakable.completed();
/*  546 */         if (LOGGER.isLoggable(Level.FINE))
/*  547 */           LOGGER.fine(" --- completed ---");
/*      */       }
/*  549 */       else if (!ok)
/*      */       {
/*  551 */         speakable.cancelled();
/*  552 */         if (LOGGER.isLoggable(Level.FINE)) {
/*  553 */           LOGGER.fine(" --- cancelled ---");
/*      */         }
/*      */       }
/*  556 */       else if (LOGGER.isLoggable(Level.FINE)) {
/*  557 */         LOGGER.fine(" --- not last: " + speakable.getText() + " --- ");
/*      */       }
/*      */ 
/*  561 */       if (LOGGER.isLoggable(Level.FINE))
/*  562 */         LOGGER.fine("Calling speakable.completed() on " + speakable.getText());
/*      */     }
/*      */     else
/*      */     {
/*  566 */       ok = false;
/*  567 */       if (LOGGER.isLoggable(Level.FINE)) {
/*  568 */         LOGGER.fine("STRANGE: speakable already completed: " + speakable.getText());
/*      */       }
/*      */     }
/*      */ 
/*  572 */     return ok;
/*      */   }
/*      */ 
/*      */   private void runProcessor(UtteranceProcessor processor, Utterance utterance, BulkTimer timer)
/*      */     throws ProcessException
/*      */   {
/*  589 */     if (processor != null) {
/*  590 */       String processorName = ".." + processor.toString();
/*  591 */       if (LOGGER.isLoggable(Level.FINE)) {
/*  592 */         LOGGER.fine("   Running " + processorName);
/*      */       }
/*  594 */       timer.start(processorName);
/*  595 */       processor.processUtterance(utterance);
/*  596 */       timer.stop(processorName);
/*      */     }
/*      */   }
/*      */ 
/*      */   public abstract Tokenizer getTokenizer();
/*      */ 
/*      */   public List getUtteranceProcessors()
/*      */   {
/*  617 */     return this.utteranceProcessors;
/*      */   }
/*      */ 
/*      */   public FeatureSet getFeatures()
/*      */   {
/*  627 */     return this.features;
/*      */   }
/*      */ 
/*      */   public void startBatch()
/*      */   {
/*  638 */     this.runTimer.setVerbose(this.detailedMetrics);
/*  639 */     this.runTimer.start();
/*      */   }
/*      */ 
/*      */   public void endBatch()
/*      */   {
/*  649 */     this.runTimer.stop();
/*      */ 
/*  651 */     if (this.metrics) {
/*  652 */       this.runTimer.show(getRunTitle() + " run");
/*  653 */       this.threadTimer.show(getRunTitle() + " thread");
/*  654 */       getAudioPlayer().showMetrics();
/*  655 */       long totalMemory = Runtime.getRuntime().totalMemory();
/*  656 */       LOGGER.info("Memory Use    : " + (totalMemory - Runtime.getRuntime().freeMemory()) / 1024L + "k  of " + totalMemory / 1024L + "k");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setOutputQueue(OutputQueue queue)
/*      */   {
/*  684 */     this.externalOutputQueue = true;
/*  685 */     this.outputQueue = queue;
/*      */   }
/*      */ 
/*      */   public OutputQueue getOutputQueue()
/*      */   {
/*  694 */     return this.outputQueue;
/*      */   }
/*      */ 
/*      */   protected abstract void loader()
/*      */     throws IOException;
/*      */ 
/*      */   private Iterator tokenize(FreeTTSSpeakable speakable)
/*      */   {
/*  709 */     return new FreeTTSSpeakableTokenizer(speakable).iterator();
/*      */   }
/*      */ 
/*      */   private String documentToString(Document dom)
/*      */   {
/*  721 */     StringBuffer buf = new StringBuffer();
/*  722 */     linearize(dom, buf);
/*  723 */     return buf.toString();
/*      */   }
/*      */ 
/*      */   private void linearize(Node n, StringBuffer buf)
/*      */   {
/*  733 */     StringBuffer endText = processNode(n, buf);
/*  734 */     Node child = n.getFirstChild();
/*  735 */     while (child != null)
/*      */     {
/*  737 */       linearize(child, buf);
/*      */ 
/*  736 */       child = child.getNextSibling();
/*      */     }
/*      */ 
/*  740 */     if (endText != null)
/*  741 */       buf.append(endText);
/*      */   }
/*      */ 
/*      */   protected StringBuffer processNode(Node n, StringBuffer buf)
/*      */   {
/*  757 */     StringBuffer endText = null;
/*      */ 
/*  759 */     int type = n.getNodeType();
/*  760 */     switch (type)
/*      */     {
/*      */     case 2:
/*  762 */       break;
/*      */     case 9:
/*  765 */       break;
/*      */     case 1:
/*  769 */       break;
/*      */     case 3:
/*  772 */       buf.append(((Text)n).getData());
/*  773 */       break;
/*      */     case 7:
/*  781 */       break;
/*      */     case 10:
/*  786 */       break;
/*      */     case 12:
/*  791 */       break;
/*      */     case 8:
/*  797 */       break;
/*      */     case 4:
/*  803 */       break;
/*      */     case 5:
/*      */     case 6:
/*  810 */       break;
/*      */     case 11:
/*      */     }
/*      */ 
/*  823 */     return endText;
/*      */   }
/*      */ 
/*      */   public void dump(PrintWriter output, int pad, String title)
/*      */   {
/*  834 */     Utilities.dump(output, pad, title);
/*  835 */     this.features.dump(output, pad + 4, title + " Features");
/*  836 */     dumpProcessors(output, pad + 4, title + " Processors");
/*      */   }
/*      */ 
/*      */   public void dumpProcessors(PrintWriter output, int pad, String title)
/*      */   {
/*  849 */     if (this.utteranceProcessors == null) {
/*  850 */       return;
/*      */     }
/*      */ 
/*  853 */     UtteranceProcessor[] processors = new Dumpable[this.utteranceProcessors.size()];
/*  854 */     processors = (UtteranceProcessor[])this.utteranceProcessors.toArray(processors);
/*      */ 
/*  857 */     Utilities.dump(output, pad, title);
/*  858 */     for (int i = 0; i < processors.length; ++i)
/*  859 */       Utilities.dump(output, pad + 4, processors[i].toString());
/*      */   }
/*      */ 
/*      */   public FeatureProcessor getFeatureProcessor(String name)
/*      */   {
/*  873 */     return (FeatureProcessor)this.featureProcessors.get(name);
/*      */   }
/*      */ 
/*      */   public void addFeatureProcessor(String name, FeatureProcessor fp)
/*      */   {
/*  884 */     this.featureProcessors.put(name, fp);
/*      */   }
/*      */ 
/*      */   public boolean isMetrics()
/*      */   {
/*  893 */     return this.metrics;
/*      */   }
/*      */ 
/*      */   public void setMetrics(boolean metrics)
/*      */   {
/*  902 */     this.metrics = metrics;
/*  903 */     if (LOGGER.isLoggable(Level.FINE))
/*  904 */       LOGGER.fine("Metrics mode is " + metrics);
/*      */   }
/*      */ 
/*      */   public boolean isDetailedMetrics()
/*      */   {
/*  914 */     return this.detailedMetrics;
/*      */   }
/*      */ 
/*      */   public void setDetailedMetrics(boolean detailedMetrics)
/*      */   {
/*  923 */     this.detailedMetrics = detailedMetrics;
/*  924 */     if (LOGGER.isLoggable(Level.FINE))
/*  925 */       LOGGER.fine("DetailedMetrics mode is " + detailedMetrics);
/*      */   }
/*      */ 
/*      */   public boolean isDumpUtterance()
/*      */   {
/*  935 */     return this.dumpUtterance;
/*      */   }
/*      */ 
/*      */   public void setDumpUtterance(boolean dumpUtterance)
/*      */   {
/*  944 */     this.dumpUtterance = dumpUtterance;
/*  945 */     if (LOGGER.isLoggable(Level.FINE))
/*  946 */       LOGGER.fine("DumpUtterance mode is " + dumpUtterance);
/*      */   }
/*      */ 
/*      */   public boolean isDumpRelations()
/*      */   {
/*  956 */     return this.dumpRelations;
/*      */   }
/*      */ 
/*      */   public void setDumpRelations(boolean dumpRelations)
/*      */   {
/*  965 */     this.dumpRelations = dumpRelations;
/*  966 */     if (LOGGER.isLoggable(Level.FINE))
/*  967 */       LOGGER.fine("DumpRelations mode is " + dumpRelations);
/*      */   }
/*      */ 
/*      */   public void setRunTitle(String runTitle)
/*      */   {
/*  977 */     this.runTitle = runTitle;
/*      */   }
/*      */ 
/*      */   public String getRunTitle()
/*      */   {
/*  986 */     return this.runTitle;
/*      */   }
/*      */ 
/*      */   public String getPhoneFeature(String phone, String featureName)
/*      */   {
/*  998 */     return null;
/*      */   }
/*      */ 
/*      */   public void deallocate()
/*      */   {
/* 1005 */     setLoaded(false);
/*      */ 
/* 1007 */     if ((!this.externalAudioPlayer) && 
/* 1008 */       (this.audioPlayer != null)) {
/* 1009 */       this.audioPlayer.close();
/* 1010 */       this.audioPlayer = null;
/*      */     }
/*      */ 
/* 1014 */     if (!this.externalOutputQueue)
/* 1015 */       this.outputQueue.close();
/*      */   }
/*      */ 
/*      */   public void setPitch(float hertz)
/*      */   {
/* 1025 */     this.pitch = hertz;
/*      */   }
/*      */ 
/*      */   public float getPitch()
/*      */   {
/* 1034 */     return this.pitch;
/*      */   }
/*      */ 
/*      */   public void setPitchRange(float range)
/*      */   {
/* 1043 */     this.range = range;
/*      */   }
/*      */ 
/*      */   public float getPitchRange()
/*      */   {
/* 1052 */     return this.range;
/*      */   }
/*      */ 
/*      */   public void setPitchShift(float shift)
/*      */   {
/* 1061 */     this.pitchShift = shift;
/*      */   }
/*      */ 
/*      */   public float getPitchShift()
/*      */   {
/* 1070 */     return this.pitchShift;
/*      */   }
/*      */ 
/*      */   public void setDurationStretch(float stretch)
/*      */   {
/* 1079 */     this.durationStretch = stretch;
/*      */   }
/*      */ 
/*      */   public float getDurationStretch()
/*      */   {
/* 1088 */     return this.durationStretch;
/*      */   }
/*      */ 
/*      */   public void setRate(float wpm)
/*      */   {
/* 1097 */     if ((wpm > 0.0F) && (wpm < 1000.0F))
/* 1098 */       setDurationStretch(this.nominalRate / wpm);
/*      */   }
/*      */ 
/*      */   public float getRate()
/*      */   {
/* 1108 */     return this.durationStretch * this.nominalRate;
/*      */   }
/*      */ 
/*      */   public void setVolume(float vol)
/*      */   {
/* 1118 */     this.volume = vol;
/*      */   }
/*      */ 
/*      */   public float getVolume()
/*      */   {
/* 1127 */     return this.volume;
/*      */   }
/*      */ 
/*      */   public Lexicon getLexicon()
/*      */   {
/* 1136 */     return this.lexicon;
/*      */   }
/*      */ 
/*      */   public void setLexicon(Lexicon lexicon)
/*      */   {
/* 1145 */     this.lexicon = lexicon;
/*      */   }
/*      */ 
/*      */   public void setWaveDumpFile(String waveDumpFile)
/*      */   {
/* 1155 */     this.waveDumpFile = waveDumpFile;
/*      */   }
/*      */ 
/*      */   public String getWaveDumpFile()
/*      */   {
/* 1164 */     return this.waveDumpFile;
/*      */   }
/*      */ 
/*      */   public void setAudioPlayer(AudioPlayer player)
/*      */   {
/* 1174 */     this.audioPlayer = player;
/* 1175 */     this.externalAudioPlayer = true;
/*      */   }
/*      */ 
/*      */   public AudioPlayer getDefaultAudioPlayer()
/*      */     throws InstantiationException
/*      */   {
/* 1192 */     if (this.defaultAudioPlayer != null) {
/* 1193 */       return this.defaultAudioPlayer;
/*      */     }
/*      */ 
/* 1196 */     String className = Utilities.getProperty("com.sun.speech.freetts.voice.defaultAudioPlayer", "com.sun.speech.freetts.audio.JavaStreamingAudioPlayer");
/*      */     try
/*      */     {
/* 1200 */       Class cls = Class.forName(className);
/* 1201 */       this.defaultAudioPlayer = ((AudioPlayer)cls.newInstance());
/* 1202 */       return this.defaultAudioPlayer;
/*      */     } catch (ClassNotFoundException e) {
/* 1204 */       throw new InstantiationException("Can't find class " + className);
/*      */     } catch (IllegalAccessException e) {
/* 1206 */       throw new InstantiationException("Can't find class " + className);
/*      */     } catch (ClassCastException e) {
/* 1208 */       throw new InstantiationException(className + " cannot be cast " + "to AudioPlayer");
/*      */     }
/*      */   }
/*      */ 
/*      */   public AudioPlayer getAudioPlayer()
/*      */   {
/* 1222 */     if (this.audioPlayer == null) {
/*      */       try {
/* 1224 */         this.audioPlayer = getDefaultAudioPlayer();
/*      */       } catch (InstantiationException e) {
/* 1226 */         e.printStackTrace();
/*      */       }
/*      */     }
/* 1229 */     return this.audioPlayer;
/*      */   }
/*      */ 
/*      */   protected URL getResource(String resource)
/*      */   {
/* 1239 */     return super.getClass().getResource(resource);
/*      */   }
/*      */ 
/*      */   protected void setName(String name)
/*      */   {
/* 1249 */     this.name = name;
/*      */   }
/*      */ 
/*      */   public String getName()
/*      */   {
/* 1259 */     return this.name;
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 1268 */     return getName();
/*      */   }
/*      */ 
/*      */   protected void setGender(Gender gender)
/*      */   {
/* 1277 */     this.gender = gender;
/*      */   }
/*      */ 
/*      */   public Gender getGender()
/*      */   {
/* 1286 */     return this.gender;
/*      */   }
/*      */ 
/*      */   protected void setAge(Age age)
/*      */   {
/* 1295 */     this.age = age;
/*      */   }
/*      */ 
/*      */   public Age getAge()
/*      */   {
/* 1304 */     return this.age;
/*      */   }
/*      */ 
/*      */   protected void setDescription(String description)
/*      */   {
/* 1313 */     this.description = description;
/*      */   }
/*      */ 
/*      */   public String getDescription()
/*      */   {
/* 1322 */     return this.description;
/*      */   }
/*      */ 
/*      */   protected void setLocale(Locale locale)
/*      */   {
/* 1331 */     this.locale = locale;
/*      */   }
/*      */ 
/*      */   public Locale getLocale()
/*      */   {
/* 1340 */     return this.locale;
/*      */   }
/*      */ 
/*      */   protected void setDomain(String domain)
/*      */   {
/* 1351 */     this.domain = domain;
/*      */   }
/*      */ 
/*      */   public String getDomain()
/*      */   {
/* 1362 */     return this.domain;
/*      */   }
/*      */ 
/*      */   public void setStyle(String style)
/*      */   {
/* 1373 */     this.style = style;
/*      */   }
/*      */ 
/*      */   public String getStyle()
/*      */   {
/* 1382 */     return this.style;
/*      */   }
/*      */ 
/*      */   protected void setOrganization(String organization)
/*      */   {
/* 1392 */     this.organization = organization;
/*      */   }
/*      */ 
/*      */   public String getOrganization()
/*      */   {
/* 1402 */     return this.organization;
/*      */   }
/*      */ 
/*      */   protected abstract UtteranceProcessor getAudioOutput()
/*      */     throws IOException;
/*      */ 
/*      */   private class FreeTTSSpeakableTokenizer
/*      */   {
/*      */     FreeTTSSpeakable speakable;
/* 1421 */     Tokenizer tok = Voice.this.getTokenizer();
/*      */ 
/*      */     public FreeTTSSpeakableTokenizer(FreeTTSSpeakable speakable)
/*      */     {
/* 1429 */       this.speakable = speakable;
/* 1430 */       if (speakable.isPlainText()) {
/* 1431 */         this.tok.setInputText(speakable.getText());
/* 1432 */       } else if (speakable.isStream()) {
/* 1433 */         Reader reader = new BufferedReader(new InputStreamReader(speakable.getInputStream()));
/*      */ 
/* 1435 */         this.tok.setInputReader(reader);
/* 1436 */       } else if (speakable.isDocument()) {
/* 1437 */         this.tok.setInputText(Voice.this.documentToString(speakable.getDocument()));
/*      */       }
/*      */     }
/*      */ 
/*      */     public Iterator iterator()
/*      */     {
/* 1445 */       return new Voice.2(this);
/*      */     }
/*      */   }
/*      */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Voice
 * JD-Core Version:    0.5.4
 */