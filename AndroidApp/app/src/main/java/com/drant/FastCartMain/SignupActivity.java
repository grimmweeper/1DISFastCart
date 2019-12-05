package com.drant.FastCartMain;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private static final String TAG = "SignupActivity";

    DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    @BindView(R.id.input_user) EditText _userText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;
    @BindView(R.id.input_name) EditText _nameText;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Initialize Firebase

        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        //binding to close keyboard on touch outside
        findViewById(R.id.touchFrame2).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup Sequence");

        final String name = _nameText.getText().toString();
        String email = _userText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() > 13) {
            _nameText.setError("Please enter valid name shorter than or equal to 12 characters");
            _nameText.requestFocus();
            return;
        } else { _userText.setError(null); }

        if (email.length() <3 || !email.contains(".com") || !email.contains("@")) {
            _userText.setError("Please enter a valid email address");
            _userText.requestFocus();
            return;
        } else { _userText.setError(null); }

        if (password.length() < 5 || password.length() > 12) {
            _passwordText.setError("Password must be between 5 and 12 alphanumeric characters");
            _passwordText.requestFocus();
            return;
        } else { _passwordText.setError(null); }

        if (!(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Passwords do not match");
            _reEnterPasswordText.requestFocus();
            return;
        } else { _reEnterPasswordText.setError(null); }


        //Sequence Animations,hide keyboard
        _signupButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Firebase Auth User Creation
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign up success, login
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                        }
                                    }
                                });

                        Log.d(TAG, "createUserWithEmail:success");

                        Intent intent = new Intent(SignupActivity.this,NavActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
//                        setResult(RESULT_OK, null);
                        String uid = user.getUid();
                        dbHandler.registeringNewUser(uid); // registers user in database
                        Toast.makeText(getBaseContext(), "Account created. Please enjoy your shopping!", Toast.LENGTH_SHORT).show();
                        _signupButton.setEnabled(true);
                        progressDialog.dismiss();
                    } else {
                        // Sign up fails
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getBaseContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        _signupButton.setEnabled(true);
                        progressDialog.dismiss();
                    }
                }
            });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, 0);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}