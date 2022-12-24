package com.example.accessibilitykeyboardapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class KeyboardIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener,
        View.OnClickListener, ClipboardManager.OnPrimaryClipChangedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "bitches";
    String pasteData = "";
    SharedPreferences prefs;
    View toolBar;
    ConstraintLayout cl;
    private CustomKeyboardView kv;
    private Keyboard keyboard;
    private AppCompatButton btnClipboardPressed, btnVoice, btnVoicePressed, btnImage, btnClipboard;
    private TextView speechTextView;
    private CandidateView mCandidateView;
    private boolean spaceAfterDot;
    private SpeechImplementation speechImplementation;
    private ImageImplementation imageImplementation;

    @Override
    public void onCreate() {
        super.onCreate();
        imageImplementation = new ImageImplementation(this);
    }

    @Override
    public View onCreateInputView() {
        setupKeyboard();
        setupClipboard();
        setupPrefrences();
        return kv;
    }

    private void setupKeyboard() {
        kv = (CustomKeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.qwerty_layout);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        kv.setPreviewEnabled(false);
    }

    private void setupClipboard() {
        ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(this);
    }

    private void setupPrefrences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        Log.d(TAG, "onStartInputView: ");
        setupPreferencesSettings();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    private void setupPreferencesSettings(){


        if (prefs.getBoolean("theme_blue", true)) {
            kv.setTheme("blue");
        } else if (prefs.getBoolean("theme_orange", false)) {
            kv.setTheme("orange");
        } else {
            kv.setTheme("blue");
        }

        spaceAfterDot = prefs.getBoolean("auto_space", false);

    }



    @Override
    public View onCreateCandidatesView() {

        cl = setmCandidateView("toolbar", R.id.toolbar_layout);
        setCandidatesViewShown(true);
        btnVoice = cl.findViewById(R.id.btn_voice);
        btnImage = cl.findViewById(R.id.btn_img);
        btnClipboard = cl.findViewById(R.id.btn_clipboard);
        setListeners();
        return cl;
    }

    private void setListeners() {
        btnVoice.setOnClickListener(this);
        btnImage.setOnClickListener(this);
        btnClipboard.setOnClickListener(this);
    }


    @Override
    public void onComputeInsets(Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (!isFullscreenMode()) {
            outInsets.contentTopInsets = outInsets.visibleTopInsets;
        }
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
                    kv.changeKeyboardLayout(R.xml.number_symbols_layout, this);

                } else if (primaryCode == 10002) {
                    kv.changeKeyboardLayout(R.xml.qwerty_layout, this);

                } else if (primaryCode == 10003) {
                    kv.changeKeyboardLayout(R.xml.symbols_layout, this);

                } else if (primaryCode == 10005) {
                    kv.changeKeyboardLayout(R.xml.number_symbols_layout, this);

                } else {
                    kv.publishText(ic, primaryCode, keyboard, spaceAfterDot);
                }
        }

    }

    @Override
    public void onClick(View v) {
        //To remove visibility make the drawable background containing icon and background
        AppCompatButton btn = (AppCompatButton) v;
        switch (btn.getId()) {
            case R.id.btn_voice:
                activateVoice();
                break;

            case R.id.clipboard_button:
                activateClipboard(btn);
                break;

            case R.id.btn_speech_clicked:
                speechImplementation.end();
                setmCandidateView("toolbar", R.id.toolbar_layout);
                break;

            case R.id.btn_img:
                imageImplementation.checkPermissions();
                imageImplementation.start(getCurrentInputConnection());
                break;

            case R.id.btn_clipboard:
                ConstraintLayout ccl = setmCandidateView("wordbar", R.id.wordsLayout);
                btnClipboardPressed = ccl.findViewById(R.id.clipboard_button);
                clipboard();
        }

    }


    private void activateVoice() {
        ConstraintLayout scl = setmCandidateView("speech_toolbar", R.id.speech_toolbar);
        speechImplementation = new SpeechImplementation(this, getCurrentInputConnection(), speechTextView, scl);
        speechImplementation.checkPermissions(this);
        AppCompatButton speechPressBtn = speechImplementation.start();
        speechPressBtn.setOnClickListener(this);
    }

    private ConstraintLayout setmCandidateView(String res, int id) {

        View toolBar = (ConstraintLayout) getLayoutInflater().inflate(getResources().getIdentifier(res, "layout",
                        this.getPackageName()),
                null);
        ConstraintLayout cl = (ConstraintLayout) toolBar.findViewById(id);
        setCandidatesView(cl);
        return cl;
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        if(speechImplementation != null){
            speechImplementation.destroy();
        }
    }



    private void clipboard() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
        Uri image = Uri.parse(item.getText().toString());
        decodeImage(image);
    }

    private void decodeImage(Uri imageURI){
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image;
        try{
            image = InputImage.fromFilePath(this, imageURI);
            Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    String resulText = text.getText();
                    getCurrentInputConnection().commitText(resulText, 1);
//                    Log.d(TAG, "onSuccess: Result: "+resulText);
//                    for (Text.TextBlock block : text.getTextBlocks()) {
//                        String blockText = block.getText();
//                        Log.d(TAG, "onSuccess: Block : "+blockText);
//                        Point[] blockCornerPoints = block.getCornerPoints();
//                        Rect blockFrame = block.getBoundingBox();
//                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: ", e);
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }



    }

    private void activateClipboard(AppCompatButton btn) {
        String text = (String) btn.getText();
        getCurrentInputConnection().commitText(text, 1);
        ClipData clip = ClipData.newPlainText("", "");
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clip);
    }

    @Override
    public void onPrimaryClipChanged() {
        Log.d("clipboard", "onPrimaryClipChanged: ");
//        clipboard();
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
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

}
