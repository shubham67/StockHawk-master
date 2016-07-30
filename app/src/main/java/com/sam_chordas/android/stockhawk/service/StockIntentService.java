package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

import java.util.logging.LogRecord;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

  android.os.Handler mHandler;

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mHandler = new Handler();
  }

  @Override protected void onHandleIntent(Intent intent) {

    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")){
      args.putString("symbol", intent.getStringExtra("symbol"));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.

    if(stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args)) == GcmNetworkManager.RESULT_FAILURE){
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(StockIntentService.this, R.string.no_stock, Toast.LENGTH_SHORT).show();
        }
      });
    }
  }
}
