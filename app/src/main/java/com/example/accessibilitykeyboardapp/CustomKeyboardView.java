package com.example.accessibilitykeyboardapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.List;

public class CustomKeyboardView extends KeyboardView {
    private boolean isShifted = false;
    private Drawable npd;
    private String theme;
    private Keyboard keyboard;
    private int height, width;
    private AppCompatButton popupBtn;



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



    public void showPopup(InputConnection inputConnection, int primaryCode){
        PopupClass popupClass = new PopupClass();
        popupClass.showPopupWindow(this, inputConnection, primaryCode);
        }



    public class PopupClass{
        public void showPopupWindow(final View view, InputConnection inputConnection, int primaryCode){
            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_layout, null);
            View outside = popupView.findViewById(R.id.outside_popup_btn);
            boolean focusable = false;
            PopupWindow popupWindow = new PopupWindow(popupView, view.getWidth(), view.getHeight(), focusable);
            popupWindow.setClippingEnabled(false);
            popupBtn = popupView.findViewById(R.id.popup_btn);
            List<Keyboard.Key> keys = getKeyboard().getKeys();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            for(Keyboard.Key key : keys){
                if(key.codes[0] == primaryCode){
                    popupBtn.setText(key.popupCharacters.toString());
                    params.setMargins(key.x + 10, key.y + (key.height/2), 0, 0);
                    popupBtn.setLayoutParams(params);
                    popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
                }
            }

            popupBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    inputConnection.commitText(popupBtn.getText().toString(), 1);
                    popupWindow.dismiss();

                }
            });

            outside.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });

//            view.setOnTouchListener(new OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    popupWindow.dismiss();
//                    return true;
//                }
//            });
        }
    }


    @SuppressLint("DrawAllocation")
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
                    paint.setTextSize(50);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    canvas.drawText(key.label.toString(), key.x + (key.width / 2), key.y + (key.height / 2) + 20,
                            paint);
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
                    width = key.width;
                    height = key.height;
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



    public void publishText(InputConnection ic, int primaryCode,
                            boolean spaceAfterDot){

            char code = (char)primaryCode;
            if((Character.isLetter(code) && isShifted)){
                code = Character.toUpperCase(code);
            }
            commitText(ic, String.valueOf(code), 1);

            if(primaryCode == 46 && spaceAfterDot){
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
            }

    }

    private void commitText(InputConnection ic, String code, int pos){
        ic.commitText(code,pos);
    }

}
