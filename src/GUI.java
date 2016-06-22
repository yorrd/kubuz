import javafx.scene.DepthTest;
import mat.Matrix4;
import mat.OrthogonalMatrix;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
        glAttachShader(pId, EXAMPLEsimplePrimitives.loadShader("./src/shaders/guivertex.glsl", GL_VERTEX_SHADER));
        glAttachShader(pId, EXAMPLEsimplePrimitives.loadShader("./src/shaders/guifragment.glsl", GL_FRAGMENT_SHADER));
        glUseProgram(pId);

        int matrixLoc = glGetUniformLocation(pId, "projModelMatrix");
        int colorLoc = glGetUniformLocation(pId, "colour");

        // uniforms
        Matrix4 orthogonalMatrix = new OrthogonalMatrix(0, EXAMPLEsimplePrimitives.WIDTH, EXAMPLEsimplePrimitives.HEIGHT, 0, 0.01, 100);
        Matrix4 modelView = new Matrix4();  // translate later maybe
        modelView.setIdentity();
        orthogonalMatrix = (Matrix4) orthogonalMatrix.mul(modelView);
        glUniformMatrix4fv(matrixLoc, false, EXAMPLEsimplePrimitives.toFFB(orthogonalMatrix));
        glUniformMatrix4fv(colorLoc, false, new float[]{.5f, .6f, .1f});

        // ========================= gui render
//        glEnable(GL_TEXTURE_2D);
//        glDisable(GL_DEPTH_TEST);
//        glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_2D, EXAMPLEsimplePrimitives.loadPNGTexture("./assets/gdv.png", GL_TEXTURE0));
//        EXAMPLEsimplePrimitives.loadPNGTexture("./assets/gdv.png", GL_TEXTURE0);
        glUniform1i(glGetUniformLocation(pId, "texture_sampler"), 0);

        // vertex array
        int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // first attribute, vertex positions
        int vboId = glGenBuffers();
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(4 * 3);
        vertexBuffer.put(new float[] {
                0, 0, 0,  // left bottom
                .5f, 0, 0,  // right bottom
                .5f, .5f, 0,  // right top
                0, .5f, 0,  // left bottom
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
//        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
