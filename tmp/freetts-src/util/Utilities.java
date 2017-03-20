/*     */ package com.sun.speech.freetts.util;
/*     */ 
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.net.URL;
/*     */ import java.nio.ByteBuffer;
/*     */ 
/*     */ public class Utilities
/*     */ {
/*     */   public static String pad(int padding)
/*     */   {
/*  40 */     if (padding > 0) {
/*  41 */       StringBuffer sb = new StringBuffer(padding);
/*  42 */       for (int i = 0; i < padding; ++i) {
/*  43 */         sb.append(' ');
/*     */       }
/*  45 */       return sb.toString();
/*     */     }
/*  47 */     return "";
/*     */   }
/*     */ 
/*     */   public static String pad(String string, int minLength)
/*     */   {
/*  62 */     String result = string;
/*  63 */     int pad = minLength - string.length();
/*  64 */     if (pad > 0)
/*  65 */       result = string + pad(minLength - string.length());
/*  66 */     else if (pad < 0) {
/*  67 */       result = string.substring(0, minLength);
/*     */     }
/*  69 */     return result;
/*     */   }
/*     */ 
/*     */   public static String deleteChar(String fromString, char charToDelete)
/*     */   {
/*  81 */     StringBuffer buffer = new StringBuffer(fromString.length());
/*  82 */     for (int i = 0; i < fromString.length(); ++i) {
/*  83 */       if (fromString.charAt(i) != charToDelete) {
/*  84 */         buffer.append(fromString.charAt(i));
/*     */       }
/*     */     }
/*  87 */     return new String(buffer);
/*     */   }
/*     */ 
/*     */   public static void dump(PrintWriter pw, int padding, String string)
/*     */   {
/*  99 */     pw.print(pad(padding));
/* 100 */     pw.println(string);
/*     */   }
/*     */ 
/*     */   public static InputStream getInputStream(URL url)
/*     */     throws IOException
/*     */   {
/* 115 */     if (url.getProtocol().equals("file")) {
/* 116 */       return new FileInputStream(url.getFile());
/*     */     }
/* 118 */     return url.openStream();
/*     */   }
/*     */ 
/*     */   public static void outString(DataOutputStream dos, String s)
/*     */     throws IOException
/*     */   {
/* 132 */     dos.writeShort((short)s.length());
/* 133 */     for (int i = 0; i < s.length(); ++i)
/* 134 */       dos.writeChar(s.charAt(i));
/*     */   }
/*     */ 
/*     */   public static String getString(DataInputStream dis)
/*     */     throws IOException
/*     */   {
/* 148 */     int size = dis.readShort();
/* 149 */     char[] charBuffer = new char[size];
/* 150 */     for (int i = 0; i < size; ++i) {
/* 151 */       charBuffer[i] = dis.readChar();
/*     */     }
/* 153 */     return new String(charBuffer, 0, size);
/*     */   }
/*     */ 
/*     */   public static String getString(ByteBuffer bb)
/*     */     throws IOException
/*     */   {
/* 166 */     int size = bb.getShort();
/* 167 */     char[] charBuffer = new char[size];
/* 168 */     for (int i = 0; i < size; ++i) {
/* 169 */       charBuffer[i] = bb.getChar();
/*     */     }
/* 171 */     return new String(charBuffer, 0, size);
/*     */   }
/*     */ 
/*     */   public static String getProperty(String name, String defaultValue)
/*     */   {
/*     */     String value;
/*     */     String value;
/*     */     try
/*     */     {
/* 190 */       value = System.getProperty(name, defaultValue);
/*     */     } catch (SecurityException se) {
/* 192 */       value = defaultValue;
/*     */     }
/* 194 */     return value;
/*     */   }
/*     */ 
/*     */   public static boolean getBoolean(String name)
/*     */   {
/*     */     boolean value;
/*     */     boolean value;
/*     */     try
/*     */     {
/* 210 */       value = Boolean.getBoolean(name);
/*     */     } catch (SecurityException se) {
/* 212 */       value = false;
/*     */     }
/* 214 */     return value;
/*     */   }
/*     */ 
/*     */   public static Long getLong(String name, long defaultValue)
/*     */   {
/*     */     Long value;
/*     */     Long value;
/*     */     try
/*     */     {
/* 232 */       value = Long.getLong(name, defaultValue);
/*     */     } catch (SecurityException se) {
/* 234 */       value = new Long(defaultValue);
/*     */     }
/* 236 */     return value;
/*     */   }
/*     */ 
/*     */   public static Integer getInteger(String name, int defaultValue)
/*     */   {
/*     */     Integer value;
/*     */     Integer value;
/*     */     try
/*     */     {
/* 254 */       value = Integer.getInteger(name, defaultValue);
/*     */     } catch (SecurityException se) {
/* 256 */       value = new Integer(defaultValue);
/*     */     }
/* 258 */     return value;
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.util.Utilities
 * JD-Core Version:    0.5.4
 */