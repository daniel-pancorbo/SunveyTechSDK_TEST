package com.rearcam.receive.view;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.rearcam.receive.R;

/**
 * Created by jm on 2018/6/12.
 */
public class DelDialogFgt extends DialogFragment {

    private Button btn_cancle;
    private Button btn_ok;
    private View.OnClickListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        btn_cancle = (Button) v.findViewById(R.id.btn_cancle);
        btn_ok = (Button) v.findViewById(R.id.btn_ok);
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btn_ok.setOnClickListener(listener);
    }

    public static class Builder{
        private Button btn_cancle;
        private Button btn_ok;
        private View.OnClickListener listener;

        public Builder setOnClickListener(View.OnClickListener listener){
            this.listener = listener;
            return this;
        }

        public DelDialogFgt build(){
            DelDialogFgt fgt = new DelDialogFgt();
            fgt.btn_cancle = btn_cancle;
            fgt.btn_ok = btn_ok;
            fgt.listener = listener;
            return fgt;
        }
    }
}
