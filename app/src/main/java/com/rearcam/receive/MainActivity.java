package com.rearcam.receive;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rearcam.aicontrol.ai.FrameProcessor;
import com.rearcam.receive.constant.SpConstant;
import com.rearcam.receive.utils.BitmapUtil;
import com.rearcam.receive.utils.FileUtil;
import com.rearcam.receive.utils.LoadingView;
import com.rearcam.receive.utils.SpUtil;

import java.io.File;
import java.util.List;

public class MainActivity extends Activity {
    ImageView jpgView,lineView,setView,bkView;
    BitmapFactory.Options options;
    public static final String TAG = "Steven";
    private boolean isToLeft = false;
    private boolean isLand = false;//是否横屏
    public final int MIN_FPS_FOR_DISPLAY        = 1; //Customize Setup (Set the value to 5 for SZ request)
    public final int STATE_APP_RESUME			= 11;
    public final int STATE_APP_PAUSE			= 12;
    public final int STATE_APP_STOP				= 13;
    public final int STATE_APP_FORCE_QUIT		= 14;

    public final int APP_START_NORMAL			= 0;
    public final int APP_BEGIN_SETUP			= 1;
    public final int APP_AFTER_SETUP			= 2;
    public final int APP_STOP_BY_DEVICE		    = 3;
    public final int APP_STOP_BY_MENUAL   		= 4;

    public int       miAPPState= APP_START_NORMAL;
    private RearCamService mService=null;


    public final int MODE_PORTRAIT				= 1;
    public final int MODE_LANDSCAPE				= 0;
    public final int ACTIVITY_TYPE_SETUP        = 1;

    Handler  mMsgHandler = null;
    TextView FPSText;
    TextView ConnectText;
    public static long FPS_MAX_INTERVAL = 1000000000L;
    long start_time,now_time;
    private double nowFPS = 0;
    Intent Serviceintent=null;
    private PowerManager.WakeLock mWakeLock;
    double new_h = 0;
    String StrTargetSSID = "iRearCamAP";
    String StrTargetPWD = "iRearCamAPPWD";
    String StrTargetChannel = "1";
    int view_mode;
    boolean show_click = true;
    LinearLayout buttonLayoutView;
    RelativeLayout titleLayoutView;
    private Button mCircle;
    boolean mbGetFirstImage=false;
    int     miImageCount=0;
    int     miCurrOrientation=-1;//Configuration.ORIENTATION_LANDSCAPE;
    Display display=null;
    private ImageView take_picture;//保存图像
    private boolean isFirstPic = true;
    private byte[] tempImgBusffer = new byte[]{};
    private ImageView openPicList;
    private ImageView showOrHideLine;//隐藏或者展示倒车辅助线
    private String[] permissions = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE
    };
    private boolean isGetPermisson = false;
    private Button mBtnShow;
    private Bitmap bm;
    private boolean isReverse = false;//图片镜像翻转
    //	private Bitmap bmp;
    private SpUtil spUtil;
    private boolean mbIsExit = false;
    private WifiManager mWifiManager;

    //LYNX DEFINES
    FrameProcessor frameProcessor;
    private ImageView frameView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			boolean isAllGranted = checkPermissionAllGranted(permissions);
			if (!isAllGranted){
				ActivityCompat.requestPermissions(this,permissions,101);
			}else{
				toStartAty();
			}
		}else{
			toStartAty();
		}*/
        setContentView(R.layout.activity_video);
        spUtil = new SpUtil(MainActivity.this, SpConstant.SP_FILE_NAME);
        mCircle = (Button) findViewById(R.id.bt_circle);
        mCircle.setOnClickListener(turnOnClickListener);

        jpgView = (ImageView)findViewById(R.id.live_video);
        jpgView.setOnClickListener(SetVisibleClickListener);

        lineView = (ImageView)findViewById(R.id.line);

        bkView=(ImageView)findViewById(R.id.bkView);

        FPSText = (TextView)findViewById(R.id.common_title_FPS_text);
        ConnectText = (TextView)findViewById(R.id.ConnectionState);

        setView = (ImageView)findViewById(R.id.live_text_resolution);
        setView.setOnClickListener(SetSSIDClickListener);

        mBtnShow = (Button) findViewById(R.id.btn_show);
        mBtnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lineView.getVisibility() == View.VISIBLE){
                    lineView.setVisibility(View.GONE);
                    spUtil.putBoolean(SpConstant.SP_SCALE,false);
                }else{
                    lineView.setVisibility(View.VISIBLE);
                    spUtil.putBoolean(SpConstant.SP_SCALE,true);
                }
            }
        });
//		bmp = ((BitmapDrawable)jpgView.getDrawable()).getBitmap();
        //added by yman on 2017/07/20
        take_picture = (ImageView) findViewById(R.id.take_picture);
        take_picture.setOnClickListener(takePictureListener);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //added by yman on 2017/11/09
        showOrHideLine = (ImageView) findViewById(R.id.show_or_hide_pic);
        showOrHideLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lineView.getVisibility() == View.VISIBLE){
                    lineView.setVisibility(View.GONE);
                }else{
                    lineView.setVisibility(View.VISIBLE);
                }
            }
        });
        boolean isShowLine = spUtil.getBoolean(SpConstant.SP_SCALE,true);
        lineView.setVisibility(isShowLine?View.VISIBLE:View.GONE);

        boolean isMirrorNormal = spUtil.getBoolean(SpConstant.SP_MIRROR,true);
        isToLeft = !isMirrorNormal;
        isReverse = !isMirrorNormal;
//		showCircleBackground();


        openPicList = (ImageView) findViewById(R.id.open_pic_list);
        openPicList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ShowPicActivity.class);
                intent.putExtra("from","MAINACTIVITY");
                intent.putExtra("imgUrl","");
                startActivity(intent);
            }
        });

        buttonLayoutView = (LinearLayout)findViewById(R.id.layout_live_func);
        titleLayoutView = (RelativeLayout)findViewById(R.id.live_fake_action_bar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            boolean isAllGranted = checkPermissionAllGranted(permissions);
            if (!isAllGranted){
                ActivityCompat.requestPermissions(this,permissions,101);
            }else{
                toStartAty();
            }
        }else{
            toStartAty();
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        CreateMessageHandler();
        miAPPState= APP_START_NORMAL;
//		if (Build.VERSION.SDK_INT >= 23 && !AppUtil.isGpsOPen(this)) {
//				Settings.Secure.putInt(getContentResolver(),Settings.Secure.LOCATION_MODE, 1);
//		}


        //LYNX SECTION
        //TODO Check Integration
        frameView = findViewById(R.id.cameraFrameView);
        frameProcessor = new FrameProcessor();



    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101){
            isGetPermisson = true;
            boolean isAllGranted = true;
            for (int grant:grantResults){
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (!isAllGranted){
                toStartAty();
            }else{
                Toast.makeText(MainActivity.this, "缺少权限，程序即将关闭，请手动打开权限。", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private void toStartAty() {
//		this.finish();
        boolean isFirst = spUtil.getBoolean(SpConstant.SP_FIRST,true);
        if (isFirst){
            ShowSetting();
        }
		/*if(mService != null)
			mService.setSSID(StrTargetSSID, StrTargetPWD);*/
//		ConnectText.setText("Set Password...");
    }


    @Override
    public synchronized void onResume() {
        Log.i("steven", "APP Life Cycle onResume ...");
        super.onResume();

        mbGetFirstImage=false;
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            // If the screen is now in landscape mode
            // 横屏显示中
            isLand = true;
        }else if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT){
            isLand = false;
        }
        StartService();
        acquireWakeLock();
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        if(dm.widthPixels<dm.heightPixels) {
            miCurrOrientation=Configuration.ORIENTATION_PORTRAIT;
        }
        else{
            miCurrOrientation=Configuration.ORIENTATION_LANDSCAPE;
        }
        SetLayout(miCurrOrientation);
        SharedPreferences settings = getSharedPreferences(RearCamMSG.REARCAM_SETUP_NAME, 0);
        StrTargetSSID= settings.getString(RearCamMSG.REARCAM_SETUP_SSID, "iRearCamAP");
        StrTargetPWD = settings.getString(RearCamMSG.REARCAM_SETUP_PASSWORD, "iRearCamPWD");
		/*StrTargetSSID= settings.getString(RearCamMSG.REARCAM_SETUP_SSID, "SWD_1ABA4A04E112");
		StrTargetPWD = settings.getString(RearCamMSG.REARCAM_SETUP_PASSWORD, "");*/
        StrTargetChannel=settings.getString(RearCamMSG.REARCAM_SETUP_CHANNEL, "1");

        if (isGetPermisson){
            if(StrTargetSSID.equals("iRearCamAP") || StrTargetPWD.equals("iRearCamPWD")) {
                ShowSetting();
            }
        }
        if (mService!=null){
            mService.setSSID(StrTargetSSID,StrTargetPWD);
        }
    }


    @Override
    public synchronized void onPause() {
        Log.i("steven", "APP Life Cycle onPause ...");
        super.onPause();
        if(myConnection != null)
            unbindService(myConnection);
        releaseWakeLock();

    }

    @Override
    protected void onStop() {
        Log.i("steven", "APP Life Cycle onStop ...");

        if(mService!=null && miAPPState==APP_START_NORMAL)
            mService.setAPPState(STATE_APP_STOP);

        super.onStop();
    }


    @Override
    protected void onDestroy() {
        Log.i("steven", "APP Life Cycle onDestroy ...");
        super.onDestroy();
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation!=miCurrOrientation){
            miCurrOrientation=newConfig.orientation;
            SetLayout(miCurrOrientation);
            //Toast.makeText(this, "CurrOrientation="+miCurrOrientation, Toast.LENGTH_SHORT).show();
        }

    }

    public void SetLayout(int iOrientation){
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int vWidth = dm.widthPixels;
        int vHeight = dm.heightPixels;
        int NewHeight=0;
        if(iOrientation==Configuration.ORIENTATION_PORTRAIT){

            NewHeight = (vWidth *480) / 640;
            android.view.ViewGroup.LayoutParams params=null;

            params = bkView.getLayoutParams();
            params.height = (NewHeight+(vHeight-NewHeight)/2);
            bkView.setLayoutParams(params);

            params= jpgView.getLayoutParams();
            params.height =NewHeight;
            jpgView.setLayoutParams(params);

            params = lineView.getLayoutParams();
            params.height =NewHeight;
            lineView.setLayoutParams(params);


        }else
        if(iOrientation==Configuration.ORIENTATION_LANDSCAPE){
            android.view.ViewGroup.LayoutParams params = jpgView.getLayoutParams();
            params.height = (int) vHeight;
            params.width = (int) vWidth;
            jpgView.setLayoutParams(params);

            params = lineView.getLayoutParams();
            params.height = (int) vHeight;
            params.width = (int) vWidth;
            lineView.setLayoutParams(params);

            params = bkView.getLayoutParams();
            params.height = (int) vHeight;
            params.width = (int) vWidth;
            bkView.setLayoutParams(params);

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mbIsExit){
        	        /*miAPPState=APP_STOP_BY_MENUAL;
        	        if(mService!=null)
        	           mService.setAPPState(STATE_APP_FORCE_QUIT);*/
                finish();
            }
            else {
                mbIsExit = true;
                Toast.makeText(this, "Try again to quit iRearCam",Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private View.OnClickListener turnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isToLeft = !isToLeft;
            isReverse = !isReverse;
//			showCircleBackground();
            spUtil.putBoolean(SpConstant.SP_MIRROR,!isReverse);
        }
    };
    private void showCircleBackground(){
        if (isToLeft){
            mCircle.setBackgroundResource(R.drawable.mirror1);
        }else{
            mCircle.setBackgroundResource(R.drawable.mirror2);
        }
    }

    private ImageView.OnClickListener SetVisibleClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            if (view_mode == MODE_LANDSCAPE) {
                show_click = !show_click;
                if (show_click) {
                    buttonLayoutView.setVisibility(View.VISIBLE);
                    titleLayoutView.setVisibility(View.VISIBLE);
                    mCircle.setVisibility(View.VISIBLE);
                    mBtnShow.setVisibility(View.VISIBLE);
                    AlphaAnimation am = new AlphaAnimation((float) 0.0, (float) 1.0);
                    am.setDuration(700);
                    buttonLayoutView.startAnimation(am);
                    titleLayoutView.startAnimation(am);
                    mCircle.startAnimation(am);
                    mBtnShow.startAnimation(am);
                } else {
                    AlphaAnimation am = new AlphaAnimation((float) 1.0, (float) 0.0);
                    am.setDuration(700);
                    buttonLayoutView.startAnimation(am);
                    titleLayoutView.startAnimation(am);
                    mCircle.startAnimation(am);
                    mBtnShow.startAnimation(am);
                    buttonLayoutView.setVisibility(View.INVISIBLE);
                    titleLayoutView.setVisibility(View.INVISIBLE);
                    mCircle.setVisibility(View.INVISIBLE);
                    mBtnShow.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    private ImageView.OnClickListener SetSSIDClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            ShowSetting();
        }
    };

    //// TODO: 2017/7/20 增加拍照功能
    private View.OnClickListener takePictureListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            LoadingView.self().show(MainActivity.this,"saving...");
            isFirstPic = false;
            tempImgBusffer = null;
            //test
			/*Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] datas = baos.toByteArray();
			if(!isFirstPic && tempImgBusffer == null){
				tempImgBusffer = new byte[datas.length];
				System.arraycopy(datas,0,tempImgBusffer,0,datas.length);
				SaveImgAsyncTask task = new SaveImgAsyncTask();
				task.execute(tempImgBusffer);
			}*/
			/*Intent intent = new Intent(MainActivity.this,ShowPicActivity.class);
			intent.putExtra("from","MAINACTIVITY");
			intent.putExtra("imgUrl","");
			startActivity(intent);*/
        }
    };

    public void ShowSetting() {
        Intent intent = new Intent(getApplicationContext(), RearCamDeviceSetupActivity.class);

        mbGetFirstImage=false;
        miAPPState= APP_BEGIN_SETUP;
        startActivityForResult(intent,ACTIVITY_TYPE_SETUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(data==null){
            Log.i("Setup", "Cancel Setup!!!");
            mbGetFirstImage=false;
            //链接不上的时候，按返回键，退出，暂时不知道是否需要增加该需要，还是一直在尝试链接？
//			onBackPressed();
            return;
        }
        else{
            miAPPState= APP_AFTER_SETUP;
            String StrResultType=data.getStringExtra(RearCamMSG.REARCAM_SETUP_RESULT);
            if(requestCode==ACTIVITY_TYPE_SETUP && StrResultType!=null){
                if(StrResultType.equals(RearCamMSG.REARCAM_SETUP_PASSWORD)){
                    StrTargetSSID = data.getStringExtra(RearCamMSG.REARCAM_SETUP_SSID);
                    StrTargetPWD  = data.getStringExtra(RearCamMSG.REARCAM_SETUP_PASSWORD);
                    Log.i("Steven", String.format("GetData : %s, %s", StrTargetSSID, StrTargetPWD ));
                    if(mService != null)
                        mService.ResetSSID(StrTargetSSID, StrTargetPWD);
                    ConnectText.setText("Set Password...");

                    if(StrTargetPWD.length() >= 5)
                        Log.i("Setup", "Set Password..."+StrTargetPWD.substring(0, 4));

                    else
                        Log.i("Setup", "Set NULL Password...");
                }
                if(StrResultType.equals(RearCamMSG.REARCAM_SETUP_CHANNEL)){
                    StrTargetChannel=data.getStringExtra(RearCamMSG.REARCAM_SETUP_CHANNEL);
                    mService.setChannel(Integer.valueOf(StrTargetChannel));
                    ConnectText.setText("Set To Channel "+StrTargetChannel+"...");
                    Log.i("Setup", "Set To Channel "+StrTargetChannel+"...");
                }
            }
        }
    }


    public ServiceConnection myConnection=new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Serviceintent = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService=((RearCamService.LocalBinder)service).getService();
            Log.i("Steven", "mService is null ...."+(mService == null));
            if(mService!=null){
                Log.i("Steven", "ServiceConnection ....");
                mService.setHandler(mMsgHandler);
                mService.setSSID(StrTargetSSID, StrTargetPWD);
                mService.setAPPState(STATE_APP_RESUME);
            }

        }
    };

    private void acquireWakeLock() {
        if(mWakeLock == null) {
            PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if(mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    public void show(byte[] showbuffer) {
        bm = BitmapFactory.decodeByteArray(showbuffer, 0, showbuffer.length, options);

        if(bm != null)
            if (!isReverse){
                jpgView.setImageBitmap(bm);
            }else{
                jpgView.setImageBitmap(BitmapUtil.reverseBitmap(bm,0));
            }

        //TODO REVIEW AI COLLITION AVOIDANCE

        frameProcessor.detect(bm, this);
        frameView.setImageBitmap(bm);
    }


    public static boolean isServiceStarted(Context context, String PackageName) {
        boolean isStarted = false;
        try {
            int intGetTaskCounter = 1000;
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> mRunningService = mActivityManager.getRunningServices(intGetTaskCounter);
            for (ActivityManager.RunningServiceInfo amService : mRunningService) {
                if (0 == amService.service.getPackageName().compareTo(PackageName)) {

                    isStarted = true;
                    break;
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return isStarted;
    }

    private void StartService() {
        if (isServiceStarted(getBaseContext(),this.getPackageName())) {
            Serviceintent = new Intent(MainActivity.this, RearCamService.class);
            bindService(Serviceintent,myConnection,Context.BIND_AUTO_CREATE);
            Log.i("Steven", "StartService Set SSID & PWD .............1");
            if(mService!=null) {
                Log.i("Steven", "StartService Set SSID & PWD .............2");
                mService.setHandler(mMsgHandler);
                mService.setSSID(StrTargetSSID, StrTargetPWD);
                mService.setAPPState(STATE_APP_RESUME);
            }
        }else {
            Log.i("Steven", "StartService Set SSID & PWD .............3");
            Serviceintent = new Intent(MainActivity.this, RearCamService.class);
            bindService(Serviceintent,myConnection,Context.BIND_AUTO_CREATE);
            startService(Serviceintent);
        }
    }

    public void CreateMessageHandler() {
        mMsgHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.i(TAG,"msg.what = "+msg.what);
                switch (msg.what) {
                    case RearCamMSG.MSG_WIFI_SET_PASSWORD_OK:
                        mbGetFirstImage=false;
                        //ConnectText.setText("Set Password OK!");
                        break;
                    case RearCamMSG.MSG_WIFI_SET_PASSWORD_FAIL:

                        ConnectText.setText("Invalid Password!");
                        mbGetFirstImage=false;
                        break;
                    case RearCamMSG.MSG_WIFI_SET_CHANNEL_OK:

                        ConnectText.setText("Set Channel OK!");
                        mbGetFirstImage=false;
                        break;
                    case RearCamMSG.MSG_WIFI_SET_CHANNEL_FAIL:

                        ConnectText.setText("Set Channel Fail!");
                        mbGetFirstImage=false;
                        break;
                    case RearCamMSG.MSG_GET_IMAGE_FAIL:
                        if(mbGetFirstImage && (miAPPState!=APP_BEGIN_SETUP)){
                            miAPPState=APP_STOP_BY_DEVICE;
                            finish();
                        }
                        else{
                            jpgView.setImageBitmap(null);
                            FPSText.setText("");
                            Log.i(TAG,"MSG_GET_IMAGE_FAIL");
                            ConnectText.setText("Connecting...");
                        }
                        break;
                    case RearCamMSG.MSG_GET_IMAGE_BUFFER:
                        byte[] tmpBuffer = msg.getData().getByteArray("JEPGBuffer");
                        if(!mbGetFirstImage){
                            mbGetFirstImage=true;
                            miImageCount=0;
                            nowFPS=30;
                        }
                        miImageCount++;
                        now_time = System.currentTimeMillis();
                        if(miImageCount == 1)
                            start_time = now_time;
                        else{
                            if(now_time-start_time>1000){
                                nowFPS = ((miImageCount-1)*1000) / (now_time-start_time);
                                if(nowFPS>30.0)
                                    nowFPS=30;
                                FPSText.setText("FPS=" + (int)nowFPS);
                                miImageCount = 0;
                            }
                        }
                        if(nowFPS<MIN_FPS_FOR_DISPLAY){//Disable Preview image when FPS less than MIN_FPS_FOR_DISPLAY
                            jpgView.setImageBitmap(null);
                            Log.i(TAG,"MSG_GET_IMAGE_BUFFER");
                            ConnectText.setText("Connecting...");
                        }
                        else{
                            show(tmpBuffer);

                            ConnectText.setText("");
                            //第一次的时候不复制数组，之后复制
                            if(!isFirstPic && tempImgBusffer == null){
                                tempImgBusffer = new byte[tmpBuffer.length];
                                System.arraycopy(tmpBuffer,0,tempImgBusffer,0,tmpBuffer.length);
                                SaveImgAsyncTask task = new SaveImgAsyncTask();
                                task.execute(tempImgBusffer);
                            }
                        }
                        break;
                    case RearCamMSG.MSG_WIFI_LISTENHB_REC_FAIL://102
                        ConnectText.setText("ERR : 102");
                        break;
                    case RearCamMSG.MSG_WIFI_SENDHB_CON_FAIL://301
                        ConnectText.setText("ERR : 301");
                        break;
                    case RearCamMSG.MSG_WIFI_SENDNHB_SEND_FAIL://302
                        ConnectText.setText("ERR : 302");
                        break;

                }
            }
        };
    }

    private class SaveImgAsyncTask extends AsyncTask<byte[], Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(byte[]... params) {
            boolean bSave = false;
            byte[] buffer = params[0];
            if(buffer.length > 0){
                //保存文件到sd卡中
                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);
                String pathname = FileUtil.getAppTempDir() + File.separator + System.currentTimeMillis() +".jpg";
                File file = new File(pathname);
                file.deleteOnExit();
                bSave = FileUtil.writeBitmapToFile(bitmap, pathname,
                        FileUtil.FileFormat.JPEG);
                if(bSave){
                    return pathname;
                }else{
                    return "";
                }
            }
            return  "";
        }

        @Override
        protected void onPostExecute(String path) {
            LoadingView.self().dismiss();
            if(!TextUtils.isEmpty(path)){
                Toast.makeText(MainActivity.this, "save success！" + FileUtil.getAppTempDir(), Toast.LENGTH_SHORT).show();
                //去掉自动跳转
				/*Intent intent = new Intent(MainActivity.this,PreviewPhotoActivity.class);
				intent.putExtra("from","MAINACTIVITY");
				intent.putExtra("imgUrl",path);
				startActivity(intent);*/
            }else{
                Toast.makeText(MainActivity.this, "fail to save！", Toast.LENGTH_SHORT).show();
            }
            //标记为false
//			isFirstPic = false;
//			tempImgBusffer = null;
        }
    }

    private void setRotation(ImageView image,int rotationNum){
        image.animate().rotation(rotationNum);
    }

    // 第一次按下返回键的事件
//	private long firstPressedTime;

    // System.currentTimeMillis() 当前系统的时间
	/*@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - firstPressedTime < 2000) {
			super.onBackPressed();
		} else {
			Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
			firstPressedTime = System.currentTimeMillis();
		}
	}*/
}