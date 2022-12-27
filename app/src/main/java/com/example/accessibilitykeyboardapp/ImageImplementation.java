package com.example.accessibilitykeyboardapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputConnection;

import androidx.core.content.ContextCompat;

import java.io.Serializable;

public class ImageImplementation {


    private Context context;
    public static Uri image;
    private InputConnection inputConnection;


    ImageImplementation(Context context){
        this.context = context;

    }


    private void getImage(int starter){
        Intent intent = new Intent(context, ImageGetterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("starter", starter);
        context.startActivity(intent);

    }

    public void checkPermissions(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(context, ImagePermission.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void start(InputConnection inputConnection, int starter){
        this.inputConnection = inputConnection;
        getImage(starter);
    }

}
