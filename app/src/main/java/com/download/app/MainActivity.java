package com.download.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.download.adapter.FileListAdapter;
import com.download.db.FileDAO;
import com.download.db.FileDAOImpl;
import com.download.entity.FileInfo;
import com.download.service.DownloadService;
import com.download.utils.ConstUtils;
import com.download.utils.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    private ListView lvFile;
    private List<FileInfo> fileInfoList = null;
    private FileListAdapter mAdapter;
    private String token = "812bdb066ef79f02a0545a39ac13e606";
    private String advPosition = "12";
    private Button btStart;
    private Button btStop;
    private FileDAO mFileDAO = null;
    private FileInfo fileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        init();
        initRegister();
        volleyGetAdvList();
    }

    private void init() {
        btStart = (Button) findViewById(R.id.bt_start);
        btStart.setOnClickListener(this);
        btStop = (Button) findViewById(R.id.bt_stop);
        btStop.setOnClickListener(this);
        lvFile = (ListView) findViewById(R.id.lv_file);
        //创建文件信息集合
        fileInfoList = new ArrayList<FileInfo>();
        mFileDAO = FileDAOImpl.getInstance();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_start:
                startDown();
                break;
            case R.id.bt_stop:
                stopDown();
                break;
        }
    }

    private void initRegister() { //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    /**
     * 更新UI的广播接收器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                //更新进度条
                int finished = intent.getIntExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
                mAdapter.updateProgress(id, finished);
            } else if (DownloadService.ACTION_FINISHED.equals(intent.getAction())) {
                FileInfo fileinfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                //更新进度为100
                mAdapter.updateProgress(fileinfo.getId(), 100);
                Toast.makeText(MainActivity.this, fileinfo.getFileName() + "下载完成", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("lsd", "onResume");
//        startDown();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("lsd", "onStop");
//        stopDown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private void volleyGetAdvList() {// 广告列表
        String url = ConstUtils.BASE_URL + "adv/list/position/" + advPosition + "/token/" + token;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("lsd", "AdvList response == " + response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    int state = jsonObject.getInt("state");
                    if (state == 1) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        //屏保广告
                        JSONArray pos_12 = data.getJSONArray("pos_12");
                        for (int i = 0; i < pos_12.length(); i++) {
                            JSONObject jsonItem = pos_12.getJSONObject(i);

                            int id = jsonItem.getInt("id");
                            String title = jsonItem.getString("title");
                            String type = jsonItem.getString("type");
                            String position = jsonItem.getString("position");
                            String text = jsonItem.getString("text");
                            String videopath = jsonItem.getString("videopath");
                            String picpath = jsonItem.getString("picpath");
                            String timelength = jsonItem.getString("timelength");
                            String clickable = jsonItem.getString("clickable");

//                            String videoTitle = videopath.substring(videopath.lastIndexOf('/') + 1);//URL中的视频名称
                            String videoExtension = videopath.substring(videopath.lastIndexOf('.'));//URL中的视频后缀名
                            UUID uuid = UUID.randomUUID();//生成随机文件名

                            List<FileInfo> fileInfos = mFileDAO.getFile(videopath);
                            if (fileInfos.size() == 0) {
                                //创建文件信息对象
                                fileInfo = new FileInfo(i, videopath, uuid + videoExtension, 0, 0);
                                fileInfoList.add(fileInfo);
                            } else {
                                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/MirrorClient/cache/ss-cache/" + fileInfos.get(0).getFileName());
                                if (!file.exists()) {
                                    mFileDAO.deleteFile(videopath);
                                    fileInfo = new FileInfo(i, videopath, uuid + videoExtension, 0, 0);
                                    fileInfoList.add(fileInfo);
                                }
                            }
                        }

                        //创建适配器
                        mAdapter = new FileListAdapter(MainActivity.this, fileInfoList);
                        //给listView设置适配器
                        lvFile.setAdapter(mAdapter);

//                        if (fileInfoList.size() > 0) {
//                            startDown();
//                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                Toast.makeText(context, "网络连接失败", Toast.LENGTH_LONG).show();

            }
        });
        request.setTag("screenListGet");
        MyApplication.getQueue().add(request);
    }

    private void startDown() {
        for (FileInfo fileInfo : fileInfoList) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.setAction(DownloadService.ACTION_START);
            intent.putExtra("fileInfo", fileInfo);
            context.startService(intent);
        }
    }

    private void stopDown() {
        for (FileInfo fileInfo : fileInfoList) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.setAction(DownloadService.ACTION_STOP);
            intent.putExtra("fileInfo", fileInfo);
            context.startService(intent);
        }
    }

}
