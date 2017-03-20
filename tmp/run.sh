
#java -cp cmu_time_awb.jar:cmutimelex.jar:en_us.jar:freetts.jar:freetts-jsapi10.jar:jsapi.jar:WebStartClock.jar -Dfreetts.voices=com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory JSAPIClock

java -cp `ls *.jar | awk '{ORS=":";print}'` -Dfreetts.voices=com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory JSAPIClock