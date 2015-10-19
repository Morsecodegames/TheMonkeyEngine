package com.morsecodegames.themonkeyengine.renderers;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.morsecodegames.themonkeyengine.shapes.Fractal;
import com.morsecodegames.themonkeyengine.shapes.Square;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Morsecode Games on 2015-10-15.
 */
public class GameRenderer implements GLSurfaceView.Renderer {

    private Fractal mFractal;
    private Square mSquare;

    private int mWidth, mHeight;
    private double mRatio, mX = 1.0f, mY = 1.0f, mZoom = 0.5f;

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        // Compile the shader code into the shader
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mFractal = new Fractal();
        mSquare = new Square();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
        mRatio = (double) width/height;
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float[] mvpMatrix = new float[]{
                (float) (-1.0/mZoom),   0.0f,                           0.0f,    0.0f,
                0.0f,                   (float) (1.0/(mZoom*mRatio)),   0.0f,    0.0f,
                0.0f,                   0.0f,                           1.0f,    0.0f,
                (float) -mX,            (float) -mY,                    0.0f,    1.0f
        };

//        mFractal.draw(mvpMatrix);
        mSquare.draw(mvpMatrix);
    }

    public void add(double dx, double dy) {
        //Both are scaled by mHeight, because the ratio is taken into account by the translation matrix
        mX+=dx/(mZoom*mHeight);
        mY+=dy/(mZoom*mHeight);
    }

    private double zoomIncrease = 1.5;

    public void zoom(double scaleFactor, double x, double y) {
        scaleFactor = (scaleFactor-1)*zoomIncrease+1;
        // Default zoom is to top center of the screen. Thus, changes should be zeroed at that point
        x-=mWidth/2;

        //Note that, because mZoom changse in the add method, there is an implicit division by log(2) hidden through limit discrete summation/integration
        double scale = Math.log(scaleFactor);

        //Move towards focus
        add(-scale*x,-scale*y);

        //add(scale)
        mZoom*=scaleFactor;
    }
}
