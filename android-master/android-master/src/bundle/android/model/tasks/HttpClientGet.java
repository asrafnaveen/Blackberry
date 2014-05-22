package bundle.android.model.tasks;

import android.os.AsyncTask;
import android.util.Log;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.utils.PSSSLSocketFactory;
import bundle.android.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;


public class HttpClientGet
extends AsyncTask<String, Void, String>
{
	private final HashMap<String, String> params;
	private final ApiRequestHelper factory;
	private final boolean connected;

	public HttpClientGet(ApiRequestHelper theFactory, boolean connected)
	{
		params = new HashMap<String, String>();

		factory = theFactory;
		this.connected = connected;
	}

	@Override
	protected String doInBackground(String... arg0)
	{

		String response = "";
		if(connected){
			String url = "";
			
			if(arg0!=null)
			{
				if(!arg0[0].contentEquals(""))
					url = factory.createGetUrl(params, arg0[0]);
				else
					url = factory.createGetUrl(params, "");
			}
			else
			{
				url = factory.createGetUrl(params, "");
			}
			

			Log.e("API Call", "url = " + url);
			try
			{
				
				
				KeyStore trustStore;
					trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				
		        trustStore.load(null, null);

		        SSLSocketFactory sf = new PSSSLSocketFactory(trustStore);
		        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		        HttpParams params = new BasicHttpParams();
		        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		        SchemeRegistry registry = new SchemeRegistry();
		        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		        registry.register(new Scheme("https", sf, 443));

		        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
		        
		        HttpClient httpClient = new DefaultHttpClient(ccm, params);
				HttpGet httpGet = new HttpGet(url);
				httpGet.setHeader("Cache-Control", "no-cache; no-store");
				HttpResponse httpResponse = httpClient.execute(httpGet);

				response = Utils.convertStreamToString(httpResponse.getEntity().getContent());

			} catch (IOException e){
				Log.e("API Response", response);
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				Log.e("Keystore exceptoin", response);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return response;
	}


	public void addParameter(String theKey, String theValue)
	{
		String encodedValue="";

		if(theValue != null){ // ignores possible null pointer exception
			try {
				encodedValue = URLEncoder.encode(theValue, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			this.params.put(theKey, encodedValue);
		}
	}
}
