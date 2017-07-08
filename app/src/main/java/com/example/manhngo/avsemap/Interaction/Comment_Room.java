package com.example.manhngo.avsemap.Interaction;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.manhngo.avsemap.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Manh Ngo on 12/3/2016.
 */

public class Comment_Room extends Activity implements View.OnClickListener {

    private Button btn_Sending;
    private Button btn_BackToMap;
    private TextView tev_Title;
    private TextView tev_Contain;
    private EditText edt_InputMsg;
    private ScrollView scrollview;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private String user_name, room_name;

    private DatabaseReference root;

    private String temp_key;

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Comment_Room", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Comment_Room", "onStop");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);


        btn_Sending = (Button) findViewById(R.id.btn_Send);
        btn_BackToMap = (Button) findViewById(R.id.btn_BackToMap);
        tev_Contain = (TextView) findViewById(R.id.tev_Contain);
        tev_Title = (TextView) findViewById(R.id.tev_title);
        scrollview = ((ScrollView) findViewById(R.id.scrollView));

        edt_InputMsg = (EditText) findViewById(R.id.msg_input);
        btn_Sending.getBackground().setAlpha(100);
        btn_BackToMap.getBackground().setAlpha(100);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.8), (int) (height*.6));

        user_name = getIntent().getExtras().get("user_name").toString();
        room_name = getIntent().getExtras().get("room_name").toString();

        setTitle("Room - " + room_name);

        root = FirebaseDatabase.getInstance().getReference().child(room_name);
        DatabaseReference rootType = root.child("Type");

        btn_Sending.setOnClickListener(this);
        btn_BackToMap.setOnClickListener(this);

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private String chat_msg, chat_user_name, title;
    private Boolean aBoolean = false;
    private void append_chat_conversation(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getKey().equals("Type")){
            Log.e("AAAA", dataSnapshot.getValue().toString());
            tev_Title.setText(dataSnapshot.getValue(String.class));
        }

        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()){
            //chat_msg = (String) ((DataSnapshot)i.next()).getValue();
            //chat_user_name = (String) ((DataSnapshot)i.next()).getValue();
            chat_msg = getColoredSpanned((String) ((DataSnapshot)i.next()).getValue(), "#211b1b" );
            chat_user_name = getColoredSpannedBold((String) ((DataSnapshot)i.next()).getValue(), "#448AFF");

            tev_Contain.append(Html.fromHtml(chat_user_name + ": "  + chat_msg+  "<br>"));
            scrollview.post(new Runnable() {
                @Override
                public void run() {
                    scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_Send){
            temp_key = root.push().getKey();
            DatabaseReference message_root = root.child(temp_key);
            Map<String, Object> cmMap2 = new HashMap<>();
            cmMap2.put("name", user_name);
            cmMap2.put("msg", edt_InputMsg.getText().toString());
            edt_InputMsg.setText("");

            message_root.updateChildren(cmMap2);
        } else if(view.getId() == R.id.btn_BackToMap){
           finish();
        }
    }

    private String getColoredSpannedBold(String text, String color) {
        String input = "<b><font color=" + color + ">" + text + "</font></b>";
        return input;
    }
    private String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }
}
