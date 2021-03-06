package com.example.comp599_a1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.AuthException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.auth.result.AuthSignInResult;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.Consumer;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;



public class MainActivity extends AppCompatActivity {

    private CheckBox isFirstTimeUser;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText emailInput;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); //initialize page view

        //initialize views for login
        isFirstTimeUser = findViewById(R.id.isFirstTimeUser);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        emailInput = findViewById(R.id.emailInput);
        loginBtn = findViewById(R.id.loginBtn);

        if(!getIntent().getBooleanExtra("fromSignOut", false)){ //ensure amplify does not reconfigure if user just signed out
            try {
                Amplify.addPlugin(new AWSCognitoAuthPlugin()); //add auth plugin
                Amplify.addPlugin(new AWSS3StoragePlugin()); //add storage plugin
                Amplify.configure(getApplicationContext()); //configure amplify
            } catch (AmplifyException e) {
                e.printStackTrace();
                Toast.makeText(this, "Our application has encountered an unexpected error. Please try again later", Toast.LENGTH_LONG).show();
                loginBtn.setEnabled(false);
            }
        }

        //toggle email input visibility
        isFirstTimeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFirstTimeUser.isChecked()) {
                    emailInput.setVisibility(View.VISIBLE);
                } else {
                    emailInput.setVisibility(View.INVISIBLE);
                }
            }
        });

        //initialize login process
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString(); //get user input values
                String password = passwordInput.getText().toString();
                String email = emailInput.getText().toString();
                authenticate(username, password, email, v);
            }
        });
    }

    //authenticate user with amplify
    private void authenticate(String username, String password, String email, View v) {
        if (username.isEmpty() || password.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Please provide credentials in order to login", Toast.LENGTH_LONG).show());
        } else {
            Amplify.Auth.signIn(username, password, new Consumer<AuthSignInResult>() {
                @Override
                public void accept(@NonNull AuthSignInResult result) {
                    switch (result.getNextStep().getSignInStep()) {
                        case DONE: //successful login for returning user
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Welcome back, " + username, Toast.LENGTH_LONG).show());
                            onLoginSuccess(v);
                            break;
                        case CONFIRM_SIGN_IN_WITH_NEW_PASSWORD: //successful login for new user
                            if (email.isEmpty()) { //new users need to provide email to verify
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Are you a new user? Please provide your email if you are logging in for the first time", Toast.LENGTH_LONG).show());
                            } else {
                                Amplify.Auth.confirmSignIn(email, new Consumer<AuthSignInResult>() {
                                    @Override
                                    public void accept(@NonNull AuthSignInResult result) {
                                        switch (result.getNextStep().getSignInStep()) {
                                            case DONE: //successful confirmation of email
                                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Welcome, " + username, Toast.LENGTH_LONG).show());
                                                onLoginSuccess(v);
                                                break;
                                            default:
                                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Unsupported sign-in confirmation: " + result.getNextStep().getSignInStep() + " Please try again later", Toast.LENGTH_LONG).show());
                                        }
                                    }
                                }, new Consumer<AuthException>() {
                                    @Override
                                    public void accept(@NonNull AuthException e) {
                                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Authentication Failed. Please try again", Toast.LENGTH_LONG).show());
                                    }
                                });
                            }
                            break;
                        default:
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Unsupported sign-in confirmation: " + result.getNextStep().getSignInStep() + " Please try again later", Toast.LENGTH_LONG).show());
                    }
                }
            }, new Consumer<AuthException>() {
                @Override
                public void accept(@NonNull AuthException e) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Authentication failed. Please try again", Toast.LENGTH_LONG).show());
                }
            });
        }
    }

    private void onLoginSuccess(View v) {
        Amplify.Auth.fetchAuthSession(
                result -> {
                    AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) result;
                    switch(cognitoAuthSession.getIdentityId().getType()) {
                        case SUCCESS:
                            runOnUiThread(() -> {
                                Intent intent = new Intent(v.getContext(), ImageProcessor.class);
                                intent.putExtra("userId", cognitoAuthSession.getIdentityId().getValue()); //pass user Id for private file access
                                startActivity(intent);
                            });
                            break;
                        case FAILURE:
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to access user credentials. Please try again later", Toast.LENGTH_LONG).show());
                    }
                },
                error -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "An exception occurred accessing credentials. Please try again later", Toast.LENGTH_LONG).show())
        );
    }

}