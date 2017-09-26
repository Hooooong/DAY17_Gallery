package com.hooooong.gallery;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Android Hong on 2017-09-26.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final int REQ_CODE  = 999;
    private String permissions[] ;

    public abstract void init();

    public BaseActivity(String[] permissions) {
        this.permissions = permissions;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkPermission();
        }else{
            init();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermission(){
        List<String> requires = new ArrayList<>();
        for(String permission : permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                requires.add(permission);
            }
        }

        if(requires.size() > 0){
            String perm[] = requires.toArray(new String[requires.size()]);
            requestPermissions(perm, REQ_CODE);
        }else{
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQ_CODE:
                boolean flag = true;
                for(int grantResult : grantResults){
                    if(grantResult != PackageManager.PERMISSION_GRANTED){
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    init();
                }else{
                    Toast.makeText(this, "권한 승인을 하지 않으면 APP 을 사용할 수 없습니다.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }
}
