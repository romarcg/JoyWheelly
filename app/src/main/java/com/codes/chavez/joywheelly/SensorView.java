package com.codes.chavez.joywheelly;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chavez on 21/04/16.
 */
public class SensorView extends View {
    private int [] values;
    private Paint bgPaint;
    private Paint txtPaint;
    private int currentWidthView;
    private int currentHeightView;
    private float[] colorPiece;
    private String title;


    public SensorView(Context context) {
        super(context);
        initView();
    }

    public SensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SensorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }


    public void initView() {
        title = new String();
        values = new int[8];
        bgPaint =  new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStrokeWidth(1);
        bgPaint.setAlpha(255);
        bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        txtPaint.setAlpha(255);
        txtPaint.setTextSize(20);
        txtPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        txtPaint.setColor(Color.BLACK);

        currentWidthView = 200;
        currentHeightView = 100;
        colorPiece = new float[3];
        colorPiece[0]= (float)255;
        colorPiece[1]= (float)0.8;
        colorPiece[2]= (float)0.9;
    }
    protected void onDraw(Canvas canvas) {
        canvas.save();
        //canvas.drawCircle(0, 0, 100, bgPaint);
        canvas.drawText(title, 10,20,txtPaint);

        int width=(currentWidthView-10)/8;
        int left,top,right,bottom;
        for (int i= 0; i < 8; i++){
            left=i*width;
            top= currentHeightView-values[i];//0;
            right=left+width;
            bottom=currentHeightView;//values[i];
            colorPiece[0]= (float)(200-values[i]);
            bgPaint.setColor(Color.HSVToColor(255, colorPiece));
            canvas.drawRect(left,top,right,bottom, bgPaint);
        }
        canvas.restore();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        currentWidthView = getWidth();
        currentHeightView = getHeight();
    }

    public void setSensorValues(int[] v){
        for (int i=0; i<8; i++){
            values[i] = v[i];
        }

    }

    public void setTitle(String str){
        title = str;
    }
    public void refreshView(){
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
