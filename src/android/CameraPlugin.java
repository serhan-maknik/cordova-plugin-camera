package cordova.plugin.camera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import java.io.FileOutputStream;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.apache.cordova.BuildHelper;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Build.VERSION.SDK_INT;

/**
 * This class echoes a string called from JavaScript.
 */
public class CameraPlugin extends CordovaPlugin {
    private int qualityValue = 70;
    private int sizeValue = 80;
    Bitmap bitmap;
    public static final int REQUEST_IMAGE = 100;
    public static final int REQUEST_PERMISSION = 200;
    private String imageFilePath = "";
    CallbackContext callbackContext;
    private String applicationId;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.applicationId = (String) BuildHelper.getBuildConfigValue(cordova.getActivity(), "APPLICATION_ID");
        this.applicationId = preferences.getString("applicationId", this.applicationId);
        if (action.equals("takePicture")) {
            String message = args.getString(0);
          //  Log.d("SERSER","MESSAGE: "+message);
            JSONObject jObj = new JSONObject(message);
            JSONObject data = jObj.getJSONObject("data");

            this.qualityValue = data.getInt("quality");
            this.sizeValue = data.getInt("requiredSize");
            dispatchTakePictureIntent();
          // this.coolMethod(message, callbackContext);
           this.callbackContext = callbackContext;

            return true;
        }
        return false;
    }

   

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } catch (JSONException e) {
            e.printStackTrace();
            this.callbackContext.error(e.getMessage());
        }

        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    String currentPhotoPath;
    private File getTempDirectoryPath() {
        File cache = cordova.getActivity().getCacheDir();
        //File cache =new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"serhan");
        // Create the cache directory if it doesn't exist

        cache.mkdirs();
/*
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        timeStamp = timeStamp+".jpg";
        File ss = new File(cache,timeStamp);*/

        return cache;
    }
    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = cordova.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
     //   File storageDir = cordova.getActivity().getCacheDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    int rotate = 0;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
      
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == -1) {
                cordova.camera.plugin.ExifHelper exif = new cordova.camera.plugin.ExifHelper();
                try {
                  
                    exif.createInFile(currentPhotoPath);
                    exif.readExifData();
                    rotate = exif.getOrientation();

                } catch (IOException e) {
                    e.printStackTrace();
                    this.callbackContext.error(e.getMessage());
                }
              //  Uri uri = Uri.fromFile(new File(currentPhotoPath));
                saveBitmapToFile(new File(currentPhotoPath));
                //      Uri uri = Uri.fromFile(saveBitmapToFile(new File(currentPhotoPath)));
                // Uri uri = Uri.parse(currentPhotoPath);
/*
                if (SDK_INT < 29) {
                    Log.d("SERSER","SDK<29");
                    try {

                        bitmap = MediaStore.Images.Media.getBitmap(cordova.getContext().getContentResolver(), uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                        this.callbackContext.error(e.getMessage());
                    }
                } else {
                    Log.d("SERSER","SDK>29");
                    try {

                        saveImageInAndroidApi29AndAbove(currentPhotoPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        this.callbackContext.error(e.getMessage());
                    }
                }
*/

            }
            else if (resultCode ==  0) {
              //  Toast.makeText(this, "You cancelled the operation", Toast.LENGTH_SHORT).show();
                this.callbackContext.error("You cancelled the operation");
            }
        }
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
    @NonNull
    public void saveImageInAndroidApi29AndAbove(String filePath) throws IOException {
      
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_" + System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
        }
        final ContentResolver resolver = cordova.getContext().getContentResolver();
        Uri uri = null;
        try {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, values);
            if (uri == null) {
                //isSuccess = false;
                this.callbackContext.error("Failed to create new MediaStore record.");
                throw new IOException("Failed to create new MediaStore record.");
            }
            try (final OutputStream stream = resolver.openOutputStream(uri)) {
                if (stream == null) {
                    //isSuccess = false;
                    this.callbackContext.error("Failed to open output stream.");
                    throw new IOException("Failed to open output stream.");
                }
             //   saveBitmapToFile(new File(filePath),stream);
            }
            //isSuccess = true;

        } catch (IOException e) {
            if (uri != null) {
                resolver.delete(uri, null, null);
            }
            throw e;
        }
    }


    public void saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            //Burası resmin büyüklüğünü ayarlıyor
            final int REQUIRED_SIZE=sizeValue;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

           
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            Bitmap bmap = rotateImage(selectedBitmap,rotate);
            bmap.compress(Bitmap.CompressFormat.JPEG, qualityValue , outputStream);
            ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();


            try {
                if (bmap.compress(Bitmap.CompressFormat.JPEG, qualityValue, jpeg_data)) {
                    byte[] code = jpeg_data.toByteArray();
                    byte[] output = Base64.encode(code, Base64.NO_WRAP);
                    String js_out = new String(output);

                    this.callbackContext.success(js_out);
                    js_out = null;
                    output = null;
                    code = null;
                }
            } catch (Exception e) {
                Log.e("Error  ",""+e.getLocalizedMessage());
                this.callbackContext.error("Error: "+e.getLocalizedMessage());
            }

        } catch (Exception e) {
            Log.d("MainActivity","Exception: "+e.getMessage());
            this.callbackContext.error("Error: "+e.getLocalizedMessage());

        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(cordova.getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
           
            // Continue only if the File was successfully created
            if (photoFile != null) {
                applicationId = applicationId +".fileprovider";
                Uri photoURI = FileProvider.getUriForFile(cordova.getContext(),
                        applicationId,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                this.cordova.startActivityForResult((CordovaPlugin) this ,takePictureIntent, REQUEST_IMAGE);
            }
        }
    }


}
