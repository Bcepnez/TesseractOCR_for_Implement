package com.bcepnez.tesseractocr;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    OcrManager manager = new OcrManager();
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int CROP = 2;
    Intent CamIntent,GalIntent,CropIntent;
    Toolbar toolbar;
    File file;
    Uri uri;
    ImageView imageView;
    Bitmap bitmap;
    final int RequestRuntimePermissionCode = 1;
    public static boolean crop;
    TextView textView,type,name,lastname,passNo,nationality,issueCountry,DOB,EXP,sex,personalInfo;
    CodeMeans codeMeans;
    String temp;
    ImageView imageView1,tester;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imgView);
        tester = (ImageView)findViewById(R.id.test);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Image to Text");
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
        personalInfo = (TextView)findViewById(R.id.personalInfo);
        textView = (TextView)findViewById(R.id.text1);
        imageView1 = (ImageView)findViewById(R.id.logo);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        home();

    }

    private void RequestRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)){
            Toast.makeText(this,"Camera permission allow to use camera",Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},RequestRuntimePermissionCode);
        }
    }

    Bitmap test;
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
                if (bitmap.getHeight()>bitmap.getWidth()){
                    bitmap = rotate(bitmap,90);
                }
                crop = true;
            }
        }
        if (crop){
            String text =tesseractOCR();
            imageView.setVisibility(View.VISIBLE);
            imageView1.setVisibility(View.INVISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap rotate(Bitmap bitmap1,int degree){
//        - => rotate anti-clockwise
//        + => rotate clockwise
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        bitmap1 = Bitmap.createBitmap(bitmap1,0,0,bitmap1.getWidth(),bitmap1.getHeight(),matrix,true);
        return bitmap1;
    }

    private String tesseractOCR(){
        Toast.makeText(this,"OCR In progress!",Toast.LENGTH_SHORT).show();
        bitmap = toGrayscale(bitmap);
        String text = manager.startRecognizer(bitmap);
        imageView.setImageBitmap(bitmap);
        if (text.length() != 0) {
            textView.setText(text);
            splitter(text);
        }
        else {
            textView.setText("No data");
            nodata("null");
        }
        crop = false;
        return text;
    }

    private void nodata(String text){
        type.setText("Passport Type : "+text);
        name.setText("Firstname : "+text);
        lastname.setText("Lastname : "+text);
        passNo.setText("Passport data : "+text);
        nationality.setText("Nationality : "+text);
        issueCountry.setText("Issuing Country  : "+text);
        DOB.setText("Date of Birth : "+text);
        EXP.setText("Expire Date : "+text);
        sex.setText("Sex : "+text);
        personalInfo.setText("Personal Number : " + text);
    }

    private void CropImage() {
        try {
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri,"image/*");
            CropIntent.putExtra("crop","true");

//            crop on landscape mode aspect 7:1
//            CropIntent.putExtra("aspectX",7);
//            CropIntent.putExtra("aspectY",1);

//            crop on portrait mode
            CropIntent.putExtra("aspectX",1);
            CropIntent.putExtra("aspectY",7);
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
        else if (item.getItemId() == R.id.btn_home){
            home();
        }
        return true;
    }

    private void home() {
        imageView1.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.INVISIBLE);
        textView.setText("                            Welcome to OCR Generator!\n                                 Please Input file to OCR");
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
        if (CamIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(CamIntent, CAMERA_REQUEST);
        }
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

    String passportno,nation,dob,Sex,exp,PersonInfo,Name,lastName,issueC;
    private boolean splitter(String data){
        String top,under;
        String[] textFromTop;
        codeMeans=new CodeMeans();
        MakeItNumeric num = new MakeItNumeric();
        MakeItAlpha alp = new MakeItAlpha();
        if (data!=null && data.trim().toUpperCase().startsWith("P")) {
            data = data.trim().toUpperCase();
            data = data.replaceAll("\\s+", "");
            data = data.replaceAll("~", "");
            data = data.replaceAll("\\(", "<");
            data = data.replaceAll("€", "E");
            data = data.replaceAll("£", "E");
            data = data.replaceAll("\\$", "S");
            data = data.replaceAll("[^A-Z0-9<]", "");
            if (data.length() < 88) {
                nodata("no data");
                return false;
            } else {
                top = data.substring(0, 44);
                under = data.substring(44, data.length());
                top = top.replaceAll("<<<..*$", "");
                top = top.replaceAll("<<", "/");
                top = top.replaceFirst("<", "/");
                top = top.replaceAll("<", " ");
//                convert all top part to alpha
                top = alp.convertToAlpha(top);

//                chktxt0.setText("---------------------------------------");

                textFromTop = top.split("/");

                passportno = under.substring(0, 9);
                passportno = passportno.replaceAll("<", "");
                dob = under.substring(13, 19);
                Sex = under.substring(20, 21);
                exp = under.substring(21, 27);
                PersonInfo = under.substring(28, 42);
                PersonInfo = PersonInfo.replaceAll("<", " ");
                if (PersonInfo.compareToIgnoreCase("              ") == 0) {
                    PersonInfo = "-";
                }
                type.setText("Passport Type : " + textFromTop[0]);
                if (textFromTop[1].length() == 1) {
//                nationality = under.substring(10,11);
//                    issueCountry.setText("Issuing Country  : " + codeMeans.decode("D"));
//                    lastname.setText("Lastname : " + textFromTop[2]);
//                    name.setText("Firstname : " + textFromTop[3]);
                    nation = "D";
                    issueC = "D";
                    lastName = textFromTop[2];
                    Name = textFromTop[3];
                    passportno = passportno.replaceAll("O", "0");
                    passportno = passportno.replaceAll("A", "4");
                    passportno = passportno.replaceAll("D", "0");
                    passportno = passportno.replaceAll("I", "1");
                    passportno = passportno.replaceAll("Q", "0");
                    passportno = passportno.replaceAll("S", "5");
                    passportno = passportno.replaceAll("U", "0");
                } else {
                    issueC = textFromTop[1].substring(0, 3);
                    lastName = textFromTop[1].substring(3, textFromTop[1].length());
                    nation = under.substring(10, 13);
                    Name = textFromTop[2];
                }

//            if want to adjust correctness of passport number must re process here
                if (passNoCheckCal(passportno, num.convertToNumeric(under.substring(9, 10)))) {
                    Toast.makeText(this, "Passport Number Correct!", Toast.LENGTH_SHORT).show();
                    passportno = temp;
                } else {
                    passportno = passportno.replaceAll("O","0");
                    if (passNoCheckCal(passportno, num.convertToNumeric(under.substring(9, 10)))) {
                        Toast.makeText(this, "Passport Number Correct!", Toast.LENGTH_SHORT).show();
                        passportno = temp;
                    }
                    else {
                        Toast.makeText(this, "Passport Number Wrong!", Toast.LENGTH_SHORT).show();
                    }
                }
//            ----------------------END CHECK CORRECNESS PART----------------------
                setTextTotextView();
                return true;
            }
        }else{
            Toast.makeText(this, "Passport format not found", Toast.LENGTH_SHORT).show();
            nodata("null");
            return false;
        }
    }
    private void setTextTotextView (){
        TextView chktxt0 = (TextView)findViewById(R.id.checktext0);
        MakeItAlpha alp = new MakeItAlpha();
        MakeItNumeric num = new MakeItNumeric();
        issueCountry.setText("Issuing Country  : " + codeMeans.decode(issueC));
        lastname.setText("Lastname : " + lastName);
        name.setText("Firstname : " + Name);
        passNo.setText("Passport data : " + passportno);
        nationality.setText("Nationality : " + alp.convertToAlpha(nation) + " : " + codeMeans.decode(alp.convertToAlpha(nation)));
        DOB.setText("Date of Birth : " + codeMeans.datecode(num.convertToNumeric(dob)));
        EXP.setText("Expire Date : " + codeMeans.datecode(num.convertToNumeric(exp)));
        sex.setText("Sex : " + codeMeans.sexcode(Sex));
        personalInfo.setText("Personal Number : " + PersonInfo);
        chktxt0.setText("---------------------------------------");
    }

    private boolean passNoCheckCal(String data,String chk){
        MakeItNumeric num = new MakeItNumeric();
        MakeItAlpha alpha = new MakeItAlpha();
        chk =num.convertToNumeric(chk);
        char[] text;
        int val;
        int checkbit = num.toint(chk);
        int sum = 0,factor;
        text = data.toCharArray();
        for (int i = 0 ; i < data.length(); i++) {
            val = num.convertForCalculate(text[i]);
            if ( i%3 == 0 ) factor = 7;
            else if ( i%3 == 1 ) factor = 3;
            else factor = 1;
            sum+=(val*factor);
        }
        temp=data;
        if (sum%10 == checkbit) {
            return true;
        }
        else return false;
    }

    private Bitmap toGrayscale(Bitmap bitmapOriginal){
        int width,height;
        height = bitmapOriginal.getHeight();
        width = bitmapOriginal.getWidth();

        Bitmap newBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas c =new Canvas(newBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixColorFilter);
        c.drawBitmap(bitmapOriginal,0,0,paint);
        return newBitmap;
    }
}
