package com.example.appforblind;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by osp121 on 18/3/17.
 */

public class SessionManager {

    private SharedPreferences sharedPreferences;
    private Context context;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("PREF",0);
        editor = sharedPreferences.edit();
    }


    /******************session for add contact************************/

    //when user say name _______ eg: name manan
    //save name in local database
    public void addContactName(String name){
        editor.putString("name",name);
        editor.apply();
    }

    //when user say number _______ eg: number 9876543210
    //save number in local database
    public void addContactNumber(String number){
        editor.putString("number",number);
        editor.apply();
    }

    //to check whether name is present or not in database
    public boolean hasName(){
        return sharedPreferences.contains("name");
    }

    //to check whether number is present or not in database
    public boolean hasContact(){
        return sharedPreferences.contains("number");
    }

    //after saving, remove from database
    public void removeNameAndContact(){
        editor.remove("name");
        editor.remove("number");
        editor.apply();
    }

    public void addContactCount(int count){
        editor.putInt("count",count);
        editor.apply();
    }

    public int getCount(){
        return sharedPreferences.getInt("count",0);
    }



    public boolean hasPreviousSession(){
        return sharedPreferences.contains("session");
    }

    public void addSession(){
        editor.putBoolean("session",true);
        editor.apply();
    }

}
