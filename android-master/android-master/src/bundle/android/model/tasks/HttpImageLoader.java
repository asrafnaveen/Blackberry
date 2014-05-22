package bundle.android.model.tasks;


import android.graphics.drawable.Drawable;

import android.os.Handler;
import android.os.Message;

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;

public class HttpImageLoader{
        private final HashMap<String, SoftReference<Drawable>> imageCache;

       public HttpImageLoader() {
           imageCache = new HashMap<String, SoftReference<Drawable>>();
        }

        public Drawable loadDrawable(final String imageUrl, boolean connected, final ImageCallback imageCallback) {
            if(connected){
                if (imageCache.containsKey(imageUrl)) {
                    SoftReference<Drawable> softReference = imageCache.get(imageUrl);
                    Drawable drawable = softReference.get();
                    if (drawable != null) {
                        return drawable;
                    }
                }
                final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
                    }
                };
                new Thread() {
                    @Override
                    public void run() {
                        Drawable drawable = loadImageFromUrl(imageUrl);
                        imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
                        Message message = handler.obtainMessage(0, drawable);
                        handler.sendMessage(message);
                    }
                }.start();
            }
            return null;
        }

        private static Drawable loadImageFromUrl(String url) {
            InputStream inputStream;
            try {
                inputStream = new URL(url).openStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return Drawable.createFromStream(inputStream, "src");
        }

        public interface ImageCallback {
            public void imageLoaded(Drawable imageDrawable, String imageUrl);
        }

}
