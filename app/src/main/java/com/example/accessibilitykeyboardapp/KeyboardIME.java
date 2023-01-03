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
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabelerOptionsBase;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class KeyboardIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener,
        View.OnClickListener, ClipboardManager.OnPrimaryClipChangedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "bitches";
    String pasteData = "";
    SharedPreferences prefs;
    View toolBar;
    ConstraintLayout tcl, icl, scl, dcl, ccl;
    private CustomKeyboardView kv;
    private Keyboard currentKeyboard;
    private List<Keyboard> keyboards = new ArrayList<Keyboard>();
    private AppCompatButton btnClipboardPressed, btnVoice, btnVoicePressed, btnImage, btnClipboard, btnDraw,
    btnClassify, btnClear, btnBack, btnSettings, btnArrowLeftToolbar, btnArrowLeftClipboard, btnArrowLeftDraw,
            btnImageLabeling;
    private TextView speechTextView;
    private CandidateView mCandidateView;
    private boolean spaceAfterDot;
    private SpeechImplementation speechImplementation;
    private ImageImplementation imageImplementation;
    private Uri imageUri = null;
    private RemoteModelManager remoteModelManager = RemoteModelManager.getInstance();
    private DigitalInkRecognitionModel model;
    private ClipboardManager clipBoard;
    private long timePressed;
    private boolean isKeyHeld = false;
    private int starter;


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

    public void commitText(String text){
        getCurrentInputConnection().commitText(text, 1);
    }



    private void setupCandidateViews(){
        View toolBar = (ConstraintLayout) getLayoutInflater().inflate(R.layout.toolbar, null);
        View speechToolBar = (ConstraintLayout) getLayoutInflater().inflate(R.layout.speech_toolbar, null);
        View imageToolBar = (ConstraintLayout) getLayoutInflater().inflate(R.layout.image_processing_layout, null);
        View drawingToolbar = (ConstraintLayout) getLayoutInflater().inflate(R.layout.drawing,null);
        View clipboardToolbar = (ConstraintLayout) getLayoutInflater().inflate(R.layout.wordbar, null);

        tcl = (ConstraintLayout) toolBar.findViewById(R.id.toolbar_layout);
        scl = (ConstraintLayout) speechToolBar.findViewById(R.id.speech_toolbar);
        icl = (ConstraintLayout) imageToolBar.findViewById(R.id.image_proc_toolbar);
        dcl = (ConstraintLayout) drawingToolbar.findViewById(R.id.drawing_layout);
        ccl = (ConstraintLayout) clipboardToolbar.findViewById(R.id.wordsLayout);
    }

    private void setupKeyboard() {
        kv = (CustomKeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboards.add(new Keyboard(this, R.xml.qwerty_layout));
        keyboards.add(new Keyboard(this, R.xml.number_symbols_layout));
        keyboards.add(new Keyboard(this, R.xml.symbols_layout));
        keyboards.add(new Keyboard(this, R.xml.drawing_layout));

        kv.setKeyboard(keyboards.get(GlobalVariables.Keyboards.qwerty.ordinal()));
        kv.setPadding(0,0,0,8);
        kv.setOnKeyboardActionListener(this);
        kv.setPreviewEnabled(false);
        currentKeyboard = keyboards.get(GlobalVariables.Keyboards.qwerty.ordinal());
    }

    private void setupClipboard() {
        clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(this);
    }

    private void setupPrefrences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        Log.d(TAG, "onStartInputView: "+ btnVoice.getId());
        if(imageUri != null){
            if(starter == 1){
                labelImage(imageUri);
            }
            else if(starter == 0){
                decodeImage(imageUri);
            }
        }
        setupPreferencesSettings();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        imageUri = intent.getParcelableExtra("imageURI");
        starter = intent.getIntExtra("starter", -1);
        setCandidatesView(icl);
        stopSelf();
        //Start a background thread and while that thread is running change candidate view to processing...



        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    private void setupPreferencesSettings(){


        if (prefs.getBoolean("theme_blue", true)) {
            kv.setTheme("blue");
            btnArrowLeftDraw.setBackground(this.getResources().getDrawable(R.drawable.arrow_left_blue));
            btnArrowLeftClipboard.setBackground(this.getResources().getDrawable(R.drawable.arrow_left_blue));
            btnArrowLeftToolbar.setBackground(this.getResources().getDrawable(R.drawable.arrow_left_blue));


        } else if (prefs.getBoolean("theme_orange", false)) {
            kv.setTheme("orange");
            btnArrowLeftDraw.setBackground(this.getResources().getDrawable(R.drawable.arrow_left_orange));
            btnArrowLeftClipboard.setBackground(this.getResources().getDrawable(R.drawable.arrow_left_orange));
            btnArrowLeftToolbar.setBackground(this.getResources().getDrawable(R.drawable.arrow_left_orange));
        } else {
            kv.setTheme("blue");
            btnArrowLeftDraw.setBackground(this.getResources().getDrawable(R.drawable.arrow_left_blue));
            btnArrowLeftClipboard.setBackground(this.getResources().getDrawable(R.drawable.arrow_left_blue));
            btnArrowLeftToolbar.setBackground(this.getResources().getDrawable(R.drawable.arrow_left_blue));
        }

        spaceAfterDot = prefs.getBoolean("auto_space", false);

    }



    @Override
    public View onCreateCandidatesView() {
        setupCandidateViews();

        setCandidatesViewShown(true);
        btnVoice = tcl.findViewById(R.id.btn_voice);
        btnImage = tcl.findViewById(R.id.btn_img);
        btnClipboard = tcl.findViewById(R.id.btn_clipboard);
        btnDraw = tcl.findViewById(R.id.btn_draw);
        btnSettings = tcl.findViewById(R.id.btn_settings);
        btnArrowLeftClipboard = ccl.findViewById(R.id.btn_arrow_left_clip);
        btnArrowLeftDraw = dcl.findViewById(R.id.btn_arrow_left_draw);
        btnArrowLeftToolbar = tcl.findViewById(R.id.btn_arrow_left_toolbar);
        btnImageLabeling = tcl.findViewById(R.id.btn_more);
        setListeners();
        return tcl;
    }

    private void setListeners() {
        btnVoice.setOnClickListener(this);
        btnImage.setOnClickListener(this);
        btnClipboard.setOnClickListener(this);
        btnDraw.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnImageLabeling.setOnClickListener(this);
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

        timePressed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - timePressed;
        Log.d(TAG, "onKey: "+ timePressed);
        if(timePressed >= 2 && (primaryCode == (Integer.parseInt("u0071".substring(2), 16)) ||
                primaryCode == (Integer.parseInt("u0077".substring(2), 16)) ||
                primaryCode == (Integer.parseInt("u0065".substring(2), 16)) ||
                primaryCode == (Integer.parseInt("u0072".substring(2), 16)) ||
                primaryCode == (Integer.parseInt("u0074".substring(2), 16)) ||
                primaryCode == (Integer.parseInt("u0079".substring(2), 16)) ||
                primaryCode == (Integer.parseInt("u0075".substring(2), 16)) ||
                primaryCode == (Integer.parseInt("u0069".substring(2), 16)) ||
                primaryCode == (Integer.parseInt("u006F".substring(2), 16)) ||
                primaryCode == (Integer.parseInt("u0070".substring(2), 16)))){
            isKeyHeld = true;
            kv.showPopup(ic, primaryCode);
        }else{
            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    kv.deleteText(ic);
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    kv.setShifted(currentKeyboard);
                    break;
                case Keyboard.KEYCODE_DONE:
                    kv.enter(ic);
                    break;

                default:

                    if (primaryCode == 10001) {
                        kv.setKeyboard(keyboards.get(GlobalVariables.Keyboards.number.ordinal()));
                        currentKeyboard = keyboards.get(GlobalVariables.Keyboards.number.ordinal());
                        kv.invalidateAllKeys();

                    } else if (primaryCode == 10002) {
                        kv.setKeyboard(keyboards.get(GlobalVariables.Keyboards.qwerty.ordinal()));
                        currentKeyboard = keyboards.get(GlobalVariables.Keyboards.qwerty.ordinal());
                        kv.invalidateAllKeys();


                    } else if (primaryCode == 10003) {
                        kv.setKeyboard(keyboards.get(GlobalVariables.Keyboards.symbol.ordinal()));
                        currentKeyboard = keyboards.get(GlobalVariables.Keyboards.symbol.ordinal());
                        kv.invalidateAllKeys();


                    } else if (primaryCode == 10005) {
                        kv.setKeyboard(keyboards.get(GlobalVariables.Keyboards.number.ordinal()));
                        currentKeyboard = keyboards.get(GlobalVariables.Keyboards.number.ordinal());
                        kv.invalidateAllKeys();
                    }else{
                        kv.publishText(ic, primaryCode, spaceAfterDot);
                    }
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
                setCandidatesView(tcl);
                break;

            case R.id.btn_more:
                imageImplementation.checkPermissions();
                imageImplementation.start(getCurrentInputConnection(), 1);

            case R.id.btn_img:
                imageImplementation.checkPermissions();
                imageImplementation.start(getCurrentInputConnection(), 0);
                break;

            case R.id.btn_clipboard:
                startClipboard();
                break;

            case R.id.btn_draw:
                draw();
                break;

            case R.id.btn_settings:
                openSettings();
                break;

        }

    }

    private void openSettings(){
        Intent intent = new Intent(this, ImePreferences.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void startClipboard(){
        setCandidatesView(ccl);
        ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
        btnClipboardPressed = ccl.findViewById(R.id.clipboard_button);
        btnClipboardPressed.setText(item.getText().toString());
        btnClipboardPressed.setVisibility(View.VISIBLE);

        btnArrowLeftClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCandidatesView(tcl);
            }
        });

        btnClipboardPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateClipboard((AppCompatButton) v);
            }
        });

    }

    private void draw(){
        kv.setKeyboard(keyboards.get(GlobalVariables.Keyboards.drawing.ordinal()));
        currentKeyboard = keyboards.get(GlobalVariables.Keyboards.drawing.ordinal());
        kv.setPadding(0,0,0,0);
        setCandidatesView(dcl);
        initalizeRecognition();
        DigitalInkImplementation digitalInkImplementation = dcl.findViewById(R.id.drawing_canvas);
        btnClassify = dcl.findViewById(R.id.btnClassify);
        btnClear = dcl.findViewById(R.id.btnClear);
        btnClassify.setEnabled(false);
        btnClassify.setVisibility(View.INVISIBLE);
        btnClassify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ink ink = digitalInkImplementation.getInk();
                DigitalInkRecognizer recognizer = DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model).build());
                recognizer.recognize(ink)
                        .addOnSuccessListener(
                                result -> getCurrentInputConnection().commitText(result.getCandidates().get(0).getText(), 1))
                        .addOnFailureListener(
                                e -> Log.e(TAG, "Error during recognition: " + e));
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                digitalInkImplementation.clear();
            }
        });

        btnArrowLeftDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kv.setKeyboard(keyboards.get(GlobalVariables.Keyboards.qwerty.ordinal()));
                currentKeyboard = keyboards.get(GlobalVariables.Keyboards.qwerty.ordinal());
                kv.setPadding(0,0,0,8);

                setCandidatesView(tcl);
            }
        });
    }

    private void initalizeRecognition(){
        DigitalInkRecognitionModelIdentifier modelIdentifier;
        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");
            model = DigitalInkRecognitionModel.builder(modelIdentifier).build();
            remoteModelManager
                    .download(model, new DownloadConditions.Builder().build())
                    .addOnSuccessListener(aVoid -> {
                        btnClassify.setEnabled(true);
                        btnClassify.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(
                            e -> Log.e(TAG, "Error while downloading a model: " + e));

        } catch (MlKitException e) {
            Log.e(TAG, "initalizeRecognition: ", e);
        }


    }

    private void activateVoice() {
        setCandidatesView(scl);
        speechImplementation = new SpeechImplementation(this, getCurrentInputConnection(), speechTextView, scl);
        speechImplementation.checkPermissions(this);
        AppCompatButton speechPressBtn = speechImplementation.start();
        speechPressBtn.setOnClickListener(this);
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        Log.d(TAG, "onFinishInputView: ");
        if(speechImplementation != null){
            speechImplementation.destroy();
        }

    }

    private void labelImage(Uri imageURI){
        Log.d(TAG, "labelImage: ");
        ImageLabelerOptions imageLabelerOptions =
                new ImageLabelerOptions.Builder().setConfidenceThreshold(0.95f).build();

        ImageLabeler labeler = ImageLabeling.getClient(imageLabelerOptions);
        InputImage image;
        try{
            image = InputImage.fromFilePath(this, imageURI);
            labeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                @Override
                public void onSuccess(List<ImageLabel> imageLabels) {
                    getCurrentInputConnection().commitText(imageLabels.get(0).getText().toString(), 1);
                    setCandidatesView(tcl);
                    imageUri = null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: ", e);
                }
            });
        }catch(IOException e){
            e.printStackTrace();
        }

    }



    private void decodeImage(Uri imageURI){
        Log.d(TAG, "decodeImage: ");
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image;
        try{
            image = InputImage.fromFilePath(this, imageURI);
            Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    String resulText = text.getText();
                    getCurrentInputConnection().commitText(resulText, 1);
                    setCandidatesView(tcl);
                    imageUri = null;
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
        clipBoard.setPrimaryClip(clip);
        btn.setText("");
        btn.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPrimaryClipChanged() {
        Log.d("clipboard", "onPrimaryClipChanged: ");
        if(!clipBoard.getPrimaryClip().getItemAt(0).getText().toString().equals("") && currentKeyboard == keyboards.get(GlobalVariables.Keyboards.qwerty.ordinal())){
            startClipboard();
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
    public void onPress(int primaryCode) {
        timePressed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
            case Keyboard.KEYCODE_DONE:
            case Keyboard.KEYCODE_SHIFT:
                kv.setPreviewEnabled(false);
                break;

            default:

                if (primaryCode == 10001 || primaryCode == 10002 || primaryCode == 10003 || primaryCode == 10005 || primaryCode == 32) {
                    kv.setPreviewEnabled(false);
                }
                else{
                    kv.setPreviewEnabled(true);
                }
        }

    }

    @Override
    public void onRelease(int primaryCode) {

    }

}
