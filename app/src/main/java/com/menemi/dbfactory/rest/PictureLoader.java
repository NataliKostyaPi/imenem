package com.menemi.dbfactory.rest;

/**
 * Created by Ui-Developer on 28.09.2016.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.menemi.dbfactory.DBHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

public class PictureLoader extends AsyncTask<Void, Void, String> {
    //Listener to be called after all data is loaded
    private DBHandler.ResultListener onBitmapLoadListener = (Object bitmap) -> {

            Log.w("OnBitmapLoadListener", "Custom listener is not defined");
    };

    private static HashMap<String, Bitmap> cashedPictures = new HashMap<>();
    private String dataURL = "";
    private Bitmap downloadedPicture;
    private static Bitmap defaultPicture;

    /**
     * After this method being called, all pictures that cannot be loaded, will get default picture, provided here
     * @param defaultPicture
     */
    public static void setDefaultPicture(Bitmap defaultPicture) {
        PictureLoader.defaultPicture = defaultPicture;
    }

    /**
     * new PictureLoader(personalGift.getAvatarUrl(), (Object bitmap) ->{});
     * handles cashing, and saving bitmaps to db
     * @param dataURL              link to the JSON file.
     * @param onBitmapLoadListener implement this interface to get picture in place
     *                             where you create object of this class
     */


    public PictureLoader(String dataURL, DBHandler.ResultListener onBitmapLoadListener) {
        if(cashedPictures.get(dataURL) != null){
            onBitmapLoadListener.onFinish(cashedPictures.get(dataURL));

        } else {
            Bitmap picureFromDB = DBHandler.getInstance().getBitmapFromDB(dataURL);
            if (picureFromDB != null){
                cashedPictures.put(dataURL,picureFromDB);
                if(dataURL.equals("http://minemi.ironexus.com/system/profile_pictures/pictures/000/000/001/thumb/open-uri20161014-9573-gq2e4l?1476466426")){
                    Log.d("stackTrace", ""+ Arrays.toString(Thread.currentThread().getStackTrace()));
                }
                onBitmapLoadListener.onFinish(picureFromDB);
            } else {
                this.dataURL = dataURL;
                this.onBitmapLoadListener = onBitmapLoadListener;
                execute();
            }

        }
    }

    @Override
    protected String doInBackground(Void... params) {


        try {
            //Recieve data from server
            URL newurl = new URL(dataURL);

            //loading picture
            downloadedPicture = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
            cashedPictures.put(dataURL,downloadedPicture);
            DBHandler.getInstance().saveBitmapToDB(dataURL, downloadedPicture);

        }catch (FileNotFoundException fileNotFoundException){
            fileNotFoundException.printStackTrace();
        downloadedPicture = defaultPicture;
        }
         catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("Loading", "listener called, PictureLoader");
        //calls listener when finished
        onBitmapLoadListener.onFinish(downloadedPicture);

    }


}
