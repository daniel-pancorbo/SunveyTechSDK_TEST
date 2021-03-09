package com.rearcam.receive;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.rearcam.receive.utils.BitmapUtil;

import java.io.File;

public class PreviewPhotoActivity extends Activity {

    private String imgUrl;
    private String from;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_preview_phote);

        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        imgUrl = intent.getStringExtra("imgUrl");
        from = intent.getStringExtra("from");
    }

    private void initView() {
        img = (ImageView) findViewById(R.id.image);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(from)){
                    if(from.equals("MAINACTIVITY")){
                        startActivity(new Intent(PreviewPhotoActivity.this,ShowPicActivity.class));
                        finish();
                    }else{
                        finish();
                    }
                }else{
                    finish();
                }
            }
        });

        if(!TextUtils.isEmpty(imgUrl)){
            File imageFile = new File(imgUrl);
            if(!imageFile.exists()){
                Toast.makeText(this, "no such file！", Toast.LENGTH_SHORT).show();
            }else{
                Bitmap bitmap = BitmapUtil.compressBitmap(imgUrl,0,0);
                img.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(!TextUtils.isEmpty(from)){
            if(from.equals("MAINACTIVITY")){
                startActivity(new Intent(this,ShowPicActivity.class));
                finish();
            }else{
                super.onBackPressed();
            }
        }else{
            super.onBackPressed();
        }
    }
}
