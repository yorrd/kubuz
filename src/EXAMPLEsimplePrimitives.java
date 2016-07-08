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
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.AL10.alGetError;
import static org.lwjgl.openal.ALC.createCapabilities;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;


public class EXAMPLEsimplePrimitives {
 
    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWWindowSizeCallback window_size_callback;
 
    // The window handle
    private long window;
    
    // Window size
    private static int WIDTH = 800;
    private static int HEIGHT = 800;
    
    private Kubus tubus;
    private Bug guy;
    private GUI gui;
    private Renderable backdrop;
	private float speed = 0.01f;
	private int numEdges = 6;
	private int segments = 4; // multiple of 2
	private long time;
	private int fps_counter;
	private float insectAngle = 0;

    static boolean paused = false;
    static boolean gameover = false;
    private static HashMap<Integer, Boolean> steeringKeysPressed = new HashMap<>();
    static {
        steeringKeysPressed.put(GLFW_KEY_RIGHT, false);
        steeringKeysPressed.put(GLFW_KEY_LEFT, false);
    }

    private long fallenSince = 0;
    private int immune = 60;

    private Playable backgroundMusic;
    private Playable click;

    private void run() {
        try {
            init();

            // initialize each object (extending Renderable) using initObject(object)
            tubus = new Kubus(segments, numEdges);
            gui = new GUI();
            guy = new Bug();
            backdrop = new Backdrop();
            backgroundMusic = new Playable("./background.wav", true, 1f);
            click = new Playable("./click.wav", false, 1f);
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

                if(gameover && key == GLFW_KEY_ESCAPE)
                    restart();
            	else if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop

                if ( key == GLFW_KEY_RIGHT || key == GLFW_KEY_LEFT ) {
                    steeringKeysPressed.put(key, action != GLFW_RELEASE);
                    if(action == GLFW_PRESS)
                        click.play();
                }
                if ( key == GLFW_KEY_B && action == GLFW_PRESS ) {
                    if(paused) resume();
                    else pause();
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
        glClearColor(0.8f, 0.8f, 0.8f, 0.8f);

        // Switch to wireframe
        glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
        // -> back to solid faces: glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );

        // Backface culling: Shows, if the triangles are correctly defined
        glDisable(GL_CULL_FACE);
        
        // enables transparency from png files
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


        // ==================================== SOUND =============================================
        // Initialize OpenAL and clear the error bit.
        try{
            ALCCapabilities devcaps = createCapabilities(alcOpenDevice((ByteBuffer) null));
            long context = alcCreateContext(alcOpenDevice((ByteBuffer) null), (IntBuffer) null);
            alcMakeContextCurrent(context);
            AL.createCapabilities(devcaps);
        } catch (Exception le) {
            le.printStackTrace();
            return;
        }
        alGetError();

    }


    private void loop() throws Exception {

        backgroundMusic.play();

        int maxTurnDegree = 20;
        int turnIncrement = 4;
        float movingIncrement = .02f;
        float posX = 0;
        float posZ = 0.2f;

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {

            if (!paused) {
                // =============================== Mechanics =========================================

                if(fallenSince == 0) tubus.moveZ(speed);
                tubus.progress();
                guy.animate();

                // evaluate this.steeringKeysPressed aka make it move like the user asked us to
                if(steeringKeysPressed.get(GLFW_KEY_RIGHT)) {
                    guy.modifyModel(0,0,0,movingIncrement,0,0);
                    posX += movingIncrement;
                    if(insectAngle < maxTurnDegree) {
                        guy.modifyModel(0,turnIncrement,0,0,0,0);
                        insectAngle += turnIncrement;
                    }
                    if(!guy.isInBounds(-tubus.getWidth() / 2, tubus.getWidth() / 2)) {
                        tubus.turn(true);
                        guy.modifyModel(0, 0, 0, -(tubus.getWidth() - guy.getBodyWidth()), 0, 0);
                        posX -= (tubus.getWidth() - guy.getBodyWidth());
                    }
                }
                if(steeringKeysPressed.get(GLFW_KEY_LEFT)) {
                    guy.modifyModel(0,0,0,-movingIncrement,0,0);
                    posX -= movingIncrement;
                    if(insectAngle > -maxTurnDegree) {
                        guy.modifyModel(0,-turnIncrement,0,0,0,0);
                        insectAngle -= turnIncrement;
                    }
                    if (!guy.isInBounds(-tubus.getWidth() / 2, tubus.getWidth() / 2)) {
                        tubus.turn(false);
                        guy.modifyModel(0, 0, 0, tubus.getWidth() - guy.getBodyWidth(), 0, 0);
                        posX += (tubus.getWidth() - guy.getBodyWidth());
                    }
                }
                if(!steeringKeysPressed.get(GLFW_KEY_LEFT) && !steeringKeysPressed.get(GLFW_KEY_RIGHT)) {
                    if(insectAngle != 0) {
                        int change = turnIncrement * (insectAngle < 0 ? 1 : -1);
                        insectAngle += change;
                        guy.modifyModel(0, change, 0, 0, 0, 0);
                    }
                }

                // check if we're falling through at the moment
                if(immune > 0) immune--;
                if(fallenSince == 0) {  // not falling yet
                    if (tubus.isHole(posX, posZ) && immune == 0) {  // if we're on a hole and not immune at the moment
                        guy.fall();
                        if(gui.reduceLife()) {
                            gameover = true;
                            pause();
                        }
                        fallenSince = System.currentTimeMillis();
                    }
                } else if(System.currentTimeMillis() - fallenSince < 2000) {
                    guy.fall();
                } else {
                    fallenSince = 0;
                    immune = 60;
                    guy.reset();
                }
            }
            
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
            glEnable(GL_DEPTH_TEST);
            tubus.render();
            guy.render();
            glDisable(GL_DEPTH_TEST);
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

    private void pause() {
        paused = true;
        gui.pauseUnPause();
        backgroundMusic.pause();
    }

    private void resume() {
        if(gameover) return;
        paused = false;
        gui.pauseUnPause();
        backgroundMusic.play();
    }

    private void restart() {
        gameover = false;
        resume();
        guy.reset();
        tubus.reset();
        // TODO reset kubus and stuff
    }
 	
    public static void main(String[] args) {
        new EXAMPLEsimplePrimitives().run();
    }
 
}
