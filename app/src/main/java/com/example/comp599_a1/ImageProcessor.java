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
import com.amplifyframework.storage.StorageItem;
import com.amplifyframework.storage.options.StorageDownloadFileOptions;
import com.amplifyframework.storage.options.StorageListOptions;
import com.amplifyframework.storage.options.StorageRemoveOptions;
import com.amplifyframework.storage.options.StorageUploadFileOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageProcessor extends AppCompatActivity {

    // Image processing
    private static final int RESULT_LOAD_IMAGE = 0;
    private static final int READ_REQUEST_CODE = 1;
    private Button loadBtn, saveLocallyBtn, filterBtn, saveToCloudBtn;
    private ImageView fileView;
    private TextView filename;
    private Spinner savedFiles;
    private final Map<String, StorageType> storedFiles = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //initialize page view

        //initialize views for image processing page
        fileView = findViewById(R.id.fileView);
        filename = findViewById(R.id.filename);
        savedFiles = findViewById(R.id.savedFiles);
        loadBtn = findViewById(R.id.loadBtn);
        saveLocallyBtn = findViewById(R.id.saveLocallyBtn);
        filterBtn = findViewById(R.id.filterBtn);
        saveToCloudBtn = findViewById(R.id.saveToCloudBtn);

        toggleButtonVisibility(Arrays.asList(filterBtn, saveLocallyBtn, saveToCloudBtn), false); //set filter and save buttons to disabled

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE}; //request permissions
        requestPermissions(permissions, READ_REQUEST_CODE);


        //onclick listener for load button. loads file into fileView
        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        //onclick listener for save locally button. save the file locally to internal storage
        saveLocallyBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                String name = filename.getText().toString();
                Bitmap bitmap = Bitmap.createBitmap(fileView.getWidth(), fileView.getHeight(), Bitmap.Config.RGB_565); //create bitmap of media
                Canvas canvas = new Canvas(bitmap);
                fileView.draw(canvas);
                storeInternally(name, bitmap); //function call to store internally
                updateStorage();
                Toast.makeText(getApplicationContext(), "File saved locally!", Toast.LENGTH_LONG).show();
            }
        });

        //onclick listener for apply filter button. simply adds a red tint over the file
        //this can easily be modified to support other types of file operations outside of the use case
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileView.setColorFilter(Color.RED, PorterDuff.Mode.LIGHTEN);
            }
        });

        //onclick listener for save to cloud button. Will write the contents of the file to amazon s3 using amplify
        saveToCloudBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                String name = filename.getText().toString();
                Bitmap bitmap = Bitmap.createBitmap(fileView.getWidth(), fileView.getHeight(), Bitmap.Config.RGB_565);  //create bitmap of media
                Canvas canvas = new Canvas(bitmap);
                fileView.draw(canvas);
                uploadFile(name, bitmap); //function call to store to the cloud
                updateStorage();
            }
        });

        //onclick listener for dropdown menu of files. When one is clicked, load the file into the fileView
        savedFiles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filename = (String) parent.getItemAtPosition(position);
                File file = new File(getFilesDir().getAbsolutePath() + "/" + filename);
                switch (storedFiles.get(filename)) {
                    case LOCAL: //if the file is saved locally
                        fileView.clearColorFilter();
                        fileView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                        ImageProcessor.this.filename.setText(file.getName());
                        toggleButtonVisibility(Arrays.asList(filterBtn, saveLocallyBtn, saveToCloudBtn), true);
                        break;
                    case CLOUD:
                        StorageDownloadFileOptions options = StorageDownloadFileOptions.builder()
                                .accessLevel(StorageAccessLevel.PRIVATE)
                                .targetIdentityId("ca-central-1:b8edbaaa-a63e-43ec-8954-f953f511729a")
                                .build();
                        Amplify.Storage.downloadFile(
                                file.getName(),
                                file,
                                options,
                                result -> {
                                        fileView.clearColorFilter();
                                        fileView.setImageBitmap(BitmapFactory.decodeFile(result.getFile().getAbsolutePath()));
                                        ImageProcessor.this.filename.setText(result.getFile().getName());
                                        toggleButtonVisibility(Arrays.asList(filterBtn, saveLocallyBtn, saveToCloudBtn), true);
                                        Toast.makeText(getApplicationContext(), "File Downloaded", Toast.LENGTH_LONG).show();
                                },
                                error -> {
                                    fileView.setImageBitmap(null);
                                    ImageProcessor.this.filename.setText("");
                                    toggleButtonVisibility(Arrays.asList(filterBtn, saveLocallyBtn, saveToCloudBtn), false);
                                    Toast.makeText(getApplicationContext(), "Unable to load cloud files. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println(error.getMessage());
                                }
                        );
                }
                updateStorage();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        initializeFiles();
        loadSavedFiles(null); //load previously stored photos into the dropdown menu

    }

    //import files from external storage
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
            String name = picturePath.substring(picturePath.lastIndexOf('/') + 1);
            if (storedFiles.containsKey(name)) { //to ensure unique filenames when loading from external storage
                filename.setText(name + "1");
            } else {
                filename.setText(name);
            }
            fileView.clearColorFilter();
            fileView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            toggleButtonVisibility(Arrays.asList(saveLocallyBtn, filterBtn, saveToCloudBtn), true);
        } else {
            toggleButtonVisibility(Arrays.asList(saveLocallyBtn, filterBtn, saveToCloudBtn), false);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Granted.
                } else {
                    //Denied.
                }
                break;
        }
    }

    //load filenames into dropdown menu and optionally select an option
    private void loadSavedFiles(String selected) {
        ArrayList<String> savedFileNames = new ArrayList<>();
        savedFileNames.addAll(storedFiles.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, savedFileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        savedFiles.setAdapter(adapter); //update dropdown
        if (selected != null) {
            savedFiles.setSelection(adapter.getPosition(selected)); //set selected value if provided (default is the first value)
        }
    }

    //helper method to toggle button visibility
    private void toggleButtonVisibility(List<Button> buttons, boolean visibility) {
        for (Button b : buttons) {
            b.setEnabled(visibility);
        }
    }

    //save the file to local storage and add it to the list of files
    private void storeInternally(String filename, Bitmap bitmap) {
        FileOutputStream outputStream = null;
        try {
            outputStream = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            storedFiles.put(filename, StorageType.LOCAL);
            loadSavedFiles(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //save the file to s3 and add it to the list of files
    private void uploadFile(String filename, Bitmap bitmap) {
        File file = new File(getFilesDir().getAbsolutePath() + filename);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            outputStream.flush();
        } catch (Exception exception) {
            Toast.makeText(getApplicationContext(), "An error occurred processing the file. Please try again", Toast.LENGTH_LONG).show();
        }

        StorageUploadFileOptions options =
                StorageUploadFileOptions.builder()
                        .accessLevel(StorageAccessLevel.PRIVATE)
                        .build();

        Amplify.Storage.uploadFile(
                filename,
                file,
                options,
                result -> {
                    Toast.makeText(getApplicationContext(), "File uploaded to Cloud!", Toast.LENGTH_LONG).show();
                    loadSavedFiles(filename);
                    file.delete();
                },
                error -> {Toast.makeText(getApplicationContext(), "Upload to cloud failed. Please try again", Toast.LENGTH_LONG).show(); file.delete();}
        );
        storedFiles.put(filename, StorageType.CLOUD);

    }

    public void initializeFiles() {
        StorageListOptions options = StorageListOptions.builder()
                .accessLevel(StorageAccessLevel.PRIVATE)
                .targetIdentityId("ca-central-1:b8edbaaa-a63e-43ec-8954-f953f511729a")
                .build();

            Amplify.Storage.list(
                    "",
                options,
                    result -> {
                        for (StorageItem item : result.getItems()) {
                            storedFiles.put(item.getKey(), StorageType.CLOUD);
                        }
                    },
                    error -> {
                        Toast.makeText(getApplicationContext(), "Unable to load cloud files. Please try again", Toast.LENGTH_LONG).show();
                        System.out.println("Error listing files from cloud: " + error.getMessage());
                    }
            );

        for (File file : getFilesDir().listFiles()) {
           storedFiles.put(file.getName(), StorageType.LOCAL);
        }
        loadSavedFiles(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateStorage(){
        storedFiles.forEach((key,value) -> {
            if(value == StorageType.CLOUD){
                new File(getFilesDir().getAbsolutePath() + '/' + key).delete();
            }else{
                StorageRemoveOptions options = StorageRemoveOptions.builder()
                        .accessLevel(StorageAccessLevel.PRIVATE)
                        .targetIdentityId("ca-central-1:b8edbaaa-a63e-43ec-8954-f953f511729a")
                        .build();

                Amplify.Storage.remove(
                        key,
                        //options,
                        result -> System.out.println("Successfully removed file from cloud storage"),
                        error -> System.out.println("Error removing file " + key + " from cloud storage: " + error.getMessage())
                );
            }
        });
    }
}
