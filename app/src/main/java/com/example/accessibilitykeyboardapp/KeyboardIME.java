package com.example.accessibilitykeyboardapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.Collection;

public class KeyboardIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener,
        View.OnClickListener, ClipboardManager.OnPrimaryClipChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    String pasteData = "";
    private CustomKeyboardView kv;
    private int isLetterPressed = 0;
    private boolean firstShiftPressed = false;
    private Keyboard keyboard;
    private AppCompatButton btn;
    private CandidateView mCandidateView;
    SharedPreferences prefs;
    private static final String TAG  = "bitches";
    private boolean spaceAfterDot;

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
        View wordBar = (LinearLayout) getLayoutInflater().inflate(R.layout.wordbar, null);
        LinearLayout ll = (LinearLayout) wordBar.findViewById(R.id.wordsLayout);
        btn = wordBar.findViewById(R.id.clipboard_button);
        btn.setOnClickListener(this);
//        Log.d(TAG, "onCreateCandidatesView: ");
//        mCandidateView = new CandidateView(this);
//        mCandidateView.setService(this);
        setCandidatesViewShown(true);
//        mCandidateView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        ll.addView(mCandidateView);
        clipboard();
        return ll;
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

        String text = (String) btn.getText();

        getCurrentInputConnection().commitText(text, 1);
        ClipData clip = ClipData.newPlainText("","");
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clip);
    }

    @Override
    public void onPrimaryClipChanged() {
        Log.d("clipboard", "onPrimaryClipChanged: ");
        clipboard();
    }
}
