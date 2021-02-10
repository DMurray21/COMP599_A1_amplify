package com.example.comp599_a1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.StorageAccessLevel;
import com.amplifyframework.storage.options.StorageUploadFileOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageProcessor extends AppCompatActivity {

    // Image processing
    private static final int RESULT_LOAD_IMAGE = 0;
    private static final int WRITE_REQUEST_CODE = 1;

    Button b_load, b_save, b_filter, b_export;
    ImageView imageView;
    TextView imageFilename;
    Spinner savedPhotos;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Layouts/buttons for photos
        imageView = findViewById(R.id.imageView);
        imageFilename = findViewById(R.id.imageFilename);
        savedPhotos = findViewById(R.id.savedPhotos);
        b_load = findViewById(R.id.b_load);
        b_save = findViewById(R.id.b_save);
        b_filter = findViewById(R.id.b_filter);
        b_export = findViewById(R.id.b_export);
        toggleButtonVisibility(Arrays.asList(b_filter,b_save),false);

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, WRITE_REQUEST_CODE);
        loadSavedImages(null);

        b_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { ;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                imageView.draw(canvas);
                storeInternally(imageFilename.getText().toString(), bitmap);
                loadSavedImages(imageFilename.getText().toString());
                Toast.makeText(getApplicationContext(), "Image saved to files!", Toast.LENGTH_LONG).show();
            }
        });

        b_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setColorFilter(Color.RED, PorterDuff.Mode.LIGHTEN);
            }
        });

        b_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                imageView.draw(canvas);
                uploadFile(imageFilename.getText().toString(), bitmap);
                Toast.makeText(getApplicationContext(), "Image saved to S3!", Toast.LENGTH_LONG).show();
            }
        });

        savedPhotos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filename = (String) parent.getItemAtPosition(position);
                File file = new File(getFilesDir().getAbsolutePath() + "/" + filename);
                if (file.exists()) {
                    imageView.clearColorFilter();
                    imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    imageFilename.setText(filename);
                    toggleButtonVisibility(Arrays.asList(b_filter,b_save),true);
                } else {
                    toggleButtonVisibility(Arrays.asList(b_filter,b_save),false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            imageFilename.setText(picturePath.substring(picturePath.lastIndexOf('/') + 1));

            imageView.clearColorFilter();
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            toggleButtonVisibility(Arrays.asList(b_save,b_filter),true);
        } else {
            toggleButtonVisibility(Arrays.asList(b_save,b_filter),false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Granted.
                } else {
                    //Denied.
                }
                break;
        }
    }


    private void loadSavedImages(String selected) {
        ArrayList<String> savedImageFilenames = new ArrayList<String>();
        for (File file : getFilesDir().listFiles()) {
            savedImageFilenames.add(file.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, savedImageFilenames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        savedPhotos.setAdapter(adapter);
        if (selected != null) {
            savedPhotos.setSelection(adapter.getPosition(selected));
        }
    }
    private void toggleButtonVisibility(List<Button> buttons, boolean visibility) {
        for (Button b: buttons) {
            b.setEnabled(visibility);
        }
    }

    private void storeInternally(String filename, Bitmap bitmap) {
        FileOutputStream outputStream = null;
        try {
            outputStream = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO move to new page
    private void uploadFile(String filename, Bitmap bitmap) {
        File exampleFile = new File(getApplicationContext().getFilesDir(), filename);

        try {
            FileOutputStream outputStream = new FileOutputStream(exampleFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        } catch (Exception exception) {
            Log.e("MyAmplifyApp", "Upload failed", exception);
        }


        StorageUploadFileOptions options =
                StorageUploadFileOptions.builder()
                        .accessLevel(StorageAccessLevel.PRIVATE)
                        .build();

        Amplify.Storage.uploadFile(
                filename,
                exampleFile,
                options,
                result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + "ExampleKey"),
                error -> Log.e("MyAmplifyApp", "Upload failed", error)
        );
    }
}
