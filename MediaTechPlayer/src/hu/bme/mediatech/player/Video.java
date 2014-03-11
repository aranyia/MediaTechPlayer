package hu.bme.mediatech.player;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

public class Video {
	public final long videoID;
	public Uri imageID;
	public String title;
	public Long duration;
	public Long size;
	
	public Video(long videoID) {
		this.videoID = videoID;
	}
	
	public Video(Bundle b) {
		videoID = b.getLong("videoID");
		title = b.getString("title");
		duration = b.getLong("duration");
		size = b.getLong("size");
	}
	
	public String getSize()	{
		float GB = (float) (size / Math.pow(1024, 3));

		if(GB < 1.0) {
			int MB = (int) Math.round(((double) (size / Math.pow(1024, 2))));
			return String.valueOf(MB) + " MB";
		}
		else return String.valueOf(GB).substring(0,4) + " GB";
	}
	
	public String getDuration()	{
		return String.valueOf(Math.round((duration/(1000*60))));
	}
	
	public Uri getURI() {
		return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(videoID));
	}
	
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putLong("videoID", videoID);
		b.putString("title", title);
		b.putLong("duration", duration);
		b.putLong("size", size);
		return b;		
	}
	
	@Override
	public String toString() {
		return title;		
	}
}