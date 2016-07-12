import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;

import mat.Matrix4;
import mat.PerspectiveMatrix;
import mat.RotationMatrix;
import mat.TranslationMatrix;
import mat.Vec3;


/**
 * Abstract class for handling objects that are renderable
 * some functions used from simplePrimivites
 *
 * There are different shaders used throughout the game, two each for 3D objects and for the GUI objects which don't
 * require any matrix transformations because of its HUD-nature.
 *
 * @author Sebastian Kriege
 *
 */

abstract class Renderable {
	
    float[] vertexArray;
    float[] textureArray;
    int[] indexArray;
    String textureFile;
    String shaderVFile = "vertex.glsl";
    String shaderFFile = "fragment.glsl";
    private int pId;
    private int vsId;
    private int fsId;
    private int projectionMatrixLocation = 0;
    private int viewMatrixLocation = 0;
    private int modelMatrixLocation = 0;
    private int useTextureLocation = 0;
    private int textureID;
    private int vaoId;
    private int vboId;
    private int tboId;
    private int iboId;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private IntBuffer indexBuffer;
    private Vec3 cameraPos = new Vec3(0,1.8f,-1.4);
    private Vec3 modelAngle = new Vec3(0,0,0);
    Vec3 translate = new Vec3(0,0,1);
    Vec3 defaultModelAngle = new Vec3(0, 0, 0);
    Vec3 defaultTranslate = new Vec3(0, 0, 1);
    private Matrix4 projectionMatrix = null;
    private Matrix4 viewMatrix = null;
    private Matrix4 modelMatrix = null;


    /**
     * Definition of texture, vertex and index arrays, sometimes a texture file. This will be called whenever
     * anything changes via the init() function
     */
    public abstract void createGeometry();

    /**
     * Initializer. The constructor of the Renderable MUST call this in order to set up buffers and texture objects.
     * This also loads and sets up the shaders and the translation matrix.
     */
    void init() {
    	// every object must create textureArray, vertexArray and indexArray via createGeometry
        createGeometry();
        // sets the texture for the object
        textureID = loadPNGTexture("./assets/" + textureFile, GL_TEXTURE0);
        // creates buffers only once
    	initBuffers();
    	// setup shades only once
    	setupShader();
    	// resets the translation matrix
        resetTranslationMatrix();
    }

    /**
     * Function to modify the translation matrix by increments
     *
     * @param setM* rotation increment in the respective axis and degrees
     * @param setT* translation increment in the respective axis
     */
    void modifyModel(float setMX, float setMY, float setMZ, float setTX, float setTY, float setTZ){
    	modelAngle.x += setMX;
    	modelAngle.y += setMY;
    	modelAngle.z += setMZ;
    	translate.x += setTX;
    	translate.y += setTY;
    	translate.z += setTZ;
        setupMatrices();
    }

    /**
     * Reset the translation and rotation matrix to default values stored in class fields
     */
    void resetTranslationMatrix() {
        modelAngle.x = defaultModelAngle.x;
        modelAngle.y = defaultModelAngle.y;
        modelAngle.z = defaultModelAngle.z;
        translate.x = defaultTranslate.x;
        translate.y = defaultTranslate.y;
        translate.z = defaultTranslate.z;
        setupMatrices();
    }

    /**
     * Initializes all necessary buffers for the object
     */
    private void initBuffers() {
    	// creates the buffer in the main memory
        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();
        tboId = glGenBuffers();
        iboId = glGenBuffers();
        vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        textureBuffer = BufferUtils.createFloatBuffer(textureArray.length);
        indexBuffer = BufferUtils.createIntBuffer(indexArray.length);
        
        // creates the buffer in the graphic card
        glBindVertexArray(vaoId);
        
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
        glBindBuffer(GL_ARRAY_BUFFER, tboId);
        glVertexAttribPointer(1, 2, GL_FLOAT, true, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
     	glBindVertexArray(0);
     	glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
     	
    }

    /**
     * Update the already initialized buffers with new data
     */
    private void updateBuffers() {
    	// clears the buffer, assigns the new data
    	vertexBuffer.clear();
    	vertexBuffer.put(vertexArray);
    	vertexBuffer.flip();

        textureBuffer.clear();
        textureBuffer.put(textureArray);
        textureBuffer.flip();
    	
    	indexBuffer.clear();
    	indexBuffer.put(indexArray);
    	indexBuffer.flip();
    	
    	// loads the new data into the graphic card
		glBindVertexArray(vaoId);
		
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STREAM_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ARRAY_BUFFER, tboId);
        glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, true, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STREAM_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

     	glBindVertexArray(0);
        
    }

    /**
     * Actually make the draw call (and set uniforms and some other stuff)
     */
    void render() {
    	// first update the buffers
    	updateBuffers();
    	
    	
        // enable shader program
        glUseProgram(pId);
        
        // update translation matrix, projection matrix and model matrix
        glUniformMatrix4fv(projectionMatrixLocation, false, toFFB(projectionMatrix));
        glUniformMatrix4fv(viewMatrixLocation, false, toFFB(viewMatrix));
        glUniformMatrix4fv(modelMatrixLocation, false, toFFB(modelMatrix));

        // binds texture
        glUniform1i(useTextureLocation, GL_TRUE);
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Bind to the VAO that has all the information about the vertices
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0); // vertex coordinates
        glEnableVertexAttribArray(1); // texture coordinates

        // Bind to the index VBO that has all the information about the order of the vertices
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);

        // Draw the vertices
        glDrawElements(GL_TRIANGLES, indexArray.length, GL_UNSIGNED_INT, 0);

        // Put everything back to default (deselect)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
        glUseProgram(0);
    }

    /**
     * Setup shaders (from the example code in moodle)
     */
    private void setupShader() {
        pId = glCreateProgram();

        vsId = loadShader("./src/shaders/" + shaderVFile, GL_VERTEX_SHADER);
        fsId = loadShader("./src/shaders/" + shaderFFile, GL_FRAGMENT_SHADER);

        glAttachShader(pId, vsId);
        glAttachShader(pId, fsId);

        // Position information will be attribute 0
        glBindAttribLocation(pId, 0, "in_Position");
        // Texture coordinates information will be attribute 1
        glBindAttribLocation(pId, 1, "in_TextureCoord");
        
        glLinkProgram(pId);
        glValidateProgram(pId);

        int errorCode = glGetError();
        if (errorCode != GL_NO_ERROR) {
            //todo: error msg
            System.out.println("ERROR - Could not create the shaders: " + errorCode);
            System.exit(-1);
        }

        // Get matrices uniform locations
        projectionMatrixLocation = glGetUniformLocation(pId,"projectionMatrix");
        viewMatrixLocation = glGetUniformLocation(pId, "viewMatrix");
        modelMatrixLocation = glGetUniformLocation(pId, "modelMatrix");
        useTextureLocation = glGetUniformLocation(pId, "useTexture");
    }

    /**
     * Load shaders (from the example code in moodle)
     *
     * @param filename name of the shader file
     * @param type shader type (GL constant)
     * @return the shader ID
     */
    private static int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not read file.");
            e.printStackTrace();
            System.exit(-1);
        }

        shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);

        // error handling
        if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0){
            System.err.println("Shader compilation failed");
            System.err.println(glGetShaderInfoLog(shaderID, 1024));
            System.exit(1);
        }

        return shaderID;
    }

    /**
     * flatten a Matrix to a buffer. Taken from the example code in moodle
     *
     * @param m the matrix
     * @return a float buffer from the matrix values
     */
    private static FloatBuffer toFFB(Matrix4 m){
		FloatBuffer res = BufferUtils.createFloatBuffer(16);
		for (int i=0;i<4;i++){
			for (int j=0;j<4;j++){
				res.put((float) m.get(i).get(j));
			}
		}
		return (FloatBuffer) res.flip();
	}

    /**
     * Set up projection-, model- and view-matrices for the shader
     */
    private void setupMatrices() {
    	// Setup projection and view matrix
    	projectionMatrix = new PerspectiveMatrix(-1,1,-1,1,0.1f,20f);
    	viewMatrix = new TranslationMatrix(cameraPos);
    	// first translate, then rotate. Remember the flipped order
        modelMatrix = (Matrix4) new RotationMatrix(modelAngle.y, mat.Axis.Y); // ... and rotate, multiply matrices
        modelMatrix = (Matrix4) new RotationMatrix(modelAngle.x, mat.Axis.X).mul(modelMatrix); // ... and rotate, multiply matrices
        modelMatrix = (Matrix4) new RotationMatrix(modelAngle.z, mat.Axis.Z).mul(modelMatrix); // ... and rotate, multiply matrices
        modelMatrix = (Matrix4) new TranslationMatrix(translate).mul(modelMatrix);  // translate...
 
    }

    /**
     * Utility function to load a PNG texture from file. Also binds the texture and returns its ID.
     *
     * @param filename texture file name
     * @param textureUnit which texture unit to bind to (GL constant)
     * @return the texture ID of the newly bound texture
     */
    private static int loadPNGTexture(String filename, int textureUnit) {
        ByteBuffer buf = null;
        int tWidth = 0;
        int tHeight = 0;

        try {
            // Open the PNG file as an InputStream
            InputStream in = new FileInputStream(filename);
            // Link the PNG decoder to this stream
            PNGDecoder decoder = new PNGDecoder(in);

            // Get the width and height of the texture
            tWidth = decoder.getWidth();
            tHeight = decoder.getHeight();


            // Decode the PNG file in a ByteBuffer
            buf = ByteBuffer.allocateDirect(
                    4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            buf.flip();

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Create a new texture object in memory and bind it
        int texId = glGenTextures();
        glActiveTexture(textureUnit);
        glBindTexture(GL_TEXTURE_2D, texId);

        // All RGB bytes are aligned to each other and each component is 1 byte
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // Upload the texture data and generate mip maps (for scaling)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tWidth, tHeight, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glGenerateMipmap(GL_TEXTURE_2D);

        // Setup the ST coordinate system
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // Setup what to do when the texture has to be scaled
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
                GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                GL_LINEAR_MIPMAP_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8f);
        return texId;
    }
}