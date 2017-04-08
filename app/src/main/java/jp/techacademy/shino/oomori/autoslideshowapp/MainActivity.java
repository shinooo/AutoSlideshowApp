package jp.techacademy.shino.oomori.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import jp.techacademy.shino.oomori.autoslideshowapp.Consts;

public class MainActivity extends AppCompatActivity {

    ImageView imageView1;
    Button btnBack;
    Button btnStartPause;
    Button btnNext;

    ContentResolver resolver;
    Cursor cursor;

    Timer mTimer;
    double mTimerSec = 0.0;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = (ImageView)findViewById(R.id.imageView1);
        btnBack = (Button) findViewById(R.id.back_button);
        btnStartPause = (Button)findViewById(R.id.start_stop_button);
        btnStartPause.setText(Consts.BTN_STRING_START);
        btnNext = (Button)findViewById(R.id.next_button);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                getContentsInfo();
            }else{
                // no Permission
                // TODO : PERMISSIONS_REQUEST_CODE は、それぞれごとに必要か調べる
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},Consts.PERMISSIONS_REQUEST_CODE);
            }
        }else{
            // Get
            getContentsInfo();
        }

        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getPreviousContents();
            }
        });

        btnStartPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(btnStartPause.getText().equals(Consts.BTN_STRING_START)) {
                    btnStartPause.setText(Consts.BTN_STRING_STOP);
                    btnBack.setEnabled(false);
                    btnNext.setEnabled(false);

                    playbackPicture();
                }else {
                    btnStartPause.setText(Consts.BTN_STRING_START);
                    btnBack.setEnabled(true);
                    btnNext.setEnabled(true);

                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getNextContents();
            }
        });
    }

    @Override
    // in the event of asking user's permission
    public void onRequestPermissionsResult(int intRequestCode,String strPermissions[],int intGrantResult[]){
        switch (intRequestCode){
            case Consts.PERMISSIONS_REQUEST_CODE:
                if(intGrantResult[0] == PackageManager.PERMISSION_GRANTED){
                    // get
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo(){
        resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // Data Type
                null, // Item (null is all Items)
                null, //condition of filter (null is no filter)
                null, // parameter of filter
                null // sort order // TODO : ソートの仕方調べる
        );

        if(cursor.moveToFirst()){
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);

            imageView1.setImageURI(imageUri);
        }
        // TODO : Cursorクローズは不要？？
        // cursor.close();
    }

    private void getNextContents(){
        if(!cursor.moveToNext()) {
            cursor.moveToFirst();
        }
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);

        imageView1.setImageURI(imageUri);
    }

    private void getPreviousContents(){
        if(!cursor.moveToPrevious()){
            cursor.moveToLast();
        }
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);

        imageView1.setImageURI(imageUri);
    }

    /* 再生ボタンをタップすると自動送りが始まり、2秒毎にスライドさせてください
    * 自動送りの間は、進むボタンと戻るボタンはタップ不可にしてください
    * 再生ボタンをタップすると停止ボタンになり */
    private void playbackPicture(){
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getNextContents();
                        }
                    });
                }
            }, 0, 2000);
        }
    }
}
