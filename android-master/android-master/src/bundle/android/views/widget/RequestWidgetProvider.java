package bundle.android.views.widget;

import bundle.android.views.NewRequestActivity;

import com.wassabi.psmobile.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class RequestWidgetProvider extends AppWidgetProvider {
	public static final String DEBUG_TAG = "RequestWidgetProvider";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		RemoteViews remoteView = new RemoteViews(context.getPackageName(),
				R.layout.requestwidget);
		
		Intent launchAppIntent = new Intent(context, NewRequestActivity.class);
		launchAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(context,
				0, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		remoteView.setOnClickPendingIntent(R.id.full_widget, launchAppPendingIntent);
		
		ComponentName requestWidget = new ComponentName(context, RequestWidgetProvider.class);
		
		appWidgetManager.updateAppWidget(requestWidget, remoteView);
	}
}
