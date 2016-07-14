package se.shapeapp.autoalarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by nille on 22/06/15.
 */
public class ActivitySuper extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the menu items for use in the action bar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                if(!ActivitySuper.this.getLocalClassName().equals("ActivitySettings")) {
                    startActivity(new Intent(ActivitySuper.this, ActivitySettings.class));
                    finish();
                }
                return true;
            case R.id.action_start:
                if(!ActivitySuper.this.getLocalClassName().equals("ActivityMain")) {
                    startActivity(new Intent(ActivitySuper.this, ActivityMain.class));
                    finish();
                }
                return true;
            case R.id.action_test_alarm:
                Intent myIntent = new Intent(this, ActivitySnooze.class);
                myIntent.putExtra("message", "wakeup");
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myIntent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
