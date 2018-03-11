package com.example.sam.androidtriviagame;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class AddWordFragment extends DialogFragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        final View dialog = inflater.inflate(R.layout.add_word_dialog, group, false);
        // any code to initialize event listeners, etc.
        Button addButton = (Button) dialog.findViewById(R.id.add);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText wordBox = (EditText) dialog.findViewById(R.id.edit1);
                EditText defnBox = (EditText) dialog.findViewById(R.id.edit2);
                String word = wordBox.getText().toString();
                String defn = defnBox.getText().toString();


                // send information back to activity using an intent
                // (activity's onActivityResult method must be public)
                StartActivity activity = (StartActivity) getActivity();
                Intent intent = new Intent();
                intent.putExtra("word", word);
                intent.putExtra("definition", defn);
                activity.onActivityResult(1,
                        Activity.RESULT_OK, intent);

            }
        });
        return dialog;
    }
}
