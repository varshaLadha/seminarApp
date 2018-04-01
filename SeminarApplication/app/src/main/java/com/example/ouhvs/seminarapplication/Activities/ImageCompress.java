package com.example.ouhvs.seminarapplication.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ouhvs.seminarapplication.Activities.BaseClass;
import com.example.ouhvs.seminarapplication.R;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageCompress extends BaseClass {

    Button btGalleryImage,btCameraImage;
    ImageView ivImageContainer;
    private static final int PICK_CAMERA_IMAGE = 2;
    private static final int PICK_GALLERY_IMAGE = 1;
    private File destFile,file1;
    private Uri imageCaptureUri;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_compress);

        initViews();
    }

    public void initViews()
    {
        btCameraImage=(Button)findViewById(R.id.bt_imageSelectCamera);
        btGalleryImage=(Button)findViewById(R.id.bt_imageSelectGallery);
        ivImageContainer=(ImageView)findViewById(R.id.iv_imageContainer);

        file1=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/camera");
        dateFormatter = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.US);
    }

    public void selectImage(View view)
    {
        switch (view.getId())
        {
            case R.id.bt_imageSelectCamera:
                //Toast.makeText(this, "Image select from camera", Toast.LENGTH_SHORT).show();
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

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_GALLERY_IMAGE:
                    Uri uriPhoto = data.getData();
                    ivImageContainer.setImageURI(uriPhoto);
                    destFile = new File(getPathFromGooglePhotosUri(uriPhoto));
                    break;
                case PICK_CAMERA_IMAGE:
                    ivImageContainer.setImageURI(imageCaptureUri);
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
}
