import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.openal.AL10.*;

/*
* Basic class for all sounds in the game including background music. Is instantiated for each sound.
*/

class Playable {

    private IntBuffer source = BufferUtils.createIntBuffer(1);

    /**
     * @param sound relative path of the sound file
     * @param loop whether to loop the sound
     * @param gain volume control
     */
    Playable(String sound, boolean loop, float gain) {

        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        alGenBuffers(buffer);

        // load the sound file. Using a class from the lwjgl 2 implementation which they removed
        if(Objects.equals(sound, "")) throw new IllegalStateException("No sound file given");
        WaveData waveFile = WaveData.create(sound);
        alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
        waveFile.dispose();

        // generate the sources
        alGenSources(source);

        // set up listener and source parameters. The listener is the same for all sounds.
        alListenerfv(AL_POSITION,
                (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind());
        alListenerfv(AL_VELOCITY,
                (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind());
        alListenerfv(AL_ORIENTATION,
                (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f }).rewind());
        alSourcefv(source.get(0), AL_POSITION,
                (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind());
        alSourcefv(source.get(0), AL_VELOCITY,
                (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind());
        alSourcei(source.get(0), AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        alSourcei(source.get(0), AL_BUFFER, buffer.get(0));
        alSourcef(source.get(0), AL_PITCH, 1.0f);
        alSourcef(source.get(0), AL_GAIN, gain);
    }

    /**
     * Allow the main class to play the sound at any time.
     */
    void play() {
        alSourcePlay(source.get(0));
    }

    /**
     * Allow the main class to pause the sound at any time.
     */
    void pause() {
        alSourcePause(source.get(0));
    }
}