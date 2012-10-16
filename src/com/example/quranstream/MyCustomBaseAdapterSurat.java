package com.example.quranstream;

import java.io.IOException;
import java.util.ArrayList;

import com.pocketjourney.media.StreamingMediaPlayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MyCustomBaseAdapterSurat extends BaseAdapter {
	private static ArrayList<SearchResults> searchArrayList;

	private LayoutInflater mInflater;
	ViewHolder holder;
	private StreamingMediaPlayer audioStreamer;
	Button play;

	public MyCustomBaseAdapterSurat(Context context,
			ArrayList<SearchResults> results) {
		searchArrayList = results;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return searchArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return searchArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.customlistsurat, null);
			holder = new ViewHolder();
			holder.txtName = (TextView) convertView.findViewById(R.id.name);
			holder.iv = (ImageView) convertView.findViewById(R.id.imageView1);
			holder.play = (ImageButton) convertView
					.findViewById(R.id.imageButton1);
			holder.rep = (ImageButton) convertView
					.findViewById(R.id.imageButton2);
			// / holder.play.setOnClickListener(klikplay);
			// holder.rep.setOnClickListener(klikrep);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.rep.setId(searchArrayList.get(position).getIdRep());
		holder.play.setId(searchArrayList.get(position).getIdPlay());
		holder.txtName.setText(searchArrayList.get(position).getName());
		holder.iv.setBackgroundResource(searchArrayList.get(position)
				.getDrawable());
		return convertView;
	}

	static class ViewHolder {
		TextView txtName;
		ImageView iv;
		ImageButton play;
		ImageButton rep;
	}

	// private OnClickListener klikplay = new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// // int pos = (Integer) v.getTag();
	// // Log.d("a",String.valueOf(pos));
	//
	//
	//
	// // holder.play.setBackgroundResource(searchArrayList.get(0)
	// // .getDrawable());
	// // startStreamingAudio();
	// }
	// };

}