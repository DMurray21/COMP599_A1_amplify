package com.example.comp599_a1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.AuthException;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.auth.result.AuthSignInResult;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.Consumer;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.Image;
import com.amplifyframework.storage.StorageAccessLevel;
import com.amplifyframework.storage.options.StorageUploadFileOptions;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    private CheckBox isFirstTimeUser;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText emailInput;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); //initialize view

        //initialize views
        isFirstTimeUser = findViewById(R.id.isFirstTimeUser);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        emailInput = findViewById(R.id.emailInput);
        loginBtn = findViewById(R.id.loginBtn);

        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin()); //initialize plugins
            Amplify.addPlugin(new AWSS3StoragePlugin());
            Amplify.configure(getApplicationContext()); //initialize amplify
        } catch (AmplifyException e) {
            Toast.makeText(this, "Our application has encountered an unexpected error. Please try again later", Toast.LENGTH_LONG).show();
            loginBtn.setEnabled(false);
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
                authenticate(username, password, email);
            }
        });
    }

    //authenticate user with amplify
    private void authenticate(String username, String password, String email) {
        if (username.isEmpty() || password.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Please provide credentials in order to login", Toast.LENGTH_LONG).show());
        } else {
            Amplify.Auth.signIn(username, password, new Consumer<AuthSignInResult>() {
                @Override
                public void accept(@NonNull AuthSignInResult result) {
                    switch (result.getNextStep().getSignInStep()) {
                        case DONE: //successful login for returning user
                            runOnUiThread(() -> {
                                Toast.makeText(getApplicationContext(), "Welcome back, " + username, Toast.LENGTH_LONG).show();
                                onLoginSuccess();
                                uploadFile(); //TODO remove this call once function is moved
                            });
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
                                                runOnUiThread(() -> {
                                                    Toast.makeText(getApplicationContext(), "Welcome, " + username, Toast.LENGTH_LONG).show();
                                                    onLoginSuccess();
                                                    uploadFile(); //TODO remove this call once function is moved
                                                });
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

    private void onLoginSuccess() {
        //TODO implement redirect to application main page
    }

    //TODO move to new page
    private void uploadFile() {
        File exampleFile = new File(getApplicationContext().getFilesDir(), "ExampleKey");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(exampleFile));
            writer.append("Example file contents");
            writer.close();
        } catch (Exception exception) {
            Log.e("MyAmplifyApp", "Upload failed", exception);
        }


        StorageUploadFileOptions options =
                StorageUploadFileOptions.builder()
                        .accessLevel(StorageAccessLevel.PRIVATE)
                        .build();

        Amplify.Storage.uploadFile(
                "khalilv_first_file",
                exampleFile,
                options,
                result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + "ExampleKey"),
                error -> Log.e("MyAmplifyApp", "Upload failed", error)
        );
    }
}