/*     */ package com.sun.speech.freetts.diphone;
/*     */ 
/*     */ import com.sun.speech.freetts.relp.Sample;
/*     */ import com.sun.speech.freetts.relp.SampleInfo;
/*     */ import com.sun.speech.freetts.util.BulkTimer;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.lang.ref.Reference;
/*     */ import java.lang.ref.WeakReference;
/*     */ import java.net.URL;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.MappedByteBuffer;
/*     */ import java.nio.channels.FileChannel;
/*     */ import java.nio.channels.FileChannel.MapMode;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.Map;
/*     */ import java.util.NoSuchElementException;
/*     */ import java.util.Set;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ public class DiphoneUnitDatabase
/*     */ {
/*  97 */   private static final Logger LOGGER = Logger.getLogger(DiphoneUnitDatabase.class.getName());
/*     */   private String name;
/*     */   private int sampleRate;
/*     */   private int numChannels;
/* 103 */   private int residualFold = 1;
/*     */   private float lpcMin;
/*     */   private float lpcRange;
/* 106 */   private int lineCount = 0;
/*     */   private Diphone defaultDiphone;
/* 108 */   private Map diphoneMap = null;
/*     */   private Map diphoneIndex;
/*     */   private SampleInfo sampleInfo;
/* 112 */   private boolean useNewIO = Utilities.getProperty("com.sun.speech.freetts.useNewIO", "true").equals("true");
/*     */ 
/* 116 */   private String cacheType = Utilities.getProperty("com.sun.speech.freetts.diphone.UnitDatabase.cacheType", "preload");
/*     */ 
/* 120 */   private boolean useIndexing = !this.cacheType.equals("preload");
/* 121 */   private boolean useCache = !this.cacheType.equals("demand");
/* 122 */   private boolean useSoftCache = this.cacheType.equals("soft");
/*     */   private static final int MAGIC = -17958194;
/*     */   private static final int INDEX_MAGIC = 16435934;
/*     */   private static final int VERSION = 1;
/*     */   private static final int MAX_DB_SIZE = 4194304;
/* 129 */   private String indexName = null;
/* 130 */   private MappedByteBuffer mbb = null;
/* 131 */   private int defaultIndex = -1;
/*     */ 
/*     */   public DiphoneUnitDatabase(URL url, boolean isBinary)
/*     */     throws IOException
/*     */   {
/* 149 */     this.diphoneMap = new LinkedHashMap();
/*     */ 
/* 151 */     InputStream is = Utilities.getInputStream(url);
/*     */ 
/* 153 */     this.indexName = getIndexName(url.toString());
/*     */ 
/* 155 */     if (isBinary)
/* 156 */       loadBinary(is);
/*     */     else {
/* 158 */       loadText(is);
/*     */     }
/* 160 */     is.close();
/* 161 */     this.sampleInfo = new SampleInfo(this.sampleRate, this.numChannels, this.residualFold, this.lpcMin, this.lpcRange, 0.0F);
/*     */   }
/*     */ 
/*     */   SampleInfo getSampleInfo()
/*     */   {
/* 173 */     return this.sampleInfo;
/*     */   }
/*     */ 
/*     */   private String getIndexName(String databaseName)
/*     */   {
/* 189 */     String indexName = null;
/* 190 */     if (databaseName.lastIndexOf(".") != -1) {
/* 191 */       indexName = databaseName.substring(0, databaseName.lastIndexOf(".")) + ".idx";
/*     */     }
/*     */ 
/* 194 */     return indexName;
/*     */   }
/*     */ 
/*     */   private void loadText(InputStream is)
/*     */   {
/* 206 */     if (is == null) {
/* 207 */       throw new Error("Can't load diphone db file.");
/*     */     }
/*     */ 
/* 210 */     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
/*     */     try {
/* 212 */       String line = reader.readLine();
/* 213 */       this.lineCount += 1;
/* 214 */       while (line != null) {
/* 215 */         if (!line.startsWith("***")) {
/* 216 */           parseAndAdd(line, reader);
/*     */         }
/* 218 */         line = reader.readLine();
/*     */       }
/* 220 */       reader.close();
/*     */     } catch (IOException e) {
/* 222 */       throw new Error(e.getMessage() + " at line " + this.lineCount);
/*     */     }
/*     */     finally
/*     */     {
/*     */     }
/*     */     String line;
/*     */   }
/*     */ 
/*     */   private void parseAndAdd(String line, BufferedReader reader)
/*     */   {
/*     */     try
/*     */     {
/* 236 */       StringTokenizer tokenizer = new StringTokenizer(line, " ");
/* 237 */       String tag = tokenizer.nextToken();
/* 238 */       if (tag.equals("NAME")) {
/* 239 */         this.name = tokenizer.nextToken();
/* 240 */       } else if (tag.equals("SAMPLE_RATE")) {
/* 241 */         this.sampleRate = Integer.parseInt(tokenizer.nextToken());
/* 242 */       } else if (tag.equals("NUM_CHANNELS")) {
/* 243 */         this.numChannels = Integer.parseInt(tokenizer.nextToken());
/* 244 */       } else if (tag.equals("LPC_MIN")) {
/* 245 */         this.lpcMin = Float.parseFloat(tokenizer.nextToken());
/* 246 */       } else if (tag.equals("COEFF_MIN")) {
/* 247 */         this.lpcMin = Float.parseFloat(tokenizer.nextToken());
/* 248 */       } else if (tag.equals("COEFF_RANGE")) {
/* 249 */         this.lpcRange = Float.parseFloat(tokenizer.nextToken());
/* 250 */       } else if (tag.equals("LPC_RANGE")) {
/* 251 */         this.lpcRange = Float.parseFloat(tokenizer.nextToken());
/* 252 */       } else if (tag.equals("ALIAS")) {
/* 253 */         String name = tokenizer.nextToken();
/* 254 */         String origName = tokenizer.nextToken();
/* 255 */         AliasDiphone diphone = new AliasDiphone(name, origName);
/* 256 */         add(diphone);
/* 257 */       } else if (tag.equals("DIPHONE")) {
/* 258 */         String name = tokenizer.nextToken();
/* 259 */         int start = Integer.parseInt(tokenizer.nextToken());
/* 260 */         int mid = Integer.parseInt(tokenizer.nextToken());
/* 261 */         int end = Integer.parseInt(tokenizer.nextToken());
/* 262 */         int numSamples = end - start;
/* 263 */         int midPoint = mid - start;
/*     */ 
/* 265 */         if (this.numChannels <= 0) {
/* 266 */           throw new Error("For diphone '" + name + "': Bad number of channels " + this.numChannels);
/*     */         }
/*     */ 
/* 269 */         if (numSamples <= 0) {
/* 270 */           throw new Error("For diphone '" + name + "': Bad number of samples " + numSamples);
/*     */         }
/*     */ 
/* 273 */         Sample[] samples = new Sample[numSamples];
/*     */ 
/* 275 */         for (int i = 0; i < samples.length; ++i) {
/* 276 */           samples[i] = new Sample(reader, this.numChannels);
/*     */         }
/* 278 */         Diphone diphone = new Diphone(name, samples, midPoint);
/* 279 */         add(diphone);
/*     */       } else {
/* 281 */         throw new Error("Unsupported tag " + tag);
/*     */       }
/*     */     } catch (NoSuchElementException nse) {
/* 284 */       throw new Error("Error parsing db " + nse.getMessage());
/*     */     } catch (NumberFormatException nfe) {
/* 286 */       throw new Error("Error parsing numbers in db " + nfe.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   private void add(Diphone diphone)
/*     */   {
/* 298 */     if (diphone instanceof AliasDiphone) {
/* 299 */       AliasDiphone adiph = (AliasDiphone)diphone;
/* 300 */       Diphone original = (Diphone)this.diphoneMap.get(adiph.getOriginalName());
/*     */ 
/* 302 */       if (original != null) {
/* 303 */         adiph.setOriginalDiphone(original);
/*     */       }
/*     */       else
/*     */       {
/* 307 */         if (LOGGER.isLoggable(Level.FINER)) {
/* 308 */           LOGGER.finer("For diphone alias " + adiph.getName() + ", could not find original " + adiph.getOriginalName());
/*     */         }
/*     */ 
/* 312 */         return;
/*     */       }
/*     */     }
/* 315 */     this.diphoneMap.put(diphone.getName(), diphone);
/* 316 */     if (this.defaultDiphone == null)
/* 317 */       this.defaultDiphone = diphone;
/*     */   }
/*     */ 
/*     */   public Diphone getUnit(String unitName)
/*     */   {
/* 329 */     Diphone diphone = null;
/*     */ 
/* 331 */     if (this.useIndexing) {
/* 332 */       diphone = getFromCache(unitName);
/* 333 */       if (diphone == null) {
/* 334 */         int index = getIndex(unitName);
/* 335 */         if (index != -1) {
/* 336 */           this.mbb.position(index);
/*     */           try {
/* 338 */             diphone = Diphone.loadBinary(this.mbb);
/* 339 */             if (diphone != null)
/*     */             {
/* 341 */               if (diphone instanceof AliasDiphone) {
/* 342 */                 AliasDiphone adiph = (AliasDiphone)diphone;
/* 343 */                 Diphone original = getUnit(adiph.getOriginalName());
/* 344 */                 if (original != null) {
/* 345 */                   adiph.setOriginalDiphone(original);
/* 346 */                   putIntoCache(unitName, adiph);
/*     */                 }
/*     */                 else
/*     */                 {
/* 350 */                   if (LOGGER.isLoggable(Level.FINER)) {
/* 351 */                     LOGGER.finer("For diphone alias " + adiph.getName() + ", could not find original " + adiph.getOriginalName());
/*     */                   }
/*     */ 
/* 355 */                   diphone = null;
/*     */                 }
/*     */               } else {
/* 358 */                 putIntoCache(unitName, diphone);
/*     */               }
/*     */             }
/*     */           } catch (IOException ioe) {
/* 362 */             System.err.println("Can't load diphone " + unitName);
/*     */ 
/* 364 */             diphone = null;
/*     */           }
/*     */         }
/*     */       }
/*     */     } else {
/* 369 */       diphone = (Diphone)this.diphoneMap.get(unitName);
/*     */     }
/*     */ 
/* 372 */     if (diphone == null) {
/* 373 */       System.err.println("Can't find diphone " + unitName);
/* 374 */       diphone = this.defaultDiphone;
/*     */     }
/*     */ 
/* 377 */     return diphone;
/*     */   }
/*     */ 
/*     */   private Diphone getFromCache(String name)
/*     */   {
/* 399 */     if (this.diphoneMap == null) {
/* 400 */       return null;
/*     */     }
/* 402 */     Diphone diphone = null;
/*     */ 
/* 404 */     if (this.useSoftCache) {
/* 405 */       Reference ref = (Reference)this.diphoneMap.get(name);
/* 406 */       if (ref != null) {
/* 407 */         diphone = (Diphone)ref.get();
/* 408 */         if (diphone == null)
/* 409 */           this.diphoneMap.remove(name);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 414 */       diphone = (Diphone)this.diphoneMap.get(name);
/*     */     }
/* 416 */     return diphone;
/*     */   }
/*     */ 
/*     */   private void putIntoCache(String diphoneName, Diphone diphone)
/*     */   {
/* 426 */     if (this.diphoneMap == null) {
/* 427 */       return;
/*     */     }
/* 429 */     if (this.useSoftCache)
/* 430 */       this.diphoneMap.put(diphoneName, new WeakReference(diphone));
/*     */     else
/* 432 */       this.diphoneMap.put(diphoneName, diphone);
/*     */   }
/*     */ 
/*     */   private void dumpCacheSize()
/*     */   {
/* 440 */     int empty = 0;
/* 441 */     int full = 0;
/* 442 */     System.out.println("Entries: " + this.diphoneMap.size());
/* 443 */     for (Iterator i = this.diphoneMap.values().iterator(); i.hasNext(); ) {
/* 444 */       Reference ref = (Reference)i.next();
/* 445 */       if (ref.get() == null)
/* 446 */         ++empty;
/*     */       else {
/* 448 */         ++full;
/*     */       }
/*     */     }
/* 451 */     System.out.println("   empty: " + empty);
/* 452 */     System.out.println("    full: " + full);
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 460 */     return this.name;
/*     */   }
/*     */ 
/*     */   public void dump()
/*     */   {
/* 467 */     System.out.println("Name        " + this.name);
/* 468 */     System.out.println("SampleRate  " + this.sampleRate);
/* 469 */     System.out.println("NumChannels " + this.numChannels);
/* 470 */     System.out.println("lpcMin      " + this.lpcMin);
/* 471 */     System.out.println("lpcRange    " + this.lpcRange);
/*     */ 
/* 473 */     for (Iterator i = this.diphoneMap.values().iterator(); i.hasNext(); ) {
/* 474 */       Diphone diphone = (Diphone)i.next();
/* 475 */       diphone.dump();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void dumpBinary(String path)
/*     */   {
/*     */     try
/*     */     {
/* 486 */       FileOutputStream fos = new FileOutputStream(path);
/* 487 */       DataOutputStream os = new DataOutputStream(fos);
/*     */ 
/* 490 */       os.writeInt(-17958194);
/* 491 */       os.writeInt(1);
/* 492 */       os.writeInt(this.sampleRate);
/* 493 */       os.writeInt(this.numChannels);
/* 494 */       os.writeFloat(this.lpcMin);
/* 495 */       os.writeFloat(this.lpcRange);
/* 496 */       os.writeInt(this.diphoneMap.size());
/*     */ 
/* 498 */       for (Iterator i = this.diphoneMap.values().iterator(); i.hasNext(); ) {
/* 499 */         Diphone diphone = (Diphone)i.next();
/* 500 */         diphone.dumpBinary(os);
/*     */       }
/* 502 */       os.flush();
/* 503 */       fos.close();
/*     */     }
/*     */     catch (FileNotFoundException fe) {
/* 506 */       throw new Error("Can't dump binary database " + fe.getMessage());
/*     */     }
/*     */     catch (IOException ioe) {
/* 509 */       throw new Error("Can't write binary database " + ioe.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   void dumpBinaryIndex(String path)
/*     */   {
/*     */     try
/*     */     {
/* 527 */       FileOutputStream fos = new FileOutputStream(path);
/* 528 */       DataOutputStream dos = new DataOutputStream(fos);
/*     */ 
/* 530 */       dos.writeInt(16435934);
/* 531 */       dos.writeInt(this.diphoneIndex.keySet().size());
/*     */ 
/* 533 */       for (Iterator i = this.diphoneIndex.keySet().iterator(); i.hasNext(); ) {
/* 534 */         String key = (String)i.next();
/* 535 */         int pos = ((Integer)this.diphoneIndex.get(key)).intValue();
/* 536 */         dos.writeUTF(key);
/* 537 */         dos.writeInt(pos);
/*     */       }
/* 539 */       dos.close();
/*     */     }
/*     */     catch (FileNotFoundException fe) {
/* 542 */       throw new Error("Can't dump binary index " + fe.getMessage());
/*     */     }
/*     */     catch (IOException ioe) {
/* 545 */       throw new Error("Can't write binary index " + ioe.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   private void loadBinaryIndex(URL url)
/*     */   {
/* 557 */     this.diphoneIndex = new HashMap();
/*     */     try
/*     */     {
/* 560 */       InputStream is = Utilities.getInputStream(url);
/* 561 */       DataInputStream dis = new DataInputStream(is);
/*     */ 
/* 563 */       if (dis.readInt() != 16435934) {
/* 564 */         throw new Error("Bad index file format");
/*     */       }
/*     */ 
/* 567 */       int size = dis.readInt();
/*     */ 
/* 569 */       for (int i = 0; i < size; ++i) {
/* 570 */         String diphoneName = dis.readUTF();
/* 571 */         int pos = dis.readInt();
/* 572 */         this.diphoneIndex.put(diphoneName, new Integer(pos));
/*     */       }
/* 574 */       dis.close();
/*     */     }
/*     */     catch (FileNotFoundException fe) {
/* 577 */       throw new Error("Can't load binary index " + fe.getMessage());
/*     */     }
/*     */     catch (IOException ioe) {
/* 580 */       throw new Error("Can't read binary index " + ioe.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   private int getIndex(String diphone)
/*     */   {
/* 593 */     Integer index = (Integer)this.diphoneIndex.get(diphone);
/* 594 */     if (index != null) {
/* 595 */       int idx = index.intValue();
/* 596 */       if (this.defaultIndex == -1) {
/* 597 */         this.defaultIndex = idx;
/*     */       }
/* 599 */       return idx;
/*     */     }
/* 601 */     System.out.println("Can't find index entry for " + diphone);
/* 602 */     return this.defaultIndex;
/*     */   }
/*     */ 
/*     */   private void loadBinary(InputStream is)
/*     */     throws IOException
/*     */   {
/* 626 */     if ((this.useNewIO) && (is instanceof FileInputStream)) {
/* 627 */       FileInputStream fis = (FileInputStream)is;
/* 628 */       if (this.useIndexing) {
/* 629 */         loadBinaryIndex(new URL(this.indexName));
/* 630 */         mapDatabase(fis);
/*     */       } else {
/* 632 */         loadMappedBinary(fis);
/*     */       }
/*     */     } else {
/* 635 */       this.useIndexing = false;
/* 636 */       DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
/*     */ 
/* 638 */       loadBinary(dis);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void loadBinary(DataInputStream dis)
/*     */     throws IOException
/*     */   {
/* 650 */     if (dis.readInt() != -17958194) {
/* 651 */       throw new Error("Bad magic in db");
/*     */     }
/* 653 */     if (dis.readInt() != 1) {
/* 654 */       throw new Error("Bad VERSION in db");
/*     */     }
/*     */ 
/* 657 */     this.sampleRate = dis.readInt();
/* 658 */     this.numChannels = dis.readInt();
/* 659 */     this.lpcMin = dis.readFloat();
/* 660 */     this.lpcRange = dis.readFloat();
/* 661 */     int size = dis.readInt();
/*     */ 
/* 663 */     for (int i = 0; i < size; ++i) {
/* 664 */       Diphone diphone = Diphone.loadBinary(dis);
/* 665 */       add(diphone);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void loadMappedBinary(FileInputStream is)
/*     */     throws IOException
/*     */   {
/* 678 */     FileChannel fc = is.getChannel();
/*     */ 
/* 680 */     MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0L, (int)fc.size());
/*     */ 
/* 682 */     bb.load();
/* 683 */     loadDatabase(bb);
/* 684 */     is.close();
/*     */   }
/*     */ 
/*     */   private void mapDatabase(FileInputStream is)
/*     */     throws IOException
/*     */   {
/* 695 */     FileChannel fc = is.getChannel();
/* 696 */     this.mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0L, (int)fc.size());
/* 697 */     this.mbb.load();
/* 698 */     loadDatabaseHeader(this.mbb);
/*     */   }
/*     */ 
/*     */   private void loadDatabaseHeader(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 709 */     if (bb.getInt() != -17958194) {
/* 710 */       throw new Error("Bad magic in db");
/*     */     }
/* 712 */     if (bb.getInt() != 1) {
/* 713 */       throw new Error("Bad VERSION in db");
/*     */     }
/*     */ 
/* 716 */     this.sampleRate = bb.getInt();
/* 717 */     this.numChannels = bb.getInt();
/* 718 */     this.lpcMin = bb.getFloat();
/* 719 */     this.lpcRange = bb.getFloat();
/*     */   }
/*     */ 
/*     */   private void loadDatabase(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 731 */     loadDatabaseHeader(bb);
/* 732 */     int size = bb.getInt();
/*     */ 
/* 734 */     this.diphoneIndex = new HashMap();
/* 735 */     for (int i = 0; i < size; ++i) {
/* 736 */       int pos = bb.position();
/* 737 */       Diphone diphone = Diphone.loadBinary(bb);
/* 738 */       add(diphone);
/* 739 */       this.diphoneIndex.put(diphone.getName(), new Integer(pos));
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean compare(DiphoneUnitDatabase other)
/*     */   {
/* 755 */     if (this.sampleRate != other.sampleRate) {
/* 756 */       return false;
/*     */     }
/*     */ 
/* 759 */     if (this.numChannels != other.numChannels) {
/* 760 */       return false;
/*     */     }
/*     */ 
/* 763 */     if (this.lpcMin != other.lpcMin) {
/* 764 */       return false;
/*     */     }
/*     */ 
/* 767 */     if (this.lpcRange != other.lpcRange) {
/* 768 */       return false;
/*     */     }
/*     */ 
/* 771 */     for (Iterator i = this.diphoneMap.values().iterator(); i.hasNext(); ) {
/* 772 */       Diphone diphone = (Diphone)i.next();
/* 773 */       Diphone otherDiphone = other.getUnit(diphone.getName());
/* 774 */       if (!diphone.compare(otherDiphone)) {
/* 775 */         System.out.println("Diphones differ:");
/* 776 */         System.out.println("THis:");
/* 777 */         diphone.dump();
/* 778 */         System.out.println("Other:");
/* 779 */         otherDiphone.dump();
/* 780 */         return false;
/*     */       }
/*     */     }
/* 783 */     return true;
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 820 */     boolean showTimes = false;
/* 821 */     String srcPath = ".";
/* 822 */     String destPath = ".";
/*     */     try
/*     */     {
/* 825 */       if (args.length > 0) {
/* 826 */         BulkTimer timer = BulkTimer.LOAD;
/* 827 */         timer.start();
/* 828 */         for (int i = 0; i < args.length; ++i) {
/* 829 */           if (args[i].equals("-src")) {
/* 830 */             srcPath = args[(++i)];
/* 831 */           } else if (args[i].equals("-dest")) {
/* 832 */             destPath = args[(++i)];
/* 833 */           } else if (args[i].equals("-generate_binary")) {
/* 834 */             String name = "diphone_units.txt";
/* 835 */             if (i + 1 < args.length) {
/* 836 */               String nameArg = args[(++i)];
/* 837 */               if (!nameArg.startsWith("-")) {
/* 838 */                 name = nameArg;
/*     */               }
/*     */             }
/*     */ 
/* 842 */             int suffixPos = name.lastIndexOf(".txt");
/*     */ 
/* 844 */             String binaryName = "diphone_units.bin";
/* 845 */             if (suffixPos != -1) {
/* 846 */               binaryName = name.substring(0, suffixPos) + ".bin";
/*     */             }
/*     */ 
/* 849 */             String indexName = "diphone_units.idx";
/*     */ 
/* 851 */             if (suffixPos != -1) {
/* 852 */               indexName = name.substring(0, suffixPos) + ".idx";
/*     */             }
/*     */ 
/* 855 */             System.out.println("Loading " + name);
/* 856 */             timer.start("load_text");
/* 857 */             DiphoneUnitDatabase udb = new DiphoneUnitDatabase(new URL("file:" + srcPath + "/" + name), false);
/*     */ 
/* 860 */             timer.stop("load_text");
/*     */ 
/* 862 */             System.out.println("Dumping " + binaryName);
/* 863 */             timer.start("dump_binary");
/* 864 */             udb.dumpBinary(destPath + "/" + binaryName);
/* 865 */             timer.stop("dump_binary");
/*     */ 
/* 867 */             timer.start("load_binary");
/* 868 */             DiphoneUnitDatabase budb = new DiphoneUnitDatabase(new URL("file:" + destPath + "/" + binaryName), true);
/*     */ 
/* 873 */             timer.stop("load_binary");
/*     */ 
/* 875 */             System.out.println("Dumping " + indexName);
/* 876 */             timer.start("dump index");
/* 877 */             budb.dumpBinaryIndex(destPath + "/" + indexName);
/* 878 */             timer.stop("dump index");
/* 879 */           } else if (args[i].equals("-compare"))
/*     */           {
/* 881 */             timer.start("load_text");
/* 882 */             DiphoneUnitDatabase udb = new DiphoneUnitDatabase(new URL("file:./diphone_units.txt"), false);
/*     */ 
/* 884 */             timer.stop("load_text");
/*     */ 
/* 886 */             timer.start("load_binary");
/* 887 */             DiphoneUnitDatabase budb = new DiphoneUnitDatabase(new URL("file:./diphone_units.bin"), true);
/*     */ 
/* 890 */             timer.stop("load_binary");
/*     */ 
/* 892 */             timer.start("compare");
/* 893 */             if (udb.compare(budb))
/* 894 */               System.out.println("other compare ok");
/*     */             else {
/* 896 */               System.out.println("other compare different");
/*     */             }
/* 898 */             timer.stop("compare");
/* 899 */           } else if (args[i].equals("-showtimes")) {
/* 900 */             showTimes = true;
/*     */           } else {
/* 902 */             System.out.println("Unknown option " + args[i]);
/*     */           }
/*     */         }
/* 905 */         timer.stop();
/* 906 */         if (showTimes)
/* 907 */           timer.show("DiphoneUnitDatabase");
/*     */       }
/*     */       else {
/* 910 */         System.out.println("Options: ");
/* 911 */         System.out.println("    -src path");
/* 912 */         System.out.println("    -dest path");
/* 913 */         System.out.println("    -compare");
/* 914 */         System.out.println("    -generate_binary");
/* 915 */         System.out.println("    -showTimes");
/*     */       }
/*     */     } catch (IOException ioe) {
/* 918 */       System.err.println(ioe);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.diphone.DiphoneUnitDatabase
 * JD-Core Version:    0.5.4
 */