package bundle.android.model.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import bundle.android.utils.Utils;


public class AsyncImageLoader extends AsyncTask<Void, Void, Bitmap> {
        private final String url;
        private final ImageView imageView;


        public AsyncImageLoader(String url, ImageView imageView){
             this.url= url;
             this.imageView = imageView;
        }
        protected Bitmap doInBackground(Void... arg0) {
            return Utils.loadImageFromNetwork(url);
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
