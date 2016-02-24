package com.example.shon.boost4;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shon.boost4.adapter.TabPagerAdapter;
import com.example.shon.boost4.service.AccelerometerService;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    public static final String MAIN_TAG = "my_app";
    public static final int FIRST_SAMPLE_POS = 0;
    public static final int TIME_INTERVAL = 1000;

    private static final int RC_SIGN_IN = 9001;
    private static final int REQ_SIGN_IN_REQUIRED = 55664;
    private static final String FIREBASE_URL = "https://boostboost.firebaseio.com/";

    public static Firebase sUsersRef;
    public static Firebase sSamplesRef;
    public static ChildEventListener sChildEventListener;
    public static ChildEventListener uChildEventListener;

    private boolean mIsBound;
    private Context mContext = this;
    private GoogleApiClient mGoogleApiClient;
    private String mAccountName;
    private ProgressDialog mProgressDialog;
    private TextView mStatusTextView;
    private TabPagerAdapter mTabAdapter;
    private ServiceConnection mConnection = new ServiceConnection() {
        private AccelerometerService mAccBoundService;

        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mAccBoundService = ((AccelerometerService.AccelerometerBinder) service).getService();

            // Tell the user about this.
            Toast.makeText(mContext, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mAccBoundService = null;

            // Tell the user about this.
            Toast.makeText(mContext, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolBarTitleTextAppearance);
        setSupportActionBar(toolbar);

        initTabLayout();
        initGoogleSignin();
        initFireBase();
    }

    private void initTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.data)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.users)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.vp_my_tabs);
        mTabAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(mTabAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initGoogleSignin() {
        Log.d(MAIN_TAG, "MainActivity: initGoogleSignin");

        mStatusTextView = (TextView) findViewById(R.id.tv_status);

        findViewById(R.id.btn_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_sign_out).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.btn_sign_in);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

    }

    private void initFireBase() {
        sUsersRef = new Firebase(FIREBASE_URL).child("Users");
//        sSamplesRef = new Firebase(FIREBASE_URL).child("measurements");
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(MAIN_TAG, "MainActivity: onStart");

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(MAIN_TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SwitchCompat switchCompat = (SwitchCompat) menu.getItem(0)
                .getActionView().findViewById(R.id.switch_control);
        switchCompat.setOnCheckedChangeListener(this);

        return true;
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(MAIN_TAG, "MainActivity: onActivityResult");

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        if (requestCode == REQ_SIGN_IN_REQUIRED && resultCode == RESULT_OK) {
            // We had to sign in - now we can finish off the token request.
            new RetrieveTokenTask().execute(mAccountName);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(MAIN_TAG, "MainActivity: handleSignInResult - " + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            if (acct != null) {
                mAccountName = acct.getEmail();

                // run an async task to get an OAuth2 token for the account
                new RetrieveTokenTask().execute(mAccountName);
            }

            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    private void authFireBase(String token) {
        Log.d(MAIN_TAG, "TOKEN IS " + token);

        if (token != null) {
            /* Successfully got OAuth token, now login with Google */
            sUsersRef.authWithOAuthToken("google", token, new AuthResultHandler("google"));
        }

    }
    private void signIn() {
        Log.d(MAIN_TAG, "MainActivity: signIn");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Log.d(MAIN_TAG, "MainActivity: signOut");
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
        sSamplesRef = null;
        updateDataFrag();
    }
    // [END signOut]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(MAIN_TAG, "MainActivity: onConnectionFailed - " + connectionResult);
    }

    private void showProgressDialog() {
        Log.d(MAIN_TAG, "MainActivity: showProgressDialog");
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        Log.d(MAIN_TAG, "MainActivity: hideProgressDialog");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        Log.d(MAIN_TAG, "MainActivity: updateUI");
        if (signedIn) {
            findViewById(R.id.btn_sign_in).setVisibility(View.GONE);
            findViewById(R.id.btn_sign_out).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.btn_sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_sign_out).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                signIn();
                break;
            case R.id.btn_sign_out:
                signOut();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(MAIN_TAG, "MainActivity: onCheckedChanged " + isChecked);
        if (isChecked) {
            doBindService();
        } else {
            doUnbindService();
        }
    }

    void doBindService() {
        Log.d(MAIN_TAG, "MainActivity: doBindService");
        bindService(new Intent(this, AccelerometerService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        Log.d(MAIN_TAG, "MainActivity: doUnbindService");
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(MAIN_TAG, "MainActivity: onDestroy");
        super.onDestroy();
        doUnbindService();
    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:profile email";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
            } catch (IOException e) {
                Log.e(MAIN_TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(MAIN_TAG, e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            authFireBase(s);
        }
    }

    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            Log.d(MAIN_TAG, provider + " auth successful \n" +
                    "authData: " + authData.toString());
            Map<String, String> map = new HashMap<>();
            map.put("provider", authData.getProvider());
            if(authData.getProviderData().containsKey("displayName")) {
                map.put("displayName", authData.getProviderData().get("displayName").toString());
            }
            if (authData.getProviderData().containsKey("email")){
                map.put("Email", authData.getProviderData().get("email").toString());
            }
            if (sUsersRef != null) sUsersRef.removeEventListener(uChildEventListener);
            sUsersRef.child(authData.getUid()).setValue(map);



            if (sSamplesRef != null) sSamplesRef.removeEventListener(sChildEventListener);
            sSamplesRef = new Firebase(FIREBASE_URL).child(authData.getUid());
            sSamplesRef.child("user").child(authData.getUid()).setValue(Boolean.TRUE);
            sSamplesRef = sSamplesRef.child("data");


            updateDataFrag();
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
        }
    }

    private void updateDataFrag() {
        mTabAdapter.notifyDataSetChanged();
    }
}

