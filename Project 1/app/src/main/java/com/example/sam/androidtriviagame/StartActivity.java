package com.example.sam.androidtriviagame;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StartActivity extends AppCompatActivity {
    //MediaPlayer mPlayer;
    Switch mySwitch;
    String word;
    String defn;
    AddWordFragment fragment;
    String playerName;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("StartActivity", "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v("StartActivity", "onRestoreInstanceState");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("StartActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        playerName = getIntent().getStringExtra("name"); //grab the name
        //mPlayer = MediaPlayer.create(this, R.raw.popculture);
        mySwitch = findViewById(R.id.musicSwitch);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentdata) {
        Log.v("StartActivity", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, intentdata);

        if (requestCode == 123456789 && resultCode == RESULT_OK){
            fragment.dismiss();
            Log.v("onactivityresult", "extract returned inupts");
            // extract returned parameters from the intent
            word = intentdata.getStringExtra("word");
            defn = intentdata.getStringExtra("definition");

            // Write a message to the database
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();;

            database.child("WordsAndDefs").child(word).setValue(defn);
            //ADD THE WORD TO THE DATABASE
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("StartActivity", "onPause");
    }

    //toggling switch turns music on or off
    public void playMusic(View view){
//        if(mySwitch.isChecked()){
//
//            //mPlayer.start();
//        }else{
//            //mPlayer.pause();
//        }
    }

    public void clickPlayLayout(View view){
        Intent playIntent = new Intent(this, PlayActivity.class );
        if(mySwitch.isChecked()) {
            playIntent.putExtra("switch", 1);
        }else{
            playIntent.putExtra("switch", 0);
        }
        playIntent.putExtra("name",playerName);
        startActivityForResult(playIntent, 1);
    }

    public void clickAddLayout(View view){
        //Intent addIntent = new Intent(this, AddWordActivity.class );
        //startActivity(addIntent);
        FragmentManager manager = getFragmentManager();
        fragment = new AddWordFragment();
        fragment.show(manager, "add_word_dialog");

    }

    public void clickScoreLayout(View view){
        Intent scoreIntent = new Intent(this, ScoreHistoryActivity.class );
        startActivity(scoreIntent);
    }

}


