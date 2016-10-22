package example.com.temp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private static final int CAMERA_REQUEST = 121;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private Button buttonPhoto;
    private Button buttonRequest;
    private ImageView imageView;
    private EditText etUrl;
    private Button buttonSaveUrl;
    Bitmap photo = null;
    private URL uploadURL = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPhoto = (Button) findViewById(R.id.bt_photo);
        buttonRequest = (Button) findViewById(R.id.bt_request);
        buttonSaveUrl = (Button) findViewById(R.id.bt_url_save);
        etUrl = (EditText) findViewById(R.id.et_url);
        imageView = (ImageView) findViewById(R.id.iv_photo);

        buttonSaveUrl.setOnClickListener(this);

        checkForCameraPermission();

    }
    private void checkForCameraPermission() {
        try {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

            int hasStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this,
                    permissions[0]);
            int hasCameraPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                    permissions[1]);


            if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSIONS);
                }

            } else if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSIONS);
                }
            } else {
                buttonPhoto.setOnClickListener(this);
                buttonRequest.setOnClickListener(this);
                loadCamera();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                try {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    {
                        buttonPhoto.setOnClickListener(this);
                        buttonRequest.setOnClickListener(this);
                        loadCamera();
                    } else
                    {
                        checkForCameraPermission();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    checkForCameraPermission();
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_photo:
                loadCamera();
                break;
            case R.id.bt_request:
                if(uploadURL == null)
                {
                    Toast.makeText(MainActivity.this, "Enter a valid URL", Toast.LENGTH_SHORT).show();
                }else
                {
                    new uploadImage().execute("");
                }
                break;
            case R.id.bt_url_save:
                if(etUrl.getText().toString().equalsIgnoreCase(""))
                {
                    Toast.makeText(MainActivity.this, "Enter a valid URL", Toast.LENGTH_SHORT).show();
                }else
                {
                    try {
                        uploadURL = new URL(etUrl.getText().toString());
                        Toast.makeText(MainActivity.this, "URL updated", Toast.LENGTH_SHORT).show();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        etUrl.setText("");
                        Toast.makeText(MainActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void loadCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            try {

                if(data != null)
                {
                    if(data.getExtras() != null)
                    {
                        photo = (Bitmap) data.getExtras().get("data");

                    }else
                    {
                        photo = BitmapFactory.decodeFile(data.getData().getPath());
                    }
                }

                imageView.setImageBitmap(photo);

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private class uploadImage extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Starting Upload", Toast.LENGTH_SHORT).show();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                HttpURLConnection connection = null;
                connection = (HttpsURLConnection) uploadURL.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "image/jpeg");

                OutputStream output = connection.getOutputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 80, out);
                Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                decoded.compress(Bitmap.CompressFormat.JPEG, 100 , output);
                output.flush();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "Upload Successfull", Toast.LENGTH_SHORT).show();
        }
    }

}
