package ge.gameland.jam2015;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class StartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(context, NotificationService.class);
                i.putExtra(NotificationService.FORCE_CHECK_KEY, true);
                context.startService(i);
            }
        }, 2000);
    }
}
