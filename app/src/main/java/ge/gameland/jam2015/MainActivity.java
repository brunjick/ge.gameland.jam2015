package ge.gameland.jam2015;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import ge.gameland.jam2015.NotificationService.NotificationBinder;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, FloatingActionMenu.OnMenuToggleListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String BASE_DOMAIN = "gameland.ge";
    public static final String BASE_URL = "http://jam.gameland.ge/";
    public static final String URI_REG = "GJ/რეგისტრაცია-ქოსფლეიზე/";
    public static final String EXTRA_POST_LINK = "post_link";

    private MainFragment fragmentMain;
    private RegistrationFragment fragmentRegistration;
    private NoConnectionFragment fragmentNoConnection;
    private FloatingActionMenu mFabMenu;
    private FloatingActionButton mFabReload;
    private FloatingActionButton mFabNotifications;
    private FloatingActionButton mFabHomepage;
    private NotificationService mService;
    private PreferencesProvider mPreferencesProvider;
    private CoordinatorLayout mCoordinatorLayout;
    boolean isBound = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferencesProvider = new PreferencesProvider(this);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        // Configure Fragments
        fragmentMain = new MainFragment();
        fragmentRegistration = new RegistrationFragment();
        fragmentNoConnection = new NoConnectionFragment();

        fragmentMain.setCallback(new MainFragment.Callback() {
            @Override
            public void onActivityCreated() {
                if (savedInstanceState == null)
                    loadPostUrl(getIntent().getExtras());
            }

            @Override
            public void onReceivedError() {
                setActiveFragment(fragmentNoConnection, false);
            }

            @Override
            public void onRegistrationPage() {
                mFabMenu.hideMenuButton(true);
                setActiveFragment(fragmentRegistration, true);
            }
        });

        fragmentNoConnection.setCallback(new NoConnectionFragment.Callback() {
            @Override
            public void retryButtonClicked() {
                setActiveFragment(fragmentMain, false);
                fragmentMain.reloadWebView();
            }
        });

        fragmentRegistration.setCallback(new RegistrationFragment.Callback() {
            @Override
            public void registrationSuccess() {
                getFragmentManager().popBackStack();
                mFabMenu.showMenuButton(true);
                showSnackBar(getResources().getString(R.string.submit_success_message),
                        R.color.colorSuccess);
            }

            @Override
            public void registrationFailed() {
                showSnackBar(getResources().getString(R.string.submit_failed_message),
                        R.color.colorError);
            }
        });

        // Configure FAB
        mFabMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        mFabReload = (FloatingActionButton) findViewById(R.id.fab_reload);
        mFabNotifications = (FloatingActionButton) findViewById(R.id.fab_notifications);
        mFabHomepage = (FloatingActionButton) findViewById(R.id.fab_home_page);

        mFabMenu.setOnMenuToggleListener(this);
        mFabReload.setOnClickListener(this);
        mFabNotifications.setOnClickListener(this);
        mFabHomepage.setOnClickListener(this);

        // Configure service
        if (!isBound) {
            Intent intent = new Intent(this, NotificationService.class);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }

        restoreNotificationState();
        setActiveFragment(fragmentMain, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        loadPostUrl(intent.getExtras());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fragmentMain.getWebView().saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fragmentMain.getWebView().restoreState(savedInstanceState);
    }

    private void loadPostUrl(Bundle extras) {
        String postUrl = (extras == null)
                ? null
                : extras.getString(EXTRA_POST_LINK, null);
        if (postUrl == null) {
            fragmentMain.loadUrl(BASE_URL);
        } else {
            fragmentMain.loadUrl(postUrl);
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            WebView webView = fragmentMain.getWebView();
            if (webView.copyBackForwardList().getCurrentIndex() > 0) {
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound)
            unbindService(mServiceConnection);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_reload:
                mFabMenu.close(true);
                fragmentMain.reloadWebView();
                break;

            case R.id.fab_notifications:
                mPreferencesProvider.notificationsEnabled(!mPreferencesProvider.notificationsEnabled());
                restoreNotificationState();
                break;

            case R.id.fab_home_page:
                fragmentMain.loadUrl(BASE_URL);
                break;
        }
    }

    @Override
    public void onMenuToggle(boolean opened) {
        // Switch between close and list icons
        if (opened) {
            mFabMenu.getMenuIconView().setImageResource(R.drawable.ic_add_24dp);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mFabMenu.getMenuIconView().setImageResource(R.drawable.ic_list_24dp);
                }
            }, 150);
        }
    }

    @Override
    public void onRefresh() {
        fragmentMain.reloadWebView();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NotificationBinder binder = (NotificationBinder) service;
            mService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isBound = false;
        }
    };

    private void showSnackBar(String text, int textColor) {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, text, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(ContextCompat.getColor(getBaseContext(), textColor));
        snackbar.show();
    }

    private void setActiveFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.fragment_main, fragment);
        transaction.commit();
    }

    private void restoreNotificationState() {
        if (mPreferencesProvider.notificationsEnabled()) {
            mFabNotifications.setLabelText(getResources().getString(R.string.notifications_enabled));
            mFabNotifications.setImageResource(R.drawable.ic_notifications_24dp);
            if (mService != null) mService.forceCheckForNewPosts();
        } else {
            mFabNotifications.setLabelText(getResources().getString(R.string.notifications_disabled));
            mFabNotifications.setImageResource(R.drawable.ic_notifications_off_24dp);
        }
    }

}
