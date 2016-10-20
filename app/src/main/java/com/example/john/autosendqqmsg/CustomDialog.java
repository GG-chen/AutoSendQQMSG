package com.example.john.autosendqqmsg;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by John on 2016/10/16.
 */
public class CustomDialog extends Dialog implements View.OnClickListener {
    private EditText editer;
    private Button cancel;
    private Button save;
    private String editText = null;

    @Override
    public void onClick(View v) {
        String text = String.valueOf(editer.getText());
        switch (v.getId()) {
            case R.id.save_action:
                customDialogListener.save(text);
                break;
            case R.id.cancel_action:
                customDialogListener.cancel();
                break;
        }
    }

    //定义回调事件，用于dialog的点击事件
    public interface OnCustomDialogListener{
        public void cancel();
        public void save(String text);
    }
    private String title;
    private OnCustomDialogListener customDialogListener;

    public CustomDialog(Context context, String title, OnCustomDialogListener listener, String editText) {
        super(context);
        this.title = title;
        this.customDialogListener = listener;
        this.editText = editText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_view);
        setTitle(title);
        editer = (EditText) findViewById(R.id.dialog_edit);
        if (!TextUtils.isEmpty(editText)) {
            editer.setText(editText);
        }
        cancel = (Button) findViewById(R.id.cancel_action);
        save = (Button) findViewById(R.id.save_action);
        cancel.setOnClickListener(this);
        save.setOnClickListener(this);
    }
}
