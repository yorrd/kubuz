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

abstract class Renderable {
	
    protected float[] vertexArray;
    protected float[] textureArray;
    protected int[] indexArray;
    protected String textureFile;
    protected String shaderVFile = "vertex.glsl";
    protected String shaderFFile = "fragment.glsl";
    protected int pId;
    protected int vsId;
    protected int fsId;
    private int projectionMatrixLocation = 0;
    private int viewMatrixLocation = 0;
    private int modelMatrixLocation = 0;
    private int useTextureLocation = 0;
    protected int textureID;
    protected int vaoId;
    protected int vboId;
    protected int tboId;
    protected int iboId;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private IntBuffer indexBuffer;    
    private Vec3 cameraPos = new Vec3(0,0,-1.4);
    private Vec3 modelAngle = new Vec3(0,0,0);
    private Matrix4 projectionMatrix = null;
    private Matrix4 viewMatrix = null;
    private Matrix4 modelMatrix = null;


    public abstract void createGeometry();

    // constructor MUST call init
    public void init() {
    	textureID = loadPNGTexture("./assets/" + textureFile, GL_TEXTURE0);
        createGeometry();
    	initBuffers();
    	setupShader();
    	setupMatrices();
    }
       
    private void initBuffers() {
        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();
        tboId = glGenBuffers();
        iboId = glGenBuffers();
        vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        textureBuffer = BufferUtils.createFloatBuffer(textureArray.length);
        indexBuffer = BufferUtils.createIntBuffer(indexArray.length);
        

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
   
    private void updateBuffers() {
    	vertexBuffer.clear();
    	vertexBuffer.put(vertexArray);
    	vertexBuffer.flip();

        textureBuffer.clear();
        textureBuffer.put(textureArray);
        textureBuffer.flip();
    	
    	indexBuffer.clear();
    	indexBuffer.put(indexArray);
    	indexBuffer.flip();
    	
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
    
    
    void render() {
    	updateBuffers();
    	
    	
        // Upload matrices to the uniform variables to shader program 0
        glUseProgram(pId);

        glUniformMatrix4fv(projectionMatrixLocation, false, toFFB(projectionMatrix));
        glUniformMatrix4fv(viewMatrixLocation, false, toFFB(viewMatrix));
        glUniformMatrix4fv(modelMatrixLocation, false, toFFB(modelMatrix));

        glUniform1i(useTextureLocation, GL_TRUE);
        
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Bind to the VAO that has all the information about the vertices
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
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
    
    
    private void setupShader() {
        pId = glCreateProgram();

        // setup shaders
        vsId = loadShader("./src/shaders/" + shaderVFile, GL_VERTEX_SHADER);
        fsId = loadShader("./src/shaders/" + shaderFFile, GL_FRAGMENT_SHADER);

        glAttachShader(pId, vsId);
        glAttachShader(pId, fsId);

        // Position information will be attribute 0
        glBindAttribLocation(pId, 0, "in_Position");
        // Texture coordinates information will be attribute 3
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
    
    // loadshader from example
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

    // toFFB from example
	private static FloatBuffer toFFB(Matrix4 m){
		FloatBuffer res = BufferUtils.createFloatBuffer(16);
		for (int i=0;i<4;i++){
			for (int j=0;j<4;j++){
				res.put((float) m.get(i).get(j));
			}
		}
		return (FloatBuffer) res.flip();
	}
    
    // Setupmatrices from example
    private void setupMatrices() {
    	// Setup projection and view matrix
    	projectionMatrix = new PerspectiveMatrix(-1,1,-1,1,0.1f,2f);
    	viewMatrix = new TranslationMatrix(cameraPos);
    	// first translate, then rotate. Remember the flipped order
        modelMatrix = new TranslationMatrix(new Vec3(0,0,1));  // translate...
        modelMatrix = (Matrix4) new RotationMatrix(modelAngle.y, mat.Axis.Y).mul(modelMatrix); // ... and rotate, multiply matrices

    }
    
    // loadPNGTexture from example
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