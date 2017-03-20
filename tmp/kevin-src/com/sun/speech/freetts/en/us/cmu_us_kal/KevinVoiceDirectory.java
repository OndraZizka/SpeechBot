/*    */ package com.sun.speech.freetts.en.us.cmu_us_kal;
/*    */ 
/*    */ import com.sun.speech.freetts.Age;
/*    */ import com.sun.speech.freetts.Gender;
/*    */ import com.sun.speech.freetts.Voice;
/*    */ import com.sun.speech.freetts.VoiceDirectory;
/*    */ import com.sun.speech.freetts.en.us.CMUDiphoneVoice;
/*    */ import com.sun.speech.freetts.en.us.CMULexicon;
/*    */ import java.io.PrintStream;
/*    */ import java.util.Locale;
/*    */ 
/*    */ public class KevinVoiceDirectory extends VoiceDirectory
/*    */ {
/*    */   public Voice[] getVoices()
/*    */   {
/* 24 */     CMULexicon lexicon = new CMULexicon("cmulex");
/* 25 */     Voice kevin = new CMUDiphoneVoice("kevin", Gender.MALE, Age.YOUNGER_ADULT, "default 8-bit diphone voice", Locale.US, "general", "cmu", lexicon, super.getClass().getResource("cmu_us_kal.bin"));
/*    */ 
/* 29 */     Voice kevin16 = new CMUDiphoneVoice("kevin16", Gender.MALE, Age.YOUNGER_ADULT, "default 16-bit diphone voice", Locale.US, "general", "cmu", lexicon, super.getClass().getResource("cmu_us_kal16.bin"));
/*    */ 
/* 34 */     Voice[] voices = { kevin, kevin16 };
/* 35 */     return voices;
/*    */   }
/*    */ 
/*    */   public static void main(String[] args)
/*    */   {
/* 42 */     System.out.println(new KevinVoiceDirectory().toString());
/*    */   }
/*    */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/SpeechBot-mavenized/target/lib/cmu_us_kal-1.2.jar
 * Qualified Name:     com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory
 * JD-Core Version:    0.5.4
 */