package com.example.john.autosendqqmsg;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 2016/10/15.
 */
public class AutoReplyService extends AccessibilityService {
    private boolean hasNotify = false;
    private Handler handler = new Handler();
    private String TAG = "发送服务";
    private String msg;
    private MyRceiver myReceiver;
    private List<Bean> beanLists;
    private int count = -1;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "服务开启");
        initData();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "服务停止！！");
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    private void initData() {
        myReceiver = new MyRceiver();
        beanLists = new ArrayList<Bean>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.communication.RECEIVER");
        registerReceiver(myReceiver, intentFilter);
    }
    public class MyRceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            count = intent.getIntExtra("count", -1);
            msg = intent.getStringExtra("text");
            if (TextUtils.isEmpty(msg)) {

                Toast.makeText(getApplicationContext(), "没有收到广播， 请重新开启服务", Toast.LENGTH_SHORT).show();
            }
            Log.i(TAG, "接收到广播了 text = " + msg );
            Log.i(TAG, "接收到广播了 count = " + count );

        }
    }


    /**
     * 必须重写的方法，响应各种事件。
     */
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        Log.i(TAG, "有提醒！！");

        int eventType = event.getEventType(); // 事件类型
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: // 通知栏事件
                Log.i(TAG, "TYPE_NOTIFICATION_STATE_CHANGED");
                //计算接受到信息的对象次数
                if (!countNoticeNumber(event)) {
                    return;
                }
                Log.i(TAG, "开始处理！！");
                if(PhoneController.isLockScreen(this)) { // 锁屏
                    PhoneController.wakeAndUnlockScreen(this);   // 唤醒点亮屏幕
                }
                openAppByNotification(event);
                hasNotify = true;
                break;

            default:
                Log.i(TAG, "DEFAULT");
                if (hasNotify) {
                    try {
                        Thread.sleep(1500); // 停1秒, 否则在qq主界面没进入聊天界面就执行了fillInputBar
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (fillInputBar(msg)) {
                        findAndPerformAction(UI.BUTTON, "发送");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);   // 返回
                            }
                        }, 1500);

                    }
                    hasNotify = false;
                }
                break;
        }
    }

    private boolean countNoticeNumber(AccessibilityEvent event) {
        boolean isHave = false;
        String name = getClassName(event);
        if (!TextUtils.isEmpty(name)) {

            for (Bean bean : beanLists) {
                    Log.i(TAG, "BEAN.NAME = " + bean.name);
                if (name.equalsIgnoreCase(bean.name)) {
                    isHave = true;
                    bean.count ++;
                    Log.i(TAG, "这个联系人发送过消息,数量加1， 现在Count为" + bean.count);
                    if (bean.count == count) {
                        bean.count = 0;
                        return true;
                     }
                 }
            }
            if (!isHave) {
                Log.i(TAG, "这个联系人没有发送过消息， 创建新的对象！");
                Bean bean1 = new Bean(1, name);
                beanLists.add(bean1);
            }

        }
        return false;


    }

    private String getClassName(AccessibilityEvent event) {


        String nameDetail = (String) event.getText().get(0);
        Log.i(TAG, "nameDetail = " + nameDetail);
        int size1 = nameDetail.lastIndexOf("(") + 1;
        Log.i(TAG, "size1 = " + size1);
        int size2 = nameDetail.indexOf(")");
        Log.i(TAG, "size2 = " + size2);
        if (size2 > size1) {
            nameDetail = nameDetail.substring(size1, size2);
            Log.i(TAG, "NAME : " + nameDetail);
            return nameDetail;
        }
        return null;
    }


    @Override
    public void onInterrupt() {
        Log.i(TAG, "onInterrupt");
    }


    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.packageNames = new String[]{Config.WX_PACKAGE_NAME};
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }

    /**
     * 查找UI控件并点击
     * @param widget 控件完整名称, 如android.widget.Button, android.widget.TextView
     * @param text 控件文本
     */
    private void findAndPerformAction(String widget, String text) {
        // 取得当前激活窗体的根节点
        if (getRootInActiveWindow() == null) {
            return;
        }

        // 通过文本找到当前的节点
        List<AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(text);
        if(nodes != null) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.getClassName().equals(widget) && node.isEnabled()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK); // 执行点击
                    break;
                }
            }
        }
    }



    /**
     * 打开QQ
     * @param event 事件
     */
    private void openAppByNotification(AccessibilityEvent event) {
        if (event.getParcelableData() != null  && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            try {
                PendingIntent pendingIntent = notification.contentIntent;
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * 填充输入框
     */
    private boolean fillInputBar(String reply) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            return findInputBar(rootNode, reply);
        }
        return false;
    }



    /**
     * 查找EditText控件
     * @param rootNode 根结点
     * @param reply 回复内容
     * @return 找到返回true, 否则返回false
     */
    private boolean findInputBar(AccessibilityNodeInfo rootNode, String reply) {
        int count = rootNode.getChildCount();
        Log.i(TAG, "root class=" + rootNode.getClassName() + ", " + rootNode.getText() + ", child: " + count);
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if (UI.EDITTEXT.equals(node.getClassName())) {   // 找到输入框并输入文本
                Log.i(TAG, "****found the EditText   text = " + reply);
                setText(node, reply);
                return true;
            }

            if (findInputBar(node, reply)) {    // 递归查找
                return true;
            }
        }
        return false;
    }


    /**
     * 设置文本
     */
    private void setText(AccessibilityNodeInfo node, String reply) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i(TAG, "set text");
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    reply);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        } else {
            ClipData data = ClipData.newPlainText("reply", reply);
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(data);
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS); // 获取焦点
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE); // 执行粘贴
        }
    }
}
