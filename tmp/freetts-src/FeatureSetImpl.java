/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import com.sun.speech.freetts.util.Utilities;
/*     */ import java.io.PrintWriter;
/*     */ import java.text.DecimalFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class FeatureSetImpl
/*     */   implements FeatureSet
/*     */ {
/*     */   private final Map featureMap;
/*     */   static DecimalFormat formatter;
/*     */ 
/*     */   public FeatureSetImpl()
/*     */   {
/*  35 */     this.featureMap = new LinkedHashMap();
/*     */   }
/*     */ 
/*     */   public boolean isPresent(String name)
/*     */   {
/*  46 */     return this.featureMap.containsKey(name);
/*     */   }
/*     */ 
/*     */   public void remove(String name)
/*     */   {
/*  56 */     this.featureMap.remove(name);
/*     */   }
/*     */ 
/*     */   public String getString(String name)
/*     */   {
/*  71 */     return (String)getObject(name);
/*     */   }
/*     */ 
/*     */   public int getInt(String name)
/*     */   {
/*  85 */     return ((Integer)getObject(name)).intValue();
/*     */   }
/*     */ 
/*     */   public float getFloat(String name)
/*     */   {
/* 100 */     return ((Float)getObject(name)).floatValue();
/*     */   }
/*     */ 
/*     */   public Object getObject(String name)
/*     */   {
/* 112 */     return this.featureMap.get(name);
/*     */   }
/*     */ 
/*     */   public void setInt(String name, int value)
/*     */   {
/* 122 */     setObject(name, new Integer(value));
/*     */   }
/*     */ 
/*     */   public void setFloat(String name, float value)
/*     */   {
/* 132 */     setObject(name, new Float(value));
/*     */   }
/*     */ 
/*     */   public void setString(String name, String value)
/*     */   {
/* 142 */     setObject(name, value);
/*     */   }
/*     */ 
/*     */   public void setObject(String name, Object value)
/*     */   {
/* 152 */     this.featureMap.put(name, value);
/*     */   }
/*     */ 
/*     */   public void dump(PrintWriter output, int pad, String title)
/*     */   {
/* 164 */     dump(output, pad, title, false);
/*     */   }
/*     */ 
/*     */   public void dump(PrintWriter output, int pad, String title, boolean showName)
/*     */   {
/* 177 */     List keys = new ArrayList(this.featureMap.keySet());
/*     */ 
/* 179 */     if (formatter == null) {
/* 180 */       formatter = new DecimalFormat("########0.000000");
/*     */     }
/*     */ 
/* 183 */     Collections.reverse(keys);
/*     */ 
/* 185 */     Utilities.dump(output, pad, title);
/* 186 */     for (Iterator i = keys.iterator(); i.hasNext(); ) {
/* 187 */       String key = (String)i.next();
/*     */ 
/* 189 */       if ((!showName) && (key.equals("name"))) {
/*     */         continue;
/*     */       }
/*     */ 
/* 193 */       Object value = getObject(key);
/* 194 */       if (value instanceof Dumpable) {
/* 195 */         Dumpable d = (Dumpable)value;
/* 196 */         d.dump(output, pad + 4, key);
/*     */       }
/* 198 */       else if (value instanceof Float) {
/* 199 */         Float fval = (Float)value;
/* 200 */         Utilities.dump(output, pad + 4, key + "=" + formatter.format(fval.floatValue()));
/*     */       }
/*     */       else {
/* 203 */         Utilities.dump(output, pad + 4, key + "=" + value);
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.FeatureSetImpl
 * JD-Core Version:    0.5.4
 */