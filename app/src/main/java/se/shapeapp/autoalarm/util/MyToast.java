package se.shapeapp.autoalarm.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by nille on 21/06/15.
 */
public class MyToast {

    private Context context;

    public MyToast(Context context){
        this.context = context;
    }
    public void writeLong(String msg){Toast.makeText(context, msg, Toast.LENGTH_LONG).show();}
    public void writeShort(String msg){Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();}
}
