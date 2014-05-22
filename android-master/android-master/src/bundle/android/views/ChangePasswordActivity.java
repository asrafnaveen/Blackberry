package bundle.android.views;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import bundle.android.PublicStuffApplication;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientPost;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomAlertDialog;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;
import org.json.JSONException;


public class ChangePasswordActivity extends PsActivity {
    private PublicStuffApplication app;
    private EditText oldPassword;
    private ImageView oldPasswordIcon;
    private EditText newPassword;
    private ImageView newPasswordIcon;
    private EditText newPasswordConfirmation;
    private ImageView newPasswordConfirmationIcon;
    private TextView passwordDescription;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        app = (PublicStuffApplication)getApplicationContext();
        
        FlurryAgent.logEvent("Change Password View");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Change Password View");
        initElements();
    }
    private void initElements(){
        RelativeLayout headerLayoutView = (RelativeLayout) this.findViewById(R.id.headerLayout);
        headerLayoutView.setBackgroundColor(app.getNavColor());

        ImageView cityIcon = (ImageView) this.findViewById(R.id.cityIcon);
        if(app.getCityIcon()!=null && !app.getCityIcon().equals("")){
            AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityIcon(), cityIcon);
            imageLoader.execute(new Void[0]);
        }
        else{
            cityIcon.setImageResource(R.drawable.ic_dark_publicstuff_icon);
        }
         passwordDescription =  (TextView)  this.findViewById(R.id.passwordDescription);

        oldPassword = (EditText)this.findViewById(R.id.oldPassword);
         oldPasswordIcon = (ImageView)this.findViewById(R.id.oldPasswordIcon);
         oldPasswordIcon.setVisibility(View.INVISIBLE);

        newPasswordIcon = (ImageView)this.findViewById(R.id.newPasswordIcon);
         newPasswordIcon.setVisibility(View.INVISIBLE);
         newPassword = (EditText)this.findViewById(R.id.newPassword);
         newPassword.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
               if(s.length()<6) newPasswordIcon.setImageResource(R.drawable.holo_light1_navigation_cancel);
               else newPasswordIcon.setImageResource(R.drawable.holo_light1_navigation_accept);
               newPasswordIcon.setVisibility(View.VISIBLE);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
         });

        newPasswordConfirmationIcon = (ImageView)this.findViewById(R.id.newPasswordConfirmationIcon);
        newPasswordConfirmationIcon.setVisibility(View.INVISIBLE);
        newPasswordConfirmation = (EditText)this.findViewById(R.id.newPasswordConfirmation);
        newPasswordConfirmation.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(!newPasswordConfirmation.getText().toString().equals(newPassword.getText().toString())) newPasswordConfirmationIcon.setImageResource(R.drawable.holo_light1_navigation_cancel);
                else newPasswordConfirmationIcon.setImageResource(R.drawable.holo_light1_navigation_accept);
                newPasswordConfirmationIcon.setVisibility(View.VISIBLE);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        newPasswordConfirmation.setOnEditorActionListener(new EditTextListener());
    }
    private class EditTextListener implements EditText.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT || (event !=null &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                updatePassword();
                return true;
            }
            return false;
        }
    }

    private void updatePassword(){
        String title = ChangePasswordActivity.this.getString(R.string.ErrorPastTense);
        String message = "";
        if(!newPasswordConfirmation.getText().toString().equals(newPassword.getText().toString())) message += ChangePasswordActivity.this.getString(R.string.passwordMatch);
        if(newPassword.getText().toString().length()<6) message += ((message.length()>0)?"\n":"") + ChangePasswordActivity.this.getString(R.string.passwordLength);
        if(oldPassword.getText().toString().equals("")) message += ((message.length()>0)?"\n":"") + ChangePasswordActivity.this.getString(R.string.passwordSecurity);
        if(!message.equals("")){

            final CustomAlertDialog dialog = new CustomAlertDialog(this, title, message, ChangePasswordActivity.this.getString(R.string.OkButton), null);

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
            final CustomProgressDialog dialog = new CustomProgressDialog(ChangePasswordActivity.this, ChangePasswordActivity.this.getString(R.string.savingSettings));
            dialog.show();
            HttpClientPost clientPost = new HttpClientPost(new ApiRequestHelper("user_password_update"), Utils.isNetworkAvailable(ChangePasswordActivity.this)){
                @Override
                protected void onPreExecute()
                {
                    super.onPreExecute();
                }
                @Override
                protected void onPostExecute(String result)
                {
                    dialog.dismiss();
                    if(result.length()>0){
                        try {
                            JSONParser parser = new JSONParser(result);
                            if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
                                oldPasswordIcon.setImageResource(R.drawable.holo_light1_navigation_accept);
                                oldPasswordIcon.setVisibility(View.VISIBLE);
                                String title = ChangePasswordActivity.this.getString(R.string.thankyou);
                                String message = ChangePasswordActivity.this.getString(R.string.passwordUpdated);
                                final CustomAlertDialog alert = new CustomAlertDialog(ChangePasswordActivity.this,title, message, ChangePasswordActivity.this.getString(R.string.OkButton), null);

                                View.OnClickListener listener = new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        int id = v.getId();
                                        switch(id){
                                            case R.id.alertConfirm:
                                                alert.dismiss();
                                                finish();
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                };
                                alert.setListener(listener);
                                alert.show();
                            }
                            else{
                                passwordDescription.setText(ChangePasswordActivity.this.getString(R.string.oldpasswordbad));
                                oldPasswordIcon.setImageResource(R.drawable.holo_light1_navigation_cancel);
                                oldPasswordIcon.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else Utils.noConnection(ChangePasswordActivity.this);
                }
            };

            if(app.getUserApiKey() != null && !app.getUserApiKey().equals("")) clientPost.addParameter("api_key", app.getUserApiKey());
            clientPost.addParameter("old_password", oldPassword.getText().toString());
            clientPost.addParameter("new_password", newPassword.getText().toString());
            clientPost.addParameter("new_password_confirm", newPasswordConfirmation.getText().toString());
            clientPost.addParameter("locale", this.getResources().getConfiguration().locale.getLanguage());
            clientPost.execute(new Void[0]);
        }
    }
    public void clickSubmit(View v){
        updatePassword();
    }
    public void back(View v){
        finish();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
}
