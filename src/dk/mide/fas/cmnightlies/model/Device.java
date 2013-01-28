package dk.mide.fas.cmnightlies.model;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Device {
    public enum Build {
        CM9, CM10, CM11
    }

    public final String name;
    public final Build build;

    public Device(String name, Build build) {
        this.name = name;
        this.build = build;
    }

    public boolean isCm9() {
        return Build.CM9 == build;
    }

    public boolean isCm10() {
        return Build.CM10 == build;
    }

    public boolean isCm11() {
        return Build.CM11 == build;
    }

    public int getBuildVersion() {
        return isCm9() ? 9 : isCm10() ? 10 : 11;
    }

    @Override
    public String toString() {
        return name;
    }

    public void save(SharedPreferences prefs) {
        Editor editor = prefs.edit();
        editor.putString("device", name);
        editor.putInt("deviceBuild", build.ordinal());
        editor.commit();
    }

    public static Device restore(SharedPreferences prefs) {
        String name = prefs.getString("device", "galaxys2");
        int ordinal = prefs.getInt("deviceBuild", Build.CM9.ordinal());
        return new Device(name, Build.values()[ordinal]);
    }
}
