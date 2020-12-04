package com.example.stickersapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Editor extends AppCompatActivity
{
    private static final int SELECT_IMAGE_GALLERY = 1;
    private static final int SELECT_IMAGE_CAMERA = 1235;

    ImageView imgUser;
    ImageButton btnTrash,btnSticker,btnSave,btnPhoto,btnCamera,btnGallery,btnMirror;
    RecyclerView recyclerView;
    ViewGroup viewGroup;
    ImageView selectedSticker;
    TypedArray stickerArray;
    boolean openPhotoButtons = false;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_editor);


        btnTrash = findViewById(R.id.btnTrash);
        btnSticker = findViewById(R.id.btnSticker);
        btnSave = findViewById(R.id.btnSave);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnCamera = findViewById(R.id.btnCameraE);
        btnGallery = findViewById(R.id.btnGalleryE);
        btnMirror = findViewById(R.id.btnMirror);

        imgUser = findViewById(R.id.imgSeleceted);
        viewGroup = findViewById(R.id.frmImageLayout);

        stickerArray = getResources().obtainTypedArray(R.array.sticker_array);

        Intent getImageIntent = getIntent();
        Uri uri = getImageIntent.getParcelableExtra("SELECTED_IMAGE_URI");

        try
        {
            Glide.with(this).load(uri).into(imgUser);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        btnTrash.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                removeSticker();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    saveImage();
                    Toast.makeText(getApplicationContext(),"Image saved successfully to gallery!",Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        });

        btnSticker.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showStickers();
            }
        });

        btnPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                animatePhotoButtons();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                replaceFromGallery();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                replaceFromCamera();
            }
        });

        btnMirror.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mirrorSticker();
            }
        });




    }


    private void mirrorSticker()
    {

        if(selectedSticker != null)
        {
            BitmapDrawable drawable = (BitmapDrawable) selectedSticker.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0,
                    0, width, height, matrix, false);
            selectedSticker.setImageBitmap(reflectionImage);
        }

    }

    private void replaceFromCamera()
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
                    "com.example.android.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,524288L);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, SELECT_IMAGE_CAMERA);
        }
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


    private void replaceFromGallery()
    {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"),SELECT_IMAGE_GALLERY);
    }

    private void removeSticker()
    {
        viewGroup.removeView(selectedSticker);
    }

    private void saveImage() throws FileNotFoundException
    {
        if(selectedSticker != null)
        {
            selectedSticker.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            selectedSticker = null;
        }
        viewGroup.setDrawingCacheEnabled(true);
        viewGroup.buildDrawingCache();
        Bitmap bm = viewGroup.getDrawingCache();
        MediaStore.Images.Media.insertImage(getContentResolver(), bm, "StickersApp" , "This image was made by StickersApp");

    }

    private void showStickers()
    {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.layout_stickers,null);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        recyclerView = mView.findViewById(R.id.rcvStickerGallery);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter1 = new RecyclerView.Adapter<ViewHolder>()
        {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_layout, parent, false);
                return new ViewHolder(view);
            }


            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, final int position)
            {
                holder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.img.setImageResource(stickerArray.getResourceId(position,0));


                holder.img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        final ImageView newSticker = new ImageView(getApplicationContext());
                        newSticker.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT));
                        Bitmap sticker = BitmapFactory.decodeResource(getResources(),stickerArray.getResourceId(position,0));
                        newSticker.setImageBitmap(sticker);
                        viewGroup.addView(newSticker);

                        if(selectedSticker != null)
                            selectedSticker.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        selectedSticker = newSticker;
                        newSticker.setBackgroundResource(R.drawable.sticker_border);


                        newSticker.setOnTouchListener(new View.OnTouchListener()
                        {

                            float mX, mY;
                            float olddistance;
                            float height, width;
                            static final int INVALID_POINTER_ID = -1;
                            PointF mFPoint = new PointF();
                            PointF mSPoint = new PointF();
                            int mPtrID1, mPtrID2;
                            float mAngle;

                            @Override
                            public boolean onTouch(View v, MotionEvent event)
                            {
                                if(selectedSticker != null)
                                    selectedSticker.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                                selectedSticker = newSticker;
                                newSticker.setBackgroundResource(R.drawable.sticker_border);


                                switch (event.getActionMasked())
                                {

                                    case MotionEvent.ACTION_MOVE :

                                        if (mPtrID1 != INVALID_POINTER_ID && mPtrID2 != INVALID_POINTER_ID)
                                        {
                                            PointF nfPoint = new PointF();
                                            PointF nsPoint = new PointF();

                                            getRawPoint(event, mPtrID1, nsPoint,newSticker);
                                            getRawPoint(event, mPtrID2, nfPoint,newSticker);

                                            mAngle = angleBetweenLines(mFPoint, mSPoint, nfPoint, nsPoint);

                                            newSticker.setRotation(mAngle);
                                        }


                                        if(event.getPointerCount() == 1)
                                        {

                                            newSticker.animate()
                                                    .x(event.getRawX() + mX)
                                                    .y(event.getRawY() + mY)
                                                    .setDuration(0)
                                                    .start();

                                        }
                                        else if(event.getPointerCount() == 2)
                                        {

                                            final float dX =event.getX(0) - event.getX(1);
                                            final float dY =event.getY(0) - event.getY(1);
                                            float newdistance = (float) Math.sqrt(dX * dX + dY * dY);
                                            float distance = newdistance - olddistance;
                                            float newWidth = ((width+distance > 150) ? (width + distance) : 150 );
                                            float newHeight = ((height+distance > 150) ? (height + distance) : 150 );
                                            FrameLayout.LayoutParams lp= new FrameLayout.LayoutParams((int) (newWidth), (int) (newHeight));
                                            newSticker.setLayoutParams(lp);

                                        }

                                        break;

                                    case MotionEvent.ACTION_DOWN :
                                        mX = newSticker.getX() - event.getRawX();
                                        mY = newSticker.getY() - event.getRawY();

                                        mPtrID1 = event.getPointerId(event.getActionIndex());

                                        break;

                                    case MotionEvent.ACTION_POINTER_DOWN:
                                        height = newSticker.getHeight();
                                        width = newSticker.getWidth();
                                        final float odX =event.getX(0) - event.getX(1);
                                        final float odY =event.getY(0) - event.getY(1);
                                        olddistance = (float) Math.sqrt(odX * odX + odY * odY);

                                        mPtrID2 = event.getPointerId(event.getActionIndex());
                                        getRawPoint(event, mPtrID1, mSPoint,newSticker);
                                        getRawPoint(event, mPtrID2, mFPoint,newSticker);

                                        break;

                                    case MotionEvent.ACTION_UP:
                                        mPtrID1 = INVALID_POINTER_ID;
                                        break;

                                    case MotionEvent.ACTION_POINTER_UP :
                                        height = newSticker.getHeight();
                                        width = newSticker.getWidth();

                                        mPtrID2 = INVALID_POINTER_ID;
                                        break;

                                    case MotionEvent.ACTION_CANCEL:
                                        mPtrID1 = INVALID_POINTER_ID;
                                        mPtrID2 = INVALID_POINTER_ID;
                                        break;

                                    default :
                                        break;
                                }
                                return true;
                            }
                        });

                        dialog.cancel();

                    }
                });
            }


            @Override
            public int getItemCount()
            {
                return stickerArray.length();
            }
        };

        recyclerView.setAdapter(adapter1);
        dialog.show();

    }



    void getRawPoint(MotionEvent ev, int index, PointF point,View v) {
        final int[] location = { 0, 0 };
        v.getLocationOnScreen(location);

        float x = ev.getX(index);
        float y = ev.getY(index);

        double angle = Math.toDegrees(Math.atan2(y, x));
        angle += v.getRotation();

        final float length = PointF.length(x, y);

        x = (float) (length * Math.cos(Math.toRadians(angle))) + location[0];
        y = (float) (length * Math.sin(Math.toRadians(angle))) + location[1];

        point.set(x, y);
    }

    private float angleBetweenLines(PointF fPoint, PointF sPoint, PointF nFpoint, PointF nSpoint) {
        float angle1 = (float) Math.atan2((fPoint.y - sPoint.y), (fPoint.x - sPoint.x));
        float angle2 = (float) Math.atan2((nFpoint.y - nSpoint.y), (nFpoint.x - nSpoint.x));

        float angle = ((float) Math.toDegrees(angle1 - angle2)) % 360;
        if (angle < -180.f) angle += 360.0f;
        if (angle > 180.f) angle -= 360.0f;
        return -angle;
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView img;
        public ViewHolder(View view)
        {
            super(view);

            img = view.findViewById(R.id.img);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == SELECT_IMAGE_GALLERY && data != null)
            {
                try
                {
                    Uri uri = data.getData();
                    Glide.with(this).load(uri).into(imgUser);

                } catch (Exception e)
                {
                    e.printStackTrace();
                }


            } else if (requestCode == SELECT_IMAGE_CAMERA)
            {
                try
                {
                    File f = new File(currentPhotoPath);
                    Uri contentUri = Uri.fromFile(f);
                    Glide.with(this).load(contentUri).into(imgUser);

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        }
    }

    private void animatePhotoButtons()
    {
        float cameraTravel;
        float galleryTravel;

        if(!openPhotoButtons)
        {
            cameraTravel = -200f;
            galleryTravel = -330f;
        }
        else
        {
            cameraTravel = 0f;
            galleryTravel = 0f;
        }

        ObjectAnimator btnCameraAnimator = ObjectAnimator.ofFloat(btnCamera,"translationY",cameraTravel);
        btnCameraAnimator.setDuration(500);
        btnCameraAnimator.start();

        ObjectAnimator btnGalleryAnimator = ObjectAnimator.ofFloat(btnGallery,"translationY",galleryTravel);
        btnGalleryAnimator.setDuration(500);
        btnGalleryAnimator.start();

        openPhotoButtons = !openPhotoButtons;
    }



}