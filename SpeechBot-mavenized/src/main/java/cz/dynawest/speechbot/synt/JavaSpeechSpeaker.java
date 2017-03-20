package cz.dynawest.speechbot.synt;

import com.sun.speech.freetts.jsapi.FreeTTSEngineCentral;
import java.util.Locale;
import javax.speech.EngineCreate;
import javax.speech.EngineList;
import javax.speech.synthesis.*;

/**
 *  Java Speech API test.
 *  @author Ondrej Zizka
 */
public class JavaSpeechSpeaker implements ISpeaker {

	protected Synthesizer synthesizer;

	public JavaSpeechSpeaker() {
		this.createSynthesizer();
	}

	private void createSynthesizer()
	{
		try 
		{
			SynthesizerModeDesc desc = new SynthesizerModeDesc(null, "time", Locale.US, Boolean.FALSE, null);

			FreeTTSEngineCentral central = new FreeTTSEngineCentral();
			EngineList list = central.createEngineList( desc );
			
			if( null == list) {
				System.err.println("Error creating engine list.");
				System.exit(1);
			}

			if( list.size() == 0 ) {
				System.err.println("No synth engines.");
				System.exit(1);
			}
			
			EngineCreate creator = (EngineCreate) list.get(0);
			this.synthesizer = ((Synthesizer) creator.createEngine());
			if( this.synthesizer == null ) {
				System.err.println("Cannot create synthesizer");
				System.exit(1);
			}
			
			this.synthesizer.allocate();
			this.synthesizer.resume();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void speak(String whatToSay) 
	{
		this.synthesizer.speakPlainText(whatToSay, null);
	}
	
}// class

