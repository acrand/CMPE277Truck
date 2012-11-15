package com.oreilly.android.foodtruckmanager.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.oreilly.android.foodtruckmanager.GmapsViewActivity;
import com.oreilly.android.foodtruckmanager.foodtrucks.FoodTruck;

public class FoodTruckReceiver extends BroadcastReceiver {

	public final static int FOODTRUCK_NEWLOCATION = 1001;
	public final static String FOODTRUCK_MAPIT =  "Map It!";
	public final static String FOODTRUCK_GETDIRECTIONS = "Directions";
	public final static String FOODTRUCK_CALLIT =  "Call It!";
	public final static String FOODTRUCK_DETAILS =  "Details!";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			FoodTruck ft = (FoodTruck) extras.getSerializable("FoodTruck");
			Log.w("DEBUG", "received new food truck" + ft.toString());

			PendingIntent pi = PendingIntent.getActivity(context, FoodTruckReceiver.FOODTRUCK_NEWLOCATION, 
					 new Intent(context, GmapsViewActivity.class)
			          .putExtra("FoodTruck", ft)
			          .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			          .setAction(FOODTRUCK_MAPIT)
			          ,PendingIntent.FLAG_UPDATE_CURRENT);

			int foodTruckID = context.getResources().getIdentifier("foodtruck", "drawable",context.getPackageName());
			
			int callID = context.getResources().getIdentifier("call", "drawable",context.getPackageName());
			int mapID = context.getResources().getIdentifier("googlemapsicon", "drawable",context.getPackageName());
			int directionID = context.getResources().getIdentifier("starthere", "drawable",context.getPackageName());

			Notification notify = new Notification.Builder(context)
					.setContentTitle(ft.getName()+ " - location update")
					.setContentText(ft.getLocation())
					.setSmallIcon(foodTruckID)
					.setLargeIcon(((BitmapDrawable)context.getResources().getDrawable(mapID)).getBitmap())
					.setContentIntent(pi)
					.setLights(1, 1, 2)
					.addAction(directionID, FOODTRUCK_GETDIRECTIONS, PendingIntent.getActivity(context, FoodTruckReceiver.FOODTRUCK_NEWLOCATION, 
							 new Intent(context, GmapsViewActivity.class)
			          .putExtra("FoodTruck", ft)
			          .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			          .setAction(FOODTRUCK_GETDIRECTIONS)
			          ,PendingIntent.FLAG_ONE_SHOT))
					.addAction(callID, FOODTRUCK_CALLIT, PendingIntent.getActivity(context, FoodTruckReceiver.FOODTRUCK_NEWLOCATION, 
							 new Intent(context, GmapsViewActivity.class)
			          .putExtra("FoodTruck", ft)
			          .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			          .setAction(FOODTRUCK_CALLIT)
			          ,PendingIntent.FLAG_ONE_SHOT))
					.build();

			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			// Hide the notification after its selected
			notify.flags |= Notification.FLAG_AUTO_CANCEL;

			notificationManager.notify(0, notify);

			Toast.makeText(context,ft.getName()+ " - location update",
					Toast.LENGTH_SHORT).show();

		}

	}

}
