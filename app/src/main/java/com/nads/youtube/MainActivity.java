package com.nads.youtube;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YouTube UADS (Unadsen) - v2.0 (Bản phối lại / Remix Edition)
 * Phối lại từ dự án YouTube-NADS do tác giả Letamphong628 thực hiện.
 * Enhanced Fork featuring:
 * - HTML5 Fullscreen with WebChromeClient CustomView & Sensor Landscape
 * - Anti-Pause Background Audio Playback via Visibility API Hook
 * - Two-Tier Ad-Blocker (Network-level interception + 16x DOM Auto-Skipper)
 * - Built-in SponsorBlock Auto-Skip integration
 * - Quick Playback Speed Switcher (0.5x - 3.0x)
 * - Clean URL copy & External Downloader integration (Seal, NewPipe, 1DM, Cobalt)
 * - Deep-link Intent handler for youtube.com and youtu.be links
 * - AMOLED Pitch Black UI with Liquid Glass bottom navigation
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private FrameLayout rootFrame;
    private LinearLayout mainAppLayout;
    private FrameLayout contentFrame;
    private FrameLayout fullscreenContainer;
    private LinearLayout setupOverlay;
    private LinearLayout step0, step1, step2, step3;
    private CheckBox chkAgree;

    private WebView mainWebView;
    private ProgressBar progressBar;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private LinearLayout navHomeLayout, navShortsLayout, navSubLayout, navYouLayout;
    private ImageView imgHome, imgShorts, imgSub, imgYou;
    private TextView txtHome, txtShorts, txtSub, txtYou;

    private static final String PREF_NAME = "YouTubeUADS_Prefs";
    private static final String KEY_SETUP_DONE = "is_setup_done";
    private static final String KEY_SPONSOR_BLOCK = "sponsor_block_enabled";
    private static final String KEY_DESKTOP_MODE = "desktop_mode_enabled";

    private static final int COLOR_ACTIVE = Color.parseColor("#38BDF8");
    private static final int COLOR_INACTIVE = Color.parseColor("#94A3B8");
    private static final int COLOR_BG_AMOLED = Color.parseColor("#000000");

    private static final String UA_MOBILE = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36";
    private static final String UA_DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private long lastBackPressTime = 0;
    private float currentSpeed = 1.0f;

    // Danh sách domain & endpoint quảng cáo cần chặn ở tầng mạng
    private static final String[] AD_URL_PATTERNS = new String[] {
            "googleads.g.doubleclick.net",
            "pagead2.googlesyndication.com",
            "adservice.google.com",
            "pubads.g.doubleclick.net",
            "static.doubleclick.net",
            "youtube.com/api/stats/ads",
            "youtube.com/pagead/",
            "youtube.com/youtubei/v1/log_event"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Cấu hình thanh trạng thái Dark Mode
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(COLOR_BG_AMOLED);
            window.setNavigationBarColor(COLOR_BG_AMOLED);

            rootFrame = new FrameLayout(this);
            rootFrame.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            rootFrame.setBackgroundColor(COLOR_BG_AMOLED);

            createMainAppUI();
            createFullscreenContainer();
            createSetupOverlayUI();

            rootFrame.addView(mainAppLayout);
            rootFrame.addView(fullscreenContainer);
            rootFrame.addView(setupOverlay);

            setContentView(rootFrame);

            // Xử lý Intent link YouTube nếu mở từ ứng dụng khác
            handleIncomingIntent(getIntent());

            checkSetupState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String targetUrl = data.toString();
                if (mainWebView != null) {
                    mainWebView.loadUrl(targetUrl);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mainWebView != null) {
            mainWebView.onResume();
        }
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean("just_logged_in", false)) {
            prefs.edit().putBoolean("just_logged_in", false).apply();
            if (step2 != null && step2.getVisibility() == View.VISIBLE) {
                animateTransition(step2, step3);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Không gọi mainWebView.onPause() nhằm giữ luồng phát âm thanh khi tắt màn hình/chuyển ứng dụng
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // Tự động bật PiP khi người dùng vuốt về Home hoặc chuyển app (Android 8.0+)
        enterPiPMode();
    }

    public void enterPiPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                PictureInPictureParams params = new PictureInPictureParams.Builder()
                        .setAspectRatio(new Rational(16, 9))
                        .build();
                enterPictureInPictureMode(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Chế độ PiP yêu cầu Android 8.0 trở lên", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSetupState() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isSetupDone = prefs.getBoolean(KEY_SETUP_DONE, false);

        if (isSetupDone) {
            setupOverlay.setVisibility(View.GONE);
            mainAppLayout.setVisibility(View.VISIBLE);
            if (mainWebView.getUrl() == null || mainWebView.getUrl().isEmpty()) {
                loadYouTubeUrl("https://m.youtube.com");
            }
        } else {
            setupOverlay.setVisibility(View.VISIBLE);
            mainAppLayout.setVisibility(View.GONE);
        }
    }

    private void markSetupAsCompleted() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SETUP_DONE, true).apply();
    }

    private void createFullscreenContainer() {
        fullscreenContainer = new FrameLayout(this);
        fullscreenContainer.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        fullscreenContainer.setBackgroundColor(Color.BLACK);
        fullscreenContainer.setVisibility(View.GONE);
    }

    private void createMainAppUI() {
        mainAppLayout = new LinearLayout(this);
        mainAppLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        mainAppLayout.setOrientation(LinearLayout.VERTICAL);
        mainAppLayout.setBackgroundColor(COLOR_BG_AMOLED);

        // Header Bar hiện đại phong cách Liquid Glass
        LinearLayout topBar = new LinearLayout(this);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(54)));
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(dp(14), 0, dp(14), 0);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(Color.parseColor("#080808"));

        // Logo + Badge PRO
        LinearLayout logoBox = new LinearLayout(this);
        logoBox.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        logoBox.setOrientation(LinearLayout.HORIZONTAL);
        logoBox.setGravity(Gravity.CENTER_VERTICAL);

        TextView logo = new TextView(this);
        logo.setText("▶ UADS");
        logo.setTextColor(Color.WHITE);
        logo.setTextSize(17);
        logo.setTypeface(Typeface.DEFAULT_BOLD);

        TextView badge = new TextView(this);
        badge.setText("Unadsen v2.0");
        badge.setTextColor(COLOR_ACTIVE);
        badge.setTextSize(10);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setPadding(dp(6), dp(2), dp(6), dp(2));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.parseColor("#152535"));
        badgeBg.setCornerRadius(dp(6));
        badgeBg.setStroke(dp(1), Color.parseColor("#2563EB"));
        badge.setBackground(badgeBg);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(-2, -2);
        badgeLp.setMargins(dp(6), 0, 0, 0);
        badge.setLayoutParams(badgeLp);

        logoBox.addView(logo);
        logoBox.addView(badge);

        // Nút điều khiển trên Header Bar
        ImageView btnSpeed = createHeaderVectorIcon(R.drawable.ic_speed);
        ImageView btnPiP = createHeaderVectorIcon(R.drawable.ic_pip);
        ImageView btnDownload = createHeaderVectorIcon(R.drawable.ic_download);
        ImageView btnCast = createHeaderVectorIcon(R.drawable.ic_cast);
        ImageView btnSearch = createHeaderVectorIcon(R.drawable.ic_search);
        ImageView btnSettings = createHeaderVectorIcon(R.drawable.ic_settings);

        btnSpeed.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showSpeedDialog(); }
        });
        btnPiP.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { enterPiPMode(); }
        });
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showDownloadShareDialog(); }
        });
        btnCast.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { handleCastAction(); }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showSearchDialog(); }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showSettingsDialog(); }
        });

        topBar.addView(logoBox);
        topBar.addView(btnSpeed);
        topBar.addView(btnPiP);
        topBar.addView(btnDownload);
        topBar.addView(btnCast);
        topBar.addView(btnSearch);
        topBar.addView(btnSettings);

        // Content Area (WebView + ProgressBar)
        contentFrame = new FrameLayout(this);
        contentFrame.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));

        mainWebView = new WebView(this);
        mainWebView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        mainWebView.setBackgroundColor(COLOR_BG_AMOLED);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        FrameLayout.LayoutParams pbLp = new FrameLayout.LayoutParams(-1, dp(3));
        pbLp.gravity = Gravity.TOP;
        progressBar.setLayoutParams(pbLp);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);

        setupWebViewSettings();

        contentFrame.addView(mainWebView);
        contentFrame.addView(progressBar);

        // Bottom Navigation Bar Liquid Glass
        LinearLayout bottomNavWrapper = new LinearLayout(this);
        bottomNavWrapper.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        bottomNavWrapper.setPadding(dp(12), dp(4), dp(12), dp(10));
        bottomNavWrapper.setBackgroundColor(COLOR_BG_AMOLED);

        LinearLayout bottomNav = new LinearLayout(this);
        bottomNav.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(56)));
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);

        GradientDrawable navBg = new GradientDrawable();
        navBg.setColor(Color.parseColor("#12141A"));
        navBg.setCornerRadius(dp(28));
        navBg.setStroke(dp(1), Color.parseColor("#1E293B"));
        bottomNav.setBackground(navBg);

        navHomeLayout = createNavTab(R.drawable.ic_nav_home, "Trang chủ", true);
        navShortsLayout = createNavTab(R.drawable.ic_nav_shorts, "Shorts", false);
        navSubLayout = createNavTab(R.drawable.ic_nav_sub, "Đăng ký", false);
        navYouLayout = createNavTab(R.drawable.ic_nav_you, "Bạn", false);

        imgHome = (ImageView) navHomeLayout.getChildAt(0);
        txtHome = (TextView) navHomeLayout.getChildAt(1);

        imgShorts = (ImageView) navShortsLayout.getChildAt(0);
        txtShorts = (TextView) navShortsLayout.getChildAt(1);

        imgSub = (ImageView) navSubLayout.getChildAt(0);
        txtSub = (TextView) navSubLayout.getChildAt(1);

        imgYou = (ImageView) navYouLayout.getChildAt(0);
        txtYou = (TextView) navYouLayout.getChildAt(1);

        navHomeLayout.setOnClickListener(this);
        navShortsLayout.setOnClickListener(this);
        navSubLayout.setOnClickListener(this);
        navYouLayout.setOnClickListener(this);

        bottomNav.addView(navHomeLayout);
        bottomNav.addView(navShortsLayout);
        bottomNav.addView(navSubLayout);
        bottomNav.addView(navYouLayout);

        bottomNavWrapper.addView(bottomNav);

        mainAppLayout.addView(topBar);
        mainAppLayout.addView(contentFrame);
        mainAppLayout.addView(bottomNavWrapper);
    }

    private void setupWebViewSettings() {
        WebSettings ws = mainWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Hỗ trợ phát âm thanh không chặn quyền cử chỉ
        ws.setMediaPlaybackRequiresUserGesture(false);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isDesktop = prefs.getBoolean(KEY_DESKTOP_MODE, false);
        ws.setUserAgentString(isDesktop ? UA_DESKTOP : UA_MOBILE);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(mainWebView, true);

        // Fullscreen WebChromeClient CustomView Handler
        mainWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;

                mainAppLayout.setVisibility(View.GONE);
                fullscreenContainer.addView(customView);
                fullscreenContainer.setVisibility(View.VISIBLE);

                // Kích hoạt toàn màn hình và xoay ngang
                int flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                getWindow().getDecorView().setSystemUiVisibility(flags);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;

                fullscreenContainer.removeView(customView);
                fullscreenContainer.setVisibility(View.GONE);
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }
                customView = null;

                // Khôi phục màn hình bình thường
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                mainAppLayout.setVisibility(View.VISIBLE);
            }
        });

        mainWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Chặn mạng các domain quảng cáo và theo dõi trước khi tải về máy
                for (String pattern : AD_URL_PATTERNS) {
                    if (url.contains(pattern)) {
                        return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // Hook sớm Visibility API để chống YouTube tạm dừng khi tắt màn hình
                injectAntiPauseScript(view);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectAntiPauseScript(view);
                injectUltraAdBlock(view);
                injectSponsorBlockScript(view);
            }
        });
    }

    /**
     * Hook chống YouTube tạm dừng video khi đổi tab, tắt màn hình hoặc chuyển sang ứng dụng khác
     */
    private void injectAntiPauseScript(WebView view) {
        String script = "javascript:(function() { " +
                "if (window.__nads_anti_pause) return; " +
                "window.__nads_anti_pause = true; " +
                "try { " +
                "   Object.defineProperty(document, 'hidden', { get: function() { return false; }, configurable: true }); " +
                "   Object.defineProperty(document, 'visibilityState', { get: function() { return 'visible'; }, configurable: true }); " +
                "   Object.defineProperty(document, 'webkitVisibilityState', { get: function() { return 'visible'; }, configurable: true }); " +
                "   var stopper = function(e) { if (e) { e.stopImmediatePropagation(); } }; " +
                "   document.addEventListener('visibilitychange', stopper, true); " +
                "   window.addEventListener('visibilitychange', stopper, true); " +
                "   document.addEventListener('webkitvisibilitychange', stopper, true); " +
                "   window.addEventListener('blur', stopper, true); " +
                "} catch (e) {} " +
                "})()";
        view.loadUrl(script);
    }

    /**
     * Cơ chế chặn quảng cáo tầng DOM: ẩn banner rác + tua tốc độ 16x & bấm Skip ad tức thì
     */
    private void injectUltraAdBlock(WebView view) {
        String injectScript = "javascript:(function() { " +
                "var css = 'ytm-pivot-bar-renderer, .pivot-bar-renderer, ytm-app-bar-renderer, ytm-mobile-topbar-renderer, " +
                ".ad-showing, .ad-container, .ytp-ad-overlay-container, ytm-promoted-sparkles-web-renderer, " +
                "ytm-mealbar-promo-renderer, ytm-upsell-dialog-renderer, #offer-module, .sparkles-light-cta, " +
                ".companion-ad-container, ytd-ad-slot-renderer { display: none !important; } " +
                "ytm-feed-filter-chip-bar-renderer { margin-top: 0px !important; padding-top: 0px !important; top: 0px !important; position: sticky !important; z-index: 999 !important; }';" +
                "var style = document.createElement('style'); " +
                "style.type = 'text/css'; " +
                "style.appendChild(document.createTextNode(css)); " +
                "document.head.appendChild(style); " +
                "if (!window.__nads_ad_loop) { " +
                "   window.__nads_ad_loop = setInterval(function() { " +
                "       var video = document.querySelector('video'); " +
                "       var isAd = document.querySelector('.ad-showing, .ad-interrupting'); " +
                "       if (isAd && video) { " +
                "           video.muted = true; " +
                "           video.playbackRate = 16.0; " +
                "           if (isFinite(video.duration)) { video.currentTime = video.duration; } " +
                "       } " +
                "       var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button, .videoAdUiSkipButton'); " +
                "       if (skipBtn) { skipBtn.click(); } " +
                "   }, 250); " +
                "} " +
                "})()";
        view.loadUrl(injectScript);
    }

    /**
     * Tự động bỏ qua đoạn quảng cáo tài trợ trong video qua SponsorBlock API
     */
    private void injectSponsorBlockScript(WebView view) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean sponsorEnabled = prefs.getBoolean(KEY_SPONSOR_BLOCK, true);
        if (!sponsorEnabled) return;

        String sponsorScript = "javascript:(function() { " +
                "if (window.__nads_sponsor_active) return; " +
                "window.__nads_sponsor_active = true; " +
                "var currentVid = ''; var segments = []; " +
                "function checkAndSkip() { " +
                "   var match = window.location.href.match(/[?&]v=([^&]+)/) || window.location.href.match(/\\/shorts\\/([^?&]+)/); " +
                "   var vid = match ? match[1] : ''; " +
                "   if (vid && vid !== currentVid) { " +
                "       currentVid = vid; segments = []; " +
                "       fetch('https://sponsor.ajay.app/api/skipSegments?videoID=' + vid + '&categories=[\"sponsor\",\"intro\",\"outro\",\"selfpromo\"]') " +
                "           .then(function(r) { return r.json(); }) " +
                "           .then(function(data) { if (Array.isArray(data)) { segments = data; } }) " +
                "           .catch(function() {}); " +
                "   } " +
                "   var v = document.querySelector('video'); " +
                "   if (v && segments.length > 0) { " +
                "       var ct = v.currentTime; " +
                "       for (var i = 0; i < segments.length; i++) { " +
                "           var seg = segments[i].segment; " +
                "           if (ct >= seg[0] && ct < seg[1] - 0.5) { " +
                "               v.currentTime = seg[1]; " +
                "               break; " +
                "           } " +
                "       } " +
                "   } " +
                "} " +
                "setInterval(checkAndSkip, 600); " +
                "})()";
        view.loadUrl(sponsorScript);
    }

    private void handleCastAction() {
        try {
            String currentUrl = mainWebView != null ? mainWebView.getUrl() : "https://m.youtube.com";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
            intent.setPackage("com.google.android.youtube");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Đang quét thiết bị Cast không dây...", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSpeedDialog() {
        final String[] speeds = new String[] { "0.5x", "0.75x", "1.0x (Chuẩn)", "1.25x", "1.5x", "1.75x", "2.0x", "2.5x", "3.0x" };
        final float[] speedValues = new float[] { 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f };

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("⚡ Tốc độ phát lại");
        b.setItems(speeds, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentSpeed = speedValues[which];
                if (mainWebView != null) {
                    mainWebView.loadUrl("javascript:(function() { " +
                            "var v = document.querySelector('video'); " +
                            "if (v) { v.playbackRate = " + currentSpeed + "; } " +
                            "})()");
                    Toast.makeText(MainActivity.this, "Đã chỉnh tốc độ: " + speeds[which], Toast.LENGTH_SHORT).show();
                }
            }
        });
        b.show();
    }

    private void showDownloadShareDialog() {
        final String currentUrl = mainWebView != null ? mainWebView.getUrl() : "https://m.youtube.com";
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("📥 Tải xuống & Chia sẻ");

        String[] options = new String[] {
                "🔗 Sao chép liên kết sạch (Không mã theo dõi)",
                "📲 Mở bằng App Tải ngoài (Seal, NewPipe, 1DM, YTDLnis)",
                "🌐 Tải qua Web Downloader nhanh (Cobalt)",
                "📤 Chia sẻ liên kết cho bạn bè"
        };

        b.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    String cleanUrl = cleanYouTubeUrl(currentUrl);
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("YouTube URL", cleanUrl));
                    Toast.makeText(MainActivity.this, "Đã sao chép link sạch:\n" + cleanUrl, Toast.LENGTH_SHORT).show();
                } else if (which == 1) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, currentUrl);
                    startActivity(Intent.createChooser(sendIntent, "Chọn ứng dụng tải video"));
                } else if (which == 2) {
                    mainWebView.loadUrl("https://cobalt.tools/#" + Uri.encode(currentUrl));
                } else if (which == 3) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, cleanYouTubeUrl(currentUrl));
                    startActivity(Intent.createChooser(shareIntent, "Chia sẻ video"));
                }
            }
        });
        b.show();
    }

    private String cleanYouTubeUrl(String url) {
        if (url == null) return "https://m.youtube.com";
        Pattern pattern = Pattern.compile("(?:v=|/shorts/|youtu\\.be/)([a-zA-Z0-9_-]{11})");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return "https://youtu.be/" + matcher.group(1);
        }
        return url;
    }

    private void showSettingsDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("⚙️ Cài đặt YouTube UADS (Unadsen)");

        final SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        final boolean currentSponsor = prefs.getBoolean(KEY_SPONSOR_BLOCK, true);
        final boolean currentDesktop = prefs.getBoolean(KEY_DESKTOP_MODE, false);

        String[] items = new String[] {
                "⚡ SponsorBlock (Bỏ qua đoạn tài trợ): " + (currentSponsor ? "BẬT" : "TẮT"),
                "🖥️ Chế độ Máy tính (Desktop Mode): " + (currentDesktop ? "BẬT" : "TẮT"),
                "🗑️ Xóa bộ nhớ đệm Cache & Cookie",
                "🔑 Đăng nhập lại Tài khoản Google"
        };

        b.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    boolean nextVal = !currentSponsor;
                    prefs.edit().putBoolean(KEY_SPONSOR_BLOCK, nextVal).apply();
                    Toast.makeText(MainActivity.this, "SponsorBlock: " + (nextVal ? "Đã bật" : "Đã tắt"), Toast.LENGTH_SHORT).show();
                    mainWebView.reload();
                } else if (which == 1) {
                    boolean nextDesktop = !currentDesktop;
                    prefs.edit().putBoolean(KEY_DESKTOP_MODE, nextDesktop).apply();
                    mainWebView.getSettings().setUserAgentString(nextDesktop ? UA_DESKTOP : UA_MOBILE);
                    Toast.makeText(MainActivity.this, "Chế độ máy tính: " + (nextDesktop ? "Đã bật" : "Đã tắt"), Toast.LENGTH_SHORT).show();
                    mainWebView.reload();
                } else if (which == 2) {
                    mainWebView.clearCache(true);
                    CookieManager.getInstance().removeAllCookies(null);
                    Toast.makeText(MainActivity.this, "Đã xóa sạch bộ nhớ tạm Cache", Toast.LENGTH_SHORT).show();
                    mainWebView.reload();
                } else if (which == 3) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                }
            }
        });
        b.show();
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔍 Tìm kiếm Video");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(10), dp(20), dp(10));

        final EditText input = new EditText(this);
        input.setHint("Nhập tên video, bài hát, nghệ sĩ...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        layout.addView(input);

        builder.setView(layout);
        builder.setPositiveButton("Tìm ngay", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                String q = input.getText().toString().trim();
                if (!q.isEmpty()) {
                    loadYouTubeUrl("https://m.youtube.com/results?search_query=" + Uri.encode(q));
                }
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private LinearLayout createNavTab(int iconRes, String label, boolean active) {
        LinearLayout tab = new LinearLayout(this);
        tab.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1.0f));
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(22), dp(22)));
        icon.setColorFilter(active ? COLOR_ACTIVE : COLOR_INACTIVE);

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextSize(10);
        text.setTextColor(active ? COLOR_ACTIVE : COLOR_INACTIVE);
        text.setGravity(Gravity.CENTER);
        text.setPadding(0, dp(2), 0, 0);

        tab.addView(icon);
        tab.addView(text);
        return tab;
    }

    private void loadYouTubeUrl(String url) {
        if (mainWebView != null) {
            mainWebView.loadUrl(url);
        }
    }

    private void createSetupOverlayUI() {
        setupOverlay = new LinearLayout(this);
        setupOverlay.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        setupOverlay.setOrientation(LinearLayout.VERTICAL);
        setupOverlay.setGravity(Gravity.CENTER);
        setupOverlay.setPadding(dp(24), dp(24), dp(24), dp(24));
        setupOverlay.setBackgroundColor(COLOR_BG_AMOLED);

        // Bước 0: Màn hình chào mừng
        step0 = new LinearLayout(this);
        step0.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        step0.setOrientation(LinearLayout.VERTICAL);
        step0.setGravity(Gravity.CENTER);

        TextView tvIcon = new TextView(this);
        tvIcon.setText("▶");
        tvIcon.setTextSize(64);
        tvIcon.setTextColor(COLOR_ACTIVE);
        tvIcon.setGravity(Gravity.CENTER);
        tvIcon.setPadding(0, dp(16), 0, dp(8));

        TextView tvWelcome = new TextView(this);
        tvWelcome.setText("YouTube UADS (Unadsen) v2.0");
        tvWelcome.setTextColor(Color.WHITE);
        tvWelcome.setTextSize(24);
        tvWelcome.setTypeface(Typeface.DEFAULT_BOLD);
        tvWelcome.setGravity(Gravity.CENTER);

        TextView tvSubWelcome = new TextView(this);
        tvSubWelcome.setText("Bản phối lại từ dự án YouTube-NADS của tác giả Letamphong628.\nTrải nghiệm xem video không quảng cáo, PiP, chạy nền và tự động bỏ qua tài trợ (SponsorBlock).");
        tvSubWelcome.setTextColor(Color.GRAY);
        tvSubWelcome.setTextSize(14);
        tvSubWelcome.setGravity(Gravity.CENTER);
        tvSubWelcome.setPadding(dp(16), dp(10), dp(16), dp(16));

        LinearLayout btnGroup0 = new LinearLayout(this);
        btnGroup0.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        btnGroup0.setOrientation(LinearLayout.HORIZONTAL);
        btnGroup0.setGravity(Gravity.CENTER);
        btnGroup0.setPadding(0, dp(16), 0, 0);

        TextView btnStart = createButton("Bắt đầu thiết lập", COLOR_ACTIVE);
        TextView btnSkip = createButton("Để sau", Color.parseColor("#272727"));

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { animateTransition(step0, step1); }
        });
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                markSetupAsCompleted();
                animateTransition(setupOverlay, mainAppLayout);
                loadYouTubeUrl("https://m.youtube.com");
            }
        });

        btnGroup0.addView(btnStart);
        btnGroup0.addView(btnSkip);

        step0.addView(tvIcon);
        step0.addView(tvWelcome);
        step0.addView(tvSubWelcome);
        step0.addView(btnGroup0);

        // Bước 1: Điều khoản dịch vụ
        step1 = new LinearLayout(this);
        step1.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        step1.setOrientation(LinearLayout.VERTICAL);
        step1.setVisibility(View.GONE);

        TextView tvTermsTitle = new TextView(this);
        tvTermsTitle.setText("1. Điều Khoản & Quyền Riêng Tư");
        tvTermsTitle.setTextColor(Color.WHITE);
        tvTermsTitle.setTextSize(18);
        tvTermsTitle.setTypeface(Typeface.DEFAULT_BOLD);

        ScrollView svText = new ScrollView(this);
        svText.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));
        svText.setPadding(dp(14), dp(14), dp(14), dp(14));
        svText.setBackgroundColor(Color.parseColor("#12141A"));

        TextView tvTermsBody = new TextView(this);
        tvTermsBody.setText("ĐIỀU KHOẢN DỊCH VỤ YOUTUBE UADS (UNADSEN) V2.0\n\n" +
                "1. Bản phối lại (Remix) phát triển dựa trên nền tảng dự án YouTube-NADS do tác giả Letamphong628 thực hiện.\n\n" +
                "2. Ứng dụng hoạt động như một trình duyệt chuyên dụng tối ưu cho YouTube di động.\n\n" +
                "3. Quyền riêng tư: Ứng dụng không thu thập, không lưu trữ thông tin đăng nhập hay theo dõi người dùng.\n\n" +
                "4. Các tính năng tích hợp: Tự động chặn quảng cáo, bảo vệ phát nền không tạm dừng, SponsorBlock và PiP.");
        tvTermsBody.setTextColor(Color.LTGRAY);
        tvTermsBody.setTextSize(13);
        svText.addView(tvTermsBody);

        chkAgree = new CheckBox(this);
        chkAgree.setText("Tôi đồng ý với các điều khoản dịch vụ");
        chkAgree.setTextColor(Color.WHITE);
        chkAgree.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView btnNext1 = createButton("Tiếp theo", COLOR_ACTIVE);
        btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (chkAgree.isChecked()) {
                    animateTransition(step1, step2);
                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng tích chọn đồng ý điều khoản!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        step1.addView(tvTermsTitle);
        step1.addView(svText);
        step1.addView(chkAgree);
        step1.addView(btnNext1);

        // Bước 2: Đăng nhập Google
        step2 = new LinearLayout(this);
        step2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        step2.setOrientation(LinearLayout.VERTICAL);
        step2.setGravity(Gravity.CENTER);
        step2.setVisibility(View.GONE);

        TextView tvAccTitle = new TextView(this);
        tvAccTitle.setText("2. Tài Khoản Google / YouTube");
        tvAccTitle.setTextColor(Color.WHITE);
        tvAccTitle.setTextSize(20);
        tvAccTitle.setTypeface(Typeface.DEFAULT_BOLD);

        TextView tvAccSub = new TextView(this);
        tvAccSub.setText("Đăng nhập tài khoản để đồng bộ danh sách kênh đăng ký, playlist và lịch sử xem.");
        tvAccSub.setTextColor(Color.GRAY);
        tvAccSub.setGravity(Gravity.CENTER);
        tvAccSub.setPadding(dp(16), dp(12), dp(16), dp(24));

        TextView btnAddAcc = createButton("Đăng nhập ngay", COLOR_ACTIVE);
        TextView btnSkipAcc = createButton("Bỏ qua", Color.parseColor("#272727"));

        btnAddAcc.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnSkipAcc.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { animateTransition(step2, step3); }
        });

        step2.addView(tvAccTitle);
        step2.addView(tvAccSub);
        step2.addView(btnAddAcc);
        step2.addView(btnSkipAcc);

        // Bước 3: Hoàn tất
        step3 = new LinearLayout(this);
        step3.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        step3.setOrientation(LinearLayout.VERTICAL);
        step3.setGravity(Gravity.CENTER);
        step3.setVisibility(View.GONE);

        TextView tvDoneTitle = new TextView(this);
        tvDoneTitle.setText("3. Thiết Lập Hoàn Tất!");
        tvDoneTitle.setTextColor(Color.WHITE);
        tvDoneTitle.setTextSize(22);
        tvDoneTitle.setTypeface(Typeface.DEFAULT_BOLD);

        TextView tvDoneSub = new TextView(this);
        tvDoneSub.setText("Mọi thứ đã sẵn sàng. Hãy bắt đầu tận hưởng âm nhạc và video tuyệt vời ngay bây giờ!");
        tvDoneSub.setTextColor(Color.GRAY);
        tvDoneSub.setGravity(Gravity.CENTER);
        tvDoneSub.setPadding(dp(16), dp(12), dp(16), dp(32));

        TextView btnFinish = createButton("Bắt đầu trải nghiệm", COLOR_ACTIVE);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                markSetupAsCompleted();
                animateTransition(setupOverlay, mainAppLayout);
                loadYouTubeUrl("https://m.youtube.com");
            }
        });

        step3.addView(tvDoneTitle);
        step3.addView(tvDoneSub);
        step3.addView(btnFinish);

        setupOverlay.addView(step0);
        setupOverlay.addView(step1);
        setupOverlay.addView(step2);
        setupOverlay.addView(step3);
    }

    private void animateTransition(View hideView, View showView) {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(180);
        hideView.startAnimation(fadeOut);
        hideView.setVisibility(View.GONE);

        showView.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(220);
        showView.startAnimation(fadeIn);
    }

    private ImageView createHeaderVectorIcon(int resId) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(resId);
        iv.setPadding(dp(9), dp(9), dp(9), dp(9));
        iv.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
        return iv;
    }

    private TextView createButton(String text, int bgColor) {
        TextView btn = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(150), dp(46));
        lp.setMargins(dp(6), dp(8), dp(6), dp(8));
        btn.setLayoutParams(lp);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(14);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dp(23));
        btn.setBackground(bg);
        return btn;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        // Thoát Fullscreen nếu đang xem video toàn màn hình
        if (customView != null) {
            if (mainWebView != null && mainWebView.getWebChromeClient() != null) {
                mainWebView.getWebChromeClient().onHideCustomView();
            }
            return;
        }

        // Quay lại trang trước trong lịch sử duyệt web
        if (mainWebView != null && mainWebView.canGoBack()) {
            mainWebView.goBack();
            return;
        }

        // Nhấn 2 lần trong 2 giây để thoát app tránh bấm nhầm
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBackPressTime < 2000) {
            super.onBackPressed();
        } else {
            lastBackPressTime = currentTime;
            Toast.makeText(this, "Nhấn lần nữa để thoát ứng dụng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        imgHome.setColorFilter(COLOR_INACTIVE);
        txtHome.setTextColor(COLOR_INACTIVE);

        imgShorts.setColorFilter(COLOR_INACTIVE);
        txtShorts.setTextColor(COLOR_INACTIVE);

        imgSub.setColorFilter(COLOR_INACTIVE);
        txtSub.setTextColor(COLOR_INACTIVE);

        imgYou.setColorFilter(COLOR_INACTIVE);
        txtYou.setTextColor(COLOR_INACTIVE);

        if (v == navHomeLayout) {
            imgHome.setColorFilter(COLOR_ACTIVE);
            txtHome.setTextColor(COLOR_ACTIVE);
            loadYouTubeUrl("https://m.youtube.com");
        } else if (v == navShortsLayout) {
            imgShorts.setColorFilter(COLOR_ACTIVE);
            txtShorts.setTextColor(COLOR_ACTIVE);
            loadYouTubeUrl("https://m.youtube.com/shorts");
        } else if (v == navSubLayout) {
            imgSub.setColorFilter(COLOR_ACTIVE);
            txtSub.setTextColor(COLOR_ACTIVE);
            loadYouTubeUrl("https://m.youtube.com/feed/subscriptions");
        } else if (v == navYouLayout) {
            imgYou.setColorFilter(COLOR_ACTIVE);
            txtYou.setTextColor(COLOR_ACTIVE);
            loadYouTubeUrl("https://m.youtube.com/feed/library");
        }
    }
}
