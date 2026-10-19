package jp.yanone.plannermemo;
import android.app.*;import android.os.*;import android.webkit.*;import android.content.*;import android.widget.*;import java.io.*;
public class MainActivity extends Activity {
 WebView web;
 @Override public void onCreate(Bundle b){super.onCreate(b);web=new WebView(this);setContentView(web);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.getSettings().setAllowFileAccess(true);web.addJavascriptInterface(new Bridge(),"Android");web.setWebViewClient(new WebViewClient());web.setWebChromeClient(new WebChromeClient());web.loadUrl("file:///android_asset/index.html");}
 class Bridge { @JavascriptInterface public void onStateChanged(String json){getSharedPreferences("widget",MODE_PRIVATE).edit().putString("state",json).apply();ScheduleWidget.refresh(MainActivity.this);} }
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}
