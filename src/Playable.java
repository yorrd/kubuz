import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.openal.AL10.*;

class Playable {

    private String soundFile = "";
    private boolean loop = false;
    private float gain = 1f;

    // initialize buffers
    private IntBuffer buffer = BufferUtils.createIntBuffer(1);
    private IntBuffer source = BufferUtils.createIntBuffer(1);
    private FloatBuffer sourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
    private FloatBuffer sourceVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
    private FloatBuffer listenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
    private FloatBuffer listenerVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
    private FloatBuffer listenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f }).rewind();

    Playable(String sound, boolean loop, float gain) {
        this.soundFile = sound;
        this.loop = loop;
        this.gain = gain;
        init();
    }

    void init() {

        alGenBuffers(buffer);

        // load the sound file. Using a class from the lwjgl 2 implementation which they removed
        if(Objects.equals(soundFile, "")) throw new IllegalStateException("No sound file given");
        WaveData waveFile = WaveData.create(soundFile);
        alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
        waveFile.dispose();

        // generate the sources
        alGenSources(source);

        // set up listener and source parameters
        alListenerfv(AL_POSITION, listenerPos);
        alListenerfv(AL_VELOCITY,  listenerVel);
        alListenerfv(AL_ORIENTATION, listenerOri);
        alSourcei(source.get(0), AL_BUFFER, buffer.get(0));
        alSourcef(source.get(0), AL_PITCH, 1.0f);
        alSourcef(source.get(0), AL_GAIN, gain);
        alSourcefv(source.get(0), AL_POSITION, sourcePos);
        alSourcefv(source.get(0), AL_VELOCITY, sourceVel);
        alSourcei(source.get(0), AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
    }

    void play() {
        alSourcePlay(source.get(0));
    }

    void pause() {
        alSourcePause(source.get(0));
    }
}