package com.example.quranstream;

import android.graphics.drawable.Drawable;

public class SearchResults {
	private String name = "";
	private String email = "";
	private String pub = "";
	private String nopub = "";
	private String link = "";
	private int drawable = 0;
	private int drawableStar = 0;
	private int id, idStar, favorit;
	private int idplay;
	private int idrep;

	public void setFav(int fav) {
		this.favorit = fav;
	}

	public int getFav() {
		return favorit;
	}

	public void setIdPlay(int id) {
		this.idplay = id;
	}

	public int getIdPlay() {
		return idplay;
	}

	public void setIdRep(int id) {
		this.idrep = id;
	}

	public int getIdRep() {
		return idrep;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setIdStar(int idstar) {
		this.idStar = idstar;
	}

	public int getIdStar() {
		return idStar;
	}

	public void setDrawable(int d) {
		this.drawable = d;
	}

	public int getDrawable() {
		return drawable;
	}

	public void setDrawableStar(int drawables) {
		this.drawableStar = drawables;
	}

	public int getDrawableStar() {
		return drawableStar;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setPub(String publickey) {
		this.pub = publickey;
	}

	public String getPub() {
		return pub;
	}

	public void setNoPub(String noPK) {
		this.nopub = noPK;
	}

	public String getNoPub() {
		return nopub;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

}