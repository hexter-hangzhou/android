package com.hexter.webview2java;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements JSBridge {
    private WebView mWebView;
    private TextView mTextView;


    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.web_view);
        mTextView = findViewById(R.id.text_view);
        //允许WebView加载JS
        mWebView.getSettings().setJavaScriptEnabled(true);
        //给WebView添加JS接口
        mWebView.addJavascriptInterface(new JSInterface(this),"launcher");

        mWebView.loadUrl("http://125.124.128.153:7800/index.html");
    }

    @Override
    public void setTextView(final String str) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.setVisibility(View.GONE);
                mTextView.setText(str);
            }
        });

    }
}
