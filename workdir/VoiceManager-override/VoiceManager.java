/*     */ package com.sun.speech.freetts;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.net.JarURLConnection;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URI;
/*     */ import java.net.URISyntaxException;
/*     */ import java.net.URL;
/*     */ import java.net.URLClassLoader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Iterator;
/*     */ import java.util.jar.Attributes;
/*     */ import java.util.jar.Attributes.Name;
/*     */ 






















/*     */ public class VoiceManager
/*     */ {
/*     */   private static final VoiceManager INSTANCE;
/*  48 */   private static final String PATH_SEPARATOR = System.getProperty("path.separator");
/*     */   private static final DynamicClassLoader classLoader;
/*     */ 
/*     */   public static VoiceManager getInstance()
/*     */   {
/*  66 */     return INSTANCE;
/*     */   }
/*     */ 



















































/*     */   public Voice[] getVoices()
/*     */   {
/* 109 */     UniqueVector voices = new UniqueVector();
/* 110 */     Collection voiceDirectories = getVoiceDirectories();
/* 111 */     Iterator iterator = voiceDirectories.iterator();
/* 112 */     while (iterator.hasNext()) {
/* 113 */       VoiceDirectory dir = (VoiceDirectory)iterator.next();
/* 114 */       voices.addArray(dir.getVoices());
/*     */     }
/*     */ 
/* 117 */     Voice[] voiceArray = new Voice[voices.size()];
/* 118 */     return (Voice[])voices.toArray(voiceArray);
/*     */   }
/*     */ 




/*     */   public String getVoiceInfo()
/*     */   {
/* 127 */     String infoString = "";
/* 128 */     Collection voiceDirectories = getVoiceDirectories();
/* 129 */     Iterator iterator = voiceDirectories.iterator();
/* 130 */     while (iterator.hasNext()) {
/* 131 */       VoiceDirectory dir = (VoiceDirectory)iterator.next();
/* 132 */       infoString = infoString + dir.toString();
/*     */     }
/* 134 */     return infoString;
/*     */   }
/*     */ 













/*     */   private Collection getVoiceDirectories()
/*     */   {
/*     */     try
/*     */     {
/* 154 */       String voiceClasses = System.getProperty("freetts.voices");
/* 155 */       if (voiceClasses != null) {
/* 156 */         return getVoiceDirectoryNamesFromProperty(voiceClasses);
/*     */       }
/*     */ 
/* 160 */       UniqueVector voiceDirectoryNames = getVoiceDirectoryNamesFromFiles();
/*     */ 
/* 163 */       UniqueVector pathURLs = getVoiceJarURLs();
/* 164 */       voiceDirectoryNames.addVector(getVoiceDirectoryNamesFromJarURLs(pathURLs));
/*     */ 
/* 170 */       URL[] voiceJarURLs = (URL[])pathURLs.toArray(new URL[pathURLs.size()]);
/*     */ 
/* 172 */       for (int i = 0; i < voiceJarURLs.length; ++i) {
/* 173 */         getDependencyURLs(voiceJarURLs[i], pathURLs);
/*     */       }
/*     */ 
/* 178 */       boolean noexpansion = Boolean.getBoolean("freetts.nocpexpansion");
/* 179 */       if (!noexpansion)
/*     */       {
/* 181 */         for (int i = 0; i < pathURLs.size(); ++i) {
/* 182 */           classLoader.addUniqueURL((URL)pathURLs.get(i));
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 187 */       UniqueVector voiceDirectories = new UniqueVector();
/* 188 */       for (int i = 0; i < voiceDirectoryNames.size(); ++i) {
/* 189 */         Class c = Class.forName((String)voiceDirectoryNames.get(i), true, classLoader);
/*     */ 
/* 191 */         voiceDirectories.add(c.newInstance());
/*     */       }
/*     */ 
/* 194 */       return voiceDirectories.elements();
/*     */     } catch (InstantiationException e) {
/* 196 */       throw new Error("Unable to load voice directory. " + e);
/*     */     } catch (ClassNotFoundException e) {
/* 198 */       throw new Error("Unable to load voice directory. " + e);
/*     */     } catch (IllegalAccessException e) {
/* 200 */       throw new Error("Unable to load voice directory. " + e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private Collection getVoiceDirectoryNamesFromProperty(String voiceClasses)
/*     */     throws InstantiationException, IllegalAccessException, ClassNotFoundException
/*     */   {
/* 213 */     String[] classnames = voiceClasses.split(",");
/*     */ 
/* 215 */     Collection directories = new ArrayList();
/*     */ 
/* 217 */     for (int i = 0; i < classnames.length; ++i) {
/* 218 */       Class c = classLoader.loadClass(classnames[i]);
/* 219 */       directories.add(c.newInstance());
/*     */     }
/*     */ 
/* 222 */     return directories;
/*     */   }
/*     */ 




/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/**/
/*     */   private void getDependencyURLs(URL url, UniqueVector dependencyURLs)
/*     */   {
							System.out.println("  getDependencyURLs(): "+url.toString());

							
/*     */     try
/*     */     {
/* 245 */       String urlDirName = getURLDirName(url);
/* 246 */       if (url.getProtocol().equals("jar"))
/*     */       {
/* 249 */         JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
/*     */ 

/* 251 */         Attributes attributes = jarConnection.getMainAttributes();
/* 252 */         String fullClassPath = attributes.getValue(Attributes.Name.CLASS_PATH);
/*     */ 
/* 254 */         if ((fullClassPath == null) || (fullClassPath.equals(""))) {
/* 255 */           return;
/*     */         }
/*     */ 
/* 259 */         String[] classPath = fullClassPath.split("\\s+");

/*     */ 
/* 261 */         for (int i = 0; i < classPath.length; ++i)  {
										System.out.println("    getDependencyURLs(): Examining classpath: " + classPath[i]);
/*     */           URL classPathURL;
/*     */           try {
/* 263 */             if (classPath[i].endsWith("/")) {
/* 264 */               classPathURL = new URL("file:" + urlDirName + classPath[i]);
/*     */             }
/*     */             else
/* 267 */               classPathURL = new URL("jar", "", "file:" + urlDirName + classPath[i] + "!/");

	/* 281 */           if (!dependencyURLs.contains(classPathURL)) {
	/* 282 */             dependencyURLs.add(classPathURL);
	/* 283 */             getDependencyURLs(classPathURL, dependencyURLs);
  /*     */           }
/*     */           }
/*     */           catch (MalformedURLException e)
/*     */           {
/* 271 */             System.err.println("Warning: unable to resolve dependency " + classPath[i] + " referenced by " + url);
/*     */           }
/*     */         }
/*     */       }
/*     */     } catch (IOException e) {
/* 288 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   private UniqueVector getVoiceDirectoryNamesFromFiles()
/*     */   {
/*     */     try
/*     */     {
/* 300 */       UniqueVector voiceDirectoryNames = new UniqueVector();
/*     */ 
/* 303 */       InputStream is = super.getClass().getResourceAsStream("internal_voices.txt");
/*     */ 
/* 305 */       if (is != null) {
/* 306 */         voiceDirectoryNames.addVector(getVoiceDirectoryNamesFromInputStream(is));
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 312 */         voiceDirectoryNames.addVector(getVoiceDirectoryNamesFromFile(getBaseDirectory() + "voices.txt"));
/*     */       }
/*     */       catch (FileNotFoundException e)
/*     */       {
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/*     */       }
/*     */ 
/* 322 */       String voicesFile = System.getProperty("freetts.voicesfile");
/* 323 */       if (voicesFile != null) {
/* 324 */         voiceDirectoryNames.addVector(getVoiceDirectoryNamesFromFile(voicesFile));
/*     */       }
/*     */ 
/* 328 */       return voiceDirectoryNames;
/*     */     } catch (IOException e) {
/* 330 */       throw new Error("Error reading voices files. " + e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private UniqueVector getVoiceDirectoryNamesFromJarURLs(UniqueVector urls)
/*     */   {
/*     */     try
/*     */     {
/* 347 */       UniqueVector voiceDirectoryNames = new UniqueVector();
/* 348 */       for (int i = 0; i < urls.size(); ++i) {
/* 349 */         JarURLConnection jarConnection = (JarURLConnection)((URL)urls.get(i)).openConnection();
/*     */ 
/* 351 */         Attributes attributes = jarConnection.getMainAttributes();
/* 352 */         String mainClass = attributes.getValue(Attributes.Name.MAIN_CLASS);
/*     */ 
/* 354 */         if ((mainClass == null) || (mainClass.trim().equals(""))) {
/* 355 */           throw new Error("No Main-Class found in jar " + (URL)urls.get(i));
/*     */         }
/*     */ 
/* 359 */         voiceDirectoryNames.add(mainClass);
/*     */       }
/* 361 */       return voiceDirectoryNames;
/*     */     } catch (IOException e) {
/* 363 */       throw new Error("Error reading jarfile manifests. ");
/*     */     }
/*     */   }
/*     */ 
/*     */   private UniqueVector getVoiceJarURLs()
/*     */   {
/* 376 */     UniqueVector voiceJarURLs = new UniqueVector();
/*     */     try
/*     */     {
/* 380 */       String baseDirectory = getBaseDirectory();
/* 381 */       if (!baseDirectory.equals("")) {
/* 382 */         voiceJarURLs.addVector(getVoiceJarURLsFromDir(baseDirectory));
/*     */       }
/*     */     }
/*     */     catch (FileNotFoundException e)
/*     */     {
/*     */     }
/*     */ 
/* 389 */     String voicesPath = System.getProperty("freetts.voicespath", "");
/* 390 */     if (!voicesPath.equals("")) {
/* 391 */       String[] dirNames = voicesPath.split(PATH_SEPARATOR);
/* 392 */       for (int i = 0; i < dirNames.length; ++i) {
/*     */         try {
/* 394 */           voiceJarURLs.addVector(getVoiceJarURLsFromDir(dirNames[i]));
/*     */         } catch (FileNotFoundException e) {
/* 396 */           throw new Error("Error loading jars from voicespath " + dirNames[i] + ". ");
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 402 */     return voiceJarURLs;
/*     */   }
/*     */ 
/*     */   private UniqueVector getVoiceJarURLsFromDir(String dirName)
/*     */     throws FileNotFoundException
/*     */   {
/*     */     try
/*     */     {
/* 414 */       UniqueVector voiceJarURLs = new UniqueVector();
/* 415 */       File dir = new File(new URI("file://" + dirName));
/* 416 */       if (!dir.isDirectory()) {
/* 417 */         throw new FileNotFoundException("File is not a directory: " + dirName);
/*     */       }
/*     */ 
/* 420 */       File[] files = dir.listFiles();
/* 421 */       for (int i = 0; i < files.length; ++i) {
/* 422 */         File file = files[i];
/* 423 */         if ((!file.isFile()) || (file.isHidden()) || (!file.getName().endsWith(".jar")))
/*     */           continue;
/* 425 */         URL jarURL = file.toURI().toURL();
/* 426 */         jarURL = new URL("jar", "", "file:" + jarURL.getPath() + "!/");
/*     */ 
/* 428 */         JarURLConnection jarConnection = (JarURLConnection)jarURL.openConnection();
/*     */ 
/* 433 */         Attributes attributes = jarConnection.getMainAttributes();
/* 434 */         if (attributes != null) {
/* 435 */           String isVoice = attributes.getValue("FreeTTSVoiceDefinition");
/*     */ 
/* 437 */           if ((isVoice != null) && (isVoice.trim().equals("true"))) {
/* 438 */             voiceJarURLs.add(jarURL);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 443 */       return voiceJarURLs;
/*     */     } catch (URISyntaxException e) {
/* 445 */       throw new Error("Error reading directory name '" + dirName + "'.");
/*     */     } catch (MalformedURLException e) {
/* 447 */       throw new Error("Error reading jars from directory " + dirName + ". ");
/*     */     }
/*     */     catch (IOException e) {
/* 450 */       throw new Error("Error reading jars from directory " + dirName + ". ");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 463 */     String names = "";
/* 464 */     Voice[] voices = getVoices();
/* 465 */     for (int i = 0; i < voices.length; ++i) {
/* 466 */       if (i == voices.length - 1) {
/* 467 */         if (i == 0)
/* 468 */           names = voices[i].getName();
/*     */         else
/* 470 */           names = names + "or " + voices[i].getName();
/*     */       }
/*     */       else {
/* 473 */         names = names + voices[i].getName() + " ";
/*     */       }
/*     */     }
/* 476 */     return names;
/*     */   }
/*     */ 
/*     */   public boolean contains(String voiceName)
/*     */   {
/* 489 */     return getVoice(voiceName) != null;
/*     */   }
/*     */ 
/*     */   public Voice getVoice(String voiceName)
/*     */   {
/* 502 */     Voice[] voices = getVoices();
/* 503 */     for (int i = 0; i < voices.length; ++i) {
/* 504 */       if (voices[i].getName().equals(voiceName)) {
/* 505 */         return voices[i];
/*     */       }
/*     */     }
/* 508 */     return null;
/*     */   }
/*     */ 
/*     */   private String getBaseDirectory()
/*     */   {
/* 519 */     String name = super.getClass().getName();
/* 520 */     int lastdot = name.lastIndexOf('.');
/* 521 */     if (lastdot != -1) {
/* 522 */       name = name.substring(lastdot + 1);
/*     */     }
/*     */ 
/* 525 */     URL url = super.getClass().getResource(name + ".class");
/* 526 */     return getURLDirName(url);
/*     */   }
/*     */ 
/*     */   private String getURLDirName(URL url)
/*     */   {
/* 537 */     String urlFileName = url.getPath();
/* 538 */     int i = urlFileName.lastIndexOf('!');
/* 539 */     if (i == -1) {
/* 540 */       i = urlFileName.length();
/*     */     }
/* 542 */     int dir = urlFileName.lastIndexOf("/", i);
/* 543 */     if (!urlFileName.startsWith("file:")) {
/* 544 */       return "";
/*     */     }
/* 546 */     return urlFileName.substring(5, dir) + "/";
/*     */   }
/*     */ 
/*     */   private UniqueVector getVoiceDirectoryNamesFromFile(String fileName)
/*     */     throws FileNotFoundException, IOException
/*     */   {
/* 563 */     InputStream is = new FileInputStream(fileName);
/* 564 */     if (is == null) {
/* 565 */       throw new IOException();
/*     */     }
/* 567 */     return getVoiceDirectoryNamesFromInputStream(is);
/*     */   }
/*     */ 
/*     */   private UniqueVector getVoiceDirectoryNamesFromInputStream(InputStream is)
/*     */     throws IOException
/*     */   {
/* 584 */     UniqueVector names = new UniqueVector();
/* 585 */     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
/*     */     while (true) {
/* 587 */       String line = reader.readLine();
/* 588 */       if (line == null) {
/*     */         break;
/*     */       }
/* 591 */       line = line.trim();
/* 592 */       if ((!line.startsWith("#")) && (!line.equals(""))) {
/* 593 */         names.add(line);
/*     */       }
/*     */     }
/* 596 */     return names;
/*     */   }
/*     */ 
/*     */   public static URLClassLoader getVoiceClassLoader()
/*     */   {
/* 607 */     return classLoader;
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  49 */     INSTANCE = new VoiceManager();
/*  50 */     ClassLoader parent = VoiceManager.class.getClassLoader();
/*  51 */     classLoader = new DynamicClassLoader(new URL[0], parent);
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.VoiceManager
 * JD-Core Version:    0.5.4
 */