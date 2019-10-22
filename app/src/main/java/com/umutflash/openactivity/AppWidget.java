package com.umutflash.openactivity;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.umutflash.openactivity.data.model.Spot;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, Spot[] favorites,
                                int appWidgetId) {


        String bodyText = "";
        for (Spot f : favorites) {
            bodyText += f.getTitle() + "\n";
            bodyText += f.getCategory() + "\n\n";

        }

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);


        views.setTextViewText(R.id.appwidget_text, bodyText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void onUpdateNewAppWidget(Context context, AppWidgetManager appWidgetManager, Spot[] favorites, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, favorites, appWidgetId);
        }
    }

}

