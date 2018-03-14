package com.example.sam.androidtriviagame;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

    String playerID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);

        playerID = getIntent().getStringExtra("id");
        //Log.v("playerID in history",playerID);


       //ArrayList scoreArray = new ArrayList(100);

        sqliteScoreHistoryDB = openOrCreateDatabase("scoreHistory",MODE_PRIVATE,null);

        //sqliteScoreHistoryDB.execSQL("Drop Table if exists "+"scoreHistory");
        String query = "CREATE TABLE IF NOT EXISTS scoreHistory ( "
                + " id TEXT PRIMARY KEY, "
                + " score TEXT NOT NULL, "
                + " time TEXT NOT NULL "
                + ")";
        sqliteScoreHistoryDB.execSQL(query);

        //print the database to log helper function
        String tableString = getTableAsString(sqliteScoreHistoryDB, "scoreHistory");
        Log.v("Table as string scoreHistory", tableString);

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
                    sqliteScoreHistoryDB.insert("scoreHistory", null, cvalues);
                }

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
