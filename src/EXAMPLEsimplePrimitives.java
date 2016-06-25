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

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;


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
    
    private Kubus tubus;
    private Kubus lubus;
    private Bug guy;
    private GUI gui;
    private Renderable backdrop;
	private float speed = 0.01f;
	private int numEdges = 5;
	private int segments = 4;
	private long time;
	private int fps_counter;
    

    private void run() {
        try {
            init();

            // initialize each object (extending Renderable) using initObject(object)
            tubus = new Kubus(segments, numEdges, "gdv.png");
            gui = new GUI("gdv_inv.png");
            guy = new Bug("gdv.png");
            backdrop = new Renderable() {

                @Override
                public void init() {
                    textureFile = "stars.png";
                    createGeometry();
                    super.init();
                }

                private void createGeometry() {
                    vertexArray = new float[] {
                            -1f, 1f, 2,
                            1f, 1f, 2,
                            1f, -1f, 2,
                            -1f, -1f, 2,
                    };
                    textureArray = new float[] {
                            -1f, 1f,
                            1f, 1f,
                            1f, -1f,
                            -1f, -1f,
                    };
                    indexArray = new int[] {0, 3, 1, 1, 3, 2};
                }
            };
            backdrop.init();
            loop();

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
        // Enable Multisampling
        glfwWindowHint(GLFW_STENCIL_BITS, 16);
        glfwWindowHint(GLFW_SAMPLES, 16);

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
            	if ( key == GLFW_KEY_RIGHT && action == GLFW_PRESS)
            		((Kubus)tubus).turn(true);
            	if ( key == GLFW_KEY_LEFT && action == GLFW_PRESS)
                    tubus.turn(false);
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
        glClearColor(0.2f, 0.2f, 0.2f, 0.5f);

        // Switch to wireframe
        glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
        // -> back to solid faces: glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );

        // Backface culling: Shows, if the triangles are correctly defined
        glDisable(GL_CULL_FACE);
        

    }


    private void loop() throws Exception {

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {


            // =============================== Mechanics =========================================

            tubus.moveZ(speed);
            tubus.progress();
        	
        	// wenn du vsync ausmachst hab ich 4000 fps und die bewegung ist seeehr schnell :>
            if (fps_counter == 0) {
                time = System.currentTimeMillis();
                fps_counter++;
            }
            else if (fps_counter == 100){
            	System.out.println("FPS: " + 100000 / (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
                fps_counter = 0;
            }
            else {
            	fps_counter++;
            }
                     
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // ================================== Draw object =====================================

            backdrop.render();
            tubus.render();
            //guy.render(); geht nicht =(
            gui.render();

            // Swap the color buffer. We never draw directly to the screen, only in this buffer. So we need to display it
    		glfwSwapBuffers(window);
            
            // Poll for window events. The key callback above will only be invoked during this call.
            glfwPollEvents();

            // reset error state
            glGetError();
            //System.out.println("=========================");
        }
    }
 	
    public static void main(String[] args) {
        new EXAMPLEsimplePrimitives().run();
    }
 
}