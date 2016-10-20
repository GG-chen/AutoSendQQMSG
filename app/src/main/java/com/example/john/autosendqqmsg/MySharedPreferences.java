package com.example.john.autosendqqmsg;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by John on 2016/10/16.
 */
public class MySharedPreferences {
    private static MySharedPreferences mySharedPreferences;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String destinction;
    public static final String zz = "zz";
    public static final String tk = "tk";

    public MySharedPreferences(Context context) {
        sp = context.getSharedPreferences("MsgMould", Activity.MODE_PRIVATE);
        editor = sp.edit();

    }

    public static MySharedPreferences getMySharedPreferences(Context context) {
        if (mySharedPreferences == null) {
            mySharedPreferences = new MySharedPreferences(context);
            return mySharedPreferences;
        } else {
            return mySharedPreferences;
        }
    }

    public void saveText(String text, boolean iszz) {
        if (iszz) {

            destinction = zz;
        } else {
            destinction = tk;
        }
        if (sp.getString(destinction, "-1") != "-1") {
            editor.putString(destinction, text);
        } else {
            editor.remove(destinction);
            editor.putString(destinction, text);
        }
        editor.commit();
        Log.i("存储", "保存成功");

    }

    public String inquiry(String key) {
        Log.i("存储", "查询成功");
        return  sp.getString(key, null);

    }
}
