package com.rearcam.receive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rearcam.receive.constant.SpConstant;
import com.rearcam.receive.utils.SpUtil;

import java.util.ArrayList;
import java.util.List;

public class RearCamDeviceSetupActivity extends Activity implements OnClickListener {
		
	public final int ACTIVITY_TYPE_SETUP        = 1;	
	
	final String LOG_TITLE = "DeviceSetup";
	
	public static final String KEY_SSID_RESULT = "SSIDRESULT";
	public static final String SSID_PREFIX = "SWD";
	MainActivity mActivity = null;
	View					mView = null;
	EditText				mEditDeviceSSID = null;
	EditText				mEditRouterSSID = null;
	EditText				mEditPwd = null;
	EditText				mEditChannel = null;
	Button					mBtnSetup = null;
	Button					mBtnChannelSetup = null;
	Spinner					mSpinnerDeviceSSID = null;
	Spinner					mSpinnerRouterSSID = null;
	Spinner					mSpinnerRouterSSID02 = null;
	ProgressDialog 			mProgressDialog = null;
	
	int						mRouterSSIDCount = 0;
	String mDeviceName = null;
	String mDeviceUID = null;
	Handler     			mHandler = null;
	Handler 				mCmdHandler = null;
	
	WifiConfiguration		mOriWifiConf = null;
	String mDeviceSSID = null, mDevicePWD = "", mDeviceChannel = null;
	String mCurrDeviceSSID = null, mCurrDevicePWD = null, mCurrDeviceChannel = null;
	Spinner                  mSpinnerDeviceChannel = null;
	private String[] channelListBuffer = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private ArrayAdapter<String> channelList;
	
	WifiManager 	mWifiManager;
	private ScanWifiReceiver mScanWifiReceiver = new ScanWifiReceiver();	
	Handler         mhScanDeivce=new Handler();
	List<String> mRouterSSIDList = null;
	List<String> mEncryptionList = null; //add by steven, 20140107
	private int     mDeviceCount=0;
	private int     miChannel=0;
	SharedPreferences mSettings;
	private TextView mWlan;
	private Button mConfirm;
	private SpUtil spUtil;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_device_setup);
		
		ImageView imgDel = null;
		mSettings= getSharedPreferences(RearCamMSG.REARCAM_SETUP_NAME, 0);
        mCurrDeviceSSID   = mSettings.getString(RearCamMSG.REARCAM_SETUP_SSID, "iRearCamAP");
        mCurrDevicePWD    = mSettings.getString(RearCamMSG.REARCAM_SETUP_PASSWORD, "iRearCamPWD");
        mCurrDeviceChannel= mSettings.getString(RearCamMSG.REARCAM_SETUP_CHANNEL, "1");
		spUtil = new SpUtil(this,SpConstant.SP_FILE_NAME);

		//Router SSID List
		mSpinnerRouterSSID = (Spinner)findViewById(R.id.spinnerRouterSSID);
		mSpinnerRouterSSID.setOnItemSelectedListener(spinnerOnItemSelectedListener); //Add by steven, 20140107

		//Router SSID List
		mSpinnerRouterSSID02 = (Spinner)findViewById(R.id.spinnerRouterSSID02);
		mSpinnerRouterSSID02.setOnItemSelectedListener(spinnerOnItemSelectedListener); //Add by steven, 20140107
		//Router SSID Edit
		
		mEditRouterSSID = (EditText)findViewById(R.id.editTextRouterSSID);
		mEditRouterSSID.setInputType(InputType.TYPE_CLASS_TEXT);
				
		mEditRouterSSID.setHint(R.string.spinner_prompt_ssid);
		
		
		
		//
		mEditPwd = (EditText)findViewById(R.id.editTextPwd);
		//mEditPwd.setInputType(InputType.TYPE_CLASS_NUMBER |InputType.TYPE_TEXT_VARIATION_PASSWORD);		
		mEditPwd.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);	
		//channel
		//mEditChannel = (EditText)findViewById(R.id.editChannel);
		//mEditChannel.setInputType(InputType.TYPE_CLASS_NUMBER);
		//
		mBtnSetup = (Button)findViewById(R.id.btnSetup);
		mBtnSetup.setOnClickListener(btnSetupClickListener);
		mBtnSetup.setEnabled(false);

		mWlan = (TextView) findViewById(R.id.tv_wlan);
		mConfirm = (Button) findViewById(R.id.confirm);
		mConfirm.setOnClickListener(btnSetupClickListener);
		mConfirm.setEnabled(false);
		
		mBtnChannelSetup = (Button)findViewById(R.id.channelbtnSetup);
		mBtnChannelSetup.setOnClickListener(btnChannelSetupClickListener);
		mBtnChannelSetup.setEnabled(false);
		
		//channel spinner
		mSpinnerDeviceChannel =  (Spinner)findViewById(R.id.setDeviceChannelSpinner);
		channelList = new ArrayAdapter<String>(this.getApplicationContext(),
				R.layout.channel_spinner_list, channelListBuffer);
		channelList.setDropDownViewResource(R.layout.channel_spinner_dropdown_list);
		mSpinnerDeviceChannel.setAdapter(channelList);
		miChannel= Integer.valueOf(mCurrDeviceChannel);
		if(miChannel>11)
			miChannel=11;
		if(miChannel<1)
			miChannel=1;
		mSpinnerDeviceChannel.setSelection(miChannel-1);
		//mSpinnerDeviceChannel
		mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(!mWifiManager.isWifiEnabled())
			mWifiManager.setWifiEnabled(true);
		registerReceiver(mScanWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));		
		mRouterSSIDList = new ArrayList<String>();
		mEncryptionList = new ArrayList<String>(); //add by steven, 20140107
		deviceSetupMsgHandler();
	}	
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public synchronized void onResume() {
		super.onResume();
		if(ScanAvailableDevices()>0)
			updateRouterSSIDList();
		else	
	       mhScanDeivce.post(rScanDeivce);
		
	}
	
	@Override
    public synchronized void onPause() { 

        super.onPause();
        mhScanDeivce.removeCallbacks(rScanDeivce);
       
	}
	
	private void setEditText(final EditText editText, final ImageView imgDel) {
		
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		imgDel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				imgDel.setVisibility(View.GONE);
				editText.setText("");
			}
		});
	}
	
	private OnItemSelectedListener spinnerOnItemSelectedListener = new OnItemSelectedListener() {
			
		@Override
	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	        // your code here
			
			
			String enType = null;
			if(position < mEncryptionList.size()){
				enType = mEncryptionList.get(position);
				if(enType.equals("NONE"))
					PostMessage(RearCamMSG.MSG_WIFI_PWD_DISABLE);
				else
					PostMessage(RearCamMSG.MSG_WIFI_PWD_ENABLE);
				
			}	
			//Log.d("steven", "On Item Selected ..."+position+" "+ enType);
			
			
	    }

	    @Override
	    public void onNothingSelected(AdapterView<?> parentView) {
	        // your code here
	    	Log.d("steven", "On Nothing Selected ...");
	    }
		
	};

	private OnClickListener btnSetupClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
					
			String enType = null;
			int position = 0;
			if(mSpinnerRouterSSID02.getSelectedItem() == null){
				Log.d("steven", "FmSpinner is empty");
				return;
			}	
			
			mDeviceSSID = mSpinnerRouterSSID02.getSelectedItem().toString();
			mDevicePWD = mEditPwd.getText().toString();
			
			//add by steven, 20140108
			position = mSpinnerRouterSSID02.getSelectedItemPosition();
			Log.d("steven", "Setup Click, position is ..."+position);
			enType = mEncryptionList.get(position);
			if(!enType.equals("NONE")){
				Log.d("steven", "Setup Click, entype is Encryption");
				
				/*if(TextUtils.isEmpty(mDevicePWD) || mDevicePWD == null){
					Log.d("steven", "Setup Click return");
					return;
					
				}*/
				
			}
			//
			if(TextUtils.isEmpty(mDeviceSSID)){ //steven test || TextUtils.isEmpty(mDevicePWD)) {
				Log.d("steven", "DeviceSetup click return");
				return;
			}	
			
			if(mDeviceSSID != null ){ //steven test //& mDevicePWD != null) {

				//add for Garmin				
				//mDevicePWD = mDevicePWD+ "0000000000000000000000";
				spUtil.putBoolean(SpConstant.SP_FIRST,false);
				mSettings.edit().putString(RearCamMSG.REARCAM_SETUP_SSID, mDeviceSSID).commit();
				mSettings.edit().putString(RearCamMSG.REARCAM_SETUP_PASSWORD, mDevicePWD).commit();
 
  	  		    Intent intent=new Intent();  
  	  	        intent.putExtra(RearCamMSG.REARCAM_SETUP_RESULT, RearCamMSG.REARCAM_SETUP_PASSWORD);
  	  	  	    intent.putExtra(RearCamMSG.REARCAM_SETUP_SSID,mDeviceSSID);
  	  	  	    intent.putExtra(RearCamMSG.REARCAM_SETUP_PASSWORD,mDevicePWD);
  	  	        setResult(ACTIVITY_TYPE_SETUP,intent);  
  	  			finish();
			}
		}
		
	};
	
	private OnClickListener btnChannelSetupClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {	
			mDeviceChannel = mSpinnerDeviceChannel.getSelectedItem().toString();
			
			if(TextUtils.isEmpty(mDeviceChannel)) {
				return;
			}	
			
			//string to int
			int channel = Integer.parseInt(mDeviceChannel);
			
			if(channel <1 || channel>11) {
				return;
			}				
			mSettings.edit().putString(RearCamMSG.REARCAM_SETUP_CHANNEL, mDeviceChannel).commit();

	  		Intent intent=new Intent();  
  	  	    intent.putExtra(RearCamMSG.REARCAM_SETUP_RESULT, RearCamMSG.REARCAM_SETUP_CHANNEL);
  	  	    intent.putExtra(RearCamMSG.REARCAM_SETUP_CHANNEL,mDeviceChannel);
  	  	    setResult(ACTIVITY_TYPE_SETUP,intent);  
			finish();
		}	
			
			
		
	};
	
	private int ScanAvailableDevices(){
		mRouterSSIDList.clear();
		mEncryptionList.clear(); //add by steven, 20140107
		mDeviceCount=0;
		List<ScanResult> results = mWifiManager.getScanResults();
		for(ScanResult result: results) {
			if(result.SSID.contains(SSID_PREFIX)) {  
			   mRouterSSIDList.add(result.SSID);
			   
			   //add by steven, 20140107
			   String enType = "NONE";
			   
			   boolean wep = result.capabilities.contains("WEP");
			   if(wep == true)
				   enType = "WEP";
			   boolean wpa = result.capabilities.contains("WPA-PSK");
			   if(wpa == true)
				   enType = "WPA";
			   boolean wpa2 = result.capabilities.contains("WPA2-PSK");
			   if(wpa2 == true)
				   enType = "WPA2";
				   
			   Log.d("steven", "Encryption scheme"+" ,SSID:"+result.SSID +" ,wep: "+wep+" ,wpa: "+wpa+" ,wpa2: "+wpa2+ " "+enType);
			   mEncryptionList.add(enType);
			   //
			   mDeviceCount++;
			}
		}
		return mDeviceCount;
	}
	
	private Runnable rScanDeivce = new Runnable() {
		@Override
		public void run() {
			Log.d("ScanDevice", "rScanDeivce");
			// TODO Auto-generated method stub
		    if(!mWifiManager.isWifiEnabled()) {
		    	mWifiManager.setWifiEnabled(true);
		    }	
		    Log.d("steven", "Steven startScan() !!!");				
			mWifiManager.startScan();
		}
	};

	
	class ScanWifiReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		mRouterSSIDList.clear();
    		mEncryptionList.clear(); //add by steven, 20140107
    		mDeviceCount=0;
    		
    		Log.d("steven", "Steven got scan result !!!");
    		
    		if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
    			List<ScanResult> results = mWifiManager.getScanResults();
    			for(ScanResult result: results) {  				
    				
    				if(result.SSID.contains(SSID_PREFIX)) {  
    				   mRouterSSIDList.add(result.SSID);
    				   
    				   //add by steven, 20140107
    				   String enType = "NONE";
    				   
    				   boolean wep = result.capabilities.contains("WEP");
    				   if(wep == true)
    					   enType = "WEP";
     				   boolean wpa = result.capabilities.contains("WPA-PSK");
     				   if(wpa == true)
     					   enType = "WPA";
     				   boolean wpa2 = result.capabilities.contains("WPA2-PSK");
     				   if(wpa2 == true)
     					   enType = "WPA2";
     				   
     				   Log.i("steven", "Encryption scheme"+" ,SSID:"+result.SSID +" ,wep: "+wep+" ,wpa: "+wpa+" ,wpa2: "+wpa2+ " "+enType);
    				   mEncryptionList.add(enType);
     				   
     				   
     				   
    				   //
    				   
    				   mDeviceCount++;   				   
    				}
    		
    			}
    		}
    		PostMessage(RearCamMSG.MSG_WIFI_SCAN_OK);
    		if(mDeviceCount==0) {
    			mhScanDeivce.postDelayed(rScanDeivce,500);
    		}	
    	}
    }//end of class WifiReceiver	
	
	
	

	private void updateRouterSSIDList() {	
		Log.d(LOG_TITLE, "updateRouterSSIDList => ssidCount = "+mDeviceCount);
		if(mDeviceCount >0){		
			mEditRouterSSID.setVisibility(View.GONE);
			mSpinnerRouterSSID.setVisibility(View.VISIBLE);
			mBtnSetup.setEnabled(true);
			mConfirm.setEnabled(true);
			mBtnChannelSetup.setEnabled(true);
			ArrayAdapter<String> adapterSSID = new ArrayAdapter<String>(this.getApplicationContext(),
									R.layout.spinner_list, mRouterSSIDList);
			adapterSSID.setDropDownViewResource(R.layout.spinner_dropdown);
			mSpinnerRouterSSID.setAdapter(adapterSSID);
			mSpinnerRouterSSID02.setAdapter(adapterSSID);
		}
		else{
			mBtnSetup.setEnabled(false);  
			mBtnChannelSetup.setEnabled(false);
		}
	}
    private void PostMessage(int iMessage) {
        Message msg = mHandler.obtainMessage(iMessage);
        mHandler.sendMessage(msg);        	
    }    
	public void deviceSetupMsgHandler() {
        mHandler = new Handler() {
        	public void handleMessage(Message msg) { 
        	       		
      	  		switch (msg.what) {
      	  		
      	  		case RearCamMSG.MSG_WIFI_SCAN_OK :
      	  			Log.d(LOG_TITLE, "deviceSetupMsgHandler => MSG_WIFI_SCAN_OK !!");
      	  			updateRouterSSIDList(); 
      	  			unregisterReceiver(mScanWifiReceiver); //add by steven
      	  			break;     	  			
      	  		case RearCamMSG.MSG_WIFI_SCAN_FAIL :
      	  			Log.d(LOG_TITLE, "deviceSetupMsgHandler => MSG_WIFI_SCAN_FAIL !!");
      	  			updateRouterSSIDList();      	  			
      	  			mEditRouterSSID.setHint(R.string.spinner_prompt_ssid);
      	  			break; 
      	  		case RearCamMSG.MSG_WIFI_PWD_ENABLE:
      	  			mEditPwd.setEnabled(true);
      	  			break;
      	  		case RearCamMSG.MSG_WIFI_PWD_DISABLE:
      	  			mEditPwd.setEnabled(false);
      	  			break;
      	  		}
        	}
        };
    }
}


