package kos.allie;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class SoundService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        /*
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        SoundPool sounds = new SoundPool.Builder().setAudioAttributes(attributes).build();
        */



        /*
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int beginningAudioStrength = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (beginningAudioStrength == 0){
            beginningAudioStrength = 1;
        }
        int currentAudioStrength = beginningAudioStrength;
        long base = System.currentTimeMillis();
        int counter = beginningAudioStrength;
        boolean shouldContinue = true;
        // since we are
        while (shouldContinue) {

            long incOrDecr = System.currentTimeMillis() % 500;
            // < 25 means we are increasing the volume every 25/beginningAudioStrength milliseconds
            // >= 25 means we are decreasing the volume every 25/beginningAudioStrength milliseconds
            // increasing
            if (incOrDecr < 250) {
                // increases the volume every 25/x seconds
                if (incOrDecr / beginningAudioStrength > currentAudioStrength*10){
                    // increase the volume
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                    currentAudioStrength++;
                }
            }
            else {
                incOrDecr = incOrDecr - 250;
                //decreases the volume every 25/x seconds
                if (incOrDecr / beginningAudioStrength > currentAudioStrength*10){
                    // decrease the volume
                    currentAudioStrength--;
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
                }
            }

            /*
            long incOrDecr = System.currentTimeMillis() % 50;
            // < 25 means we are increasing the volume every 25/beginningAudioStrength milliseconds
            // >= 25 means we are decreasing the volume every 25/beginningAudioStrength milliseconds
            // increasing
            if (incOrDecr < 25) {
                // increases the volume every 25/x seconds
                if (incOrDecr / beginningAudioStrength > currentAudioStrength){
                    // increase the volume
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                    currentAudioStrength++;
                }
            }
            else {
                incOrDecr = incOrDecr - 25;
                //decreases the volume every 25/x seconds
                if (incOrDecr / beginningAudioStrength > currentAudioStrength){
                    // decrease the volume
                    currentAudioStrength--;
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
                }
            }

        }
        // sets the audio back to normal
        for (int i = (beginningAudioStrength - currentAudioStrength); i > 0; i--){
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
        }
        */
    }


}