package eu.javimar.notitas.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import eu.javimar.notitas.R;
import eu.javimar.notitas.widget.NotitasWidgetProvider;

public final class WidgetUtil
{
    public static void refreshWidget(Context context)
    {
        // Update the widget with fresh data when adding, updating or deleting Notes
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int [] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, NotitasWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }
}
