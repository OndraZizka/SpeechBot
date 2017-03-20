/*     */ package com.sun.speech.freetts.relp;
/*     */ 
/*     */ import com.sun.speech.freetts.ProcessException;
/*     */ import com.sun.speech.freetts.Utterance;
/*     */ import com.sun.speech.freetts.UtteranceProcessor;
/*     */ import com.sun.speech.freetts.Voice;
/*     */ import com.sun.speech.freetts.audio.AudioPlayer;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ import javax.sound.sampled.AudioFormat;
/*     */ 
/*     */ public class AudioOutput
/*     */   implements UtteranceProcessor
/*     */ {
/*  33 */   private static final Logger LOGGER = Logger.getLogger(AudioOutput.class.getName());
/*     */ 
/*  36 */   private static final AudioFormat AUDIO_8KHZ = new AudioFormat(8000.0F, 16, 1, true, true);
/*     */ 
/*  38 */   private static final AudioFormat AUDIO_16KHZ = new AudioFormat(16000.0F, 16, 1, true, true);
/*     */ 
/*     */   public void processUtterance(Utterance utterance)
/*     */     throws ProcessException
/*     */   {
/*  53 */     LPCResult lpcResult = (LPCResult)utterance.getObject("target_lpcres");
/*  54 */     SampleInfo sampleInfo = (SampleInfo)utterance.getObject("SampleInfo");
/*     */ 
/*  56 */     AudioPlayer audioPlayer = utterance.getVoice().getAudioPlayer();
/*     */ 
/*  58 */     audioPlayer.setAudioFormat(getAudioFormat(sampleInfo));
/*  59 */     audioPlayer.setVolume(utterance.getVoice().getVolume());
/*     */ 
/*  61 */     if (LOGGER.isLoggable(Level.FINE)) {
/*  62 */       LOGGER.fine("=== " + utterance.getString("input_text"));
/*     */     }
/*     */ 
/*  65 */     if (!lpcResult.playWave(audioPlayer, utterance))
/*  66 */       throw new ProcessException("Output Cancelled");
/*     */   }
/*     */ 
/*     */   private AudioFormat getAudioFormat(SampleInfo sampleInfo)
/*     */   {
/*  83 */     if (sampleInfo.getSampleRate() == 8000)
/*  84 */       return AUDIO_8KHZ;
/*  85 */     if (sampleInfo.getSampleRate() == 16000) {
/*  86 */       return AUDIO_16KHZ;
/*     */     }
/*  88 */     return new AudioFormat(sampleInfo.getSampleRate(), 16, 1, true, true);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 100 */     return "AudioOutput";
/*     */   }
/*     */ }

/* Location:           /home/ondra/work/BOTS/SpeechBot/workdir/freetts-1.2/lib/freetts.jar
 * Qualified Name:     com.sun.speech.freetts.relp.AudioOutput
 * JD-Core Version:    0.5.4
 */