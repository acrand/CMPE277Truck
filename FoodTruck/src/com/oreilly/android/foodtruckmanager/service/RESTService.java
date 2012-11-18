package com.oreilly.android.foodtruckmanager.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.oreilly.android.foodtruckmanager.foodtrucks.FoodTruck;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class RESTService extends Service {

	private Looper serviceLooper;

	private Messenger mMessenger ;
    
	private static ServiceHandler serviceHandler;

	private static boolean stopThreadID = false;
	private int counter = 0;

	public static synchronized boolean stopRequested() {
		return stopThreadID;
	}

	public static synchronized void setStopRequested(boolean flag) {
		stopThreadID = flag;
	}

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			
			while (!stopRequested()) {
				counter++;
				synchronized (this) {
					try {
						callTwitterAPI();
						System.out.println("waiting: "+counter);
						// sleep for 5 seconds.
						wait(5000);
					} catch (Exception e) {
						throw new RuntimeException(
								"Error occurred in service thread");
					}
				}
			}
			stopSelf(msg.arg1);
		}

		public void callTwitterAPI() {
			Context ctx = getApplicationContext();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			long last_tweet = prefs.getLong("last.tweet.id", 0);
			System.out.println("LAST TWEET " + last_tweet);
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
	        HttpGet httpGet = new HttpGet("http://search.twitter.com/search.json?q=%23CMPE277Truck&since_id="+last_tweet);
			try {
				System.out.println("Calling TWITTER");
				HttpResponse response = httpClient.execute(httpGet,
						localContext);
				HttpEntity entity = response.getEntity();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						entity.getContent()));

				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					System.out.println(inputLine);
					JSONParser parser = new JSONParser();  
					JSONObject obj = (JSONObject)parser.parse(inputLine);
					JSONArray arr = (JSONArray) obj.get("results");
					if (!arr.isEmpty()) {						
						obj = (JSONObject) arr.get(0);
						Object id_str = obj.get("id_str");
						last_tweet = Long.parseLong(id_str.toString());
						prefs.edit().putLong("last.tweet.id", last_tweet).commit();
						Object obj_text = obj.get("text");
						String piece[] = obj_text.toString().split("/", 6);
						System.out.println(piece[1]);
						System.out.println(piece[2]);
						System.out.println(piece[3]);
						System.out.println(piece[4]);
						System.out.println(piece[5]);	
						FoodTruck ft = new FoodTruck();
						ft.setName(piece[1]);
						ft.setTime(piece[2]);
						ft.setLocation(piece[3]);
						ft.setPhone(piece[4]);
						ft.setDescription(piece[5]);
						broadcastFoodTruck(ft);
					}
					else { System.out.println("No new entries."); }
						
				}
				in.close();

			} catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void onCreate() {
        //Create new thread to run in background not main UI thread
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		serviceLooper = thread.getLooper();
		
		serviceHandler = new ServiceHandler(serviceLooper);
		
		mMessenger = new Messenger(serviceHandler);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("Starting service id: " + startId);
		
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putLong("last.tweet.id", 0).commit();

		
		Toast.makeText(this, "service starting id: " + startId,
				Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		Message msg = serviceHandler.obtainMessage();
		msg.arg1 = startId;
		serviceHandler.sendMessage(msg);

		
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}


	@Override
	public void onDestroy() {
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    prefs.edit().putLong("last.tweet.id", 0).commit();
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
	}
	

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding app", Toast.LENGTH_SHORT).show();
        
        return mMessenger.getBinder();
    }    

	private void broadcastFoodTruck(FoodTruck ft) {

		/*Create new intent and broadcast food truck*/
		sendBroadcast(new Intent().putExtra("FoodTruck", ft).setAction(
				"com.oreilly.android.foodtruckmanager.broadcast"));

	}
}