package hu.bme.mediatech.player;

import java.util.ArrayList;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class VideoList extends ListActivity implements OnItemClickListener {
	private ArrayList<Video> videos;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getListView().setOnItemClickListener(this);
        
        videos = videoQuery();
        setListAdapter(new ArrayAdapter<Video>(this, android.R.layout.simple_list_item_1, videos));
    }
    
    @Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Intent intent = new Intent("mediatechplayer.selected.VIDEO");
		intent.putExtra("data", videos.get(position).toBundle());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);		
	}
    
    private ArrayList<Video> videoQuery() {
    	ArrayList<Video> videos = new ArrayList<Video>();
    	
    	final String[] projection = { MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.TITLE,
    								  MediaStore.Video.VideoColumns.DURATION, MediaStore.Video.VideoColumns.DATE_ADDED, 
    								  MediaStore.Video.VideoColumns.SIZE };    	
    	Cursor c = this.getContentResolver().query(
    	          MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
    	          projection, null, null, MediaStore.Video.VideoColumns.TITLE + " ASC");

    	while(c.moveToNext())
    	{
    		Video film = new Video(c.getLong(c.getColumnIndex("_id")));
    		film.title = c.getString(c.getColumnIndex("title"));
    		film.duration = c.getLong(c.getColumnIndex("duration"));
    		film.size = c.getLong(c.getColumnIndex("_size"));
    		videos.add(film);
    	}        
        
        if (c.moveToFirst());
        else Toast.makeText(this, "Can't find any videos.", Toast.LENGTH_LONG).show();
        c.close();
        
        return videos;
    }
}
