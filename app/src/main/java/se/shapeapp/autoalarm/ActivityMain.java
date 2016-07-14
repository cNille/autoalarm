package se.shapeapp.autoalarm;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import se.shapeapp.autoalarm.util.HandlerAlarm;
import se.shapeapp.autoalarm.util.MyToast;
import se.shapeapp.autoalarm.util.Settings;

public class ActivityMain extends ActivitySuper implements CompoundButton.OnCheckedChangeListener {

    private TextView event_tv;
    private TextView bedtime_tv;
    private TextView awake_tv;
    private HandlerAlarm alarmHandler;
    private MyToast toast;
    private Settings settings;
    private CheckBox activate_automatic, custom_alarm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = new Settings(this);
        alarmHandler = new HandlerAlarm(this);
        toast = new MyToast(this);
        event_tv = (TextView)findViewById(R.id.data);
        bedtime_tv = (TextView)findViewById(R.id.textview_bedtime);
        awake_tv = (TextView)findViewById(R.id.textview_awaketime);
        activate_automatic = (CheckBox) findViewById(R.id.activate_automatic);
        activate_automatic.setOnCheckedChangeListener(this);
        custom_alarm = (CheckBox) findViewById(R.id.checkBox_custom_alarm);
        custom_alarm.setOnCheckedChangeListener(this);



    }

    public void onResume(){
        super.onResume();
        if(settings.loadIntSetting("alarm_active", 0) == 1){
            event_tv.setText(settings.loadStringSetting("event_date"));
            bedtime_tv.setText(settings.loadStringSetting("bedtime_date"));
            awake_tv.setText(settings.loadStringSetting("awaketime_date"));
            activate_automatic.setChecked(true);
        }
        if(settings.loadIntSetting("custom_alarm_active", 0) == 1){
            custom_alarm.setChecked(true);
        }
    }

    public void removeAlarmClick(){
        alarmHandler.removeAlarm();
        settings.saveIntSetting("alarm_active", 0);
        bedtime_tv.setText("");
        awake_tv.setText("");
        toast.writeShort("Alarm deactivated.");
    }

    public void addAlarmClick(){
        alarmHandler.addAlarm(null);
        settings.saveIntSetting("alarm_active", 1);
        String str = alarmHandler.getEventDateString();
        settings.saveStringSetting("event_date", str);
        event_tv.setText(str);
        str = alarmHandler.getBedtimeDateString();
        settings.saveStringSetting("bedtime_date", str);
        bedtime_tv.setText(str);
        str = alarmHandler.getAwaketimeDateString();
        settings.saveStringSetting("awaketime_date", str);
        awake_tv.setText(str);
        String msg = alarmHandler.getTimeToBedtime() + "\n" + alarmHandler.getTimeToWaketime();
        toast.writeLong("Alarm activated.\n" + msg);
    }

    public void setCustomAlarm(){

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                settings.saveIntSetting("custom_alarm_hour", hourOfDay);
                settings.saveIntSetting("custom_alarm_minute", minute);
                updateCustomAlarm();
            }
        }, 12, 0, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void updateCustomAlarm(){
        int hour = settings.loadIntSetting("custom_alarm_hour", 12);
        int minute = settings.loadIntSetting("custom_alarm_minute", 0);

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        if(hour == calendar.get(Calendar.HOUR_OF_DAY)){
            if(minute <= calendar.get(Calendar.MINUTE)) {
                calendar.add(Calendar.DATE, 1);
            }
        } else if(hour < calendar.get(Calendar.HOUR_OF_DAY)){
            calendar.add(Calendar.DATE, 1);
        }
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Date customDate = new Date(calendar.getTimeInMillis());

        alarmHandler.addAlarm(customDate);

        // Update TextView
        TextView custom_alarm_time = (TextView) findViewById(R.id.custom_alarm_time);
        String m = minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute);
        String h = hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour);
        custom_alarm_time.setText(h + ":" + m);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.equals(activate_automatic)){
            if (isChecked){
                addAlarmClick();
            } else{
                removeAlarmClick();
            }
        } else if(buttonView.equals(custom_alarm)){
            if (isChecked){
                settings.saveIntSetting("custom_alarm_active", 1);
                setCustomAlarm();
            } else{
                settings.saveIntSetting("custom_alarm_active", 0);
            }
        }
    }
}