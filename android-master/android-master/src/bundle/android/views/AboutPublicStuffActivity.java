package bundle.android.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import bundle.android.PublicStuffApplication;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;

import com.flurry.android.FlurryAgent;
//import com.google.android.gms.common.GooglePlayServicesUtil;
import com.wassabi.psmobile.R;


public class AboutPublicStuffActivity extends PsActivity {
    private PublicStuffApplication app;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutpublicstuff);
        this.app = (PublicStuffApplication)getApplicationContext();
        Utils.useLatoInView(this, this.findViewById(android.R.id.content));
        
        FlurryAgent.logEvent("About View");
        DataStore appStore = new DataStore(app);
		appStore.saveToPrefs("currentEvent", "About View");
        
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
    }

    public void termsConditions(View v){
        Intent intent = new Intent(AboutPublicStuffActivity.this, TermsActivity.class);
        startActivity(intent);
    }

    public void privacy(View v){
        Intent intent = new Intent(AboutPublicStuffActivity.this, PrivacyActivity.class);
        startActivity(intent);
    }
    
    public void playLicense(View v){
    	//String playLicense = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this);
    	//new AlertDialog.Builder(this).setMessage(playLicense).create().show();
    }
    
    public void emailTech(View v){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"tech@publicstuff.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Question from PublicStuff Android App");
        try {
            startActivity(Intent.createChooser(i, ""));
        } catch (android.content.ActivityNotFoundException ex) {
        }
    }
    public void back(View v){
        finish();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      
      
    }
}
