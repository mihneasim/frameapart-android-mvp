package frameapart.io.frameapart;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.os.Build;
import android.content.pm.PackageManager;

import android.content.Intent;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static int RESULT_LOAD_IMG = 1;
    private static final int REQUEST_WRITE_PERMISSION = 786;
    private Bitmap overlayBitmap;
    String imgDecodableString;


    // Camera related
    private static final int TAKE_PICTURE_REQUEST_B = 100;
    private static final int REQUEST_CAMERA_PERMISSION = 787;

    private ImageView mCapturedImageView;
    private Bitmap mCameraBitmap;
    private Button mSaveImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ;

        // camera
        mCapturedImageView = (ImageView) findViewById(R.id.capturedImgView);
        findViewById(R.id.capture_image_button).setOnClickListener(mCaptureImageButtonClickListener);

//        mSaveImageButton = (Button) findViewById(R.id.save_image_button);
//        mSaveImageButton.setOnClickListener(mSaveImageButtonClickListener);
//        mSaveImageButton.setEnabled(false);
    }

    public void requestGalleryPermission(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {
            loadImageFromGallery();
        }
    }

    public void requestCameraPermission(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            loadImageFromCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImageFromGallery();
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImageFromCamera();
        }
    }

    public void loadImageFromGallery() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE_REQUEST_B) {
            if (resultCode == RESULT_OK) {
                // Recycle the previous bitmap.
                if (mCameraBitmap != null) {
                    mCameraBitmap.recycle();
                    mCameraBitmap = null;
                }

                try {
                    mCameraBitmap = BitmapFactory.decodeStream(this.openFileInput("myImage"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (mCameraBitmap != null) {
                    mCapturedImageView.setImageBitmap(mCameraBitmap);
                }

            } else {
                mCameraBitmap = null;
//                mSaveImageButton.setEnabled(false);
            }
        } else

        {


            try {
                // When an Image is picked
                if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                        && null != data) {
                    // Get the Image from data

                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    ImageView imgView = (ImageView) findViewById(R.id.imgView);
                    // Set the Image in ImageView after decoding the String
                    overlayBitmap = BitmapFactory.decodeFile(imgDecodableString);
                    int h = 100;
                    int w = (int) (h * overlayBitmap.getWidth() / ((double) overlayBitmap.getHeight()));

                    overlayBitmap = Bitmap.createScaledBitmap(overlayBitmap, w, h, true);
                    overlayBitmap = ExifUtil.rotateBitmap(imgDecodableString, overlayBitmap);

                    imgView.setImageBitmap(overlayBitmap);

                } else {
                    Toast.makeText(this, "You haven't picked Image",
                            Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                        .show();
            }
        }

    }


    // Camera
    private OnClickListener mCaptureImageButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            requestCameraPermission(v);
        }
    };

//    private OnClickListener mSaveImageButtonClickListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            File saveFile = openFileForImage();
//            if (saveFile != null) {
//                saveImageToFile(saveFile);
//            } else {
//                Toast.makeText(MainActivity.this, "Unable to open file for saving image.",
//                        Toast.LENGTH_LONG).show();
//            }
//        }
//    };


    private void loadImageFromCamera() {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        if (overlayBitmap != null) {
            Bitmap smaller;
            int h = 100;
            int w = (int) (h * overlayBitmap.getWidth() / ((double) overlayBitmap.getHeight()));

            smaller = Bitmap.createScaledBitmap(overlayBitmap, w, h, true);
            intent.putExtra(CameraActivity.EXTRA_OVERLAY_DATA, smaller);
        }
        startActivityForResult(intent, TAKE_PICTURE_REQUEST_B);
    }

//    private File openFileForImage() {
//        File imageDirectory = null;
//        String storageState = Environment.getExternalStorageState();
//        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
//            imageDirectory = new File(
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//                    "com.oreillyschool.android2.camera");
//            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
//                imageDirectory = null;
//            } else {
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_hh_mm",
//                        Locale.getDefault());
//
//                return new File(imageDirectory.getPath() +
//                        File.separator + "image_" +
//                        dateFormat.format(new Date()) + ".png");
//            }
//        }
//        return null;
//    }
//
//    private void saveImageToFile(File file) {
//        if (mCameraBitmap != null) {
//            FileOutputStream outStream = null;
//            try {
//                outStream = new FileOutputStream(file);
//                if (!mCameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
//                    Toast.makeText(MainActivity.this, "Unable to save image to file.",
//                            Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "Saved image to: " + file.getPath(),
//                            Toast.LENGTH_LONG).show();
//                }
//                outStream.close();
//            } catch (Exception e) {
//                Toast.makeText(MainActivity.this, "Unable to save image to file.",
//                        Toast.LENGTH_LONG).show();
//            }
//        }
//    }


}