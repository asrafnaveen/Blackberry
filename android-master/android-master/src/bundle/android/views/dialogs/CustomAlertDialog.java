package bundle.android.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;


public class CustomAlertDialog extends Dialog
{
    private Button confirmButton;
    private Button cancelButton;


    public CustomAlertDialog(Context context, String title, String message, String confirm, String cancel)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.customalertdialog);
        Utils.useLatoInView(context, this.findViewById(android.R.id.content));
        initWidgets(title, message, confirm, cancel);
    }


    private void initWidgets(String title, String message, String confirm, String cancel)
    {
        TextView titleView = (TextView) findViewById(R.id.alertTitle);
        TextView messageView = (TextView) findViewById(R.id.alertMessage);
       confirmButton = (Button)findViewById(R.id.alertConfirm);
       cancelButton = (Button)findViewById(R.id.alertCancel);
        View v = findViewById(R.id.viewSeparator);

        titleView.setText(title);

        if(message!=null) messageView.setText(message);
        else messageView.setVisibility(View.GONE);

        confirmButton.setText(confirm);

        if(cancel!=null) {
            cancelButton.setText(cancel);
            setCancelable(false);
        }
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
