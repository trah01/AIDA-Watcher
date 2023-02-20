package com.trah.aida;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String SHARED_PREFERENCE_NAME = "MyPrefs";
    private final String DEFAULT_URL_KEY = "DefaultUrl";
    private String mDefaultUrl = "http://192.168.100.245:88/";


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置为全屏模式
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 隐藏状态栏
        getSupportActionBar().hide();

        // 强制横屏
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.webView);
        setUpWebView();
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        // 获取SharedPreferences的编辑器
        editor = sharedPreferences.edit();

        // 从SharedPreferences中获取上次保存的默认URL
        mDefaultUrl = sharedPreferences.getString(DEFAULT_URL_KEY, mDefaultUrl);


        mWebView.loadUrl(mDefaultUrl);

        // 长按WebView弹出对话框，输入新的网址并设置为默认网址
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("输入新的网址");

                // 设置输入框
                final EditText editText = new EditText(MainActivity.this);
                editText.setMaxLines(1); // 设置只允许输入一行
                editText.setText(mDefaultUrl);
                builder.setView(editText);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newUrl = editText.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(newUrl);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setConnectTimeout(2000);
                                    connection.setRequestMethod("HEAD");
                                    int responseCode = connection.getResponseCode();
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        // 网址测试成功，将新网址设置为默认网址

                                        mDefaultUrl = newUrl;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                mWebView.loadUrl(mDefaultUrl);
                                                editor.putString(DEFAULT_URL_KEY, mDefaultUrl);
                                                editor.apply();
                                                finishAffinity();

                                                // 启动一个新的MainActivity实例
                                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        });
                                    } else {
                                        // 网址测试失败，提示错误信息
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "网址测试失败，请输入有效的网址", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "网址测试失败，请输入有效的网址", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                        // 在这里添加对新网址的访问测试
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

                return true;
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptEnabled(true);

        // 设置渲染缩放比例
        mWebView.setInitialScale(100);

        // 使用Chromium打开网页
        WebView.setWebContentsDebuggingEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient());
    }
}