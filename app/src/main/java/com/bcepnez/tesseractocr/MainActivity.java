package com.bcepnez.tesseractocr;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    OcrManager manager = new OcrManager();
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int CROP = 2;
    String TAG = "Main Activity";
    Intent CamIntent,GalIntent,CropIntent;
    Toolbar toolbar;
    File file;
    Uri uri;
    ImageView imageView;
    Bitmap bitmap;
    final int RequestRuntimePermissionCode = 1;
    public static boolean crop;
    TextView textView,type,name,lastname,passNo,chkbit,nation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imgView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Image to Text Converter");
        setSupportActionBar(toolbar);
        int camPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if (camPermission == PackageManager.PERMISSION_DENIED){
            RequestRuntimePermission();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        manager.initAPI();
        //no err, init API ok!
        type = (TextView)findViewById(R.id.type);
        name = (TextView)findViewById(R.id.name);
        lastname = (TextView)findViewById(R.id.lastname);
        passNo = (TextView)findViewById(R.id.PassportNo);
        chkbit = (TextView)findViewById(R.id.checkbit);
        nation = (TextView)findViewById(R.id.national);
    }

    private void RequestRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)){
            Toast.makeText(this,"Camera permission allow to use camera",Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},RequestRuntimePermissionCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK && !crop ){
            if (!crop){ CropImage(); }
        }
        else if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK  && !crop ) {
            if(data!= null && data.getData()!=null){
                uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    if (!crop){ CropImage(); }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (requestCode == CROP && resultCode == RESULT_OK && !crop){
            if (data!=null && data.getData()!=null){
                uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                crop = true;
            }

        }
        if (crop){
            tesseractOCR();
        }
    }

    private void tesseractOCR(){
        ProgressDialog process = new ProgressDialog(this);
        process.setTitle("Please wait a minute");
        process.setMessage("Process Progress");
        process.setCancelable(false);
        process.show();
        String text = manager.startRecognizer(bitmap);
        imageView.setImageBitmap(bitmap);
        textView = (TextView)findViewById(R.id.text1);
        if (text.length() != 0) textView.setText(text);
        else textView.setText("No data");
        process.dismiss();
        crop = false;
        splitter(text);

    }

    private void CropImage() {
        try {
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri,"image/*");
            CropIntent.putExtra("crop","true");
            CropIntent.putExtra("aspectX",7);
            CropIntent.putExtra("aspectY",1);
            CropIntent.putExtra("scaleUpIfNeeded",true);
            CropIntent.putExtra("scaleDownIfNeeded",true);
            CropIntent.putExtra("data",true);
            crop = true;
//            Toast.makeText(this,"****"+crop+"****",Toast.LENGTH_SHORT).show();
            startActivityForResult(CropIntent,CROP);
        }
        catch (ActivityNotFoundException ex){
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        crop = false;
        if (item.getItemId() == R.id.btn_camera) {
            openCamera();
        } else if (item.getItemId() == R.id.btn_gallery) {
            openGallery();
        }
        return true;
    }

    private void openGallery() {
        GalIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(GalIntent,RESULT_LOAD_IMAGE);
    }

    private void openCamera() {
        CamIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(),
                "File"+String.valueOf(System.currentTimeMillis())+".jpg" );
        uri = Uri.fromFile(file);
        CamIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        CamIntent.putExtra("data",true);
        startActivityForResult(CamIntent,CAMERA_REQUEST);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RequestRuntimePermissionCode : {
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,"Permission Canceled",Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private boolean splitter(String data){
        if (data!=null && data.trim().toUpperCase().startsWith("P")){
            data = data.trim().toUpperCase();
            String[] splitdata = new String[30];
            int i=0;
            data = data.trim().toUpperCase();
            data = data.replaceAll("\\s+","");
            data = data.replaceAll("~","");
            data = data.replaceAll("\\(","<");
            data = data.replaceAll("€","E");
            data = data.replaceAll("£","E");
            data = data.replaceAll("\\$","S");
            data = data.replaceAll("<"," ");
            data = data.replaceAll("\\s+"," ");
            splitdata = data.split(" ");
//            Toast.makeText(this,"Data : "+data+"***",Toast.LENGTH_SHORT).show();
//            for (i = 0;i<splitdata.length;i++) {
//                    Toast.makeText(this, "^^^Data " + i + " : " + splitdata[i] + "^^^", Toast.LENGTH_SHORT).show();
//            }
            CodeMeans codeMeans=new CodeMeans();
            type.setText("Passport Type : "+splitdata[0]);
            if (splitdata[1].length()==1){

                nation.setText("Issuing Country  : "+codeMeans.decode(splitdata[1]));
                lastname.setText("Lastname : "+splitdata[2]);
                name.setText("Firstname : "+splitdata[3]);
                passNo.setText("Passport data : "+splitdata[4]);
                chkbit.setText("Checkbit : "+splitdata[5]);
            }
            else {
                String national,sptname;
                national = splitdata[1].substring(0,3);
                sptname = splitdata[1].substring(3,splitdata[1].length());
                nation.setText("Issuing Country  : "+codeMeans.decode(national));
                lastname.setText("Lastname : "+sptname);
                name.setText("Firstname : "+splitdata[2]);
                passNo.setText("Passport data : "+splitdata[3]);
                chkbit.setText("Checkbit : "+splitdata[4]);
            }

//            Toast.makeText(this,"Data : "+data+"***",Toast.LENGTH_SHORT).show();

            return true;
        }
        else {
            Toast.makeText(this,"No Passport format",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
