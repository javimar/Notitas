package eu.javimar.notitas.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

import es.dmoral.toasty.Toasty;
import eu.javimar.notitas.R;
import eu.javimar.notitas.synch.ReminderReceiver;
import eu.javimar.notitas.widget.NotitasWidgetProvider;

public final class HelperUtils {
    public static void refreshWidget(Context context) {
        // Update the widget with fresh data when adding, updating or deleting Notes
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, NotitasWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }

    public static int[] getDpsFromDevice(Context context) {
        int[] dp = new int[2];

        // display dimensions in pixels of this device
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;
        // get DIPs
        dp[0] = width / (context.getResources().getDisplayMetrics().densityDpi
                / DisplayMetrics.DENSITY_DEFAULT);
        dp[1] = height / (context.getResources().getDisplayMetrics().densityDpi
                / DisplayMetrics.DENSITY_DEFAULT);

        return dp;
    }

    public static boolean isContentUriPointingToValidResource(Uri uri, Context context) {
        boolean valid = false;

        ContentResolver cr = context.getContentResolver();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cur = cr.query(uri, projection,
                null, null, null);
        if (cur != null) {
            if (cur.moveToFirst()) {
                String filePath = cur.getString(0);

                if (new File(filePath).exists()) {
                    valid = true;
                }
            }
            cur.close();
        }
        return valid;
    }

    public static boolean isInternalUriPointingToValidResource(Uri uri, Context context) {
        boolean valid = true;

        if (!uri.toString().startsWith("file")) {
            uri = Uri.fromFile(new File(uri.toString()));
        }
        try {
            context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            valid = false;
        }
        return valid;
    }

    // Return a "unique" integer based on a String.
    public static int getKeyFromStr(String string) {
        int key = 0;
        for (int i = 0; i < string.length(); i++) {
            key = key + (int) string.charAt(i);
        }
        return key;
    }

    public static boolean int2Bool(int i) {
        return i == 1;
    }

    public static int bool2Int(boolean b) {
        if (b) return 1;
        return 0;
    }

    public static void cancelReminder(Context context, String title,
                                      boolean isNotaBeingDeleted) {
        int requestCode = getKeyFromStr(title);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);

        if (!isNotaBeingDeleted)
            Toasty.info(context, context.getString(R.string.reminder_off),
                    Toast.LENGTH_SHORT).show();
    }
}