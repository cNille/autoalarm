package se.shapeapp.autoalarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import se.shapeapp.autoalarm.util.Settings;

/**
 * Created by nille on 22/06/15.
 */
public class ActivityAlarmBarcode extends Activity {

    private TextView myCode;
    private String password;
    private Settings settings;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_layout);
        myCode = (TextView) findViewById(R.id.my_code);

        settings = new Settings(this);
        password = settings.loadStringSetting("password");
        IntentIntegrator.initiateScan(this);
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
                myCode.setText(result.getContents());

                String resultPw = result.getContents();
                if(!password.equals("")) {
                    if(password.equals(resultPw)) {
                        turnOffClick(null);
                    } else {
                        Toast.makeText(this, "Scanned: " + result.getContents() + ", this is incorrect, find: " + password, Toast.LENGTH_LONG).show();
                        setResult(RESULT_CANCELED, getIntent());
                        finish();
                    }
                } else {
                    turnOffClick(null);
                }
            }
        }
    }
}
