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
import android.widget.EditText;
import android.widget.TextView;
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
import org.json.JSONArray;
import org.json.JSONException;


public class LoginActivity extends PsActivity {
    private PublicStuffApplication app;
    private EditText email;
    private EditText password;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        app = (PublicStuffApplication)getApplicationContext();
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        
        FlurryAgent.logEvent("Login");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "Login");
        
        initElements();
    }
    private void initElements(){
        email = (EditText)this.findViewById(R.id.email);
        password = (EditText)this.findViewById(R.id.password);
        password.setOnEditorActionListener(new EditTextListener());
    }
    private class EditTextListener implements EditText.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT || (event !=null &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                login();
                return true;
            }
            return false;
        }
    }
    private void login(){
        String alertTitle = LoginActivity.this.getString(R.string.errorRequest);
        String alertMessage = "";
        String em = email.getText().toString();
        String pw = password.getText().toString();
        if (em.equals("") || pw.equals("")) alertMessage += LoginActivity.this.getString(R.string.bothFields);
        if(!alertMessage.equals("")){

            final CustomAlertDialog dialog = new CustomAlertDialog(this, alertTitle, alertMessage, LoginActivity.this.getString(R.string.OkButton), null);

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
            HttpClientPost clientPost = new HttpClientPost(new ApiRequestHelper("user_login"), Utils.isNetworkAvailable(LoginActivity.this)){
                final CustomProgressDialog dialog = new CustomProgressDialog(LoginActivity.this, LoginActivity.this.getString(R.string.loggingIn));
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
                            if(parser.getResponse().has("city_follows")){
                                JSONArray cities = parser.getResponse().getJSONArray("city_follows");
                                for(int i=0; i<cities.length(); i++){
                                    int id = cities.getJSONObject(i).getInt("city_follow");
                                    if(id == app.getCitySpaceId()) app.setCityUserFollowing(true);
                                }
                            }
                            Intent intent = new Intent(LoginActivity.this, CityDashboardActivity.class);
                            startActivity(intent);
                            Intent data = new Intent();
                            LoginActivity.this.setResult(RESULT_OK, data);
                            finish();
                        }
                        else{
                            resultMessage = parser.getStatus().getMessage();

                            final CustomAlertDialog alert = new CustomAlertDialog(LoginActivity.this, resultTitle, resultMessage, LoginActivity.this.getString(R.string.OkButton), null);

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
                       Utils.noConnection(LoginActivity.this);
                   }
                }
            };
            clientPost.addParameter("email", em);
            clientPost.addParameter("password", pw);
            
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
            clientPost.addParameter("space_id", String.valueOf(app.getCitySpaceId()));
            clientPost.execute(new Void[0]);
            
        }
    }
    /**
     * finish creating account
     * @param v
     */
    public void clickSubmit(View v){
        login();
    }
    /**
     *
     * @param v
     */
    public void back(View v){
        finish();
    }

    /**
     * click on forgot password button
     * @param v
     */
    public void forgotPassword(View v){
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }

}
