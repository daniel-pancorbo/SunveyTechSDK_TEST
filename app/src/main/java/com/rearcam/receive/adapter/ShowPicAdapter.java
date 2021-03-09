package com.rearcam.receive.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rearcam.receive.R;
import com.rearcam.receive.utils.BitmapUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by ZTE on 2017/7/20.
 */

public class ShowPicAdapter extends BaseAdapter {

    private List<String> pathes;
    private Context context;

    public ShowPicAdapter(Context context, List<String> pathes) {
        this.context = context;
        this.pathes = pathes;
    }

    @Override
    public int getCount() {
        return pathes.size();
    }

    @Override
    public Object getItem(int position) {
        return pathes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final String path = pathes.get(position);
        final File file = new File(path);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.show_pic_item_layout, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.name = (TextView) convertView.findViewById(R.id.pic_name);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.size = (TextView) convertView.findViewById(R.id.size);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.image.setImageBitmap(BitmapUtil.compressBitmap(path,35,60));
        holder.name.setText(file.getName());
        SimpleDateFormat sf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        String date = sf.format(new Date(file.lastModified()));
        holder.date.setText(date);

        long fileLength = file.length();
        String size = "0.00B";
        if(fileLength >=1024*1024){//1Mb
            size = String.format("%.2f",fileLength*1.00/1024l/1024L) + "MB";
        }else if(fileLength >=1024){//1Kb
            size = String.format("%.2f",fileLength*1.00/1024L) + "KB";
        }else{
            size = fileLength + "B";
        }
        holder.size.setText(size);

        return convertView;
    }

    public static class ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView date;
        public TextView size;
    }
}
