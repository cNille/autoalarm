package se.shapeapp.autoalarm.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

import java.text.Format;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import se.shapeapp.autoalarm.ReceiverNotification;
import se.shapeapp.autoalarm.ReceiverSnooze;

/**
 * Created by nille on 21/06/15.
 */
public class HandlerAlarm {

    private Settings settings;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private Context context;
    private HandlerCalendar calendarHandler;
    private static final int alarmId = 123456;
    private int numberOfNotification = 2;
    private int notificationPreTime = 900000; //In millis
    private int hoursSleep, minutesAwakening, maxHour, maxMinute, minHour, minMinute;
    private Date eventTime;
    private Date bedTime;
    private Date awakeTime;
    private Format df, tf;

    private boolean isTesting = false;

    public HandlerAlarm(Context context){
        this.context = context;
        calendarHandler = new HandlerCalendar(context);
        alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        eventTime = new Date();
        bedTime = new Date();
        awakeTime = new Date();
        df = DateFormat.getDateFormat(context);
        tf = DateFormat.getTimeFormat(context);
        settings = new Settings(context);
    }

    private void loadSettings(){
        hoursSleep = settings.loadIntSetting("hours_sleep", settings.default_sleep);
        minutesAwakening = settings.loadIntSetting("minutes_before", settings.default_awakening);
        maxHour = settings.loadIntSetting("max_hour_wakeup", settings.default_maxHour);
        maxMinute = settings.loadIntSetting("max_minute_wakeup", settings.default_maxMinute);
        minHour = settings.loadIntSetting("min_hour_wakeup", settings.default_minHour);
        minMinute = settings.loadIntSetting("min_minute_wakeup", settings.default_minMinute);
        hoursSleep = limitInt(hoursSleep, 16, 0);
        minutesAwakening = limitInt(minutesAwakening, 300, 0);
        maxHour = limitInt(maxHour, 23, 0);
        maxMinute = limitInt(maxMinute, 59, 0);
        minHour = limitInt(minHour, 23, 0);
        minMinute = limitInt(minMinute, 59, 0);
    }

    public int limitInt(int number, int max, int min){
        if(number < min)
            return min;
        if(number > max)
            return max;
        return number;
    }

    public void addAlarm(Date date){
        loadSettings();
        if(date == null) {
            eventTime = calendarHandler.getTomorrowsFirstEvent();

            if(isTesting){
                setNextBedtimeAlarm(System.currentTimeMillis() + 2000, 0, 0);
                //setNextAwakeAlarm(System.currentTimeMillis() + 2000);
                return; }

            int within = withinUsersTimeLimit(eventTime);
            if(within < 0){
                eventTime = updateTimeOfDay(eventTime, minHour, minMinute);
            } else if(within > 0){
                eventTime = updateTimeOfDay(eventTime, maxHour, maxMinute);
            }
            setNextBedtimeAlarm(bedTime(eventTime.getTime()), numberOfNotification, notificationPreTime);
        } else{
            eventTime = date;
        }
        setNextAwakeAlarm(awakeTime(eventTime.getTime()));
    }

    private Date updateTimeOfDay(Date date, int hour, int minute){
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return new Date(calendar.getTimeInMillis());
    }

    private int withinUsersTimeLimit(Date date) {
        return withinTimeLimit(date, maxHour, maxMinute, minHour, minMinute);
    }

    /* Checks if a date is within the time-limit (time of day)
     *  Returns;
     *  -1  if it earlier than the timelimit
     *  0   if it is within the timelimit
     *  1   if it is later than the timelimit.
     *  */
    private int withinTimeLimit(Date date, int maxH, int maxM, int minH, int minM){
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int h = calendar.get(Calendar.HOUR);
        int m = calendar.get(Calendar.MINUTE);
        boolean lessThanMax = h < maxH || h == maxH && m <= maxM;
        boolean moreThanMin = h > minH || h == minH && m >= minM;

        if(!lessThanMax)
            return 1;
        if(!moreThanMin)
            return -1;

        return 0;
    }

    private void setNextBedtimeAlarm(long dateTime, int numberOfNotification, int notificationPreTime) {
        this.bedTime = new Date(dateTime);
        String msg = "";
        for(int i = 0; i <= numberOfNotification; i++){
            msg =  String.valueOf((notificationPreTime * i)/ 60000) + " minutes left";
            setNotification(dateTime - (notificationPreTime * i), alarmId + i, i == 0, msg);
        }
    }

    private void setNextAwakeAlarm(long dateTime) {
        this.awakeTime = new Date(dateTime);
        setNotification(dateTime, alarmId - 1, true, "wakeup");
    }

    private void setNotification(long time, int id, boolean isAlarm, String msg){
        pendingIntent = getPIntent(id, isAlarm ? ReceiverSnooze.class : ReceiverNotification.class, msg);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    public void removeAlarm(){
        for(int i = -1 ;  i <= numberOfNotification; i++){
            alarmManager.cancel(getPIntent(alarmId + i, i <= 0 ? ReceiverSnooze.class : ReceiverNotification.class, ""));
        }
    }

    private PendingIntent getPIntent(int id, Class<?> c, String msg) {
        Intent intent = new Intent(context, c);
        intent.putExtra("message", msg);
        //PendingIntent.FLAG_UPDATE_CURRENT for not getting extras as null in receivers.
        return PendingIntent.getBroadcast(context.getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //Return timeInMillis for bedtime/awaketime
    private long bedTime(long time){ return awakeTime(time) - (hoursSleep * 3600 * 1000);}
    private long awakeTime(long time){ return time - (minutesAwakening * 60 * 1000); }

    //Return a string to present the date.
    public String getBedtimeDateString(){ return "Bedtime at: " + dt2String(bedTime); }
    public String getAwaketimeDateString(){ return "Wakeup at: " + dt2String(awakeTime); }
    public String getEventDateString(){ return calendarHandler.getTitle() + " at " + dt2String(eventTime); }

    //Returns calculated time until the date.
    public String getTimeToBedtime(){ return "Bedtime in " + timeDiff(bedTime.getTime()); }
    public String getTimeToWaketime(){ return "Wakeup in " + timeDiff(awakeTime.getTime());}

    //Returns a date formated to a string.
    private String dt2String(Date dt){return df.format(dt.getTime()) + " at " + tf.format(dt.getTime());}

    //Calculates time until the parameter-time, returns it as a string.
    private String timeDiff(long time) {
        long diff = time - new Date().getTime();
        int totalSeconds = (int) diff / 1000;
        int days = (totalSeconds / 3600) / 24;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        String d = days == 0 ? "" : Integer.toString(days) + " days, ";
        String h = hours == 0 ? "" : Integer.toString(hours) + " h and ";
        String m = Integer.toString(minutes) + " min";
        return  d + h  + m ;
    }
}