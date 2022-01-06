package com.nodexsoutions.appfordeaf;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.gifview.library.GifView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SpeechRecognizer speechRecognizer;
    private ImageButton mic;
    private TextView textView;
    private ImageView sign;
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_CODE = 21671;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        init();

        checkAndRequestPermissions();

        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        mic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        textView.setText("Listening...");
                        speechRecognizer.startListening(intent);
                        break;
                }
                return false;
            }
        });
    }

    private void init(){
        mic = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        sign = findViewById(R.id.sign);
    }

    private void checkAndRequestPermissions() {
        int audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (audio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_CODE);
        }else{
            initspeechrecognizer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_CODE: {

                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);

                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    if (perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                        initspeechrecognizer();
                    } else {
                        explain(getResources().getString(R.string.you_need_some_mandatory));
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void explain(String msg) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        checkAndRequestPermissions();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }

    private void initspeechrecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)){
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "onEndOfSpeech: ");
                }

                @Override
                public void onError(int error) {
                    switch (error){
                        case 7:
                            textView.setText("No voice detected");
                            break;
                        case 13:
                            textView.setText("Language not available");
                            break;
                        case 12:
                            textView.setText("Language not supported");
                            break;
                        case 3:
                            textView.setText("Audio Error");
                            break;
                        default:
                            textView.setText("");
                            break;
                    }
                    sign.setImageDrawable(null);
                }

                @Override
                public void onResults(Bundle results) {
                    List<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    process(result.get(0));
                    textView.setText(result.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }

    private void process(String command) {
        command = command.toLowerCase();
        sign.setVisibility(View.VISIBLE);
        if (command.contains("single")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.single));
        }else if (command.contains("table")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.bench));
        }else if (command.contains("boy") || command.contains("male")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.boy));
        }else if (command.contains("car")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.car));
        }else if (command.contains("couch")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.couch));
        }else if (command.contains("cup")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.cup));
        }else if (command.contains("dad") || command.contains("father") || command.contains("daddy")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.dad));
        }else if (command.contains("divorce")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.divorce));
        }else if (command.contains("doctor")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.doctor));
        }else if (command.contains("drink")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.drink));
        }else if (command.contains("exercise")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.exercise));
        }else if (command.contains("girl")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.girl));
        }else if (command.contains("house")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.house));
        }else if (command.contains("husband")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.husband));
        }else if (command.contains("marriage")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.marriage));
        }else if (command.contains("milk")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.milk));
        }else if (command.contains("mom") || command.contains("mother")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.mom));
        }else if (command.contains("phone")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.phone));
        }else if (command.contains("sit")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.sit));
        }else if (command.contains("wife")){
            sign.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.wife));
        }else {
            sign.setImageDrawable(null);
            Toast.makeText(getApplicationContext(), "These words signs not available. Try words like father, mother, phone etc", Toast.LENGTH_SHORT).show();
        }
    }

}