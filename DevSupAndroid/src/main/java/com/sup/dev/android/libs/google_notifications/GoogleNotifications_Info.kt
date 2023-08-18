package com.sup.dev.android.libs.google_notifications


internal class GoogleNotifications_Info

/**

Не реализовано, чтоб не тащить лишнии библиотеки в сапорт.

Сурс: https://www.simplifiedcoding.net/firebase-cloud-messaging-tutorial-android/

Серверная часть в проекте DevSupJavaPc (https://github.com/ZeonXX/DevSupJavaPc/blob/master/src/com/sup/dev/java_pc/google/GoogleNotification.java)
Токен генерируется один раз, при установке приложения, его нужно передать на сервер и хранить для уведомления этого устройтсва.

Настройка:
1. Подключить Firebase Cloud Messaging в Android Studio
2. Добавить 2 каласса описанныйх ниже и прописать их в manifest.
3. Вызвать init у GoogleNotificationsReceiver



public class GoogleNotifications extends FirebaseInstanceIdService {

private static CallbackSource<String> onToken;
private static CallbackSource<RemoteMessage> onReceive;

public static void init(CallbackSource<String> onToken, CallbackSource<RemoteMessage> onReceive){
GoogleNotifications.onToken = onToken;
GoogleNotifications.onReceive = onReceive;

String token = FirebaseInstanceId.getInstance().getToken();
if(token != null) onToken.callback(token);
}

@Override
public void onTokenRefresh() {
onToken.callback(FirebaseInstanceId.getInstance().getToken());
}

static void onReceive(RemoteMessage remoteMessage){
onReceive.callback(remoteMessage);
}

}






public class GoogleNotificationsReceiver extends FirebaseMessagingService {

@Override
public void onMessageReceived(RemoteMessage remoteMessage) {
super.onMessageReceived(remoteMessage);
GoogleNotifications.onReceive(remoteMessage);
}

}




<service
android:name=".GoogleNotifications">
<intent-filter>
<action android:name="com.google.firebase.INSTANCE_ID_EVENT"/>
</intent-filter>
</service>

<service
android:name=".GoogleNotificationsReceiver">
<intent-filter>
<action android:name="com.google.firebase.MESSAGING_EVENT"/>
</intent-filter>
</service>


 */