import mat.Matrix4;
import mat.OrthogonalMatrix;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

class GUI {

    private static GUI instance;

    static GUI getInstance() {
        if(instance == null) {
            instance = new GUI();
        }
        return instance;
    }

    void render() {
        int pId = glCreateProgram();

        // setup shaders
        int vsId = EXAMPLEsimplePrimitives.loadShader("./src/shaders/guivertex.glsl", GL_VERTEX_SHADER);
        int fsId = EXAMPLEsimplePrimitives.loadShader("./src/shaders/guifragment.glsl", GL_FRAGMENT_SHADER);

        glAttachShader(pId, vsId);
        glAttachShader(pId, fsId);

        glLinkProgram(pId);
        glValidateProgram(pId);

        int errorCode = glGetError();
        if (errorCode != GL_NO_ERROR) {
            //todo: error msg
            System.out.println("ERROR - Could not create the shaders: " + errorCode);
            System.exit(-1);
        }

        glUseProgram(pId);

        // ========================= gui render
//        glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_2D, EXAMPLEsimplePrimitives.loadPNGTexture("./assets/gdv_inv.png", GL_TEXTURE0));
        glEnable(GL_TEXTURE_2D);
        int textureID = EXAMPLEsimplePrimitives.loadPNGTexture("./assets/gdv_inv.png", GL_TEXTURE0);

        // vertex array
        int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // first attribute, vertex positions
        int vboId = glGenBuffers();
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(4 * 3);
        vertexBuffer.put(new float[] {
                0, 0, 1,  // left bottom
                .5f, 0, 1,  // right bottom
                .5f, .5f, 1,  // right top
                0, .5f, 1,  // left bottom
        }).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        // second attribute, textures
        vboId = glGenBuffers();
        FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(4 * 2);
        vertexBuffer.put(new float[] {
                0f, 0f,
                1f, 0f,
                1f, 1f,
                0f, 1f
        }).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        // indices
        vboId = glGenBuffers();
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(6);
        indexBuffer.put(new int[]{0, 1, 2, 0, 2, 3}).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // actually draw
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);  // index buffer length = 6

        // reset
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
