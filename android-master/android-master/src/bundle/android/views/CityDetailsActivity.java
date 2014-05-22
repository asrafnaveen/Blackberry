package bundle.android.views;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import bundle.android.PublicStuffApplication;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.model.helper.ApiResponseHelper;
import bundle.android.model.json.JSONParser;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.model.tasks.HttpClientGet;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import bundle.android.views.dialogs.CustomAlertDialog;
import bundle.android.views.dialogs.CustomProgressDialog;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;
import org.json.JSONException;


public class CityDetailsActivity extends PsActivity {
    private PublicStuffApplication app;
    private Button follow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.citydetails);
        app = (PublicStuffApplication)getApplicationContext();
        initElements();
        
        FlurryAgent.logEvent("City Details");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "City Details");
        
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
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
        TextView cityName = (TextView) this.findViewById(R.id.cityName);
        cityName.setText(app.getCityAppName());

        TextView cityTagline = (TextView) this.findViewById(R.id.cityTagline);
        if(!app.getCityTagline().equals("")){
            cityTagline.setText(app.getCityTagline());
        }
        else{
            cityTagline.setVisibility(View.GONE);
        }
        Display display = getWindowManager().getDefaultDisplay();
        ImageView citySkyline = (ImageView) this.findViewById(R.id.citySkyline);
        
        if(!app.getCityBanner().equals("")){
            AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityBanner(), citySkyline);
            imageLoader.execute(new Void[0]);
            citySkyline.getLayoutParams().height = display.getWidth() *9/16;
        }
        /*else{
            citySkyline.setImageResource(R.drawable.ic_dark_publicstuff_icon);
        }*/

        TextView cityAbout = (TextView) this.findViewById(R.id.cityAbout);
        if(!app.getCityAbout().equals("")){
            cityAbout.setText(app.getCityAbout());
        }

        follow = (Button) this.findViewById(R.id.follow);
        if(app.getCityUserFollowing()){
            follow.setText(CityDetailsActivity.this.getString(R.string.unfollow));
            follow.setPressed(true);
        }
        else{
            follow.setText(CityDetailsActivity.this.getString(R.string.follow));
            follow.setPressed(false);
        }

        Button websitePetition = (Button) this.findViewById(R.id.websitePetition);
        if(!app.getCityUrl().equals("")) websitePetition.setText(CityDetailsActivity.this.getString(R.string.visitcityweb));
        else websitePetition.setText(CityDetailsActivity.this.getString(R.string.petitioncity));
    }

    public void back(View v){
       finish();
    }

    public void newRequestActivity(View v){
        Intent intent = new Intent(CityDetailsActivity.this, NewRequestActivity.class);
        startActivityForResult(intent, 0);
        finish();
    }

    public void follow(View v){
        final CustomProgressDialog dialog = new CustomProgressDialog(CityDetailsActivity.this, CityDetailsActivity.this.getString(R.string.loadingDialog));
        dialog.show();
        HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("city_follow"), Utils.isNetworkAvailable(CityDetailsActivity.this)){
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String result)
            {
            	if(dialog != null && dialog.isShowing()){
					dialog.dismiss();
				}
                try {
                    if(result.length()>0){
                        JSONParser parser = new JSONParser(result);
                        if((parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_SUCCESS))){
                            app.setCityUserFollowing(!app.getCityUserFollowing());
                            if(app.getCityUserFollowing()){
                                follow.setText(CityDetailsActivity.this.getString(R.string.unfollow));
                                follow.setPressed(true);
                            }
                            else{
                                follow.setText(CityDetailsActivity.this.getString(R.string.follow));
                                follow.setPressed(false);
                            }
                        }
                        else{
                            if(parser.getStatus().getMessage().equals("Api Key not provided")){
                                final CustomAlertDialog alert = new CustomAlertDialog(CityDetailsActivity.this, CityDetailsActivity.this.getString(R.string.loginNow), CityDetailsActivity.this.getString(R.string.pleaseLogin), CityDetailsActivity.this.getString(R.string.login), CityDetailsActivity.this.getString(R.string.CancelButton));

                                View.OnClickListener listener = new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        int id = v.getId();
                                        switch(id){
                                            case R.id.alertConfirm:
                                                Intent intent = new Intent(CityDetailsActivity.this, CreateAccountActivity.class);
                                                startActivity(intent);
                                                alert.dismiss();
                                                finish();
                                                break;
                                            case R.id.alertCancel:
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
                            else{
                                final CustomAlertDialog alert = new CustomAlertDialog(CityDetailsActivity.this, CityDetailsActivity.this.getString(R.string.ErrorPastTense), parser.getStatus().getMessage(), CityDetailsActivity.this.getString(R.string.OkButton), null);

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
                        }
                    }
                    else Utils.noConnection(CityDetailsActivity.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        clientGet.addParameter("space_id", String.valueOf(app.getCitySpaceId()));
        clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
        
        if(app.getUserApiKey()!=null && !app.getUserApiKey().equals(""))clientGet.addParameter("api_key", app.getUserApiKey());
        clientGet.execute("");
    }

    public void websitePetition(View v){
        if(!app.getCityUrl().equals("")){
            Bundle b = new Bundle();
            b.putString("url", app.getCityUrl());
            b.putString("title", app.getCityAppName() + " Website");
            Intent intent = new Intent(CityDetailsActivity.this,WebViewActivity.class);
            intent.putExtras(b);
            startActivity(intent);
        }
        else{
            final CustomProgressDialog dialog = new CustomProgressDialog(CityDetailsActivity.this, CityDetailsActivity.this.getString(R.string.loadingDialog));
            dialog.show();
            HttpClientGet clientGet = new HttpClientGet(new ApiRequestHelper("city_follow"), Utils.isNetworkAvailable(CityDetailsActivity.this)){
                @Override
                protected void onPreExecute()
                {
                    super.onPreExecute();
                }
                @Override
                protected void onPostExecute(String result)
                {
                	if(dialog != null && dialog.isShowing()){
						dialog.dismiss();
					}
                     if(result.length()>0){
                        try {

                            JSONParser parser = new JSONParser(result);
                                if(parser.getStatus().getMessage().equals("Api Key not provided")){
                                    final CustomAlertDialog alert = new CustomAlertDialog(CityDetailsActivity.this, CityDetailsActivity.this.getString(R.string.loginNow), CityDetailsActivity.this.getString(R.string.petitionCityVerbose), CityDetailsActivity.this.getString(R.string.loginNow), CityDetailsActivity.this.getString(R.string.CancelButton));

                                    View.OnClickListener listener = new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            int id = v.getId();
                                            switch(id){
                                                case R.id.alertConfirm:
                                                    Intent intent = new Intent(CityDetailsActivity.this, CreateAccountActivity.class);
                                                    startActivity(intent);
                                                    alert.dismiss();
                                                    finish();
                                                    break;
                                                case R.id.alertCancel:
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
                                else{
                                    String title = (parser.getStatus().getType().equals(ApiResponseHelper.STATUS_TYPE_ERROR))?"There was an error":"Thank you";
                                    final CustomAlertDialog alert = new CustomAlertDialog(CityDetailsActivity.this, title, parser.getStatus().getMessage(), CityDetailsActivity.this.getString(R.string.OkButton), null);

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

                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else Utils.noConnection(CityDetailsActivity.this);
                }
            };
            clientGet.addParameter("space_id", String.valueOf(app.getCitySpaceId()));
            clientGet.addParameter("locale", getResources().getConfiguration().locale.getLanguage());
            if(app.getUserApiKey()!=null && !app.getUserApiKey().equals(""))clientGet.addParameter("api_key", app.getUserApiKey());
            clientGet.execute("");
        }

    }

    @Override
    protected void onPause() {
       //System.gc();
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
       //System.gc();
        super.onDestroy();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
}