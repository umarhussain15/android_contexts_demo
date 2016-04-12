package demo.com.contexts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Umar on 19-Mar-16.
 */
public class NumberPickerDialog extends DialogFragment  {
    SharedPreferences sp;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        sp=getActivity().getSharedPreferences(getResources().getString(R.string.shared_pref_key),
                Context.MODE_PRIVATE);
        final android.widget.NumberPicker myNumberPicker =
                new android.widget.NumberPicker(getActivity());
        // Create Number picker and add it to alert dialog view
        // set alert dialog buttons and on Ok button
        // put values in share preferences and also on
        // parent activity
        myNumberPicker.setMaxValue(15);
        myNumberPicker.setMinValue(1);
         int value=sp
                .getInt("interval",5);
//        myNumberPicker.setFormatter();
        myNumberPicker.setValue(value);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(myNumberPicker).setTitle("Choose Interval (minutes)")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int value2=myNumberPicker.getValue();
                        sp.edit().putInt("interval",value2).commit();
                        TextView tv=(TextView)getActivity().findViewById(R.id.tvinterval);
                        tv.setText(value2+"");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
        ;
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
