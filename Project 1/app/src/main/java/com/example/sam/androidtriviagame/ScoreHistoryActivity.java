package com.example.sam.androidtriviagame;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;


public class ScoreHistoryActivity extends AppCompatActivity {

    LinearLayout linear;

    SQLiteDatabase sqliteScoreHistoryDB;
    SQLiteDatabase sqliteHighScoreDB;

    String playerID;

    String CHANNEL_ID = "trivia";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);

        playerID = getIntent().getStringExtra("id");

        //individual score history sqlite db
        sqliteScoreHistoryDB = openOrCreateDatabase("scoreHistory",MODE_PRIVATE,null);

        //sqliteScoreHistoryDB.execSQL("Drop Table if exists "+"scoreHistory");
        String query = "CREATE TABLE IF NOT EXISTS scoreHistory ( "
                + " id TEXT PRIMARY KEY, "
                + " score TEXT NOT NULL, "
                + " time TEXT NOT NULL "
                + ")";
        sqliteScoreHistoryDB.execSQL(query);

        //print the database to log helper function
        //String tableString = getTableAsString(sqliteScoreHistoryDB, "scoreHistory");
        //Log.v("Table as string scoreHistory", tableString);

        //high score sqlite db
        sqliteHighScoreDB = openOrCreateDatabase("highScore",MODE_PRIVATE, null);

        String query1 = "CREATE TABLE IF NOT EXISTS highScore ( "
                + " id TEXT PRIMARY KEY, "
                + " name TEXT NOT NULL, "
                + " score TEXT NOT NULL "
                + ")";
        sqliteHighScoreDB.execSQL(query1);

        //print the database to log helper function
        //String tableString = getTableAsString(sqliteHighScoreDB, "highScore");
        //Log.v("Table as string highScore", tableString);


        DatabaseReference fb = FirebaseDatabase.getInstance().getReference();
        DatabaseReference scoreHistoryFB = fb.child("Players").child(playerID).child("Scores");

        scoreHistoryFB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot :dataSnapshot.getChildren()){
                    String key = snapshot.getKey();
                    Log.v("ondatachange in history Key",key);

                    String s = snapshot.child("Score").getValue().toString();
                    String month = snapshot.child("TimeStamp").child("month").getValue().toString();
                    String date = snapshot.child("TimeStamp").child("date").getValue().toString();
                    String hours = snapshot.child("TimeStamp").child("hours").getValue().toString();
                    String minutes = snapshot.child("TimeStamp").child("minutes").getValue().toString();
                    String seconds = snapshot.child("TimeStamp").child("seconds").getValue().toString();
                    String t = month+"/"+date+"  "+hours+":"+minutes+":"+seconds;

                    Log.v("s", s);
                    Log.v("t",t);

                    //add values to SQLite database
                    ContentValues cvalues = new ContentValues();
                    cvalues.put("id", key);
                    cvalues.put("score", s);
                    cvalues.put("time",t);
                    sqliteScoreHistoryDB.replace("scoreHistory", null, cvalues);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("onCancelled in Firebase","");
            }
        });

        DatabaseReference f = FirebaseDatabase.getInstance().getReference();
        DatabaseReference highScoreDB = f.child("HighScores");

        highScoreDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String key = snapshot.getKey();
                    Log.v("ondatachange in high score key",key);

                    String name = snapshot.child("Name").getValue().toString();
                    String score = snapshot.child("Score").getValue().toString();

                    Log.v("name", name);
                    Log.v("score", score);

                    //add values to the sqlite database
                    ContentValues cvalues = new ContentValues();
                    cvalues.put("id", key);
                    cvalues.put("name", name);
                    cvalues.put("score", score);
                    sqliteHighScoreDB.replace("highScore", null, cvalues);


                }
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "TriviaApp", NotificationManager.IMPORTANCE_HIGH);
                mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);

                Notification.Builder builder = new Notification.Builder(ScoreHistoryActivity.this)
                        .setContentTitle("Android Trivia Game")
                        .setContentText("Score sync is complete")
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_stat_android)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setChannelId(CHANNEL_ID);
                Notification notification = builder.build();



                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.createNotificationChannel(mChannel);

                // Issue the notification.
                mNotificationManager.notify(23456 , notification);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("onCancelled in Firebase","");
            }
        });


        setupLayout();


    }

    private void setupLayout(){
        linear = findViewById(R.id.scoresLayout);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View childView1;

        int h = 1; //used as an id to differentiate between each newly added inflated view

        //get the local player scores and show them
        Cursor cr = sqliteScoreHistoryDB.rawQuery("SELECT score, time FROM scoreHistory", null);
        cr.moveToFirst();
        //use a cursor to loop through all the scores
        while( cr != null && cr.getCount() != 0 ) {
            Log.v("cursor position", Integer.toString(cr.getPosition()));
            childView1 = inflater.inflate(R.layout.add_score, null);
            childView1.setId(h);

            linear.addView(childView1);

            View view = findViewById(h);
            TextView timeview = view.findViewById(R.id.timeText);
            TextView percentview = view.findViewById(R.id.percentText);

            String timev = cr.getString(cr.getColumnIndex("time"));
            String percentv = cr.getString(cr.getColumnIndex("score"));
            Log.v("timev",timev);
            Log.v("percentv",percentv);

            timeview.setText(timev);
            percentview.setText(percentv);

            cr.moveToNext();
            //need this to make sure that it doesn't pull a null from the SQLite database
            //not sure why they cr != null doesn't do this for me
            if(cr.getPosition() >= cr.getCount()){
                break;
            }

            h++;
        }
        cr.close();

        childView1 = inflater.inflate(R.layout.add_score, null);
        childView1.setId(h);

        linear.addView(childView1);

        View view1 = findViewById(h);
        TextView timeview = view1.findViewById(R.id.timeText);
        TextView percentview = view1.findViewById(R.id.percentText);

        timeview.setText("High Score Name");
        percentview.setText("Score");
        h++;


        //get the high scores and show them on the screen
        Cursor cr1 = sqliteHighScoreDB.rawQuery("SELECT name, score FROM highScore", null);
        cr1.moveToFirst();
        //use a cursor to loop through all the scores
        while( cr1 != null && cr1.getCount() != 0 ) {
            Log.v("cursor1 position", Integer.toString(cr1.getPosition()));
            childView1 = inflater.inflate(R.layout.add_score, null);
            childView1.setId(h);

            linear.addView(childView1);

            View view = findViewById(h);
            TextView timeview1 = view.findViewById(R.id.timeText);
            TextView percentview1 = view.findViewById(R.id.percentText);

            String name = cr1.getString(cr1.getColumnIndex("name"));
            String score = cr1.getString(cr1.getColumnIndex("score"));
            Log.v("name",name);
            Log.v("score",score);

            timeview1.setText(name);
            percentview1.setText(score);

            cr1.moveToNext();
            //need this to make sure that it doesn't pull a null from the SQLite database
            //not sure why they cr != null doesn't do this for me
            if(cr1.getPosition() >= cr1.getCount()){
                break;
            }

            h++;
        }

        cr1.close();
    }

    public void backClick(View view){
        finish();
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
