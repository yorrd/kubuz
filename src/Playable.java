import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC.createCapabilities;
import static org.lwjgl.openal.ALC10.*;

class Playable {

    private String soundFile = "";
    private boolean loop = false;
    private float gain = 1f;

    IntBuffer buffer = BufferUtils.createIntBuffer(1);
    IntBuffer source = BufferUtils.createIntBuffer(1);
    FloatBuffer sourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
    FloatBuffer sourceVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
    FloatBuffer listenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
    FloatBuffer listenerVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
    FloatBuffer listenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f }).rewind();

    Playable(String sound, boolean loop, float gain) {
        this.soundFile = sound;
        this.loop = loop;
        this.gain = gain;

        if(init() == AL_FALSE)
            System.out.println("Error loading sound.");
    }

    int init() {
        initListener();

        // Load wav data into a buffer.
        alGenBuffers(buffer);

        if(alGetError() != AL_NO_ERROR)
            return AL_FALSE;

        //Loads the wave file from this class's package in your classpath
        if(Objects.equals(soundFile, "")) throw new IllegalStateException("No sound file given");
        WaveData waveFile = WaveData.create(soundFile);
        alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);

        waveFile.dispose();

        // Bind the buffer with the source.
        alGenSources(source);

        if (alGetError() != AL_NO_ERROR)
            return AL_FALSE;

        alSourcei(source.get(0), AL_BUFFER, buffer.get(0));
        alSourcef(source.get(0), AL_PITCH, 1.0f);
        alSourcef(source.get(0), AL_GAIN, gain);
        alSourcefv(source.get(0), AL_POSITION, sourcePos);
        alSourcefv(source.get(0), AL_VELOCITY, sourceVel);
        alSourcei(source.get(0), AL_LOOPING, loop ? AL_TRUE : AL_FALSE);

        // Do another error check and return.
        if (alGetError() == AL_NO_ERROR)
            return AL_TRUE;

        return AL_FALSE;
    }

    void initListener() {
        alListenerfv(AL_POSITION,    listenerPos);
        alListenerfv(AL_VELOCITY,    listenerVel);
        alListenerfv(AL_ORIENTATION, listenerOri);
    }

    void play() {
        alSourcePlay(source.get(0));
    }

    void pause() {
        alSourcePause(source.get(0));
    }

    void destroy() {
        alDeleteSources(source);
        alDeleteBuffers(buffer);
    }
}