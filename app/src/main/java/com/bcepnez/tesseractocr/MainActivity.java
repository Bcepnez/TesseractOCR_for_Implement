package com.bcepnez.tesseractocr;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
    TextView textView,type,name,lastname,passNo,nationality,issueCountry,DOB,EXP,sex;
    CodeMeans codeMeans;


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
        issueCountry = (TextView)findViewById(R.id.country);
        name = (TextView)findViewById(R.id.name);
        lastname = (TextView)findViewById(R.id.lastname);
        passNo = (TextView)findViewById(R.id.PassportNo);
        nationality = (TextView)findViewById(R.id.nationality);
        DOB = (TextView)findViewById(R.id.DOB);
        sex = (TextView)findViewById(R.id.sex);
        EXP = (TextView)findViewById(R.id.EXP);
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
        Toast.makeText(this,"OCR In progress!",Toast.LENGTH_SHORT).show();
        String text = manager.startRecognizer(bitmap);
        imageView.setImageBitmap(bitmap);
        textView = (TextView)findViewById(R.id.text1);
        if (text.length() != 0) {
            textView.setText(text);
            splitter(text);
        }
        else {
            textView.setText("No data");
            nodata();
        }
        crop = false;
    }

    private void nodata(){
        type.setText("Passport Type : null");
        name.setText("Firstname : null");
        lastname.setText("Lastname : null");
        passNo.setText("Passport data : null");
        nationality.setText("Nationality : null");
        issueCountry.setText("Issuing Country  : null");
        DOB.setText("Date of Birth : null");
        EXP.setText("Expire Date : null");
        sex.setText("Sex : null");
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
        String top,under;
        String[] textFromTop;
        TextView chktxt0 = (TextView)findViewById(R.id.checktext0);
        TextView chktxt1 = (TextView)findViewById(R.id.checktext1);
        String passportno,nation,dob,Sex,exp;
        codeMeans=new CodeMeans();
        if (data!=null && data.trim().toUpperCase().startsWith("P")){
            data = data.trim().toUpperCase();
            data = data.replaceAll("\\s+","");
            data = data.replaceAll("~","");
            data = data.replaceAll("\\(","<");
            data = data.replaceAll("€","E");
            data = data.replaceAll("£","E");
            data = data.replaceAll("\\$","S");
//            int ind = data.indexOf("<<<");
            top = data.substring(0,44);
            under = data.substring(44,data.length());
            top = top.replaceAll("<<<..*<$","");
            top = top.replaceAll("<<","/");
            top = top.replaceFirst("<","/");
            top = top.replaceAll("<"," ");

            while (under.startsWith("<") ){
                under = under.replaceFirst("<","");
            }
            chktxt0.setText("Top Data : "+top);
            chktxt1.setText("Under Data : "+under);

            textFromTop = top.split("/");

            passportno = under.substring(0,9);
            passportno = passportno.replaceAll("<","");
            dob = under.substring(13,19);
            Sex = under.substring(20,21);
            exp = under.substring(21,27);
            type.setText("Passport Type : "+textFromTop[0]);
            if (textFromTop[1].length()==1){
//                nationality = under.substring(10,11);
                nation = "D";
                issueCountry.setText("Issuing Country  : "+codeMeans.decode("D"));
                lastname.setText("Lastname : "+textFromTop[2]);
                name.setText("Firstname : "+textFromTop[3]);
            }
            else {
                String issuingcountry,sptname;
                issuingcountry = textFromTop[1].substring(0,3);
                sptname = textFromTop[1].substring(3,textFromTop[1].length());
                nation = under.substring(10,13);
                issueCountry.setText("Issuing Country  : "+codeMeans.decode(issuingcountry));
                lastname.setText("Lastname : "+sptname);
                name.setText("Firstname : "+textFromTop[2]);
            }
            MakeItNumeric num = new MakeItNumeric();

            passNo.setText("Passport data : "+passportno);
            nationality.setText("Nationality : "+nation+" : "+codeMeans.decode(nation));
            DOB.setText("Date of Birth : "+codeMeans.datecode(num.convertToNumeric(dob)));
            EXP.setText("Expire Date : "+codeMeans.datecode(num.convertToNumeric(exp)));
            sex.setText("Sex : "+codeMeans.sexcode(Sex));
            return true;
        }
        else {
            Toast.makeText(this,"No Passport format",Toast.LENGTH_SHORT).show();
            type.setText("Passport Type : null");
            name.setText("Firstname : null");
            lastname.setText("Lastname : null");
            passNo.setText("Passport data : null");
            nationality.setText("Nationality : null");
            issueCountry.setText("Issuing Country  : null");
            DOB.setText("Date of Birth : null");
            EXP.setText("Expire Date : null");
            sex.setText("Sex : null");
            return false;
        }
    }
}
