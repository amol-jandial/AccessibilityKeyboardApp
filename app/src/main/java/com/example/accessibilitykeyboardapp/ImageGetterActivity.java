package com.example.accessibilitykeyboardapp;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.inputmethod.InputConnection;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

@SuppressWarnings("ALL")
public class ImageGetterActivity extends AppCompatActivity {

    private static final int REQ_CODE = 1000;
    private TextRecognizer recognizer;
    private Uri image;
    private Context context;
    private ActivityResultLauncher<Intent> intentActivityResultLauncher;
    public static final int PICK_IMAGE = 1;
    private int starter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        starter = getIntent().getIntExtra("starter", -1);
        getImage();
    }


    private void getImage(){


        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, REQ_CODE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
                if(requestCode == REQ_CODE){
                    image =  data.getData();
                    Intent serviceIntent = new Intent(this, KeyboardIME.class);
                    serviceIntent.putExtra("imageURI", image);
                    serviceIntent.putExtra("starter", starter);
                    this.startService(serviceIntent);
//                    ClipData clip = ClipData.newPlainText("", image.toString());
//                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    clipboardManager.setPrimaryClip(clip);
//                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);

                    finish();
                }
            }
    }

}
