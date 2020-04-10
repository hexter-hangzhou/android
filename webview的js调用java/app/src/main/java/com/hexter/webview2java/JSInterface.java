package com.hexter.webview2java;

import android.webkit.JavascriptInterface;

public class JSInterface {

    private JSBridge jsBridge;

    public JSInterface(JSBridge jsBridge){
        this.jsBridge = jsBridge;
    }

    //此方法不在主线程中执行
    @JavascriptInterface //必须添加此注解，否则无法识别
    public void setValue(String value){
        jsBridge.setTextView(value);
    }
}