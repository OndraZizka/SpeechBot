/*     */ package com.sun.speech.freetts.lexicon;
/*     */ 
/*     */ import com.sun.speech.freetts.util.BulkTimer;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.net.URL;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public class LetterToSoundImpl
/*     */   implements LetterToSound
/*     */ {
/*     */   static final String TOTAL = "TOTAL";
/*     */   static final String INDEX = "INDEX";
/*     */   static final String STATE = "STATE";
/*     */   static final String PHONE = "PHONE";
/* 150 */   protected boolean tokenizeOnLoad = false;
/*     */ 
/* 157 */   protected boolean tokenizeOnLookup = false;
/*     */   private static final int MAGIC = -559038737;
/*     */   private static final int VERSION = 1;
/* 175 */   private Object[] stateMachine = null;
/*     */ 
/* 180 */   private int numStates = 0;
/*     */   private static final int WINDOW_SIZE = 4;
/* 194 */   private char[] fval_buff = new char[8];
/*     */   protected HashMap letterIndex;
/*     */   private static List phonemeTable;
/*     */ 
/*     */   public LetterToSoundImpl(URL ltsRules, boolean binary)
/*     */     throws IOException
/*     */   {
/* 218 */     BulkTimer.LOAD.start("LTS");
/* 219 */     InputStream is = ltsRules.openStream();
/* 220 */     if (binary)
/* 221 */       loadBinary(is);
/*     */     else {
/* 223 */       loadText(is);
/*     */     }
/* 225 */     is.close();
/* 226 */     BulkTimer.LOAD.stop("LTS");
/*     */   }
/*     */ 
/*     */   private void loadText(InputStream is)
/*     */     throws IOException
/*     */   {
/* 243 */     String tokenize = Utilities.getProperty("com.sun.speech.freetts.lexicon.LTSTokenize", "load");
/*     */ 
/* 246 */     this.tokenizeOnLoad = tokenize.equals("load");
/* 247 */     this.tokenizeOnLookup = tokenize.equals("lookup");
/*     */ 
/* 249 */     this.letterIndex = new HashMap();
/*     */ 
/* 251 */     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
/* 252 */     String line = reader.readLine();
/* 253 */     while (line != null) {
/* 254 */       if (!line.startsWith("***")) {
/* 255 */         parseAndAdd(line);
/*     */       }
/* 257 */       line = reader.readLine();
/*     */     }
/*     */   }
/*     */ 
/*     */   private void loadBinary(InputStream is)
/*     */     throws IOException
/*     */   {
/* 270 */     DataInputStream dis = new DataInputStream(is);
/*     */ 
/* 272 */     if (dis.readInt() != -559038737) {
/* 273 */       throw new Error("Bad LTS binary file format");
/*     */     }
/*     */ 
/* 276 */     if (dis.readInt() != 1) {
/* 277 */       throw new Error("Bad LTS binary file version");
/*     */     }
/*     */ 
/* 282 */     int phonemeTableSize = dis.readInt();
/* 283 */     phonemeTable = new ArrayList(phonemeTableSize);
/*     */ 
/* 285 */     for (int i = 0; i < phonemeTableSize; ++i) {
/* 286 */       String phoneme = dis.readUTF();
/* 287 */       phonemeTable.add(phoneme);
/*     */     }
/*     */ 
/* 292 */     int letterIndexSize = dis.readInt();
/* 293 */     this.letterIndex = new HashMap();
/* 294 */     for (int i = 0; i < letterIndexSize; ++i) {
/* 295 */       char c = dis.readChar();
/* 296 */       int index = dis.readInt();
/* 297 */       this.letterIndex.put(Character.toString(c), new Integer(index));
/*     */     }
/*     */ 
/* 302 */     int stateMachineSize = dis.readInt();
/* 303 */     this.stateMachine = new Object[stateMachineSize];
/* 304 */     for (int i = 0; i < stateMachineSize; ++i) {
/* 305 */       int type = dis.readInt();
/*     */ 
/* 307 */       if (type == 2)
/* 308 */         this.stateMachine[i] = FinalState.loadBinary(dis);
/* 309 */       else if (type == 1)
/* 310 */         this.stateMachine[i] = DecisionState.loadBinary(dis);
/*     */       else
/* 312 */         throw new Error("Unknown state type in LTS load");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void parseAndAdd(String line)
/*     */   {
/* 326 */     StringTokenizer tokenizer = new StringTokenizer(line, " ");
/* 327 */     String type = tokenizer.nextToken();
/*     */ 
/* 329 */     if ((type.equals("STATE")) || (type.equals("PHONE"))) {
/* 330 */       if (this.tokenizeOnLoad)
/* 331 */         this.stateMachine[this.numStates] = getState(type, tokenizer);
/*     */       else {
/* 333 */         this.stateMachine[this.numStates] = line;
/*     */       }
/* 335 */       this.numStates += 1;
/* 336 */     } else if (type.equals("INDEX")) {
/* 337 */       Integer index = new Integer(tokenizer.nextToken());
/* 338 */       if (index.intValue() != this.numStates) {
/* 339 */         throw new Error("Bad INDEX in file.");
/*     */       }
/* 341 */       String c = tokenizer.nextToken();
/* 342 */       this.letterIndex.put(c, index);
/*     */     }
/* 344 */     else if (type.equals("TOTAL")) {
/* 345 */       this.stateMachine = new Object[Integer.parseInt(tokenizer.nextToken())];
/*     */     }
/*     */   }
/*     */ 
/*     */   public void dumpBinary(String path)
/*     */     throws IOException
/*     */   {
/* 366 */     FileOutputStream fos = new FileOutputStream(path);
/* 367 */     DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
/*     */ 
/* 370 */     dos.writeInt(-559038737);
/* 371 */     dos.writeInt(1);
/*     */ 
/* 375 */     phonemeTable = findPhonemes();
/* 376 */     dos.writeInt(phonemeTable.size());
/* 377 */     for (Iterator i = phonemeTable.iterator(); i.hasNext(); ) {
/* 378 */       String phoneme = (String)i.next();
/* 379 */       dos.writeUTF(phoneme);
/*     */     }
/*     */ 
/* 384 */     dos.writeInt(this.letterIndex.size());
/* 385 */     for (Iterator i = this.letterIndex.keySet().iterator(); i.hasNext(); ) {
/* 386 */       String letter = (String)i.next();
/* 387 */       int index = ((Integer)this.letterIndex.get(letter)).intValue();
/* 388 */       dos.writeChar(letter.charAt(0));
/* 389 */       dos.writeInt(index);
/*     */     }
/*     */ 
/* 394 */     dos.writeInt(this.stateMachine.length);
/*     */ 
/* 396 */     for (int i = 0; i < this.stateMachine.length; ++i) {
/* 397 */       getState(i).writeBinary(dos);
/*     */     }
/* 399 */     dos.close();
/*     */   }
/*     */ 
/*     */   private List findPhonemes()
/*     */   {
/* 408 */     Set set = new HashSet();
/* 409 */     for (int i = 0; i < this.stateMachine.length; ++i) {
/* 410 */       if (this.stateMachine[i] instanceof FinalState) {
/* 411 */         FinalState fstate = (FinalState)this.stateMachine[i];
/* 412 */         if (fstate.phoneList != null) {
/* 413 */           for (int j = 0; j < fstate.phoneList.length; ++j) {
/* 414 */             set.add(fstate.phoneList[j]);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 419 */     return new ArrayList(set);
/*     */   }
/*     */ 
/*     */   protected State getState(int i)
/*     */   {
/* 433 */     State state = null;
/* 434 */     if (this.stateMachine[i] instanceof String) {
/* 435 */       state = getState((String)this.stateMachine[i]);
/* 436 */       if (this.tokenizeOnLookup)
/* 437 */         this.stateMachine[i] = state;
/*     */     }
/*     */     else {
/* 440 */       state = (State)this.stateMachine[i];
/*     */     }
/* 442 */     return state;
/*     */   }
/*     */ 
/*     */   protected State getState(String s)
/*     */   {
/* 453 */     StringTokenizer tokenizer = new StringTokenizer(s, " ");
/* 454 */     return getState(tokenizer.nextToken(), tokenizer);
/*     */   }
/*     */ 
/*     */   protected State getState(String type, StringTokenizer tokenizer)
/*     */   {
/* 468 */     if (type.equals("STATE")) {
/* 469 */       int index = Integer.parseInt(tokenizer.nextToken());
/* 470 */       String c = tokenizer.nextToken();
/* 471 */       int qtrue = Integer.parseInt(tokenizer.nextToken());
/* 472 */       int qfalse = Integer.parseInt(tokenizer.nextToken());
/* 473 */       return new DecisionState(index, c.charAt(0), qtrue, qfalse);
/* 474 */     }if (type.equals("PHONE")) {
/* 475 */       return new FinalState(tokenizer.nextToken());
/*     */     }
/* 477 */     return null;
/*     */   }
/*     */ 
/*     */   protected char[] getFullBuff(String word)
/*     */   {
/* 488 */     char[] full_buff = new char[word.length() + 8];
/*     */ 
/* 492 */     for (int i = 0; i < 3; ++i)
/*     */     {
/* 494 */       full_buff[i] = '0';
/*     */     }
/* 496 */     full_buff[3] = '#';
/* 497 */     word.getChars(0, word.length(), full_buff, 4);
/* 498 */     for (int i = 0; i < 3; ++i)
/*     */     {
/* 500 */       full_buff[(full_buff.length - i - 1)] = '0';
/*     */     }
/* 502 */     full_buff[(full_buff.length - 4)] = '#';
/* 503 */     return full_buff;
/*     */   }
/*     */ 
/*     */   public String[] getPhones(String word, String partOfSpeech)
/*     */   {
/* 517 */     ArrayList phoneList = new ArrayList();
/*     */ 
/* 525 */     char[] full_buff = getFullBuff(word);
/*     */ 
/* 534 */     for (int pos = 0; pos < word.length(); ++pos) {
/* 535 */       for (int i = 0; i < 4; ++i) {
/* 536 */         this.fval_buff[i] = full_buff[(pos + i)];
/* 537 */         this.fval_buff[(i + 4)] = full_buff[(i + pos + 1 + 4)];
/*     */       }
/*     */ 
/* 540 */       char c = word.charAt(pos);
/* 541 */       Integer startIndex = (Integer)this.letterIndex.get(Character.toString(c));
/* 542 */       if (startIndex == null) {
/*     */         continue;
/*     */       }
/* 545 */       int stateIndex = startIndex.intValue();
/* 546 */       State currentState = getState(stateIndex);
/* 547 */       while (!currentState instanceof FinalState) {
/* 548 */         stateIndex = ((DecisionState)currentState).getNextState(this.fval_buff);
/*     */ 
/* 551 */         currentState = getState(stateIndex);
/*     */       }
/* 553 */       ((FinalState)currentState).append(phoneList);
/*     */     }
/* 555 */     return (String[])phoneList.toArray(new String[0]);
/*     */   }
/*     */ 
/*     */   public boolean compare(LetterToSoundImpl other)
/*     */   {
/* 569 */     for (Iterator i = this.letterIndex.keySet().iterator(); i.hasNext(); ) {
/* 570 */       String key = (String)i.next();
/* 571 */       Integer thisIndex = (Integer)this.letterIndex.get(key);
/* 572 */       Integer otherIndex = (Integer)other.letterIndex.get(key);
/* 573 */       if (!thisIndex.equals(otherIndex)) {
/* 574 */         System.out.println("Bad Index for " + key);
/* 575 */         return false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 581 */     for (int i = 0; i < this.stateMachine.length; ++i) {
/* 582 */       State state = getState(i);
/* 583 */       State otherState = other.getState(i);
/* 584 */       if (!state.compare(otherState)) {
/* 585 */         System.out.println("Bad state " + i);
/* 586 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 590 */     return true;
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 863 */     boolean showTimes = false;
/* 864 */     String srcPath = ".";
/* 865 */     String destPath = ".";
/* 866 */     String name = "cmulex_lts";
/*     */     try
/*     */     {
/* 869 */       if (args.length > 0) {
/* 870 */         BulkTimer timer = new BulkTimer();
/* 871 */         timer.start();
/* 872 */         for (int i = 0; i < args.length; ++i) {
/* 873 */           if (args[i].equals("-src")) {
/* 874 */             srcPath = args[(++i)];
/* 875 */           } else if (args[i].equals("-dest")) {
/* 876 */             destPath = args[(++i)];
/* 877 */           } else if ((args[i].equals("-name")) && (i < args.length - 1))
/*     */           {
/* 879 */             name = args[(++i)];
/* 880 */           } else if (args[i].equals("-generate_binary"))
/*     */           {
/* 882 */             System.out.println("Loading " + name);
/* 883 */             timer.start("load_text");
/* 884 */             LetterToSoundImpl text = new LetterToSoundImpl(new URL("file:" + srcPath + "/" + name + ".txt"), false);
/*     */ 
/* 888 */             timer.stop("load_text");
/*     */ 
/* 890 */             System.out.println("Dumping " + name);
/* 891 */             timer.start("dump_binary");
/* 892 */             text.dumpBinary(destPath + "/" + name + ".bin");
/* 893 */             timer.stop("dump_binary");
/*     */           }
/* 895 */           else if (args[i].equals("-compare"))
/*     */           {
/* 897 */             timer.start("load_text");
/* 898 */             LetterToSoundImpl text = new LetterToSoundImpl(new URL("file:./" + name + ".txt"), false);
/*     */ 
/* 900 */             timer.stop("load_text");
/*     */ 
/* 902 */             timer.start("load_binary");
/* 903 */             LetterToSoundImpl binary = new LetterToSoundImpl(new URL("file:./" + name + ".bin"), true);
/*     */ 
/* 905 */             timer.stop("load_binary");
/*     */ 
/* 907 */             timer.start("compare");
/* 908 */             if (!text.compare(binary))
/* 909 */               System.out.println("NOT EQUIVALENT");
/*     */             else {
/* 911 */               System.out.println("ok");
/*     */             }
/* 913 */             timer.stop("compare");
/* 914 */           } else if (args[i].equals("-showtimes")) {
/* 915 */             showTimes = true;
/*     */           } else {
/* 917 */             System.out.println("Unknown option " + args[i]);
/*     */           }
/*     */         }
/* 920 */         timer.stop();
/* 921 */         if (showTimes)
/* 922 */           timer.show("LTS loading and dumping");
/*     */       }
/*     */       else {
/* 925 */         System.out.println("Options: ");
/* 926 */         System.out.println("    -src path");
/* 927 */         System.out.println("    -dest path");
/* 928 */         System.out.println("    -compare");
/* 929 */         System.out.println("    -generate_binary");
/* 930 */         System.out.println("    -showTimes");
/*     */       }
/*     */     } catch (IOException ioe) {
/* 933 */       System.err.println(ioe);
/*     */     }
/*     */   }
/*     */ 
/*     */   static class FinalState
/*     */     implements LetterToSoundImpl.State
/*     */   {
/*     */     static final int TYPE = 2;
/*     */     String[] phoneList;
/*     */ 
/*     */     public FinalState(String phones)
/*     */     {
/* 728 */       if (phones.equals("epsilon")) {
/* 729 */         this.phoneList = null;
/*     */       } else {
/* 731 */         int i = phones.indexOf('-');
/* 732 */         if (i != -1) {
/* 733 */           this.phoneList = new String[2];
/* 734 */           this.phoneList[0] = phones.substring(0, i);
/* 735 */           this.phoneList[1] = phones.substring(i + 1);
/*     */         } else {
/* 737 */           this.phoneList = new String[1];
/* 738 */           this.phoneList[0] = phones;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*     */     public FinalState(String[] phones)
/*     */     {
/* 749 */       this.phoneList = phones;
/*     */     }
/*     */ 
/*     */     public void append(ArrayList array)
/*     */     {
/* 759 */       if (this.phoneList == null) {
/* 760 */         return;
/*     */       }
/* 762 */       for (int i = 0; i < this.phoneList.length; ++i)
/* 763 */         array.add(this.phoneList[i]);
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 776 */       if (this.phoneList == null)
/* 777 */         return "PHONE epsilon";
/* 778 */       if (this.phoneList.length == 1) {
/* 779 */         return "PHONE " + this.phoneList[0];
/*     */       }
/* 781 */       return "PHONE " + this.phoneList[0] + "-" + this.phoneList[1];
/*     */     }
/*     */ 
/*     */     public boolean compare(LetterToSoundImpl.State other)
/*     */     {
/* 794 */       if (other instanceof FinalState) {
/* 795 */         FinalState otherState = (FinalState)other;
/* 796 */         if (this.phoneList == null) {
/* 797 */           return otherState.phoneList == null;
/*     */         }
/* 799 */         for (int i = 0; i < this.phoneList.length; ++i) {
/* 800 */           if (!this.phoneList[i].equals(otherState.phoneList[i])) {
/* 801 */             return false;
/*     */           }
/*     */         }
/* 804 */         return true;
/*     */       }
/*     */ 
/* 807 */       return false;
/*     */     }
/*     */ 
/*     */     public void writeBinary(DataOutputStream dos)
/*     */       throws IOException
/*     */     {
/* 819 */       dos.writeInt(2);
/* 820 */       if (this.phoneList == null) {
/* 821 */         dos.writeInt(0);
/*     */       } else {
/* 823 */         dos.writeInt(this.phoneList.length);
/* 824 */         for (int i = 0; i < this.phoneList.length; ++i)
/* 825 */           dos.writeInt(LetterToSoundImpl.phonemeTable.indexOf(this.phoneList[i]));
/*     */       }
/*     */     }
/*     */ 
/*     */     public static LetterToSoundImpl.State loadBinary(DataInputStream dis)
/*     */       throws IOException
/*     */     {
/* 842 */       int phoneListLength = dis.readInt();
/*     */       String[] phoneList;
/*     */       String[] phoneList;
/* 844 */       if (phoneListLength == 0)
/* 845 */         phoneList = null;
/*     */       else {
/* 847 */         phoneList = new String[phoneListLength];
/*     */       }
/* 849 */       for (int i = 0; i < phoneListLength; ++i) {
/* 850 */         int index = dis.readInt();
/* 851 */         phoneList[i] = ((String)LetterToSoundImpl.access$000().get(index));
/*     */       }
/* 853 */       return new FinalState(phoneList);
/*     */     }
/*     */   }
/*     */ 
/*     */   static class DecisionState
/*     */     implements LetterToSoundImpl.State
/*     */   {
/*     */     static final int TYPE = 1;
/*     */     int index;
/*     */     char c;
/*     */     int qtrue;
/*     */     int qfalse;
/*     */ 
/*     */     public DecisionState(int index, char c, int qtrue, int qfalse)
/*     */     {
/* 626 */       this.index = index;
/* 627 */       this.c = c;
/* 628 */       this.qtrue = qtrue;
/* 629 */       this.qfalse = qfalse;
/*     */     }
/*     */ 
/*     */     public int getNextState(char[] chars)
/*     */     {
/* 641 */       return (chars[this.index] == this.c) ? this.qtrue : this.qfalse;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 651 */       return "STATE " + Integer.toString(this.index) + " " + Character.toString(this.c) + " " + Integer.toString(this.qtrue) + " " + Integer.toString(this.qfalse);
/*     */     }
/*     */ 
/*     */     public void writeBinary(DataOutputStream dos)
/*     */       throws IOException
/*     */     {
/* 665 */       dos.writeInt(1);
/* 666 */       dos.writeInt(this.index);
/* 667 */       dos.writeChar(this.c);
/* 668 */       dos.writeInt(this.qtrue);
/* 669 */       dos.writeInt(this.qfalse);
/*     */     }
/*     */ 
/*     */     public static LetterToSoundImpl.State loadBinary(DataInputStream dis)
/*     */       throws IOException
/*     */     {
/* 683 */       int index = dis.readInt();
/* 684 */       char c = dis.readChar();
/* 685 */       int qtrue = dis.readInt();
/* 686 */       int qfalse = dis.readInt();
/* 687 */       return new DecisionState(index, c, qtrue, qfalse);
/*     */     }
/*     */ 
/*     */     public boolean compare(LetterToSoundImpl.State other)
/*     */     {
/* 698 */       if (other instanceof DecisionState) {
/* 699 */         DecisionState otherState = (DecisionState)other;
/* 700 */         return (this.index == otherState.index) && (this.c == otherState.c) && (this.qtrue == otherState.qtrue) && (this.qfalse == otherState.qfalse);
/*     */       }
/*     */ 
/* 705 */       return false;
/*     */     }
/*     */   }
/*     */ 
/*     */   static abstract interface State
/*     */   {
/*     */     public abstract void writeBinary(DataOutputStream paramDataOutputStream)
/*     */       throws IOException;
/*     */ 
/*     */     public abstract boolean compare(State paramState);
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.lexicon.LetterToSoundImpl
 * JD-Core Version:    0.5.4
 */