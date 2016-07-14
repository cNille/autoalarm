package se.shapeapp.autoalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;

/**
 * Created by nille on 20/06/15.
 */

public class ReceiverNotification extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        context.startService(intent);

        String message = intent.getStringExtra("message");
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Bedtime soon ")
                .setContentText("In: " + message).setSmallIcon(R.drawable.logga)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLights(Color.RED, 3000, 3000)
                .setVibrate(new long[]{0, 400, 250, 400, 250, 400, 250})
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

}