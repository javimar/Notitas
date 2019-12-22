package eu.javimar.notitas.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;

import eu.javimar.notitas.R;
import eu.javimar.notitas.widget.NotitasWidgetProvider;

public final class Utils
{
    public static void refreshWidget(Context context)
    {
        // Update the widget with fresh data when adding, updating or deleting Notes
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int [] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, NotitasWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }

    public static int[] getDpsFromDevice(Context context)
    {
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
        dp[1] = height/ (context.getResources().getDisplayMetrics().densityDpi
                / DisplayMetrics.DENSITY_DEFAULT);

        return dp;
    }

    public static boolean isContentUriPointingToValidResource(Uri uri, Context context)
    {
        boolean valid = false;

        ContentResolver cr = context.getContentResolver();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cur = cr.query(uri, projection,
                null, null, null);
        if (cur != null)
        {
            if (cur.moveToFirst())
            {
                String filePath = cur.getString(0);

                if(new File(filePath).exists())
                {
                    valid = true;
                }
            }
            cur.close();
        }
        return valid;
    }

    public static boolean isInternalUriPointingToValidResource(Uri uri, Context context)
    {
        boolean valid = true;

        if(!uri.toString().startsWith("file"))
        {
            uri = Uri.fromFile(new File(uri.toString()));
        }
        try
        {
            context.getContentResolver().openInputStream(uri);
        }
        catch (FileNotFoundException e)
        {
            valid = false;
        }
        return valid;
    }
}