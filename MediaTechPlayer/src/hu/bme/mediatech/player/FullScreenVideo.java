package hu.bme.mediatech.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class FullScreenVideo extends Activity {
	private VideoView view;
	private MediaController mediaController;
	private Video video;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_video);
        
        Intent in = super.getIntent();
        video = new Video(in.getBundleExtra("data"));
        int seekTo = in.getIntExtra("seek", 0);
        
        mediaController = new MediaController(this);
        
        view = (VideoView) findViewById(R.id.fullscreenVideoView);
        view.setMediaController(mediaController);
        view.setVideoURI(video.getURI());
        view.seekTo(seekTo);
        view.start();
    }
}
