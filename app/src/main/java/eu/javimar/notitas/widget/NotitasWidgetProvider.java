package eu.javimar.notitas.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.TaskStackBuilder;

import eu.javimar.notitas.MainActivity;
import eu.javimar.notitas.R;
import eu.javimar.notitas.view.NotaDetailActivity;

public class NotitasWidgetProvider extends AppWidgetProvider {
    private static final String WIDGET_ITEM_CLICK_ACTION = "widget_item_click_action";

    // Called when the BroadcastReceiver receives an Intent broadcast.
    @Override
    public void onReceive(Context context, Intent intent) {
        // receives the fillInIntent from RemoteViews getViewAt
        if (intent.getAction() != null && intent.getAction().equals(WIDGET_ITEM_CLICK_ACTION)) {
            int notaId = intent.getIntExtra("notaId", 0);

            Intent intentDetail = new Intent(context, NotaDetailActivity.class);
            intentDetail.putExtra("notaId", notaId);
            intentDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(intentDetail);
            stackBuilder.startActivities();
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Set up the intent that starts the RemoteViews, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, NotitasWidgetRemoteViewsService.class);
            // Get the layout for the App Widget
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_detail_main);
            // This adapter connects to a RemoteViewsService through the specified intent.
            // This is how you populate the data.
            remoteViews.setRemoteAdapter(R.id.widget_list, intent);

            // The empty view is displayed when the collection has no items.
            remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);

            // Create an Intent to launch MainActivity
            Intent intentMain = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(context, 0, intentMain, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_header, pendingIntent);

            // This section makes it possible for items to have individualized behavior.
            // It does this by setting up a pending intent template. Individuals items of a collection
            // cannot set up their own pending intents. Instead, the collection as a whole sets
            // up a pending intent template, and the individual items set a fillInIntent
            // to create unique behavior on an item-by-item basis.
            Intent itemIntent = new Intent(context, NotitasWidgetProvider.class);
            // Set the action for the intent when the user touches a particular view, it will have the effect of
            // broadcasting TOAST_ACTION.
            itemIntent.setAction(WIDGET_ITEM_CLICK_ACTION);
            PendingIntent itemClickPendingIntent = PendingIntent.getBroadcast(context, 0, itemIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_list, itemClickPendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}