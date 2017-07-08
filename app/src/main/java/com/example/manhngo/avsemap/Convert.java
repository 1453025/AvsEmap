package com.example.manhngo.avsemap;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Manh Ngo on 12/5/2016.
 */

public class Convert {

    Context context;




    public void setContext(Context context) {
        this.context = context;
    }



    public Address getAddressFromLatLng(double latitude, double longitue, Context context){
        Address _address = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(latitude, longitue, 1);
            _address = addresses.get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return _address;
    }
    public String getAddressStringFromLatLng(double latitude, double longitue, Context context){
        String strAddress = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(latitude, longitue, 1);
            if(addresses != null){
                int i = 0;
                do{
                    strAddress = strAddress + addresses.get(0).getAddressLine(i);
                    i++;
                    if(i == addresses.get(0).getMaxAddressLineIndex()){
                        break;
                    }
                    strAddress = strAddress + ", ";
                }while (i < addresses.get(0).getMaxAddressLineIndex());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strAddress;
    }
    public Address getAddressFromString(String strAddress, Context context){
        List<Address> addressesSearch = null;
        Geocoder geocoder = new Geocoder(context);

        try{;
            addressesSearch = geocoder.getFromLocationName(strAddress,1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Address address = addressesSearch.get(0);

        return address;
    }
    public static String convertLatTngTo(LatLng latLng){
        return String.valueOf(latLng.latitude +"-"+latLng.longitude).toString().replace(".", "a");
    }

    public static LatLng convertStringTo(String string){
        String mstring = string.replace("a", ".");
        String[] latlong =  mstring.split("-");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        LatLng location = new LatLng(latitude, longitude);
        return location;
    }
}
