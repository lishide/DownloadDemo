package com.download.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.download.app.R;
import com.download.entity.FileInfo;
import com.download.service.DownloadService;

import java.util.List;

/**
 * create by luoxiaoke on 2016/4/30 17:02.
 * use for
 */
public class FileListAdapter extends BaseAdapter {
    private Context context;
    private List<FileInfo> fileList;
    private LayoutInflater inflater;

    public FileListAdapter(Context context, List<FileInfo> fileList) {
        this.context = context;
        this.fileList = fileList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FileInfo fileInfo = fileList.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_down, null);
            holder = new ViewHolder();
            holder.tvFile = (TextView) convertView.findViewById(R.id.tv_fileName);
            holder.proText = (TextView) convertView.findViewById(R.id.tv_progress);
            holder.btStart = (Button) convertView.findViewById(R.id.bt_start);
            holder.btStop = (Button) convertView.findViewById(R.id.bt_stop);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.pb_progress);

            holder.tvFile.setText(fileInfo.getFileName());
            holder.progressBar.setMax(100);
            holder.btStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.proText.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_START);
                    intent.putExtra("fileInfo", fileInfo);
                    context.startService(intent);

                }
            });
            holder.btStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_STOP);
                    intent.putExtra("fileInfo", fileInfo);
                    context.startService(intent);

                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int pro = (int) fileInfo.getFinish();
        holder.progressBar.setProgress(pro);
        holder.proText.setText(new StringBuffer().append(pro).append("%"));
        return convertView;
    }

    /**
     * 更新列表项中的进度条
     *
     * @param id       id
     * @param progress 进度
     */
    public void updateProgress(int id, long progress) {
        FileInfo fileInfo = fileList.get(id);
        fileInfo.setFinish(progress);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView tvFile;
        TextView proText;
        Button btStart;
        Button btStop;
        ProgressBar progressBar;
    }
}
