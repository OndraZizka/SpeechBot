/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ public class Item
/*     */   implements Dumpable
/*     */ {
/*     */   private Relation ownerRelation;
/*     */   private ItemContents contents;
/*     */   private Item parent;
/*     */   private Item daughter;
/*     */   private Item next;
/*     */   private Item prev;
/*     */ 
/*     */   public Item(Relation relation, ItemContents sharedContents)
/*     */   {
/*  45 */     this.ownerRelation = relation;
/*  46 */     if (sharedContents != null)
/*  47 */       this.contents = sharedContents;
/*     */     else {
/*  49 */       this.contents = new ItemContents();
/*     */     }
/*  51 */     this.parent = null;
/*  52 */     this.daughter = null;
/*  53 */     this.next = null;
/*  54 */     this.prev = null;
/*     */ 
/*  57 */     getSharedContents().addItemRelation(relation.getName(), this);
/*     */   }
/*     */ 
/*     */   public Item getItemAs(String relationName)
/*     */   {
/*  70 */     return getSharedContents().getItemRelation(relationName);
/*     */   }
/*     */ 
/*     */   public Relation getOwnerRelation()
/*     */   {
/*  80 */     return this.ownerRelation;
/*     */   }
/*     */ 
/*     */   public ItemContents getSharedContents()
/*     */   {
/*  89 */     return this.contents;
/*     */   }
/*     */ 
/*     */   public boolean hasDaughters()
/*     */   {
/*  98 */     return this.daughter != null;
/*     */   }
/*     */ 
/*     */   public Item getDaughter()
/*     */   {
/* 107 */     return this.daughter;
/*     */   }
/*     */ 
/*     */   public Item getNthDaughter(int which)
/*     */   {
/* 118 */     Item d = this.daughter;
/* 119 */     int count = 0;
/* 120 */     while ((count++ != which) && (d != null)) {
/* 121 */       d = d.next;
/*     */     }
/* 123 */     return d;
/*     */   }
/*     */ 
/*     */   public Item getLastDaughter()
/*     */   {
/* 132 */     Item d = this.daughter;
/* 133 */     if (d == null) {
/* 134 */       return null;
/*     */     }
/* 136 */     while (d.next != null) {
/* 137 */       d = d.next;
/*     */     }
/* 139 */     return d;
/*     */   }
/*     */ 
/*     */   public Item addDaughter(Item item)
/*     */   {
/* 151 */     Item p = getLastDaughter();
/*     */     Item newItem;
/*     */     Item newItem;
/* 153 */     if (p != null) {
/* 154 */       newItem = p.appendItem(item);
/*     */     }
/*     */     else
/*     */     {
/*     */       ItemContents contents;
/*     */       ItemContents contents;
/* 156 */       if (item == null)
/* 157 */         contents = new ItemContents();
/*     */       else {
/* 159 */         contents = item.getSharedContents();
/*     */       }
/* 161 */       newItem = new Item(getOwnerRelation(), contents);
/* 162 */       newItem.parent = this;
/* 163 */       this.daughter = newItem;
/*     */     }
/* 165 */     return newItem;
/*     */   }
/*     */ 
/*     */   public Item createDaughter()
/*     */   {
/* 175 */     return addDaughter(null);
/*     */   }
/*     */ 
/*     */   public Item getParent()
/*     */   {
/* 186 */     for (Item n = this; n.prev != null; n = n.prev) {
/* 187 */       if (n == null) {
/* 188 */         return null;
/*     */       }
/*     */     }
/* 191 */     return n.parent;
/*     */   }
/*     */ 
/*     */   public Utterance getUtterance()
/*     */   {
/* 212 */     return getOwnerRelation().getUtterance();
/*     */   }
/*     */ 
/*     */   public FeatureSet getFeatures()
/*     */   {
/* 221 */     return getSharedContents().getFeatures();
/*     */   }
/*     */ 
/*     */   public void dump(PrintWriter out, int pad, String title)
/*     */   {
/* 231 */     String itemName = title + ":" + toString();
/* 232 */     getFeatures().dump(out, pad, itemName);
/* 233 */     if (hasDaughters()) {
/* 234 */       Item daughter = getDaughter();
/* 235 */       while (daughter != null) {
/* 236 */         daughter.dump(out, pad + 8, "d");
/* 237 */         daughter = daughter.next;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object findFeature(String pathAndFeature)
/*     */   {
/* 271 */     Voice voice = getOwnerRelation().getUtterance().getVoice();
/* 272 */     Object results = null;
/*     */ 
/* 275 */     int lastDot = pathAndFeature.lastIndexOf(".");
/*     */     String path;
/*     */     String feature;
/*     */     String path;
/* 278 */     if (lastDot == -1) {
/* 279 */       String feature = pathAndFeature;
/* 280 */       path = null;
/*     */     } else {
/* 282 */       feature = pathAndFeature.substring(lastDot + 1);
/* 283 */       path = pathAndFeature.substring(0, lastDot);
/*     */     }
/*     */ 
/* 287 */     Item item = findItem(path);
/* 288 */     if (item != null) {
/* 289 */       FeatureProcessor fp = voice.getFeatureProcessor(feature);
/*     */ 
/* 291 */       if (fp != null) {
/*     */         try {
/* 293 */           results = fp.process(item);
/*     */         } catch (ProcessException pe) {
/* 295 */           System.err.println("Trouble while processing " + fp.toString());
/*     */         }
/*     */       }
/*     */       else {
/* 299 */         results = item.getFeatures().getObject(feature);
/*     */       }
/*     */     }
/* 302 */     results = (results == null) ? "0" : results;
/*     */ 
/* 308 */     return results;
/*     */   }
/*     */ 
/*     */   public Item findItem(String path)
/*     */   {
/* 333 */     Item pitem = this;
/*     */ 
/* 336 */     if (path == null) {
/* 337 */       return this;
/*     */     }
/*     */ 
/* 340 */     StringTokenizer tok = new StringTokenizer(path, ":.");
/*     */ 
/* 342 */     while ((pitem != null) && (tok.hasMoreTokens())) {
/* 343 */       String token = tok.nextToken();
/* 344 */       if (token.equals("n")) {
/* 345 */         pitem = pitem.getNext();
/* 346 */       } else if (token.equals("p")) {
/* 347 */         pitem = pitem.getPrevious();
/* 348 */       } else if (token.equals("nn")) {
/* 349 */         pitem = pitem.getNext();
/* 350 */         if (pitem != null)
/* 351 */           pitem = pitem.getNext();
/*     */       }
/* 353 */       else if (token.equals("pp")) {
/* 354 */         pitem = pitem.getPrevious();
/* 355 */         if (pitem != null)
/* 356 */           pitem = pitem.getPrevious();
/*     */       }
/* 358 */       else if (token.equals("parent")) {
/* 359 */         pitem = pitem.getParent();
/* 360 */       } else if ((token.equals("daughter")) || (token.equals("daughter1"))) {
/* 361 */         pitem = pitem.getDaughter();
/* 362 */       } else if (token.equals("daughtern")) {
/* 363 */         pitem = pitem.getLastDaughter();
/* 364 */       } else if (token.equals("R")) {
/* 365 */         String relationName = tok.nextToken();
/* 366 */         pitem = pitem.getSharedContents().getItemRelation(relationName);
/*     */       } else {
/* 368 */         System.out.println("findItem: bad feature " + token + " in " + path);
/*     */       }
/*     */     }
/*     */ 
/* 372 */     return pitem;
/*     */   }
/*     */ 
/*     */   public Item getNext()
/*     */   {
/* 382 */     return this.next;
/*     */   }
/*     */ 
/*     */   public Item getPrevious()
/*     */   {
/* 392 */     return this.prev;
/*     */   }
/*     */ 
/*     */   public Item appendItem(Item originalItem)
/*     */   {
/*     */     ItemContents contents;
/*     */     ItemContents contents;
/* 408 */     if (originalItem == null)
/* 409 */       contents = null;
/*     */     else {
/* 411 */       contents = originalItem.getSharedContents();
/*     */     }
/*     */ 
/* 414 */     Item newItem = new Item(getOwnerRelation(), contents);
/* 415 */     newItem.next = this.next;
/* 416 */     if (this.next != null) {
/* 417 */       this.next.prev = newItem;
/*     */     }
/*     */ 
/* 420 */     attach(newItem);
/*     */ 
/* 422 */     if (this.ownerRelation.getTail() == this) {
/* 423 */       this.ownerRelation.setTail(newItem);
/*     */     }
/* 425 */     return newItem;
/*     */   }
/*     */ 
/*     */   void attach(Item item)
/*     */   {
/* 434 */     this.next = item;
/* 435 */     item.prev = this;
/*     */   }
/*     */ 
/*     */   public Item prependItem(Item originalItem)
/*     */   {
/*     */     ItemContents contents;
/*     */     ItemContents contents;
/* 450 */     if (originalItem == null)
/* 451 */       contents = null;
/*     */     else {
/* 453 */       contents = originalItem.getSharedContents();
/*     */     }
/*     */ 
/* 456 */     Item newItem = new Item(getOwnerRelation(), contents);
/* 457 */     newItem.prev = this.prev;
/* 458 */     if (this.prev != null) {
/* 459 */       this.prev.next = newItem;
/*     */     }
/* 461 */     newItem.next = this;
/* 462 */     this.prev = newItem;
/* 463 */     if (this.parent != null) {
/* 464 */       this.parent.daughter = newItem;
/* 465 */       newItem.parent = this.parent;
/* 466 */       this.parent = null;
/*     */     }
/* 468 */     if (this.ownerRelation.getHead() == this) {
/* 469 */       this.ownerRelation.setHead(newItem);
/*     */     }
/* 471 */     return newItem;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 480 */     String name = getFeatures().getString("name");
/* 481 */     if (name == null) {
/* 482 */       name = "";
/*     */     }
/* 484 */     return name;
/*     */   }
/*     */ 
/*     */   public boolean equalsShared(Item otherItem)
/*     */   {
/* 495 */     if (otherItem == null) {
/* 496 */       return false;
/*     */     }
/* 498 */     return getSharedContents().equals(otherItem.getSharedContents());
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.Item
 * JD-Core Version:    0.5.4
 */