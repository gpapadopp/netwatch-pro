package eu.gpapadop.netwatchpro.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import eu.gpapadop.netwatchpro.R;

public class NotificationsHandler {
    private static final String CHANNEL_ID = "netwatch_pro_notifications_channel";
    private static final int NOTIFICATION_ID = 1;
    private Context appContext;

    public NotificationsHandler(Context newAppContext){
        this.appContext = newAppContext;
        this.setupNotificationChannel();
    }

    private void setupNotificationChannel(){
        NotificationManager mNotificationManager = (NotificationManager) this.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "netwatch_pro_notifications_channel";
        CharSequence name = this.appContext.getString(R.string.app_name);
        String description = this.appContext.getString(R.string.app_name);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name,importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);
    }

    public void showStickyNotification(String title, String message) {
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.appContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.globe_dots)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true); // Make the notification sticky

        // Show the notification
        NotificationManager notificationManager = this.appContext.getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void hideNotification() {
        NotificationManager notificationManager = this.appContext.getSystemService(NotificationManager.class);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
