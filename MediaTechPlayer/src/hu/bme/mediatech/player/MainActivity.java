package hu.bme.mediatech.player;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

public class MainActivity extends Activity implements OnClickListener, OnCompletionListener, 
													  OnPreparedListener, SurfaceHolder.Callback, OnVideoSizeChangedListener {
	private Context context;
	private MediaPlayer player;
	private SurfaceView view;
	private SurfaceHolder holder;
	private ImageButton playPause, stop;
	private Button videos, music;
	private SeekBar bar;
	private Video video;
	private AudioTrack audioTrack;
	private boolean readyToPlay;
	private Intent openListIntent = new Intent().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	private TextView timeElapsed, timeRemaining;
	private Handler handler;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = getApplicationContext();
        
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnVideoSizeChangedListener(this);
     
        view = (SurfaceView) findViewById(R.id.surfaceView);
        holder = view.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this);

        playPause = (ImageButton) findViewById(R.id.buttonPlayPause);
        stop = (ImageButton) findViewById(R.id.buttonStop);        
        videos = (Button) findViewById(R.id.buttonVideos);
        music = (Button) findViewById(R.id.buttonMusic);
        
        timeElapsed = (TextView) findViewById(R.id.timeElapsed);
        timeRemaining = (TextView) findViewById(R.id.timeRemaining);
        
        view.setOnClickListener(this);
        playPause.setOnClickListener(this);
        stop.setOnClickListener(this);
        videos.setOnClickListener(this);
        music.setOnClickListener(this);
        
        bar = (SeekBar) findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				if(fromUser) {
					player.seekTo(progress);
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) { }
			@Override
			public void onStopTrackingTouch(SeekBar bar) { }        	
        });
        
        handler = new Handler() {
        	public void handleMessage(Message msg) {
        		refreshCounter(msg.arg1, msg.arg2);
        	}};  
        
        final Intent in = super.getIntent();
        final String intentAction = in.getAction();
        if(intentAction != null) {
		    if(intentAction.equals("mediatechplayer.selected.VIDEO")) {
		        	video = new Video(in.getBundleExtra("data"));
		    }
		    else if(intentAction.equals("mediatechplayer.selected.AUDIOTRACK")) {
		        	audioTrack = new AudioTrack(in.getBundleExtra("data"));
		    }
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
        if(player != null) player.release();
        if(audioTrack != null) stopService(new Intent(context, PlayerService.class));
    }
    
    private void prepareFile(Uri uri, boolean isVideo) throws IllegalStateException, IOException {
    	if(isVideo) {
    		player.setDisplay(holder);
    		player.setScreenOnWhilePlaying(true);
    	}
    	if(!isVideo) { 
    		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        	player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
    	}
    	player.setDataSource(context, uri);
    	player.prepare();
    	displayMetadata(video != null);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonPlayPause:
    		if(player.isPlaying()) pausePlayback();
    		else startPlayback();
			break;
		case R.id.buttonStop:
			stopPlayback();
			break;
		case R.id.buttonVideos:
	    	openListIntent.setAction("mediatechplayer.select.VIDEO");
	    	startActivity(openListIntent);
			break;
		case R.id.buttonMusic:
			openListIntent.setAction("mediatechplayer.select.ALBUM");
	    	startActivity(openListIntent);
			break;
		case R.id.surfaceView:
			openListIntent.setAction("mediatechplayer.fullscreen.VIDEO");
			openListIntent.putExtra("data", video.toBundle());
			openListIntent.putExtra("seek", player.getCurrentPosition());
			startActivity(openListIntent);
			player.pause();
			break;
		}		
	}
	
	private Thread monitorProgress() {
		return new Thread(new Runnable() {
			@Override
			public void run() {
	            int currentPosition = -1, total, tElapsed, tRemaining;
            	total = player.getDuration();
	            bar.setMax(total);
	            
	            while (player != null && currentPosition < total) {
	                try {
	                    Thread.sleep(100);
	                    currentPosition = player.getCurrentPosition();
	                    tElapsed = currentPosition / 1000;
	                    tRemaining = (total / 1000) - tElapsed;
	                    handler.obtainMessage(1, tElapsed, tRemaining).sendToTarget();
	                } catch (InterruptedException e) { return; } 
	                  catch (Exception e) { return; }
	                bar.setProgress(currentPosition);
	            }
	        }
		});
	}
	
	private void refreshCounter(int tElapsed, int tRemaining) {
        timeElapsed.setText(String.format("%d:%02d:%02d",
        		(int) Math.floor(tElapsed / 3600), 
        		(int) Math.floor(tElapsed % 3600 / 60), tElapsed % 3600 % 60));
        timeRemaining.setText(String.format("-%d:%02d:%02d",
        		(int) Math.floor(tRemaining / 3600), 
        		(int) Math.floor(tRemaining % 3600 / 60), tRemaining % 3600 % 60));		
	}
	
	private void displayMetadata(boolean isVideo) {
		TextView title = (TextView) findViewById(R.id.textViewTitle);
		TextView album = (TextView) findViewById(R.id.textViewAlbum);
		ImageView albumArt = (ImageView) findViewById(R.id.imageViewAlbumArt);
		TextView artist = (TextView) findViewById(R.id.textViewArtist);
		TextView duration = (TextView) findViewById(R.id.textViewDuration);
		TextView size = (TextView) findViewById(R.id.textViewSize);
		
			title.setVisibility(View.VISIBLE);
		if(isVideo)	{
			title.setText(video.title);
			duration.setText(video.getDuration() +" "+ getText(R.string.minutes));
			duration.setVisibility(View.VISIBLE);
			size.setText(video.getSize());
			size.setVisibility(View.VISIBLE);
			albumArt.setVisibility(View.GONE);
		}
		else { 
			title.setText(audioTrack.title);
			album.setText(audioTrack.album);
			album.setVisibility(View.VISIBLE);
			artist.setText(audioTrack.artist);
			artist.setVisibility(View.VISIBLE);			
			albumArt.setImageURI(Uri.parse(audioTrack.albumArtUri));
			albumArt.setVisibility(View.VISIBLE);
		}
	}
	
	public void startPlayback() {
		if(readyToPlay) {
			player.start();
			playPause.setImageResource(R.drawable.pause);
		}
		if(audioTrack != null) {
			Intent serviceIntent = new Intent(context, PlayerService.class)
									.putExtra("data", audioTrack.toBundle());		
			startService(serviceIntent);
		}
	}
	
	public void pausePlayback() {
		player.pause();
		playPause.setImageResource(R.drawable.play);
		if(audioTrack != null) {
			stopService(new Intent(context, PlayerService.class));
		}
	}
	
	public void stopPlayback() {
		player.seekTo(0);
		player.pause();
		playPause.setImageResource(R.drawable.play);
		if(audioTrack != null) {
			stopService(new Intent(context, PlayerService.class));
		}
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		monitorProgress().start();
		readyToPlay = true;
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		stopPlayback();
		if(audioTrack != null) stopService(new Intent(context, PlayerService.class));
	}
	
	@Override
    public void onVideoSizeChanged(MediaPlayer player, int width, int height) { }
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        try {
	        if(video != null)
				prepareFile(video.getURI(), true);
	        else if(audioTrack != null)
	        	prepareFile(audioTrack.getURI(), false);
		} catch (IllegalStateException e) { } 
		  catch (IOException e) { }   
        
		float videoWidth = player.getVideoWidth();
        float videoHeight = player.getVideoHeight();        
        float screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width =(int) screenWidth;
        lp.height = (int) ((videoHeight / videoWidth) * screenWidth);
        
        view.setLayoutParams(lp);
        holder.setFixedSize(lp.width, lp.width);
        startPlayback();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) { }
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) { }
}