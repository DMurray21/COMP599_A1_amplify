# Storing Sensitive User Data 

As a mobile app developer, you may need to collect and store sensitive user data in your application. You may need to track a user’s diet and exercise routine for a health and wellness application, store photos taken by a user for a pdf scanner application, or keep a detailed account of a user’s schedule for a day planner or calendar application.

The focus of this article will revolve around two ways for an application to store data, the benefits and risks of each one, as well as tips to mitigate security and privacy vulnerabilities. The use case of the article will be a simple Image Processor application where images can be uploaded, downloaded, edited and saved. However, much information regarding the storage of sensitive data extends beyond the scope of the example application. 

The Image Processor application has fairly basic functionality. It ensures the user is authenticated before being given access to the application. It allows users to load images from their camera roll, apply a filter on the image, and save it to local storage or to an external cloud service. A user can also view all the different photos they have saved and have the option to delete them.

## User Requirements for Data Collection and Storage

Before diving into how the application will save to local storage or to the cloud, it is an important exercise to understand the different expectations a user may have when you collect and store their data, as well as the relevant risks and harms.

### What behaviour would a user expect from the application?

- The application must only have access to features on the device that are essential for full application functionality. In our case, the Image Processor must have read access to a user’s camera roll to select photos to filter and save.
- The application must ask for explicit consent from the user to access the necessary features on the phone. Without given consent, the application should not be able to perform the corresponding functionality.
- Security mechanisms should be in place to protect the user’s images from unauthorized access.
- Data quality shouldn’t be affected by storing the data. In our case, the images stored by the user shouldn’t be distorted when they are saved to or retrieved from storage.
- Implementing different security and privacy measures should not hinder the user’s experience. The user should be able to navigate the application without feeling hindered by the different measures put in place by the developer to maintain privacy and security standards.

### Privacy Risks and Harmful Data Actions to be Mitigated

As an application developer you have the responsibility to meet the user’s expectations to balance functionality, privacy and security. You also have the responsibility to follow standards to mitigate the different risks the user is exposed to such as:

- Insecurity: If insufficient security mechanisms are in place, photos may be stolen or disseminated without consent. Different authentication and access control measures must be in place to mitigate these risks. Mechanisms implemented by the image processor include authentication, access control, and encryption. They will be covered more in depth [later](https://github.com/DMurray21/COMP599_A1_amplify/blob/main/Article.md#privacy-controls) on in the article.
- Exclusion: Users must be continually informed with changes to privacy policies and to the data collected by the application. They also must be able to access the photos stored on the application at all times.
- Intrusion: The user’s images must not be accessed by unauthorized parties.
- Distortion: The user’s images must not be modified without consent.

Failing to mitigate these different risks leaves the user open to adverse experiences while using the application. We highlight some of the main ones below: 

- The user may experience a loss of autonomy characterized by having to give up using an application that is part of their routine. In the present context, a third party intrusion into the image processor may make a user feel uncomfortable about taking pictures and saving them to their gallery.
- The user may lose trust in the application.
- The user may be embarrassed by the photos exposed by a data breach and have their personal lives stigmatized.

## Storage Options 

Before continuing to the code demo, it is important to note the advantages and in turn, disadvantages of storing images locally versus through a cloud service. The following section will highlight some of the relevant points.

### Local storage

Local storage means the images are stored on physical devices such as hard drives or solid-state drives (SSD). Specific to the use case of our application, they are stored within the [android file system](https://developer.android.com/training/data-storage) on the mobile device. 

The primary benefits of using local storage are:

- Increased if not complete control of how the data is stored, how it is backed up, who can access it, and the security and privacy mechanisms protecting it.
- Faster retrieval and access to the data.

The primary downsides of using local storage are:

- The developer will have to implement their own security and privacy controls necessary to protect the data. This can be very costly and error prone if not done with care.
- It is always a risk storing data on physical devices. If the device is compromised or lost, this can lead to [unauthorized access](https://www.bbc.com/news/business-45785227) to data.

As stated in the Android file system document listed above there are four options to choose from when storing data, depending on the use case. We include an image of the section here for convenience.

![Data and file storage overview](https://github.com/DMurray21/COMP599_A1_amplify/blob/main/Images/data_storage_overview.png)

We will look more into app-specific storage as it is relevant to our use case.

### Internal vs External Storage

Within app-specific storage there are two subclasses, internal storage, and external storage. Descriptions of each are given from the android documentation below.

![internal_storage_description](https://github.com/DMurray21/COMP599_A1_amplify/blob/main/Images/internal_storage_desc.png)

It is clear to see when dealing with sensitive information it is best to use internal rather than external storage. If images are stored on external storage, they are still kept within the scope of the application, but other applications granted the MANAGE_EXTERNAL_STORAGE permission (such as the [Android File application](https://play.google.com/store/apps/details?id=com.google.android.apps.nbu.files&hl=en_CA&gl=US)) will still be able to read and write to them. It is important to notice this distinction when using app-specific storage. More on this can be found [here](https://www.journaldev.com/9383/android-internal-storage-example-tutorial) 

### Cloud Storage

Cloud storage means the files are stored on the internet typically through a third-party cloud computing provider. Specific to the use case of our application, our files are stored through the [Amazon Simple Storage Service (S3)](https://aws.amazon.com/s3/).

The primary benefits of using cloud storage are: 

- Much more scalable and cost-effective than local storage. 
- Easy to implement and often have well documented API’s.
- The risk of storing data on physical devices as with local storage is mitigated.

The primary downsides of using cloud storage are:

- Less control of how the data is stored, who has access to it, and the security mechanisms protecting it. You must comply with the cloud service provider’s privacy policy. 
- You are dependent on the security system of the cloud. Although most of them are quite well established and trusted, there have been [incidents](https://www.theguardian.com/technology/2016/aug/31/dropbox-hack-passwords-68m-data-breach) where cloud systems have been compromised. 
- Slower retrieval and access to data. However, as edge computing becomes more prevalent this is becoming less of a downside. 

A good practice and something to consider when storing data through a cloud service is to implement security and privacy controls locally on top of the controls provided by the cloud service.

### App Demo

Here is a [working demonstration](https://drive.google.com/file/d/1yPD1226fkZpIvA8CjoGJoAxyJ4rReKVT/view?usp=sharing) of our application.

**Notes on demo and relevant documentation**

In this demo we show in order:
1. Logging into the application for the first time.
2. Accepting the required permissions.
3. Loading an image from external storage.
4. Applying a filter.
5. Saving the image locally and confirming that it is stored successfully.
6. Loading another image from external storage .
7. Saving the image to cloud storage and confirming that it is stored successfully.
8. Deleting files from both local storage and cloud storage and confirming that they are deleted.
9. Signing out of the application.
10. Trying to sign-in to the application with incorrect credentials.

To integrate Amazon S3 with our application we used the AWS [Amplify Framework](https://aws.amazon.com/amplify/). We followed this [documentation](https://docs.amplify.aws/lib/project-setup/prereq/q/platform/android) to install the Amplify CLI and other prerequisites, and this [documentation](https://docs.amplify.aws/lib/project-setup/create-application/q/platform/android#option-2-follow-the-instructions) to initialize our application with Amplify. Once that was complete, we added the Amplify Auth, and Amplify Storage categories to our application. For the authentication category, we followed the documentation [here](https://docs.amplify.aws/lib/auth/getting-started/q/platform/android) and for the storage category [here](https://docs.amplify.aws/lib/storage/getting-started/q/platform/android). Note that when selecting who should have access to our bucket, we chose Auth users only to remain true to the application requirements. We also looked at the [file access levels documentation](https://docs.amplify.aws/lib/storage/configureaccess/q/platform/android#private-access) to ensure that files were only accessible by the creating users, and the [accessing credentials documentation](https://docs.amplify.aws/lib/auth/access_credentials/q/platform/android) for fetching the current user ID.

A benefit of using cloud services, as previously mentioned, is that they typically provide great documentation, and come preconfigured with many privacy features. We will touch on some AWS features we encountered here but this list is by no means exhaustive.

- The buckets can be configured to automatically encrypt new objects stored. Again, we have little control over how they are encrypted but it can be a helpful feature especially if you are using a trusted provider.
- Access control features and permissions can be set on the bucket.
- The administrator has the option to allow users to sign themselves up for the service through the application or to only allow administrators to create users. This can be a helpful feature if the application is to be distributed to a set of known users.

Watch [this](https://drive.google.com/file/d/1xh4gfqwQItIlpALbP3ARZfSUCbMhjK20/view?usp=sharing) to visualize the user creation process. Once the user is created, they will then get an email with their credentials which they can use to sign into the app.

We will now dive into a brief analysis of the code and important functionality.

### Code Overview

#### Asking for permission to read from the gallery

Before attempting to load photos from the gallery, it is necessary to ask for the appropriate permission in the `AndroidManifest.xml`.

`<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>`

This permission also need to be requested in the `ImageProcessor.java` class where the bulk of the calls to the Amplify API take place. Requesting this permission leads to a popup appearing on the device prompting the user to allow or deny the requested permission. 

```
String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE}; //request permissions
requestPermissions(permissions, READ_REQUEST_CODE);
```
Finally, we must display the appropriate message to the user when they accept or deny the requested permission.

```
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
```

#### Configuring the application with the Amplify framwork

This code is taken from the [docs](https://docs.amplify.aws/lib/project-setup/create-application/q/platform/android#n4-initialize-amplify-in-the-application). We add the authorization and storage plugins then call `Amplify.configure()` with our application context, catching appropriate exceptions that may occur. We also add a flag to check if the user has just signed out from the application, in which case we do not need to reconfigure Amplify. 

```  
 if(!getIntent().getBooleanExtra("fromSignOut", false)) {
//ensure amplify does not reconfigure if user just signed out

            try {
            
                Amplify.addPlugin(new AWSCognitoAuthPlugin());  //add auth plugin
                Amplify.addPlugin(new AWSS3StoragePlugin());  //add storage plugin
                Amplify.configure(getApplicationContext());  //configure amplify
                
            } catch (AmplifyException e) {
            
                e.printStackTrace();
                Toast.makeText(this, "Our application has encountered an unexpected error. Please try again later", Toast.LENGTH_LONG).show();
                loginBtn.setEnabled(false);
                
            }
        }
```
#### Authenticating with Amplify

We first ensure that the username and password inputs are non-empty, then call `Amplify.Auth.signIn()` to proceed with the authentication process. `Amplify.Auth.signIn()` takes as input a username, password, as well as success and error callbacks. If the authentication was successful we display a toast to the user, and proceed to the main page of the application. If the authentication was successful, but it is the user's first time entering the application, we first check that they provided the extra email verification input, then proceed by calling `Amplify.Auth.confirmSignIn()`. This method takes as input a confirmation code which we set to be the user email (this can easily be changed) as well as success and error callbacks. If the confirmation is successful, we display a toast to the new user, and proceed to the main page of the application. If at any point along the way authentication was not successful or an exception occurred, we display an appropriate toast to the user and prompt them to try again. Some possible improvements here would be to limit the number of incorrect attempts a user has to sign-in and to include [multi-factor authentication](https://docs.amplify.aws/lib/auth/signin/q/platform/android#multi-factor-authentication).

```
private void authenticate(String username, String password, String email, View v) {
        if (username.isEmpty() || password.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Please provide credentials in order to login", Toast.LENGTH_LONG).show());
        } else {
            Amplify.Auth.signIn(username, password, new Consumer<AuthSignInResult>() {
                @Override
                public void accept(@NonNull AuthSignInResult result) {
                    switch (result.getNextStep().getSignInStep()) {
                        case DONE: //successful login for returning user
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Welcome back, " + username, Toast.LENGTH_LONG).show());
                            onLoginSuccess(v);
                            break;
                        case CONFIRM_SIGN_IN_WITH_NEW_PASSWORD: //successful login for new user
                            if (email.isEmpty()) { //new users need to provide email to verify
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Are you a new user? Please provide your email if you are logging in for the first time", Toast.LENGTH_LONG).show());
                            } else {
                                Amplify.Auth.confirmSignIn(email, new Consumer<AuthSignInResult>() {
                                    @Override
                                    public void accept(@NonNull AuthSignInResult result) {
                                        switch (result.getNextStep().getSignInStep()) {
                                            case DONE: //successful confirmation of email
                                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Welcome, " + username, Toast.LENGTH_LONG).show());
                                                onLoginSuccess(v);
                                                break;
                                            default:
                                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Unsupported sign-in confirmation: " + result.getNextStep().getSignInStep() + " Please try again later", Toast.LENGTH_LONG).show());
                                        }
                                    }
                                }, new Consumer<AuthException>() {
                                    @Override
                                    public void accept(@NonNull AuthException e) {
                                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Authentication Failed. Please try again", Toast.LENGTH_LONG).show());
                                    }
                                });
                            }
                            break;
                        default:
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Unsupported sign-in confirmation: " + result.getNextStep().getSignInStep() + " Please try again later", Toast.LENGTH_LONG).show());
                    }
                }
            }, new Consumer<AuthException>() {
                @Override
                public void accept(@NonNull AuthException e) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Authentication failed. Please try again", Toast.LENGTH_LONG).show());
                }
            });
        }
    }
```

After the user is successfully authenticated with Amplify, we call `Amplify.Auth.fetchSession()` to fetch the user identification needed for listing and downloading private files. We then proceed to the main page of the application passing along the fetched user ID. 

```
private void onLoginSuccess(View v) {
        Amplify.Auth.fetchAuthSession(
                result -> {
                    AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) result;
                    switch(cognitoAuthSession.getIdentityId().getType()) {
                        case SUCCESS:
                            runOnUiThread(() -> {
                                Intent intent = new Intent(v.getContext(), ImageProcessor.class);
                                intent.putExtra("userId", cognitoAuthSession.getIdentityId().getValue()); //pass user Id for private file access
                                startActivity(intent);
                            });
                            break;
                        case FAILURE:
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Failed to access user credentials. Please try again later", Toast.LENGTH_LONG).show());
                    }
                },
                error -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "An exception occurred accessing credentials. Please try again later", Toast.LENGTH_LONG).show())
        );
    }
```
#### Loading Images from the Gallery

When the user clicks the Load from Media button, we first check if the permission has been granted then proceed to let the user choose an image to load from the gallery.

```
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
```
After the user has selected an image from the gallery, we retrieve the image filename, and load the image into the user interface. Note if the image filename already exists within the application, we append a “1” to the start to ensure unique filenames. 

```
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
```

#### Configuring the Saved Files Dropdown Menu

This function will load the saved filenames into the dropdown menu for the user to select. It will optionally set a selected value for the user, or by default set the selection to the first item in the list.

```
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
```

When an image is selected in the dropdown menu, we first check if the image is saved locally, or on the cloud. If the file is saved locally, we call `fileView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));` to load the image into the UI. If it is saved on the cloud, we call `Amplify.Storage.downloadFile()` and pass in the filename, a file object to capture the downloaded image, `StorageDownloadFileOptions` needed to access private images, as well as success and error callbacks. 

```
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
```

`initializeFiles()` will load the saved filenames into our collection then update the dropdown menu. We call this once, when the page is loaded. We list the files saved in internal storage by calling `getFilesDir().listFiles()` and the files saved on the cloud by calling `Amplify.Storage.list()` with the appropriate `StorageListOptions` to list private files.

```
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
```

#### Saving Images to Local and Cloud Storage

This function will save the image to local storage. We open the file location in internal storage using `openFileOutput(filename, MODE_PRIVATE)` then write the image to the file using `bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)`. We then add the filename to our collection and update the dropdown menu accordingly. 

```
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
```
This function will save the image to cloud storage. We first write the image to a local file analogous to the method described above. We then call `Amplify.Storage.uploadFile()` to upload the file to the cloud, passing in `StorageUploadFileOptions` to ensure the file is uploaded to a private bucket.

```
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
``` 

We also need to include a helper function to ensure users files are not saved in both local storage and on the cloud. We added this to keep consistent with the user requirement that files are saved only where expected. We loop through the saved files and check where the file is expected to be saved. If it is expected to be saved locally, we ensure it is deleted from the cloud. If it is expected to be saved to the cloud, we ensure it is deleted locally. We also ensure that any unrecognized files (that could be created as a result of a failed upload/download) are deleted from local storage. 

```
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
```

#### Deleting a File from the App

When the user clicks the delete button, the file is deleted locally or on the cloud depending on where it is stored. The image is then cleared from the user interface, the buttons are disabled, and the dropdown menu of saved files is updated.

```
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
```

This function removes the input filename from the cloud. We call `Amplify.Storage.remove()` with the appropriate `StorageRemoveOptions` to remove private files.

```
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
``` 

#### Signing out of the App

When the user clicks the sign-out button, `Amplify.Auth.signOut()` is called. If the sign-out is successful, we return the user to the login page. Note we pass the boolean `fromSignOut` here so as not to reconfigure Amplify on the login page. 

```
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
```

## Privacy Controls

As previously mentioned, we have implemented three privacy controls to help mitigate the different privacy risks and data harms. They are listed below: 

- User authentication upon opening the application.
- Authorization mechanisms aimed at keeping a user’s photos private.
- Data encryption methods upon saving the images to the cloud.

Each of these controls will be covered in depth in the following three sections.

### Authentication

One of the main advantages of working within the Amazon Web Services ecosystem is the ease with which we can integrate other services offered by AWS. Specifically, we used Amazon Cognito to create users and provide them credentials to access the application.

To initialize users of the application, an Amazon Cognito user is created and added to an Identity and Access Management (IAM) user group with appropriate permissions. In our case, they are given access to a private S3 bucket where images from all users are stored. The term “private bucket” may be confusing at the moment, but the different file access levels will be defined in the following section.

Setting up authentication for an image processing application may seem like overkill, but it truly is an essential step in ensuring that unauthorized access to the users’ data is limited.

### Authorization

Once a user is created and has been successfully authenticated, it is important to ensure that each user’s data is isolated from the others. 

When configuring storage capabilities with the Amplify plugin, Amazon gives us the option to configure the access levels authenticated users have to the application’s S3 bucket. This all ties back to the private bucket created earlier. Amazon has defined three different file access levels:

1. Public: All files in a public bucket are accessible to authenticated users. Users are given read and write permissions on all files. Choosing this access level is clearly inappropriate for our use case as it is crucial that users only be able to view, modify, and delete their own images.
2. Protected: Similarly to a public bucket, all files in a protected bucket are accessible to authenticated users. However, users are given write access to only their own files. Making the bucket protected takes an important step in the right direction by limiting write access, but user privacy is still clearly violated for our use case.
3. Private: All files in a private bucket are, well …  private! This means that users are given read and write access only to their own files. This ensures that each user’s data is successfully isolated from the others. Thus, storing files in a private bucket is our chosen method for the Image Processor.

By ensuring that users are given the appropriate access levels, we’ve taken another important step in ensuring that data is protected from unauthorized access.

### Data Encryption 

The two previous privacy controls we’ve implemented are tasked with ensuring authorized access to the images stored through the application. Data encryption is aimed at making sure that in the case where attackers gain access to the storage bucket, they won’t be able to make use of their findings.

Enabling [data encryption with AWS S3](https://docs.aws.amazon.com/AmazonS3/latest/userguide/bucket-encryption.html) isn’t a tall task as it can be set up with the click of a button. It suffices to look under the “Properties” section of your S3 bucket to enable server-side encryption when storing files to the bucket.

![S3_bucket_encryption](https://github.com/DMurray21/COMP599_A1_amplify/blob/main/Images/Screen%20Shot%202021-02-18%20at%208.47.47%20PM.png)

Amazon’s server-side encryption’s default behaviour is to encrypt files upon saving them to the bucket and decrypting them upon download. Although Amazon offers different options when it comes to [choosing the keys to encrypt the files](https://docs.aws.amazon.com/AmazonS3/latest/userguide/serv-side-encryption.html), we have decided to use S3-Managed Keys for the context of the Image Processor. With S3-Managed Keys, each object saved to the bucket will be encrypted with a unique key. This prevents an attacker who has access to a key to have access to all files in the bucket. Furthermore, as an additional safeguard, each key is encrypted with a master key that is regularly rotated.

It is also important to note that encrypting files has no effect on user experience. As long as a user is authenticated and is performing an authorized action, reading and modifying their images will be a seamless process.

## Conclusion

This article provided an overview of ways to store sensitive user data, associated privacy and security risks, as well as controls to mitigate them. It is tailored to the use case of the Image Processor but the topics are relevant for any application that collects and stores user data. It is important to consider these topics in the design of your application and to continually improve your practices to meet evolving standards and threats.  


 

