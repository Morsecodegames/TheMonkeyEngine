package com.morsecodegames.themonkeyengine.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.morsecodegames.themonkeyengine.renderers.GameRenderer;

/**
 * Created by Morsecode Games on 2015-10-15.
 */
public class GameView extends GLSurfaceView {

    GameRenderer mGameRenderer;
    ScaleGestureDetector mDetector;

    public GameView(Context context) {
        super(context);

        // OpenGL Settings
        setEGLContextClientVersion(2);

        // Set Detector
        mDetector = new ScaleGestureDetector(context, new ScaleListener());

        // Set Renderer
        mGameRenderer = new GameRenderer();
        setRenderer(mGameRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    // Position represents focus while twoFingers is true and previous position otherwise
    float mPreviousX;
    float mPreviousY;

    private boolean twoFingers=false;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mDetector.onTouchEvent(e);

        switch (e.getAction()&MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mPreviousX=e.getX(0);
                mPreviousY=e.getY(0);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                twoFingers=true;

                mPreviousX = (e.getX(0)+e.getX(1))/2;
                mPreviousY = (e.getY(0)+e.getY(1))/2;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                twoFingers=false;

                int remainingFinger = 1-e.getActionIndex();

                mPreviousX = e.getX(remainingFinger);
                mPreviousY = e.getY(remainingFinger);
                break;

            case MotionEvent.ACTION_MOVE:
                float tempX, tempY;
                if(twoFingers){
                    // If there are two points, track focus. Translations will be done when the focus changes
                    // For instance, when both fingers move in parallel, it should act as a pan
                    tempX = (e.getX(0)+e.getX(1))/2;
                    tempY = (e.getY(0)+e.getY(1))/2;
                }
                else {
                    // If there is only one point, track it
                    tempX = e.getX(0);
                    tempY = e.getY(0);
                }

                mGameRenderer.add(tempX - mPreviousX, tempY - mPreviousY);

                mPreviousX=tempX;
                mPreviousY=tempY;

                requestRender();
                break;
        }

        return true;

    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mGameRenderer.zoom(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            return true;
        }
    }
}
