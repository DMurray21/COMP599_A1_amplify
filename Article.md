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

- Insecurity: If insufficient security mechanisms are in place, photos may be stolen or disseminated without consent. Different authentication and access control measures must be in place to mitigate these risks. Mechanisms implemented by the image processor include authentication, access control, and encryption. They will be covered more in depth in the **(link)** section of the article.
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

![Data and file storage overview]
