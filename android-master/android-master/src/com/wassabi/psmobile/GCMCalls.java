package com.wassabi.psmobile;

import android.content.Context;
import android.util.Log;
import bundle.android.PublicStuffApplication;

import com.google.android.gcm.GCMRegistrar;

public class GCMCalls {

	public static void registrationCall(Context context)
	{
		GCMRegistrar.checkDevice(context);
    	GCMRegistrar.checkManifest(context);
    	String regId = GCMRegistrar.getRegistrationId(context);
    	if (regId.equals("")) {
    	  GCMRegistrar.register(context, "572258611066");
    	} else {
    		PublicStuffApplication app = (PublicStuffApplication)context;
    		app.setUserGCM(regId);
    	  Log.v("Log Report: ", "Already registered");
    	}
		
	}
	
}
