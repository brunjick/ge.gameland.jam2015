package ge.gameland.jam2015;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Callback mCallback;
    private WebView mWebView;
    private SwipeRefreshLayout mRefreshLayout;
    private Context mContext;

    public MainFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mContext = getActivity();

        // Configure WebView
        mWebView = (WebView) v.findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWebView.setWebViewClient(new CustomWebViewClient(mContext, new CustomWebViewClient.Callback() {
            @Override
            public void onReceivedError() {
                mCallback.onReceivedError();
            }

            @Override
            public void onRegistrationPage() {
                mCallback.onRegistrationPage();
            }
        }));

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, final int newProgress) {
                if (!mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            mRefreshLayout.setRefreshing(true);
                        }
                    });
                }

                if (newProgress >= 100) {
                    mRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            mRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });

        // Configure SwipeRefreshLayout
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mRefreshLayout.setOnRefreshListener(this);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback.onActivityCreated();
    }

    @Override
    public void onRefresh() {
        reloadWebView();
    }

    public WebView getWebView() {
        return mWebView;
    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    public void reloadWebView() {
        mWebView.reload();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onActivityCreated();

        void onReceivedError();

        void onRegistrationPage();
    }

}
