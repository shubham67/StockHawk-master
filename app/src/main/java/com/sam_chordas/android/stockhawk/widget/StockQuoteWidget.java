package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.PlotActivity;

/**
 * Created by Bittu on 7/7/2016.
 */
public class StockQuoteWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;

        for(int i = 0; i < N; i++){

            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, StockQuoteRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.quote_collection_widget);
            view.setRemoteAdapter(R.id.widget_list_view, intent);
            view.setEmptyView(R.id.widget_list_view, R.id.widget_empty_text);

            Intent templateIntent = new Intent(context, PlotActivity.class);
            templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent templatePendingIntent = PendingIntent.getActivity(context, 0, templateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            view.setPendingIntentTemplate(R.id.widget_list_view, templatePendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, view);
        }
    }
}
