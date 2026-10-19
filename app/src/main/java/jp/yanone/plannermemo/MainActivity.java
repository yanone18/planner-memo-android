package jp.yanone.plannermemo;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.content.*;
import android.view.inputmethod.*;
import android.text.InputType;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    WebView web;

    // The calendar uses single-line HTML inputs. Force the Android keyboard
    // to show Done instead of Next, while leaving multiline memo input alone.
    private class PlannerWebView extends WebView {
        PlannerWebView(Context context) { super(context); }

        @Override public InputConnection onCreateInputConnection(EditorInfo info) {
            InputConnection base = super.onCreateInputConnection(info);
            if (base == null) return null;
            boolean multiline = (info.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
            if (multiline) return base;
            info.imeOptions = (info.imeOptions & ~EditorInfo.IME_MASK_ACTION)
                    | EditorInfo.IME_ACTION_DONE;
            info.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
            return new InputConnectionWrapper(base, false) {
                @Override public boolean performEditorAction(int actionCode) {
                    if (actionCode == EditorInfo.IME_ACTION_DONE
                            || actionCode == EditorInfo.IME_ACTION_NEXT) {
                        post(() -> {
                            evaluateJavascript("window.plannerFinishScheduleInput && window.plannerFinishScheduleInput()", null);
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            if (imm != null) imm.hideSoftInputFromWindow(getWindowToken(), 0);
                        });
                        return true;
                    }
                    return super.performEditorAction(actionCode);
                }
            };
        }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        web = new PlannerWebView(this);
        setContentView(web);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.addJavascriptInterface(new Bridge(), "Android");
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        web.loadUrl("file:///android_asset/index.html");
    }
    class Bridge {
        @JavascriptInterface public void onStateChanged(String json) {
            getSharedPreferences("widget", MODE_PRIVATE).edit().putString("state", json).apply();
            ScheduleWidget.refresh(MainActivity.this);
        }
    }
    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
