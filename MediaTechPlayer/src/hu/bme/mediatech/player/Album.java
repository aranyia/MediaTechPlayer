package hu.bme.mediatech.player;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

public class Album {
	public final long albumID;
	public String title;
	public String albumArtUri;
	
	public Album(long albumID) {
		this.albumID = albumID;
	}
	
	public Album(Bundle b) {
		albumID = b.getLong("albumID");
		title = b.getString("title");
		albumArtUri = b.getString("albumArtUri");
	}
	
	public Uri getURI() {
		return Uri.parse(albumArtUri);
	}
	
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putLong("albumID", albumID);
		b.putString("title", title);
		b.putString("albumArtUri", albumArtUri);
		return b;
	}
	
	@Override
	public String toString() {
		return title;
	}
}
