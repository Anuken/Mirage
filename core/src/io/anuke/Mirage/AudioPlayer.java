package io.anuke.Mirage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.io.Decoder;
import com.badlogic.gdx.audio.io.Mpg123Decoder;
import com.badlogic.gdx.audio.io.VorbisDecoder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AudioPlayer implements Disposable{

	private final int sample = 2048;

	private Decoder decoder;
	private AudioDevice device;

	private boolean playing = false;

	private short[] samples = new short[sample];

	public AudioPlayer() {
		
	}

	private void startPlayback(){
		playing = true;
		Thread playbackThread = new Thread(new Runnable(){
			@Override
			public void run(){
				int readSamples = 0;
				while(playing && (readSamples = decoder.readSamples(samples, 0, samples.length)) > 0){
					device.writeSamples(samples, 0, readSamples);
				}
				if(readSamples == 0)
					playing = false;
			}
		});
		playbackThread.setDaemon(true);
		playbackThread.start();

	}

	public void pause(){
		playing = false;
	}

	public void resume(){
		if(playing)
			throw new GdxRuntimeException("Already playing!");
		if(device == null)
			throw new GdxRuntimeException("No audio file specified!");

		startPlayback();
	}

	public boolean isPlaying(){
		return playing;
	}

	public void playFile(FileHandle file){
		playing = false;

		if(decoder != null)
			decoder.dispose();
		if(device != null)
			device.dispose();

		FileHandle externalFile = null;

		externalFile = Gdx.files.external(".tmp/audio-spectrum.mp3");
		file.copyTo(externalFile);

		if(file.extension().equals("mp3")){
			decoder = new Mpg123Decoder(externalFile);
		}else{
			decoder = new VorbisDecoder(externalFile);
		}

		samples = new short[sample];

		device = Gdx.audio.newAudioDevice(decoder.getRate(), decoder.getChannels() == 1 ? true : false);
		device.setVolume(1f);

		startPlayback();
	}

	@Override
	public void dispose(){
		playing = false;
		device.dispose();
		decoder.dispose();
		Gdx.files.external("tmp/audio-spectrum.mp3").delete();
	}
}
