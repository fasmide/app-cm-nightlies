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


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dk.mide.fas.cmnightlies.model.Change;
import dk.mide.fas.cmnightlies.model.Device;
import dk.mide.fas.cmnightlies.model.Device.Build;
import dk.mide.fas.cmnightlies.model.ListItem;
import dk.mide.fas.cmnightlies.model.Section;

public class Service {
	
	private static final String CM9_CHANGES_URL = "http://cm9log-app.appspot.com/changelog/?device=";
	private static final String CM9_DEVICES_URL = "http://cm9log-app.appspot.com/devices/";
	private static final String CM10_CHANGES_URL = "http://cm10log-app.appspot.com/changelog/?device=";
    private static final String CM10_DEVICES_URL = "http://cm10log-app.appspot.com/devices/";
    	
    public static ArrayList<Device> getCm9Devices() {
        return convertToDevice(getDevices(CM9_DEVICES_URL), Build.CM9);
    }
    
   public static ArrayList<Device> getCm10Devices() {
       return convertToDevice(getDevices(CM10_DEVICES_URL), Build.CM10);
    }
    
	private static ArrayList<String> getDevices(String url) {
	    try {
    	    Gson gson = new Gson();
    		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
    		InputStreamReader reader = new InputStreamReader(con.getInputStream());
    		Type collectionType = new TypeToken<ArrayList<String>>(){}.getType();
    		ArrayList<String> liste = gson.fromJson(reader, collectionType);
    		Collections.sort(liste);
    		reader.close();
    		con.disconnect();
    		return liste;
        } catch (Exception e) {
            Log.d(NightliesActivity.TAG, "getDevices exception", e);
            return new ArrayList<String>();
        }
	}
	
	private static ArrayList<Device> convertToDevice(ArrayList<String> names, Build build) {
	    ArrayList<Device> devices = new ArrayList<Device>(names.size());
	    for (String name : names) {
	        devices.add(new Device(name, build));
	    }
	    return devices;
	}
	
    public static String[] convertToArray(ArrayList<Device> devices) {
        String[] array = new String[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
            array[i] = devices.get(i).name;
        }
        return array;
    }
	
	public static ArrayList<ListItem> getChanges(Device device) throws Exception{
		String url = (device.isCm9() ? CM9_CHANGES_URL : CM10_CHANGES_URL) + device.name;
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
