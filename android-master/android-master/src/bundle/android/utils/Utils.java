package bundle.android.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import bundle.android.views.dialogs.CustomAlertDialog;

import com.wassabi.psmobile.R;

import java.io.*;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;


public class Utils {

    private static Typeface FONT_REGULAR;
    private static Typeface FONT_BOLD;
    private static Typeface FONT_ITALIC;

    /**
     * iterate through views and add Lato font
     * @param context
     * @param v
     */
    public static void useLatoInView(final Context context, final View v) {
        initializeFonts(context);
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                       useLatoInView(context, child);
                }
            }
            else if (v instanceof TextView) {
                if(((TextView)v).getTypeface().isBold()){
                    ((TextView)v).setTypeface(FONT_BOLD);
                }
                else if(((TextView)v).getTypeface().isItalic()){
                    ((TextView)v).setTypeface(FONT_ITALIC);
                }
                else 
                	((TextView)v).setTypeface(FONT_REGULAR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }
    }

    public static void addColorFilter(final Drawable d, String color){
        int iColor = Color.parseColor(color);

        int red = (iColor & 0xFF0000) / 0xFFFF;
        int green = (iColor & 0xFF00) / 0xFF;
        int blue = iColor & 0xFF;

        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };

        ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);

        d.setColorFilter(colorFilter);
    }

    /**
     * global font variables
     * @param context
     */
    private static void initializeFonts(final Context context) {
        FONT_REGULAR = Typeface.createFromAsset(context.getAssets(), "lato_regular.ttf");
        FONT_BOLD = Typeface.createFromAsset(context.getAssets(), "lato_bold.ttf");
        FONT_ITALIC = Typeface.createFromAsset(context.getAssets(), "lato_italic.ttf");
    }

    /**
     * For image capture
     * @param cont
     * @return
     */
    public static File getTempFile(Context cont) {
        // it will return /sdcard/image.tmp
        final File path = new File(Environment.getExternalStorageDirectory(), cont.getPackageName());
        if (!path.exists()) {
            path.mkdir();
        }
        return new File(path, "image.tmp");
    }

    /**
     * get File name from url
     * @param theUrl
     * @return
     */
    public static String getFileName(String theUrl)
    {
        int lastIndex = theUrl.lastIndexOf("/");

        return theUrl.substring(lastIndex+1);
    }

    /**
     * get file path of capture image from uri and content resolver
     * @param uri
     * @param resolver
     * @return
     */
    public static String getPath(Uri uri, ContentResolver resolver) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * decode image from file
     * @param f
     * @return
     */
    public static Bitmap decodeFile(File f){
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            int image_size = 400;
            if (o.outHeight > image_size || o.outWidth > image_size) {
                scale = (int)Math.pow(2, (int) Math.round(Math.log(image_size / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
        }
        return b;
    }
Bitmap scaleBitmap(String file, int width, int height){

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

        int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)height);
        int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)width);

        if (heightRatio > 1 || widthRatio > 1)
        {
            if (heightRatio > widthRatio)
            {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
}


    /**
     * Convert data from url into a string
     * @param is
     * @return
     */
    public static String convertStreamToString(InputStream is)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
               //System.gc();
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Copy stream from url by buffer size
     * @param is
     * @param os
     */
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    public static Bitmap loadImageFromNetwork(String u) {
        Bitmap bmp = null;
        try{
            URL url;
            url = new URL(u);
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        }
        catch (IOException e){
            e.printStackTrace();
        }
        return bmp;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static void noConnection(Context context){
        final CustomAlertDialog alert = new CustomAlertDialog(context, context.getString(R.string.ErrorPastTense), context.getString(R.string.ErrorInternet), context.getString(R.string.OkButton), null);

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

    public static String parseDate(long d, Context context){
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(new Date());
        Calendar pastCal = Calendar.getInstance();
        pastCal.setTime(new Date(d*1000));
        Date newDate = new Date();
        Date oldDate = new Date(d*1000);
        long newTime =newDate.getTime();
        long oldTime =oldDate.getTime();
        int daysAgo = (int)((newTime-oldTime)/ (1000 * 60 * 60 *24));
        String timeAgo = "";
        if(daysAgo>0){
            if(daysAgo<=7){
                timeAgo = daysAgo + ((daysAgo>1)?" days": " day")  + " ago";
            }
            else if(daysAgo<=30){
                int weeksAgo = daysAgo/7;
                int weeksRem = daysAgo%7;
                if(weeksRem>5){
                    weeksAgo ++;
                }
                timeAgo = "about " + weeksAgo + ((weeksAgo>1)?" weeks": " week")  + " ago";
            }
            else if(daysAgo<=365){
                int monthsAgo = daysAgo/30;
                int monthsRem = daysAgo%30;
                if(monthsRem>26){
                    monthsAgo ++;
                }
                timeAgo = "about " + monthsAgo + ((monthsAgo>1)?" months": " month")  + " ago";
            }
            else if(daysAgo>365){
                int yearsAgo = daysAgo/365;
                int yearsRem = daysAgo%365;
                if(yearsRem>350){
                    yearsAgo ++;
                }
                timeAgo = "about " + yearsAgo + ((yearsAgo>1)?" years": " year")  + " ago";
            }
        }

        // String timeAgo = DateUtils.getRelativeTimeSpanString(d*1000, System.currentTimeMillis(), DateUtils.YEAR_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        String time = DateUtils.getRelativeDateTimeString(context, d * 1000, DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0).toString();
        if(timeAgo.equals(""))  return      time;
        else return   time + " ("+timeAgo+")";


    }
}
