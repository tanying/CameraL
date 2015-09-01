package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by sichao.hu on 8/21/15.
 */
public class EvoSlider extends View{

    private static final int TOUCH_GAP=180;
    private static final int SLIDER_LENGTH=300;
    private static final float BUTTON_RADIUS=5;
    private static final float FRACTION=1.0f;

    public EvoSlider(Context context ,AttributeSet attrs){
        super(context,attrs);
        this.setVisibility(View.GONE);
    }


    public EvoSlider(Context context) {
        super(context);
        this.setVisibility(View.GONE);
    }

    public interface EVOChangedListener{
        public void onEVOChanged(int value);
    }


    EVOChangedListener mEVOListener;
    public void setEvoListener(EVOChangedListener listener){
        mEVOListener=listener;
    }

    private float mStep=0;
    public void slideTo(float deltaX,float deltaY){
        float deltaStep=0;
        if(ORIENTATION_PORTRAIT==mOrientation){
            deltaStep=deltaY*FRACTION;
        }else{
            deltaStep=deltaX*FRACTION;
        }
        float expectValue=mStep+deltaStep;
        if(expectValue> mMaxStepForSlider ||expectValue< mMinStepForSlider){
            return;
        }
        mStep+=deltaStep;
        if(mEVOListener!=null){
            float realValue=((mMaxEvoStep - mMinEvoStep)*(mStep-mOrigin)/(mMaxStepForSlider - mMinStepForSlider));//float metric=(maxValue-minValue)/(maxStep-minStep)

            int totalSteps=(int)realValue;

            float tolerance = realValue - (float)totalSteps;
            if (tolerance >= 0.5) {
                totalSteps += 1;
            }else if(tolerance<=-0.5){
                totalSteps-=1;
            }
            if(totalSteps> mMaxEvoStep){
                totalSteps=mMaxEvoStep;
            }
            if(totalSteps<mMinEvoStep){
                totalSteps=mMinEvoStep;
            }
            mEVOListener.onEVOChanged(totalSteps);
        }

        invalidate();
    }

    private int mMaxStepForSlider =0;
    private int mMinStepForSlider =0;
    private int mMaxEvoStep =0;
    private int mMinEvoStep =0;
    private float mOrigin=0;
    public void setValueBound(int maxEVO,int minEVO){
        mMaxStepForSlider =SLIDER_LENGTH;
        mMinStepForSlider =0;
        mMaxEvoStep =maxEVO;
        mMinEvoStep =minEVO;
        mOrigin=SLIDER_LENGTH*Math.abs(minEVO)/(Math.abs(maxEVO)+Math.abs(minEVO));
    }

    private float mEvoStep;
    public void setValueBound(int maxEvo,int minEvo,float evoStep){
        setValueBound(maxEvo,minEvo);
        mEvoStep=evoStep;
    }

    public void resetSlider(int value){
        if(value==0) {
            mStep = mOrigin;
        }else{
            mStep=SLIDER_LENGTH*(value- mMinEvoStep)/(mMaxEvoStep - mMinEvoStep);
        }
        if(mEVOListener!=null){
            mEVOListener.onEVOChanged(value);
        }
    }

    public void resetSlider(){
        resetSlider(0);
    }

    private float mX;
    private float mY;
    public synchronized void setCoord(float x,float y){
        mX=x;
        mY=y;
        invalidate();
    }


    private int mBoundRight=0;
    private int mBoundBottom=0;
    public synchronized void setBound(int right,int bottom){
        mBoundRight=right;
        mBoundBottom=bottom;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            if(event.getHistorySize()==0){
                return true;
            }else{
                float startX=event.getHistoricalX(0);
                float endX=event.getX();
                float startY=event.getHistoricalY(0);
                float endY=event.getY();
                this.slideTo(endX-startX, startY-endY);//the top left of view coordinate is (0,0) , so once it's coming close to bottom , the coordinate value of Y is becoming larger
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private Paint mPaint;
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mPaint==null){
            mPaint=new Paint();
            mPaint.setColor(Color.YELLOW);
            mPaint.setStyle(Style.FILL);
            mPaint.setStrokeWidth(1.0f);
        }
        PointF start=getSliderStart();
        PointF end=getSliderEnd();
        PointF buttonCoord=getSliderButtonCoord();
        canvas.drawLine(start.x, start.y, end.x, end.y, mPaint);
        canvas.drawCircle(buttonCoord.x, buttonCoord.y, BUTTON_RADIUS, mPaint);

    }

    private static final int ORIENTATION_PORTRAIT=0;
    private static final int ORIENTATION_LANDSCAPE=1;
    private int mOrientation=ORIENTATION_PORTRAIT;
    private PointF getSliderCenterCoord(){//Only used in onDraw , which is already synchronized under this class
        PointF p=new PointF();
        if(ORIENTATION_PORTRAIT==mOrientation){
            if(mX+TOUCH_GAP>mBoundRight){
                p.x=mX-TOUCH_GAP;
            }else{
                p.x=mX+TOUCH_GAP;
            }
            p.y=(int)mY;
        }else{
            if(mY+TOUCH_GAP>mBoundBottom){
                p.y=mY+TOUCH_GAP;
            }else{
                p.y=mY-TOUCH_GAP;
            }
            p.x=mX;
        }
        return p;
    }

    private PointF getSliderStart(){
        PointF center=getSliderCenterCoord();
        PointF startPoint=new PointF();
        if(ORIENTATION_PORTRAIT==mOrientation){
            startPoint.y=center.y-SLIDER_LENGTH/2;
            startPoint.x=center.x;
        }else{
            startPoint.x=center.x+SLIDER_LENGTH/2;
            startPoint.y=center.y;
        }
        return startPoint;
    }


    private PointF getSliderEnd(){
        PointF center=getSliderCenterCoord();
        PointF endPoint=new PointF();
        if(ORIENTATION_PORTRAIT==mOrientation){
            endPoint.y=center.y+SLIDER_LENGTH/2;
            endPoint.x=center.x;
        }else{
            endPoint.x=center.x-SLIDER_LENGTH/2;
            endPoint.y=center.y;
        }
        return endPoint;
    }

    private PointF getSliderButtonCoord(){
        PointF end=getSliderEnd();
        PointF buttonCoord=new PointF();
        if(ORIENTATION_PORTRAIT==mOrientation){
            buttonCoord.x=end.x;
            buttonCoord.y=end.y-mStep;
        }else{
            buttonCoord.y=end.y;
            buttonCoord.x=end.x-mStep;
        }
        return buttonCoord;
    }

}
