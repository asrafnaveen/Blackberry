package bundle.android.model.tasks;

import android.os.AsyncTask;
import android.util.Log;
import bundle.android.model.helper.ApiRequestHelper;
import bundle.android.utils.PSSSLSocketFactory;
import bundle.android.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.*;
import java.net.MalformedURLException;
import java.security.KeyStore;
import java.util.*;


public class HttpClientPost
extends AsyncTask<Void, Void, String>
{
	private final HashMap<String, String> params;
	private final ApiRequestHelper factory;
	private final boolean connected;

	public HttpClientPost(ApiRequestHelper theFactory, boolean connected)
	{
		params = new HashMap<String, String>();

		factory = theFactory;
		this.connected = connected;
	}


	@Override
	protected String doInBackground(Void... params)
	{
		String response = "";
		if(connected){
			String url = factory.createPostUrl();

			Log.e("API POST", "url = " + url);

			try{

				KeyStore trustStore;
				trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

				trustStore.load(null, null);

				SSLSocketFactory sf = new PSSSLSocketFactory(trustStore);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				HttpParams paramshttp = new BasicHttpParams();
				HttpProtocolParams.setVersion(paramshttp, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(paramshttp, HTTP.UTF_8);

				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				registry.register(new Scheme("https", sf, 443));

				ClientConnectionManager ccm = new ThreadSafeClientConnManager(paramshttp, registry);

				HttpClient httpClient = new DefaultHttpClient(ccm, paramshttp);

				HttpPost httpPost = new HttpPost(url);
				HttpContext localContext = new BasicHttpContext();
				MultipartEntity entity = factory.createPostParams(this.params);
				httpPost.setEntity(entity);
				HttpResponse httpResponse = httpClient.execute(httpPost, localContext);

				response = Utils.convertStreamToString(httpResponse.getEntity().getContent());
			}
			catch (MalformedURLException ex){
				System.out.print("me");
			}
			catch (IOException ioe){
				System.out.print("me");
			}
			catch(Exception ex){
				System.out.print("me");
			}
			Log.d("API Response", response);
		}

		return response;

	}


	public void addParameter(String theKey, String theValue)
	{
		this.params.put(theKey, theValue);
	}
}
