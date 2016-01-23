package ge.gameland.jam2015;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CustomWebViewClient extends WebViewClient {

    private static final String JS_FILE = "inject.js";

    private Callback mCallback;
    private String jsInjectCode;
    private Context mContext;

    public CustomWebViewClient(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;

        // Read js code from assets
        InputStream input = null;
        try {
            input = context.getAssets().open(JS_FILE);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            jsInjectCode = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (jsInjectCode == null) jsInjectCode = "";
            try {
                if (input != null) input.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);

        if (uri.getPath().contains(MainActivity.URI_REG)) {
            mCallback.onRegistrationPage();
        } else if (uri.getHost().contains(MainActivity.BASE_DOMAIN)) {
            // Load url within our WebView
            return false;
        } else {
            // Redirect to default url handler app
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // Simply execute js code
        view.loadUrl(jsInjectCode);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        onError();
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        onError();
    }

    private void onError() {
        mCallback.onReceivedError();
    }

    public interface Callback {
        void onReceivedError();

        void onRegistrationPage();
    }
}
