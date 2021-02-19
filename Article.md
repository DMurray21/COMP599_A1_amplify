# Storing Sensitive User Data 

As a mobile app developer, you may need to collect and store sensitive user data in your application. You may need to track a user’s diet and exercise routine for a health and wellness application, store photos taken by a user for a pdf scanner application, or keep a detailed account of a user’s schedule for a day planner or calendar application.

The focus of this article will revolve around two ways for an application to store data, the benefits and risks of each one, as well as tips to mitigate security and privacy harms. The use case of the article will be a simple image processor application where images can be uploaded, downloaded, edited and saved. However, much information regarding the storage of sensitive data extends beyond the scope of the example application. 

The image processor application has fairly basic functionality. It ensures the user is authenticated before being given access to the application. It allows users to load images from their camera roll, apply a filter on the image, and save it to local storage or to an external cloud service. A user can also view all the different photos they have saved and have the option to delete them.

## User Requirements for Data Collection and Storage

Before diving into how the application will save to internal storage or to the cloud, it is an important exercise to understand the different expectations a user may have when you collect and store their data, as well as the relevant risks and harms.

### What behaviour would a user expect from the application?

- The application must only have access to features on the device that are essential for full application functionality. In our case, the image processor must have read access to a user’s camera roll to select photos to filter and save.
- The application must ask for explicit consent from the user to access the necessary features on the phone. Without given consent, the application should not be able to function.
- Security mechanisms should be in place to protect the user’s images from unauthorized access from other users or attackers.
- Data quality shouldn’t be affected by storing the data. In our case, the images stored by the user shouldn’t be distorted when they are saved or retrieved from internal or cloud storage.
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

As stated in the Android data and image storage overview document listed above there are four options to choose from when storing data, depending on the use case. We include an image of the section here for convenience.

![Data and file storage overview](https://github.com/DMurray21/COMP599_A1_amplify/blob/main/data_storage_overview.png)

We will look more into app-specific storage as it is relevant to our use case.

### Internal vs External Storage

Within app-specific storage there are two subclasses, internal storage, and external storage. Descriptions of each are given from the android documentation below.

![internal_storage_description](https://github.com/DMurray21/COMP599_A1_amplify/blob/main/internal_storage_desc.png)

It is clear to see, when dealing with sensitive information it is best to use internal rather than external storage. If images are stored on external storage, they are still kept within the scope of the application, but other applications granted the MANAGE_EXTERNAL_STORAGE permission (such as the Android File (link to file app here) application) will still be able to read and write to them. It is important to notice this distinction when using app-specific storage. More on this can be found [here](https://www.journaldev.com/9383/android-internal-storage-example-tutorial) 

### Cloud Storage

Cloud storage means the files are stored on the internet typically through a third-party cloud computing provider. Specific to the use case of our application, our files are stored through the [Amazon Simple Storage Service (S3)](https://aws.amazon.com/s3/).

The primary benefits of using cloud storage are: 

- Much more scalable and cost-effective than local storage. 
- Easy to implement and often have well documented API’s.
- The risk of storing data on physical devices as with local storage is removed.

The primary downsides of using cloud storage are:

- Less control of how the data is stored, who has access to it, and the security mechanisms protecting it. You must comply with the cloud provider’s privacy policy. 
- You are dependent on the security system of the cloud. Although most of them are quite well established and trusted, there have been [incidents](https://www.theguardian.com/technology/2016/aug/31/dropbox-hack-passwords-68m-data-breach) where cloud systems have been compromised. 
- Slower retrieval and access to data. However, as edge computing becomes more prevalent this is becoming less of a downside. 

A good practice and something to consider when storing data through a cloud service is to implement security and privacy controls locally on top of the security and privacy controls provided by the cloud service.

### App Demo

Here is a working demonstration of our application.

![app_demo](https://github.com/DMurray21/COMP599_A1_amplify/blob/main/2021-02-18%2013-34-07.mkv)

**Notes on demo and relevant documentation**

In this demo we show in order:
1. Logging into the application for the first time 
2. Accepting the required permissions 
3. Loading an image from external storage
4. Applying a filter
5. Saving the image locally and confirming that it is stored successfully
6. Loading another image from external storage 
7. Saving the image to cloud storage and confirming that it is stored successfully 
8. Deleting files from both local storage and cloud storage and confirming that they are deleted 
9. Signing out of the application
10. Trying to sign into the application with incorrect credentials

To integrate Amazon S3 with our application we used the AWS [Amplify Framework](https://aws.amazon.com/amplify/). We followed the [documentation](https://docs.amplify.aws/lib/project-setup/prereq/q/platform/android) to install the Amplify CLI and other prerequisites, and the [documentation](https://docs.amplify.aws/lib/project-setup/create-application/q/platform/android#option-2-follow-the-instructions) to initialize our application with Amplify. Once that was complete, we added the Amplify Auth, and Amplify Storage categories to our application. For the authentication category we followed the documentation [here](https://docs.amplify.aws/lib/auth/getting-started/q/platform/android) and for the storage category [here](https://docs.amplify.aws/lib/storage/getting-started/q/platform/android). Note that when selecting who should have access to our bucket, we chose Auth users only to remain true to the application requirements. We also looked at the [file access levels documentation](https://docs.amplify.aws/lib/storage/configureaccess/q/platform/android#protected-access) to ensure that files were only accessible to the creating users, and the [accessing credentials documentation](https://docs.amplify.aws/lib/auth/access_credentials/q/platform/android) for fetching the current user ID.

A benefit about using cloud services, as mentioned before, is that they typically provide great documentation, and come preconfigured with many privacy features. We will touch on some AWS features we encountered here but this list is by no means exhaustive.

- The buckets can be configured to automatically encrypt new objects stored. Again, we have little control over how they are encrypted but it can be a helpful feature especially if you are using a trusted provider.
- Access control features and permissions can be set on the bucket.
- The administrator has the option to allow users to sign themselves up for the service through the application or to only allow administrators to create users. This can be a helpful feature if the application is to be distributed to a set of known users.

![creating_a_user](https://github.com/DMurray21/COMP599_A1_amplify/blob/main/2021-02-18%2019-48-17.mkv)

The user will then get an email with their credentials which they can use to sign into the app.

We will now dive into a brief analysis of the code and important functionality.

**CODE**

## Privacy Controls

As previously mentioned, we have implemented three privacy controls to help mitigate the different privacy risks and data harms.

- User authentication upon opening the application.
- Authorization mechanisms aimed at keeping a user’s photos private.
- Data encryption methods upon saving the images to the cloud.

Each of these controls will be covered in depth in the following three sections.

### Authentication

One of the main advantages of working within the Amazon Web Services ecosystem is the ease with which we can integrate other services offered by AWS. Specifically, we used Amazon Cognito to create users and provide them credentials to access the application.

To initialize users of the application, an Amazon Cognito user is created and added to an Identity and Access Management (IAM) user group with appropriate permissions. In our case, they are given access to a private S3 bucket where images from all users are stored. These permissions include read and write access to the storage bucket, meaning that an authenticated user will be able to download, upload, and remove their files from the bucket. The term “private bucket” may be confusing at the moment, but the different file access levels will be defined in the following section.

Setting up authentication for an image processing application may seem like overkill, but it truly is an essential step in ensuring that unauthorized access to the users’ data is limited.

### Authorization

Once a user is created and has been successfully authenticated, it is important to ensure that each user’s data is isolated from the others. 

When configuring storage capabilities with the Amplify plugin, Amazon gives us the option to configure the access levels authenticated users have to the application’s S3 bucket. This all ties back to the private bucket created earlier. Amazon has defined three different file access levels:

1. Public: All files in a public bucket are accessible to authenticated users. Users are given read and write permissions on all files. Choosing this access level is clearly inappropriate for our use case as it is crucial that users only be able to view, modify, and delete their own images.
2. Protected: Similarly to a public bucket, all files in a protected bucket are accessible to authenticated users. However, users are only given write access to their own files. Making the bucket protected takes an important step in the right direction by limiting write access, but user privacy is still clearly violated for our use case.
3. Private: All files in a private bucket are, well …  private! This means that users are only given read and write access to their own files. This ensures that each user’s data is successfully isolated from the others. Thus, storing files in a private bucket is our chosen method for the Image Processor.

By ensuring that users are given the appropriate access levels, we’ve taken another important step in ensuring that data is protected from unauthorized access.

### Data Encryption 

The two previous privacy controls we’ve implemented are tasked with ensuring authorized access to the images stored through the application. Data encryption is aimed at making sure that in the case where attackers gain access to the storage bucket, they won’t be able to make use of their findings.

Enabling [data encryption with AWS S3](https://docs.aws.amazon.com/AmazonS3/latest/userguide/bucket-encryption.html) isn’t a tall task as it can be set up with the click of a button. It suffices to look under the “Properties” section of your S3 bucket to enable server-side encryption when storing files to the bucket.

![S3_bucket_encryption](https://github.com/DMurray21/COMP599_A1_amplify/blob/main/Screen%20Shot%202021-02-18%20at%208.47.47%20PM.png)

Amazon’s server-side encryption’s default behaviour is to encrypt files upon saving them to the bucket and decrypting them upon download. Although Amazon offers different options when it comes to [choosing the keys to encrypt the files](https://docs.aws.amazon.com/AmazonS3/latest/userguide/serv-side-encryption.html), we have decided to use S3-Managed Keys for the context of the Image Processor. With S3-Managed Keys, each object saved to the bucket will be encrypted with a unique key. This prevents an attacker who has access to a key to have access to all files in the bucket. Furthermore, as an additional safeguard, each key is encrypted with a master key that is regularly rotated.

It is also important to note that encrypting files has no effect on user experience. As long as a user is authenticated and is performing an authorized action, reading and modifying their images will be a seamless process.

## Conclusion

This article provided an overview of ways to store sensitive user data, associated privacy and security risks as well as controls to mitigate them. It is tailored to the use case of the image processor but the topics are relevant for any application that collects and stores user data. It is important to consider these topics in the design of the application and to continually improve your practices to meet evolving standards and threats.  


 

