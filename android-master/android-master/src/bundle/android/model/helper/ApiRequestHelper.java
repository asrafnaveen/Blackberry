package bundle.android.model.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import bundle.android.PublicStuff;
import bundle.android.utils.Utils;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;


public class ApiRequestHelper {
    private static final String KEY_DEVICE = "device";
    private static final String KEY_VERSION = "version";
    private String action;

    void setAction(String act){
        this.action = act;
    }
    String getAction(){
        return this.action;
    }

    public ApiRequestHelper(){

    }
    public ApiRequestHelper(String action){
        this.setAction(action);
    }
    

    public String createGetUrl(HashMap<String, String> theParams, String address)
    {
    	
    	if(!address.contentEquals(""))
    	{
    		StringBuilder result = new StringBuilder("http://maps.googleapis.com/maps/api/geocode/json?");
    		address = address.replace(" ", "+");
            result.append("address="+address);
            result.append("&sensor=true");
           
        	return result.toString();
    	}
    	else
    	{
    		StringBuilder result = new StringBuilder(PublicStuff.BASE_URL);
    		result.append(getAction());
    		result.append("/?");
    		theParams.put(KEY_DEVICE, PublicStuff.DEVICE);
    		theParams.put(KEY_VERSION, PublicStuff.VERSION);
    		fillUrlWithParams(result, theParams);

    		return result.toString();
    	}
    }

    private void fillUrlWithParams(StringBuilder theUrlBuilder, HashMap<String, String> theParams)
    {

        Set<Map.Entry<String, String>> set = theParams.entrySet();

        for (Map.Entry<String, String> me : set) {
            String key = me.getKey();
            String value = me.getValue();
            theUrlBuilder.append(key).append("=").append(value);
            theUrlBuilder.append("&");
        }
    }

    public String createPostUrl()
    {
        StringBuilder result = new StringBuilder(PublicStuff.BASE_URL);
        result.append(getAction());
        return result.toString();
    }

    public MultipartEntity createPostParams(HashMap<String, String> theParams)
        throws Exception
    {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        Set<Map.Entry<String, String>> set = theParams.entrySet();

        for (Map.Entry<String, String> me : set) {
            String key = me.getKey();
            String value = me.getValue();

            BasicNameValuePair bnvp = new BasicNameValuePair(key, value);
            nameValuePairs.add(bnvp);
        }
        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

        for (NameValuePair nameValuePair : nameValuePairs) {
            String keyName = nameValuePair.getName();
            String valueName = nameValuePair.getValue();
            if (keyName.equalsIgnoreCase("image")) {
                // If the key equals to "image", we use FileBody to transfer the data
               //System.gc();
                File imageFile = new File(valueName);
                ExifInterface exif = new ExifInterface(
                        imageFile.getAbsolutePath());
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                Bitmap bm = Utils.decodeFile(imageFile);
                int rotate = 0;
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                }
                if(rotate>0){
                    Matrix matrix = new Matrix();
                    // rotate the Bitmap
                    matrix.postRotate(rotate - 360);

                    // recreate the new Bitmap
                    bm = Bitmap.createBitmap(bm, 0, 0,
                            bm.getWidth(), bm.getHeight(), matrix, true);
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 75, bos);

                byte[] data = bos.toByteArray();
                ByteArrayBody bab = new ByteArrayBody(data, "uploaded_image.jpg");
                multipartEntity.addPart("uploadedfile", bab);
            } else if (keyName.equalsIgnoreCase("video")) {
            	File imageFile = new File(valueName);
            	
            	 FileBody filebodyVideo = new FileBody(new File(valueName));
            	 
            	 
            	 
            	 /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
            	 byte[] data = bos.toByteArray();
            	 ByteArrayBody bab = new ByteArrayBody(data, "uploaded_image.jpg");*/
            	 multipartEntity.addPart("uploadedfile", filebodyVideo);
            	 
            } else if (keyName.equalsIgnoreCase("audio")) {
            	
            } else {
                // Normal string data
                multipartEntity.addPart(keyName, new StringBody(valueName));
            }
        }
        return multipartEntity;
    }
}
