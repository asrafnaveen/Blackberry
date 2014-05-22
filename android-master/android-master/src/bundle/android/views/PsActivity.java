package bundle.android.views;

import com.flurry.android.FlurryAgent;

import android.app.Activity;

public class PsActivity extends Activity {
	
	@Override
	public void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, "2C3QVVZMX8Q5M6KF3458");
		
	}
	
	@Override
	public void onStop(){
		super.onStop();
		FlurryAgent.endTimedEvent(this.getSharedPreferences("publicStuffPrefs", MODE_PRIVATE).getString("currentEvent", ""));
		FlurryAgent.onEndSession(this);
		
	}

}
