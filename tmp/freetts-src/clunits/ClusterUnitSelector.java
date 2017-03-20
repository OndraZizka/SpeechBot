/*      */ package com.sun.speech.freetts.clunits;
/*      */ 
/*      */ import com.sun.speech.freetts.FeatureSet;
/*      */ import com.sun.speech.freetts.FeatureSetImpl;
/*      */ import com.sun.speech.freetts.Item;
/*      */ import com.sun.speech.freetts.PathExtractor;
/*      */ import com.sun.speech.freetts.PathExtractorImpl;
/*      */ import com.sun.speech.freetts.ProcessException;
/*      */ import com.sun.speech.freetts.Relation;
/*      */ import com.sun.speech.freetts.Utterance;
/*      */ import com.sun.speech.freetts.UtteranceProcessor;
/*      */ import com.sun.speech.freetts.Voice;
/*      */ import com.sun.speech.freetts.cart.CART;
/*      */ import com.sun.speech.freetts.relp.Sample;
/*      */ import com.sun.speech.freetts.relp.SampleInfo;
/*      */ import com.sun.speech.freetts.relp.SampleSet;
/*      */ import de.dfki.lt.freetts.ClusterUnitNamer;
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ import java.net.URL;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */ 
/*      */ public class ClusterUnitSelector
/*      */   implements UtteranceProcessor
/*      */ {
/*   43 */   private static final Logger LOGGER = Logger.getLogger(ClusterUnitSelector.class.getName());
/*      */ 
/*   46 */   private static final PathExtractor DNAME = new PathExtractorImpl("R:SylStructure.parent.parent.name", true);
/*      */   private ClusterUnitDatabase clunitDB;
/*      */   private ClusterUnitNamer unitNamer;
/*      */ 
/*      */   public ClusterUnitSelector(URL url)
/*      */     throws IOException
/*      */   {
/*   63 */     this(url, null);
/*      */   }
/*      */ 
/*      */   public ClusterUnitSelector(URL url, ClusterUnitNamer unitNamer)
/*      */     throws IOException
/*      */   {
/*   82 */     if (url == null) {
/*   83 */       throw new IOException("Can't load cluster unit database");
/*      */     }
/*   85 */     boolean binary = url.getPath().endsWith(".bin");
/*   86 */     this.clunitDB = new ClusterUnitDatabase(url, binary);
/*   87 */     this.unitNamer = unitNamer;
/*      */   }
/*      */ 
/*      */   public SampleInfo getSampleInfo()
/*      */   {
/*   97 */     return this.clunitDB.getSampleInfo();
/*      */   }
/*      */ 
/*      */   public void processUtterance(Utterance utterance)
/*      */     throws ProcessException
/*      */   {
/*  130 */     Relation segs = utterance.getRelation("Segment");
/*      */ 
/*  132 */     utterance.setObject("SampleInfo", this.clunitDB.getSampleInfo());
/*      */ 
/*  134 */     utterance.setObject("sts_list", this.clunitDB.getSts());
/*      */ 
/*  136 */     Viterbi vd = new Viterbi(segs, this.clunitDB);
/*      */ 
/*  138 */     for (Item s = segs.getHead(); s != null; s = s.getNext()) {
/*  139 */       setUnitName(s);
/*      */     }
/*      */ 
/*  144 */     vd.decode();
/*      */ 
/*  148 */     if (!vd.result("selected_unit")) {
/*  149 */       LOGGER.severe("clunits: can't find path");
/*  150 */       throw new Error();
/*      */     }
/*      */ 
/*  155 */     vd.copyFeature("unit_prev_move");
/*  156 */     vd.copyFeature("unit_this_move");
/*      */ 
/*  160 */     Relation unitRelation = utterance.createRelation("Unit");
/*      */ 
/*  162 */     for (Item s = segs.getHead(); s != null; s = s.getNext()) {
/*  163 */       Item unit = unitRelation.appendItem();
/*  164 */       FeatureSet unitFeatureSet = unit.getFeatures();
/*  165 */       int unitEntry = s.getFeatures().getInt("selected_unit");
/*      */ 
/*  168 */       unitFeatureSet.setString("name", s.getFeatures().getString("name"));
/*      */ 
/*  172 */       String clunitName = s.getFeatures().getString("clunit_name");
/*      */       int unitStart;
/*      */       int unitStart;
/*  174 */       if (s.getFeatures().isPresent("unit_this_move"))
/*  175 */         unitStart = s.getFeatures().getInt("unit_this_move");
/*      */       else
/*  177 */         unitStart = this.clunitDB.getStart(unitEntry);
/*      */       int unitEnd;
/*      */       int unitEnd;
/*  180 */       if ((s.getNext() != null) && (s.getNext().getFeatures().isPresent("unit_prev_move")))
/*      */       {
/*  182 */         unitEnd = s.getNext().getFeatures().getInt("unit_prev_move");
/*      */       }
/*  184 */       else unitEnd = this.clunitDB.getEnd(unitEntry);
/*      */ 
/*  187 */       unitFeatureSet.setInt("unit_entry", unitEntry);
/*  188 */       ClusterUnit clunit = new ClusterUnit(this.clunitDB, clunitName, unitStart, unitEnd);
/*      */ 
/*  190 */       unitFeatureSet.setObject("unit", clunit);
/*      */ 
/*  192 */       unitFeatureSet.setInt("unit_start", clunit.getStart());
/*  193 */       unitFeatureSet.setInt("unit_end", clunit.getEnd());
/*  194 */       unitFeatureSet.setInt("instance", unitEntry - this.clunitDB.getUnitIndex(clunitName, 0));
/*      */ 
/*  197 */       if (LOGGER.isLoggable(Level.FINE)) {
/*  198 */         LOGGER.fine(" sr " + this.clunitDB.getSampleInfo().getSampleRate() + " " + s.getFeatures().getFloat("end") + " " + (int)(s.getFeatures().getFloat("end") * this.clunitDB.getSampleInfo().getSampleRate()));
/*      */       }
/*      */ 
/*  203 */       unitFeatureSet.setInt("target_end", (int)(s.getFeatures().getFloat("end") * this.clunitDB.getSampleInfo().getSampleRate()));
/*      */ 
/*  208 */       ClusterUnitDatabase.UnitOriginInfo unitOrigin = this.clunitDB.getUnitOriginInfo(unitEntry);
/*  209 */       if (unitOrigin != null) {
/*  210 */         unitFeatureSet.setString("origin", unitOrigin.originFile);
/*  211 */         unitFeatureSet.setFloat("origin_start", unitOrigin.originStart);
/*  212 */         unitFeatureSet.setFloat("origin_end", unitOrigin.originEnd);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setUnitName(Item seg)
/*      */   {
/*  225 */     if (this.unitNamer != null) {
/*  226 */       this.unitNamer.setUnitName(seg);
/*  227 */       return;
/*      */     }
/*      */ 
/*  230 */     String cname = null;
/*      */ 
/*  232 */     String segName = seg.getFeatures().getString("name");
/*      */ 
/*  234 */     Voice voice = seg.getUtterance().getVoice();
/*  235 */     String silenceSymbol = voice.getPhoneFeature("silence", "symbol");
/*  236 */     if (silenceSymbol == null)
/*  237 */       silenceSymbol = "pau";
/*  238 */     if (segName.equals(silenceSymbol)) {
/*  239 */       cname = silenceSymbol + "_" + seg.findFeature("p.name");
/*      */     }
/*      */     else {
/*  242 */       String dname = ((String)DNAME.findFeature(seg)).toLowerCase();
/*  243 */       cname = segName + "_" + stripQuotes(dname);
/*      */     }
/*  245 */     seg.getFeatures().setString("clunit_name", cname);
/*      */   }
/*      */ 
/*      */   private String stripQuotes(String s)
/*      */   {
/*  257 */     StringBuffer sb = new StringBuffer(s.length());
/*  258 */     for (int i = 0; i < s.length(); ++i) {
/*  259 */       char c = s.charAt(i);
/*  260 */       if (c != '\'') {
/*  261 */         sb.append(c);
/*      */       }
/*      */     }
/*  264 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/*  274 */     return "ClusterUnitSelector";
/*      */   }
/*      */ 
/*      */   static class ViterbiPath
/*      */   {
/*      */     int score;
/*      */     int state;
/*      */     ClusterUnitSelector.ViterbiCandidate candidate;
/*      */     private FeatureSet f;
/*      */     ViterbiPath from;
/*      */     ViterbiPath next;
/*      */ 
/*      */     ViterbiPath()
/*      */     {
/* 1009 */       this.score = 0;
/* 1010 */       this.state = 0;
/* 1011 */       this.candidate = null;
/* 1012 */       this.f = null;
/* 1013 */       this.from = null;
/* 1014 */       this.next = null;
/*      */     }
/*      */ 
/*      */     void setFeature(String name, Object value)
/*      */     {
/* 1023 */       if (this.f == null) {
/* 1024 */         this.f = new FeatureSetImpl();
/*      */       }
/* 1026 */       this.f.setObject(name, value);
/*      */     }
/*      */ 
/*      */     Object getFeature(String name)
/*      */     {
/* 1037 */       Object value = null;
/* 1038 */       if (this.f != null) {
/* 1039 */         value = this.f.getObject(name);
/*      */       }
/* 1041 */       return value;
/*      */     }
/*      */ 
/*      */     boolean isPresent(String name)
/*      */     {
/* 1054 */       if (this.f == null) {
/* 1055 */         return false;
/*      */       }
/* 1057 */       return getFeature(name) != null;
/*      */     }
/*      */ 
/*      */     public String toString()
/*      */     {
/* 1067 */       return "ViterbiPath score " + this.score + " state " + this.state;
/*      */     }
/*      */   }
/*      */ 
/*      */   static class ViterbiCandidate
/*      */   {
/*      */     int score;
/*      */     Object value;
/*      */     int ival;
/*      */     int pos;
/*      */     Item item;
/*      */     ViterbiCandidate next;
/*      */ 
/*      */     ViterbiCandidate()
/*      */     {
/*  968 */       this.score = 0;
/*  969 */       this.value = null;
/*  970 */       this.ival = 0;
/*  971 */       this.pos = 0;
/*  972 */       this.item = null;
/*  973 */       this.next = null;
/*      */     }
/*      */ 
/*      */     void set(Object obj)
/*      */     {
/*  981 */       this.value = obj;
/*      */     }
/*      */ 
/*      */     void setInt(int ival)
/*      */     {
/*  991 */       this.ival = ival;
/*  992 */       set(new Integer(ival));
/*      */     }
/*      */ 
/*      */     public String toString()
/*      */     {
/* 1001 */       return "VC: Score " + this.score + " ival " + this.ival + " Pos " + this.pos;
/*      */     }
/*      */   }
/*      */ 
/*      */   static class ViterbiPoint
/*      */   {
/*  904 */     Item item = null;
/*      */ 
/*  906 */     int numStates = 0;
/*  907 */     int numPaths = 0;
/*  908 */     ClusterUnitSelector.ViterbiCandidate cands = null;
/*  909 */     ClusterUnitSelector.ViterbiPath paths = null;
/*  910 */     ClusterUnitSelector.ViterbiPath[] statePaths = null;
/*  911 */     ViterbiPoint next = null;
/*      */ 
/*      */     public ViterbiPoint(Item item)
/*      */     {
/*  919 */       this.item = item;
/*      */     }
/*      */ 
/*      */     public void initPathArray(int size)
/*      */     {
/*  928 */       if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  929 */         ClusterUnitSelector.LOGGER.fine("init_path_array: " + size);
/*      */       }
/*  931 */       this.numStates = size;
/*  932 */       this.statePaths = new ClusterUnitSelector.ViterbiPath[size];
/*      */     }
/*      */ 
/*      */     public void initDynamicPathArray(ClusterUnitSelector.ViterbiCandidate candidate)
/*      */     {
/*  946 */       int i = 0;
/*  947 */       for (ClusterUnitSelector.ViterbiCandidate cc = candidate; cc != null; )
/*      */       {
/*  949 */         cc.pos = i;
/*      */ 
/*  948 */         ++i; cc = cc.next;
/*      */       }
/*      */ 
/*  951 */       if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  952 */         ClusterUnitSelector.LOGGER.fine("init_dynamic_ path_array: " + i);
/*      */       }
/*  954 */       initPathArray(i);
/*      */     }
/*      */ 
/*      */     public String toString() {
/*  958 */       return " pnt: " + this.numStates + " paths " + this.numPaths;
/*      */     }
/*      */   }
/*      */ 
/*      */   static class Viterbi
/*      */   {
/*  303 */     private int numStates = -1;
/*  304 */     private boolean bigIsGood = false;
/*  305 */     private ClusterUnitSelector.ViterbiPoint timeline = null;
/*  306 */     private ClusterUnitSelector.ViterbiPoint lastPoint = null;
/*  307 */     private FeatureSet f = null;
/*      */     private ClusterUnitDatabase clunitDB;
/*      */ 
/*      */     public Viterbi(Relation segs, ClusterUnitDatabase db)
/*      */     {
/*  317 */       ClusterUnitSelector.ViterbiPoint last = null;
/*  318 */       this.clunitDB = db;
/*  319 */       this.f = new FeatureSetImpl();
/*  320 */       for (Item s = segs.getHead(); ; s = s.getNext()) {
/*  321 */         ClusterUnitSelector.ViterbiPoint n = new ClusterUnitSelector.ViterbiPoint(s);
/*      */ 
/*  325 */         if (this.numStates > 0) {
/*  326 */           n.initPathArray(this.numStates);
/*      */         }
/*  328 */         if (last != null)
/*  329 */           last.next = n;
/*      */         else {
/*  331 */           this.timeline = n;
/*      */         }
/*  333 */         last = n;
/*      */ 
/*  335 */         if (s == null) {
/*  336 */           this.lastPoint = n;
/*  337 */           break;
/*      */         }
/*      */       }
/*      */ 
/*  341 */       if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  342 */         ClusterUnitSelector.LOGGER.fine("num states " + this.numStates);
/*      */       }
/*      */ 
/*  345 */       if (this.numStates == 0) {
/*  346 */         this.timeline.paths = new ClusterUnitSelector.ViterbiPath();
/*      */       }
/*      */ 
/*  349 */       if (this.numStates == -1)
/*  350 */         this.timeline.initPathArray(1);
/*      */     }
/*      */ 
/*      */     public void setFeature(String name, Object obj)
/*      */     {
/*  361 */       this.f.setObject(name, obj);
/*      */     }
/*      */ 
/*      */     public Object getFeature(String name)
/*      */     {
/*  372 */       return this.f.getObject(name);
/*      */     }
/*      */ 
/*      */     void decode()
/*      */     {
/*  390 */       for (ClusterUnitSelector.ViterbiPoint p = this.timeline; p.next != null; p = p.next)
/*      */       {
/*  392 */         p.cands = getCandidate(p.item);
/*  393 */         if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  394 */           ClusterUnitSelector.LOGGER.fine("decode " + p.cands);
/*      */         }
/*  396 */         if (this.numStates != 0) {
/*  397 */           if (this.numStates == -1)
/*      */           {
/*  399 */             p.next.initDynamicPathArray(p.cands);
/*      */           }
/*      */ 
/*  409 */           for (int i = 0; i < p.numStates; ++i) {
/*  410 */             if ((((p != this.timeline) || (i != 0))) && (p.statePaths[i] == null)) {
/*      */               continue;
/*      */             }
/*      */ 
/*  414 */             ClusterUnitSelector.ViterbiCandidate c = p.cands;
/*  415 */             for (; c != null; c = c.next)
/*      */             {
/*  418 */               ClusterUnitSelector.ViterbiPath np = getPath(p.statePaths[i], c);
/*      */ 
/*  423 */               addPaths(p.next, np);
/*      */             }
/*      */           }
/*      */         }
/*      */         else {
/*  428 */           System.err.println("Viterbi.decode: general beam search not implemented");
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*      */     void addPaths(ClusterUnitSelector.ViterbiPoint point, ClusterUnitSelector.ViterbiPath path)
/*      */     {
/*      */       ClusterUnitSelector.ViterbiPath nextPath;
/*  443 */       for (ClusterUnitSelector.ViterbiPath p = path; p != null; p = nextPath) {
/*  444 */         nextPath = p.next;
/*  445 */         addPath(point, p);
/*      */       }
/*      */     }
/*      */ 
/*      */     void addPath(ClusterUnitSelector.ViterbiPoint point, ClusterUnitSelector.ViterbiPath newPath)
/*      */     {
/*  463 */       if (point.statePaths[newPath.state] == null)
/*      */       {
/*  465 */         point.statePaths[newPath.state] = newPath; } else {
/*  466 */         if (!isBetterThan(newPath.score, point.statePaths[newPath.state].score)) {
/*      */           return;
/*      */         }
/*  469 */         point.statePaths[newPath.state] = newPath;
/*      */       }
/*      */     }
/*      */ 
/*      */     private boolean isBetterThan(int a, int b)
/*      */     {
/*  486 */       if (this.bigIsGood) {
/*  487 */         return a > b;
/*      */       }
/*  489 */       return a < b;
/*      */     }
/*      */ 
/*      */     boolean result(String feature)
/*      */     {
/*  503 */       if ((this.timeline == null) || (this.timeline.next == null)) {
/*  504 */         return true;
/*      */       }
/*  506 */       ClusterUnitSelector.ViterbiPath path = findBestPath();
/*      */ 
/*  508 */       if (path == null) {
/*  509 */         return false;
/*      */       }
/*      */ 
/*  512 */       for (; path != null; path = path.from) {
/*  513 */         if (path.candidate != null) {
/*  514 */           path.candidate.item.getFeatures().setObject(feature, path.candidate.value);
/*      */         }
/*      */       }
/*      */ 
/*  518 */       return true;
/*      */     }
/*      */ 
/*      */     void copyFeature(String feature)
/*      */     {
/*  528 */       ClusterUnitSelector.ViterbiPath path = findBestPath();
/*  529 */       if (path == null) {
/*  530 */         return;
/*      */       }
/*      */ 
/*  533 */       for (; path != null; path = path.from)
/*  534 */         if ((path.candidate != null) && (path.isPresent(feature)))
/*  535 */           path.candidate.item.getFeatures().setObject(feature, path.getFeature(feature));
/*      */     }
/*      */ 
/*      */     private ClusterUnitSelector.ViterbiCandidate getCandidate(Item item)
/*      */     {
/*  552 */       String unitType = item.getFeatures().getString("clunit_name");
/*  553 */       CART cart = this.clunitDB.getTree(unitType);
/*      */ 
/*  555 */       int[] clist = (int[])cart.interpret(item);
/*      */ 
/*  563 */       ClusterUnitSelector.ViterbiCandidate all = null;
/*  564 */       for (int i = 0; i < clist.length; ++i) {
/*  565 */         ClusterUnitSelector.ViterbiCandidate p = new ClusterUnitSelector.ViterbiCandidate();
/*  566 */         p.next = all;
/*  567 */         p.item = item;
/*  568 */         p.score = 0;
/*      */ 
/*  570 */         p.setInt(this.clunitDB.getUnitIndex(unitType, clist[i]));
/*  571 */         all = p;
/*      */ 
/*  573 */         if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE))
/*  574 */           ClusterUnitSelector.LOGGER.fine("    gc adding " + clist[i]);
/*      */       }
/*      */       ClusterUnitSelector.ViterbiCandidate lc;
/*      */       int e;
/*  586 */       if ((this.clunitDB.getExtendSelections() > 0) && (item.getPrevious() != null))
/*      */       {
/*  589 */         lc = (ClusterUnitSelector.ViterbiCandidate)item.getPrevious().getFeatures().getObject("clunit_cands");
/*      */ 
/*  591 */         if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  592 */           ClusterUnitSelector.LOGGER.fine("      lc " + lc);
/*      */         }
/*  594 */         for (e = 0; (lc != null) && (e < this.clunitDB.getExtendSelections()); )
/*      */         {
/*  597 */           int nu = this.clunitDB.getNextUnit(lc.ival);
/*  598 */           if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  599 */             ClusterUnitSelector.LOGGER.fine("      e: " + e + " nu: " + nu);
/*      */           }
/*  601 */           if (nu != 65535)
/*      */           {
/*  606 */             for (ClusterUnitSelector.ViterbiCandidate gt = all; gt != null; gt = gt.next) {
/*  607 */               if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  608 */                 ClusterUnitSelector.LOGGER.fine("       gt " + gt.ival + " nu " + nu);
/*      */               }
/*  610 */               if (nu == gt.ival)
/*      */               {
/*      */                 break;
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*  617 */             if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  618 */               ClusterUnitSelector.LOGGER.fine("nu " + this.clunitDB.getUnit(nu).getName() + " all " + this.clunitDB.getUnit(all.ival).getName() + " " + all.ival);
/*      */             }
/*      */ 
/*  622 */             if ((gt == null) && (this.clunitDB.isUnitTypeEqual(nu, all.ival)))
/*      */             {
/*  625 */               ClusterUnitSelector.ViterbiCandidate p = new ClusterUnitSelector.ViterbiCandidate();
/*  626 */               p.next = all;
/*  627 */               p.item = item;
/*  628 */               p.score = 0;
/*  629 */               p.setInt(nu);
/*  630 */               all = p;
/*  631 */               ++e;
/*      */             }
/*      */           }
/*  596 */           lc = lc.next;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  635 */       item.getFeatures().setObject("clunit_cands", all);
/*  636 */       return all;
/*      */     }
/*      */ 
/*      */     private ClusterUnitSelector.ViterbiPath getPath(ClusterUnitSelector.ViterbiPath path, ClusterUnitSelector.ViterbiCandidate candidate)
/*      */     {
/*  657 */       ClusterUnitSelector.ViterbiPath newPath = new ClusterUnitSelector.ViterbiPath();
/*      */ 
/*  659 */       newPath.candidate = candidate;
/*  660 */       newPath.from = path;
/*      */       int cost;
/*      */       int cost;
/*  669 */       if ((path == null) || (path.candidate == null)) {
/*  670 */         cost = 0;
/*      */       } else {
/*  672 */         int u0 = path.candidate.ival;
/*  673 */         int u1 = candidate.ival;
/*      */         int cost;
/*  674 */         if (this.clunitDB.getOptimalCoupling() == 1) {
/*  675 */           Cost oCost = getOptimalCouple(u0, u1);
/*  676 */           if (oCost.u0Move != -1) {
/*  677 */             newPath.setFeature("unit_prev_move", new Integer(oCost.u0Move));
/*      */           }
/*      */ 
/*  680 */           if (oCost.u1Move != -1) {
/*  681 */             newPath.setFeature("unit_this_move", new Integer(oCost.u1Move));
/*      */           }
/*      */ 
/*  684 */           cost = oCost.cost;
/*      */         }
/*      */         else
/*      */         {
/*      */           int cost;
/*  685 */           if (this.clunitDB.getOptimalCoupling() == 2)
/*  686 */             cost = getOptimalCoupleFrame(u0, u1);
/*      */           else {
/*  688 */             cost = 0;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  693 */       cost *= 5;
/*      */ 
/*  695 */       newPath.state = candidate.pos;
/*  696 */       if (path == null)
/*  697 */         newPath.score = (cost + candidate.score);
/*      */       else {
/*  699 */         newPath.score = (cost + candidate.score + path.score);
/*      */       }
/*      */ 
/*  702 */       return newPath;
/*      */     }
/*      */ 
/*      */     private ClusterUnitSelector.ViterbiPath findBestPath()
/*      */     {
/*  714 */       ClusterUnitSelector.ViterbiPath bestPath = null;
/*      */       int worst;
/*      */       int worst;
/*  716 */       if (this.bigIsGood)
/*  717 */         worst = -2147483648;
/*      */       else {
/*  719 */         worst = 2147483647;
/*      */       }
/*      */ 
/*  722 */       int best = worst;
/*      */ 
/*  725 */       ClusterUnitSelector.ViterbiPoint t = this.lastPoint;
/*      */ 
/*  727 */       if (this.numStates != 0) {
/*  728 */         if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  729 */           ClusterUnitSelector.LOGGER.fine("fbp ns " + this.numStates + " t " + t.numStates + " best " + best);
/*      */         }
/*      */ 
/*  736 */         for (int i = 0; i < t.numStates; ++i) {
/*  737 */           if ((t.statePaths[i] == null) || (!isBetterThan(t.statePaths[i].score, best)))
/*      */             continue;
/*  739 */           best = t.statePaths[i].score;
/*  740 */           bestPath = t.statePaths[i];
/*      */         }
/*      */       }
/*      */ 
/*  744 */       return bestPath;
/*      */     }
/*      */ 
/*      */     Cost getOptimalCouple(int u0, int u1)
/*      */     {
/*  762 */       Cost cost = new Cost();
/*      */ 
/*  764 */       int u1_p = this.clunitDB.getPrevUnit(u1);
/*      */ 
/*  767 */       if (u1_p == u0) {
/*  768 */         return cost;
/*      */       }
/*      */ 
/*  775 */       if ((u1_p == 65535) || (this.clunitDB.getPhone(u0) != this.clunitDB.getPhone(u1_p)))
/*      */       {
/*  778 */         cost.cost = (10 * getOptimalCoupleFrame(u0, u1));
/*  779 */         return cost;
/*      */       }
/*      */ 
/*  789 */       int u0_end = this.clunitDB.getEnd(u0) - this.clunitDB.getStart(u0);
/*  790 */       int u1_p_end = this.clunitDB.getEnd(u1_p) - this.clunitDB.getStart(u1_p);
/*  791 */       int u0_st = u0_end / 3;
/*  792 */       int u1_p_st = u1_p_end / 3;
/*      */       int fcount;
/*      */       int fcount;
/*  794 */       if (u0_end - u0_st < u1_p_end - u1_p_st) {
/*  795 */         fcount = u0_end - u0_st;
/*      */       }
/*      */       else
/*      */       {
/*  800 */         fcount = u1_p_end - u1_p_st;
/*      */       }
/*      */ 
/*  808 */       int best_u0 = u0_end;
/*  809 */       int best_u1_p = u1_p_end;
/*  810 */       int best_val = 2147483647;
/*      */ 
/*  812 */       for (int i = 0; i < fcount; ++i) {
/*  813 */         int a = this.clunitDB.getStart(u0) + u0_st + i;
/*  814 */         int b = this.clunitDB.getStart(u1_p) + u1_p_st + i;
/*  815 */         int dist = getFrameDistance(a, b, this.clunitDB.getJoinWeights(), this.clunitDB.getMcep().getSampleInfo().getNumberOfChannels()) + Math.abs(this.clunitDB.getSts().getFrameSize(a) - this.clunitDB.getSts().getFrameSize(b)) * this.clunitDB.getContinuityWeight();
/*      */ 
/*  822 */         if (dist < best_val) {
/*  823 */           best_val = dist;
/*  824 */           best_u0 = u0_st + i;
/*  825 */           best_u1_p = u1_p_st + i;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  831 */       cost.u0Move = (this.clunitDB.getStart(u0) + best_u0);
/*  832 */       cost.u1Move = (this.clunitDB.getStart(u1_p) + best_u1_p);
/*  833 */       cost.cost = (30000 + best_val);
/*  834 */       return cost;
/*      */     }
/*      */ 
/*      */     int getOptimalCoupleFrame(int u0, int u1)
/*      */     {
/*  849 */       if (this.clunitDB.getPrevUnit(u1) == u0)
/*  850 */         return 0;
/*      */       int a;
/*      */       int a;
/*  853 */       if (this.clunitDB.getNextUnit(u0) != 65535)
/*  854 */         a = this.clunitDB.getEnd(u0);
/*      */       else {
/*  856 */         a = this.clunitDB.getEnd(u0) - 1;
/*      */       }
/*  858 */       int b = this.clunitDB.getStart(u1);
/*      */ 
/*  860 */       return getFrameDistance(a, b, this.clunitDB.getJoinWeights(), this.clunitDB.getMcep().getSampleInfo().getNumberOfChannels()) + Math.abs(this.clunitDB.getSts().getFrameSize(a) - this.clunitDB.getSts().getFrameSize(b)) * this.clunitDB.getContinuityWeight();
/*      */     }
/*      */ 
/*      */     public int getFrameDistance(int a, int b, int[] joinWeights, int order)
/*      */     {
/*  880 */       if (ClusterUnitSelector.LOGGER.isLoggable(Level.FINE)) {
/*  881 */         ClusterUnitSelector.LOGGER.fine(" gfd  a " + a + " b " + b + " or " + order);
/*      */       }
/*      */ 
/*  884 */       short[] bv = this.clunitDB.getMcep().getSample(b).getFrameData();
/*  885 */       short[] av = this.clunitDB.getMcep().getSample(a).getFrameData();
/*      */ 
/*  887 */       int r = 0; for (int i = 0; i < order; ++i) {
/*  888 */         int diff = av[i] - bv[i];
/*  889 */         r += Math.abs(diff) * joinWeights[i] / 65536;
/*      */       }
/*  891 */       return r;
/*      */     }
/*      */   }
/*      */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.clunits.ClusterUnitSelector
 * JD-Core Version:    0.5.4
 */