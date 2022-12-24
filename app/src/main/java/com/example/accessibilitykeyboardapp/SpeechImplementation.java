package com.example.accessibilitykeyboardapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechImplementation {
    private SpeechRecognizer speechRecognizer;
    private static final Integer RecordAudioRequestCode = 1;
    private static final String TAG = "logging";
    private Intent speechRecognizerIntent;
    private InputConnection ic;
    private TextView speechTextView;
    private AppCompatButton btnVoicePressed;

    SpeechImplementation(Context context, InputConnection ic, TextView speechTextView, ConstraintLayout scl){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context, ComponentName.unflattenFromString("com" +
                ".google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService"));
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        this.ic = ic;
        this.speechTextView = scl.findViewById(R.id.tv_speech);
        this.btnVoicePressed = scl.findViewById(R.id.btn_speech_clicked);
    }

    public AppCompatButton start(){
        setListeners();
        speechRecognizer.startListening(speechRecognizerIntent);
        return btnVoicePressed;
    }

    public void end(){
        speechRecognizer.stopListening();
    }

    public void destroy(){
        speechRecognizer.destroy();
    }

    public void checkPermissions(Context context){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(context, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }

    private void setListeners(){
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "onReadyForSpeech: ");
                speechTextView.setText("Speak Now");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech: ");
                speechTextView.setText("Listening...");

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
                speechTextView.setText("Speak Now");
            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                Log.d(TAG, "onResults: ");
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                ic.commitText(data.get(0), 1);
                speechRecognizer.startListening(speechRecognizerIntent);
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
