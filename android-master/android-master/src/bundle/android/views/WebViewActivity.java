package bundle.android.views;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.content.pm.*;
import android.content.res.Configuration;
import bundle.android.PublicStuffApplication;
import bundle.android.model.tasks.AsyncImageLoader;
import bundle.android.utils.DataStore;
import bundle.android.utils.Utils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.flurry.android.FlurryAgent;
import com.wassabi.psmobile.R;


public class WebViewActivity extends PsActivity{
	private PublicStuffApplication app;
	private Bundle b;
	private String website;
	private WebView webView;



	private String title;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (PublicStuffApplication)getApplicationContext();
		DataStore appStore = new DataStore(app);
		//get initializing data from newRequestActivity
		b = getIntent().getExtras();
		website = b.getString("url");
		title = b.getString("title");
		
		Map<String, String> widgetParams = new HashMap<String, String>();
		widgetParams.put(app.getCityAppName(), title);
		widgetParams.put("Title", title);

        FlurryAgent.logEvent("Widget", widgetParams);
        appStore.saveToPrefs("currentEvent", "Widget");
        
		//set content view and fonts
		setContentView(R.layout.webview);
		Utils.useLatoInView(this, this.findViewById(android.R.id.content));
		initElements();
	}
	private void initElements(){
		RelativeLayout headerLayoutView = (RelativeLayout) this.findViewById(R.id.headerLayout);
		headerLayoutView.setBackgroundColor(app.getNavColor());

		ImageView cityIcon = (ImageView) this.findViewById(R.id.cityIcon);
		cityIcon = (ImageView)this.findViewById(R.id.cityIcon);
		if(app.getCityIcon()!=null && !app.getCityIcon().equals("")){
			AsyncImageLoader imageLoader = new AsyncImageLoader(app.getCityIcon(), cityIcon);
			imageLoader.execute(new Void[0]);
		}
		else{
			cityIcon.setImageResource(R.drawable.ic_dark_publicstuff_icon);
		}
		TextView titleView = (TextView) this.findViewById(R.id.webViewTitle);
		titleView.setText(title);
		if(website.contains("youtube.com"))
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(website)));
			finish();
		}
		else
		{
			webView = (WebView) this.findViewById(R.id.web);
			webView.getSettings().setJavaScriptEnabled(true);
			if(website.contains("twitter.com"))
				webView.getSettings().setUserAgentString("PUBLICSTUFF MOBILE MEGA BROWSER 2.0 EPIC EDITION");
			//webView.getSettings().setDomStorageEnabled(true);
			webView.getSettings().setUseWideViewPort(true);
			webView.getSettings().setLoadWithOverviewMode(true);
			webView.getSettings().setSaveFormData(true);


			if(website.endsWith(".pdf")) {
				try
				{
					webView.loadUrl("https://docs.google.com/viewer?url=" + website);
				}
				catch (ActivityNotFoundException e)
				{
				}
			}
			else if (website.startsWith("tel:")) {
				PackageManager pm = WebViewActivity.this.getPackageManager();
				if(!pm.hasSystemFeature("android.hardware.telephony"))
				{

				}
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(website));
				startActivity(intent);
			}
			else
			{

				webView.setWebViewClient(new WebViewClient(){
					ProgressDialog progressDialog = new ProgressDialog(WebViewActivity.this);
					public void onPageStarted(WebView view, String url, Bitmap favicon) {
				        // Show your progress dialog here
						progressDialog.setMessage(WebViewActivity.this.getString(R.string.loadingDialog));
						
						if(progressDialog!=null || !progressDialog.isShowing())
						progressDialog.show();
				          

				       super.onPageStarted(view, url, favicon);

				    }
					
					public void onPageFinished(WebView view, String url){
						if(progressDialog!=null || !progressDialog.isShowing())
							progressDialog.dismiss();
					}

					
				});
				
				webView.setWebChromeClient(new WebChromeClient(){
					public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
					    callback.invoke(origin, true, false);
					 }
				});

				webView.loadUrl(website);

			}//end else
		}
	}

	public void back(View v){
		finish();
	}
	
	@Override
    public void onStart(){
        super.onStart();
        
        FlurryAgent.onStartSession(this, "2C3QVVZMX8Q5M6KF3458");
    }
	
	@Override
	protected void onPause() {
		//webView.stopLoading();
		//webView.removeAllViews(); //trying to kill chrome client
		//webView = null;
		
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
      //setContentView(R.layout.webview);
      
    }
}
