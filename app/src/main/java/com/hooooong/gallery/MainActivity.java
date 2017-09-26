package com.hooooong.gallery;

import android.Manifest;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

/**
 * 권한설정
 * <p>
 * - Manifest.permission.CAMERA
 * - Manifest.permission.WRITE_EXTERNAL_STORAGE
 */
public class MainActivity extends BaseActivity {

    private static final int REQ_GALLERY = 111;
    private static final int REQ_CAMERA = 222;

    private ImageView imageView;

    public MainActivity() {
        super(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    @Override
    public void init() {
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.gallery);

        // Camera Start
    }

    public void onGallery(View view) {
        // Table 이름 : MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_GALLERY);
    }

    // 저장된 File 의 경로를 가지고 있는 Content Uri
    Uri fileUri = null;

    public void onCamera(View view) {
        // 카메라 앱 띄워서 결과 이미지 저장하기
        // 1. Intent 생성
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 2. 호환성 처리 버전
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // LOLLIPOP 이상일 경우
            // 3. 실제 File 이 저장되는 File 객체 <- 빈 파일을 하나 생성한다.
            // Source 코드 상에서는 File 객체를 생성하면 Uri 가 자동으로 생성이 되지 않는다.

            // 3-1. 실제 파일이 저장되는 곳에 권한이 부여되어 있어야 한다.
            //      LOLLIPOP 부터는 File Provider 를 Manifest.xml 에 설정해야 한다.
            try {
                File photoFile = createFile();

                // 갤러리에서 나오지 않을때
                refreshMedia(photoFile);

                // BuildConfig.APPLICATION_ID : 는 Gradle 과 Manifest 에 설정되있는 Application Id 이다.
                fileUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", // 권한 설정
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, REQ_CAMERA);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            // LOLLIPOP 미만일 경우
            startActivityForResult(intent, REQ_CAMERA);
        }
    }

    // Media File 갱신
    private void refreshMedia(File photoFile) {
        MediaScannerConnection.scanFile(this,
                new String[]{photoFile.getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }

    // 이미지를 저장하기 위해서 쓰기 권한이 있는 빈 파일을 생성해두는 함수
    private File createFile() throws IOException {
        // 임시 파일명 생성
        String tempFileName = "Temp_" + System.currentTimeMillis();
        // 임시 파일 저장용 디렉토리 형성
        //Environment.getExternalStorageDirectory() 에 getAbsolutePath() 를 꼭 써줘야 한다.
        File tempDIR = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CameraN/");
        // 생성 체크
        if (!tempDIR.exists()) {
            tempDIR.mkdirs();
        }

        // 실제 임시파일을 생성
        // createTempFIle 은 꼭 App 이 종료될 때 삭제시켜줘야 한다.
        File tempFile = File.createTempFile(
                tempFileName,   // 파일명
                ".jpg",         // 확장자
                tempDIR);       // 경로

        //File tempFile = new File("/"+tempDIR +"/"+ tempFileName+ ".jpg");
        //tempFile.createNewFile();

        return tempFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri;
        switch (requestCode) {
            case REQ_GALLERY:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        imageUri = data.getData();
                        imageView.setImageURI(imageUri);
                    }
                }
                break;
            case REQ_CAMERA:
                if (resultCode == RESULT_OK) {
                    // 롤리팝 미만 버전 체크
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imageView.setImageURI(fileUri);
                    } else {
                        if (data != null) {
                            imageUri = data.getData();
                            imageView.setImageURI(imageUri);
                        }
                    }
                }
                break;
        }
    }
}
