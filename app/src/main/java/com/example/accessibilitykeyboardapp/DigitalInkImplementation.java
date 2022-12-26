package com.example.accessibilitykeyboardapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.digitalink.Ink;

public class DigitalInkImplementation extends View {

    private Path path = new Path();
    private Ink.Stroke.Builder strokeBuilder;
    private Ink.Builder inkBuilder = Ink.builder();
    private float touchX, touchY, currentX, currentY;
    private long touchT;
    private int touchTolerance = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private Canvas extraCanvas;
    private Bitmap extraBitmap;
    private Paint paint;



    public DigitalInkImplementation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
        if(extraBitmap != null){
            extraBitmap.recycle();
        }else{
            extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            extraCanvas = new Canvas(extraBitmap);
            extraCanvas.drawColor(Color.WHITE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(extraBitmap, 0f, 0f, null);
    }

    public void init(){
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(4f);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX();
        touchY = event.getY();
        touchT = System.currentTimeMillis();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }
        return true;
    }

    private void touchStart(){
        path.reset();
        path.moveTo(touchX, touchY);

        currentX = touchX;
        currentY = touchY;
        strokeBuilder = Ink.Stroke.builder();
        strokeBuilder.addPoint(Ink.Point.create(currentX, currentY, touchT));
    }

    private void touchMove(){
        float dx = Math.abs(touchX - currentX);
        float dy = Math.abs(touchY - currentY);

        if(dx >= touchTolerance || dy >= touchTolerance){
            path.quadTo(currentX, currentY, (touchX + currentX)/2, (touchY + currentY)/2);
            currentX = touchX;
            currentY = touchY;
            strokeBuilder.addPoint(Ink.Point.create(touchX, touchY, touchT));
            extraCanvas.drawPath(path, paint);
        }
        invalidate();
    }

    private void touchUp(){
        strokeBuilder.addPoint(Ink.Point.create(touchX, touchY, touchT));
        inkBuilder.addStroke(strokeBuilder.build());
        path.reset();
    }

    public Ink getInk(){
        Ink ink = inkBuilder.build();
        return ink;
    }

    public void clear(){
        path.reset();
        inkBuilder = Ink.builder();
        extraCanvas.drawColor(Color.WHITE);
        invalidate();
    }


}
