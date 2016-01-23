package ge.gameland.jam2015;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreferencesProvider {

    private static final String READ_POSTS = "READ_POSTS_SET";
    private static final String NOTIFICATIONS_ENABLED = "NOTIFICATIONS_ENABLED";
    private SharedPreferences mPreferences;

    public PreferencesProvider(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public synchronized boolean notificationsEnabled() {
        return mPreferences.getBoolean(NOTIFICATIONS_ENABLED, true);
    }

    public synchronized void notificationsEnabled(boolean state) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(NOTIFICATIONS_ENABLED, state);
        editor.apply();
    }

    public synchronized void addReadPost(String id) {
        if (id == null || id.isEmpty()) return;
        if (!isPostRead(id)) {
            List<String> list = getReadPosts();
            list.add(id);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putStringSet(READ_POSTS, new HashSet<>(list));
            editor.apply();
        }
    }

    public synchronized boolean isPostRead(String id) {
        List<String> readPosts = getReadPosts();
        return readPosts.contains(id);
    }

    public synchronized List<String> getReadPosts() {
        Set<String> set = mPreferences.getStringSet(READ_POSTS, Collections.<String>emptySet());
        return new ArrayList<>(set);
    }

}
