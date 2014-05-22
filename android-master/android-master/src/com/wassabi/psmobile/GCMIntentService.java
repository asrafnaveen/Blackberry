package com.wassabi.psmobile;

import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import bundle.android.PublicStuffApplication;
import bundle.android.utils.DataStore;
import bundle.android.views.CityDashboardActivity;
import bundle.android.views.MainActivity;
import bundle.android.views.RequestDetailsActivity;

import com.google.android.gcm.GCMBaseIntentService;
import com.wassabi.psmobile.R;

public class GCMIntentService extends GCMBaseIntentService {

	/*public GCMIntentService(String senderId) {
		super(senderId);
		Log.d("GCMIntentService", senderId);
	}*/

	private PublicStuffApplication app;

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.d("onError", arg1);
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Log.d("onMessage", String.valueOf(arg1));
		handleMessage(arg0, arg1);
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.d("onRegistered", arg1);
		
		//add id to sharedprefs
		app = (PublicStuffApplication)arg0;
		app.setUserGCM(arg1);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.d("onUnregistered", arg1);
		app = (PublicStuffApplication)arg0;
		app.setUserGCM("");
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.d("onRecoverableError", errorId);
		return false;
	}

	private void handleMessage(Context context, Intent intent) {
		// TODO Auto-generated method stub
		/*Utils.notiMsg = intent.getStringExtra("msg");
		Utils.notiTitle = intent.getStringExtra("title");
		Utils.notiType = intent.getStringExtra("type");
		Utils.notiUrl = intent.getStringExtra("url");*/

		app = (PublicStuffApplication)getApplicationContext();
		if(app.getUserApiKey()!=null)
		{
			//string tokenize, parses url and puts each section in an arraylist
			StringTokenizer route = new StringTokenizer(intent.getStringExtra("route_url"), "/");
			int tokens = route.countTokens();
			ArrayList<String> relevantTokens = new ArrayList<String>();
			while(route.hasMoreTokens())
			{
				relevantTokens.add(route.nextToken());
			}
			
			final DataStore appStore = new DataStore(app);
			if(intent.getStringExtra("type").contentEquals("request_comment") && app.getUserApiKey().contentEquals(intent.getStringExtra("api_key")) && (appStore.getFromPrefs(app.getUserApiKey()+"pushComment", true) == true))
			{
				CharSequence tickerText = app.getResources().getString(R.string.app_name); // ticker-text
				CharSequence contentMessage = intent.getStringExtra("title");//intent.getStringExtra("message"); //message

				NotificationManager notificationManager =
						(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
				Intent notificationIntent = new Intent(context, RequestDetailsActivity.class);
				notificationIntent.putExtra("request_id", Integer.valueOf(relevantTokens.get(relevantTokens.size()-1)));
				PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				notification.setContentText(contentMessage)
				.setContentTitle(tickerText)
				.setContentInfo(contentMessage)
				.setContentIntent(pendingIntent)
				.setDefaults(Notification.DEFAULT_LIGHTS)
				.setTicker(tickerText)
				.setSmallIcon(R.drawable.launcher)
				.setAutoCancel(true)
				.setWhen(System.currentTimeMillis());

				notificationManager.notify(1, notification.build());

				//PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				//WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
				//wl.acquire();
			}
			else if(intent.getStringExtra("type").contentEquals("request_status") && app.getUserApiKey().contentEquals(intent.getStringExtra("api_key")) 
					&& !appStore.getFromPrefs(app.getUserApiKey()+"pushPref", "").contentEquals(app.getResources().getString(R.string.typePushMe)))
			{
				CharSequence tickerText = app.getResources().getString(R.string.app_name); // ticker-text
				CharSequence contentMessage = intent.getStringExtra("title");//intent.getStringExtra("message"); //message
				
				NotificationManager notificationManager =
						(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
				Intent notificationIntent = new Intent(context, RequestDetailsActivity.class);
				notificationIntent.putExtra("request_id", Integer.valueOf(relevantTokens.get(relevantTokens.size()-1)));
				PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				notification.setContentText(contentMessage)
				.setContentTitle(tickerText)
				.setContentInfo(contentMessage)
				.setContentIntent(pendingIntent)
				.setDefaults(Notification.DEFAULT_LIGHTS)
				.setTicker(tickerText)
				.setSmallIcon(R.drawable.launcher)
				.setAutoCancel(true)
				.setWhen(System.currentTimeMillis());

				notificationManager.notify(1, notification.build());

//				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//				WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
//				wl.acquire();
			}
			
		}

	}

}
