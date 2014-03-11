package hu.bme.mediatech.player;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

public class AudioTrack {
	public final long audioID;
	public String title;
	public String album;
	public String albumArtUri;
	public String artist;
	public int track;
	public int year;
	public Long duration;
	public Long size;
	
	public AudioTrack(long audioID) {
		this.audioID = audioID;
	}
	
	public AudioTrack(Bundle b) {
		audioID = b.getLong("audioID");
		title = b.getString("title");
		album = b.getString("album");
		albumArtUri = b.getString("albumArtUri");
		artist = b.getString("artist");
		track = b.getInt("track");
		year = b.getInt("year");
		duration = b.getLong("duration");
		size = b.getLong("size");
	}

	public String getDuration()	{
		return String.format("%02d:%02d",
				(int) Math.floor(duration % 3600 / 60),
				duration % 3600 % 60);
	}
	
	public Uri getURI() {
		return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(audioID));
	}
	
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putLong("audioID", audioID);
		b.putString("title", title);
		b.putString("album", album);
		b.putString("albumArtUri", albumArtUri);
		b.putString("artist", artist);
		b.putInt("year", year);
		b.putInt("track", track);
		b.putLong("duration", duration);
		b.putLong("size", size);
		return b;
	}
	
	@Override
	public String toString() {
		return title;
	}
}
