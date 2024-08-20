package flowtimer;

import static org.lwjgl.openal.AL10.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.util.WaveData;

public class OpenAL {

	private static final float PITCH = 1.0f;
	private static final float GAIN = 1.0f;
	private static final FloatBuffer SOURCE_POSITION = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer SOURCE_VELOCITY = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer LISTENER_POSITION = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer LISTENER_VELOCITY = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
	private static final FloatBuffer LISTENER_ORIENTATION = (FloatBuffer) BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f }).rewind();

	private static HashMap<String, Integer> loadedSounds;
	private static ArrayList<Integer> bufferList;
	private static ArrayList<Integer> sourceList;

	private static ALCapabilities _alCapabilities;
	private static long device;
	private static long context;

	public static void init() {
		try {
			device = ALC10.alcOpenDevice((ByteBuffer) null);
			ALCCapabilities alcCapabilities = ALC.createCapabilities(device);

			context = ALC10.alcCreateContext(device, (IntBuffer) null);
			ALC10.alcMakeContextCurrent(context);

 			_alCapabilities = AL.createCapabilities(alcCapabilities);

			alListenerfv(AL_POSITION, LISTENER_POSITION);
			alListenerfv(AL_VELOCITY, LISTENER_VELOCITY);
			alListenerfv(AL_ORIENTATION, LISTENER_ORIENTATION);
			loadedSounds = new HashMap<>();
			bufferList = new ArrayList<>();
			sourceList = new ArrayList<>();
		} catch (Exception e) {
			ErrorHandler.handleException(e, false);
		}
	}

	public static int createSource(String filePath) throws Exception {
		if(loadedSounds.containsKey(filePath)) {
			return loadedSounds.get(filePath);
		}
		return createSourceInternal(filePath, WaveData.create(OpenAL.class.getResource(filePath)));
	}
	
	public static int createSource(File file) throws Exception {
		if(loadedSounds.containsKey(file.getPath())) {
			return loadedSounds.get(file.getPath());
		}
		return createSourceInternal(file.getPath(), WaveData.create(new BufferedInputStream(new FileInputStream(file))));
	}
	
	private static int createSourceInternal(String filePath, WaveData waveFile) throws Exception {
		int buffer = alGenBuffers();
		int source = alGenSources();
		alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		if(alGetError() != AL_NO_ERROR) {
			throw new RuntimeException("Error while loading audio file! " + filePath);
		}
		alSourcei(source, AL_BUFFER, buffer);
		alSourcef(source, AL_PITCH, PITCH);
		alSourcef(source, AL_GAIN, GAIN);
		alSourcefv(source, AL_POSITION, SOURCE_POSITION);
		alSourcefv(source, AL_VELOCITY, SOURCE_VELOCITY);
		loadedSounds.put(filePath, source);
		bufferList.add(buffer);
		sourceList.add(source);
		return source;
	}

	public static void playSource(int source) {
		alSourcePlay(source);
	}

	public static void dispose() {
		bufferList.forEach(AL10::alDeleteBuffers);
		sourceList.forEach(AL10::alDeleteSources);
		ALC10.alcDestroyContext(context);
 		ALC10.alcCloseDevice(device);
	}
}