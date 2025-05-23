package net.sourceforge.openlightcamera;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/** Handles a history of save locations.
 */
public class SaveLocationHistory {
    private static final String TAG = "SaveLocationHistory";
    private final MainActivity main_activity;
    private final String pref_base;
    private final ArrayList<String> save_location_history = new ArrayList<>();

    /** Creates a new SaveLocationHistory class. This manages a history of save folder locations.
     * @param main_activity MainActivity.
     * @param pref_base String to use for shared preferences.
     * @param folder_name The current save folder.
     */
    SaveLocationHistory(MainActivity main_activity, String pref_base, String folder_name) {
        if( MyDebug.LOG )
            Log.d(TAG, "pref_base: " + pref_base);
        this.main_activity = main_activity;
        this.pref_base = pref_base;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);

        // read save locations
        save_location_history.clear();
        int save_location_history_size = sharedPreferences.getInt(pref_base + "_size", 0);
        if( MyDebug.LOG )
            Log.d(TAG, "save_location_history_size: " + save_location_history_size);
        for(int i=0;i<save_location_history_size;i++) {
            String string = sharedPreferences.getString(pref_base + "_" + i, null);
            if( string != null ) {
                if( MyDebug.LOG )
                    Log.d(TAG, "save_location_history " + i + ": " + string);
                save_location_history.add(string);
            }
        }
        // also update, just in case a new folder has been set
        updateFolderHistory(folder_name, false); // update_icon can be false, as updateGalleryIcon() is called later in MainActivity.onResume()
        //updateFolderHistory("/sdcard/Pictures/OpenCameraTest");
    }

    /** Updates the save history with the current save location (should be called after changing the save location).
     * @param folder_name The folder name to add or update in the history.
     * @param update_icon Whether to update the gallery icon. If false, it's the caller's responsibility to call
     * MainActivity.updateGalleryIcon().
     */
    void updateFolderHistory(String folder_name, boolean update_icon) {
        updateFolderHistory(folder_name);
        if( update_icon ) {
            // If the folder has changed, need to update the gallery icon.
            // Note that if using option to strip all exif tags, we won't be able to find the most recent image - so seems
            // better to stick with the current gallery thumbnail. (Also beware that we call this method when changing
            // non-trivial settings, even if the save folder wasn't actually changed.)
            if( !main_activity.getStorageUtils().getLastMediaScannedHasNoExifDateTime() ) {
                main_activity.updateGalleryIcon();
            }
        }
    }

    /** Updates the save history with the supplied folder name
     * @param folder_name The folder name to add or update in the history.
     */
    private void updateFolderHistory(String folder_name) {
        if( MyDebug.LOG ) {
            Log.d(TAG, "updateFolderHistory: " + folder_name);
            Log.d(TAG, "save_location_history size: " + save_location_history.size());
            for(int i=0;i<save_location_history.size();i++) {
                Log.d(TAG, save_location_history.get(i));
            }
        }
        while( save_location_history.remove(folder_name) ) {
        }
        save_location_history.add(folder_name);
        while( save_location_history.size() > 6 ) {
            save_location_history.remove(0);
        }
        writeSaveLocations();
        if( MyDebug.LOG ) {
            Log.d(TAG, "updateFolderHistory exit:");
            Log.d(TAG, "save_location_history size: " + save_location_history.size());
            for(int i=0;i<save_location_history.size();i++) {
                Log.d(TAG, save_location_history.get(i));
            }
        }
    }

    /** Clears the folder history, and reinitialise it with the current folder.
     * @param folder_name The current folder name.
     */
    void clearFolderHistory(String folder_name) {
        if( MyDebug.LOG )
            Log.d(TAG, "clearFolderHistory: " + folder_name);
        save_location_history.clear();
        updateFolderHistory(folder_name, true); // to re-add the current choice, and save
    }

    /** Writes the history to the SharedPreferences.
     */
    private void writeSaveLocations() {
        if( MyDebug.LOG )
            Log.d(TAG, "writeSaveLocations");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(pref_base + "_size", save_location_history.size());
        if( MyDebug.LOG )
            Log.d(TAG, "save_location_history_size = " + save_location_history.size());
        for(int i=0;i<save_location_history.size();i++) {
            String string = save_location_history.get(i);
            editor.putString(pref_base + "_" + i, string);
        }
        editor.apply();
    }

    /** Return the size of the history.
     * @return The size of the history.
     */
    public int size() {
        return save_location_history.size();
    }

    /** Returns a save location entry.
     * @param index The index to return.
     * @return The save location at this index.
     */
    public String get(int index) {
        return save_location_history.get(index);
    }

    /** Removes a save location entry.
     * @param index The index to remove.
     */
    public void remove(int index) {
        save_location_history.remove(index);
    }

    /** Sets a save location entry.
     * @param index The index to set.
     * @param element The new entry.
     */
    public void set(int index, String element) {
        save_location_history.set(index, element);
    }

    // for testing:
    /** Should be used for testing only.
     * @param value The value to search the location history for.
     * @return Whether the save location history contains the supplied value.
     */
    public boolean contains(String value) {
        return save_location_history.contains(value);
    }
}
