package bundle.android.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;

import java.util.Calendar;
import java.util.Date;


public class CustomDateDialog extends Dialog
{
    private TextView titleView;
    private DatePicker datePicker;
    private Button confirmButton;
    private Button cancelButton;

    public CustomDateDialog(Context context, String title, Date date, String confirm, String cancel)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.datealertdialog);
        Utils.useLatoInView(context, this.findViewById(android.R.id.content));
        initWidgets(title, date, confirm, cancel);
    }


    private void initWidgets(String title, Date date, String confirm, String cancel)
    {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        final Calendar earliest = Calendar.getInstance();
        earliest.set(2000,0,1, 0, 0, 0);
        titleView =  (TextView)findViewById(R.id.alertTitle);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        confirmButton = (Button)findViewById(R.id.alertConfirm);
        cancelButton = (Button)findViewById(R.id.alertCancel);
        View v = findViewById(R.id.viewSeparator);

        titleView.setText(title);

        datePicker.init(year, month, day, null);
        try{
            datePicker.setMaxDate(new Date().getTime()+100000);
            datePicker.setMinDate(earliest.getTime().getTime());
        }
        catch (Throwable e){

        }


        confirmButton.setText(confirm);

        if(cancel!=null) cancelButton.setText(cancel);
        else {
            cancelButton.setVisibility(View.GONE);
            v.setVisibility(View.GONE);
        }

    }
    public void setListener(View.OnClickListener theListener)
    {
        confirmButton.setOnClickListener(theListener);
        cancelButton.setOnClickListener(theListener);
    }
}
