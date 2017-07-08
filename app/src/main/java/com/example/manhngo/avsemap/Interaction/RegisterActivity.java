package com.example.manhngo.avsemap.Interaction;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by Manh Ngo on 12/3/2016.
 */

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private EditText edtUserName;
    private EditText edtEmailFiled;
    private EditText edtPassword;
    private Button btnRegister;
    private Button btnBackToMap;


    // [START declare_auth]
    private FirebaseAuth firebaseAuth;
    // [END declare_auth]



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        // Hide action bar
        //this.getActionBar().hide();

        //Initialize

        edtUserName = (EditText) findViewById(R.id.edt_UserFirstNameRegister);
        edtEmailFiled = (EditText) findViewById(R.id.edt_EmailRegister);
        edtPassword = (EditText) findViewById(R.id.edt_PassWordRegister);

        btnRegister = (Button) findViewById(R.id.btn_RegisterButton);
        btnBackToMap = (Button) findViewById(R.id.btn_BackToMap);

        btnBackToMap.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        // [START initialize_auth]
        firebaseAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]

    }

    // [START on_start_add_listener]
    @Override
    protected void onStart() {
        super.onStart();
        //firebaseAuth.addAuthStateListener();
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    protected void onStop() {
        super.onStop();
    }
    // [END on_stop_remove_listener]

    private void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        // [START create_user_with_email]
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                            hideProgressDialog();

                        }else {
                            createUserInformation();
                            firebaseAuth.signOut();
                            firebaseAuth.signInWithEmailAndPassword(email, password);
                            hideProgressDialog();
                            startActivity(new Intent(RegisterActivity.this, MapsActivity.class));
                            finish();
                        }

                        // [START_EXCLUDE]

                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_w
    }




    private boolean validateForm(){
        boolean valid = true;
        String email = edtEmailFiled.getText().toString();
        if(TextUtils.isEmpty(email)){
            edtEmailFiled.setError("Required.");
            valid = false;
        }else{
            edtEmailFiled.setError(null);
        }

        String password = edtPassword.getText().toString();
        if(TextUtils.isEmpty(password)){
            edtPassword.setError("Required.");
            valid = false;
        }else{
            edtPassword.setError(null);
        }
        return valid;
    }

    private void createUserInformation() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(edtUserName.getText().toString())
                .build();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }else{

                        }
                    }
                });
    }
    @Override
    public void onClick(View v) {
        if(v == btnRegister){
            String userEmail = edtEmailFiled.getText().toString();
            String userPassword = edtPassword.getText().toString();

            createAccount(userEmail,userPassword);

        }else if(v == btnBackToMap){
            startActivity(new Intent(RegisterActivity.this, MapsActivity.class));
            finish();
        }


    }

    public void onClickLogin(View view){
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
}
