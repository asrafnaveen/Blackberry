package bundle.android.views;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import bundle.android.PublicStuffApplication;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.HttpClientPost;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomAlertDialog;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;
import org.json.JSONException;


public class ForgotPasswordActivity extends PsActivity {
    private EditText email;
    private PublicStuffApplication app;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword);
        app = (PublicStuffApplication)getApplicationContext();
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        
        FlurryAgent.logEvent("Forgot Password");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Forgot Password");
        
        initElements();
    }
    private void initElements(){
        email = (EditText)this.findViewById(R.id.email);
        email.setOnEditorActionListener(new EditTextListener());
    }
    private class EditTextListener implements EditText.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT || (event !=null &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                forgotPassword();
                return true;
            }
            return false;
        }
    }

    private void forgotPassword(){
        String alertTitle = ForgotPasswordActivity.this.getString(R.string.errorRequest);
        String alertMessage = "";
        String em = email.getText().toString();
        if (em.equals("")) alertMessage += ForgotPasswordActivity.this.getString(R.string.enterEmail);
        if(!alertMessage.equals("")){

            final CustomAlertDialog dialog = new CustomAlertDialog(this, alertTitle, alertMessage, ForgotPasswordActivity.this.getString(R.string.OkButton), null);

            View.OnClickListener listener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int id = v.getId();
                    switch(id){
                        case R.id.alertConfirm:
                            dialog.dismiss();
                            break;
                        default:
                            break;
                    }
                }
            };
            dialog.setListener(listener);
            dialog.show();
        }
        else{
            HttpClientPost clientPost = new HttpClientPost(new ApiRequestHelper("user_password_reset"), Utils.isNetworkAvailable(ForgotPasswordActivity.this)){
                final CustomProgressDialog dialog = new CustomProgressDialog(ForgotPasswordActivity.this, ForgotPasswordActivity.this.getString(R.string.passReset));
                @Override
                protected void onPreExecute()
                {
                    super.onPreExecute();
                    dialog.show();
                    dialog.setCancelable(false);
                }
                @Override
                protected void onPostExecute(String result)
                {
                    if(result.length()>0){
                        try {
                            String resultTitle;
                            String resultMessage;
                            JSONParser parser = new JSONParser(result);
                            resultTitle = parser.getStatus().getType();
                            resultMessage = parser.getStatus().getMessage();

                            final CustomAlertDialog alert = new CustomAlertDialog(ForgotPasswordActivity.this, resultTitle, resultMessage, ForgotPasswordActivity.this.getString(R.string.OkButton), null);

                            View.OnClickListener listener = new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    int id = v.getId();
                                    switch(id){
                                        case R.id.alertConfirm:
                                            alert.dismiss();
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            };
                            alert.setListener(listener);
                            alert.show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finally{
                            if(dialog != null && dialog.isShowing()){
                                dialog.dismiss();
                            }
                        }
                    } else {
                        if(dialog != null && dialog.isShowing()){
                            dialog.dismiss();
                        }
                        Utils.noConnection(ForgotPasswordActivity.this);
                    }
                }
            };
            clientPost.addParameter("email", em);
            clientPost.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
            clientPost.execute(new Void[0]);
        }
    }
    /**
     * finish creating account
     * @param v
     */
    public void clickSubmit(View v){
         forgotPassword();
    }
    /**
     *
     * @param v
     */
    public void back(View v){
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
    
}
