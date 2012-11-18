package com.oreilly.android.foodtruckmanager;

import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.oreilly.android.foodtruckmanager.receiver.RESTResultReceiver;
import com.oreilly.android.foodtruckmanager.service.RESTService;

public class FoodTruckApplication extends Application implements RESTResultReceiver.Receiver
{    
    private RESTResultReceiver receiver;
//    private String restQuery = "http://search.twitter.com/search.json?q=%23android&rpp=5&include_entities=true&result_type=mixed";
//    
//    public static final String COMMAND_STR_QUERY = "query";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        System.out.println("Application created");
     
        // Start service here
        createService();
    }
    
    public void createService()
    {
        /*Start Rest service intent*/
        startService(new Intent(this, RESTService.class));
        
    }

    public void onPause() {
        receiver.setReceiver(null);
    }

    public void onReceiveResult(int resultCode, Bundle resultData) 
    {
        /*switch (resultCode) {
        case RUNNING:
            //show progress
            break;
        case FINISHED:
            List results = resultData.getParcelableList("results");
            // do something interesting
            // hide progress
            break;
        case ERROR:
            // handle the error;
            break;
            */
    }
}
