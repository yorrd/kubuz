
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


/**
 * Main class, holds GLFW logic, the main loop, key handling, etc.
 *
 * @author Kai Brobeil and Sebastian Kriege
 */

public class Kubuz {

    private Tubus tubus;
    private Insect player;
    private GUI gui;
    private LevelGUI levelGUI;
    private Renderable backdrop;
	private float speed = 0.01f;
	private int numEdges = 6;
	private int segments = 4; // multiple of 2
	private float insectAngle = 0;

    static boolean paused = false;
    static boolean gameOver = false;
    private static HashMap<Integer, Boolean> steeringKeysPressed = new HashMap<>();
    static {
        steeringKeysPressed.put(GLFW_KEY_RIGHT, false);
        steeringKeysPressed.put(GLFW_KEY_LEFT, false);
        steeringKeysPressed.put(GLFW_KEY_SPACE, false);
    }

    private int currentlyFalling = 0;
    private int immune = 60;

    private Playable backgroundMusic;
    private Playable click;
    private Playable gameoverSound;

    // window handle
    private long window;

    // window size
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    /**
     * Main Function. Will be run from the public static void main(). Here, we initialize all needed objects and start
     * the loop. Also, we call the init function to set up keybindings and some other stuff.
     */
    private void run() {
        try {
            init();

            // initialize each object (extending Renderable) using initObject(object)
            tubus = new Tubus(segments, numEdges);
            gui = new GUI();
            levelGUI = new LevelGUI();
            player = new Insect(tubus.getGround(), numEdges * 0.01f);
            backdrop = new Backdrop();
            backgroundMusic = new Playable("./background.wav", true, 1f);
            click = new Playable("./click.wav", false, 1f);
            gameoverSound = new Playable("./gameover.wav", false, 1f);
            // start with the pause screen to allow the player to prepare
            pause();
            // begin the main loop here in which all the action happens
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

    /**
     * Preparing OpenGL for rendering, the various actions are commented. Setting up key bindings and the window
     * resize listener.
     */
    private void init() {

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
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {

                if(gameOver && key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    restart();
            	else if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop

                if ( key == GLFW_KEY_RIGHT || key == GLFW_KEY_LEFT || key == GLFW_KEY_SPACE ) {
                    steeringKeysPressed.put(key, action != GLFW_RELEASE);
                    if(action == GLFW_PRESS)
                        click.play();
                }
                if ( key == GLFW_KEY_B && action == GLFW_PRESS ) {
                    if(paused) resume();
                    else pause();
                }
                if ( key == GLFW_KEY_BACKSPACE && action == GLFW_RELEASE && paused ) {
                    if(numEdges > 9) numEdges = 3;
                    tubus = new Tubus(segments, ++numEdges);
                    player = new Insect(tubus.getGround(), numEdges * 0.01f);
                    restart();
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
        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
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


    /**
     * The heart of the application, this is run 60 times a second. It will handle game mechanics such as falling
     * through holes and reacting on key presses as well as render all objects with each frame.
     *
     * @throws Exception
     */
    private void loop() throws Exception {

        backgroundMusic.play();

        // initialize some generic variables which can be tweaked to change the player experience
        int maxTurnDegree = 20;
        int turnIncrement = 4;
        float movingIncrement = .03f;
        float posX = 0;
        float posZ = 0.2f;

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {

            if (!paused && currentlyFalling == 0) {
                // =============================== Mechanics =========================================

                tubus.moveZ(speed);
                if(tubus.progress()) levelGUI.increase();
                player.animate();

                // evaluate this.steeringKeysPressed aka make it move like the user asked us to
                if(steeringKeysPressed.get(GLFW_KEY_RIGHT)) {
                    // move
                    player.modifyModel(0,0,0,movingIncrement,0,0);
                    posX += movingIncrement;
                    // turn
                    if(insectAngle < maxTurnDegree) {
                        player.modifyModel(0,turnIncrement,0,0,0,0);
                        insectAngle += turnIncrement;
                    }
                    // wrap if we reach the sides
                    if(!player.isInBounds(-tubus.getWidth() / 2, tubus.getWidth() / 2)) {
                        tubus.turn(true);
                        player.modifyModel(0, 0, 0, -(tubus.getWidth() - player.getBodyWidth()), 0, 0);
                        posX -= (tubus.getWidth() - player.getBodyWidth());
                    }
                }
                if(steeringKeysPressed.get(GLFW_KEY_LEFT)) {
                    // move
                    player.modifyModel(0,0,0,-movingIncrement,0,0);
                    posX -= movingIncrement;
                    // turn
                    if(insectAngle > -maxTurnDegree) {
                        player.modifyModel(0,-turnIncrement,0,0,0,0);
                        insectAngle -= turnIncrement;
                    }
                    // wrap if we reach the sides
                    if (!player.isInBounds(-tubus.getWidth() / 2, tubus.getWidth() / 2)) {
                        tubus.turn(false);
                        player.modifyModel(0, 0, 0, tubus.getWidth() - player.getBodyWidth(), 0, 0);
                        posX += (tubus.getWidth() - player.getBodyWidth());
                    }
                }
                // slowly reset rotation of the insect if there are no keys pressed
                if(!steeringKeysPressed.get(GLFW_KEY_LEFT) && !steeringKeysPressed.get(GLFW_KEY_RIGHT)) {
                    if(insectAngle != 0) {
                        int change = turnIncrement * (insectAngle < 0 ? 1 : -1);
                        insectAngle += change;
                        player.modifyModel(0, change, 0, 0, 0, 0);
                    }
                }
                // initialize jump
                if(steeringKeysPressed.get(GLFW_KEY_SPACE) && !player.isJumping()) {
                    player.jump();
                }
                // call jump animation steps if we're still jumping
                if(player.isJumping())
                    player.doJumpStep();

                // check if we're falling through at the moment
                if(immune > 0) immune--;
                if (tubus.isHole(posX, posZ) && immune == 0 && !player.isJumping()) {  // if we're on a hole and not immune at the moment
                    player.fall();
                    currentlyFalling = 80;
                }
            } else if (!paused && currentlyFalling == 1) {
                // we just fell through, now reset everything and reduce the lives by one
                if(gui.reduceLife()) {
                    gameOver = true;
                    pause();
                }
            	currentlyFalling = 0;
                immune = 60;
                player.reset();
                insectAngle = 0;
                posX = 0;
            } else if(!paused && currentlyFalling > 0) {
                // do a fall animation step until we fell a bit (don't instantly reset, that'd look weird)
                player.fall();
                currentlyFalling -= 1;
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // ================================== Draw object =====================================

            backdrop.render();
            // only render tubus and player with depth test, the rest are UI and backdrop elements which are just
            // rendered in the right order to ensure correct display of the objects
            glEnable(GL_DEPTH_TEST);
            tubus.render();
            player.render();
            glDisable(GL_DEPTH_TEST);
            levelGUI.render();
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

    /**
     * Pause function. It will play the gameover sound if the player has failed and pause the background music.
     */
    private void pause() {
        if(gameOver) gameoverSound.play();
        paused = true;
        gui.pauseUnPause();
        backgroundMusic.pause();
    }

    /**
     * Resume, see pause.
     */
    private void resume() {
        if(gameOver) return;
        paused = false;
        gui.pauseUnPause();
        backgroundMusic.play();
    }

    /**
     * Restart function. Reset all objects in the scene and call the resume function
     */
    private void restart() {
        gameOver = false;
        player.reset();
        tubus.reset();
        levelGUI.reset();
        gui = new GUI();
        resume();
    }
 	
    public static void main(String[] args) {
        new Kubuz().run();
    }
 
}
