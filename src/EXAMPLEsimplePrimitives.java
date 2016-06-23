/**
  * A simple application for displaying a rectangle with drawElements and triangle strip
  * 
  * @author Thorsten Gattinger
  * 
  * sources:
  * http://wiki.lwjgl.org/wiki/The_Quad_with_DrawElements and the other Quad-parts
  * getting started: http://www.lwjgl.org/guide
  * http://hg.l33tlabs.org/twl/file/tip/src/de/matthiasmann/twl/utils/PNGDecoder.java
  */

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

// matrix Utilities was removed with lwjgl3, we use our own
import mat.*;

public class EXAMPLEsimplePrimitives {
 
    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback   keyCallback;
    private GLFWWindowSizeCallback window_size_callback;
 
    // The window handle
    private long window;
    
    // Window size
    static int WIDTH = 800;
    static int HEIGHT = 640;
    
    // Buffer IDs
  	private int vaoId = 0;
  	private int vboId = 0;	//vertex
  	private int vbotId = 0;	//texture coords
  	private int vboiId = 0;	//index
  	private int indicesCount = 0;
  	private int verticesCount = 0;
  	
  	// Shader variables
  	private int vsId = 0;
    private int fsId = 0;
    private int pId = 0;
    private int textureID = 0;
    
    // Moving variables
    private int projectionMatrixLocation = 0;
    private int viewMatrixLocation = 0;
    private int modelMatrixLocation = 0;
    private Matrix4 projectionMatrix = null;
    private Matrix4 viewMatrix = null;
    private Matrix4 modelMatrix = null;
    private Vec3 modelAngle = new Vec3(0,0,0);
    private Vec3 cameraPos = new Vec3(0,0,-1.4);
    private float deltaRot = 5f;

    // toggles & interactions
    private boolean showMesh = true;
    private boolean useBackfaceCulling = true;
    private int useTexture = GL_TRUE;
    private int useTextureLocation = 0;

    private void run() {
        try {
            init();
            setupShaders();
            setupMatrices();

            // initialize each object (extending Renderable) using initObject(object)
            Kubus tubus = new Kubus();
            tubus.render();
//            initObject(tubus);
            loop(tubus);

            // Release window and window callbacks
            glfwDestroyWindow(window);
            glfwSetKeyCallback(window, null);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            // Terminate GLFW and release the GLFWerrorfun
            glfwTerminate();
        }
    }
 
    private void init() {
    	
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
 
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable
		// Set OpenGL version to 3.2.0
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
            	if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
            	if ( key == GLFW_KEY_RIGHT )
                    modelAngle.y += deltaRot;
            	if ( key == GLFW_KEY_LEFT )
                    modelAngle.y -= deltaRot;
            	if ( key == GLFW_KEY_M && action == GLFW_PRESS ) {
            		showMesh = !showMesh;
            		if (showMesh) glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
            		else glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
            	}
            	if ( key == GLFW_KEY_T && action == GLFW_PRESS ){
            		if (useTexture==1)
            			useTexture=0;
            		else
            			useTexture=1;
            	}
            	if ( key == GLFW_KEY_C && action == GLFW_PRESS ) {
            		useBackfaceCulling = !useBackfaceCulling;
            		if (useBackfaceCulling) glEnable(GL_CULL_FACE);
            		else glDisable(GL_CULL_FACE);
            	}
            }
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
            window,
            (vidmode.width() - WIDTH) / 2,
            (vidmode.height() - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // Setup a window size callback for viewport adjusting while resizing
        glfwSetWindowSizeCallback(window, window_size_callback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				// Viewport: Use full display size
		        glViewport(0, 0, width, height);
			}
        });


        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Debug: We need version 3.2 or newer
        System.out.println("We need OpenGL version 3.2.0. You use " + glGetString(GL_VERSION));

        // Viewport: Use full display size
        glViewport(0, 0, WIDTH, HEIGHT);

        // Set the clear color - gray
        glClearColor(0.3f, 0.3f, 0.3f, 0.0f);

        // Switch to wireframe
        glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
        // -> back to solid faces: glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );

        // Backface culling: Shows, if the triangles are correctly defined
        glDisable(GL_CULL_FACE);

    }


    private void setupMatrices() {

    	// Setup projection and view matrix
    	projectionMatrix = new PerspectiveMatrix(-1,1,-1,1,0.1f,2);
    	viewMatrix = new TranslationMatrix(cameraPos);
    }

    public static int loadShader(String filename, int type) {
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

    private void setupShaders() {
        int errorCheckValue = glGetError();

        // ============================= 1. Shader: For vertices ==================================
        // Load the vertex shader
        vsId = loadShader("./src/shaders/vertex.glsl", GL_VERTEX_SHADER);
        // Load the fragment shader
        fsId = loadShader("./src/shaders/fragment.glsl", GL_FRAGMENT_SHADER);

        // Create a new shader program that links both shaders
        pId = glCreateProgram();
        glAttachShader(pId, vsId);
        glAttachShader(pId, fsId);

        // Position information will be attribute 0
        glBindAttribLocation(pId, 0, "in_Position");
        // Color information will be attribute 1
        glBindAttribLocation(pId, 1, "in_Color");
        // Normal information will be attribute 2
        glBindAttribLocation(pId, 2, "in_Normal");
        // Texture coordinates information will be attribute 3
        glBindAttribLocation(pId, 3, "in_TextureCoord");

        glLinkProgram(pId);
        glValidateProgram(pId);

        errorCheckValue = glGetError();
        if (errorCheckValue != GL_NO_ERROR) {
        	//todo: error msg
            System.out.println("ERROR - Could not create the shaders:");
            System.exit(-1);
        }

        // Get matrices uniform locations
        projectionMatrixLocation = glGetUniformLocation(pId,"projectionMatrix");
        viewMatrixLocation = glGetUniformLocation(pId, "viewMatrix");
        modelMatrixLocation = glGetUniformLocation(pId, "modelMatrix");
        useTextureLocation = glGetUniformLocation(pId, "useTexture");
    }

    /**
     * Uses an external class to load a PNG image and bind it as texture
     * @param filename
     * @param textureUnit
     * @return textureID
     */
    static int loadPNGTexture(String filename, int textureUnit) {
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
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, tWidth, tHeight, 0,
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

        return texId;
    }

    private void initObject(Renderable object) {
        textureID = loadPNGTexture(object.getTextureFilename(), GL_TEXTURE0);

    	// ================================== 1. Define vertices ==================================

    	// Vertices, the order is not important.
		float[] vertices = object.getVertexArray();
		// Sending data to OpenGL requires the usage of (flipped) byte buffers
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();
		verticesCount = vertices.length/3;

		// Texture Coordinates for each vertex, ST
		float[] textureCoords = object.getTextureArray();
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(textureCoords.length);
		textureCoordsBuffer.put(textureCoords);
		textureCoordsBuffer.flip();

		// ================================== 2. Define indices for vertices ======================

		// OpenGL expects to draw the first vertices in counter clockwise order by default
		int[] indices = object.getIndexArray();

		indicesCount = indices.length;
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();

		// ================================== 3. Make the data accessible =========================

		// Create a new Vertex Array Object in memory and select it (bind)
		// A VAO can have up to 16 attributes (VBO's) assigned to it by default
		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		// Create a new Vertex Buffer Object (VBO) in memory and select it (bind)
		// A VBO is a collection of Vectors which in this case resemble the location of each vertex.
		vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
		// Put the VBO in the attributes list at index 0
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		// Deselect (bind to 0) the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Create a new VBO and select it (bind) - TEXTURE COORDS
        vbotId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbotId);
        glBufferData(GL_ARRAY_BUFFER, textureCoordsBuffer, GL_STATIC_DRAW);
        //index 3, 2 values (ST)
        glVertexAttribPointer(3, 2, GL_FLOAT, true, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the indices and select it (bind) - INDICES
		vboiId = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
		// Deselect (bind to 0) the VBO
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // Deselect (bind to 0) the VAO
     	glBindVertexArray(0);

    }

    private void loop(Renderable object) throws Exception {

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {


            // =============================== Mechanics =========================================

            try {
                ((Kubus)object).moveZ(0.004f);
            } catch(Exception ignored) {}
            initObject(object);


            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // =============================== Update matrices ====================================

            // first translate, then rotate. Remember the flipped order
            modelMatrix = new TranslationMatrix(new Vec3(0,0,1));  // translate...
            modelMatrix = (Matrix4) new RotationMatrix(modelAngle.y, mat.Axis.Y).mul(modelMatrix); // ... and rotate, multiply matrices

            // Upload matrices to the uniform variables to shader program 0
            glUseProgram(pId);

            glUniformMatrix4fv(projectionMatrixLocation, false, toFFB(projectionMatrix));
            glUniformMatrix4fv(viewMatrixLocation, false, toFFB(viewMatrix));
            glUniformMatrix4fv(modelMatrixLocation, false, toFFB(modelMatrix));

            glUniform1i(useTextureLocation, useTexture);

            glUseProgram(0);

            // ================================== Draw object =====================================

            glUseProgram(pId);

            // Bind to the VAO that has all the information about the vertices
            glBindVertexArray(vaoId);
            glEnableVertexAttribArray(0);
//            glEnableVertexAttribArray(1); // not sure anymore what these were for, probably normals and stuff
//            glEnableVertexAttribArray(2); // need to clean up all of this...
            glEnableVertexAttribArray(3); // texture coordinates

            // Bind to the index VBO that has all the information about the order of the vertices
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId);

            // Draw the vertices
            glDrawElements(GL_TRIANGLES, indicesCount, GL_UNSIGNED_INT, 0);

            // Put everything back to default (deselect)
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glDisableVertexAttribArray(0);
//            glDisableVertexAttribArray(1);
//            glDisableVertexAttribArray(2);
            glDisableVertexAttribArray(3);
            glBindVertexArray(0);
            glUseProgram(0);

            // ================================== Draw GUI =====================================

            GUI.getInstance().render();

            // Swap the color buffer. We never draw directly to the screen, only in this buffer. So we need to display it
    		glfwSwapBuffers(window);
            
            // Poll for window events. The key callback above will only be invoked during this call.
            glfwPollEvents();

            // reset error state
            glGetError();
            System.out.println("=========================");
        }
    }
    
    /**
     * Converts a Matrix4 to a flipped float buffer
     * @param m
     * @return
     */
	protected static FloatBuffer toFFB(Matrix4 m){
		FloatBuffer res = BufferUtils.createFloatBuffer(16);
		for (int i=0;i<4;i++){
			for (int j=0;j<4;j++){
				res.put((float) m.get(i).get(j));
			}
		}
		return (FloatBuffer) res.flip();
	}
	
    public static void main(String[] args) {
        new EXAMPLEsimplePrimitives().run();
    }
 
}
