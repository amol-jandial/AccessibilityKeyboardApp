package com.example.accessibilitykeyboardapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

public class KeyboardIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener,
        View.OnClickListener, ClipboardManager.OnPrimaryClipChangedListener {

    String pasteData = "";
    private CustomKeyboardView kv;
    private Keyboard keyboard;
    private Button btn;

    private CandidateView mCandidateView;

    @Override
    public View onCreateInputView() {
        kv = (CustomKeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.qwerty_layout);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(this);
        return kv;
    }

    @Override
    public View onCreateCandidatesView() {
        View wordBar = (LinearLayout) getLayoutInflater().inflate(R.layout.wordbar, null);
        LinearLayout ll = (LinearLayout) wordBar.findViewById(R.id.wordsLayout);
        btn = (Button) wordBar.findViewById(R.id.clipboard_button);
        btn.setOnClickListener(this);
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        setCandidatesViewShown(true);
        mCandidateView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ll.addView(mCandidateView);
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
//                else if(primaryCode == 10004){
//                    ic.commitText(pasteData, 1);
//                    ClipData clip = ClipData.newPlainText("","");
//                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    clipboardManager.setPrimaryClip(clip);
//                    Keyboard currentKeyboard = kv.getKeyboard();
//                    List<Keyboard.Key> keys = currentKeyboard.getKeys();
//                    Log.d("clipboard bitch", "clipboard: ");
//
//                    for(Keyboard.Key key : keys){
//                        if(key.label.equals(pasteData)){
//                            key.label = "";
//                            pasteData = "";
//                            kv.invalidateAllKeys();
//                        }
//                    }
//                }
                else {
                    kv.publishText(ic, primaryCode);
                }
        }
    }

    private void clipboard(){
       btn.setVisibility(View.VISIBLE);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
        Log.d("bitches", "clipboard: "+ item.getText());
        if(item.getText() != null){
            btn.setText(item.getText());
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
        Button btn = (Button) v;
        String text = (String) btn.getText();
        getCurrentInputConnection().commitText(text, 1);
        ClipData clip = ClipData.newPlainText("","");
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clip);
        btn.setVisibility(View.GONE);
    }

    @Override
    public void onPrimaryClipChanged() {
        Log.d("clipboard", "onPrimaryClipChanged: ");
        clipboard();
    }
}
