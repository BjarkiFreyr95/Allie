package kos.allie;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.provider.MediaStore;
import android.os.Build;
import android.content.ContentUris;
import android.support.v4.content.ContextCompat;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import android.content.Context;
import android.media.AudioManager;
import android.widget.Button;
import android.content.Intent;
import android.media.MediaPlayer;
import android.app.Activity;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.widget.TextView;


import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    int sinCounter;
    boolean isPlaying;
    boolean isChangedOne;
    boolean isChangedTwo;
    boolean isChangedThree;
    boolean isChangedFour;
    boolean isRunning;
    boolean shouldContinue;
    boolean atLeastOneSong;
    int currentSongPlayingIndex;
    int numberOfSongs;
    List<Uri> mptList;
    List<String> songNames;
    MediaPlayer ring;

    File allMusic[];
    float audioStrength[];


    Thread thread;
    int seconds;
    public static final String PREFS_NAME = "MyPrefsFile";
    String lastDayUsed;
    String fileName = "/activity.txt";
    private static final int READ_REQUEST_CODE = 42;
    File directoryInUse = null;
    boolean shouldChangeDirectory = false;
    boolean isStarting = false;
    String folderPath = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permisson", "Permission is granted");
        }
        else {
            Log.d("Permission", "asking for permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 9998);
        }
        sinCounter = 0;
        isStarting = true;
        audioStrength = new float[25];
        // float array for audiostrength in left and right ear.
        for (int i = 0; i < 25; i++) {
            String stringTemp = (float) (Math.sin(Math.toRadians(((float) i / 25) * 360)) * 0.5 + 0.5) + "0000";
            stringTemp = stringTemp.substring(0, 4);
            if (Float.parseFloat(stringTemp) <= 1) {
                audioStrength[i] = Float.parseFloat(stringTemp);
            }
            else {
                audioStrength[i] = 0;
            }
            //audioStrength[i] = (float) (Math.sin(Math.toRadians(((float) i / 25) * 360)) * 0.5 + 0.5);
        }
        // to see how audioStrength is stored.
        for (int i = 0; i < 25; i++) {
            if (i != 0) {
                Log.d("arraystuff", "" + audioStrength[i] + "  " + (audioStrength[25 - i]));
            }
            else {
                Log.d("arraystuff", "" + audioStrength[i] + "  " + audioStrength[i]);
            }

        }

        Log.d("onCreate", "inside onCreate");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //gets all prefs (used to store Time and last day it was used)
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,0);
        int lastSeconds = pref.getInt("Time", -1); //
        lastDayUsed = pref.getString("Day", "");
        seconds = 0;
        if (lastSeconds > -1) {
            seconds = lastSeconds - 1;
            addSecond();
        }

        // get todays date in (day, month, year).
        String tempToday = new SimpleDateFormat("dd-MMM-yyyy").format(java.util.Calendar.getInstance().getTime());

        Log.d("DateDifference", "Date difference: " + lastDayUsed + " and " + tempToday);


        if (!lastDayUsed.equals(tempToday)) {
            Log.d("DateDifference", "Last date was " + lastDayUsed + " but current is " + tempToday);
            saveToTextFile(lastDayUsed, Integer.toString(lastSeconds));
            // need to call a function to update whatever textfile we are using and then reset Time Today to 00:00:00
            SharedPreferences theDate = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = theDate.edit();
            editor.putInt("Time", 0);
            editor.putString("Day", tempToday);
            editor.commit();

            lastDayUsed = tempToday;
            seconds = -1;
            addSecond();
        }
        else {
            Log.d("ehv","Did not enter lastDayUsed");
        }
        shouldChangeDirectory = true;

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart", "inside onStart");

        SharedPreferences pref = getSharedPreferences(PREFS_NAME,0);
        folderPath = pref.getString("Path", "");
        Log.d("folderPath is", folderPath);
        //ring = MediaPlayer.create(MainActivity.this, R.raw.avisong);


        //
        if (folderPath == ""){
            directoryInUse = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            Log.d("musicDir location", directoryInUse.getAbsolutePath());
            changeDirectory(directoryInUse);
            Log.d("directory", "directory is in downloads");
            shouldChangeDirectory = false;

        }
        else if (shouldChangeDirectory == true) {

            changeDirectory(directoryInUse = new File(folderPath));
            shouldChangeDirectory = false;
        }
        if (isStarting) {
            isChangedOne = false;
            isChangedTwo = false;
            isChangedThree = false;
            isChangedFour = true;
            isPlaying = false;
            isStarting = false;
        }
    }




    public void saveToTextFile(String day, String StrSeconds) {
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AllieData";
        try
        {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File (fullPath, "usageData.txt");
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file, true));

            osw.write(day + " Seconds: " + StrSeconds + "\n");
            osw.flush();
            osw.close();
            Log.d("asdf","written to text file");
        }
        catch (Exception e)
        {
            Log.e("saveToExternalStorage()", e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Time", seconds);
        editor.commit();
        Log.d("onStop", "inside onStop");
        Log.d("asdf", "seconds are or are supposed to be " + seconds);
    }


    public void pFileSearch() {
        Log.d("asdf", "inside performFileSearch");




        if (ring != null){
            if (isChangedFour) {
                isChangedFour = !isChangedFour;
                try {
                    Log.d("thread", "joining thread");
                    thread.join();
                }
                catch (Exception ex) { }

                isChangedFour = !isChangedFour;
            }
            ring.stop();
            ring.release();
        }
        isPlaying = false;
        if (numberOfSongs != 0) {
            ((TextView) findViewById(R.id.currentSongText)).setText(songNames.get(currentSongPlayingIndex));
        }
        else {
            ((TextView) findViewById(R.id.currentSongText)).setText(" ");
        }
        ((Button) findViewById(R.id.PlayButton)).setText("Play");

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permisson", "Permission is granted");
        }
        else {
            Log.d("Permission", "asking for permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 9998);
        }

        Log.d("creating intent", "214");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        shouldChangeDirectory = true;
        Log.d("pFile", "about to call ");
        startActivityForResult(intent.createChooser(intent, "Choose directory"), 9999);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Inside", "onActivityResult");
        Log.d("requestCode", "is " + requestCode);
        switch(requestCode) {
            case 9999:
                if (data != null){
                    Log.d("asdf", "data != null");
                    Uri uri = data.getData();
                    Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    String path = getPath(this, docUri);
                    if (path != null) {
                        Log.d("SomePathStuff", path);
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("Path", path);
                        editor.commit();
                    }

                    //changeDirectory(fTemp);
                }
                break;
        }
    }



    public void changeDirectory(File newDirectory) {
        Log.d("newDirectory path: ", newDirectory.getAbsolutePath());
        Log.d("newDirectory path: ", "" + newDirectory.exists());
        atLeastOneSong = false;


        if (newDirectory != null) {
            numberOfSongs = 0;
            allMusic = newDirectory.listFiles();
            if (allMusic == null) {
                Log.d("asdfasdfasdfasdfasdf", "allMusic is null");
            }
            else {
                Log.d("asdf", "allMusic NOT null");
                for (int i = 0; i < allMusic.length; i++) {
                    Log.d("allMusicPath", allMusic[i].getAbsolutePath());
                }
                mptList = new ArrayList<>();
                songNames = new ArrayList<>();
                for (int i = 0; i < allMusic.length; i++) {
                    if (allMusic[i].getName().length() > 4) {
                        if (allMusic[i].getName().substring(allMusic[i].getName().length() - 4).toLowerCase().equals(".mp3")){
                            if (i == 0){
                                Log.d("some Uri stuff: " , allMusic[i].getName());
                                Log.d(" uri parsed: ", allMusic[i].getAbsolutePath());
                            }
                            mptList.add(Uri.parse(allMusic[i].getAbsolutePath()));
                            numberOfSongs++;
                            songNames.add(allMusic[i].getName());
                            Log.d("music", "added " + allMusic[i].getName());
                            if (atLeastOneSong == false){
                                ring = MediaPlayer.create(MainActivity.this, mptList.get(0));
                                ((TextView) findViewById(R.id.currentSongText)).setText(songNames.get(0));
                                currentSongPlayingIndex = 0;
                                atLeastOneSong = true;
                                ring.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        NextSong();
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    public void PreviousSong() {
        if (atLeastOneSong) {
            if (isChangedFour) {
                isChangedFour = !isChangedFour;
                try {
                    thread.join();
                }
                catch (Exception ex) { }

                isChangedFour = !isChangedFour;
            }
            ring.stop();
            ring.release();
            saveSeconds();
            currentSongPlayingIndex--;
            if (currentSongPlayingIndex < 0){
                currentSongPlayingIndex = numberOfSongs - 1;
            }
            //ring = MediaPlayer.create(MainActivity.this, Uri.parse(allMusic[currentSongPlayingIndex].getAbsolutePath()));
            ring = MediaPlayer.create(MainActivity.this, mptList.get(currentSongPlayingIndex));
            ((TextView) findViewById(R.id.currentSongText)).setText(songNames.get(currentSongPlayingIndex));
            if (isPlaying) {
                ring.start();
            }
            if (isChangedFour) {
                LeftRightSin();
            }
            ring.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    NextSong();
                }
            });
        }
    }

    public void NextSong() {


        if (atLeastOneSong) {
            if (isChangedFour) {
                isChangedFour = !isChangedFour;
                try {
                    thread.join();
                }
                catch (Exception ex) { }

                isChangedFour = !isChangedFour;
            }
            ring.stop();
            ring.release();
            saveSeconds();
            currentSongPlayingIndex++;
            if (currentSongPlayingIndex == numberOfSongs){
                currentSongPlayingIndex = 0;
            }

            ring = MediaPlayer.create(MainActivity.this, mptList.get(currentSongPlayingIndex));
            //ring = MediaPlayer.create(MainActivity.this, Uri.parse());
            ((TextView) findViewById(R.id.currentSongText)).setText(songNames.get(currentSongPlayingIndex));
            if (isPlaying) {
                ring.start();
            }
            if (isChangedFour) {
                LeftRightSin();
            }
            ring.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    NextSong();
                }
            });
        }
    }

    public void ButtonNextSong(View v) {
        NextSong();
    }

    public void ButtonPrevSong(View v) {
        PreviousSong();
    }

    public void clickButtonPlay(View v) {
        if (atLeastOneSong) {
            if (isPlaying == false){
                ((Button) v).setText("Playing");
                ring.start();
                isChangedFour = true;
                isPlaying = true;
                LeftRightSin();
            }
            else{
                ((Button) v).setText("Play");
                isChangedFour = false;
                ring.pause();
                isPlaying = false;
            }
        }
    }



    public void clickButtonChangeOne(View v) {
        if (!isChangedOne) {
            ((Button) v).setText("Selected");
            isChangedOne = !isChangedOne;

            Thread thread = new Thread() {
                @Override
                public void run() {
                    long base = System.currentTimeMillis();
                    boolean rightEar = true;
                    while(isChangedOne){
                        if (System.currentTimeMillis() - base >= 25){
                            base = System.currentTimeMillis();
                            if (rightEar){
                                ring.setVolume(1, 0);
                            }
                            else{
                                ring.setVolume(0, 1);
                            }
                            rightEar = !rightEar;
                        }

                    }
                    ring.setVolume(1, 1);
                }
            };
            thread.start();
        }
        else {
            ring.setVolume(1, 1);
            ((Button) v).setText("Left/Right");
            isChangedOne = !isChangedOne;
        }
         /*
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,0);

        if (isRunning) {
            shouldContinue = false;
        }
        else {
            shouldContinue = true;
            ((Button) v).setText("changed");
            changeAudio();
        }
        */
    }

    public void clickButtonChangeTwo(View v) {
        if (!isChangedTwo) {
            ((Button) v).setText("Selected");
            isChangedTwo = !isChangedTwo;

            Thread thread = new Thread() {
                @Override
                public void run() {
                    long base = System.currentTimeMillis();
                    boolean rightEar = true;
                    while(isChangedTwo){
                        if (System.currentTimeMillis() - base >= 25){
                            base = System.currentTimeMillis();
                            if (rightEar){
                                ring.setVolume(0, 0);
                            }
                            else{
                                ring.setVolume(1, 1);
                            }
                            rightEar = !rightEar;
                        }
                    }
                    ring.setVolume(1, 1);
                }
            };
            thread.start();
        }
        else {
            ring.setVolume(1, 1);
            ((Button) v).setText("Left/Right Sin");
            isChangedTwo = !isChangedTwo;
        }
    }

    public void clickButtonChangeThree(View v) {
        if (!isChangedThree) {
            ((Button) v).setText("Selected");
            isChangedThree = !isChangedThree;

            Thread thread = new Thread() {
                @Override
                public void run() {
                    long base = System.currentTimeMillis();
                    boolean rightEar = true;
                    while(isChangedThree){
                        long temp = System.currentTimeMillis() % 50;
                        if (temp >= 25) {
                            // increasing
                            float audioStrength = (temp - 25) * 4 / 100;
                            audioStrength = (float) Math.sin(audioStrength * 90);
                            ring.setVolume(audioStrength, audioStrength);
                        }
                        else {
                            //decreasing
                            float audioStrength = temp * 4 / 100;
                            audioStrength = (float) Math.sin(audioStrength * 90);
                            ring.setVolume(1-audioStrength, 1-audioStrength);
                        }
                    }
                    ring.setVolume(1, 1);
                }
            };
            thread.start();
        }
        else {
            ring.setVolume(1, 1);
            ((Button) v).setText("Mute/Unmute");
            isChangedThree = !isChangedThree;
        }
    }

    // left/right sin
    public void clickButtonChangeFour(View v) {
        if (!isChangedFour) {
            ((Button) v).setText("Selected");
            isChangedFour = !isChangedFour;
            LeftRightSin();
        }
        else {
            ring.setVolume(1, 1);
            ((Button) v).setText("Left/Right Sin");
            isChangedFour = !isChangedFour;
        }
    }

    public void updateTextFile() {

    }

    public void addSecond() {
        seconds++;
        long remainder = seconds;
        int tHours = (int) seconds/3600;
        remainder = (int) remainder - tHours * 3600;
        int tMin = (int) (remainder / 60);
        int tSec = (int) (remainder - tMin * 60);
        String toPrint = "Time this session: ";
        if (tHours < 10) {
            toPrint += "0";
        }
        toPrint += tHours + ":";
        if (tMin < 10) {
            toPrint += "0";
        }
        toPrint += tMin + ":";
        if (tSec < 10) {
            toPrint += "0";
        }
        toPrint += tSec;
        ((TextView) findViewById(R.id.currentTimeInUse)).setText(toPrint);
    }

    public void saveSeconds() {
        thread = new Thread() {
            @Override
            public void run() {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("Time", seconds);
                editor.commit();
                Log.d("asdf", "seconds are or are supposed to be " + seconds);
            }
        };
        thread.start();
    }

    public void LeftRightSin() {
        if (atLeastOneSong) {
            if (atLeastOneSong) {
                LeftRightSinTwo();
                return;
            }

            Log.d("Inside", "LeftRightSin");

            if (thread != null && thread.isAlive()) {
                Log.d("thread", "was alive, should have not been");
                try {
                    isChangedFour = false;
                    thread.join();
                    Log.d("thread", "was stopped");
                    isChangedFour = true;
                }
                catch (Exception ex) {}

            }
            thread = new Thread() {
                @Override
                public void run() {
                    long startingTime = System.currentTimeMillis(); // this is used to add a second, updates every second.
                    // lastTimeTaken and timeNow are used to see if a millisecond has passed, if so we call ring.setVolume with relevant strength.
                    long lastTimeTaken = System.currentTimeMillis();
                    long timeNow;
                    int counter = 0;
                    long changer = 0; //changer is how many millisecond has passed, we add that to currentIndexArray
                    long lastChanger = 0;
                    int currentIndexArray = 0; // says which audiostrength index we are supposed to use
                    Log.d("inside", "thread");

                    boolean shouldChange = false;
                    String tempToPrint[] = new String[10];
                    for (int i = 0; i < 10; i++) {
                        tempToPrint[i] = "";
                    }
                    float nextChangeMade = getNextChangeMade(currentIndexArray, currentIndexArray + 1);

                    while(isChangedFour){ //isChangedFour is simply a boolean that indicates the thread should keep running.

                        timeNow = System.currentTimeMillis();
                        changer = (timeNow - lastTimeTaken); //get how many milliseconds have passed since last time we changed the settings

                        if (changer != lastChanger) { // so we don't check on the same thing multiple times.
                            lastChanger = changer;
                            nextChangeMade = getNextChangeMade(currentIndexArray, currentIndexArray + (int) changer);
                            if (nextChangeMade > 10 || changer > 5) {
                                shouldChange = true;
                                lastTimeTaken = timeNow;
                                currentIndexArray = currentIndexArray + (int) changer;
                                while (currentIndexArray >= 25) { // The size of the float array audioStrength is 25 and we go in circles.
                                    currentIndexArray = currentIndexArray - 25;
                                }
                            }
                        }
                        if (shouldChange) {
                            shouldChange = false;
                            counter++;
                            try {
                                if (currentIndexArray != 0) {
                                    // if the current strength of left is 0.2 the rigth must be 0.8. We use this method to get it.
                                    ring.setVolume(audioStrength[currentIndexArray], audioStrength[25 - currentIndexArray]);
                                }
                                else {
                                    // if not the strength is 0.5
                                    ring.setVolume(audioStrength[currentIndexArray], audioStrength[currentIndexArray]);
                                }
                            } catch (Exception ex) {
                                Log.d("Exception", "caught while trying to change the audiostrength");
                            }
                        }
                        // If a second has passed we update the textview in the function "addSecond()".
                        if (timeNow - startingTime > 1000) {
                            startingTime = timeNow;
                            Log.d("counter", counter + "");
                            counter = 0;
                            if (isPlaying){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addSecond();
                                    }
                                });
                            }
                        }
                    }
                    try {
                        // restore to default full audioStrength.
                        ring.setVolume(1, 1);
                        Log.d("Default", "back to normal settings");
                    } catch (IllegalStateException e) {
                        Log.d("Exception", "Illegal state exception caught");
                    }
                }
            };
            thread.start();
        }
    }


    public float getNextChangeMade(int currentIndex, int nextIndexArray) {
        while (nextIndexArray >= 25) {
            nextIndexArray = nextIndexArray - 25;
        }
        return Math.abs(audioStrength[nextIndexArray] - audioStrength[currentIndex])*100;
    }


    public void LeftRightSinTwo() {
        Log.d("inside", "leftRightSinTwo");
        thread = new Thread() {
            @Override
            public void run() {
                long base = System.currentTimeMillis();
                boolean rightEar = true;
                long startingTime = System.currentTimeMillis();
                while(isChangedFour){
                    if (System.currentTimeMillis() - startingTime > 1000) {
                        startingTime = System.currentTimeMillis();
                        if (isPlaying){
                            addSecond();
                        }
                    }
                    long temp = System.currentTimeMillis() % 25;
                    //int tempInt = (int) temp;
                    //float audioStrength = (float) (Math.sin(Math.toRadians(((float) (temp / 25)) * 360)) * 0.5 + 0.5);
                    //Log.d("audioStrength", audioStrength + "");
                    //ring.setVolume(audioStrength, 1-audioStrength);
                    //ring.setVolume();

                    /*
                    if (temp < 25) {
                        ring.setVolume(1, 0);
                        if (rightEar) {
                            rightEar = false;

                        }
                    }
                    else {
                        ring.setVolume(0, 1);
                        if (!rightEar) {
                            rightEar = true;

                        }
                    }
                    */
                    if (temp < 12) {
                        if (rightEar) {
                            ring.setVolume(1, 1);
                            rightEar = false;
                        }
                    }
                    else {
                        if (!rightEar) {
                            ring.setVolume(0, 0);
                            rightEar = true;
                        }
                    }



                }
                ring.setVolume(1, 1);
            }
        };
        thread.start();
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                try {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } catch (NumberFormatException e) {
                    return null;
                }

            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_folder) {
            pFileSearch();
        }

        return super.onOptionsItemSelected(item);

    }

}
