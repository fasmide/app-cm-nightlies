package dk.mide.fas.cmnightlies;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dk.mide.fas.cmnightlies.model.Change;
import dk.mide.fas.cmnightlies.model.ListItem;
import dk.mide.fas.cmnightlies.model.Section;

public class Service {

	String changesUrl = "http://cm10log-app.appspot.com/changelog/?device=";
	String devicesUrl = "http://cm10log-app.appspot.com/devices/";
	public ArrayList<String> getDevices() throws Exception {
		Gson gson = new Gson();

		HttpURLConnection con = (HttpURLConnection) new URL(devicesUrl).openConnection();

		InputStreamReader reader = new InputStreamReader(con.getInputStream());
		Type collectionType = new TypeToken<ArrayList<String>>(){}.getType();
		ArrayList<String> liste = gson.fromJson(reader, collectionType);
		Collections.sort(liste);
		reader.close();
		con.disconnect();
		return liste;

	}
	public ArrayList<ListItem> getChanges(String device) throws Exception{
		String url = changesUrl + device;
		Gson gson = new Gson();
		HttpURLConnection connection = 
				(HttpURLConnection) new URL(url).openConnection();

		InputStreamReader reader = new InputStreamReader(connection.getInputStream());
		Type collectionType = new TypeToken<ArrayList<Change>>(){}.getType();
		ArrayList<Change> liste = gson.fromJson(reader, collectionType);

		ArrayList<ListItem> sectionedList = new ArrayList<ListItem>(liste.size());
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		Date lastDate = null;
		for (Change change : liste) {
			if(lastDate == null)
			{
				lastDate = df.parse(change.last_updated);
				sectionedList.add(new Section(lastDate));
				sectionedList.add(change);
			} else {
				Calendar cal1 = Calendar.getInstance();
				Calendar cal2 = Calendar.getInstance();
				cal1.setTime(lastDate);
				Date parsedDate = df.parse(change.last_updated);
				cal2.setTime(parsedDate);
				boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
				if(sameDay) {
					sectionedList.add(change);
				} else {
					sectionedList.add(new Section(parsedDate));
					sectionedList.add(change);
					lastDate = parsedDate;
				}
			}
		}
		reader.close();
		connection.disconnect();

		return sectionedList;
	}

}
