package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Bittu on 7/7/2016.
 */
public class StockQuoteRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockQuoteRemoteViewsFactory(getApplicationContext());
    }

    class StockQuoteRemoteViewsFactory implements RemoteViewsFactory{

        private Context context;
        private Cursor cursor;

        public StockQuoteRemoteViewsFactory(Context context){
            this.context = context;
        }

        @Override
        public void onCreate() {
            cursor = executeQuery();
        }

        @Override
        public void onDataSetChanged() {
            cursor = executeQuery();
        }

        private Cursor executeQuery(){
            return getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.CHANGE, QuoteColumns.ISUP},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null);
        }

        @Override
        public void onDestroy() {
            cursor.close();
        }

        @Override
        public int getCount() {
            if(cursor != null)
                return cursor.getCount();
            else
                return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            cursor.moveToPosition(position);
            String symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
            String change = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));
            String bid_price = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));
            int is_up = cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP));

            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);
            view.setTextViewText(R.id.stock_symbol, symbol);
            view.setTextViewText(R.id.bid_price, bid_price);



            if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1){

                view.setInt(R.id.change, "setBackgroundResource",
                        R.drawable.percent_change_pill_green);

            } else{

                view.setInt(R.id.change, "setBackgroundResource",
                        R.drawable.percent_change_pill_red);

            }

            view.setTextViewText(R.id.change, change);

            Intent fillInIntent = new Intent();
            Uri uri = QuoteProvider.Quotes.withSymbol(symbol);
            fillInIntent.setData(uri);
            fillInIntent.putExtra("symbol", symbol);

            view.setOnClickFillInIntent(R.id.container, fillInIntent);


            return view;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if(cursor != null)
                return cursor.getInt(cursor.getColumnIndex(QuoteColumns._ID));
            else
                return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
