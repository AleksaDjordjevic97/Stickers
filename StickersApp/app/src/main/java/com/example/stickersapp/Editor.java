package com.example.stickersapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;


public class Editor extends AppCompatActivity
{
    ImageView imgUser;
    ImageButton btnTrash,btnSticker,btnSave,btnPhoto;
    RecyclerView recyclerView;
    ViewGroup viewGroup;
    ImageView selectedSticker;
    TypedArray stickerArray;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_editor);


        btnTrash = findViewById(R.id.btnTrash);
        btnSticker = findViewById(R.id.btnSticker);
        btnSave = findViewById(R.id.btnSave);


        imgUser = findViewById(R.id.imgSeleceted);
        viewGroup = findViewById(R.id.frmImageLayout);

        stickerArray = getResources().obtainTypedArray(R.array.sticker_array);



        Intent getImageIntent = getIntent();
        Uri uri = getImageIntent.getParcelableExtra("SELECTED_IMAGE_URI");
        try
        {
            imgUser.setImageURI(uri);
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
                            PointF DownPT = new PointF();
                            PointF StartPT = new PointF();
                            float olddistance,oldrotation;
                            float height, width;
                            float oldK;
                            float lineA;
                            float m1;

                            @Override
                            public boolean onTouch(View v, MotionEvent event)
                            {
                                if(selectedSticker != null)
                                    selectedSticker.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                                selectedSticker = newSticker;
                                newSticker.setBackgroundResource(R.drawable.sticker_border);

                                switch (event.getAction() & MotionEvent.ACTION_MASK)
                                {

                                    case MotionEvent.ACTION_MOVE :
                                        if(event.getPointerCount() == 1)
                                        {
                                            float x =StartPT.x + event.getX() - DownPT.x;
                                            float y = StartPT.y + event.getY() - DownPT.y;
                                            newSticker.setX((int) (StartPT.x + event.getX() - DownPT.x));
                                            newSticker.setY((int) (StartPT.y + event.getY() - DownPT.y));
                                            Log.i("MOVE","Moved to x="+x+" and y="+y);
                                            Log.i("MOVE","StartPT  x="+StartPT.x+"  y="+StartPT.y);
                                            Log.i("MOVE","DownPT  x="+DownPT.x+"  y="+DownPT.y);
                                        }
                                        else if(event.getPointerCount() == 2)
                                        {
                                            newSticker.setX((int) (StartPT.x + event.getX(0) - DownPT.x));
                                            newSticker.setY((int) (StartPT.y + event.getY(0) - DownPT.y));

                                            final float dX =event.getX(0) - event.getX(1);
                                            final float dY =event.getY(0) - event.getY(1);
                                            float newdistance = (float) Math.sqrt(dX * dX + dY * dY);
                                            float distance = newdistance - olddistance;
                                            float newWidth = ((width+distance > 150) ? (width + distance) : 150 );
                                            float newHeight = ((height+distance > 150) ? (height + distance) : 150 );
                                            FrameLayout.LayoutParams lp= new FrameLayout.LayoutParams((int) (newWidth), (int) (newHeight));
                                            newSticker.setLayoutParams(lp);


                                            //Nacin 1
//                                            Log.i("ROTATION","dX="+dX+" dY="+dY);
//                                            double radians = Math.atan2(dY, dX);
//                                            Log.i("ROTATION","Radians="+radians);
//                                            float newRot = (float) Math.toDegrees(radians);
//                                            Log.i("ROTATION","Old Angle="+oldrotation);
//                                            Log.i("ROTATION","New Angle="+newRot);
//
//                                            float r = newRot - oldrotation;
//                                            Log.i("ROTATION","Rotate to="+r);
//                                            newSticker.setRotation((int) r);
//                                            //oldrotation = newSticker.getRotation();
//                                            Log.i("ROTATION","ROTATION SET");

                                            //Nacin 2
//                                            float dot_product = event.getX(0)*event.getX(1) + event.getY(0)*event.getY(1);
//                                            float cross_product = event.getX(0)*event.getY(1) - event.getY(0)*event.getX(1);
//                                            double angleRad = Math.atan2(Math.abs(cross_product),dot_product);
//                                            float angle = (float) Math.toDegrees(angleRad);
//                                            if(cross_product < 0)
//                                                angle = (float) (360.0-angle);
//                                            newSticker.setRotation(angle);


                                              //Nacin 3
//                                            float m2 = (event.getY(1) - event.getY(0)) / (event.getX(1) - event.getX(0));
//                                            float lineB =(float) (Math.atan(m2) * 180 / Math.PI);
//
//                                            float angle = lineA - lineB;
//                                            Log.i("ROTATION","LineA="+lineA);
//                                            Log.i("ROTATION","LineB="+lineB);
//                                            Log.i("ROTATION","Rotate to="+angle);





//                                            float newK = dY/dX;
//                                            float tgFi = (newK-oldK)/(1+oldK*newK);
//                                            double arctgFi = Math.atan((double)tgFi);
//                                            float newRot = oldrotation + (float)arctgFi;
//                                            newSticker.setRotation(15*newRot);
//                                            oldrotation = newRot;
//                                            oldK = newK;
                                        }
                                        StartPT.set( newSticker.getX(), newSticker.getY() );
                                        Log.i("ROTATION","STARTPT SET");
                                        break;
                                    case MotionEvent.ACTION_DOWN :
                                            DownPT.set(event.getX(), event.getY());
                                            StartPT.set(newSticker.getX(), newSticker.getY());
                                            Log.i("ACTION DOWN","DownPT x="+event.getX()+" y="+event.getY());
                                            Log.i("ACTION DOWN","StartPT x="+newSticker.getX()+" y="+newSticker.getY());
                                        break;

                                    case MotionEvent.ACTION_POINTER_DOWN:
                                        height = newSticker.getHeight();
                                        width = newSticker.getWidth();
                                        final float odX =event.getX(0) - event.getX(1);
                                        final float odY =event.getY(0) - event.getY(1);
                                        olddistance = (float) Math.sqrt(odX * odX + odY * odY);


                                        //double oradians = Math.atan2(odY, odX);
                                        oldrotation = newSticker.getRotation();
                                        Log.i("ACTION POINTER DOWN","odX="+odX+" odY="+odY);
                                        Log.i("ACTION POINTER DOWN","Old Rotation="+oldrotation);

                                        //Nacin 3
//                                        m1 = (event.getY(1) - event.getY(0)) / (event.getX(1) - event.getX(0));
//                                        lineA = (float) (Math.atan(m1) * 180 / Math.PI);




                                        break;

                                    case MotionEvent.ACTION_POINTER_UP :
                                        height = newSticker.getHeight();
                                        width = newSticker.getWidth();
                                        oldrotation = newSticker.getRotation();
                                        Log.i("ACTION POINTER UP","New Old Rotation="+oldrotation);
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

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView img;
        public ViewHolder(View view)
        {
            super(view);

            img = view.findViewById(R.id.img);
        }
    }




}