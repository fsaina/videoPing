package com.example.filipsaina.videoping;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.filipsaina.videoping.provider.Provider;

/**
 * Personal implementation of the Android provided WebView that is better suited
 * for the applications usage as a video content player.
 * Created by filipsaina on 21/05/15.
 */
public class VideoWebViewPlayer extends WebView {

    private WebSettings playerSettings;
    private Provider provider;
    private String videoId;

    public VideoWebViewPlayer(Context context) {
        super(context);
        playerConstructor();
    }

    public VideoWebViewPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        playerConstructor();
    }

    public VideoWebViewPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        playerConstructor();
    }

    //defining basic properties
    private void playerConstructor(){
        //setup the player
        playerSettings =  this.getSettings();
        playerSettings.setJavaScriptEnabled(true);
        playerSettings.setPluginState(WebSettings.PluginState.ON);
        playerSettings.setAllowFileAccess(true);
        playerSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        playerSettings.setAppCacheEnabled(true);
        playerSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        if (Build.VERSION.SDK_INT >= 17) {
            //this enables autoplay
            playerSettings.setMediaPlaybackRequiresUserGesture(true);
        }
        this.setScrollbarFadingEnabled(true);
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        this.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public View getVideoLoadingProgressView() {
                ProgressBar pb = new ProgressBar(getContext());
                pb.setIndeterminate(true);
                return pb;
            }
        });
        this.setWebViewClient(new WebViewClient() {
            /*
            When the webPage loads the page, simulate a user press(a bad workaround)
            so the player would start playing the video
             */
            public void onPageFinished(WebView view, String url) {
                //guaranteed to autostart the video
                emulateClick(view, 0);
            }
        });
    }

    /**
     * Method used for loading content to a VideoWebPayer class.
     It is necessary that the referred Provider classes implements the necessary
     methods and the videoId is provided via service API
     * @param provider class that fully implements the Provider interface
     * @param videoId String object that was provided by the given provider API
     */
    public void playVideo(Provider provider, String videoId){
        this.provider = provider;
        this.videoId = videoId;
        //start from the start
        this.loadDataWithBaseURL(null, provider.getFullVideoUrl(videoId, "0"), "text/html", "utf-8", null);
    }

    /**
     * Used for jumping to given postions trught-out the video content
     * @param startSecond
     */
    //TODO implement video duration exceptins(too big seekTo, or negative)
    public void seekTo(String startSecond){
        //start from the second >time<
        this.loadDataWithBaseURL(null, provider.getFullVideoUrl(videoId, startSecond),"text/html", "utf-8", null);
    }

    /*
    Workaround for the play pause functionality for the video player
    Another way to do such a thing would be to call onPause onResume
    on hidden WebView threads (this is implementable)
     */
    public static void emulateClick(final WebView webview, long delay) {
        long downTime = SystemClock.uptimeMillis();

        float x = webview.getLeft() + webview.getWidth()/2;
        float y = webview.getBottom() - webview.getHeight()/2;

        final MotionEvent motionEvent = MotionEvent.obtain( downTime, downTime + delay, MotionEvent.ACTION_DOWN, x, y, 0 );
        final MotionEvent motionEvent2 = MotionEvent.obtain( downTime + delay + 1, downTime + delay * 2, MotionEvent.ACTION_UP, x, y, 0 );

        Runnable tapdown = new Runnable() {
            @Override
            public void run() {
                if (webview != null) {
                    webview.dispatchTouchEvent(motionEvent);
                }
            }
        };

        Runnable tapup = new Runnable() {
            @Override
            public void run() {
                if (webview != null) {
                    webview.dispatchTouchEvent(motionEvent2);
                }
            }
        };
        webview.postDelayed(tapdown, 0);
        webview.postDelayed(tapup, 10);
    }
}
