package com.feedbackssdk.myvault.Managers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.feedbackssdk.myvault.Activities.MainActivity;
import com.feedbackssdk.myvault.R;

public class NotificationsManager {
    private static final String SUCCESS_CHANNEL_ID = "process_text_channel";
    private static final String ERROR_CHANNEL_ID = "process_text_error_channel";
    private static final int SUCCESS_NOTIFICATION_ID = 2;
    private static final int ERROR_NOTIFICATION_ID = 3;

    // Show a success notification using SUCCESS_CHANNEL_ID.
    public static void showSuccessNotification(Context context, String message) {
        createNotificationChannel(context, SUCCESS_CHANNEL_ID, "Process Text Notifications", "Notifications for processed text.");

        // Create an Intent to launch your app (MainActivity in this example)
        Intent launchIntent = new Intent(context, MainActivity.class);
        // Clear the back stack to create a fresh start when launched from the notification
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create a PendingIntent from the Intent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SUCCESS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_vault)
                .setContentTitle("My Vault")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)  // Attach the PendingIntent here
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManagerCompat.notify(SUCCESS_NOTIFICATION_ID, builder.build());
    }

    // Show an error notification using ERROR_CHANNEL_ID.
    public static void showErrorNotification(Context context, String message) {
        createNotificationChannel(context, ERROR_CHANNEL_ID, "Process Text Error", "Error notifications for processed text.");

        PendingIntent dummyIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(), // an empty Intent
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ERROR_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_vault)
                .setContentTitle("My Vault Error")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                // Set the dummy intent so nothing significant happens on tap.
                .setContentIntent(dummyIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManagerCompat.notify(ERROR_NOTIFICATION_ID, builder.build());
    }

    // Create a notification channel with the specified ID, name, and description.
    private static void createNotificationChannel(Context context, String channelId, String channelName, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
