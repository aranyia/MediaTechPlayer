package hu.bme.mediatech.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PlayerService extends Service {
	private static final String TAG = "Audio Player Service";
	private static Context context;	
	private static AudioTrack audioTrack;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		context = getApplicationContext();
		Log.i(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	if(intent != null && intent.hasExtra("data"))
    		audioTrack = new AudioTrack(intent.getBundleExtra("data"));
    	
    	if(audioTrack != null) {
    	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, 
    								  new Intent(context, MainActivity.class)
    								  .setAction("").setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    	CharSequence metadata = String.format("%s (%s)", audioTrack.album, audioTrack.artist);
    	Notification notification = new Notification(R.drawable.play, null, 0);
    	notification.flags |= Notification.FLAG_ONGOING_EVENT;
     	notification.setLatestEventInfo(context, 
     			audioTrack.title, metadata, contentIntent);

    	startForeground(R.string.serviceName, notification);
    	}
    	
		Log.i(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }
}
