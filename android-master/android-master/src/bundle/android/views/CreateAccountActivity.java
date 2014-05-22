package bundle.android.views;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import bundle.android.PublicStuffApplication;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.HttpClientPost;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomAlertDialog;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.wassabi.psmobile.R;
import org.json.JSONException;

public class CreateAccountActivity extends PsActivity {
    private PublicStuffApplication app;
    private EditText username;
    private EditText email;
    private EditText password;
    private TextView defaultCityDescription;

    private  final int LOGIN_ACTIVITY =1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount);
        app = (PublicStuffApplication)getApplicationContext();
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        
        FlurryAgent.logEvent("Create Account");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Create Account");
        
        initElements();
    }
    private void initElements(){
        username = (EditText)this.findViewById(R.id.username);
        email = (EditText)this.findViewById(R.id.email);
        password = (EditText)this.findViewById(R.id.password);
        password.setOnEditorActionListener(new EditTextListener());
        defaultCityDescription = (TextView)this.findViewById(R.id.defaultCityDescription);
        defaultCityDescription.setText(String.format(CreateAccountActivity.this.getString(R.string.citydescription), app.getCityAppName()));
    }
    private class EditTextListener implements EditText.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT || (event !=null &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                createAccount();
                return true;
            }
            return false;
        }
    }
    private void createAccount(){
        String alertTitle = CreateAccountActivity.this.getString(R.string.errorRequest);
        String alertMessage = "";
        String un =  username.getText().toString();
        String em = email.getText().toString();
        String pw = password.getText().toString();
        if (un.equals("") || em.equals("") || pw.equals("")) alertMessage += CreateAccountActivity.this.getString(R.string.accountRequirement);
        if(!pw.equals("") && pw.length()<5) alertMessage += CreateAccountActivity.this.getString(R.string.passwordCharacters);
        if(!alertMessage.equals("")){

            final CustomAlertDialog dialog = new CustomAlertDialog(this, alertTitle, alertMessage, CreateAccountActivity.this.getString(R.string.OkButton), null);

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
            HttpClientPost clientPost = new HttpClientPost(new ApiRequestHelper("user_register"), Utils.isNetworkAvailable(CreateAccountActivity.this)){
                final CustomProgressDialog dialog = new CustomProgressDialog(CreateAccountActivity.this, CreateAccountActivity.this.getString(R.string.creatingAccount));
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
                            if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
                                app.setUserApiKey(parser.getResponse().getString("api_key"));
                                app.setUserName(parser.getResponse().getString("username"));
                                Intent intent = new Intent(CreateAccountActivity.this, CityDashboardActivity.class);
                                startActivityForResult(intent, 0);
                                finish();
                            }
                            else{
                                resultMessage = parser.getStatus().getMessage();

                                final CustomAlertDialog alert = new CustomAlertDialog(CreateAccountActivity.this, resultTitle, resultMessage, CreateAccountActivity.this.getString(R.string.OkButton), null);

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
                            }
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
                       Utils.noConnection(CreateAccountActivity.this);
                   }
                }
            };
            clientPost.addParameter("username", un);
            clientPost.addParameter("email", em);
            clientPost.addParameter("password", pw);
            clientPost.addParameter("space_id", String.valueOf(app.getCitySpaceId()));
            
            
        	if (!app.getUserGCM().contentEquals("")){
        		clientPost.addParameter("api_key", app.getUserApiKey());
     			clientPost.addParameter("push_system", "android");
     			clientPost.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
     			clientPost.addParameter("device_token", app.getUserGCM());
     			Log.e("gcm device token:", app.getUserGCM());
     			clientPost.addParameter("bundle_id", app.getPackageName());
     			Log.e("app package:", app.getPackageName());
     			clientPost.addParameter("device_uid", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        	}
            
            if(Build.PRODUCT != null) clientPost.addParameter("device_model", Build.PRODUCT);
            if(Build.VERSION.RELEASE !=null) clientPost.addParameter("device_os", Build.VERSION.RELEASE);
            clientPost.execute(new Void[0]);
        }
    }

    /**
     * finish creating account
     * @param v
     */
    public void clickSubmit(View v){
       createAccount();
    }

    /**
     * Skip logging in
     * @param v
     */
    public void skip(View v){
        Intent intent = new Intent(CreateAccountActivity.this, CityDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Login in activity
     * @param v
     */
    public void login(View v){
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_ACTIVITY);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            return;
        }

        if (resultCode == RESULT_OK) finish();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
}
