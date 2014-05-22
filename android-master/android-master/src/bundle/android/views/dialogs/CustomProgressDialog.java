package bundle.android.views.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;
import bundle.android.utils.Utils;

import com.wassabi.psmobile.R;


public class CustomProgressDialog extends Dialog
{
    private TextView progressTextView;


    public CustomProgressDialog(Context context, String title)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.customprogressdialog);
        Utils.useLatoInView(context, this.findViewById(android.R.id.content));
        initWidgets(title);
    }


    private void initWidgets(String title)
    {
        progressTextView = (TextView)findViewById(R.id.progressDialogTitle);
        progressTextView.setText(title);
    }

}

