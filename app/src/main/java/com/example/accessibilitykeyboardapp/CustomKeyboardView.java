package com.example.accessibilitykeyboardapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import androidx.core.content.res.ResourcesCompat;

import java.util.List;

public class CustomKeyboardView extends KeyboardView implements View.OnTouchListener {
    private boolean isShifted = false;
    private Drawable npd;
    private String theme;


    public CustomKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setTheme(String theme){
        this.theme = theme;
        invalidateAllKeys();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.number_on_top, null));
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for(Keyboard.Key key : keys) {
            if (key.codes[0] == -1) {
                if (isShifted) {
                    if (theme.equals("blue")) {
                        npd = (Drawable) getContext().getResources().getDrawable(R.drawable.shift_on_blue);
                    } else if (theme.equals("orange")) {
                        npd = (Drawable) getContext().getResources().getDrawable(R.drawable.shift_on_orange);
                    }
                } else {
                    if (theme.equals("blue")) {
                        npd = (Drawable) getContext().getResources().getDrawable(R.drawable.shift_off_blue);
                    } else if (theme.equals("orange")) {
                        npd = (Drawable) getContext().getResources().getDrawable(R.drawable.shift_off_orange);
                    }
                }
                npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                npd.draw(canvas);
            }
            if (key.codes[0] == -5) {
                if (theme.equals("blue")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.delete_blue);
                } else if (theme.equals("orange")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.delete_orange);
                }
                npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                npd.draw(canvas);
            }

            if (key.codes[0] == -4) {
                if (theme.equals("blue")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.enter_blue);
                } else if (theme.equals("orange")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.enter_orange);
                }
                npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                npd.draw(canvas);
            }

            if (key.codes[0] == 10001 || key.codes[0] == 10005) {
                if (theme.equals("blue")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.numpad_blue);
                } else if (theme.equals("orange")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.numpad_orange);
                }
                npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                npd.draw(canvas);
            }

            if (key.codes[0] == 10002) {
                if (theme.equals("blue")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.alphabet_blue);
                } else if (theme.equals("orange")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.alphabet_orange);
                }
                    npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                    npd.draw(canvas);
            }

            if (key.codes[0] == 10003) {
                if (theme.equals("blue")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.symbols_blue);
                } else if (theme.equals("orange")) {
                    npd = (Drawable) getContext().getResources().getDrawable(R.drawable.symbols_orange);
                }
                npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                npd.draw(canvas);
            }

                if (key.codes[0] == 44 || key.codes[0] == 46
                        || key.codes[0] == (Integer.parseInt("u003C".substring(2), 16))
                        || key.codes[0] == (Integer.parseInt("u003E".substring(2), 16))
                ) {
                    if (theme.equals("blue")) {
                        npd = (Drawable) getContext().getResources().getDrawable(R.drawable.special_key_background_blue);
                    } else if (theme.equals("orange")) {
                        npd = (Drawable) getContext().getResources().getDrawable(R.drawable.special_key_background_orange);
                    }
                    npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                    npd.draw(canvas);
                    paint.setColor(ResourcesCompat.getColor(getResources(), R.color.keyboard_text, null));
                    paint.setTextSize(30);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    canvas.drawText(key.label.toString(), key.x + (key.width / 2), key.y + (key.height / 2) + 10, paint);
                }

                if (key.codes[0] == (Integer.parseInt("u0071".substring(2), 16)) ||
                        key.codes[0] == (Integer.parseInt("u0077".substring(2), 16)) ||
                        key.codes[0] == (Integer.parseInt("u0065".substring(2), 16)) ||
                        key.codes[0] == (Integer.parseInt("u0072".substring(2), 16)) ||
                        key.codes[0] == (Integer.parseInt("u0074".substring(2), 16)) ||
                        key.codes[0] == (Integer.parseInt("u0079".substring(2), 16)) ||
                        key.codes[0] == (Integer.parseInt("u0075".substring(2), 16)) ||
                        key.codes[0] == (Integer.parseInt("u0069".substring(2), 16)) ||
                        key.codes[0] == (Integer.parseInt("u006F".substring(2), 16)) ||
                        key.codes[0] == (Integer.parseInt("u0070".substring(2), 16))
                ) {

                    canvas.drawText(key.popupCharacters.toString(), key.x + (key.width - 10), key.y + 20, paint);
                }
            }
        }


    public void deleteText(InputConnection ic){
        ic.deleteSurroundingText(1, 0);
    }


    public void setShifted(Keyboard keyboard){
        Log.d("shift", "setShifted: ");
        isShifted = !isShifted;
        keyboard.setShifted(isShifted);
        invalidateAllKeys();
    }

    public void enter(InputConnection ic){
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }

    public int publishText(InputConnection ic, int primaryCode, Keyboard keyboard, boolean firstShiftPressed,
                           boolean spaceAfterDot){
        char code = (char)primaryCode;
        if((Character.isLetter(code) && isShifted)){
            code = Character.toUpperCase(code);
        }
        commitText(ic, String.valueOf(code), 1);

        if(primaryCode == 46 && spaceAfterDot){
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        }
        if(Character.isLetter(code)){
            Log.d("shifting", "letter pressed");
            return 1;
        }
        return -1;
    }

    public void changeKeyboardLayout(int layout){
        Keyboard keyboard = new Keyboard(getContext(), layout);
        setKeyboard(keyboard);
        invalidateAllKeys();
    }

    private void commitText(InputConnection ic, String code, int pos){
        ic.commitText(code,pos);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
