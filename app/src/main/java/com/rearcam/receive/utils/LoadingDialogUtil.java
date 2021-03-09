package com.rearcam.receive.utils;


import android.content.Context;
import android.view.WindowManager;

import com.rearcam.receive.R;


public class LoadingDialogUtil {
    public static Loading createLoading(Context context, String message){
        Loading loading = new Loading(context, R.style.loadingDialogTheme, message);

        WindowManager.LayoutParams params = loading.getWindow().getAttributes();
//
        params.alpha = 0.6f;// 透明度
        loading.getWindow().setAttributes(params);
        return loading;
    }
}



