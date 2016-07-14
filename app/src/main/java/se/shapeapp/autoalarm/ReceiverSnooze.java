package se.shapeapp.autoalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by nille on 21/06/15.
 */
public class ReceiverSnooze extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        context.startService(intent);

        String message = intent.getStringExtra("message");
        Intent myIntent = new Intent(context, ActivitySnooze.class);
        myIntent.putExtra("message", message);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }
}