package se.shapeapp.autoalarm.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by nille on 21/06/15.
 */
public class HandlerCalendar {

    private Cursor cursor;
    private String title = "N/A";
    private String description = "";
    public HandlerCalendar(Context context){
        /* Defines which columns (variables of the event we are interested in retrieving.
         * In this case we retrieve the title, datestart and description
        */
        String[] column = new String[]{
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DESCRIPTION
        };

        /* The filter for the query. The ?s will be replaced with the arguments */
        String filter = CalendarContract.Events.DTSTART + " > " + Long.toString(tomorrowMillis());

        /* Defining the arguments dtstart and the current time in millis
         * These will be as compered in the filter => dtstart > time */
        String event_date = CalendarContract.Events.DTSTART;
        String[] arguments = new String[]{};

        /* Defining our preferred order. Here we want the events
         * ordered by dtstart in ascending order */
        String order = CalendarContract.Events.DTSTART + " ASC";

        /* Adding the query with our preferences to the cursor */
        cursor = context.getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                column,
                filter,
                arguments,
                order
        );
        cursor.moveToFirst();
    }

    public long tomorrowMillis(){
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        return c.getTimeInMillis();
    }

    /**
     * Gets the date where the cursor is pointing at right now. If
     * that event is a Week XX event we recursively ignore it.
     * @return
     */
    public Date getTomorrowsFirstEvent(){
        Long start = 0L;
        try {
            title = cursor.getString(0);
            start = cursor.getLong(1);
        } catch (Exception e) { e.printStackTrace(); }

        Date eventDate = new Date(start);
        Date nowDate = new Date();

        if( title.startsWith("Week")){
            cursor.moveToNext();
            return getTomorrowsFirstEvent();
        }
        return eventDate;
    }
    public String getTitle(){ return title;}
}
