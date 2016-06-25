package com.jdelorenzo.hitthegym;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.Logger;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A login screen that offers login via Google Sign In.
 * {@see https://github.com/firebase/quickstart-android/blob/master/auth/app/src/main/java/com/google/firebase/quickstart/auth/GoogleSignInActivity.java}
 */
public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    @BindView(R.id.login_progress) View mProgressView;
    @BindView(R.id.login_form) View mLoginFormView;
    @BindView(R.id.google_sign_in_button) SignInButton mGoogleSignInButton;
    @BindView(R.id.facebook_login_button) LoginButton mFacebookLoginButton;
    @BindView(R.id.email) EditText mEmailView;
    @BindView(R.id.password) EditText mPasswordView;
    private GoogleApiClient mGoogleApiClient;
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAnalytics mFirebaseAnalytics;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //log Facebook app activation events
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        //set up auth state listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mFirebaseAnalytics.setUserId(user.getUid());
                    //SyncAdapter.initializeSyncAdapter(getApplicationContext());

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        //set up facebook login
        mCallbackManager = CallbackManager.Factory.create();
        mFacebookLoginButton.setReadPermissions("email", "public_profile");
        mFacebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                showProgress(false);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                showProgress(false);
            }

            @Override
            public void onError(FacebookException error) {
                showProgress(false);
                FirebaseCrash.logcat(Log.WARN, LOG_TAG, "Facebook sign in unsuccessful:  " +
                        error.getLocalizedMessage());
            }
        });

        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            // There's immediate result available.
            handleGoogleSignInResult(pendingResult.get());
        } else {
            // There's no immediate result ready
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleGoogleSignInResult(result);
                }
            });
        }
        mGoogleSignInButton.setSize(SignInButton.SIZE_WIDE);
        mGoogleSignInButton.setScopes(gso.getScopeArray());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @OnClick(R.id.google_sign_in_button)
    public void signIn() {
        mFirebaseAnalytics.setUserProperty(getString(R.string.analytics_sign_in_key),
                getString(R.string.analytics_sign_in__google));
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        showProgress(true);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            FirebaseCrash.logcat(Log.WARN, LOG_TAG, "Facebook SignIn Failed:  " +
                                    task.getException());
                            Toast.makeText(getApplicationContext(),
                                    "Facebook Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        showProgress(false);
                    }
                });
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
            if (acct != null) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                preferences.edit()
                        .putString(getString(R.string.prefs_display_name_key), acct.getGivenName())
                        .putString(getString(R.string.prefs_google_user_id_token), acct.getIdToken())
                        .apply();
                mFirebaseAnalytics.setUserProperty(getString(R.string.analytics_user_name),
                        acct.getGivenName());
            }
        } else {
            // Signed out, show unauthenticated UI.
            String resultMessage = result.getStatus().getStatusMessage();
            int code = result.getStatus().getStatusCode();
            if (code == GoogleSignInStatusCodes.NETWORK_ERROR) {
                Toast.makeText(this, getString(R.string.common_google_play_services_network_error_text),
                        Toast.LENGTH_SHORT).show();
            }
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Google sign in unsuccessful.  Code " + code);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        showProgress(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "Completed firebase sign on with Google:  " +
                                task.isSuccessful());
                        if (!task.isSuccessful()) {
                            FirebaseCrash.logcat(Log.WARN, LOG_TAG,
                                    "Firebase sign on unsuccessful:  " + task.getException());
                        }
                        showProgress(false);
                    }
                });
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.error_connection_failed), Toast.LENGTH_SHORT).show();
        FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Connection failed:  " + connectionResult.toString());
        showProgress(false);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            valid = false;
        } else {
            mEmailView.setError(null);
        }

        String password = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            valid = false;
        } else {
            mPasswordView.setError(null);
        }

        return valid;
    }

    @OnClick(R.id.email_register_button)
    public void createAccount() {
        if (!validateForm()) {
            return;
        }

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        showProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    showProgress(false);
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        if (e != null) {
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
    }

    @OnClick(R.id.email_sign_in_button)
    public void emailSignIn() {
        if (!validateForm()) {
            return;
        }

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        showProgress(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            FirebaseCrash.logcat(Log.WARN, LOG_TAG, "Email sign in failed:  " + task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        showProgress(false);
                    }
                });
    }
}

