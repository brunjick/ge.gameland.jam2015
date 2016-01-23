package ge.gameland.jam2015;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class NotificationProvider {

    private static NotificationCompat.Builder mNotification;
    private static String sTitle;
    private Context mContext;

    public NotificationProvider(Context context) {
        mContext = context;
        sTitle = mContext.getResources().getString(R.string.notification_title);
        if (mNotification == null) {
            mNotification = new NotificationCompat.Builder(context);
            mNotification.setAutoCancel(true);
            mNotification.setSmallIcon(R.mipmap.ic_launcher);
        }
    }

    public void show(String text, String link) {
        long uniqueID = System.currentTimeMillis();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        mNotification.setWhen(uniqueID);
        mNotification.setSound(soundUri);
        mNotification.setContentTitle(sTitle);
        mNotification.setContentText(text);

        Intent intent = new Intent(mContext, NotificationReceiverActivity.class);
        intent.putExtra(MainActivity.EXTRA_POST_LINK, link);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) uniqueID, mNotification.build());
    }

}
