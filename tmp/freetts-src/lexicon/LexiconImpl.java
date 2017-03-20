/*     */ package com.sun.speech.freetts.lexicon;
/*     */ 
/*     */ import com.sun.speech.freetts.util.BulkTimer;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.BufferedInputStream;
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
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.MappedByteBuffer;
/*     */ import java.nio.channels.FileChannel;
/*     */ import java.nio.channels.FileChannel.MapMode;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public abstract class LexiconImpl
/*     */   implements Lexicon
/*     */ {
/*  93 */   protected boolean tokenizeOnLoad = false;
/*     */ 
/* 100 */   protected boolean tokenizeOnLookup = false;
/*     */   private static final int MAGIC = 12237598;
/*     */   private static final int VERSION = 1;
/*     */   private URL compiledURL;
/*     */   private URL addendaURL;
/*     */   private URL letterToSoundURL;
/*     */   private Map addenda;
/*     */   private Map compiled;
/* 140 */   private LetterToSound letterToSound = null;
/*     */ 
/* 145 */   private ArrayList partsOfSpeech = new ArrayList();
/*     */   private static Map loadedCompiledLexicons;
/* 162 */   private boolean loaded = false;
/*     */ 
/* 167 */   private boolean binary = false;
/*     */ 
/* 172 */   private static final String[] NO_PHONES = new String[0];
/*     */ 
/* 177 */   private char[] charBuffer = new char[''];
/*     */ 
/* 182 */   private boolean useNewIO = Utilities.getProperty("com.sun.speech.freetts.useNewIO", "true").equals("true");
/*     */ 
/*     */   public LexiconImpl(URL compiledURL, URL addendaURL, URL letterToSoundURL, boolean binary)
/*     */   {
/* 200 */     setLexiconParameters(compiledURL, addendaURL, letterToSoundURL, binary);
/*     */   }
/*     */ 
/*     */   public LexiconImpl()
/*     */   {
/* 209 */     String tokenize = Utilities.getProperty("com.sun.speech.freetts.lexicon.LexTokenize", "never");
/*     */ 
/* 212 */     this.tokenizeOnLoad = tokenize.equals("load");
/* 213 */     this.tokenizeOnLookup = tokenize.equals("lookup");
/*     */   }
/*     */ 
/*     */   protected void setLexiconParameters(URL compiledURL, URL addendaURL, URL letterToSoundURL, boolean binary)
/*     */   {
/* 228 */     this.compiledURL = compiledURL;
/* 229 */     this.addendaURL = addendaURL;
/* 230 */     this.letterToSoundURL = letterToSoundURL;
/* 231 */     this.binary = binary;
/*     */   }
/*     */ 
/*     */   public boolean isLoaded()
/*     */   {
/* 240 */     return this.loaded;
/*     */   }
/*     */ 
/*     */   public void load()
/*     */     throws IOException
/*     */   {
/* 249 */     BulkTimer.LOAD.start("Lexicon");
/*     */ 
/* 251 */     if (this.compiledURL == null) {
/* 252 */       throw new IOException("Can't load lexicon");
/*     */     }
/*     */ 
/* 255 */     if (this.addendaURL == null) {
/* 256 */       throw new IOException("Can't load lexicon addenda ");
/*     */     }
/*     */ 
/* 259 */     if (loadedCompiledLexicons == null) {
/* 260 */       loadedCompiledLexicons = new HashMap();
/*     */     }
/* 262 */     if (!loadedCompiledLexicons.containsKey(this.compiledURL)) {
/* 263 */       InputStream compiledIS = Utilities.getInputStream(this.compiledURL);
/* 264 */       if (compiledIS == null) {
/* 265 */         throw new IOException("Can't load lexicon from " + this.compiledURL);
/*     */       }
/* 267 */       Map newCompiled = createLexicon(compiledIS, this.binary, 65000);
/* 268 */       loadedCompiledLexicons.put(this.compiledURL, newCompiled);
/* 269 */       compiledIS.close();
/*     */     }
/* 271 */     this.compiled = Collections.unmodifiableMap((Map)loadedCompiledLexicons.get(this.compiledURL));
/*     */ 
/* 273 */     InputStream addendaIS = Utilities.getInputStream(this.addendaURL);
/* 274 */     if (addendaIS == null) {
/* 275 */       throw new IOException("Can't load lexicon addenda from " + this.addendaURL);
/*     */     }
/*     */ 
/* 281 */     this.addenda = createLexicon(addendaIS, this.binary, 50);
/* 282 */     addendaIS.close();
/*     */ 
/* 287 */     String userAddenda = Utilities.getProperty("com.sun.speech.freetts.lexicon.userAddenda", null);
/*     */ 
/* 289 */     if (userAddenda != null) {
/*     */       try {
/* 291 */         URL userAddendaURL = new URL(userAddenda);
/* 292 */         InputStream userAddendaIS = Utilities.getInputStream(userAddendaURL);
/*     */ 
/* 294 */         if (userAddendaIS == null) {
/* 295 */           throw new IOException("Can't load user addenda from " + userAddenda);
/*     */         }
/*     */ 
/* 298 */         Map tmpAddenda = createLexicon(userAddendaIS, false, 50);
/* 299 */         userAddendaIS.close();
/* 300 */         Iterator keys = tmpAddenda.keySet().iterator();
/* 301 */         while (keys.hasNext()) {
/* 302 */           Object key = keys.next();
/* 303 */           this.addenda.put(key, tmpAddenda.get(key));
/*     */         }
/*     */       } catch (MalformedURLException e) {
/* 306 */         throw new IOException("User addenda URL is malformed: " + userAddenda);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 311 */     this.loaded = true;
/* 312 */     BulkTimer.LOAD.stop("Lexicon");
/* 313 */     this.letterToSound = new LetterToSoundImpl(this.letterToSoundURL, this.binary);
/*     */   }
/*     */ 
/*     */   protected Map createLexicon(InputStream is, boolean binary, int estimatedSize)
/*     */     throws IOException
/*     */   {
/* 330 */     if (binary) {
/* 331 */       if ((this.useNewIO) && (is instanceof FileInputStream)) {
/* 332 */         FileInputStream fis = (FileInputStream)is;
/* 333 */         return loadMappedBinaryLexicon(fis, estimatedSize);
/*     */       }
/* 335 */       DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
/*     */ 
/* 337 */       return loadBinaryLexicon(dis, estimatedSize);
/*     */     }
/*     */ 
/* 340 */     return loadTextLexicon(is, estimatedSize);
/*     */   }
/*     */ 
/*     */   protected Map loadTextLexicon(InputStream is, int estimatedSize)
/*     */     throws IOException
/*     */   {
/* 355 */     Map lexicon = new LinkedHashMap(estimatedSize * 4 / 3);
/* 356 */     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
/*     */ 
/* 359 */     String line = reader.readLine();
/* 360 */     while (line != null) {
/* 361 */       if (!line.startsWith("***")) {
/* 362 */         parseAndAdd(lexicon, line);
/*     */       }
/* 364 */       line = reader.readLine();
/*     */     }
/* 366 */     return lexicon;
/*     */   }
/*     */ 
/*     */   protected void parseAndAdd(Map lexicon, String line)
/*     */   {
/* 376 */     StringTokenizer tokenizer = new StringTokenizer(line, "\t");
/* 377 */     String phones = null;
/*     */ 
/* 379 */     String wordAndPos = tokenizer.nextToken();
/* 380 */     String pos = wordAndPos.substring(wordAndPos.length() - 1);
/* 381 */     if (!this.partsOfSpeech.contains(pos)) {
/* 382 */       this.partsOfSpeech.add(pos);
/*     */     }
/* 384 */     if (tokenizer.hasMoreTokens()) {
/* 385 */       phones = tokenizer.nextToken();
/*     */     }
/* 387 */     if ((phones != null) && (this.tokenizeOnLoad))
/* 388 */       lexicon.put(wordAndPos, getPhones(phones));
/* 389 */     else if (phones == null)
/* 390 */       lexicon.put(wordAndPos, NO_PHONES);
/*     */     else
/* 392 */       lexicon.put(wordAndPos, phones);
/*     */   }
/*     */ 
/*     */   public String[] getPhones(String word, String partOfSpeech)
/*     */   {
/* 408 */     return getPhones(word, partOfSpeech, true);
/*     */   }
/*     */ 
/*     */   public String[] getPhones(String word, String partOfSpeech, boolean useLTS)
/*     */   {
/* 426 */     String[] phones = null;
/* 427 */     phones = getPhones(this.addenda, word, partOfSpeech);
/* 428 */     if (phones == null) {
/* 429 */       phones = getPhones(this.compiled, word, partOfSpeech);
/*     */     }
/* 431 */     if ((useLTS) && 
/* 432 */       (phones == null) && (this.letterToSound != null)) {
/* 433 */       phones = this.letterToSound.getPhones(word, partOfSpeech);
/*     */     }
/*     */ 
/* 436 */     if (phones != null) {
/* 437 */       String[] copy = new String[phones.length];
/* 438 */       System.arraycopy(phones, 0, copy, 0, phones.length);
/* 439 */       return copy;
/*     */     }
/* 441 */     return null;
/*     */   }
/*     */ 
/*     */   protected String[] getPhones(Map lexicon, String word, String partOfSpeech)
/*     */   {
/* 460 */     partOfSpeech = fixPartOfSpeech(partOfSpeech);
/* 461 */     String[] phones = getPhones(lexicon, word + partOfSpeech);
/* 462 */     int i = 0;
/* 463 */     while ((i < this.partsOfSpeech.size()) && (phones == null))
/*     */     {
/* 465 */       if (!partOfSpeech.equals((String)this.partsOfSpeech.get(i)))
/* 466 */         phones = getPhones(lexicon, word + (String)this.partsOfSpeech.get(i));
/* 464 */       ++i;
/*     */     }
/*     */ 
/* 470 */     return phones;
/*     */   }
/*     */ 
/*     */   protected String[] getPhones(Map lexicon, String wordAndPartOfSpeech)
/*     */   {
/* 485 */     Object value = lexicon.get(wordAndPartOfSpeech);
/* 486 */     if (value instanceof String[])
/* 487 */       return (String[])value;
/* 488 */     if (value instanceof String)
/*     */     {
/* 490 */       String[] phoneArray = getPhones((String)value);
/* 491 */       if (this.tokenizeOnLookup) {
/* 492 */         lexicon.put(wordAndPartOfSpeech, phoneArray);
/*     */       }
/* 494 */       return phoneArray;
/*     */     }
/* 496 */     return null;
/*     */   }
/*     */ 
/*     */   protected String[] getPhones(String phones)
/*     */   {
/* 509 */     ArrayList phoneList = new ArrayList();
/* 510 */     StringTokenizer tokenizer = new StringTokenizer(phones, " ");
/* 511 */     while (tokenizer.hasMoreTokens()) {
/* 512 */       phoneList.add(tokenizer.nextToken());
/*     */     }
/* 514 */     return (String[])phoneList.toArray(new String[0]);
/*     */   }
/*     */ 
/*     */   public void addAddendum(String word, String partOfSpeech, String[] phones)
/*     */   {
/* 528 */     String pos = fixPartOfSpeech(partOfSpeech);
/* 529 */     if (!this.partsOfSpeech.contains(pos)) {
/* 530 */       this.partsOfSpeech.add(pos);
/*     */     }
/* 532 */     this.addenda.put(word + pos, phones);
/*     */   }
/*     */ 
/*     */   public void removeAddendum(String word, String partOfSpeech)
/*     */   {
/* 542 */     this.addenda.remove(word + fixPartOfSpeech(partOfSpeech));
/*     */   }
/*     */ 
/*     */   private void outString(DataOutputStream dos, String s)
/*     */     throws IOException
/*     */   {
/* 555 */     dos.writeByte((byte)s.length());
/* 556 */     for (int i = 0; i < s.length(); ++i)
/* 557 */       dos.writeChar(s.charAt(i));
/*     */   }
/*     */ 
/*     */   private String getString(DataInputStream dis)
/*     */     throws IOException
/*     */   {
/* 571 */     int size = dis.readByte();
/* 572 */     for (int i = 0; i < size; ++i) {
/* 573 */       this.charBuffer[i] = dis.readChar();
/*     */     }
/* 575 */     return new String(this.charBuffer, 0, size);
/*     */   }
/*     */ 
/*     */   private String getString(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 588 */     int size = bb.get();
/* 589 */     for (int i = 0; i < size; ++i) {
/* 590 */       this.charBuffer[i] = bb.getChar();
/*     */     }
/* 592 */     return new String(this.charBuffer, 0, size);
/*     */   }
/*     */ 
/*     */   private void dumpBinaryLexicon(Map lexicon, String path)
/*     */   {
/*     */     try
/*     */     {
/* 625 */       FileOutputStream fos = new FileOutputStream(path);
/* 626 */       DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
/*     */ 
/* 628 */       List phonemeList = findPhonemes(lexicon);
/*     */ 
/* 630 */       dos.writeInt(12237598);
/* 631 */       dos.writeInt(1);
/* 632 */       dos.writeInt(phonemeList.size());
/*     */ 
/* 634 */       for (int i = 0; i < phonemeList.size(); ++i) {
/* 635 */         outString(dos, (String)phonemeList.get(i));
/*     */       }
/*     */ 
/* 638 */       dos.writeInt(lexicon.keySet().size());
/* 639 */       for (Iterator i = lexicon.keySet().iterator(); i.hasNext(); ) {
/* 640 */         String key = (String)i.next();
/* 641 */         outString(dos, key);
/* 642 */         String[] phonemes = getPhones(lexicon, key);
/* 643 */         dos.writeByte((byte)phonemes.length);
/* 644 */         for (int index = 0; index < phonemes.length; ++index) {
/* 645 */           int phonemeIndex = phonemeList.indexOf(phonemes[index]);
/* 646 */           if (phonemeIndex == -1) {
/* 647 */             throw new Error("Can't find phoneme index");
/*     */           }
/* 649 */           dos.writeByte((byte)phonemeIndex);
/*     */         }
/*     */       }
/* 652 */       dos.close();
/*     */     } catch (FileNotFoundException fe) {
/* 654 */       throw new Error("Can't dump binary database " + fe.getMessage());
/*     */     }
/*     */     catch (IOException ioe) {
/* 657 */       throw new Error("Can't write binary database " + ioe.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   private Map loadMappedBinaryLexicon(FileInputStream is, int estimatedSize)
/*     */     throws IOException
/*     */   {
/* 675 */     FileChannel fc = is.getChannel();
/*     */ 
/* 677 */     MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0L, (int)fc.size());
/*     */ 
/* 679 */     bb.load();
/* 680 */     int size = 0;
/* 681 */     int numEntries = 0;
/* 682 */     List phonemeList = new ArrayList();
/*     */ 
/* 688 */     Map lexicon = new LinkedHashMap(estimatedSize * 4 / 3);
/*     */ 
/* 690 */     if (bb.getInt() != 12237598) {
/* 691 */       throw new Error("bad magic number in lexicon");
/*     */     }
/*     */ 
/* 694 */     if (bb.getInt() != 1) {
/* 695 */       throw new Error("bad version number in lexicon");
/*     */     }
/*     */ 
/* 698 */     size = bb.getInt();
/* 699 */     for (int i = 0; i < size; ++i) {
/* 700 */       String phoneme = getString(bb);
/* 701 */       phonemeList.add(phoneme);
/*     */     }
/* 703 */     numEntries = bb.getInt();
/*     */ 
/* 705 */     for (int i = 0; i < numEntries; ++i) {
/* 706 */       String wordAndPos = getString(bb);
/* 707 */       String pos = Character.toString(wordAndPos.charAt(wordAndPos.length() - 1));
/*     */ 
/* 709 */       if (!this.partsOfSpeech.contains(pos)) {
/* 710 */         this.partsOfSpeech.add(pos);
/*     */       }
/*     */ 
/* 713 */       int numPhonemes = bb.get();
/* 714 */       String[] phonemes = new String[numPhonemes];
/*     */ 
/* 716 */       for (int j = 0; j < numPhonemes; ++j) {
/* 717 */         phonemes[j] = ((String)phonemeList.get(bb.get()));
/*     */       }
/* 719 */       lexicon.put(wordAndPos, phonemes);
/*     */     }
/* 721 */     fc.close();
/* 722 */     return lexicon;
/*     */   }
/*     */ 
/*     */   private Map loadBinaryLexicon(InputStream is, int estimatedSize)
/*     */     throws IOException
/*     */   {
/* 738 */     DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
/*     */ 
/* 740 */     int size = 0;
/* 741 */     int numEntries = 0;
/* 742 */     List phonemeList = new ArrayList();
/*     */ 
/* 747 */     Map lexicon = new LinkedHashMap();
/*     */ 
/* 749 */     if (dis.readInt() != 12237598) {
/* 750 */       throw new Error("bad magic number in lexicon");
/*     */     }
/*     */ 
/* 753 */     if (dis.readInt() != 1) {
/* 754 */       throw new Error("bad version number in lexicon");
/*     */     }
/*     */ 
/* 757 */     size = dis.readInt();
/* 758 */     for (int i = 0; i < size; ++i) {
/* 759 */       String phoneme = getString(dis);
/* 760 */       phonemeList.add(phoneme);
/*     */     }
/* 762 */     numEntries = dis.readInt();
/*     */ 
/* 764 */     for (int i = 0; i < numEntries; ++i) {
/* 765 */       String wordAndPos = getString(dis);
/* 766 */       String pos = Character.toString(wordAndPos.charAt(wordAndPos.length() - 1));
/*     */ 
/* 768 */       if (!this.partsOfSpeech.contains(pos)) {
/* 769 */         this.partsOfSpeech.add(pos);
/*     */       }
/*     */ 
/* 772 */       int numPhonemes = dis.readByte();
/* 773 */       String[] phonemes = new String[numPhonemes];
/*     */ 
/* 775 */       for (int j = 0; j < numPhonemes; ++j) {
/* 776 */         phonemes[j] = ((String)phonemeList.get(dis.readByte()));
/*     */       }
/* 778 */       lexicon.put(wordAndPos, phonemes);
/*     */     }
/* 780 */     dis.close();
/* 781 */     return lexicon;
/*     */   }
/*     */ 
/*     */   public void dumpBinary(String path)
/*     */   {
/* 792 */     String compiledPath = path + "_compiled.bin";
/* 793 */     String addendaPath = path + "_addenda.bin";
/*     */ 
/* 795 */     dumpBinaryLexicon(this.compiled, compiledPath);
/* 796 */     dumpBinaryLexicon(this.addenda, addendaPath);
/*     */   }
/*     */ 
/*     */   private List findPhonemes(Map lexicon)
/*     */   {
/* 807 */     List phonemeList = new ArrayList();
/* 808 */     for (Iterator i = lexicon.keySet().iterator(); i.hasNext(); ) {
/* 809 */       String key = (String)i.next();
/* 810 */       String[] phonemes = getPhones(lexicon, key);
/* 811 */       for (int index = 0; index < phonemes.length; ++index) {
/* 812 */         if (!phonemeList.contains(phonemes[index])) {
/* 813 */           phonemeList.add(phonemes[index]);
/*     */         }
/*     */       }
/*     */     }
/* 817 */     return phonemeList;
/*     */   }
/*     */ 
/*     */   public boolean compare(LexiconImpl other)
/*     */   {
/* 830 */     return (compare(this.addenda, other.addenda)) && (compare(this.compiled, other.compiled));
/*     */   }
/*     */ 
/*     */   private boolean compare(Map lex, Map other)
/*     */   {
/* 843 */     for (Iterator i = lex.keySet().iterator(); i.hasNext(); ) {
/* 844 */       String key = (String)i.next();
/* 845 */       String[] thisPhonemes = getPhones(lex, key);
/* 846 */       String[] otherPhonemes = getPhones(other, key);
/* 847 */       if (thisPhonemes == null) {
/* 848 */         System.out.println(key + " not found in this.");
/* 849 */         return false;
/* 850 */       }if (otherPhonemes == null) {
/* 851 */         System.out.println(key + " not found in other.");
/* 852 */         return false;
/* 853 */       }if (thisPhonemes.length == otherPhonemes.length) {
/* 854 */         for (int j = 0; j < thisPhonemes.length; ++j) {
/* 855 */           if (!thisPhonemes[j].equals(otherPhonemes[j]))
/* 856 */             return false;
/*     */         }
/*     */       }
/*     */       else {
/* 860 */         return false;
/*     */       }
/*     */     }
/* 863 */     return true;
/*     */   }
/*     */ 
/*     */   protected static String fixPartOfSpeech(String partOfSpeech)
/*     */   {
/* 872 */     return (partOfSpeech == null) ? "0" : partOfSpeech;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.lexicon.LexiconImpl
 * JD-Core Version:    0.5.4
 */