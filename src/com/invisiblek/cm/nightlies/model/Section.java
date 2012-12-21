package com.invisiblek.cm.nightlies.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Section implements ListItem {
	public Date d;

	public boolean isSection()
	{
		return true;
	}
	public String getDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.UK);
		return sdf.format(d);
	}
	public Section (Date d) {
		this.d = d;
	}
}
