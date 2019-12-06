package com.drant.FastCartMain;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.drant.FastCartMain.NavActivity.userObject;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final int REQUEST_SIGNUP = 0;

//    DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    @BindView(R.id.input_user) EditText _userText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //binding to close keyboard on touch outside
        findViewById(R.id.touchFrame).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
                return true;
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d("LoginActivity", "Login Sequence Start");

        String email = _userText.getText().toString();
        String password = _passwordText.getText().toString();

        //Input Validation
        if (email.length() <3 || !email.contains(".com") || !email.contains("@") ) {
            _userText.setError("Please enter a valid email address");
            _userText.requestFocus();
            return;
        } else {_userText.setError(null);}

        if (password.length() < 5 || password.length() > 12) {
            _passwordText.setError("Password must be between 5 and 12 alphanumeric characters");
            _passwordText.requestFocus();
            return;
        } else {_passwordText.setError(null); }

        //Sequence Animations, hide keyboard
        _loginButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Firebase Auth and Token Retrieval
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, move to main activity
                        Log.d("LoginActivity", "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();
                        userObject.setUserId(uid);
                        Intent intent = new Intent(LoginActivity.this,NavActivity.class);
                        startActivity(intent);
                        Toast.makeText(getBaseContext(), "Login successful", Toast.LENGTH_SHORT).show();
                        _loginButton.setEnabled(true);
                        progressDialog.dismiss();
                    } else {
                        // Sign in fails
                        Log.w("LoginActivity", "signInWithEmail:failure", task.getException());
                        Toast.makeText(getBaseContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                        _loginButton.setEnabled(true);
                        progressDialog.dismiss();
                    }
                }
            });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here and complete startActivityForResult loop
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

}
