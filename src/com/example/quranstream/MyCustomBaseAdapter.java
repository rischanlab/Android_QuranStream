package com.example.quranstream;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyCustomBaseAdapter extends BaseAdapter {
	private static ArrayList<SearchResults> searchArrayList;

	private LayoutInflater mInflater;

	public MyCustomBaseAdapter(Context context, ArrayList<SearchResults> results) {
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
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.customlist, null);
			holder = new ViewHolder();
			holder.txtName = (TextView) convertView.findViewById(R.id.name);
			holder.ib = (ImageButton) convertView.findViewById(R.id.ib);
			holder.bStar = (Button) convertView.findViewById(R.id.bStar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txtName.setText(searchArrayList.get(position).getName());
		holder.ib.setBackgroundResource(searchArrayList.get(position)
				.getDrawable());
		holder.ib.setId(searchArrayList.get(position).getId());
		holder.bStar.setId(searchArrayList.get(position).getIdStar());
//		holder.bStar.setBackgroundResource(searchArrayList.get(position)
//				.getDrawableStar());
		return convertView;
	}

	static class ViewHolder {
		TextView txtName;
		ImageButton ib;
		Button bStar;
	}
}