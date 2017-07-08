package com.example.manhngo.avsemap.Interaction;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.example.manhngo.avsemap.MapsActivity;
import com.example.manhngo.avsemap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Manh Ngo on 12/5/2016.
 */

public class SignedIn extends MapsActivity {

    private Context context;
    private Activity activityCompat;

    public SignedIn(Context context) {
        this.context = context;
    }

    public boolean wasSignedIn(final FirebaseAuth firebaseAuth){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            return false;
        }
        return true;
    }

    public void showAlerDialogWasSignedIn(final FirebaseAuth firebaseAuth){

        if(wasSignedIn(firebaseAuth) == false){
            showAlerDiaglogNotSigned();
        }else{
            showAlerDialogSigned(firebaseAuth);
        }
    }
    public void showAlerDiaglogNotSigned(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.notSigned);
        builder.setNeutralButton(R.string.login, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(context, LoginActivity.class));
            }
        });
        builder.setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(context, RegisterActivity.class));
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog object and return it
        builder.show();
    }


    public AlertDialog.Builder showAlerDialogSigned(final FirebaseAuth firebaseAuth){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.Signed +  firebaseAuth.getCurrentUser().getEmail() + " "
                + firebaseAuth.getCurrentUser().getDisplayName())
                .setPositiveButton(R.string.signout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        firebaseAuth.signOut();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder;
    }
}
