package com.example.quranstream;

import java.io.IOException;
import java.util.ArrayList;

import com.pocketjourney.media.StreamingMediaPlayer;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SuratActivity extends Activity implements OnItemClickListener {
	ListView lv1;
	// SearchResults sr1;
	MyCustomBaseAdapter cAdapter;
	ArrayList<SearchResults> searchResults;
	DataBaseHelper dm;
	String[] namareciter;
	ArrayList<SearchResults> results;
	private boolean isPlaying;
	ImageButton play, rep;
	private StreamingMediaPlayer audioStreamer;
	boolean b = false;
	boolean b2 = false;
	boolean r = false;
	int ir = 2;
	SharedPreferences sp;
	SharedPreferences.Editor spe;
	String reciterlink;
	int i, fav;
	ImageView reciters;
	ProgressBar pbs;

	private NotificationManager mNotificationManager;
	private int SIMPLE_NOTFICATION_ID;
	Notification notifyDetails;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stream);
		reciters = (ImageView) findViewById(R.id.title_reciters);
		reciters.setBackgroundResource(R.drawable.ic_menu_bookmarked_normal);
		pbs = (ProgressBar) findViewById(R.id.pbStream);
		pbs.setVisibility(View.GONE);
		// lv1 = (ListView) findViewById(R.id.lview);
		results = new ArrayList<SearchResults>();
		// sr1 = new SearchResults();
		sp = this.getSharedPreferences("dataLink", 0);
		spe = sp.edit();
		reciterlink = sp.getString("linkreciters", "");
		fav = sp.getInt("fav", 1);
		Log.d("favorit", String.valueOf(fav));
		if (fav == 1) {
			reciters.setBackgroundResource(R.drawable.ic_menu_bookmarked_normal);
		} else {
			reciters.setBackgroundResource(R.drawable.ic_menu_normal);

		}
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		String nama = sp.getString("namareciters", "");
		TextView t = (TextView) findViewById(R.id.reciters);
		t.setText(nama);
		// Log.d("link --->", reciterlink);
		dm = new DataBaseHelper(this);
		try {
			dm.createDataBase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}

		try {
			dm.openDataBase();
		} catch (SQLException sqle) {
			throw sqle;
		}
		ArrayList<SearchResults> data = ambil();

		namareciter = new String[data.size()];
		// Log.d("namareciter", String.valueOf(namareciter.length));

		final ListView lv1 = (ListView) findViewById(R.id.lview);
		lv1.setAdapter(new MyCustomBaseAdapterSurat(this, data));
		lv1.setDivider(null);
		lv1.setDividerHeight(0);
		lv1.setOnItemClickListener(this);
		lv1.setCacheColorHint(Color.TRANSPARENT);
		lv1.requestFocus(0);
		// Log.d("kkk", String.valueOf(audioStreamer.getMediaPlayer().));

	}

	private ArrayList<SearchResults> ambil() {
		// TODO Auto-generated method stub
		ArrayList<ArrayList<Object>> data = dm.AmbilSurat();
		namareciter = new String[data.size()];

		for (int i = 0; i < data.size(); i++) {
			ArrayList<Object> b = data.get(i);
			// Log.d("b.get(0).toString()", b.get(0).toString());
			SearchResults s = new SearchResults();
			s.setName(b.get(1).toString());
			s.setIdPlay(i);
			s.setIdRep(i);
			s.setLink(b.get(3).toString());
			s.setDrawable(R.drawable.ic_button_normal);
			results.add(s);
		}
		return results;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), results.get(arg2).getName(),
				Toast.LENGTH_SHORT).show();
		// SearchResults s = new SearchResults();
		// s.setDrawable(R.drawable.ic_play_on);

	}

	public void playbtn(View v) {
		int m = 1;
		String g = results.get(v.getId()).getLink();
		play = (ImageButton) v;

		// b=false , b2=false
		if (!b && !b2) {// ON false,false
			startStreamingAudio(reciterlink + g, m);
			// audioStreamer.getMediaPlayer().setLooping(true);
			v.setBackgroundResource(R.drawable.ic_play_on);
			b = true;
			return;
		} else if (b && !b2) {// pause true,false

			audioStreamer.getMediaPlayer().pause();
			v.setBackgroundResource(R.drawable.ic_play_off);

			b = false;
			b2 = true;
			return;

		} else if (!b && b2) {// resume false true
			audioStreamer.getMediaPlayer().start();
			// audioStreamer.startPlayProgressUpdater();
			v.setBackgroundResource(R.drawable.ic_play_on);
			Log.d("play", "b");
			b = true;
			b2 = true;
			return;
		} else if (b && b2) { // false
			audioStreamer.getMediaPlayer().stop();
			v.setBackgroundResource(R.drawable.ic_play_off);

			b = false;
			b2 = false;
			return;
		}
		// if (audioStreamer.getMediaPlayer().isPlaying()) {
		// audioStreamer.getMediaPlayer().pause();
		// v.setBackgroundResource(R.drawable.ic_play_on);
		// } else {
		// audioStreamer.getMediaPlayer().start();
		// audioStreamer.startPlayProgressUpdater();
		// v.setBackgroundResource(R.drawable.ic_play_off);
		// }
		// isPlaying = !isPlaying;

	}

	public void repeatbtn(View v) {
		int m = 2;
		rep = (ImageButton) v;
		Log.d("klik:", "btn repeat");

		// Log.d("ir", String.valueOf(i));

		switch (ir) {
		case 2:

			v.setBackgroundResource(R.drawable.ic_repeatsatu_on);
			i = ir++;
			startStreamingAudio("", 2);
			break;

		case 3:
			v.setBackgroundResource(R.drawable.ic_repeatdua_on);
			i = ir++;
			startStreamingAudio("", 2);
			break;
		case 4:

			v.setBackgroundResource(R.drawable.ic_repeattiga_on);
			i = ir++;
			startStreamingAudio("", 2);
			break;
		case 5:
			v.setBackgroundResource(R.drawable.ic_share_off);
			ir = 2;
			break;
		}

	}

	private void startStreamingAudio(String url, int mode) {

		// audioStreamer
		// .startStreaming(
		// "http://www.pocketjourney.com/downloads/pj/tutorials/audio.mp3",
		// 1717, 214);
		//
		audioStreamer = new StreamingMediaPlayer(this, null, play, null, i, pbs,mNotificationManager);
		if (mode == 1) {
			audioStreamer.startStreaming(url, 5208, 216);
		} else {
			audioStreamer.setrepeat(i);
			// audioStreamer.getMediaPlayer().setLooping(true);
		}

	}
}
