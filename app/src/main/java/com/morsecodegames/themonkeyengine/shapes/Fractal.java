package com.morsecodegames.themonkeyengine.shapes;

import android.opengl.GLES20;

import com.morsecodegames.themonkeyengine.renderers.GameRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Morsecode Games on 2015-10-15.
 */
public class Fractal {

    static float coords[] = {
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
    };

    static final int COORDS_PER_VERTEX = 3;

    private final String vertexShaderCode =
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "   gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
                    "precision highp float;" +
                    "uniform mat4 uMVPMatrix;" +
                    "void main() {" +
                    // Transform given position to coordinate space
                    "  vec2 p = (uMVPMatrix * vec4(gl_PointCoord,0,1)).xy;" +
                    "  vec2 c = p;" +
                    // Set default color to black in HSV
                    "  vec3 color=vec3(0.0,0.0,0.0);" +
                    // Use 200 as an arbitrary limit. The higher the number, the slower and more detailed it will be
                    "  for(int i=0;i<200;i++){" +
                    // Perform z = z^2 + c using p, which represents the real and imaginary parts of z
                    "  	   p= vec2(p.x*p.x-p.y*p.y,2.0*p.x*p.y)+c;" +
                    "      if (dot(p,p)>4.0){" +
                    // colorRegulator continuously increases smoothly by 1 for every additional step it takes to break
                    "         float colorRegulator = float(i-1)-log(log(length(p)))/log(2.0);" +
                    // Set color to a cycling color scheme using the smooth number
                    "         color = vec3(0.95 + .012*colorRegulator , 1.0, .2+.4*(1.0+sin(.3*colorRegulator)));"+
                    "         break;" +
                    "      }" +
                    "  }" +
                    //Convert HSV to RGB. Algorithm from https://gist.github.com/patriciogonzalezvivo/114c1653de9e3da6e1e3
                    "  vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);" +
                    "  vec3 m = abs(fract(color.xxx + K.xyz) * 6.0 - K.www);" +
                    "  gl_FragColor.rgb = color.z * mix(K.xxx, clamp(m - K.xxx, 0.0, 1.0), color.y);" +
                    "  gl_FragColor.a=1.0;" +
                    "}";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;

    private final short drawOrder[] = {0, 1, 2, 0, 2, 3};
    private final int vertexStride = COORDS_PER_VERTEX * 4;

    public Fractal() {
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
