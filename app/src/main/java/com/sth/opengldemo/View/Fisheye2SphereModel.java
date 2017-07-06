package com.sth.opengldemo.View;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.Log;

import com.sth.opengldemo.Util.Parameters;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.sth.opengldemo.Util.ShaderUtils.checkGlError;

/**
 * Created by Fishby on 2017/5/23.
 */

public class Fisheye2SphereModel {

    private static final int sPositionDataSize = 3;
    private static final int sTextureCoordinateDataSize = 2;
    private static final int sTextureAlphaDataSize = 1;

    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mTexCoordinateBuffer_left;
    private FloatBuffer mTexCoordinateBuffer_right;
    private FloatBuffer mTexAlpha;
    private ShortBuffer indexBuffer;
    private int mNumIndices;

    /*
        (0, 1)--------(1, 1)
          |             |
          |             |
          |             |
        (0, 0)--------(1, 0)
    */
    /*
    private float radius_left = 461.5f / 2;
    private float center_x_left = 237.0f;
    private float center_y_left = height - 243.0f;

    private float radius_right = 457.5f / 2;
    private float center_x_right = 722.0f;
    private float center_y_right = height - 251.0f;
    */
    /*
    private float radius_left = 453.0f / 2;
    private float center_x_left = 233.5f;
    private float center_y_left = height - 255.5f;

    private float radius_right = 453.0f / 2;
    private float center_x_right = 711.5f;
    private float center_y_right = height - 258.5f;
    */
    private float width = Parameters.reference_width;
    private float height = Parameters.reference_height;

    private float radius_left = Parameters.radius_left;
    private float center_x_left = Parameters.center_x_left;
    private float center_y_left = height - Parameters.center_y_left;

    private float radius_right = Parameters.radius_right;
    private float center_x_right = Parameters.center_x_right;
    private float center_y_right = height - Parameters.center_y_right;

    final float PI = (float) Math.PI;
    final float M2_PI = (float) (Math.PI * 2);

    private final int NUM_ANGLE = 101;
    private final float DIF_ANGLE = 1.005f*PI/180;
    //private final float REAL_ANGLE[] = { 0f, 0.0366519142918809f, 0.0733038285837619f, 0.109955742875643f, 0.146607657167524f, 0.183259571459405f, 0.219911485751286f, 0.256563400043166f, 0.293215314335047f, 0.329867228626928f, 0.366519142918809f, 0.403171057210690f, 0.439822971502571f, 0.476474885794452f, 0.513126800086333f, 0.549778714378214f, 0.586430628670095f, 0.623082542961976f, 0.659734457253857f, 0.696386371545738f, 0.733038285837618f, 0.769690200129499f, 0.806342114421380f, 0.842994028713261f, 0.879645943005142f, 0.916297857297023f, 0.952949771588904f, 0.989601685880785f, 1.02625360017267f, 1.06290551446455f, 1.09955742875643f, 1.13620934304831f, 1.17286125734019f, 1.20951317163207f, 1.24616508592395f, 1.28281700021583f, 1.31946891450771f, 1.35612082879959f, 1.39277274309148f, 1.42942465738336f, 1.46607657167524f, 1.50272848596712f, 1.53938040025900f, 1.57603231455088f, 1.61268422884276f, 1.64933614313464f, 1.68598805742652f, 1.72263997171840f, 1.75929188601028f, 1.79594380030217f, 1.83259571459405f };
    //private final float REAL_HEIGHT[] = { 0f, 0.0318345600000000f, 0.0636742400000000f, 0.0955241400000000f, 0.127389280000000f, 0.159274580000000f, 0.191184850000000f, 0.223124740000000f, 0.255098740000000f, 0.287111130000000f, 0.319165990000000f, 0.351267180000000f, 0.383418290000000f, 0.415622630000000f, 0.447883190000000f, 0.480202530000000f, 0.512582750000000f, 0.545025320000000f, 0.577530950000000f, 0.610099340000000f, 0.642728960000000f, 0.675416660000000f, 0.708157340000000f, 0.740943370000000f, 0.773764130000000f, 0.806605290000000f, 0.839448120000000f, 0.872268670000000f, 0.905036920000000f, 0.937715830000000f, 0.970260430000000f, 1.00261687000000f, 1.03472151000000f, 1.06650010000000f, 1.09786711000000f, 1.12872524000000f, 1.15896514000000f, 1.18846555000000f, 1.21709368000000f, 1.24470607000000f, 1.27114991000000f, 1.29626468000000f, 1.31988420000000f, 1.34183883000000f, 1.36195771000000f, 1.38007068000000f, 1.39600971000000f, 1.40960951000000f, 1.42070724000000f, 1.42914121000000f, 1.43474885000000f };
    private final float REAL_ANGLE[] = {
            0f,
            1.005f,
            2.01f,
            3.015f,
            4.02f,
            5.025f,
            6.03f,
            7.035f,
            8.04f,
            9.045f,
            10.05f,
            11.055f,
            12.06f,
            13.065f,
            14.07f,
            15.075f,
            16.08f,
            17.085f,
            18.09f,
            19.095f,
            20.1f,
            21.105f,
            22.11f,
            23.115f,
            24.12f,
            25.125f,
            26.13f,
            27.135f,
            28.14f,
            29.145f,
            30.15f,
            31.155f,
            32.16f,
            33.165f,
            34.17f,
            35.175f,
            36.18f,
            37.185f,
            38.19f,
            39.195f,
            40.2f,
            41.205f,
            42.21f,
            43.215f,
            44.22f,
            45.225f,
            46.23f,
            47.235f,
            48.24f,
            49.245f,
            50.25f,
            51.255f,
            52.26f,
            53.265f,
            54.27f,
            55.275f,
            56.28f,
            57.285f,
            58.29f,
            59.295f,
            60.3f,
            61.305f,
            62.31f,
            63.315f,
            64.32f,
            65.325f,
            66.33f,
            67.335f,
            68.34f,
            69.345f,
            70.35f,
            71.355f,
            72.36f,
            73.365f,
            74.37f,
            75.375f,
            76.38f,
            77.385f,
            78.39f,
            79.395f,
            80.4f,
            81.405f,
            82.41f,
            83.415f,
            84.42f,
            85.425f,
            86.43f,
            87.435f,
            88.44f,
            89.445f,
            90.45f,
            91.455f,
            92.46f,
            93.465f,
            94.47f,
            95.475f,
            96.48f,
            97.485f,
            98.49f,
            99.495f,
            100.5f
    } ;
    private final float REAL_HEIGHT[] = {
        0f,
                0.01450533f,
                0.02901007f,
                0.04351363f,
                0.05801541f,
                0.07251481f,
                0.08701125f,
                0.10150413f,
                0.11599286f,
                0.13047683f,
                0.14495546f,
                0.15942814f,
                0.17389428f,
                0.18835328f,
                0.20280453f,
                0.21724742f,
                0.23168136f,
                0.24610573f,
                0.26051991f,
                0.27492329f,
                0.28931525f,
                0.30369517f,
                0.3180624f,
                0.33241631f,
                0.34675626f,
                0.36108159f,
                0.37539164f,
                0.38968574f,
                0.4039632f,
                0.41822334f,
                0.43246544f,
                0.44668878f,
                0.46089263f,
                0.47507622f,
                0.48923878f,
                0.50337951f,
                0.51749759f,
                0.53159218f,
                0.5456624f,
                0.55970734f,
                0.57372606f,
                0.5877176f,
                0.60168093f,
                0.615615f,
                0.62951871f,
                0.64339091f,
                0.65723039f,
                0.67103589f,
                0.68480608f,
                0.69853959f,
                0.71223494f,
                0.7258906f,
                0.73950495f,
                0.75307629f,
                0.76660283f,
                0.78008265f,
                0.79351378f,
                0.80689409f,
                0.82022135f,
                0.83349323f,
                0.84670723f,
                0.85986073f,
                0.87295098f,
                0.88597505f,
                0.89892989f,
                0.91181225f,
                0.92461872f,
                0.93734573f,
                0.94998952f,
                0.96254613f,
                0.97501142f,
                0.98738107f,
                0.99965055f,
                1.01181512f,
                1.02386987f,
                1.03580969f,
                1.04762925f,
                1.05932307f,
                1.07088547f,
                1.08231061f,
                1.09359247f,
                1.10472492f,
                1.11570168f,
                1.12651637f,
                1.13716251f,
                1.14763359f,
                1.15792303f,
                1.16802428f,
                1.1779308f,
                1.18763612f,
                1.19713387f,
                1.20641782f,
                1.21548193f,
                1.22432037f,
                1.23292755f,
                1.24129822f,
                1.24942742f,
                1.25731061f,
                1.26494361f,
                1.27232271f,
                1.27944465f
    };
    private final float COMBINE_ANGLE = Parameters.combine_angle; // 2 * COMBINE_ANGLE

    private final float FOV=PI*210/180;
    private PointF getLongLatPointF_left(float t, float p)
    {
        PointF result = null;
        float x, y, z, r;
        float theta = M2_PI * (t - 0.5f);
        float phi = PI * (p - 0.5f);

        x = (float) (Math.cos(phi) * Math.sin(theta));
        y = (float) (Math.cos(phi) * Math.cos(theta));
        z = (float) Math.sin(phi);

        theta = (float) Math.atan2(z, x);// + PI;//加了180度
        phi = (float) Math.atan2(Math.sqrt(x * x + z * z), y);

        int min_n = (int)(phi / DIF_ANGLE);
        int max_n = min_n + 1;

        if (max_n >= NUM_ANGLE)
            return null;

        float tmp = REAL_HEIGHT[min_n] + (REAL_HEIGHT[max_n] - REAL_HEIGHT[min_n]) / DIF_ANGLE * (phi - REAL_ANGLE[min_n]*PI/180);
        r = radius_left / REAL_HEIGHT[NUM_ANGLE - 1] * tmp;
        //r=radius_left * phi / FOV;
        result = new PointF(
                (r * (float)Math.cos(theta) + center_x_left) / width,
                (r * (float)Math.sin(theta) + center_y_left) / height
        );
        return result;
    }

    private PointF getLongLatPointF_right(float t, float p)
    {
        PointF result = null;
        float x, y, z, r;

        if(t<0.5) {
            float theta = M2_PI * (t);
            float phi = PI * (p - 0.5f);

            x = (float) (Math.cos(phi) * Math.sin(theta));
            y = (float) (Math.cos(phi) * Math.cos(theta));
            z = (float) Math.sin(phi);

            theta = (float) Math.atan2(z, x);
            phi = (float) Math.atan2(Math.sqrt(x * x + z * z), y);

            int min_n = (int)(phi / DIF_ANGLE);
            int max_n = min_n + 1;

            if (max_n >= NUM_ANGLE)
                return null;

            float tmp = REAL_HEIGHT[min_n] + (REAL_HEIGHT[max_n] - REAL_HEIGHT[min_n]) / DIF_ANGLE * (phi - REAL_ANGLE[min_n]*PI/180);
            r = radius_right / REAL_HEIGHT[NUM_ANGLE - 1] * tmp;
            //r=radius_right * phi / FOV;
            result = new PointF(
                    (r * (float)Math.cos(theta) + center_x_right) / width,
                    (r * (float)Math.sin(theta) + center_y_right) / height
            );
        }else if(t>=0.5){
            float theta = M2_PI * (t - 1.0f);
            float phi = PI * (p - 0.5f);

            x = (float) (Math.cos(phi) * Math.sin(theta));
            y = (float) (Math.cos(phi) * Math.cos(theta));
            z = (float) Math.sin(phi);

            theta = (float) Math.atan2(z, x);
            phi = (float) Math.atan2(Math.sqrt(x * x + z * z), y);

            int min_n = (int)(phi / DIF_ANGLE);
            int max_n = min_n + 1;

            if (max_n >= NUM_ANGLE)
                return null;

            float tmp = REAL_HEIGHT[min_n] + (REAL_HEIGHT[max_n] - REAL_HEIGHT[min_n]) / DIF_ANGLE * (phi - REAL_ANGLE[min_n]*PI/180);
            r = radius_right / REAL_HEIGHT[NUM_ANGLE - 1] * tmp;

            result = new PointF(
                    (r * (float)Math.cos(theta) + center_x_right) / width,
                    (r * (float)Math.sin(theta) + center_y_right) / height
            );
        }
        return result;
    }

    /**
     * modified from hzqiujiadi on 16/1/8.
     * original source code:
     * https://github.com/shulja/viredero/blob/a7d28b21d762e8479dc10cde1aa88054497ff649/viredroid/src/main/java/org/viredero/viredroid/Sphere.java
     * @param radius 半径，半径应该在远平面和近平面之间
     * @param rings
     * @param sectors
     */
    public Fisheye2SphereModel(float radius, int rings, int sectors) {
        final float PI = (float) Math.PI;
        final float PI_2 = (float) (Math.PI / 2);

        float R = 1f/(float)rings;
        float S = 1f/(float)sectors;
        short r, s;
        float x, y, z;

        int numPoint = (rings + 1) * (sectors + 1);
        float[] vertexs = new float[numPoint * 3];
        float[] texcoords_left = new float[numPoint * 2];
        float[] texcoords_right = new float[numPoint * 2];
        float[] tex_alpha = new float[numPoint];
        short[] indices = new short[numPoint * 6];

        //纹理映射2d-3d
        int t_left = 0, t_right = 0, v = 0, t_alpha = 0;
        for(r = 0; r < rings + 1; r++) {
            for(s = 0; s < sectors + 1; s++) {
                x = (float) (Math.cos(2*PI * s * S) * Math.sin( PI * r * R ));
                y = (float) Math.sin( -PI_2 + PI * r * R );
                z = (float) (Math.sin(2*PI * s * S) * Math.sin( PI * r * R ));

                //PointF tmp = genLongLatPointF(s*S, r*R);
                PointF left = getLongLatPointF_left(s*S, r*R);
                PointF right = getLongLatPointF_right(s*S, r*R);

                if(left==null)
                {
                    texcoords_left[t_left++] = 0;
                    texcoords_left[t_left++] = 0;
                }else{
                    texcoords_left[t_left++] = left.x;
                    texcoords_left[t_left++] = left.y;
                }

                if(right==null)
                {
                    texcoords_right[t_right++] = 0;
                    texcoords_right[t_right++] = 0;
                }else{
                    texcoords_right[t_right++] = right.x;
                    texcoords_right[t_right++] = right.y;
                }
                float t = s*S;

                if(t < 0.25f - COMBINE_ANGLE || t > 0.75f + COMBINE_ANGLE)
                {
                    tex_alpha[t_alpha++] = 0.0f;
                }
                else if(t>0.25f + COMBINE_ANGLE && t < 0.75f - COMBINE_ANGLE)
                {
                    tex_alpha[t_alpha++] = 1.0f;
                }
                else if(t <= 0.25f + COMBINE_ANGLE)
                {
                    float diff = t - 0.25f + COMBINE_ANGLE;
                    tex_alpha[t_alpha++] = diff / 2.0f / COMBINE_ANGLE;
                }
                else if(t <= 0.75f + COMBINE_ANGLE)
                {
                    float diff = t - 0.75f + COMBINE_ANGLE;
                    tex_alpha[t_alpha++] = 1.0f - diff / 2.0f / COMBINE_ANGLE;
                }

                //tex_alpha[t_alpha++] = 0.5f;

                vertexs[v++] = x * radius;
                vertexs[v++] = y * radius;
                vertexs[v++] = z * radius;
            }
        }

        //球体绘制坐标索引，用于  glDrawElements
        int counter = 0;
        int sectorsPlusOne = sectors + 1;
        for(r = 0; r < rings; r++){
            for(s = 0; s < sectors; s++) {
                indices[counter++] = (short) (r * sectorsPlusOne + s);       //(a)
                indices[counter++] = (short) ((r+1) * sectorsPlusOne + (s));    //(b)
                indices[counter++] = (short) ((r) * sectorsPlusOne + (s+1));  // (c)
                indices[counter++] = (short) ((r) * sectorsPlusOne + (s+1));  // (c)
                indices[counter++] = (short) ((r+1) * sectorsPlusOne + (s));    //(b)
                indices[counter++] = (short) ((r+1) * sectorsPlusOne + (s+1));  // (d)
            }
        }

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                vertexs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexs);
        vertexBuffer.position(0);

        // initialize vertex byte buffer for shape coordinates
        /*
        ByteBuffer cc = ByteBuffer.allocateDirect(
                texcoords.length * 4);
        cc.order(ByteOrder.nativeOrder());
        FloatBuffer texBuffer = cc.asFloatBuffer();
        texBuffer.put(texcoords);
        texBuffer.position(0);
        */
        ByteBuffer cc_left = ByteBuffer.allocateDirect(
                texcoords_left.length * 4);
        cc_left.order(ByteOrder.nativeOrder());
        FloatBuffer texBuffer_left = cc_left.asFloatBuffer();
        texBuffer_left.put(texcoords_left);
        texBuffer_left.position(0);

        ByteBuffer cc_right = ByteBuffer.allocateDirect(
                texcoords_right.length * 4);
        cc_right.order(ByteOrder.nativeOrder());
        FloatBuffer texBuffer_right = cc_right.asFloatBuffer();
        texBuffer_right.put(texcoords_right);
        texBuffer_right.position(0);

        ByteBuffer cc_alpha = ByteBuffer.allocateDirect(
                tex_alpha.length * 4);
        cc_alpha.order(ByteOrder.nativeOrder());
        FloatBuffer texBuffer_alpha = cc_alpha.asFloatBuffer();
        texBuffer_alpha.put(tex_alpha);
        texBuffer_alpha.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        indexBuffer = dlb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        mTexCoordinateBuffer_left=texBuffer_left;
        mTexCoordinateBuffer_right=texBuffer_right;
        mTexAlpha=texBuffer_alpha;
        mVerticesBuffer=vertexBuffer;
        mNumIndices=indices.length;
    }


    public void uploadVerticesBuffer(int positionHandle){
        FloatBuffer vertexBuffer = getVerticesBuffer();
        if (vertexBuffer == null) return;
        vertexBuffer.position(0);

        GLES20.glVertexAttribPointer(positionHandle, sPositionDataSize, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");
    }

    public void uploadTexCoordinateBuffer(int textureCoordinateHandle_left, int textureCoordinateHandle_right, int textureAlphaHandle){
        FloatBuffer textureBuffer_left = getTexCoordinateBuffer_left();
        FloatBuffer textureBuffer_right = getTexCoordinateBuffer_right();
        FloatBuffer textureAlpha = getTexAlpha();

        if (textureBuffer_left == null) return;
        if (textureBuffer_right == null) return;
        if (textureAlpha == null) return;
        textureBuffer_left.position(0);
        textureBuffer_right.position(0);
        textureAlpha.position(0);

        GLES20.glVertexAttribPointer(textureCoordinateHandle_left, sTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, textureBuffer_left);
        GLES20.glVertexAttribPointer(textureCoordinateHandle_right, sTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, textureBuffer_right);
        GLES20.glVertexAttribPointer(textureAlphaHandle, sTextureAlphaDataSize, GLES20.GL_FLOAT, false, 0, textureAlpha);

        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle_left);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle_right);
        GLES20.glEnableVertexAttribArray(textureAlphaHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");
    }

    public FloatBuffer getVerticesBuffer() {
        return mVerticesBuffer;
    }

    public FloatBuffer getTexCoordinateBuffer_left() {
        return mTexCoordinateBuffer_left;
    }

    public FloatBuffer getTexCoordinateBuffer_right() {
        return mTexCoordinateBuffer_right;
    }

    public FloatBuffer getTexAlpha() {
        return mTexAlpha;
    }

    public void draw() {
        indexBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }
}
