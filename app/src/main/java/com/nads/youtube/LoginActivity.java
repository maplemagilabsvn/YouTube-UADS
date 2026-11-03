package com.nads.youtube;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private WebView loginWebView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#080808"));

        // Header bar
        LinearLayout header = new LinearLayout(this);
        header.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(54)));
        header.setBackgroundColor(Color.parseColor("#0F0F0F"));
        header.setPadding(dp(16), 0, dp(16), 0);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView btnClose = new TextView(this);
        btnClose.setText("✕ Đóng");
        btnClose.setTextColor(Color.parseColor("#EF4444"));
        btnClose.setTextSize(15);
        btnClose.setTypeface(Typeface.DEFAULT_BOLD);
        btnClose.setPadding(dp(4), dp(8), dp(12), dp(8));
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });

        TextView title = new TextView(this);
        title.setText("Đăng nhập Tài khoản Google");
        title.setTextColor(Color.WHITE);
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));

        TextView btnReload = new TextView(this);
        btnReload.setText("🔄");
        btnReload.setTextSize(16);
        btnReload.setPadding(dp(8), dp(8), dp(4), dp(8));
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (loginWebView != null) loginWebView.reload();
            }
        });

        header.addView(btnClose);
        header.addView(title);
        header.addView(btnReload);

        FrameLayout contentFrame = new FrameLayout(this);
        contentFrame.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        FrameLayout.LayoutParams pbLp = new FrameLayout.LayoutParams(-1, dp(3));
        pbLp.gravity = Gravity.TOP;
        progressBar.setLayoutParams(pbLp);
        progressBar.setMax(100);

        loginWebView = new WebView(this);
        loginWebView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        WebSettings ws = loginWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(loginWebView, true);

        loginWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        loginWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("youtube.com") && !url.contains("accounts.google.com")) {
                    SharedPreferences prefs = getSharedPreferences("YouTubeNADS_Prefs", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("is_logged_in", true)
                            .putBoolean("just_logged_in", true)
                            .apply();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                    return true;
                }
                view.loadUrl(url);
                return true;
            }
        });

        loginWebView.loadUrl("https://accounts.google.com/ServiceLogin?service=youtube");

        contentFrame.addView(loginWebView);
        contentFrame.addView(progressBar);

        layout.addView(header);
        layout.addView(contentFrame);

        setContentView(layout);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (loginWebView != null && loginWebView.canGoBack()) {
            loginWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
