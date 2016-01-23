package ge.gameland.jam2015;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    public static final String FORCE_CHECK_KEY = "FORCE_CHECK";
    private static final String URL_POSTS = "http://jam.gameland.ge/wp-json/wp/v2/posts/";
    private static final String ACCEPTED_TAG_NAME = "mobile";
    private static final int TIME_DELAY = 1800000; // 30 minutes
    private static Timer mTimer;
    private final IBinder mBinder = new NotificationBinder();
    private NotificationProvider mNotificationProvider;
    private PreferencesProvider mPreferencesProvider;

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mNotificationProvider = new NotificationProvider(this);
        mPreferencesProvider = new PreferencesProvider(this);
        startBackgroundTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null
                && intent.getExtras() != null
                && intent.getExtras().getBoolean(FORCE_CHECK_KEY, false))
            forceCheckForNewPosts();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class NotificationBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    private void startBackgroundTask() {
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForNewPosts();
            }
        }, 0, TIME_DELAY);
    }

    public void forceCheckForNewPosts() {
        // This method is normally called from UI thread, so run it on background
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                checkForNewPosts();
            }
        });
    }

    private synchronized void checkForNewPosts() {
        if (!mPreferencesProvider.notificationsEnabled()) return;
        try {
            JSONArray newPosts = new JSONArray();
            JSONArray posts = new JSONArray(httpGET(URL_POSTS));
            int postLen = posts.length();
            for (int i = postLen - 1; i >= 0; i--) {
                JSONObject post = posts.getJSONObject(i);
                String postID = Integer.toString(post.getInt("id"));
                if (!mPreferencesProvider.isPostRead(postID)) {
                    JSONArray tags = new JSONArray(httpGET(URL_POSTS + postID + "/terms/tag"));
                    int tagLen = tags.length();
                    for (int j = 0; j < tagLen; j++) {
                        JSONObject tag = tags.getJSONObject(j);
                        String tagName = tag.getString("slug");
                        if (tagName.toLowerCase().equals(ACCEPTED_TAG_NAME)) {
                            JSONObject postJson = new JSONObject();
                            postJson.put("id", postID);
                            postJson.put("title", post.getJSONObject("title").getString("rendered"));
                            postJson.put("link", post.getString("link"));
                            newPosts.put(postJson);
                            break;
                        }
                    }
                }
            }
            if (newPosts.length() > 0) {
                for (int i = 0; i < newPosts.length(); i++) {
                    JSONObject post = newPosts.getJSONObject(i);
                    String id = post.getString("id");
                    String text = post.getString("title");
                    String link = post.getString("link");
                    mNotificationProvider.show(text, link);
                    mPreferencesProvider.addReadPost(id);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String httpGET(String _url) {
        String finalResult = "";
        InputStreamReader input = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(_url);
            input = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
            reader = new BufferedReader(input);
            String strTemp;
            while (null != (strTemp = reader.readLine())) {
                finalResult = finalResult + strTemp;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (input != null)
                    input.close();
                if (reader != null)
                    reader.close();
            } catch (Exception ignored) {
            }
        }

        return finalResult;
    }

}
