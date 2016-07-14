package se.shapeapp.autoalarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import se.shapeapp.autoalarm.util.AlarmType;
import se.shapeapp.autoalarm.util.HandlerAlarm;
import se.shapeapp.autoalarm.util.Settings;

/**
 * Created by nille on 23/06/15.
 */
public class ActivitySnooze extends Activity {

    public MediaPlayer mPlayer;
    private PowerManager.WakeLock wakelock;
    private HandlerAlarm alarmHandler;
    private Settings settings;
    protected String msg;
    protected int type;
    private TextView snoozeText;
    protected boolean isWakeUpAlarm;
    static final int ALARM_REQUEST = 1;  // The request code


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarm_layout);

        settings = new Settings(this);
        alarmHandler = new HandlerAlarm(this);
        this.snoozeText = (TextView) findViewById(R.id.snooze_text);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");
        wakelock.acquire();
        setSound();

        type = settings.loadIntSetting("alarm_type", 0);
        //type = getIntent().getIntExtra("type", 0);
        msg = getIntent().getStringExtra("message");
        isWakeUpAlarm = msg.contains("wakeup");
        if(isWakeUpAlarm){
            sleepMode(false);
            startSnoozeActivity(type);
        } else {
            startSnoozeActivity(AlarmType.NORMAL);
        }
    }

    private void startSnoozeActivity(int type){
        Intent i;
        if(type == AlarmType.BARCODE){
            i = new Intent(this, ActivityAlarmBarcode.class);
            startActivityForResult(i, ALARM_REQUEST);
        } else { //Default case
            i = new Intent(this, ActivityAlarmStandard.class);
            startActivityForResult(i, ALARM_REQUEST);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ALARM_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                this.turnOff();
            } else {
                sleepMode(false);
                startSnoozeActivity(type);
            }
        }
    }

    private void sleepMode(boolean sleepMode){
        //Sets the screen brightness
        int bright = sleepMode ? 10 : 250;
        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, bright);

        //Sets phone in silent mode when in sleepmode.
        int mode = sleepMode ? AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_NORMAL ;
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(mode);
    }

    public void turnOff() {
        silentSnooze();
        wakelock.release();
        if(settings.loadIntSetting("automatic", 0) == 1 && msg.contains("wakeup"))
            this.setNextAlarm();

        if(!isWakeUpAlarm)
            sleepMode(true);
        //this.finish();
    }

    public void cancelClick(View v){
        this.finish();
    }

    public void snoozeClick(View v){
        //TODO: Implement.
    }

    private void setNextAlarm(){
        // Make sure all pending intent are removed.
        alarmHandler.removeAlarm();

        // Create a new alarm for the next day.
        alarmHandler.addAlarm(null);
    }

    private void silentSnooze() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer = null;
        }
    }

    private void setSound() {
        // Create Media Player for sounding the system alarm
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(this, alert);
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mPlayer.setLooping(true);
            mPlayer.setVolume(0.1f, 0.1f);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            // TODO : Implement Error Checking
        }
    }
}
