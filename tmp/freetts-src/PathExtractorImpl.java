/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ public class PathExtractorImpl
/*     */   implements PathExtractor
/*     */ {
/*  41 */   private static final Logger LOGGER = Logger.getLogger(PathExtractorImpl.class.getName());
/*     */   public static final String INTERPRET_PATHS_PROPERTY = "com.sun.speech.freetts.interpretCartPaths";
/*     */   public static final String LAZY_COMPILE_PROPERTY = "com.sun.speech.freetts.lazyCartCompile";
/*  58 */   private static final boolean INTERPRET_PATHS = Utilities.getProperty("com.sun.speech.freetts.interpretCartPaths", "false").equals("true");
/*     */ 
/*  60 */   private static final boolean LAZY_COMPILE = Utilities.getProperty("com.sun.speech.freetts.lazyCartCompile", "true").equals("true");
/*     */   private String pathAndFeature;
/*     */   private String path;
/*     */   private String feature;
/*     */   private Object[] compiledPath;
/*  67 */   private boolean wantFeature = false;
/*     */ 
/*     */   public PathExtractorImpl(String pathAndFeature, boolean wantFeature)
/*     */   {
/*  73 */     this.pathAndFeature = pathAndFeature;
/*  74 */     if (INTERPRET_PATHS) {
/*  75 */       this.path = pathAndFeature;
/*  76 */       return;
/*     */     }
/*     */ 
/*  79 */     if (wantFeature) {
/*  80 */       int lastDot = pathAndFeature.lastIndexOf(".");
/*     */ 
/*  83 */       if (lastDot == -1) {
/*  84 */         this.feature = pathAndFeature;
/*  85 */         this.path = null;
/*     */       } else {
/*  87 */         this.feature = pathAndFeature.substring(lastDot + 1);
/*  88 */         this.path = pathAndFeature.substring(0, lastDot);
/*     */       }
/*  90 */       this.wantFeature = wantFeature;
/*     */     } else {
/*  92 */       this.path = pathAndFeature;
/*     */     }
/*     */ 
/*  95 */     if (!LAZY_COMPILE)
/*  96 */       this.compiledPath = compile(this.path);
/*     */   }
/*     */ 
/*     */   public Item findItem(Item item)
/*     */   {
/* 107 */     if (INTERPRET_PATHS) {
/* 108 */       return item.findItem(this.path);
/*     */     }
/*     */ 
/* 111 */     if (this.compiledPath == null) {
/* 112 */       this.compiledPath = compile(this.path);
/*     */     }
/*     */ 
/* 115 */     Item pitem = item;
/*     */ 
/* 117 */     for (int i = 0; (pitem != null) && (i < this.compiledPath.length); ) {
/* 118 */       OpEnum op = (OpEnum)this.compiledPath[(i++)];
/* 119 */       if (op == OpEnum.NEXT) {
/* 120 */         pitem = pitem.getNext();
/* 121 */       } else if (op == OpEnum.PREV) {
/* 122 */         pitem = pitem.getPrevious();
/* 123 */       } else if (op == OpEnum.NEXT_NEXT) {
/* 124 */         pitem = pitem.getNext();
/* 125 */         if (pitem != null)
/* 126 */           pitem = pitem.getNext();
/*     */       }
/* 128 */       else if (op == OpEnum.PREV_PREV) {
/* 129 */         pitem = pitem.getPrevious();
/* 130 */         if (pitem != null)
/* 131 */           pitem = pitem.getPrevious();
/*     */       }
/* 133 */       else if (op == OpEnum.PARENT) {
/* 134 */         pitem = pitem.getParent();
/* 135 */       } else if (op == OpEnum.DAUGHTER) {
/* 136 */         pitem = pitem.getDaughter();
/* 137 */       } else if (op == OpEnum.LAST_DAUGHTER) {
/* 138 */         pitem = pitem.getLastDaughter();
/* 139 */       } else if (op == OpEnum.RELATION) {
/* 140 */         String relationName = (String)this.compiledPath[(i++)];
/* 141 */         pitem = pitem.getSharedContents().getItemRelation(relationName);
/*     */       } else {
/* 143 */         System.out.println("findItem: bad feature " + op + " in " + this.path);
/*     */       }
/*     */     }
/*     */ 
/* 147 */     return pitem;
/*     */   }
/*     */ 
/*     */   public Object findFeature(Item item)
/*     */   {
/* 159 */     if (INTERPRET_PATHS) {
/* 160 */       return item.findFeature(this.path);
/*     */     }
/*     */ 
/* 163 */     Item pitem = findItem(item);
/* 164 */     Object results = null;
/* 165 */     if (pitem != null) {
/* 166 */       if (LOGGER.isLoggable(Level.FINER)) {
/* 167 */         LOGGER.finer("findFeature: Item [" + pitem + "], feature '" + this.feature + "'");
/*     */       }
/*     */ 
/* 171 */       FeatureProcessor fp = pitem.getOwnerRelation().getUtterance().getVoice().getFeatureProcessor(this.feature);
/*     */ 
/* 175 */       if (fp != null) {
/* 176 */         if (LOGGER.isLoggable(Level.FINER)) {
/* 177 */           LOGGER.finer("findFeature: There is a feature processor for '" + this.feature + "'");
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 182 */           results = fp.process(pitem);
/*     */         } catch (ProcessException pe) {
/* 184 */           LOGGER.severe("trouble while processing " + fp);
/* 185 */           throw new Error(pe);
/*     */         }
/*     */       } else {
/* 188 */         results = pitem.getFeatures().getObject(this.feature);
/*     */       }
/*     */     }
/*     */ 
/* 192 */     results = (results == null) ? "0" : results;
/* 193 */     if (LOGGER.isLoggable(Level.FINER)) {
/* 194 */       LOGGER.finer("findFeature: ...results = '" + results + "'");
/*     */     }
/* 196 */     return results;
/*     */   }
/*     */ 
/*     */   private Object[] compile(String path)
/*     */   {
/* 207 */     List list = new ArrayList();
/*     */ 
/* 209 */     if (path == null) {
/* 210 */       return list.toArray();
/*     */     }
/*     */ 
/* 213 */     StringTokenizer tok = new StringTokenizer(path, ":.");
/*     */ 
/* 215 */     while (tok.hasMoreTokens()) {
/* 216 */       String token = tok.nextToken();
/* 217 */       OpEnum op = OpEnum.getInstance(token);
/* 218 */       if (op == null) {
/* 219 */         throw new Error("Bad path compiled " + path);
/*     */       }
/*     */ 
/* 222 */       list.add(op);
/*     */ 
/* 224 */       if (op == OpEnum.RELATION) {
/* 225 */         list.add(tok.nextToken());
/*     */       }
/*     */     }
/* 228 */     return list.toArray();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 234 */     return this.pathAndFeature;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.PathExtractorImpl
 * JD-Core Version:    0.5.4
 */