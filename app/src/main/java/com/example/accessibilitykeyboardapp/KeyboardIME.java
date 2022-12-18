package com.example.accessibilitykeyboardapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyboardIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener
         {

    private CustomKeyboardView kv;
    private Keyboard keyboard;
    String pasteData = "";
//             ClipboardManager.OnPrimaryClipChangedListener
    @Override
    public View onCreateInputView() {
        kv = (CustomKeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.qwerty_layout);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

//        ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//        clipBoard.addPrimaryClipChangedListener(this);
//        clipboard();
        return kv;
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

        switch(primaryCode){
            case Keyboard.KEYCODE_DELETE :
                kv.deleteText(ic);
                break;
            case Keyboard.KEYCODE_SHIFT:
                kv.setShifted(keyboard);
                break;
            case Keyboard.KEYCODE_DONE:
                kv.enter(ic);
                break;

            default:

                if(primaryCode == 10001){
                   kv.changeKeyboardLayout(R.xml.number_symbols_layout);
                }
                else if(primaryCode == 10002){
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
                else{
                    kv.publishText(ic, primaryCode);
                }
        }
    }

//    private void clipboard(){
//        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//        ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
//        pasteData = (String) item.getText();
//        Keyboard currentKeyboard = kv.getKeyboard();
//        List<Keyboard.Key> keys = currentKeyboard.getKeys();
//        Log.d("clipboard bitch", "clipboard Data: "+pasteData);
//
//        for(Keyboard.Key key : keys){
//            if(key.label.equals("")){
//                key.label = pasteData;
//                kv.invalidateAllKeys();
//            }
//        }
//
//    }







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

//    @Override
//    public void onPrimaryClipChanged() {
//        Log.d("clipboard", "onPrimaryClipChanged: ");
//        clipboard();
//    }
}
