package bundle.android.utils;

import android.content.Context;

import java.io.File;


public class FileCache {

    private final File cacheDir;

    /**
     * build file cache for images from server
     * @param context
     */
    public FileCache(Context context){
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"PublicStuff");
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }

    /**
     * Get file from url of local
     * @param url
     * @return
     */
    public File getFile(String url){
        String filename=String.valueOf(url.hashCode());
        return new File(cacheDir, filename);

    }

    /**
     * clear cache file
     */
    public void clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }

}
