package com.example.manhngo.avsemap;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Manh Ngo on 12/5/2016.
 */

public class OnCircle extends AppCompatActivity {

    private Location location;
    private GoogleMap mMap;
    private Context context;
    private Circle circle;

    public Circle getCircle() {
        return circle;
    }

    public OnCircle(GoogleMap mMap, Context context) {
        this.mMap = mMap;
        this.context = context;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        location = new Location("");
    }

    public Circle drawCircle(LatLng latLng, String type){
        Integer color;
        switch (type){
            case TypeStuck.ACCIDENT:
                color = ContextCompat.getColor(context,R.color.accident);
                break;
            case TypeStuck.STUCK:
                color = ContextCompat.getColor(context,R.color.stuck);
                break;
            case TypeStuck.CONSTRUCT:
                color = ContextCompat.getColor(context,R.color.construct);
                break;
            case TypeStuck.FLOODING:
                color = ContextCompat.getColor(context,R.color.flooding);
                break;
            case TypeStuck.POLICE:
                color = ContextCompat.getColor(context,R.color.police);
                break;
            default:
                color = ContextCompat.getColor(context,R.color.red_normal);
        }

        return mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(200)
                .strokeWidth(0)
                .fillColor(color)
                .clickable(true));
    }

}
