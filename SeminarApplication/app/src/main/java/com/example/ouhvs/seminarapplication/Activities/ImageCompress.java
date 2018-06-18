package com.example.ouhvs.seminarapplication.Activities;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ouhvs.seminarapplication.Activities.BaseClass;
import com.example.ouhvs.seminarapplication.FCM.Constants;
import com.example.ouhvs.seminarapplication.FCM.NotificationUtils;
import com.example.ouhvs.seminarapplication.ModalClass.RecyclerAdapter;
import com.example.ouhvs.seminarapplication.ModalClass.UserData;
import com.example.ouhvs.seminarapplication.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImageCompress extends BaseClass {

    Gson gson;
    UserData userDetail;
    String object;
    Button btGalleryImage,btCameraImage,btUpload;
    ImageView ivImageContainer,ivCompressImageContainer;
    RecyclerView contacts;
    private static final int PICK_CAMERA_IMAGE = 2;
    private static final int PICK_GALLERY_IMAGE = 1;
    public static File destFile,file1,file;
    private Uri imageCaptureUri;
    private SimpleDateFormat dateFormatter;
    public static final String IMAGE_DIRECTORY = "ImageScalling";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    ActionBar ab;
    ProgressBar progressBar;

    int dataSize;

    AmazonS3 s3;
    TransferUtility transferUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_compress);

        initViews();

        credentialsProvider();

        setTransferUtility();

        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageCompress();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals("registrationComplete")) {
                    FirebaseMessaging.getInstance().subscribeToTopic("global");

                } else if (intent.getAction().equals("pushNotification")) {

                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String str = extras.getString("foreground");

                        if (str != null) {
                            Constants.title=intent.getStringExtra("title");
                            Constants.message=intent.getStringExtra("message");
                            Toast.makeText(getApplicationContext(), Constants.title, Toast.LENGTH_LONG).show();
                        }
                    } else {

                        Constants.message = intent.getStringExtra("message");
                        Constants.title = intent.getStringExtra("title");

                        Toast.makeText(getApplicationContext(), Constants.title , Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    public void initViews() {
        gson=new Gson();
        object=GlobalClass.pref.getString("userDetail","");
        userDetail=gson.fromJson(object,UserData.class);
        btCameraImage=(Button)findViewById(R.id.bt_imageSelectCamera);
        btGalleryImage=(Button)findViewById(R.id.bt_imageSelectGallery);
        btUpload=(Button)findViewById(R.id.bt_upload);
        ivImageContainer=(ImageView)findViewById(R.id.iv_imageContainer);
        ivCompressImageContainer=(ImageView)findViewById(R.id.iv_compressImageContainer);
        contacts=(RecyclerView)findViewById(R.id.recyclerView);
        progressBar=(ProgressBar)findViewById(R.id.pbLoader);

        ab=getSupportActionBar();
        ab.setTitle("Share Image");

        file1=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/camera");
        file = new File(Environment.getExternalStorageDirectory()
                + "/" + IMAGE_DIRECTORY);

        if (!file.exists()) {
            file.mkdirs();
        }

        dateFormatter = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.US);
    }

    public void selectImage(View view) {
        switch (view.getId())
        {
            case R.id.bt_imageSelectCamera:
                destFile = new File(file1, "IMG_"
                        + dateFormatter.format(new Date()).toString() + ".png");
                imageCaptureUri = Uri.fromFile(destFile);

                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
                startActivityForResult(intentCamera, PICK_CAMERA_IMAGE);
                break;
            case R.id.bt_imageSelectGallery:
                //Toast.makeText(this, "Image select from Gallery", Toast.LENGTH_SHORT).show();
                Intent intentGalley = new Intent(Intent.ACTION_PICK);
                intentGalley.setType("image/*");
                startActivityForResult(intentGalley, PICK_GALLERY_IMAGE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String scheme;
        dataSize=0;

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_GALLERY_IMAGE:
                    Uri uriPhoto = data.getData();

                    ivImageContainer.setImageURI(uriPhoto);

                    scheme=uriPhoto.getScheme();

                    if(scheme.equals(ContentResolver.SCHEME_CONTENT))
                    {
                        try {
                            InputStream fileInputStream=getApplicationContext().getContentResolver().openInputStream(uriPhoto);
                            dataSize = fileInputStream.available()/1024;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.e("ImageCompress", "onActivityResult: "+dataSize/1024);
                        System.out.println("File size in bytes"+dataSize/1024);

                    }
                    else if(scheme.equals(ContentResolver.SCHEME_FILE))
                    {
                        String path = uriPhoto.getPath();
                        try {
                            File f = new File(path);
                            Log.e( "onActivityResult: ",f.length()/1024+"");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    destFile = new File(getPathFromGooglePhotosUri(uriPhoto));
                    btUpload.setVisibility(View.VISIBLE);
                    break;
                case PICK_CAMERA_IMAGE:
                    ivImageContainer.setImageURI(imageCaptureUri);

                    scheme=imageCaptureUri.getScheme();

                    if(scheme.equals(ContentResolver.SCHEME_CONTENT))
                    {
                        try {
                            InputStream fileInputStream=getApplicationContext().getContentResolver().openInputStream(imageCaptureUri);
                            dataSize = fileInputStream.available()/1024;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.e("ImageCompress", "onActivityResult: "+dataSize/1024);
                        System.out.println("File size in bytes"+dataSize/1024);

                    }
                    else if(scheme.equals(ContentResolver.SCHEME_FILE))
                    {
                        String path = imageCaptureUri.getPath();
                        try {
                            File f = new File(path);
                            //Toast.makeText(this, ""+f.length()/1024, Toast.LENGTH_SHORT).show();
                            dataSize=(int)f.length()/1024;
                            Log.e( "onActivityResult: ","onActivityResult"+f.length()/1024+"");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    btUpload.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    public String getPathFromGooglePhotosUri(Uri uriPhoto){
        if (uriPhoto == null)
            return null;

        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uriPhoto, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);

            String tempFilename = getTempFilename(this);
            output = new FileOutputStream(tempFilename);

            int read;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return tempFilename;
        } catch (IOException ignored) {
            // Nothing we can do
        } finally {
            closeSilently(input);
            closeSilently(output);
        }
        return null;
    }

    private static String getTempFilename(Context context) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("image", "tmp", outputDir);
        return outputFile.getAbsolutePath();
    }

    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            // Do nothing
        }
    }

    public void imageCompress(){
        if(isConnectingToInternet()) {
            Bitmap bmp = decodeFile(destFile);
            ivCompressImageContainer.setImageBitmap(bmp);

            progressBar.setVisibility(View.VISIBLE);
            contacts.setVisibility(View.GONE);
            //userData=null;
            setFileToUpload(ivCompressImageContainer);
        }else {
            Toast.makeText(this, "There's no internet connection. Please turn on internet. ", Toast.LENGTH_SHORT).show();
        }
    }

    public void displayData(){
        final ArrayList<UserData> userData=new ArrayList<UserData>();
        StringRequest sr=new StringRequest(Request.Method.GET, "http://lanetteamvarsha.000webhostapp.com/seminarApi/getData.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject=new JSONObject(response);
                    if(jsonObject.getInt("success")==1)
                    {
                        JSONArray jsonArray=jsonObject.getJSONArray("data");
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject1=jsonArray.getJSONObject(i);
                            if(!userDetail.getMobileno().equals(jsonObject1.getString("mobileNo"))) {
                                userData.add(new UserData(jsonObject1.getString("username"), jsonObject1.getString("mobileNo"), jsonObject1.getString("fcmId")));
                            }
                        }
    
                        RecyclerAdapter adapter=new RecyclerAdapter(ImageCompress.this,userData,userDetail.getName());
                        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
                        contacts.setLayoutManager(layoutManager);
                        contacts.setAdapter(adapter);
                        progressBar.setVisibility(View.GONE);
                        contacts.setVisibility(View.VISIBLE);
                    }else {
                        Toast.makeText(ImageCompress.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                    
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d( "onErrorResponse: ","Error occurred "+error.getMessage());
            }
        });

        RequestQueue rq= Volley.newRequestQueue(this);
        rq.add(sr);
    }

    private Bitmap decodeFile(File f) {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            //Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        if(dataSize>=30 && dataSize<=800){
            scale=4;
        }

        Log.i("Scale value",scale+"");
        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Log.d(TAG, "Width :" + b.getWidth() + " Height :" + b.getHeight());

        destFile = new File(file, "img_"
                + dateFormatter.format(new Date()).toString() + ".png");
        try {
            FileOutputStream out = new FileOutputStream(destFile);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    public void credentialsProvider(){

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:365984f3-f307-40c1-9fb3-02454e799d17", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        setAmazonS3Client(credentialsProvider);
    }

    public void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider){

        // Create an S3 client
        s3 = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
    }

    public void setTransferUtility(){

        transferUtility = new TransferUtility(s3, getApplicationContext());
    }

    public void transferObserverListener(TransferObserver transferObserver){

        transferObserver.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.e("statechange", state+"");
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
                Log.e("percentage",percentage +"");
                if(percentage==100){
                    Toast.makeText(ImageCompress.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                    displayData();
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("error","error "+ex.getMessage());
            }

        });
    }

    public void setFileToUpload(View view){

        TransferObserver transferObserver = transferUtility.upload(
                "varsha123",     /* The bucket to upload to */
                destFile.getName(),    /* The key for the uploaded object */
                destFile       /* The file where the data to upload exists */
        );

        transferObserverListener(transferObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("registrationComplete"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("pushNotification"));

        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private boolean isConnectingToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
}
