package bundle.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import bundle.android.PublicStuffApplication;
import bundle.android.model.vo.*;

import java.io.*;
import java.util.ArrayList;


public class DataStore implements LoaderManager.LoaderCallbacks<SharedPreferences>
{
    private static final String PREFS_NAME = "publicStuffPrefs";

    private final Context context;
    private Location currLocation = null;
    private final PublicStuffApplication app;

    public DataStore(Context theContext)
    {
        context = theContext;
        this.app = (PublicStuffApplication)context.getApplicationContext();
    }

    public void saveToPrefs(String key, String value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(key, value);

        SharedPreferencesLoader.persist(editor);
    }

    public void saveToPrefs(String key, int value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(key, value);

        SharedPreferencesLoader.persist(editor);
    }

    public void saveToPrefs(String key, long value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(key, value);

        SharedPreferencesLoader.persist(editor);
    }
    public void saveToPrefs(String key, float value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putFloat(key, value);

        SharedPreferencesLoader.persist(editor);
    }

    public void saveToPrefs(String key, boolean value){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(key, value);

        SharedPreferencesLoader.persist(editor);
    }

    public String getFromPrefs(String key, String d){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getString(key, d);
    }
    public int getFromPrefs(String key, int d){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getInt(key, d);
    }
    public long getFromPrefs(String key, long d){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getLong(key, d);
    }
    public float getFromPrefs(String key, float d){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getFloat(key, d);
    }
    public boolean getFromPrefs(String key, boolean d){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getBoolean(key, d);
    }

    public void saveExternalRequestDraft(final ArrayList<RequestDraftVO> requestDraftVOs){
    	new Thread() {
            public void run() {
            	final File suspend_f=new File(app.getCurrCacheDir(), "user_drafts");

                FileOutputStream   fos  = null;
                ObjectOutputStream oos  = null;
                boolean            keep = true;

                try {
                    fos = new FileOutputStream(suspend_f);
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(requestDraftVOs);
                }
                catch (Exception e) {
                    keep = false;


                }
                finally {
                    try {
                        if (oos != null)   oos.close();
                        if (fos != null)   fos.close();
                        if (!keep) suspend_f.delete();
                    }
                    catch (Exception e) { /* do nothing */ }
                }
            }
          }.run();
    	
    }

    public ArrayList<RequestDraftVO> getExternalRequestDraft(){
        ArrayList<RequestDraftVO> requestDraftVOs = new ArrayList<RequestDraftVO>();
        final File suspend_f=new File(app.getCurrCacheDir(), "user_drafts");

        FileInputStream fis = null;
        ObjectInputStream is = null;
        // boolean            keep = true;

        try {

            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            requestDraftVOs  = (ArrayList<RequestDraftVO>) is.readObject();
        }catch(Exception e)
        {
            String val= e.getMessage();


        }finally {
            try {
                if (fis != null)   fis.close();
                if (is != null)   is.close();

            }
            catch (Exception e) { }
        }
        return requestDraftVOs;
    }

    public void saveExternalDraftCustomFields(final CustomFieldArrayVO customFields, final int id){
    	new Thread() {
    		public void run() {
    			final File suspend_f=new File(app.getCurrCacheDir(), "custom_fields_"+id);

    			FileOutputStream   fos  = null;
    			ObjectOutputStream oos  = null;
    			boolean            keep = true;

    			try {
    				fos = new FileOutputStream(suspend_f);
    				oos = new ObjectOutputStream(fos);
    				oos.writeObject(customFields);
    			}
    			catch (Exception e) {
    				keep = false;


    			}
    			finally {
    				try {
    					if (oos != null)   oos.close();
    					if (fos != null)   fos.close();
    					if (!keep) suspend_f.delete();
    				}
    				catch (Exception e) { /* do nothing */ }
    			}
    		}
    	}.run();

    }
    public CustomFieldArrayVO getExternalDraftCustomFields(int id){
        CustomFieldArrayVO custFields = new CustomFieldArrayVO();
        final File suspend_f=new File(app.getCurrCacheDir(), "custom_fields_"+id);

        FileInputStream fis = null;
        ObjectInputStream is = null;
        // boolean            keep = true;

        try {

            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            custFields  = (CustomFieldArrayVO) is.readObject();
        }catch(Exception e)
        {
            String val= e.getMessage();


        }finally {
            try {
                if (fis != null)   fis.close();
                if (is != null)   is.close();

            }
            catch (Exception e) { }
        }
        return custFields;
    }
    public void saveExternalRequestList(final ArrayList<RequestListVO> requestListVOs, final String filename){
    	new Thread() {
    		public void run() {
    			final File suspend_f=new File(app.getCurrCacheDir(), filename);

    			FileOutputStream   fos  = null;
    			ObjectOutputStream oos  = null;
    			boolean            keep = true;

    			try {
    				fos = new FileOutputStream(suspend_f);
    				oos = new ObjectOutputStream(fos);
    				oos.writeObject(requestListVOs);
    			}
    			catch (Exception e) {
    				keep = false;


    			}
    			finally {
    				try {
    					if (oos != null)   oos.close();
    					if (fos != null)   fos.close();
    					if (!keep) suspend_f.delete();
    				}
    				catch (Exception e) { /* do nothing */ }
    			}
    		}
    	}.run();
    }

    public ArrayList<RequestListVO> getExternalRequestList(String filename){
        ArrayList<RequestListVO> requestListVOs = new ArrayList<RequestListVO>();
        final File suspend_f=new File(app.getCurrCacheDir(), filename);

        FileInputStream fis = null;
        ObjectInputStream is = null;
        // boolean            keep = true;

        try {

            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            requestListVOs  = (ArrayList<RequestListVO>) is.readObject();
        }catch(Exception e)
        {
            String val= e.getMessage();


        }finally {
            try {
                if (fis != null)   fis.close();
                if (is != null)   is.close();

            }
            catch (Exception e) { }
        }
        return requestListVOs;
    }

    public void saveExternalWidgetList(final ArrayList<WidgetVO> widgetVOs){
    	new Thread() {
    		public void run() {
    			final File suspend_f=new File(app.getCurrCacheDir(), "city_widgets");

    			FileOutputStream   fos  = null;
    			ObjectOutputStream oos  = null;
    			boolean            keep = true;

    			try {
    				fos = new FileOutputStream(suspend_f);
    				oos = new ObjectOutputStream(fos);
    				oos.writeObject(widgetVOs);
    			}
    			catch (Exception e) {
    				keep = false;


    			}
    			finally {
    				try {
    					if (oos != null)   oos.close();
    					if (fos != null)   fos.close();
    					if (!keep) suspend_f.delete();
    				}
    				catch (Exception e) { /* do nothing */ }
    			}
    		}
    	}.run();
    	
    	
    }

    public ArrayList<WidgetVO> getExternalWidgetList(){
        ArrayList<WidgetVO> widgetVOs = new ArrayList<WidgetVO>();
        final File suspend_f=new File(app.getCurrCacheDir(), "city_widgets");

        FileInputStream fis = null;
        ObjectInputStream is = null;
        // boolean            keep = true;

        try {

            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            widgetVOs = (ArrayList<WidgetVO>) is.readObject();
        }catch(Exception e)
        {
            String val= e.getMessage();


        }finally {
            try {
                if (fis != null)   fis.close();
                if (is != null)   is.close();

            }
            catch (Exception e) { }
        }
        return widgetVOs;
    }
    public void saveExternalUserData(final UserVO userVO){
    	new Thread() {
    		public void run() {
    			final File suspend_f=new File(app.getCurrCacheDir(), "user_data");

    			FileOutputStream   fos  = null;
    			ObjectOutputStream oos  = null;
    			boolean            keep = true;

    			try {
    				fos = new FileOutputStream(suspend_f);
    				oos = new ObjectOutputStream(fos);
    				oos.writeObject(userVO);
    			}
    			catch (Exception e) {
    				keep = false;


    			}
    			finally {
    				try {
    					if (oos != null)   oos.close();
    					if (fos != null)   fos.close();
    					if (!keep) suspend_f.delete();
    				}
    				catch (Exception e) { /* do nothing */ }
    			}
    		}
    	}.run();

    }

    public UserVO getExternalUserData(){
        UserVO userVO = new UserVO();
        final File suspend_f=new File(app.getCurrCacheDir(), "user_data");

        FileInputStream fis = null;
        ObjectInputStream is = null;
        // boolean            keep = true;

        try {

            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            userVO = (UserVO) is.readObject();
        }catch(Exception e)
        {
            String val= e.getMessage();


        }finally {
            try {
                if (fis != null)   fis.close();
                if (is != null)   is.close();

            }
            catch (Exception e) { }
        }
        return userVO;

    }

    public void saveExternalNotificationData(final ArrayList<NotificationVO> notificationVOs){
    	new Thread() {
    		public void run() {
    			final File suspend_f=new File(app.getCurrCacheDir(), "user_notifications");

    			FileOutputStream   fos  = null;
    			ObjectOutputStream oos  = null;
    			boolean            keep = true;

    			try {
    				fos = new FileOutputStream(suspend_f);
    				oos = new ObjectOutputStream(fos);
    				oos.writeObject(notificationVOs);
    			}
    			catch (Exception e) {
    				keep = false;


    			}
    			finally {
    				try {
    					if (oos != null)   oos.close();
    					if (fos != null)   fos.close();
    					if (!keep) suspend_f.delete();
    				}
    				catch (Exception e) { /* do nothing */ }
    			}
    		}
    	}.run();
    	
    	
    }
    public ArrayList<NotificationVO> getExternalNotificationData(){
        ArrayList<NotificationVO> notificationVOs = new ArrayList<NotificationVO>();
        final File suspend_f=new File(app.getCurrCacheDir(), "user_notifications");

        FileInputStream fis = null;
        ObjectInputStream is = null;
        // boolean            keep = true;

        try {

            fis = new FileInputStream(suspend_f);
            is = new ObjectInputStream(fis);
            notificationVOs = (ArrayList<NotificationVO>) is.readObject();
        }catch(Exception e)
        {
            String val= e.getMessage();


        }finally {
            try {
                if (fis != null)   fis.close();
                if (is != null)   is.close();

            }
            catch (Exception e) { }
        }
        return notificationVOs;

    }

    public void setCurrLocation(Location theLocation){
        this.currLocation = theLocation;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("lat", String.valueOf(theLocation.getLatitude()));
        editor.putString("lon", String.valueOf(theLocation.getLongitude()));
        
        SharedPreferencesLoader.persist(editor);
    }

    public Location getCurrLocation(){
        return this.currLocation;
    }
    
    public LocationVO getCurrLocationVO(){
    	SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    	return new LocationVO(Double.valueOf(prefs.getString("lat", "0")), Double.valueOf(prefs.getString("lon", "0")), context);
    }

	@Override
	public Loader<SharedPreferences> onCreateLoader(int arg0, Bundle arg1) {
		return(new SharedPreferencesLoader(context));
	}

	@Override
	public void onLoadFinished(Loader<SharedPreferences> arg0,
			SharedPreferences arg1) {
		// TODO Auto-generated method stub
		SharedPreferences.Editor editor = arg1.edit();

		SharedPreferencesLoader.persist(editor);

	}

	@Override
	public void onLoaderReset(Loader<SharedPreferences> arg0) {
		//unused
		
	}

}