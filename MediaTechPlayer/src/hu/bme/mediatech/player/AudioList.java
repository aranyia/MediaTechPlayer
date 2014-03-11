package hu.bme.mediatech.player;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AudioList extends ListActivity implements OnItemClickListener {
	private ArrayList<Album> albums;
	private ArrayList<AudioTrack> audioTracks;
	private Intent intent = new Intent();
	private static final int SELECT_ALBUM = 0, SELECT_AUDIOTRACK = 1;
	private int request;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getListView().setOnItemClickListener(this);
        
        final Intent in = super.getIntent();
        final String intentAction = in.getAction();
        if(intentAction.equals("mediatechplayer.select.ALBUM")) 
        	request = SELECT_ALBUM;
        else if(intentAction.equals("mediatechplayer.select.AUDIOTRACK"))
        	request = SELECT_AUDIOTRACK;
        
        switch(request) {
        case SELECT_ALBUM:
        	albums = albumQuery();
        	setListAdapter(new ArrayAdapter<Album>(this, android.R.layout.simple_list_item_1, albums));
        	break;
        case SELECT_AUDIOTRACK:
        	Album selectedAlbum = new Album(in.getBundleExtra("data"));
        	audioTracks = audioQuery(selectedAlbum);
        	setListAdapter(new ArrayAdapter<AudioTrack>(this, android.R.layout.simple_list_item_1, audioTracks));
        	break;
        }
    }

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		switch(request) {
        case SELECT_ALBUM:
        	intent.setAction("mediatechplayer.select.AUDIOTRACK");
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		intent.putExtra("data", albums.get(position).toBundle());
        	break;
        case SELECT_AUDIOTRACK:
        	intent.setAction("mediatechplayer.selected.AUDIOTRACK");
        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		intent.putExtra("data", audioTracks.get(position).toBundle());
        	break;
		}
		startActivity(intent);
	}
	
	private ArrayList<Album> albumQuery() {
		ArrayList<Album> albums = new ArrayList<Album>();
		final String[] projection = { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM,
									  MediaStore.Audio.Albums.ALBUM_ART };
		Cursor c = this.getContentResolver().query(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
				projection, null, null, MediaStore.Audio.Albums.ALBUM + " ASC");
    	while(c.moveToNext()) {
    		Album album = new Album(c.getLong(c.getColumnIndex("_id")));
    		album.title = c.getString(c.getColumnIndex("album"));
    		album.albumArtUri = c.getString(c.getColumnIndex("album_art")); 
    		albums.add(album);
    	}
    	
    	checkMissingData(c);
    	c.close();
    	
		return albums;
	}
	
	private ArrayList<AudioTrack> audioQuery() { 
		return audioQuery(null); 
	}
    private ArrayList<AudioTrack> audioQuery(Album selectedAlbum) {
    	ArrayList<AudioTrack> audioTracks = new ArrayList<AudioTrack>();
    	
    	String selection = null;
    	String[] selectionArgs = null;
    	String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " ASC";
    	final String[] projection = { MediaStore.Audio.AudioColumns._ID, MediaStore.Audio.AudioColumns.TITLE,
    								  MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
    								  MediaStore.Audio.AudioColumns.ARTIST, MediaStore.Audio.Media.TRACK,
    								  MediaStore.Audio.AudioColumns.YEAR, MediaStore.Audio.AudioColumns.DURATION,    								  
    								  MediaStore.Audio.AudioColumns.SIZE };   	
  	
    	if(selectedAlbum != null) {
    		selection = MediaStore.Audio.AudioColumns.ALBUM_ID + "= ?";
    		selectionArgs = new String[] { String.valueOf(selectedAlbum.albumID) };
    		sortOrder = MediaStore.Audio.AudioColumns.TRACK + " ASC";
    	}
    	
    	Cursor c = this.getContentResolver().query(
    	          MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
    	          projection, selection, selectionArgs, sortOrder);

    	while(c.moveToNext()) {
    		AudioTrack audioTrack = new AudioTrack(c.getLong(c.getColumnIndex("_id")));
    		audioTrack.title = c.getString(c.getColumnIndex("title"));
    		audioTrack.album = c.getString(c.getColumnIndex("album"));
    		audioTrack.artist = c.getString(c.getColumnIndex("artist"));
    		audioTrack.track = c.getInt(c.getColumnIndex("track"));
    		audioTrack.year = c.getInt(c.getColumnIndex("year"));
    		audioTrack.duration = c.getLong(c.getColumnIndex("duration"));
    		audioTrack.size = c.getLong(c.getColumnIndex("_size"));
    		if(selectedAlbum != null) audioTrack.albumArtUri = selectedAlbum.albumArtUri;
    		audioTracks.add(audioTrack);
    	}
    	
    	checkMissingData(c);
        c.close();
        
        return audioTracks;
    }
    
    private void checkMissingData(Cursor c) {
        if (c.moveToFirst());
        else Toast.makeText(this, getText(R.string.noMedia), Toast.LENGTH_LONG).show();
    }
}
