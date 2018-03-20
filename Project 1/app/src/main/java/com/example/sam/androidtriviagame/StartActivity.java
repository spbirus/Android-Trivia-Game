package com.example.sam.androidtriviagame;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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
    String playerID;


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
        playerID = getIntent().getStringExtra("id");
        //mPlayer = MediaPlayer.create(this, R.raw.popculture);
        mySwitch = findViewById(R.id.musicSwitch);

        //check for camera permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED);
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, 6789);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentdata) {
        Log.v("StartActivity", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, intentdata);

        //callback from when dialog box is done entering data
        if (requestCode == 123456789 && resultCode == RESULT_OK){
            fragment.dismiss();
            Log.v("onactivityresult", "extract returned inupts");
            // extract returned parameters from the intent
            word = intentdata.getStringExtra("word");
            defn = intentdata.getStringExtra("definition");

            // Write a message to the database
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();

            String key = database.child("WordsAndDefs").push().getKey();
            database.child("WordsAndDefs").child(key).child("word").setValue(word); //need to grab a random integer as the ID
            database.child("WordsAndDefs").child(key).child("defn").setValue(defn);
            //ADD THE WORD TO THE DATABASE
        }

        //callback from the camera
        if (requestCode == 6789 && resultCode == RESULT_OK) {
            Bitmap bmp = (Bitmap) intentdata.getExtras().get("data");
            ImageView img = (ImageView) findViewById(R.id.camera_image);
            img.setImageBitmap(bmp);
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
        playIntent.putExtra("id",playerID);
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
        scoreIntent.putExtra("id", playerID);
        startActivity(scoreIntent);
    }

    public void clickCamera(View view){

        Intent picIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(picIntent, 6789);


    }

}


