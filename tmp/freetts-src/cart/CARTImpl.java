/*     */ package com.sun.speech.freetts.cart;
/*     */ 
/*     */ import com.sun.speech.freetts.Item;
/*     */ import com.sun.speech.freetts.PathExtractor;
/*     */ import com.sun.speech.freetts.PathExtractorImpl;
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.net.URL;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class CARTImpl
/*     */   implements CART
/*     */ {
/*  85 */   private static final Logger LOGGER = Logger.getLogger(CARTImpl.class.getName());
/*     */   static final String TOTAL = "TOTAL";
/*     */   static final String NODE = "NODE";
/*     */   static final String LEAF = "LEAF";
/*     */   static final String OPERAND_MATCHES = "MATCHES";
/* 120 */   Node[] cart = null;
/*     */ 
/* 125 */   transient int curNode = 0;
/*     */ 
/*     */   public CARTImpl(URL url)
/*     */     throws IOException
/*     */   {
/* 138 */     BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
/* 139 */     String line = reader.readLine();
/* 140 */     while (line != null) {
/* 141 */       if (!line.startsWith("***")) {
/* 142 */         parseAndAdd(line);
/*     */       }
/* 144 */       line = reader.readLine();
/*     */     }
/* 146 */     reader.close();
/*     */   }
/*     */ 
/*     */   public CARTImpl(BufferedReader reader, int nodes)
/*     */     throws IOException
/*     */   {
/* 158 */     this(nodes);
/*     */ 
/* 160 */     for (int i = 0; i < nodes; ++i) {
/* 161 */       String line = reader.readLine();
/* 162 */       if (!line.startsWith("***"))
/* 163 */         parseAndAdd(line);
/*     */     }
/*     */   }
/*     */ 
/*     */   private CARTImpl(int numNodes)
/*     */   {
/* 174 */     this.cart = new Node[numNodes];
/*     */   }
/*     */ 
/*     */   public void dumpBinary(DataOutputStream os)
/*     */     throws IOException
/*     */   {
/* 185 */     os.writeInt(this.cart.length);
/* 186 */     for (int i = 0; i < this.cart.length; ++i)
/* 187 */       this.cart[i].dumpBinary(os);
/*     */   }
/*     */ 
/*     */   public static CART loadBinary(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 204 */     int numNodes = bb.getInt();
/* 205 */     CARTImpl cart = new CARTImpl(numNodes);
/*     */ 
/* 207 */     for (int i = 0; i < numNodes; ++i) {
/* 208 */       String nodeCreationLine = Utilities.getString(bb);
/* 209 */       cart.parseAndAdd(nodeCreationLine);
/*     */     }
/* 211 */     return cart;
/*     */   }
/*     */ 
/*     */   public static CART loadBinary(DataInputStream is)
/*     */     throws IOException
/*     */   {
/* 227 */     int numNodes = is.readInt();
/* 228 */     CARTImpl cart = new CARTImpl(numNodes);
/*     */ 
/* 230 */     for (int i = 0; i < numNodes; ++i) {
/* 231 */       String nodeCreationLine = Utilities.getString(is);
/* 232 */       cart.parseAndAdd(nodeCreationLine);
/*     */     }
/* 234 */     return cart;
/*     */   }
/*     */ 
/*     */   protected void parseAndAdd(String line)
/*     */   {
/* 244 */     StringTokenizer tokenizer = new StringTokenizer(line, " ");
/* 245 */     String type = tokenizer.nextToken();
/* 246 */     if ((type.equals("LEAF")) || (type.equals("NODE"))) {
/* 247 */       this.cart[this.curNode] = getNode(type, tokenizer, this.curNode);
/* 248 */       this.cart[this.curNode].setCreationLine(line);
/* 249 */       this.curNode += 1;
/* 250 */     } else if (type.equals("TOTAL")) {
/* 251 */       this.cart = new Node[Integer.parseInt(tokenizer.nextToken())];
/* 252 */       this.curNode = 0;
/*     */     } else {
/* 254 */       throw new Error("Invalid CART type: " + type);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Node getNode(String type, StringTokenizer tokenizer, int currentNode)
/*     */   {
/* 270 */     if (type.equals("NODE")) {
/* 271 */       String feature = tokenizer.nextToken();
/* 272 */       String operand = tokenizer.nextToken();
/* 273 */       Object value = parseValue(tokenizer.nextToken());
/* 274 */       int qfalse = Integer.parseInt(tokenizer.nextToken());
/* 275 */       if (operand.equals("MATCHES")) {
/* 276 */         return new MatchingNode(feature, value.toString(), currentNode + 1, qfalse);
/*     */       }
/*     */ 
/* 281 */       return new ComparisonNode(feature, value, operand, currentNode + 1, qfalse);
/*     */     }
/*     */ 
/* 287 */     if (type.equals("LEAF")) {
/* 288 */       return new LeafNode(parseValue(tokenizer.nextToken()));
/*     */     }
/*     */ 
/* 291 */     return null;
/*     */   }
/*     */ 
/*     */   protected Object parseValue(String string)
/*     */   {
/* 302 */     int openParen = string.indexOf("(");
/* 303 */     String type = string.substring(0, openParen);
/* 304 */     String value = string.substring(openParen + 1, string.length() - 1);
/* 305 */     if (type.equals("String"))
/* 306 */       return value;
/* 307 */     if (type.equals("Float"))
/* 308 */       return new Float(Float.parseFloat(value));
/* 309 */     if (type.equals("Integer"))
/* 310 */       return new Integer(Integer.parseInt(value));
/* 311 */     if (type.equals("List")) {
/* 312 */       StringTokenizer tok = new StringTokenizer(value, ",");
/* 313 */       int size = tok.countTokens();
/*     */ 
/* 315 */       int[] values = new int[size];
/* 316 */       for (int i = 0; i < size; ++i) {
/* 317 */         float fval = Float.parseFloat(tok.nextToken());
/* 318 */         values[i] = Math.round(fval);
/*     */       }
/* 320 */       return values;
/*     */     }
/* 322 */     throw new Error("Unknown type: " + type);
/*     */   }
/*     */ 
/*     */   public Object interpret(Item item)
/*     */   {
/* 335 */     int nodeIndex = 0;
/*     */ 
/* 338 */     while (!this.cart[nodeIndex] instanceof LeafNode) {
/* 339 */       DecisionNode decision = (DecisionNode)this.cart[nodeIndex];
/* 340 */       nodeIndex = decision.getNextNode(item);
/*     */     }
/* 342 */     if (LOGGER.isLoggable(Level.FINER)) {
/* 343 */       LOGGER.finer("LEAF " + this.cart[nodeIndex].getValue());
/*     */     }
/* 345 */     return ((LeafNode)this.cart[nodeIndex]).getValue();
/*     */   }
/*     */ 
/*     */   static class LeafNode extends CARTImpl.Node
/*     */   {
/*     */     public LeafNode(Object value)
/*     */     {
/* 653 */       super(value);
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 660 */       return "LEAF " + getValueString();
/*     */     }
/*     */   }
/*     */ 
/*     */   static class MatchingNode extends CARTImpl.DecisionNode
/*     */   {
/*     */     Pattern pattern;
/*     */ 
/*     */     public MatchingNode(String feature, String regex, int qtrue, int qfalse)
/*     */     {
/* 617 */       super(feature, regex, qtrue, qfalse);
/* 618 */       this.pattern = Pattern.compile(regex);
/*     */     }
/*     */ 
/*     */     public int getNextNode(Object val)
/*     */     {
/* 626 */       return (this.pattern.matcher((String)val).matches()) ? this.qtrue : this.qfalse;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 635 */       StringBuffer buf = new StringBuffer("NODE " + getFeature() + " " + "MATCHES");
/*     */ 
/* 637 */       buf.append(getValueString() + " ");
/* 638 */       buf.append(Integer.toString(this.qtrue) + " ");
/* 639 */       buf.append(Integer.toString(this.qfalse));
/* 640 */       return buf.toString();
/*     */     }
/*     */   }
/*     */ 
/*     */   static class ComparisonNode extends CARTImpl.DecisionNode
/*     */   {
/*     */     static final String LESS_THAN = "<";
/*     */     static final String EQUALS = "=";
/*     */     static final String GREATER_THAN = ">";
/*     */     String comparisonType;
/*     */ 
/*     */     public ComparisonNode(String feature, Object value, String comparisonType, int qtrue, int qfalse)
/*     */     {
/* 517 */       super(feature, value, qtrue, qfalse);
/* 518 */       if ((!comparisonType.equals("<")) && (!comparisonType.equals("=")) && (!comparisonType.equals(">")))
/*     */       {
/* 521 */         throw new Error("Invalid comparison type: " + comparisonType);
/*     */       }
/* 523 */       this.comparisonType = comparisonType;
/*     */     }
/*     */ 
/*     */     public int getNextNode(Object val)
/*     */     {
/* 537 */       boolean yes = false;
/*     */ 
/* 540 */       if ((this.comparisonType.equals("<")) || (this.comparisonType.equals(">")))
/*     */       {
/*     */         float cart_fval;
/*     */         float cart_fval;
/* 544 */         if (this.value instanceof Float)
/* 545 */           cart_fval = ((Float)this.value).floatValue();
/*     */         else
/* 547 */           cart_fval = Float.parseFloat(this.value.toString());
/*     */         float fval;
/*     */         float fval;
/* 549 */         if (val instanceof Float)
/* 550 */           fval = ((Float)val).floatValue();
/*     */         else {
/* 552 */           fval = Float.parseFloat(val.toString());
/*     */         }
/* 554 */         if (this.comparisonType.equals("<"))
/* 555 */           yes = fval < cart_fval;
/*     */         else
/* 557 */           yes = fval > cart_fval;
/*     */       }
/*     */       else {
/* 560 */         String sval = val.toString();
/* 561 */         String cart_sval = this.value.toString();
/* 562 */         yes = sval.equals(cart_sval);
/*     */       }
/*     */       int ret;
/*     */       int ret;
/* 564 */       if (yes)
/* 565 */         ret = this.qtrue;
/*     */       else {
/* 567 */         ret = this.qfalse;
/*     */       }
/*     */ 
/* 570 */       if (CARTImpl.LOGGER.isLoggable(Level.FINER)) {
/* 571 */         CARTImpl.LOGGER.finer(trace(val, yes, ret));
/*     */       }
/*     */ 
/* 574 */       return ret;
/*     */     }
/*     */ 
/*     */     private String trace(Object value, boolean match, int next) {
/* 578 */       return "NODE " + getFeature() + " [" + value + "] " + this.comparisonType + " [" + getValue() + "] " + ((match) ? "Yes" : "No") + " next " + next;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 591 */       return "NODE " + getFeature() + " " + this.comparisonType + " " + getValueString() + " " + Integer.toString(this.qtrue) + " " + Integer.toString(this.qfalse);
/*     */     }
/*     */   }
/*     */ 
/*     */   static abstract class DecisionNode extends CARTImpl.Node
/*     */   {
/*     */     private PathExtractor path;
/*     */     protected int qfalse;
/*     */     protected int qtrue;
/*     */ 
/*     */     public String getFeature()
/*     */     {
/* 430 */       return this.path.toString();
/*     */     }
/*     */ 
/*     */     public Object findFeature(Item item)
/*     */     {
/* 441 */       return this.path.findFeature(item);
/*     */     }
/*     */ 
/*     */     public final int getNextNode(Item item)
/*     */     {
/* 452 */       return getNextNode(findFeature(item));
/*     */     }
/*     */ 
/*     */     public DecisionNode(String feature, Object value, int qtrue, int qfalse)
/*     */     {
/* 466 */       super(value);
/* 467 */       this.path = new PathExtractorImpl(feature, true);
/* 468 */       this.qtrue = qtrue;
/* 469 */       this.qfalse = qfalse;
/*     */     }
/*     */ 
/*     */     public abstract int getNextNode(Object paramObject);
/*     */   }
/*     */ 
/*     */   static abstract class Node
/*     */   {
/*     */     protected Object value;
/*     */     private String creationLine;
/*     */ 
/*     */     public Node(Object value)
/*     */     {
/* 362 */       this.value = value;
/*     */     }
/*     */ 
/*     */     public Object getValue()
/*     */     {
/* 369 */       return this.value;
/*     */     }
/*     */ 
/*     */     public String getValueString()
/*     */     {
/* 376 */       if (this.value == null)
/* 377 */         return "NULL()";
/* 378 */       if (this.value instanceof String)
/* 379 */         return "String(" + this.value.toString() + ")";
/* 380 */       if (this.value instanceof Float)
/* 381 */         return "Float(" + this.value.toString() + ")";
/* 382 */       if (this.value instanceof Integer) {
/* 383 */         return "Integer(" + this.value.toString() + ")";
/*     */       }
/* 385 */       return this.value.getClass().toString() + "(" + this.value.toString() + ")";
/*     */     }
/*     */ 
/*     */     public void setCreationLine(String line)
/*     */     {
/* 394 */       this.creationLine = line;
/*     */     }
/*     */ 
/*     */     public final void dumpBinary(DataOutputStream os)
/*     */       throws IOException
/*     */     {
/* 403 */       Utilities.outString(os, this.creationLine);
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.cart.CARTImpl
 * JD-Core Version:    0.5.4
 */