package com.platepicks;

/**
 * Created by Jordan on 5/18/2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.mobile.user.IdentityProvider;
import com.amazonaws.mobile.user.signin.SignInManager;
import com.amazonaws.mobile.user.signin.SignInProvider;
import com.amazonaws.services.s3.model.Permission;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {
    private final String LOG_TAG = LoginFragment.class.getSimpleName();
    CallbackManager callbackManager;
    private TextView textView;

    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    private SignInManager signInManager;

    /**
     * SignInResultsHandler handles the results from sign-in for a previously signed in user.
     */
    private class SignInResultsHandler implements IdentityManager.SignInResultsHandler {
        /**
         * Receives the successful sign-in result for an alraedy signed in user and starts the main
         * activity.
         * @param provider the identity provider used for sign-in.
         */
        @Override
        public void onSuccess(final IdentityProvider provider) {
            Log.d(LOG_TAG, String.format("User sign-in with previous %s provider succeeded",
                    provider.getDisplayName()));

            // The sign-in manager is no longer needed once signed in.
            SignInManager.dispose();

            Toast.makeText(LoginFragment.this.getContext(), String.format("Sign-in with %s succeeded.",
                    provider.getDisplayName()), Toast.LENGTH_LONG).show();

            AWSMobileClient.defaultMobileClient()
                    .getIdentityManager()
                    .loadUserInfoAndImage(provider, new Runnable() {
                        @Override
                        public void run() {
                            Log.d(String.valueOf(SignInResultsHandler.this), "Success");
                        }
                    });
        }

        /**
         * For the case where the user previously was signed in, and an attempt is made to sign the
         * user back in again, there is not an option for the user to cancel, so this is overriden
         * as a stub.
         * @param provider the identity provider with which the user attempted sign-in.
         */
        @Override
        public void onCancel(final IdentityProvider provider) {
            Log.wtf(LOG_TAG, "Cancel can't happen when handling a previously sign-in user.");
        }

        /**
         * Receives the sign-in result that an error occurred signing in with the previously signed
         * in provider and re-directs the user to the sign-in activity to sign in again.
         * @param provider the identity provider with which the user attempted sign-in.
         * @param ex the exception that occurred.
         */
        @Override
        public void onError(final IdentityProvider provider, Exception ex) {
            Log.e(LOG_TAG,
                    String.format("Cognito credentials refresh with %s provider failed. Error: %s",
                            provider.getDisplayName(), ex.getMessage()), ex);

            Toast.makeText(LoginFragment.this.getContext(), String.format("Sign-in with %s failed.",
                    provider.getDisplayName()), Toast.LENGTH_LONG).show();
        }
    }

    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(LOG_TAG, "Facebook login successful!");
//            AccessToken accessToken = loginResult.getAccessToken();
//            Profile profile = Profile.getCurrentProfile();
            //displayMessage(profile);
            //Toast.makeText(getActivity(), "profile", Toast.LENGTH_SHORT).show();

            // AWS Code to get user identity
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
            AWSMobileClient.defaultMobileClient().getIdentityManager().getCredentialsProvider().setLogins(logins);
            AWSMobileClient.defaultMobileClient().getIdentityManager().getUserID(new IdentityManager.IdentityHandler() {
                @Override
                public void handleIdentityID(String identityId) {
                    Log.d(LOG_TAG, "IdentityID: " + identityId);
                }

                @Override
                public void handleError(Exception exception) {
                    Log.d(LOG_TAG, "Error");
                }
            });
            Log.d(LOG_TAG, "User Name: " + AWSMobileClient.defaultMobileClient().getIdentityManager().getUserName());
            Log.d(LOG_TAG, "Credentials Provider: " + AWSMobileClient.defaultMobileClient().getIdentityManager().getCredentialsProvider());

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {

        }

    };

    public LoginFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "LoginFragment Initiated");
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                //Toast.makeText(getActivity(), "newProfile", Toast.LENGTH_SHORT).show();
                //displayMessage(currentProfile);

            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                signInManager = SignInManager.getInstance(LoginFragment.this.getContext());

                final SignInProvider provider = signInManager.getPreviouslySignedInProvider();

                // if the user was already previously in to a provider.
                if (provider != null) {
                    Log.d(LOG_TAG, "There is a provider");
                    Log.d(LOG_TAG, "Provider: " + provider.getUserName());
                    Log.d(LOG_TAG, "Provider: " + provider.getCognitoLoginKey());
                    Log.d(LOG_TAG, "Provider: " + provider.getDisplayName());
                    // asynchronously handle refreshing credentials and call our handler.
                    signInManager.refreshCredentialsWithProvider((Activity) LoginFragment.this.getContext(),
                            provider, new SignInResultsHandler());
                } else {
                    // There is no provider
                    Log.d(LOG_TAG, "There is no provider");
                }
            }
        });
        thread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_button, container, false);
    }

    @Override
    public  void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        textView = (TextView) view.findViewById(R.id.loginTextView);

        loginButton.setReadPermissions("public_profile");
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, callback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void displayMessage(Profile profile){
        if(profile != null){
            textView.setText(profile.getName());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    @Override
    public void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        displayMessage(profile);
    }

}
