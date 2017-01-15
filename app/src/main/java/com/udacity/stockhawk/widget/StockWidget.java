package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidget extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            Log.d("on Recieve","widget");
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int ids[] = manager.getAppWidgetIds(new ComponentName(context, getClass()));
            manager.notifyAppWidgetViewDataChanged(ids, R.id.list_widget);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        super.onUpdate(context,appWidgetManager,appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.stock_widget);
            Log.d("on Update ","widget");
            Intent intent=new Intent(context, MainActivity.class);
            PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,0);
            views.setOnClickPendingIntent(R.id.header,pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapter1(context, views);
            }
            Intent clickIntentTemplate = new Intent(context, DetailActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.list_widget, clickPendingIntentTemplate);
            views.setEmptyView(R.id.list_widget,R.id.empty_text);
            appWidgetManager.updateAppWidget(appWidgetId,views);
        }
    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        Log.d("on Update ","adapter");
        views.setRemoteAdapter(R.id.list_widget,
                new Intent(context, StockWidgetService.class));
    }
    @SuppressWarnings("deprecation")
    private void setRemoteAdapter1(Context context, @NonNull final RemoteViews views) {
        Log.d("on Update ","adapter");
        views.setRemoteAdapter(0, R.id.list_widget,
                new Intent(context, StockWidgetService.class));
    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

