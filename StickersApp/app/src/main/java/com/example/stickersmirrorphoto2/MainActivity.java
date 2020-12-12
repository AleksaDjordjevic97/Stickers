 package com.example.stickersmirrorphoto2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity
{
    ImageButton btnGallery, btnCamera, btnPrivacyPolicy;
    private static final int SELECT_IMAGE_GALLERY = 1;
    private static final int SELECT_IMAGE_CAMERA = 1235;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);

        btnGallery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openGallery();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                takeCameraPhoto();
            }
        });

        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openPrivacyPolicy();
            }
        });
    }

    private void openGallery()
    {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"),SELECT_IMAGE_GALLERY);

    }


    private File createImageFile() throws IOException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }




    private void takeCameraPhoto()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try
        {
            photoFile = createImageFile();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        if (photoFile != null)
        {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.stickersmirrorphoto1.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, SELECT_IMAGE_CAMERA);
        }

    }


    private void openPrivacyPolicy()
    {
        String url = "https://en.wikipedia.org/wiki/Privacy_policy";
        Intent privacyPolicyIntent = new Intent(Intent.ACTION_VIEW);
        privacyPolicyIntent.setData(Uri.parse(url));
        startActivity(privacyPolicyIntent);
    }

    private void goToEditor(Uri photo)
    {
        Intent editorIntent = new Intent(getApplicationContext(),Editor.class);
        editorIntent.putExtra("SELECTED_IMAGE_URI",photo);
        startActivity(editorIntent);
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            if(requestCode == SELECT_IMAGE_GALLERY && data != null)
            {
                try
                {
                    Uri uri = data.getData();
                    goToEditor(uri);

                }catch (Exception e)
                {
                    e.printStackTrace();
                }


            }
            else if(requestCode == SELECT_IMAGE_CAMERA)
            {
                try
                {
                    File f = new File(currentPhotoPath);
                    Uri contentUri = Uri.fromFile(f);
                    goToEditor(contentUri);


                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        }

    }






}