package com.example.accessibilitykeyboardapp;

import android.content.Context;
import android.graphics.Canvas;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

public class CustomKeyboardView extends KeyboardView {
    private boolean isShifted = false;

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void deleteText(InputConnection ic){
        ic.deleteSurroundingText(1, 0);
    }


    public void setShifted(Keyboard keyboard){
        isShifted = !isShifted;
        keyboard.setShifted(isShifted);
        invalidateAllKeys();
    }

    public void enter(InputConnection ic){
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }

    public void publishText(InputConnection ic, int primaryCode){
        char code = (char)primaryCode;
        if(Character.isLetter(code) && isShifted){
            code = Character.toUpperCase(code);
        }
        commitText(ic, String.valueOf(code));
    }

    public void changeKeyboardLayout(int layout){
        Keyboard keyboard = new Keyboard(getContext(), layout);
        setKeyboard(keyboard);
        invalidateAllKeys();
    }

    private void commitText(InputConnection ic, String code){
        ic.commitText(code,1);
    }


}
