package com.example.quranstream;

import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	ListView lv1;
	// SearchResults sr1;
	MyCustomBaseAdapter cAdapter;
	// ArrayList<SearchResults> searchResults;
	DataBaseHelper dm;
	String[] namareciter;
	ArrayList<SearchResults> results;
	boolean b = false;
	boolean c = false;
	SharedPreferences sp;
	SharedPreferences.Editor spe;
	ArrayList<SearchResults> data;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// lv1 = (ListView) findViewById(R.id.listView1);
		results = new ArrayList<SearchResults>();
		// sr1 = new SearchResults();

		sp = this.getSharedPreferences("dataLink", 0);
		spe = sp.edit();

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
		tampilkanDataReciters();

	}

	private void tampilkanDataReciters() {
		// TODO Auto-generated method stub

		data = ambil();

		namareciter = new String[data.size()];
		// Log.d("namareciter", String.valueOf(namareciter.length));

		ListView lv1 = (ListView) findViewById(R.id.lv1);

		cAdapter = new MyCustomBaseAdapter(this, data);
		cAdapter.notifyDataSetChanged();
		lv1.setAdapter(cAdapter);
		lv1.setDivider(null);
		lv1.setDividerHeight(0);
		lv1.setCacheColorHint(Color.TRANSPARENT);
		lv1.requestFocus(0);
	}

	private ArrayList<SearchResults> ambil() {
		// TODO Auto-generated method stub
		ArrayList<ArrayList<Object>> data = dm.AmbilReciters();
		namareciter = new String[data.size()];

		for (int i = 0; i < data.size(); i++) {
			ArrayList<Object> b = data.get(i);
			// Log.d("b.get(0).toString()", b.get(0).toString());
			SearchResults s = new SearchResults();
			s.setId(Integer.parseInt(b.get(0).toString()));
			s.setName(b.get(1).toString());
			s.setLink(b.get(3).toString());
			s.setFav(Integer.parseInt(b.get(4).toString()));
			if (b.get(4).toString().equals("1")) {
				s.setDrawable(R.drawable.ic_menu_bookmarked_normal);
				//s.setDrawableStar(R.drawable.rate_star_med_on);
			} else {
				s.setDrawable(R.drawable.ic_menu_normal);
				//s.setDrawableStar(R.drawable.rate_star_med_off);
			}
			s.setIdStar(Integer.parseInt(b.get(0).toString()));
			results.add(s);
		}
		return results;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void klikreciters(View v) {

		ArrayList<Object> b =  dm.ambilBaris(v.getId());

		Log.d("id", String.valueOf(v.getId()));
		int y = v.getId() + 1;
		Log.d("favor", String.valueOf(b.get(3).toString()));
		Log.d("reciters", String.valueOf(b.get(1).toString()));
		Log.d("link", String.valueOf(b.get(2).toString()));
		spe.putString("linkreciters", b.get(2).toString());
		spe.putString("namareciters", b.get(1).toString());
		spe.putInt("fav",Integer.parseInt(b.get(3).toString()));
		spe.commit();
		startActivity(new Intent(this, SuratActivity.class));

		// if (!b) {// on
		// spe.putString("linkreciters", a);
		// spe.putString("namareciters", results.get(y).getName());
		// spe.commit();
		// v.setBackgroundResource(R.drawable.ic_menu_bookmarked_normal);
		// b = true;
		//
		// startActivity(new Intent(this, SuratActivity.class));
		// } else {
		//
		// v.setBackgroundResource(R.drawable.ic_menu_normal);
		// b = false;
		// }

	}

	public void klikStar(View vi) {
		// Log.d("id start", String.valueOf(vi.getId()));
		int y = vi.getId() - 1;

		if (!c) {
			vi.setBackgroundResource(R.drawable.rate_star_med_on);
			dm.setFavorit(vi.getId(), 1);
			// spe.putInt("fav", results.get(y).getFav());
			// spe.commit();
			c = true;
			data.clear();
			tampilkanDataReciters();

			// startActivity(new Intent(this, SuratActivity.class));
		} else {

			vi.setBackgroundResource(R.drawable.rate_star_med_off);
			dm.setFavorit(vi.getId(), 0);
			c = false;
			data.clear();
			tampilkanDataReciters();
		}

	}
	// @Override
	// public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
	// long arg3) {
	// // TODO Auto-generated method stub
	// Toast.makeText(MainActivity.this, "b", Toast.LENGTH_SHORT).show();
	//
	// }
	//
	// @Override
	// public void onNothingSelected(AdapterView<?> arg0) {
	// // TODO Auto-generated method stub
	//
	// Toast.makeText(MainActivity.this, "c", Toast.LENGTH_SHORT).show();
	// }

}
