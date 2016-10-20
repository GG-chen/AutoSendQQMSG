package com.example.john.autosendqqmsg;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CustomDialog.OnCustomDialogListener {

    private Button bind;
    private Button unbind;
    private EditText text;
    private String msg;
    private Intent intentBroadCast = new Intent("com.example.communication.RECEIVER");
    private Intent mHomeIntent;
    private boolean servicing = false;
    private boolean iszz;
    private String title;
    private  CustomDialog dialog;
    private MySharedPreferences mySharedPreferences;
    private Button tozz;
    private Button totk;
    private ImageView zzImage;
    private ImageView tkImage;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("Main", "我接受到回掉了");
            servicing = true;
            sendBroadcastMsg();
            intoMain();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private EditText countEditText;
    private int count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }


    private void initData() {
        //TODO
        mySharedPreferences = MySharedPreferences.getMySharedPreferences(this);


    }

    private void initView() {
        bind = (Button) findViewById(R.id.btn_bind);
        unbind = (Button) findViewById(R.id.btn_unbind);
        text = (EditText) findViewById(R.id.mian_text);
        tozz = (Button) findViewById(R.id.tk_to_zz);
        totk = (Button) findViewById(R.id.zz_to_tk);
        zzImage = (ImageView) findViewById(R.id.to_zz_edit);
        tkImage = (ImageView) findViewById(R.id.to_tk_edit);
        countEditText = (EditText) findViewById(R.id.count);
        bind.setOnClickListener(this);
        unbind.setOnClickListener(this);
        tozz.setOnClickListener(this);
        totk.setOnClickListener(this);
        zzImage.setOnClickListener(this);
        tkImage.setOnClickListener(this);

    }

    private String getMSGText() {

        return String.valueOf(text.getText());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bind:
                Log.i("Main", "click!!");
                msg =  getMSGText();
                count =Integer.valueOf(String.valueOf(countEditText.getText()));
                //开启服务
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(this,"数据不能为空", Toast.LENGTH_SHORT).show();
                } else if (count == 0) {
                    Toast.makeText(this,"时间间隔必须大于1！！", Toast.LENGTH_SHORT).show();

                } else {
                    if (!servicing) {
                        //当服务未开启时开启服务
                        Log.i("Main", "数据不为空正在发送。。。");
                        Intent intent = new Intent(this, AutoReplyService.class);
                        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                    } else {
                        //当服务开启过  先关闭之前的服务  然后打开一个新的服务
                        unbindService(serviceConnection);
                        Intent intent = new Intent(this, AutoReplyService.class);
                        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                    }


                }

                break;
            case R.id.btn_unbind:
                //关闭服务
                if (servicing) {

                    unbindService(serviceConnection);
                    servicing = false;
                    Toast.makeText(this, "服务已关闭", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "没有服务开启", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.tk_to_zz:
                //太康去郑州
                copyToEditText(MySharedPreferences.zz);
                break;
            case R.id.zz_to_tk:
                //郑州去太康
                copyToEditText(MySharedPreferences.tk);
                break;
            case R.id.to_tk_edit:
                //编辑去太康模板
                iszz = false;
                showCustomDialog(iszz);
                break;
            case R.id.to_zz_edit:
                //编辑去郑州模板
                iszz = true;
                showCustomDialog(iszz);
                break;
        }
    }

    private void copyToEditText(String destinction) {
        Log.i("Main", "开始查询");
        String inquiryText = mySharedPreferences.inquiry(destinction);
        if (inquiryText == "nothing") {
            Toast.makeText(this, "必须先编辑模板才有模板",  Toast.LENGTH_SHORT).show();
        } else {
            this.text.setText(inquiryText);
        }
    }


    private void showCustomDialog(boolean iszz) {
        String editText = null;
        if (iszz) {
            title = "太康到郑州";
            editText = mySharedPreferences.inquiry(MySharedPreferences.zz);
        } else {
            title = "郑州到太康";
            editText = mySharedPreferences.inquiry(MySharedPreferences.tk);
        }
         dialog = new CustomDialog(this,title,this, editText);
        dialog.show();

    }

    private void intoMain() {

        mHomeIntent =  new Intent(Intent.ACTION_MAIN, null);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(mHomeIntent);
    }

    private void sendBroadcastMsg() {
        intentBroadCast.putExtra("count", count);
        intentBroadCast.putExtra("text", msg);
        sendBroadcast(intentBroadCast);
    }

    @Override
    protected void onDestroy() {
        servicing = false;
        super.onDestroy();

    }


    @Override
    public void cancel() {
        dialog.cancel();
    }

    @Override
    public void save(String text) {

        mySharedPreferences.saveText(text, iszz);
        copyToEditText(iszz ? MySharedPreferences.zz : MySharedPreferences.tk);
        cancel();

    }
}
