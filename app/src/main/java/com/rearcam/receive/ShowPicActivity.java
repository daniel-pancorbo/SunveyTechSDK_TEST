package com.rearcam.receive;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rearcam.receive.adapter.ShowPicAdapter;
import com.rearcam.receive.utils.FileUtil;
import com.rearcam.receive.utils.LoadingView;
import com.rearcam.receive.view.DelDialogFgt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//展示历史图片以及当前图片
public class ShowPicActivity extends FragmentActivity {

    private String rootPath = FileUtil.getAppTempDir();
    private ListView pic_list;
    private List<String> picPathes;
    private ShowPicAdapter adapter;
    private View empty;
    private DelDialogFgt delDialogFgt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);

        initView();
        initData();
    }

    private void initData() {
        /*picPathes = FileUtil.getPictures(rootPath);
        if(picPathes == null){
            picPathes = new ArrayList<>();
        }
        adapter = new ShowPicAdapter(this,picPathes);
        pic_list.setAdapter(adapter);*/
        picPathes = new ArrayList<>();
        GetImgAsyncTask getImgAsyncTask = new GetImgAsyncTask();
        getImgAsyncTask.execute();
    }

    private void initView() {
        pic_list = (ListView) findViewById(R.id.pic_list);
        empty = findViewById(R.id.empty);
        pic_list.setEmptyView(empty);

        pic_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = picPathes.get(position);
                Intent intent = new Intent(ShowPicActivity.this,PreviewPhotoActivity.class);
                intent.putExtra("imgUrl",path);
                intent.putExtra("from","SHOWPICACTIVITY");
                startActivity(intent);
            }
        });

        pic_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String path = picPathes.get(position);
                delDialogFgt = new DelDialogFgt.Builder()
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                File file = new File(path);
                                file.delete();
                                picPathes.remove(position);
                                adapter.notifyDataSetChanged();
                                delDialogFgt.dismiss();
                            }
                        }).build();
                delDialogFgt.show(getSupportFragmentManager(),"delDialogFgt");
                return true;
            }
        });
    }


    private class GetImgAsyncTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> tempPathes;
            tempPathes = FileUtil.getPictures(rootPath);
            if(tempPathes == null){
                tempPathes = new ArrayList<>();
            }else{
                Collections.sort(tempPathes, new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        File file1 = new File(lhs);
                        File file2 = new File(rhs);
                        return file2.getName().compareTo(file1.getName());
                    }
                });
            }
            return tempPathes;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LoadingView.self().show(ShowPicActivity.this,"正在获取...");
        }


        @Override
        protected void onPostExecute(List<String> pathes) {
            LoadingView.self().dismiss();
            picPathes.addAll(pathes);
            adapter = new ShowPicAdapter(ShowPicActivity.this,picPathes);
            pic_list.setAdapter(adapter);
        }
    }

}