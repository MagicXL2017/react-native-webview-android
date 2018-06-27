package com.burnweb.rnwebview;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.VideoView;

<<<<<<< HEAD
=======
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.SystemClock;
>>>>>>> d6feafc81eef4d9f8dabe36f34404c8f32f30781
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.common.SystemClock;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;

class RNWebView extends WebView implements LifecycleEventListener {

    private final EventDispatcher mEventDispatcher;
    private final RNWebViewManager mViewManager;

    private final WebViewEventEmitter webEventEmitter;

    private String charset = "UTF-8";
    private String baseUrl = "file:///";
    private String injectedJavaScript = null;
    private boolean allowUrlRedirect = false;

<<<<<<< HEAD
    private boolean isFullScreen = false;
    private boolean isCanFullScreen = false;

    private double viewWidth = 100;
    private double viewHeight = 100;

    private View mCustomView;
    private int mOriginalSystemUiVisibility;
    private int mOriginalOrientation;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    CustomWebChromeClient customWebChromeClient;

    @Override
    public void setId(int id) {
        super.setId(id);
        webEventEmitter.setViewId(id);
    }

=======
    private String currentUrl = "";
    private String shouldOverrideUrlLoadingUrl = "";
>>>>>>> d6feafc81eef4d9f8dabe36f34404c8f32f30781

    protected class EventWebClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            int navigationType = 0;

            if (currentUrl.equals(url) || url.equals("about:blank")) { // for regular .reload() and html reload.
                navigationType = 3;
            }

            shouldOverrideUrlLoadingUrl = url;
            mEventDispatcher.dispatchEvent(new ShouldOverrideUrlLoadingEvent(getId(), SystemClock.nanoTime(), url, navigationType));

            return true;
        }

        public void onPageFinished(WebView view, String url) {
            mEventDispatcher.dispatchEvent(new NavigationStateChangeEvent(getId(), SystemClock.nanoTime(), view.getTitle(), false, url, view.canGoBack(), view.canGoForward()));

<<<<<<< HEAD
            // Run javascript code that detects the video end and notifies the interface
            String js = "javascript:(function() {";
            js += "_ytrp_html5_video = document.getElementsByTagName('video')[0];";
            js += "if (_ytrp_html5_video !== undefined) {";
            {
                js += "function _ytrp_html5_video_ended() {";
                {
//                        js += "_ytrp_html5_video.removeEventListener('ended', _ytrp_html5_video_ended);";
                    js += "_VideoEnabledWebView.notifyVideoEnd();"; // Must match Javascript interface name and method of VideoEnableWebView
                }
                js += "}";
                js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);";

//                    js += "function _ytrp_html5_video_fullscreenchange() {";
//                    {
//                        js += "_VideoEnabledWebView.notifyVideoSetFullScreen();"; // Must match Javascript interface name and method of VideoEnableWebView
//                    }
//                    js += "}";
//                    js += "_ytrp_html5_video.addEventListener('fullscreenchange', _ytrp_html5_video_fullscreenchange);";
//
//                    js += "_ytrp_html5_video_fullscreen_control = document.getElementsByClassName('vjs-fullscreen-control')[0];";
//                    js += "function _ytrp_html5_video_fullscreen_control() {";
//                    {
//                        js += "_ytrp_html5_video.trigger('fullscreenchange')";
//                    }
//                    js += "}";
//                    js += "_ytrp_html5_video_fullscreen_control.addEventListener('touchstart', _ytrp_html5_video_fullscreen_control);";

            }
            js += "}";
//                js += "_VideoEnabledWebView.notifyVideoSetFullScreen();";


//                view.loadUrl(js);
=======
            currentUrl = url;
>>>>>>> d6feafc81eef4d9f8dabe36f34404c8f32f30781

            if(RNWebView.this.getInjectedJavaScript() != null) {
//                view.loadUrl("\n" + RNWebView.this.getInjectedJavaScript() + ";\n})();");
                js += RNWebView.this.getInjectedJavaScript() + ";";
            }
            js += "})();";
            view.loadUrl(js);

        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mEventDispatcher.dispatchEvent(new NavigationStateChangeEvent(getId(), SystemClock.nanoTime(), view.getTitle(), true, url, view.canGoBack(), view.canGoForward()));
        }
    }

    protected class CustomWebChromeClient extends WebChromeClient implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {



        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            getModule().showAlert(url, message, result);
            return true;
        }

        // For Android 4.1+
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            getModule().startFileChooserIntent(uploadMsg, acceptType);
        }

        // For Android 5.0+
        @SuppressLint("NewApi")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            return getModule().startFileChooserIntent(filePathCallback, fileChooserParams.createIntent());
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) // Only available in API level 14+
        {
            onShowCustomView(view, callback);
        }

        public void onShowCustomView(View view, CustomViewCallback callback){

            Log.d("RNWebView","onShowCustomView-Run");
            isCanFullScreen = true;
            if (view instanceof SurfaceView){
                Log.d("RNWebView","onShowCustomView-is SurfaceView");
            }else{
                Log.d("RNWebView","onShowCustomView-not is SurfaceView");
            }

            if (view instanceof VideoView){
                Log.d("onShowCustomView","onShowCustomView-is VideoView");
            }else{
                Log.d("RNWebView","onShowCustomView-not is VideoView");
            }



            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                onHideCustomView();
                return;
            }

            // 1. Stash the current state
            mCustomView = view;
            mOriginalSystemUiVisibility = getModule().getActivity().getWindow().getDecorView().getSystemUiVisibility();
            mOriginalOrientation = getModule().getActivity().getRequestedOrientation();

            // 2. Stash the custom view callback
            mCustomViewCallback = callback;


            // 3. Add the custom view to the view hierarchy
            FrameLayout decor = (FrameLayout) getModule().getActivity().getWindow().getDecorView();

            decor.addView(mCustomView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // 4. Change the state of the window

            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

//            if (android.os.Build.VERSION.SDK_INT >= 19) {
//                uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;//0x00001000; // SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide
//            } else {
//                uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
//            }

            decor.setSystemUiVisibility(uiFlags);

            isFullScreen = true;
            webEventEmitter.onFullScreen(isFullScreen);

            getModule().getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            super.onShowCustomView(view, callback);
        }

        public void onHideCustomView(){
            Log.d("RNWebView","onHideCustomView-Run");
            // 1. Remove the custom view
            FrameLayout decor = (FrameLayout) getModule().getActivity().getWindow().getDecorView();
            if (mCustomView != null) {
                decor.removeView(mCustomView);
                mCustomView = null;
            }

            // 2. Restore the state to it's original form
            getModule().getActivity().getWindow().getDecorView()
                    .setSystemUiVisibility(mOriginalSystemUiVisibility);
            getModule().getActivity().setRequestedOrientation(mOriginalOrientation);

            // 3. Call the custom view callback
            if (mCustomViewCallback != null) {
                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;
            }

            isFullScreen = false;
            webEventEmitter.onFullScreen(isFullScreen);
//            super.onHideCustomView();

        }

        @Override
        public void onPrepared(MediaPlayer mp) // Video will start playing, only called in the case of VideoView (typically API level <11)
        {
//            if (loadingView != null)
//            {
//                loadingView.setVisibility(View.GONE);
//            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) // Video finished playing, only called in the case of VideoView (typically API level <11)
        {
            onHideCustomView();
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) // Error while playing video, only called in the case of VideoView (typically API level <11)
        {
            return false; // By returning false, onCompletion() will be called
        }

        /**
         * Notifies the class that the back key has been pressed by the user.
         * This must be called from the Activity's onBackPressed(), and if it returns false, the activity itself should handle it. Otherwise don't do anything.
         * @return Returns true if the event was handled, and false if it is not (video view is not visible)
         */
        public boolean onBackPressed()
        {
            if (isFullScreen)
            {
                onHideCustomView();
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    protected class GeoWebChromeClient extends CustomWebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }
    }

    public class JavascriptInterface
    {
        @android.webkit.JavascriptInterface @SuppressWarnings("unused")
        public void notifyVideoEnd() // Must match Javascript interface method of VideoEnabledWebChromeClient
        {
            Log.d("RNWebView", "GOT notifyVideoEnd");
            // This code is not executed in the UI thread, so we must force that to happen
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {

                    if (isCanFullScreen){
                        if (customWebChromeClient != null && mCustomView != null)
                    {
                        customWebChromeClient.onHideCustomView();
                    }
                    }else{
                        quitFullScreen();
                    }

                }
            });
        }

        @android.webkit.JavascriptInterface @SuppressWarnings("unused")
        public void notifyVideoSetFullScreen() // Must match Javascript interface method of VideoEnabledWebChromeClient
        {
            Log.d("RNWebView", "GOT notifyVideoSetFullScreen");
            // This code is not executed in the UI thread, so we must force that to happen
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    setFullScreen();
                }
            });
        }
        @android.webkit.JavascriptInterface @SuppressWarnings("unused")
        public void notifyVideoExitFullScreen() // Must match Javascript interface method of VideoEnabledWebChromeClient
        {
            Log.d("RNWebView", "GOT notifyVideoExitFullScreen");
            // This code is not executed in the UI thread, so we must force that to happen
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    quitFullScreen();
                }
            });
        }
    }

    public RNWebView(RNWebViewManager viewManager, ThemedReactContext reactContext) {
        super(reactContext);

        mViewManager = viewManager;
        mEventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();

        webEventEmitter = new WebViewEventEmitter(reactContext);

        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setSupportZoom(false);
        this.getSettings().setBuiltInZoomControls(false);
        this.getSettings().setDomStorageEnabled(true);
        this.getSettings().setGeolocationEnabled(false);
        this.getSettings().setPluginState(WebSettings.PluginState.ON);
        this.getSettings().setAllowFileAccess(true);
        this.getSettings().setAllowFileAccessFromFileURLs(true);
        this.getSettings().setAllowUniversalAccessFromFileURLs(true);
        this.getSettings().setLoadsImagesAutomatically(true);
        this.getSettings().setBlockNetworkImage(false);
        this.getSettings().setBlockNetworkLoads(false);
        this.getSettings().setAllowContentAccess(false);

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            this.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        this.setWebViewClient(new EventWebClient());
<<<<<<< HEAD

        this.addJavascriptInterface(new JavascriptInterface(), "_VideoEnabledWebView");
        customWebChromeClient = getCustomClient();
        this.setWebChromeClient(customWebChromeClient);

        Log.i("RNWebView","RNWebView");

=======
        this.setWebChromeClient(getCustomClient());

        this.addJavascriptInterface(RNWebView.this, "webView");
>>>>>>> d6feafc81eef4d9f8dabe36f34404c8f32f30781
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setAllowUrlRedirect(boolean a) {
        this.allowUrlRedirect = a;
    }

    public boolean getAllowUrlRedirect() {
        return this.allowUrlRedirect;
    }

    public void setInjectedJavaScript(String injectedJavaScript) {
        this.injectedJavaScript = injectedJavaScript;
    }

    public String getInjectedJavaScript() {
        return this.injectedJavaScript;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void shouldOverrideWithResult(RNWebView view, ReadableArray args) {
        if (!args.getBoolean(0)) {
            view.loadUrl(shouldOverrideUrlLoadingUrl);
        }
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }


    public void setIsFullScreen(boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
    }

    public boolean getIsFullScreen() {
        return this.isFullScreen;
    }



    public CustomWebChromeClient getCustomClient() {
        return new CustomWebChromeClient();
    }

    public GeoWebChromeClient getGeoClient() {
        return new GeoWebChromeClient();
    }

    public RNWebViewModule getModule() {
        return mViewManager.getPackage().getModule();
    }

    @Override
    public void onHostResume() {
        Log.d("RNWebView","onHostResume");
    }

    @Override
    public void onHostPause() {
        Log.d("RNWebView","onHostPause");
    }

    @Override
    public void onHostDestroy() {
        Log.d("RNWebView","onHostDestroy");
        destroy();
    }

    @Override
    public void onDetachedFromWindow() {
        this.loadDataWithBaseURL(this.getBaseUrl(), "<html></html>", "text/html", this.getCharset(), null);
        super.onDetachedFromWindow();
    }

<<<<<<< HEAD
//    public void callJS(WebView view, String args) {
//        if(args != null) {
//            view.loadUrl("javascript:(function() {\n" + args + ";\n})();");
//        }
//    }


    /**
     * 设置全屏
     */
    private void setFullScreen() {

        if (isCanFullScreen) {
            Log.d("RNWebView","setFullScreen-CanFullScreen-NoRunSetFullScreen");
            return;
        }else{
            Log.d("RNWebView","setFullScreen-NoCanFullScreen-RunSetFullScreen");
        }
        // 设置全屏的相关属性，获取当前的屏幕状态，然后设置全屏
//        getModule().getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Log.d("RNWebView","setFullScreen-Run");
//        if (mCustomView != null) {
//            quitFullScreen();
//            return;
//        }

        // 1. Stash the current state
//        mCustomView = view;
        mOriginalSystemUiVisibility = getModule().getActivity().getWindow().getDecorView().getSystemUiVisibility();
        mOriginalOrientation = getModule().getActivity().getRequestedOrientation();

        // 2. Stash the custom view callback
//        mCustomViewCallback = callback;

        FrameLayout decor = (FrameLayout) getModule().getActivity().getWindow().getDecorView();

//        View view = getModule().getActivity().getWindow();


//        Log.d("RNWebView-RootView",view.getFocusedChild());
//
//        if (view instanceof SurfaceView){
//            Log.d("RNWebView","setFullScreen-is SurfaceView");
//        }else{
//            Log.d("RNWebView","setFullScreen-not is SurfaceView");
//        }
//
//        if (view instanceof VideoView){
//            Log.d("onShowCustomView","setFullScreen-is VideoView");
//        }else{
//            Log.d("RNWebView","setFullScreen-not is VideoView");
//        }

//        decor.addView(view, new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));

        // 4. Change the state of the window

        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

//            if (android.os.Build.VERSION.SDK_INT >= 19) {
//                uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;//0x00001000; // SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide
//            } else {
//                uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
//            }

        decor.setSystemUiVisibility(uiFlags);

//        isFullScreen = true;
//        webEventEmitter.onFullScreen(isFullScreen);

        getModule().getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        // 全屏下的状态码：1098974464
//        // 窗口下的状态吗：1098973440
        isFullScreen=true;
        webEventEmitter.onFullScreen(isFullScreen);
    }

    /**
     * 退出全屏
     */
    private void quitFullScreen() {
        if (isCanFullScreen) {
            Log.d("RNWebView","quitFullScreen-CanFullScreen-NoRunQuitFullScreen");
            return;
        }else{
            Log.d("RNWebView","quitFullScreen-NoCanFullScreen-RunQuitFullScreen");
        }
        if (isFullScreen) {
            Log.d("RNWebView","quitFullScreen-isFullScreen-RunQuitFullScreen");
            // 声明当前屏幕状态的参数并获取
// 1. Remove the custom view
//        FrameLayout decor = (FrameLayout) getModule().getActivity().getWindow().getDecorView();
//        if (mCustomView != null) {
//            decor.removeView(mCustomView);
//            mCustomView = null;
//        }

            // 2. Restore the state to it's original form
            getModule().getActivity().getWindow().getDecorView()
                    .setSystemUiVisibility(mOriginalSystemUiVisibility);
//        getModule().getActivity().setRequestedOrientation(mOriginalOrientation);

            // 3. Call the custom view callback
//        if (mCustomViewCallback != null){
//            mCustomViewCallback.onCustomViewHidden();
//            mCustomViewCallback = null;
//        }

            if (getModule().getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                getModule().getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            isFullScreen = false;
            webEventEmitter.onFullScreen(isFullScreen);
        }else{
            Log.d("RNWebView","quitFullScreen-NoIsFullScreen-NoRunQuitFullScreen");
        }
    }

=======
    @JavascriptInterface
     public void postMessage(String jsParamaters) {
        mEventDispatcher.dispatchEvent(new MessageEvent(getId(), jsParamaters));
    }
>>>>>>> d6feafc81eef4d9f8dabe36f34404c8f32f30781
}
