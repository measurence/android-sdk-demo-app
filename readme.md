# Measurence

Measurence's mission is to transform retail stores into "physical websites", by providing:

1. actionable knowledge about customers behavior into the stores
1. marketing solutions and technologies aimed at improving the customers engagement and loyalty

As a primary source of Knowledge, Measurence install and manages a network of WiFi sensors which collect anonymized and aggregated information about foot traffic within retail stores.

Measurence crunches this information in order to deliver its own analytics solution, and also distributes it to partners in order to provide the highest possible value to the stakeholders.

For any inquiry, drop us a email at info@measurence.com

# Measurence API Platform

In order to implement an effective and efficient distribution to its partners of the collected information, Measurence has create an "API Platform".

The Measurence API Platform is currently intended to serve 3 use cases:

* a mobile app, which subscribes to receive customers "session updates" (related to the device where it's installed) via push notification (e.g. Google Cloud Messaging)
* a mobile app, which subscribes to receive customers "session updates" (related to the device where it's installed) to its Back End servers
* a Back End server of a Measurence partner, which subscribes to receive "session updates" of _ALL_ devices captured by the Measurence sensors

# Measurence Android SDK and Demo App

The Android SDK is intended to be embedded in an Android mobile app in order to implement the related use cases (see previous Sections).

The Measurence Android SDK Demo App is a reference implementation which showcases how to use the SDK in order to subscribe to the presence events, and to listen to the GCM push notifications sent by the Measurence platform.

# Measurence API Platform: definitions

A `Session` is a time window of permanence of a device within a location monitored by the Measurence sensors.

A `Session update` is an update of a Session which carries information such as:

* the anonymized device mac address
* the time window of permanence
* the (Measurence defined) identifier of the location where the session occurred
* whether the update is about a _new_ session, an _existing_ session (which is extended in time), or an _ended_ session
* whether it is the first time this device has been seen in the venue
* possibly, a list of "user identities" (e.g. an email) associated to the device

A `Measurence Partner` is an organization with whom Measurence has signed an agreement to share the "session update" of a subset the captured devices (or all the devices). Each partner is assigned a "Partner Id", which is registered into the Measurence Back Office system.

# Measurence API Platform: subscription flow

A partner is assigned a partner id, and is registered into the Measurence Back Office. Please contact the Measurence personnel; Measurence email is info@measurence.com.

## Configure the demo app

### GCM Push notifications
1. Put the Partner Id provided by Measurence in `measurence-android-sdk-demo-app\app\src\main\res\values\gcm.xml`, by overwriting value `MEASURENCE_PARTNER_ID`
1. Provide to Measurence the `GCM API Key` of its Google Api Project
    * see http://developer.android.com/google/gcm/gs.html in order to understand how to obtain and API Key
1. Get a Project Number for its Google Api Project (see http://developer.android.com/google/gcm/gs.html)
1. Configure the app by putting the Project Number in `measurence-android-sdk-demo-app\app\src\main\res\values\gcm.xml`, by overwriting value `GOOGLE_CLOUD_MESSAGING_PROJECT_NUMBER`

### HTTP Post notifications at partners' Back End servers

1. If you are planning to receive notifications at your own servers (via an HTTP Post), you have to provide to Measurence a suitable URL

## Configure the demo app: User Identity (mandatory)

The Measurence APIs requires a "User Identity" to be passed: this is intended to be the user name of the user of the app (e.g. an email).

For the Demo app to work, just put your email into `measurence-android-sdk-demo-app\app\src\main\res\values\strings.xml`, by overwriting value `user_identity`.

## Running the Demo App

You may want to import the project in [Android Studio](https://developer.android.com/sdk/installing/studio.html), and execute it in your device.

Then, you can choose from the app menu a suitable subscription action:

* if you choose to subscribe to GCM notifications, the app will then be filled with upcoming Session Updates (even if in background)
* if you choose to subscribe to HTTP Post notifications, your Back End servers will receive Session Updates
* note that you may want to subscribe to both