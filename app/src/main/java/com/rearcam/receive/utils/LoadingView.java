package com.rearcam.receive.utils;

import android.content.Context;


public class LoadingView {

    private static LoadingView single_instance;
    private Loading loading;

    private LoadingView() {

    }

    synchronized public static LoadingView self() {
        if (single_instance == null)
            single_instance = new LoadingView();
        return single_instance;
    }

    synchronized public void show(Context context, String showText) {
        //if(mSVProgressHUD == null)
        //mSVProgressHUD = new SVProgressHUD(context);
        //mSVProgressHUD.showWithStatus(showText);
        loading = LoadingDialogUtil.createLoading(context,showText);
        loading.show();
    }

    synchronized public void dismiss() {
        if (loading != null)
            loading.dismiss();
    }


    public boolean isShowing() {
        if (loading != null && loading.isShowing()) {
            return true;
        }
        return false;
    }
}
