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

import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
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
import java.util.Optional;

public class ImageProcessor extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 0;
    private static final int READ_REQUEST_CODE = 1;
    private Button loadBtn, saveLocallyBtn, filterBtn, saveToCloudBtn, deleteFileBtn, signOutBtn;
    private ImageView fileView;
    private TextView filename;
    private Spinner savedFiles;
    private final Map<String, StorageType> storedFiles = new HashMap<>();
    private String userId;


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
        signOutBtn = findViewById(R.id.signOutBtn);
        deleteFileBtn = findViewById(R.id.deleteFileBtn);
        userId = getIntent().getStringExtra("userId"); //access userId passed from login page (MainActivity)

        toggleButtonVisibility(Arrays.asList(filterBtn, saveLocallyBtn, saveToCloudBtn, deleteFileBtn), false); //set filter and save buttons to disabled

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE}; //request permissions
        requestPermissions(permissions, READ_REQUEST_CODE);


        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){ //check if permission was granted
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RESULT_LOAD_IMAGE);
                }else{
                    Toast.makeText(getApplicationContext(), "Permission has not been granted", Toast.LENGTH_LONG).show();
                }
            }
        });

        //on click listener for delete widget. will delete the file locally or from the cloud and update the dropdown menu
        deleteFileBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                String fileToDelete = filename.getText().toString();
                if(storedFiles.get(fileToDelete) == StorageType.CLOUD){ //file is stored on the cloud
                    deleteFromCloud(fileToDelete); //delete file from cloud
                }
                storedFiles.remove(fileToDelete); //remove from list of known files
                updateStorage(); //this will remove the file locally
                fileView.setImageBitmap(null); //clear ui image
                ImageProcessor.this.filename.setText(""); //clear ui filename
                toggleButtonVisibility(Arrays.asList(filterBtn, saveLocallyBtn, saveToCloudBtn, deleteFileBtn), false); //disable buttons
                loadSavedFiles(Optional.empty()); //update dropdown menu
            }
        });

        //on click listener for sign-out button. will sign out the user and return back to login page
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amplify.Auth.signOut(
                        () -> runOnUiThread(() -> {
                            Intent intent = new Intent(v.getContext(), MainActivity.class); //return back to login page
                            intent.putExtra("fromSignOut", true); //to ensure amplify doesn't reload
                            startActivity(intent);
                        }),
                        error -> runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Error signing out. Please try again", Toast.LENGTH_LONG).show();
                        })
                );
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
            }
        });

        //onclick listener for apply filter button. simply adds a red tint over the file
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
            }
        });

        savedFiles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filename = (String) parent.getItemAtPosition(position);
                File file = new File(getFilesDir().getAbsolutePath() + "/" + filename); //get local file or create if it does not exist
                switch (storedFiles.get(filename)) {
                    case LOCAL: //if the file is saved locally
                        fileView.clearColorFilter(); //clear the filter if it previously existed
                        fileView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath())); //update ui image
                        ImageProcessor.this.filename.setText(file.getName()); //update ui filename
                        toggleButtonVisibility(Arrays.asList(filterBtn, saveLocallyBtn, saveToCloudBtn, deleteFileBtn), true); //enable buttons
                        updateStorage(); //ensure that no cloud files are stored locally
                        break;
                    case CLOUD: //the file is stored on the cloud
                        //download config
                        StorageDownloadFileOptions options = StorageDownloadFileOptions.builder()
                                .accessLevel(StorageAccessLevel.PRIVATE)
                                .targetIdentityId(userId) //pass user id
                                .build();

                        Amplify.Storage.downloadFile(
                                file.getName(),
                                file,
                                options,
                                result -> { //download success. file is now downloaded locally for editing
                                    runOnUiThread(() -> {
                                        fileView.clearColorFilter(); //clear the filter if it previously existed
                                        fileView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath())); //update ui image
                                        ImageProcessor.this.filename.setText(file.getName()); //update ui filename
                                        toggleButtonVisibility(Arrays.asList(filterBtn, saveLocallyBtn, saveToCloudBtn, deleteFileBtn), true); //enable buttons
                                        Toast.makeText(getApplicationContext(), "File downloaded from cloud", Toast.LENGTH_LONG).show();
                                    });
                                },
                                error -> { //download failed
                                    runOnUiThread(() -> {
                                        fileView.setImageBitmap(null); //clear ui image
                                        ImageProcessor.this.filename.setText(""); //clear ui filename
                                        toggleButtonVisibility(Arrays.asList(filterBtn, saveLocallyBtn, saveToCloudBtn, deleteFileBtn), false); //disable buttons
                                        Toast.makeText(getApplicationContext(), "Unable to load cloud files. Please try again", Toast.LENGTH_LONG).show();
                                    });
                                }
                        );
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        initializeFiles(); //load dropdown menu
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
            String name = picturePath.substring(picturePath.lastIndexOf('/') + 1);
            if (storedFiles.containsKey(name)) { //to ensure unique filenames when loading from external storage. we will add a '1' to the filename
                filename.setText("1" + name);
            } else {
                filename.setText(name);
            }
            fileView.clearColorFilter(); //ensure there is no filter
            fileView.setImageBitmap(BitmapFactory.decodeFile(picturePath)); //set ui image
            toggleButtonVisibility(Arrays.asList(saveLocallyBtn, filterBtn, saveToCloudBtn, deleteFileBtn), true); //enable buttons
        } else {
            toggleButtonVisibility(Arrays.asList(saveLocallyBtn, filterBtn, saveToCloudBtn, deleteFileBtn), false); //disable buttons
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Read external storage permission not granted. You will not be able to load photos from the gallery", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadSavedFiles(Optional<String> selected) {
        ArrayList<String> savedFileNames = new ArrayList<>();
        savedFileNames.addAll(storedFiles.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, savedFileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        savedFiles.setAdapter(adapter); //update dropdown
        if (selected.isPresent()) {
            savedFiles.setSelection(adapter.getPosition(selected.get())); //set selected value if provided (default is the first value)
        }
    }

    //helper method to toggle button visibility
    private void toggleButtonVisibility(List<Button> buttons, boolean visibility) {
        for (Button b : buttons) {
            b.setEnabled(visibility);
        }
    }

    //save the file to local storage and add it to the list of files
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void storeInternally(String filename, Bitmap bitmap) {
        FileOutputStream outputStream = null;
        try {
            outputStream = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            storedFiles.put(filename, StorageType.LOCAL);
            updateStorage(); //ensure it is not saved to the cloud
            loadSavedFiles(Optional.of(filename)); //update dropdown menu
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //save the file to s3 and add it to the list of files
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void uploadFile(String filename, Bitmap bitmap) {
        File file = new File(getFilesDir().getAbsolutePath() + filename);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); //write image to file
            outputStream.close();
            outputStream.flush();
        } catch (Exception exception) {
            Toast.makeText(getApplicationContext(), "An error occurred processing the file. Please try again", Toast.LENGTH_LONG).show();
        }

        //upload config
        StorageUploadFileOptions options =
                StorageUploadFileOptions.builder()
                        .accessLevel(StorageAccessLevel.PRIVATE)
                        .build();

        //upload file to s3
        Amplify.Storage.uploadFile(
                filename,
                file,
                options,
                result -> {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "File uploaded to Cloud!", Toast.LENGTH_LONG).show();
                        storedFiles.put(filename, StorageType.CLOUD);
                        updateStorage(); //ensure it is not saved locally
                        loadSavedFiles(Optional.of(filename)); //update dropdown menu
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Upload to cloud failed. Please try again", Toast.LENGTH_LONG).show();
                    });
                }
        );
    }

    //list saved file names from internal and cloud storage
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void initializeFiles() {

        for (File file : getFilesDir().listFiles()) {
            storedFiles.put(file.getName(), StorageType.LOCAL); //add local filenames
        }

        //list config
        StorageListOptions options = StorageListOptions.builder()
                .accessLevel(StorageAccessLevel.PRIVATE)
                .targetIdentityId(userId)
                .build();

        //list private files from s3
        Amplify.Storage.list(
                "",
                options,
                result -> {
                    runOnUiThread(() -> {
                        for (StorageItem item : result.getItems()) {
                            storedFiles.put(item.getKey(), StorageType.CLOUD); //add cloud filenames
                        }
                        loadSavedFiles(Optional.empty()); //update dropdown menu once filenames are loaded
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        loadSavedFiles(Optional.empty()); //can still load local filenames
                        Toast.makeText(getApplicationContext(), "Unable to load cloud files. Please try again", Toast.LENGTH_LONG).show();
                    });
                }
        );
    }

    //ensures no duplicate files are stored. ie if file is saved on the cloud it is not saved locally. if the file is saved locally it is not saved on the cloud.
    //if the file is not recognized as being saved on either, it is deleted from local storage
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateStorage() {

        storedFiles.forEach((key, value) -> {
            if (value == StorageType.CLOUD) {
                new File(getFilesDir().getAbsolutePath() + '/' + key).delete(); //delete local files if they are saved on the cloud
            } else { //delete cloud files if they are saved locally
                deleteFromCloud(key);
            }
        });

        //delete unrecognized files
        for (File file : getFilesDir().listFiles()){
            if(!storedFiles.containsKey(file.getName())){
                file.delete();
            }
        }
    }

    public void deleteFromCloud(String filename){
        StorageRemoveOptions options = StorageRemoveOptions.builder()
                .accessLevel(StorageAccessLevel.PRIVATE)
                .targetIdentityId(userId)
                .build();

        Amplify.Storage.remove(
                filename,
                options,
                result -> {},
                error -> runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Error removing: " + filename + " from the cloud. Please try again", Toast.LENGTH_LONG).show();
                })
        );
    }
}
