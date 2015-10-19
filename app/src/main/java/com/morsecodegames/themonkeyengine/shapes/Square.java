package com.morsecodegames.themonkeyengine.shapes;

import android.opengl.GLES20;

import com.morsecodegames.themonkeyengine.renderers.GameRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Morsecode Games on 2015-10-16.
 */
public class Square {
    static float coords[] = {
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
    };

    static final int COORDS_PER_VERTEX = 3;

    private final String vertexShaderCode =
            "" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "   gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "" +
                    "uniform mat4 uMVPMatrix" +
                    "void main() {" +
                    "   gl_FragColor = vev4(0.0, 1.0, 0.0, 1.0);" +
                    "}";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;

    private final short drawOrder[] = {0, 1, 2, 0, 2, 3};
    private final int vertexStride = COORDS_PER_VERTEX * 4;

    public Square() {
        // Shape coordinates
        ByteBuffer scb = ByteBuffer.allocateDirect(coords.length * 4);
        scb.order(ByteOrder.nativeOrder());
        vertexBuffer = scb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        // Draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Prepare shaders
        int vertexShader = GameRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GameRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Create OpenGL program
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL
        GLES20.glUseProgram(mProgram);

        // Get handles
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass to shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Add array of vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Draw
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
