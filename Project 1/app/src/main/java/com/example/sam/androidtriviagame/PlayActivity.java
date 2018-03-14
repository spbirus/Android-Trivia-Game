package com.example.sam.androidtriviagame;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class PlayActivity extends AppCompatActivity {
    Scanner scanOriginal; //scans the original file
    Scanner scanNew; //scans the new file with newly added words
    String[] wordAndDef; //stores the correct word and its matching def

    PrintStream output; //file for the scores

    TextView word;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    ProgressBar bar;

    String[] storedWords = new String[100]; //store the words and definitions in an array to be randomly accessed
    String[] storedDefs = new String[100]; //store the defs to be randomly accessed for incorrect answers
    int totalWords = 0;
    int correctWordNumber;
    ArrayList alreadySelectedWords;

    int finalScore; //amount of correct answers
    String finalPercent; //percent of correct answers
    int timer; //keeps track of how many words shown.  Should cap at 5
    Random rand = new Random();

    int correctNumber; //stores which button is correct
    String correctAnswer; //stores which button is correct

    int switchData; //stores text to speech
    private TextToSpeech tts;
    private boolean isTTSinitialized;

    String playerName;
    String playerID;

    SQLiteDatabase sqliteWordDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        switchData = getIntent().getIntExtra("switch",0);
        playerName = getIntent().getStringExtra("name");
        playerID = getIntent().getStringExtra("id");

        Intent callingIntent = getIntent();

        isTTSinitialized = false;

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                isTTSinitialized = true;
            }
        });

        //grab all the widgets
        word = (TextView) findViewById(R.id.dictionaryWord);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        bar = (ProgressBar) findViewById(R.id.progressBar);

        //initialize the database
        sqliteWordDB = openOrCreateDatabase("WordsAndDefs",MODE_PRIVATE,null);

        //sqliteWordDB.execSQL("Drop Table if exists "+"WordsAndDefs");
        String query = "CREATE TABLE IF NOT EXISTS WordsAndDefs ( "
                + " id TEXT PRIMARY KEY, "
                + " word TEXT NOT NULL, "
                + " defn TEXT NOT NULL "
                + ")";
        sqliteWordDB.execSQL(query);

        String tableString = getTableAsString(sqliteWordDB, "WordsAndDefs");
        Log.v("Table as string", tableString);

        //initialize the array to reset after each time the play activity is called
        alreadySelectedWords= new ArrayList();

        //set timer
        timer = 1;

        //set initial score to 0
        finalScore = 0;


        DatabaseReference fb = FirebaseDatabase.getInstance().getReference();
        DatabaseReference wordsAndDefsFB = fb.child("WordsAndDefs");

        wordsAndDefsFB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                for(DataSnapshot snapshot :data.getChildren()){
                    String key = snapshot.getKey();
                    Log.v("onDataChange in playActivity. Key",key);

                    String w = snapshot.child("word").getValue().toString();
                    String d = snapshot.child("defn").getValue().toString();

                    Log.v("w", w);
                    Log.v("d",d);

                    //add values to SQLite database
                    ContentValues cvalues = new ContentValues();
                    cvalues.put("id", key);
                    cvalues.put("word", w);
                    cvalues.put("defn",d);
                    sqliteWordDB.insert("WordsAndDefs", null, cvalues);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("onCancelled in Firebase","");
            }
        });



        //getWord();

        getTotalWords();
        getWordSQL();
        setDefSQL();
    }
    private void getTotalWords(){
        Cursor cr = sqliteWordDB.rawQuery("SELECT word FROM WordsAndDefs", null);
        totalWords = cr.getCount();
        cr.close();
        Log.v("Total Word #", Integer.toString(totalWords));
    }

    private void getWordSQL(){
        correctWordNumber = getRandomWithExclusion(rand, 1, totalWords-1, alreadySelectedWords); //has to be -2 so that array out of bounds exceptions aren't thrown
        alreadySelectedWords.add(correctWordNumber);

        Log.v("Correct word #", Integer.toString(correctWordNumber));

        String wordText = null;
        //set up a cursor to get the word
        Cursor cr = sqliteWordDB.rawQuery("SELECT word FROM WordsAndDefs", null);
        if( cr != null ) {
            cr.move(correctWordNumber);
            wordText = cr.getString(cr.getColumnIndex("word"));
        }
        cr.close();

        word.setText(wordText);

        //tts the word
        if(switchData == 1){
            Log.v("tts initialized", ""+isTTSinitialized);
            if (isTTSinitialized) {
                Log.v("tts word", "is in here");
                tts.speak("The word is " + wordAndDef[0],
                        TextToSpeech.QUEUE_FLUSH, null); //first time flush is used to skip the previous tts if there is some left
            }
        }
    }

//    public void getWord(){
//        correctWordNumber = getRandomWithExclusion(rand, 0, totalWords-2, alreadySelectedWords); //has to be -2 so that array out of bounds exceptions aren't thrown
//        alreadySelectedWords.add(correctWordNumber);
//        wordAndDef = storedWords[correctWordNumber].split(",");
//
//
//
//        //set the text on screen to the word
//        word.setText(wordAndDef[0]);
//
//        //tts the word
//        if(switchData == 1){
//            Log.v("tts initialized", ""+isTTSinitialized);
//            if (isTTSinitialized) {
//                Log.v("tts word", "is in here");
//                tts.speak("The word is " + wordAndDef[0],
//                        TextToSpeech.QUEUE_FLUSH, null); //first time flush is used to skip the previous tts if there is some left
//            }
//        }
//
//        //set the definitions
//        //both the correct one and the rest
//        setDef();
//    }

    public void setDefSQL(){
        correctNumber = rand.nextInt(5) + 1;
        correctAnswer = "Button"+correctNumber;
        ArrayList exclude = new ArrayList();
        exclude.add(correctWordNumber); //exclude the correct answer from being put as a random wrong answer

        String defnText = null;
        //set up a cursor to get the def
        Cursor cr = sqliteWordDB.rawQuery("SELECT defn FROM WordsAndDefs", null);
        if( cr != null) {
            cr.move(correctWordNumber);
            defnText = cr.getString(cr.getColumnIndex("defn"));// cr.getString(correctWordNumber);
        }
        cr.close();
        //set the correct answer
        switch (correctNumber){
            case 1:
                button1.setText(defnText);
                break;
            case 2:
                button2.setText(defnText);
                break;
            case 3:
                button3.setText(defnText);
                break;
            case 4:
                button4.setText(defnText);
                break;
            case 5:
                button5.setText(defnText);
                break;
        }

        //set the other ones to wrong answers
        for(int i =1; i<=5; i++){
            //Log.v("Loop for wrong answers", ""+i);
            //Log.v("size", ""+exclude.size());
            //Log.v("exclude", ""+exclude.toString());
            int wrongRandom = getRandomWithExclusion(rand, 1, 5, exclude);
            //Log.v("wrongRandom", ""+wrongRandom);
            exclude.add(wrongRandom);
            Collections.sort(exclude); //needs to be in sorted order to exclude the correct numbers

            String defnWrongText = null;
            //set up a cursor to get the def
            Cursor crWrong = sqliteWordDB.rawQuery("SELECT defn FROM WordsAndDefs", null);
            if(crWrong != null){
                crWrong.move(wrongRandom);
                defnWrongText = crWrong.getString(crWrong.getColumnIndex("defn"));
            }
            crWrong.close();

            if(i != correctNumber) {
                switch (i) {
                    case 1:
                        button1.setText(defnWrongText);
                        if (switchData == 1) {
                            Log.v("tts initialized", "" + isTTSinitialized);
                            if (isTTSinitialized) {
                                tts.speak("Choice 1 " + defnWrongText,
                                        TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                        break;
                    case 2:
                        button2.setText(defnWrongText);
                        if (switchData == 1) {
                            Log.v("tts initialized", "" + isTTSinitialized);
                            if (isTTSinitialized) {
                                tts.speak("Choice 2 " + defnWrongText,
                                        TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                        break;
                    case 3:
                        button3.setText(defnWrongText);
                        if (switchData == 1) {
                            Log.v("tts initialized", "" + isTTSinitialized);
                            if (isTTSinitialized) {
                                tts.speak("Choice 3 " + defnWrongText,
                                        TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                        break;
                    case 4:
                        button4.setText(defnWrongText);
                        if (switchData == 1) {
                            if (isTTSinitialized) {
                                tts.speak("Choice 4 " + defnWrongText,
                                        TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                        break;
                    case 5:
                        button5.setText(defnWrongText);
                        if (switchData == 1) {
                            Log.v("tts initialized", "" + isTTSinitialized);
                            if (isTTSinitialized) {
                                tts.speak("Choice 5 " + defnWrongText,
                                        TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                        break;
                }

            }else{
                //need to tts the correct answer too
                if(switchData == 1){
                    Log.v("tts initialized", ""+isTTSinitialized);
                    if (isTTSinitialized) {
                        tts.speak("Choice "+correctNumber+ " " + defnText,
                                TextToSpeech.QUEUE_ADD, null);
                    }
                }
            }
        }

    }

//    public void setDef(){
//        //randomize which one has the correct answer
//        correctNumber = rand.nextInt(5) + 1;
//        correctAnswer = "Button"+correctNumber;
//        ArrayList exclude = new ArrayList();
//        exclude.add(correctWordNumber); //exclude the correct answer from being put as a random wrong answer
//
//        //set the correct answer
//        switch (correctNumber){
//            case 1:
//                button1.setText(wordAndDef[1]);
//                break;
//            case 2:
//                button2.setText(wordAndDef[1]);
//                break;
//            case 3:
//                button3.setText(wordAndDef[1]);
//                break;
//            case 4:
//                button4.setText(wordAndDef[1]);
//                break;
//            case 5:
//                button5.setText(wordAndDef[1]);
//                break;
//        }
//
//        //set the other ones to wrong answers
//        for(int i =1; i<=5; i++){
//            //Log.v("Loop for wrong answers", ""+i);
//            //Log.v("size", ""+exclude.size());
//            //Log.v("exclude", ""+exclude.toString());
//            int wrongRandom = getRandomWithExclusion(rand, 0, 4, exclude);
//            //Log.v("wrongRandom", ""+wrongRandom);
//            exclude.add(wrongRandom);
//            Collections.sort(exclude); //needs to be in sorted order to exclude the correct numbers
//
//
//
//            if(i != correctNumber) {
//                switch (i) {
//                    case 1:
//                        button1.setText(storedDefs[wrongRandom]);
//                        if (switchData == 1) {
//                            Log.v("tts initialized", "" + isTTSinitialized);
//                            if (isTTSinitialized) {
//                                tts.speak("Choice 1 " + storedDefs[wrongRandom],
//                                        TextToSpeech.QUEUE_ADD, null);
//                            }
//                        }
//                        break;
//                    case 2:
//                        button2.setText(storedDefs[wrongRandom]);
//                        if (switchData == 1) {
//                            Log.v("tts initialized", "" + isTTSinitialized);
//                            if (isTTSinitialized) {
//                                tts.speak("Choice 2 " + storedDefs[wrongRandom],
//                                        TextToSpeech.QUEUE_ADD, null);
//                            }
//                        }
//                        break;
//                    case 3:
//                        button3.setText(storedDefs[wrongRandom]);
//                        if (switchData == 1) {
//                            Log.v("tts initialized", "" + isTTSinitialized);
//                            if (isTTSinitialized) {
//                                tts.speak("Choice 3 " + storedDefs[wrongRandom],
//                                        TextToSpeech.QUEUE_ADD, null);
//                            }
//                        }
//                        break;
//                    case 4:
//                        button4.setText(storedDefs[wrongRandom]);
//                        if (switchData == 1) {
//                            if (isTTSinitialized) {
//                                tts.speak("Choice 4 " + storedDefs[wrongRandom],
//                                        TextToSpeech.QUEUE_ADD, null);
//                            }
//                        }
//                        break;
//                    case 5:
//                        button5.setText(storedDefs[wrongRandom]);
//                        if (switchData == 1) {
//                            Log.v("tts initialized", "" + isTTSinitialized);
//                            if (isTTSinitialized) {
//                                tts.speak("Choice 5 " + storedDefs[wrongRandom],
//                                        TextToSpeech.QUEUE_ADD, null);
//                            }
//                        }
//                        break;
//                }
//
//            }else{
//                //need to tts the correct answer too
//                if(switchData == 1){
//                    Log.v("tts initialized", ""+isTTSinitialized);
//                    if (isTTSinitialized) {
//                        tts.speak("Choice "+correctNumber+ " " + wordAndDef[1],
//                                TextToSpeech.QUEUE_ADD, null);
//                    }
//                }
//            }
//        }
//
//    }

    public void onDefClick(View view){

            //check if the def picked is correct
            switch (view.getId()) {
                case R.id.button1:
                    checkAnswer("Button1");
                    break;
                case R.id.button2:
                    checkAnswer("Button2");
                    break;
                case R.id.button3:
                    checkAnswer("Button3");
                    break;
                case R.id.button4:
                    checkAnswer("Button4");
                    break;
                case R.id.button5:
                    checkAnswer("Button5");
                    break;
            }
            //get new word
            getWordSQL();
            //set the new defn
            setDefSQL();
            timer++;

            if(timer > 5) {
                Toast.makeText(getApplicationContext(), "End of game", Toast.LENGTH_SHORT).show();

                changeToPercent();

                //create timestamp to send with the final percent
                Timestamp stamp = new Timestamp(System.currentTimeMillis());
                

                String line = stamp + ", " + finalPercent;
                Log.v("addwordactivity", ""+line);


                DatabaseReference database = FirebaseDatabase.getInstance().getReference();


                String id = playerID; //convert player id to a string to send to database
                database.child("Players").child(id).child("Name").setValue(playerName);
                //need to check if there is a new high score
                String scoreKey = database.child("Players").child(id).child("Scores").push().getKey();
                database.child("Players").child(id).child("Scores").child(scoreKey).child("Score").setValue(finalPercent);
                database.child("Players").child(id).child("Scores").child(scoreKey).child("TimeStamp").setValue(stamp);
                //go back to the menu
                finish();
            }
    }

    public void changeToPercent(){
        finalPercent = String.valueOf((double) finalScore/5*100);
    }

    //picks a random number and blocks it from being where the correct one is while also not picking the same incorrect twice
    public int getRandomWithExclusion(Random rnd, int start, int end, ArrayList exclude) {

        int random = start + rnd.nextInt(end - start + 1 - exclude.size()+1); //not sure why this has to be size + 1

        for (Object ex : exclude) {
            if (random < (int) ex) {
                break;
            }
            random++;
        }
        return random;
    }

    public void checkAnswer(String s){
        if(correctAnswer.equals(s)){
            Toast.makeText(getApplicationContext(), "Correct Answer", Toast.LENGTH_SHORT).show();
            finalScore++;
        }else{
            Toast.makeText(getApplicationContext(), "Wrong Answer", Toast.LENGTH_SHORT).show();
        }
        //update the progress bar to show how many questions left
        bar.incrementProgressBy(20);
    }

    /**
     * Helper function that parses a given table into a string
     * and returns it for easy printing. The string consists of
     * the table name and then each row is iterated through with
     * column_name: value pairs printed out.
     *
     * @param db the database to get the table from
     * @param tableName the the name of the table to parse
     * @return the table tableName as a string
     */
    public String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.d("tableAsString", "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }
        allRows.close();

        return tableString;
    }

}
