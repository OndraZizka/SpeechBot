/*     */ package com.sun.speech.freetts.clunits;
/*     */ 
/*     */ import com.sun.speech.freetts.cart.CART;
/*     */ import com.sun.speech.freetts.cart.CARTImpl;
/*     */ import com.sun.speech.freetts.relp.SampleInfo;
/*     */ import com.sun.speech.freetts.relp.SampleSet;
/*     */ import com.sun.speech.freetts.util.BulkTimer;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.BufferedOutputStream;
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
/*     */ import java.net.URL;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.MappedByteBuffer;
/*     */ import java.nio.channels.FileChannel;
/*     */ import java.nio.channels.FileChannel.MapMode;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.NoSuchElementException;
/*     */ import java.util.Set;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public class ClusterUnitDatabase
/*     */ {
/*     */   static final int CLUNIT_NONE = 65535;
/*     */   private DatabaseClusterUnit[] units;
/*     */   private UnitType[] unitTypes;
/*     */   private SampleSet sts;
/*     */   private SampleSet mcep;
/*     */   private UnitOriginInfo[] unitOrigins;
/*     */   private int continuityWeight;
/*     */   private int optimalCoupling;
/*     */   private int extendSelections;
/*     */   private int joinMethod;
/*     */   private int[] joinWeights;
/*     */   private int joinWeightShift;
/*  72 */   private Map cartMap = new HashMap();
/*  73 */   private CART defaultCart = null;
/*     */   private transient List unitList;
/*     */   private transient int lineCount;
/*     */   private transient List unitTypesList;
/*     */   private static final int MAGIC = -255144934;
/*     */   private static final int VERSION = 4096;
/*     */ 
/*     */   ClusterUnitDatabase(URL url, boolean isBinary)
/*     */     throws IOException
/*     */   {
/*  92 */     BulkTimer.LOAD.start("ClusterUnitDatabase");
/*  93 */     InputStream is = Utilities.getInputStream(url);
/*  94 */     if (isBinary)
/*  95 */       loadBinary(is);
/*     */     else {
/*  97 */       loadText(is);
/*     */     }
/*  99 */     is.close();
/*     */ 
/* 102 */     String urlString = url.toExternalForm();
/* 103 */     URL debugURL = new URL(urlString.substring(0, urlString.lastIndexOf(".")) + ".debug");
/*     */     try {
/* 105 */       InputStream debugInfoStream = Utilities.getInputStream(debugURL);
/* 106 */       loadUnitOrigins(debugInfoStream);
/*     */     }
/*     */     catch (IOException ioe) {
/*     */     }
/* 110 */     BulkTimer.LOAD.stop("ClusterUnitDatabase");
/*     */   }
/*     */ 
/*     */   int getStart(int unitEntry)
/*     */   {
/* 123 */     return this.units[unitEntry].start;
/*     */   }
/*     */ 
/*     */   int getEnd(int unitEntry)
/*     */   {
/* 135 */     return this.units[unitEntry].end;
/*     */   }
/*     */ 
/*     */   int getPhone(int unitEntry)
/*     */   {
/* 146 */     return this.units[unitEntry].phone;
/*     */   }
/*     */ 
/*     */   CART getTree(String unitType)
/*     */   {
/* 157 */     CART cart = (CART)this.cartMap.get(unitType);
/*     */ 
/* 159 */     if (cart == null) {
/* 160 */       System.err.println("ClusterUnitDatabase: can't find tree for " + unitType);
/*     */ 
/* 162 */       return this.defaultCart;
/*     */     }
/* 164 */     return cart;
/*     */   }
/*     */ 
/*     */   int getUnitTypeIndex(String name)
/*     */   {
/* 178 */     int start = 0;
/* 179 */     int end = this.unitTypes.length;
/*     */ 
/* 181 */     while (start < end) {
/* 182 */       int mid = (start + end) / 2;
/* 183 */       int c = this.unitTypes[mid].getName().compareTo(name);
/* 184 */       if (c == 0)
/* 185 */         return mid;
/* 186 */       if (c > 0) {
/* 187 */         end = mid;
/*     */       }
/* 189 */       start = mid + 1;
/*     */     }
/*     */ 
/* 192 */     return -1;
/*     */   }
/*     */ 
/*     */   int getUnitIndex(String unitType, int instance)
/*     */   {
/* 204 */     int i = getUnitTypeIndex(unitType);
/* 205 */     if (i == -1) {
/* 206 */       error("getUnitIndex: can't find unit type " + unitType);
/* 207 */       i = 0;
/*     */     }
/* 209 */     if (instance >= this.unitTypes[i].getCount()) {
/* 210 */       error("getUnitIndex: can't find instance " + instance + " of " + unitType);
/*     */ 
/* 212 */       instance = 0;
/*     */     }
/* 214 */     return this.unitTypes[i].getStart() + instance;
/*     */   }
/*     */ 
/*     */   int getUnitIndexName(String name)
/*     */   {
/* 226 */     int lastIndex = name.lastIndexOf('_');
/* 227 */     if (lastIndex == -1) {
/* 228 */       error("getUnitIndexName: bad unit name " + name);
/* 229 */       return -1;
/*     */     }
/* 231 */     int index = Integer.parseInt(name.substring(lastIndex + 1));
/* 232 */     String type = name.substring(0, lastIndex);
/* 233 */     return getUnitIndex(type, index);
/*     */   }
/*     */ 
/*     */   int getExtendSelections()
/*     */   {
/* 242 */     return this.extendSelections;
/*     */   }
/*     */ 
/*     */   int getNextUnit(int which)
/*     */   {
/* 251 */     return this.units[which].next;
/*     */   }
/*     */ 
/*     */   int getPrevUnit(int which)
/*     */   {
/* 262 */     return this.units[which].prev;
/*     */   }
/*     */ 
/*     */   boolean isUnitTypeEqual(int unitA, int unitB)
/*     */   {
/* 276 */     return this.units[unitA].type == this.units[unitB].type;
/*     */   }
/*     */ 
/*     */   int getOptimalCoupling()
/*     */   {
/* 289 */     return this.optimalCoupling;
/*     */   }
/*     */ 
/*     */   int getContinuityWeight()
/*     */   {
/* 299 */     return this.continuityWeight;
/*     */   }
/*     */ 
/*     */   int[] getJoinWeights()
/*     */   {
/* 308 */     return this.joinWeights;
/*     */   }
/*     */ 
/*     */   DatabaseClusterUnit getUnit(String unitName)
/*     */   {
/* 320 */     return null;
/*     */   }
/*     */ 
/*     */   DatabaseClusterUnit getUnit(int which)
/*     */   {
/* 331 */     return this.units[which];
/*     */   }
/*     */ 
/*     */   UnitOriginInfo getUnitOriginInfo(int which)
/*     */   {
/* 342 */     if (this.unitOrigins != null) {
/* 343 */       return this.unitOrigins[which];
/*     */     }
/* 345 */     return null;
/*     */   }
/*     */ 
/*     */   String getName()
/*     */   {
/* 355 */     return "ClusterUnitDatabase";
/*     */   }
/*     */ 
/*     */   SampleInfo getSampleInfo()
/*     */   {
/* 364 */     return this.sts.getSampleInfo();
/*     */   }
/*     */ 
/*     */   SampleSet getSts()
/*     */   {
/* 374 */     return this.sts;
/*     */   }
/*     */ 
/*     */   SampleSet getMcep()
/*     */   {
/* 383 */     return this.mcep;
/*     */   }
/*     */ 
/*     */   int getJoinWeightShift()
/*     */   {
/* 394 */     return this.joinWeightShift;
/*     */   }
/*     */ 
/*     */   private int calcJoinWeightShift(int[] joinWeights)
/*     */   {
/* 406 */     int first = joinWeights[0];
/* 407 */     for (int i = 1; i < joinWeights.length; ++i) {
/* 408 */       if (joinWeights[i] != first) {
/* 409 */         return 0;
/*     */       }
/*     */     }
/*     */ 
/* 413 */     int divisor = 65536 / first;
/* 414 */     if (divisor == 2)
/* 415 */       return 1;
/* 416 */     if (divisor == 4) {
/* 417 */       return 2;
/*     */     }
/* 419 */     return 0;
/*     */   }
/*     */ 
/*     */   private void loadText(InputStream is)
/*     */   {
/* 432 */     this.unitList = new ArrayList();
/* 433 */     this.unitTypesList = new ArrayList();
/*     */ 
/* 435 */     if (is == null) {
/* 436 */       throw new Error("Can't load cluster db file.");
/*     */     }
/*     */ 
/* 439 */     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
/*     */     try {
/* 441 */       String line = reader.readLine();
/* 442 */       this.lineCount += 1;
/* 443 */       while (line != null) {
/* 444 */         if (!line.startsWith("***")) {
/* 445 */           parseAndAdd(line, reader);
/*     */         }
/* 447 */         line = reader.readLine();
/*     */       }
/* 449 */       reader.close();
/*     */ 
/* 451 */       this.units = new DatabaseClusterUnit[this.unitList.size()];
/* 452 */       this.units = ((DatabaseClusterUnit[])this.unitList.toArray(this.units));
/* 453 */       this.unitList = null;
/*     */ 
/* 455 */       this.unitTypes = new UnitType[this.unitTypesList.size()];
/* 456 */       this.unitTypes = ((UnitType[])this.unitTypesList.toArray(this.unitTypes));
/* 457 */       this.unitTypesList = null;
/*     */     }
/*     */     catch (IOException e) {
/* 460 */       throw new Error(e.getMessage() + " at line " + this.lineCount);
/*     */     }
/*     */     finally
/*     */     {
/*     */     }
/*     */     String line;
/*     */   }
/*     */ 
/*     */   private void parseAndAdd(String line, BufferedReader reader)
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 476 */       StringTokenizer tokenizer = new StringTokenizer(line, " ");
/* 477 */       String tag = tokenizer.nextToken();
/* 478 */       if (tag.equals("CONTINUITY_WEIGHT")) {
/* 479 */         this.continuityWeight = Integer.parseInt(tokenizer.nextToken());
/* 480 */       } else if (tag.equals("OPTIMAL_COUPLING")) {
/* 481 */         this.optimalCoupling = Integer.parseInt(tokenizer.nextToken());
/* 482 */       } else if (tag.equals("EXTEND_SELECTIONS")) {
/* 483 */         this.extendSelections = Integer.parseInt(tokenizer.nextToken());
/* 484 */       } else if (tag.equals("JOIN_METHOD")) {
/* 485 */         this.joinMethod = Integer.parseInt(tokenizer.nextToken());
/* 486 */       } else if (tag.equals("JOIN_WEIGHTS")) {
/* 487 */         int numWeights = Integer.parseInt(tokenizer.nextToken());
/* 488 */         this.joinWeights = new int[numWeights];
/* 489 */         for (int i = 0; i < numWeights; ++i) {
/* 490 */           this.joinWeights[i] = Integer.parseInt(tokenizer.nextToken());
/*     */         }
/*     */ 
/* 493 */         this.joinWeightShift = calcJoinWeightShift(this.joinWeights);
/*     */       }
/* 495 */       else if (tag.equals("STS")) {
/* 496 */         String name = tokenizer.nextToken();
/* 497 */         if (name.equals("STS"))
/* 498 */           this.sts = new SampleSet(tokenizer, reader);
/*     */         else
/* 500 */           this.mcep = new SampleSet(tokenizer, reader);
/*     */       }
/* 502 */       else if (tag.equals("UNITS")) {
/* 503 */         int type = Integer.parseInt(tokenizer.nextToken());
/* 504 */         int phone = Integer.parseInt(tokenizer.nextToken());
/* 505 */         int start = Integer.parseInt(tokenizer.nextToken());
/* 506 */         int end = Integer.parseInt(tokenizer.nextToken());
/* 507 */         int prev = Integer.parseInt(tokenizer.nextToken());
/* 508 */         int next = Integer.parseInt(tokenizer.nextToken());
/* 509 */         DatabaseClusterUnit unit = new DatabaseClusterUnit(type, phone, start, end, prev, next);
/*     */ 
/* 512 */         this.unitList.add(unit);
/* 513 */       } else if (tag.equals("CART")) {
/* 514 */         String name = tokenizer.nextToken();
/* 515 */         int nodes = Integer.parseInt(tokenizer.nextToken());
/* 516 */         CART cart = new CARTImpl(reader, nodes);
/* 517 */         this.cartMap.put(name, cart);
/*     */ 
/* 519 */         if (this.defaultCart == null)
/* 520 */           this.defaultCart = cart;
/*     */       }
/* 522 */       else if (tag.equals("UNIT_TYPE")) {
/* 523 */         String name = tokenizer.nextToken();
/* 524 */         int start = Integer.parseInt(tokenizer.nextToken());
/* 525 */         int count = Integer.parseInt(tokenizer.nextToken());
/* 526 */         UnitType unitType = new UnitType(name, start, count);
/* 527 */         this.unitTypesList.add(unitType);
/*     */       } else {
/* 529 */         throw new Error("Unsupported tag " + tag + " in db line `" + line + "'");
/*     */       }
/*     */     } catch (NoSuchElementException nse) {
/* 532 */       throw new Error("Error parsing db " + nse.getMessage());
/*     */     } catch (NumberFormatException nfe) {
/* 534 */       throw new Error("Error parsing numbers in db line `" + line + "':" + nfe.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   private void loadBinary(InputStream is)
/*     */     throws IOException
/*     */   {
/* 550 */     if (is instanceof FileInputStream) {
/* 551 */       FileInputStream fis = (FileInputStream)is;
/* 552 */       FileChannel fc = fis.getChannel();
/*     */ 
/* 554 */       MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0L, (int)fc.size());
/*     */ 
/* 556 */       bb.load();
/* 557 */       loadBinary(bb);
/* 558 */       is.close();
/*     */     } else {
/* 560 */       loadBinary(new DataInputStream(is));
/*     */     }
/*     */   }
/*     */ 
/*     */   private void loadBinary(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 573 */     if (bb.getInt() != -255144934) {
/* 574 */       throw new Error("Bad magic in db");
/*     */     }
/* 576 */     if (bb.getInt() != 4096) {
/* 577 */       throw new Error("Bad VERSION in db");
/*     */     }
/*     */ 
/* 580 */     this.continuityWeight = bb.getInt();
/* 581 */     this.optimalCoupling = bb.getInt();
/* 582 */     this.extendSelections = bb.getInt();
/* 583 */     this.joinMethod = bb.getInt();
/* 584 */     this.joinWeightShift = bb.getInt();
/*     */ 
/* 586 */     int weightLength = bb.getInt();
/* 587 */     this.joinWeights = new int[weightLength];
/* 588 */     for (int i = 0; i < this.joinWeights.length; ++i) {
/* 589 */       this.joinWeights[i] = bb.getInt();
/*     */     }
/*     */ 
/* 592 */     int unitsLength = bb.getInt();
/* 593 */     this.units = new DatabaseClusterUnit[unitsLength];
/* 594 */     for (int i = 0; i < this.units.length; ++i) {
/* 595 */       this.units[i] = new DatabaseClusterUnit(bb);
/*     */     }
/*     */ 
/* 598 */     int unitTypesLength = bb.getInt();
/* 599 */     this.unitTypes = new UnitType[unitTypesLength];
/* 600 */     for (int i = 0; i < this.unitTypes.length; ++i) {
/* 601 */       this.unitTypes[i] = new UnitType(bb);
/*     */     }
/* 603 */     this.sts = new SampleSet(bb);
/* 604 */     this.mcep = new SampleSet(bb);
/*     */ 
/* 606 */     int numCarts = bb.getInt();
/* 607 */     this.cartMap = new HashMap();
/* 608 */     for (int i = 0; i < numCarts; ++i) {
/* 609 */       String name = Utilities.getString(bb);
/* 610 */       CART cart = CARTImpl.loadBinary(bb);
/* 611 */       this.cartMap.put(name, cart);
/*     */ 
/* 613 */       if (this.defaultCart == null)
/* 614 */         this.defaultCart = cart;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void loadBinary(DataInputStream is)
/*     */     throws IOException
/*     */   {
/* 628 */     if (is.readInt() != -255144934) {
/* 629 */       throw new Error("Bad magic in db");
/*     */     }
/* 631 */     if (is.readInt() != 4096) {
/* 632 */       throw new Error("Bad VERSION in db");
/*     */     }
/*     */ 
/* 635 */     this.continuityWeight = is.readInt();
/* 636 */     this.optimalCoupling = is.readInt();
/* 637 */     this.extendSelections = is.readInt();
/* 638 */     this.joinMethod = is.readInt();
/* 639 */     this.joinWeightShift = is.readInt();
/*     */ 
/* 641 */     int weightLength = is.readInt();
/* 642 */     this.joinWeights = new int[weightLength];
/* 643 */     for (int i = 0; i < this.joinWeights.length; ++i) {
/* 644 */       this.joinWeights[i] = is.readInt();
/*     */     }
/*     */ 
/* 647 */     int unitsLength = is.readInt();
/* 648 */     this.units = new DatabaseClusterUnit[unitsLength];
/* 649 */     for (int i = 0; i < this.units.length; ++i) {
/* 650 */       this.units[i] = new DatabaseClusterUnit(is);
/*     */     }
/*     */ 
/* 653 */     int unitTypesLength = is.readInt();
/* 654 */     this.unitTypes = new UnitType[unitTypesLength];
/* 655 */     for (int i = 0; i < this.unitTypes.length; ++i) {
/* 656 */       this.unitTypes[i] = new UnitType(is);
/*     */     }
/* 658 */     this.sts = new SampleSet(is);
/* 659 */     this.mcep = new SampleSet(is);
/*     */ 
/* 661 */     int numCarts = is.readInt();
/* 662 */     this.cartMap = new HashMap();
/* 663 */     for (int i = 0; i < numCarts; ++i) {
/* 664 */       String name = Utilities.getString(is);
/* 665 */       CART cart = CARTImpl.loadBinary(is);
/* 666 */       this.cartMap.put(name, cart);
/*     */ 
/* 668 */       if (this.defaultCart == null)
/* 669 */         this.defaultCart = cart;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void loadUnitOrigins(InputStream is)
/*     */     throws IOException
/*     */   {
/* 685 */     this.unitOrigins = new UnitOriginInfo[this.units.length];
/* 686 */     BufferedReader in = new BufferedReader(new InputStreamReader(is));
/*     */ 
/* 688 */     String currentLine = null;
/*     */     do
/* 690 */       if ((currentLine = in.readLine()) == null) break;
/* 691 */     while (!currentLine.startsWith("EST_Header_End"));
/*     */ 
/* 693 */     while ((currentLine = in.readLine()) != null) {
/* 694 */       String[] tokens = currentLine.split(" ");
/* 695 */       String name = tokens[0];
/* 696 */       int index = getUnitIndexName(name);
/*     */       try {
/* 698 */         this.unitOrigins[index] = new UnitOriginInfo();
/* 699 */         this.unitOrigins[index].originFile = tokens[1];
/* 700 */         this.unitOrigins[index].originStart = Float.valueOf(tokens[2]).floatValue();
/* 701 */         this.unitOrigins[index].originEnd = Float.valueOf(tokens[4]).floatValue(); } catch (NumberFormatException nfe) {
/*     */       }
/*     */     }
/* 704 */     in.close();
/*     */   }
/*     */ 
/*     */   void dumpBinary(String path)
/*     */   {
/*     */     try
/*     */     {
/* 715 */       FileOutputStream fos = new FileOutputStream(path);
/* 716 */       DataOutputStream os = new DataOutputStream(new BufferedOutputStream(fos));
/*     */ 
/* 719 */       os.writeInt(-255144934);
/* 720 */       os.writeInt(4096);
/* 721 */       os.writeInt(this.continuityWeight);
/* 722 */       os.writeInt(this.optimalCoupling);
/* 723 */       os.writeInt(this.extendSelections);
/* 724 */       os.writeInt(this.joinMethod);
/* 725 */       os.writeInt(this.joinWeightShift);
/* 726 */       os.writeInt(this.joinWeights.length);
/* 727 */       for (int i = 0; i < this.joinWeights.length; ++i) {
/* 728 */         os.writeInt(this.joinWeights[i]);
/*     */       }
/*     */ 
/* 731 */       os.writeInt(this.units.length);
/* 732 */       for (int i = 0; i < this.units.length; ++i) {
/* 733 */         this.units[i].dumpBinary(os);
/*     */       }
/*     */ 
/* 736 */       os.writeInt(this.unitTypes.length);
/* 737 */       for (int i = 0; i < this.unitTypes.length; ++i) {
/* 738 */         this.unitTypes[i].dumpBinary(os);
/*     */       }
/* 740 */       this.sts.dumpBinary(os);
/* 741 */       this.mcep.dumpBinary(os);
/*     */ 
/* 743 */       os.writeInt(this.cartMap.size());
/* 744 */       for (Iterator i = this.cartMap.keySet().iterator(); i.hasNext(); ) {
/* 745 */         String name = (String)i.next();
/* 746 */         CART cart = (CART)this.cartMap.get(name);
/*     */ 
/* 748 */         Utilities.outString(os, name);
/* 749 */         cart.dumpBinary(os);
/*     */       }
/* 751 */       os.close();
/*     */     }
/*     */     catch (FileNotFoundException fe)
/*     */     {
/* 757 */       throw new Error("Can't dump binary database " + fe.getMessage());
/*     */     }
/*     */     catch (IOException ioe) {
/* 760 */       throw new Error("Can't write binary database " + ioe.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean compare(ClusterUnitDatabase other)
/*     */   {
/* 774 */     System.out.println("Warning: Compare not implemented yet");
/* 775 */     return false;
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 806 */     boolean showTimes = false;
/* 807 */     String srcPath = ".";
/* 808 */     String destPath = ".";
/*     */     try
/*     */     {
/* 811 */       if (args.length > 0) {
/* 812 */         BulkTimer timer = new BulkTimer();
/* 813 */         timer.start();
/* 814 */         for (int i = 0; i < args.length; ++i) {
/* 815 */           if (args[i].equals("-src")) {
/* 816 */             srcPath = args[(++i)];
/* 817 */           } else if (args[i].equals("-dest")) {
/* 818 */             destPath = args[(++i)];
/* 819 */           } else if (args[i].equals("-generate_binary")) {
/* 820 */             String name = "clunits.txt";
/* 821 */             if (i + 1 < args.length) {
/* 822 */               String nameArg = args[(++i)];
/* 823 */               if (!nameArg.startsWith("-")) {
/* 824 */                 name = nameArg;
/*     */               }
/*     */             }
/*     */ 
/* 828 */             int suffixPos = name.lastIndexOf(".txt");
/*     */ 
/* 830 */             String binaryName = "clunits.bin";
/* 831 */             if (suffixPos != -1) {
/* 832 */               binaryName = name.substring(0, suffixPos) + ".bin";
/*     */             }
/*     */ 
/* 835 */             System.out.println("Loading " + name);
/* 836 */             timer.start("load_text");
/* 837 */             ClusterUnitDatabase udb = new ClusterUnitDatabase(new URL("file:" + srcPath + "/" + name), false);
/*     */ 
/* 841 */             timer.stop("load_text");
/*     */ 
/* 843 */             System.out.println("Dumping " + binaryName);
/* 844 */             timer.start("dump_binary");
/* 845 */             udb.dumpBinary(destPath + "/" + binaryName);
/* 846 */             timer.stop("dump_binary");
/*     */           }
/* 848 */           else if (args[i].equals("-compare"))
/*     */           {
/* 850 */             timer.start("load_text");
/* 851 */             ClusterUnitDatabase udb = new ClusterUnitDatabase(new URL("file:./cmu_time_awb.txt"), false);
/*     */ 
/* 854 */             timer.stop("load_text");
/*     */ 
/* 856 */             timer.start("load_binary");
/* 857 */             ClusterUnitDatabase budb = new ClusterUnitDatabase(new URL("file:./cmu_time_awb.bin"), true);
/*     */ 
/* 860 */             timer.stop("load_binary");
/*     */ 
/* 862 */             timer.start("compare");
/* 863 */             if (udb.compare(budb))
/* 864 */               System.out.println("other compare ok");
/*     */             else {
/* 866 */               System.out.println("other compare different");
/*     */             }
/* 868 */             timer.stop("compare");
/* 869 */           } else if (args[i].equals("-showtimes")) {
/* 870 */             showTimes = true;
/*     */           } else {
/* 872 */             System.out.println("Unknown option " + args[i]);
/*     */           }
/*     */         }
/* 875 */         timer.stop();
/* 876 */         if (showTimes)
/* 877 */           timer.show("ClusterUnitDatabase");
/*     */       }
/*     */       else {
/* 880 */         System.out.println("Options: ");
/* 881 */         System.out.println("    -src path");
/* 882 */         System.out.println("    -dest path");
/* 883 */         System.out.println("    -compare");
/* 884 */         System.out.println("    -generate_binary");
/* 885 */         System.out.println("    -showTimes");
/*     */       }
/*     */     } catch (IOException ioe) {
/* 888 */       System.err.println(ioe);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void error(String s)
/*     */   {
/* 998 */     System.out.println("ClusterUnitDatabase Error: " + s);
/*     */   }
/*     */ 
/*     */   class UnitOriginInfo
/*     */   {
/*     */     String originFile;
/*     */     float originStart;
/*     */     float originEnd;
/*     */ 
/*     */     UnitOriginInfo()
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   class DatabaseClusterUnit
/*     */   {
/*     */     int type;
/*     */     int phone;
/*     */     int start;
/*     */     int end;
/*     */     int prev;
/*     */     int next;
/*     */ 
/*     */     DatabaseClusterUnit(int type, int phone, int start, int end, int prev, int next)
/*     */     {
/* 917 */       this.type = type;
/* 918 */       this.phone = phone;
/* 919 */       this.start = start;
/* 920 */       this.end = end;
/* 921 */       this.prev = prev;
/* 922 */       this.next = next;
/*     */     }
/*     */ 
/*     */     DatabaseClusterUnit(ByteBuffer bb)
/*     */       throws IOException
/*     */     {
/* 933 */       this.type = bb.getInt();
/* 934 */       this.phone = bb.getInt();
/* 935 */       this.start = bb.getInt();
/* 936 */       this.end = bb.getInt();
/* 937 */       this.prev = bb.getInt();
/* 938 */       this.next = bb.getInt();
/*     */     }
/*     */ 
/*     */     DatabaseClusterUnit(DataInputStream is)
/*     */       throws IOException
/*     */     {
/* 949 */       this.type = is.readInt();
/* 950 */       this.phone = is.readInt();
/* 951 */       this.start = is.readInt();
/* 952 */       this.end = is.readInt();
/* 953 */       this.prev = is.readInt();
/* 954 */       this.next = is.readInt();
/*     */     }
/*     */ 
/*     */     String getName()
/*     */     {
/* 963 */       return ClusterUnitDatabase.this.unitTypes[this.type].getName();
/*     */     }
/*     */ 
/*     */     void dumpBinary(DataOutputStream os)
/*     */       throws IOException
/*     */     {
/* 974 */       os.writeInt(this.type);
/* 975 */       os.writeInt(this.phone);
/* 976 */       os.writeInt(this.start);
/* 977 */       os.writeInt(this.end);
/* 978 */       os.writeInt(this.prev);
/* 979 */       os.writeInt(this.next);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.clunits.ClusterUnitDatabase
 * JD-Core Version:    0.5.4
 */