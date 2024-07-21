package eu.javimar.notitas.util;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import eu.javimar.notitas.R;
import eu.javimar.notitas.view.NotaDetailActivity;

public class NotificationHelper extends ContextWrapper {
    // The id of the channel. Necessary for Android O
    private static final String NOTITAS_CHANNEL_ID = "notitas_channel_id";
    private static final String NOTITAS_CHANNEL_NAME = "notitas_channel_name";
    private static final String NOTITAS_GROUP = "notitas_group";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    private void createChannels() {
        NotificationChannel owsChannel =
                new NotificationChannel(NOTITAS_CHANNEL_ID, NOTITAS_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH);
        owsChannel.enableLights(true);
        owsChannel.enableVibration(true);
        owsChannel.setLightColor(R.color.colorPrimary);
        owsChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(owsChannel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification(String header,
                                                             int notaId,
                                                             String message,
                                                             int requestCode) {
        // Open activity
        Intent resultIntent = new Intent(this, NotaDetailActivity.class);
        resultIntent.putExtra("notaId", notaId);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(getApplicationContext(), NOTITAS_CHANNEL_ID)
                .setContentTitle(header)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setSmallIcon(R.drawable.ic_nota_notification)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent))
                .setWhen(System.currentTimeMillis())
                .setGroup(NOTITAS_GROUP)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);
    }
}