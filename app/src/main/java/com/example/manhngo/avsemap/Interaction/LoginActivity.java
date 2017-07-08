package com.example.manhngo.avsemap.Interaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.manhngo.avsemap.MapsActivity;
import com.example.manhngo.avsemap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A activity_login1 screen that offers activity_login1 via email/password.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText mEmailField;
    private EditText mPasswordField;

    private Button btnLogin;
    private Button btnBackToMap;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(this);

        // [START initialize_auth]
        firebaseAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        //Views

//        if(firebaseAuth.getCurrentUser() != null){
//            //close this activity
//            finish();
//            //opening profile activity
//            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
//        }
        mEmailField = (EditText) findViewById(R.id.edt_EmailLogin);
        mPasswordField = (EditText) findViewById(R.id.edt_PasswordLogin);

        //Buttons
        btnLogin = (Button) findViewById(R.id.btn_Login);
        btnBackToMap = (Button) findViewById(R.id.btn_BackToMap);

        btnBackToMap.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }else{

                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void signIn(String email, String password){
        Log.d(TAG, "signIn:"+email);
        if(!validateForm()){
            return;
        }

        showProgressDialog();

        // [START   sign_in_with_email]
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            hideProgressDialog();
                        }else{
                            Toast.makeText(LoginActivity.this, R.string.auth_succeed,
                                    Toast.LENGTH_SHORT).show();
                            hideProgressDialog();
                            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                            finish();
                        }

                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }


    @Override
    public void onClick(View v) {
        if(v == btnLogin){
            String email = mEmailField.getText().toString();
            String password = mPasswordField.getText().toString();
            signIn(email,password);
        }
        else if(v == btnBackToMap){

            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
            finish();
        }

    }

    public void onClickRegister(View view){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }
}