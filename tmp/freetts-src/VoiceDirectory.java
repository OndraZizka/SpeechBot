/*    */ package com.sun.speech.freetts;
/*    */ 
/*    */ import java.util.Locale;
/*    */ 
/*    */ public abstract class VoiceDirectory
/*    */ {
/*    */   public abstract Voice[] getVoices();
/*    */ 
/*    */   public String toString()
/*    */   {
/* 54 */     String newline = System.getProperty("line.separator");
/* 55 */     Voice[] voices = getVoices();
/* 56 */     String s = "VoiceDirectory '" + super.getClass().getName() + "'" + newline;
/*    */ 
/* 59 */     for (int i = 0; i < voices.length; ++i) {
/* 60 */       s = s + newline + "Name: " + voices[i].getName() + newline + "\tDescription: " + voices[i].getDescription() + newline + "\tOrganization: " + voices[i].getOrganization() + newline + "\tDomain: " + voices[i].getDomain() + newline + "\tLocale: " + voices[i].getLocale().toString() + newline + "\tStyle: " + voices[i].getStyle() + newline + "\tGender: " + voices[i].getGender().toString() + newline + "\tAge: " + voices[i].getAge().toString() + newline + "\tPitch: " + voices[i].getPitch() + newline + "\tPitch Range: " + voices[i].getPitchRange() + newline + "\tPitch Shift: " + voices[i].getPitchShift() + newline + "\tRate: " + voices[i].getRate() + newline + "\tVolume: " + voices[i].getVolume() + newline + newline;
/*    */     }
/*    */ 
/* 75 */     return s;
/*    */   }
/*    */ 
/*    */   public static void main(String[] args)
/*    */   {
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.VoiceDirectory
 * JD-Core Version:    0.5.4
 */