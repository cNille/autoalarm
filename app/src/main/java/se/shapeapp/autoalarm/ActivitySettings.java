package se.shapeapp.autoalarm;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import se.shapeapp.autoalarm.util.AlarmType;
import se.shapeapp.autoalarm.util.Settings;

/**
 * Created by nille on 22/06/15.
 */
public class ActivitySettings extends ActivitySuper implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private Settings settings;
    private EditText minutes_before, hours_sleep;
    private CheckBox automatic, specific;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main);
        settings = new Settings(this);

        //Views with settings-input.
        hours_sleep = (EditText)findViewById(R.id.edittext_hours_sleep);
        minutes_before = (EditText)findViewById(R.id.edittext_time_before_meeting);
        automatic = (CheckBox) findViewById(R.id.checkbox_auto);
        specific = (CheckBox) findViewById(R.id.checkbox_specific_code);
        specific.setOnCheckedChangeListener(this);

        spinner = (Spinner) findViewById(R.id.spinner);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.alarm_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void onResume(){
        super.onResume();
        String h = String.valueOf(settings.loadIntSetting("hours_sleep", settings.default_sleep));
        String m = String.valueOf(settings.loadIntSetting("minutes_before", settings.default_awakening));
        hours_sleep.setText(h);
        minutes_before.setText(m);
        automatic.setChecked(settings.loadIntSetting("automatic", 0) == 1);
        specific.setChecked(!settings.loadStringSetting("password").equals(""));

        spinner.setSelection(settings.loadIntSetting("alarm_type", 0));
        updateText();
    }

    public void saveSettings(View v){
        String h = hours_sleep.getText().toString();
        String m = minutes_before.getText().toString();

        int auto = automatic.isChecked() ? 1 : 0;
        settings.saveIntSetting("automatic", auto);

        if(h.length() > 0){
            settings.saveIntSetting("hours_sleep", Integer.parseInt(h));
        }
        if(m.length() > 0){
            settings.saveIntSetting("minutes_before", Integer.parseInt(m));
        }
        updateText();
        Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
    }

    public void openLatestWaketime(View v){
        int maxHour = settings.loadIntSetting("max_hour_wakeup", settings.default_maxHour);
        int maxMinute = settings.loadIntSetting("max_minute_wakeup", settings.default_maxMinute);

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                settings.saveIntSetting("max_hour_wakeup", hourOfDay);
                settings.saveIntSetting("max_minute_wakeup", minute);
                updateText();
            }
        }, maxHour, maxMinute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void openEarliestWaketime(View v){
        int minHour = settings.loadIntSetting("min_hour_wakeup", settings.default_minHour);
        int minMinute = settings.loadIntSetting("min_minute_wakeup", settings.default_minMinute);

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                settings.saveIntSetting("min_hour_wakeup", hourOfDay);
                settings.saveIntSetting("min_minute_wakeup", minute);
                updateText();
            }
        }, minHour, minMinute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    private void updateText(){
        updateLatestText();
        updateEarliestText();
    }
    private void updateLatestText(){
        int maxHour = settings.loadIntSetting("max_hour_wakeup", settings.default_maxHour);
        int maxMinute = settings.loadIntSetting("max_minute_wakeup", settings.default_maxMinute);
        TextView latest = (TextView) findViewById(R.id.latest_time);
        String m = maxMinute < 10 ? "0" + String.valueOf(maxMinute) : String.valueOf(maxMinute);
        String h = maxHour < 10 ? "0" + String.valueOf(maxHour) : String.valueOf(maxHour);
        latest.setText(h + ":" + m);
    }

    private void updateEarliestText(){
        int minHour = settings.loadIntSetting("min_hour_wakeup", settings.default_minHour);
        int minMinute = settings.loadIntSetting("min_minute_wakeup", settings.default_minMinute);
        TextView earliest = (TextView) findViewById(R.id.earliest_time);
        String m = minMinute < 10 ? "0" + String.valueOf(minMinute) : String.valueOf(minMinute);
        String h = minHour < 10 ? "0" + String.valueOf(minHour) : String.valueOf(minHour);
        earliest.setText(h + ":" + m);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        settings.saveIntSetting("alarm_type", 0);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        specific.setSelected(false);
        settings.saveIntSetting("alarm_type", position);
        settings.saveStringSetting("password", "");
    }

    public void turnOffClick(View v) {
        //Turn off alarm.
        setResult(RESULT_OK, getIntent());
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() != null) {
                String res = result.getContents();
                settings.saveStringSetting("password", res);
                specific.setSelected(!res.equals(""));
                Toast.makeText(this, "Password: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked && settings.loadStringSetting("password").equals("")){
            int alarm_type = settings.loadIntSetting("alarm_type", 0);

            if (alarm_type == AlarmType.BARCODE) {
                IntentIntegrator.initiateScan(this);
            } else if (alarm_type == AlarmType.QR) {
                IntentIntegrator.initiateScan(this, IntentIntegrator.QR_CODE_TYPES);
            }
        } else {
            settings.saveStringSetting("password", "");
        }
    }
}
