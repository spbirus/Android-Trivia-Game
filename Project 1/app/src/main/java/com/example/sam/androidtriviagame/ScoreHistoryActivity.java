package com.example.sam.androidtriviagame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;


public class ScoreHistoryActivity extends AppCompatActivity {

    LinearLayout linear;
    Scanner scanNew;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);

        ArrayList scoreArray = new ArrayList(100);





        try {
            scanNew = new Scanner(openFileInput("finalscores.txt"));
            //add new to the array
            while (scanNew.hasNextLine()) {
                scoreArray.add(scanNew.nextLine());
            }
        } catch (FileNotFoundException e) {
            Log.v("Play activity", "new file not found");
        }


        linear = findViewById(R.id.scoresLayout);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View childView1;
        int h = 1; //used as an id to differentiate between each newly added inflated view
        for (Object s : scoreArray) {
            String[] k = s.toString().split(",");

            childView1 = inflater.inflate(R.layout.add_score, null);
            childView1.setId(h);

            linear.addView(childView1);

            View view = findViewById(h);
            TextView timeview = view.findViewById(R.id.timeText);
            TextView percentview = view.findViewById(R.id.percentText);

            timeview.setText(k[0]);
            percentview.setText(k[1]);

            h++;

        }

    }

    public void backClick(View view){
        finish();
    }
}
