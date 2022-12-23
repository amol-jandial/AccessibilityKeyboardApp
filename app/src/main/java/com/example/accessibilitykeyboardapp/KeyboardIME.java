package com.example.accessibilitykeyboardapp;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class KeyboardIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener,
        View.OnClickListener, ClipboardManager.OnPrimaryClipChangedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    String pasteData = "";
    private CustomKeyboardView kv;
    private int isLetterPressed = 0;
    private boolean firstShiftPressed = false;
    private Keyboard keyboard;
    private AppCompatButton btn, btnVoice, btnVoicePressed;
    private TextView speechTextView;
    private CandidateView mCandidateView;
    SharedPreferences prefs;
    private static final String TAG  = "bitches";
    private boolean spaceAfterDot;
    private SpeechRecognizer speechRecognizer;
    public static final Integer RecordAudioRequestCode = 1;
    private Intent speechRecognizerIntent;
    View toolBar;
    ConstraintLayout cl;



    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: START");
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService"));
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        Log.d(TAG, "onCreate: END");

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
                getCurrentInputConnection().commitText(data.get(0), 1);
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

    @Override
    public View onCreateInputView() {
        kv = (CustomKeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.qwerty_layout);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        kv.setPreviewEnabled(false);
        Log.d(TAG, "onCreateInputView: ");

        ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        return kv;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged: ");
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {

        if(prefs.getBoolean("auto_cap", true)){
            isLetterPressed = 0;
            firstShiftPressed = false;
            kv.setShifted(keyboard);
        }else{
            isLetterPressed = -1;
            firstShiftPressed = true;
        }

        if(prefs.getBoolean("theme_blue", true)){
            kv.setTheme("blue");
        }
        else if(prefs.getBoolean("theme_orange", false)){
            kv.setTheme("orange");
        }else{
            kv.setTheme("blue");
        }

        spaceAfterDot = prefs.getBoolean("auto_space", false);


    }

    @Override
    public View onCreateCandidatesView() {
        toolBar = (ConstraintLayout) getLayoutInflater().inflate(R.layout.toolbar, null);
        cl = (ConstraintLayout) toolBar.findViewById(R.id.toolbar_layout);
        setCandidatesViewShown(true);
        btnVoice = cl.findViewById(R.id.btn_voice);
        setListeners();
        return cl;
    }

    private void setListeners() {
        btnVoice.setOnClickListener(this);
    }




    @Override
    public void onComputeInsets(Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (!isFullscreenMode()) {
            outInsets.contentTopInsets = outInsets.visibleTopInsets;
        }
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {
//        popupWindow.release();
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                kv.deleteText(ic);
                break;
            case Keyboard.KEYCODE_SHIFT:
                kv.setShifted(keyboard);
                break;
            case Keyboard.KEYCODE_DONE:
                kv.enter(ic);
                break;

            default:

                if (primaryCode == 10001) {
                    kv.changeKeyboardLayout(R.xml.number_symbols_layout);
                } else if (primaryCode == 10002) {
                    kv.changeKeyboardLayout(R.xml.qwerty_layout);
                }
                else if(primaryCode == 10003){
                    kv.changeKeyboardLayout(R.xml.symbols_layout);
                }
                else if (primaryCode == 10005) {
                    kv.changeKeyboardLayout(R.xml.number_symbols_layout);
                }
                else {
                    isLetterPressed = kv.publishText(ic, primaryCode, keyboard, firstShiftPressed, spaceAfterDot);
                }
        }
        if(isLetterPressed == 1 && !firstShiftPressed){
            Log.d("shifting", "shifted to not shifted");
            kv.setShifted(keyboard);
            isLetterPressed = -1;
            firstShiftPressed = true;
        }
    }

    private void clipboard(){
       btn.setVisibility(View.VISIBLE);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
        Log.d("bitches", "clipboard: "+ item.getText());
        if(item.getText() != null){
            btn.setText(item.getText().toString());
        }
    }


    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    @Override
    public void onClick(View v) {
        //To remove visibility make the drawable background containing icon and background
        AppCompatButton btn = (AppCompatButton) v;
        switch(btn.getId()){
            case R.id.btn_voice:
                activateVoice();
                break;

            case R.id.clipboard_button:
                activateClipboard(btn);
                break;

            case R.id.btn_speech_clicked:
                speechRecognizer.stopListening();
                setCandidatesView(cl);
        }

    }

    private void activateVoice() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        View speechToolBar = (ConstraintLayout) getLayoutInflater().inflate(R.layout.speech_toolbar, null);
        ConstraintLayout cl = (ConstraintLayout) speechToolBar.findViewById(R.id.speech_toolbar);
        setCandidatesView(cl);
        speechTextView = cl.findViewById(R.id.tv_speech);
        btnVoicePressed = cl.findViewById(R.id.btn_speech_clicked);
        btnVoicePressed.setOnClickListener(this);
        Log.d(TAG, "before start listening ");
        speechRecognizer.startListening(speechRecognizerIntent);
    }


    @Override
    public void onFinishInputView(boolean finishingInput) {
        speechRecognizer.destroy();
    }

    private void checkPermission(){
        Intent intent = new Intent(this, PermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        KeyboardIME.this.startActivity(intent);
    }

    private void activateClipboard(AppCompatButton btn){
        String text = (String) btn.getText();
        getCurrentInputConnection().commitText(text, 1);
        ClipData clip = ClipData.newPlainText("","");
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clip);
    }

    @Override
    public void onPrimaryClipChanged() {
        Log.d("clipboard", "onPrimaryClipChanged: ");
//        clipboard();
    }









}
