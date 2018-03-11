package com.example.sam.androidtriviagame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

public class AddWordActivity extends AppCompatActivity {
    EditText term;
    EditText definition;
    Button okayButton;
    PrintStream output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        term = (EditText) findViewById(R.id.termText);
        definition = (EditText) findViewById(R.id.defText);
        okayButton = (Button) findViewById(R.id.addTermOkayButton);

        try {
            output = new PrintStream(openFileOutput("newwordsdefinitions.txt", MODE_APPEND));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.v("Add word activity", "new file not found");
        }
    }

    public void addTerm(View view){
        //check to make sure something is inputted
        if(term.getText().toString() != "" && definition.getText().toString() != "") {
            String line = term.getText().toString() + ", " + definition.getText().toString();
            Log.v("addwordactivity", ""+line);
            output.println(line);
            output.close();

            //go back to the main menu
            finish();
        }else{
            Toast.makeText(getApplicationContext(), "Please enter values for term and definition", Toast.LENGTH_SHORT);
        }
    }
}
