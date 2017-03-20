/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.net.URL;
/*     */ import java.net.URLClassLoader;
/*     */ import java.util.HashSet;
/*     */ 
/*     */ class DynamicClassLoader extends URLClassLoader
/*     */ {
/*     */   private HashSet classPath;
/*     */ 
/*     */   public DynamicClassLoader(URL[] urls, ClassLoader parent)
/*     */   {
/* 640 */     super(urls, parent);
/* 641 */     this.classPath = new HashSet(urls.length);
/* 642 */     for (int i = 0; i < urls.length; ++i)
/* 643 */       this.classPath.add(urls[i]);
/*     */   }
/*     */ 
/*     */   public synchronized void addUniqueURL(URL url)
/*     */   {
/* 655 */     String name = url.toString();
/* 656 */     if ((!this.classPath.contains(url)) && (name.indexOf("freetts.jar") < 0)) {
/* 657 */       super.addURL(url);
/* 658 */       this.classPath.add(url);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Class loadClass(String name)
/*     */     throws ClassNotFoundException
/*     */   {
/* 667 */     Class loadedClass = findLoadedClass(name);
/* 668 */     if (loadedClass == null) {
/*     */       try {
/* 670 */         loadedClass = findClass(name);
/*     */       }
/*     */       catch (ClassNotFoundException e)
/*     */       {
/*     */       }
/* 675 */       if (loadedClass == null) {
/* 676 */         loadedClass = super.loadClass(name);
/*     */       }
/*     */     }
/* 679 */     return loadedClass;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.DynamicClassLoader
 * JD-Core Version:    0.5.4
 */