
package cz.dynawest.speechbot.synt;


import com.sun.speech.freetts.*;
import com.sun.speech.freetts.audio.*;
import java.util.Locale;
import java.util.Random;
import javax.sound.sampled.*;
import javax.speech.Central;
import javax.speech.EngineList;
import javax.speech.synthesis.SynthesizerModeDesc;


/**
 * FreeTTS / Mbrola speaker.
 */
public class MbrolaSpeaker implements ISpeaker {

    private boolean noisy = true;
    private Voice voice;
    private AudioPlayer voicePlayer;
	
	static {
		System.setProperty("com.sun.speech.freetts.audio.AudioPlayer.openFailDelayMs", "100");
		System.setProperty("com.sun.speech.freetts.audio.AudioPlayer.totalOpenFailDelayMs", "30000");
		//System.setProperty("freetts.voices","com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory"); // Only words for time demo app.
		System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory"); // ClassCastException
		//System.setProperty("freetts.voices", "de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory");

		//listAllVoices("general");
	}

    
    public MbrolaSpeaker() {

        //final String VOICE_NAME = "mbrola_us3";
				
        VoiceManager voiceManager = VoiceManager.getInstance();
 
		// List available voices.
		Voice[] voices = voiceManager.getVoices();
		if( 0 == voices.length ){
            System.err.println("  ERROR: No voices available.");
            System.exit(1);
        }
			
		System.out.println("  Voices available ("+voices.length+") :");
		for (int i = 0; i < voices.length; i++) {
			System.out.println("    " + voices[i].getName()	+ " (" + voices[i].getDomain() + " domain)");
		}
 		
        //this.voice = voiceManager.getVoice( VOICE_NAME );
		this.voice = voices[ new Random().nextInt(voices.length) ];
		System.out.println("  Randomly picked voice: "+this.voice.getName());
		
        if( this.voice == null ) {
            System.err.println("  ERROR: Voice not found.");
            System.exit(1);
        }

        this.voice.allocate();
        this.voicePlayer = new JavaClipAudioPlayer();
        this.voicePlayer.setAudioFormat(new AudioFormat(8000, 16, 1, false, true));
    }
    
		
    // This method is not called from anywhere yet.
    public void release() {
        this.voice.deallocate();
    }

		
		
		/**
		 * Says the string, stores into a wav file.
		 */
		public void speakToFile(String whatToSay, String filename) {
            AudioPlayer filePlayer = filePlayer = new SingleFileAudioPlayer( filename,  AudioFileFormat.Type.WAVE );
            filePlayer.setAudioFormat( new AudioFormat( 8000, 16, 1, false, true ));
            this.speak( whatToSay, filePlayer );
            filePlayer.close();
		}

		
		
		/**
		 * Says the string.
		 */
		@Override
		public void speak( String whatToSay ) {
				//this.speak( whatToSay, this.voicePlayer );
				this.voice.speak( whatToSay );
		}

		
		/**
		 * Says the string with given player.
		 */
    private void speak( String whatToSay, AudioPlayer player ) {
				AudioPlayer oldPlayer = this.voice.getAudioPlayer();
        this.voice.setAudioPlayer( player );
        this.voice.speak( whatToSay );
        this.voice.setAudioPlayer( oldPlayer );
    }

	
	
	
	/**
	 * List all voices.
	 */
	public static void listAllVoices(String modeName)
	{
		System.out.println();
		System.out.println("All " + modeName + " Mode JSAPI Synthesizers and Voices:");

		SynthesizerModeDesc required = new SynthesizerModeDesc(null, modeName, Locale.US, null, null);

		EngineList engineList = Central.availableSynthesizers(required);
		for (int i = 0; i < engineList.size(); ++i)
		{
			SynthesizerModeDesc desc = (SynthesizerModeDesc) engineList.get(i);
			System.out.println("    " + desc.getEngineName() + " (mode=" + desc.getModeName() + ", locale=" + desc.getLocale() + "):");

			javax.speech.synthesis.Voice[] voices = desc.getVoices();
			for (int j = 0; j < voices.length; ++j) {
				System.out.println("        " + voices[j].getName());
			}
		}
	}


}// class
