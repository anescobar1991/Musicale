package com.anescobar.musicale.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.models.UserCredentials;
import com.anescobar.musicale.utils.SessionManager;
import com.anescobar.musicale.utils.NetworkUtil;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Result;
import de.umass.lastfm.Session;

/**
 * This activity is used to display login screen and holds all UI logic for screen
 *
 * @author Andres Escobar
 * @version 7/14/14
 */
public class LoginActivity extends Activity {
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private SessionManager mSessionManager = new SessionManager();
    private NetworkUtil mNetworkUtil = new NetworkUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSessionManager.discardSession(this);

        //initializes UI elements to be manipulated later
        mUserNameEditText = (EditText) findViewById(R.id.activity_login_username_textfield);
        mPasswordEditText = (EditText) findViewById(R.id.activity_login_password_textfield);
        mLoginButton = (Button) findViewById(R.id.activity_login_login_button);

        //sets editText listeners
        mPasswordEditText.addTextChangedListener(textWatcher);
        mUserNameEditText.addTextChangedListener(textWatcher);

        //sets edit text listener for password text field. When Done button is pushed login action is performed
        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    UserCredentials credentials = new UserCredentials(mUserNameEditText.getText().toString(),
                            mPasswordEditText.getText().toString());
                    //authenticates and creates a lastFm session using credentials in username and password fields
                    authenticate(credentials);

                    return true;
                }
                return false;
            }
        });

        //checks for empty username and password textfields upon activity creation
        checkFieldsForEmptyValues();
    }

    //TextWatcher used for disabling login button if userID or password textfields are empty
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after) {
            checkFieldsForEmptyValues(); //calls method that checks for empty textfields
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    /**
     * navigates to Last.Fm sign up page in browser
     * called when Don't have Last.Fm account button is tapped
     */
    public void navToLastFmSignUpPage(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://secure.last.fm/join"));
        startActivity(browserIntent);
    }

    /**
     * method called when login button is tapped
     * starts asyncTask which gets session from lastFm and start home screen activity
     */
    public void login(View v) {
        UserCredentials credentials = new UserCredentials(mUserNameEditText.getText().toString(),
                mPasswordEditText.getText().toString());

        //authenticates and creates a lastFm session using credentials in username and password fields
        authenticate(credentials);
    }

    /**
     * checks both username and password textfields for empty values
     * Used to only enable login button when neither text fields are empty
     */
    private void checkFieldsForEmptyValues() {
        String passwordFieldContents = mPasswordEditText.getText().toString();
        String usernameFieldContents = mUserNameEditText.getText().toString();

        if (usernameFieldContents.length() == 0 && passwordFieldContents.length() == 0 ) {
            disableLoginButton();
        } else if (usernameFieldContents.length() != 0 && passwordFieldContents.length() == 0 ) {
            disableLoginButton();
        } else if (usernameFieldContents.length() == 0 && passwordFieldContents.length() != 0 ) {
            disableLoginButton();
        } else {
            enableLoginButton();
        }
    }

    /**
     * displays toast on bottom of screen
     * @param message String to be displayed on toast
     * @param toastLength enum to indicate how long toast should remain on screen, either Toast.LENGTH_SHORT, or Toast.LENGTH_LONG
     */
    private void displayToastMessage(String message, int toastLength) {
        Toast toast = Toast.makeText(this, message, toastLength);
        toast.show();
    }

    /**
     * disables Login button and sets its appearance to show user that it is disabled
     */
    private void disableLoginButton() {
        mLoginButton.setEnabled(false);
        mLoginButton.setTextColor(getResources().getColor(R.color.flat_button_disabled));
    }

    /**
     * enables Login button and sets its appearance to show user that it is enables
     */
    private void enableLoginButton() {
        mLoginButton.setEnabled(true);
        mLoginButton.setTextColor(getResources().getColor(R.color.primary_dark));
    }

    /**
     * authenticates user and gets a lastFm session
     * @param credentials User's lastFm credentials to be used to get a session
     */
    private void authenticate(UserCredentials credentials) {
        //hides keyboard upon tapping login button
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mPasswordEditText.getWindowToken(), 0);

        //if either username or password fields are blank dont do anything
        if (credentials.username.length() != 0 && credentials.password.length() != 0) {
            if (mNetworkUtil.isNetworkAvailable(this)) { //only calls backend if there is network available
                new GetSessionTask().execute(credentials); //executes getSessionTask
            } else { //no network connectivity
                displayToastMessage(getString(R.string.error_no_network_connectivity), Toast.LENGTH_LONG);
            }
        }
    }

    //async task that handles user authentication with last.fm
    private class GetSessionTask extends AsyncTask<UserCredentials, Void, Session> {
        private final String LAST_FM_API_KEY = getString(R.string.lastFm_api_key);
        private final String LAST_FM_SECRET = getString(R.string.lastFm_secret);
        private ProgressBar loginProgressBar = (ProgressBar) findViewById(R.id.activity_login_login_progressbar);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show login progress bar and hides login button
            mLoginButton.setVisibility(View.GONE);
            loginProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Session doInBackground(UserCredentials... userCredentials) {
            Caller.getInstance().setCache(null); //needed b/c of bug in library

            //returns null if there was any issue
            return Authenticator.getMobileSession(userCredentials[0].username, userCredentials[0].password,
                    LAST_FM_API_KEY, LAST_FM_SECRET);
        }

        protected void onPostExecute(Session session) {
            mLoginButton.setVisibility(View.VISIBLE);
            loginProgressBar.setVisibility(View.GONE);

            if (session == null) { //something went wrong
                Result response = Caller.getInstance().getLastResult();
                Log.w("session_getter_error", response.toString());
                if (response.getErrorCode() == 4) { //wrong username and/or password was provided by user
                    displayToastMessage(getString(R.string.error_incorrect_credentials), Toast.LENGTH_SHORT);
                } else { //something else went wrong so show user generic error message
                    displayToastMessage(getString(R.string.error_generic), Toast.LENGTH_LONG);
                }

            } else { //valid session was retrieved from last.Fm service
                //caches session to sharedPreferences so that it persists even when app is out of memory
                mSessionManager.cacheSession(session,getApplicationContext());

                //navigates to home activity
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

}