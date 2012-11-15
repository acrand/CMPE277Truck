package com.oreilly.android.foodtruckmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.oreilly.android.foodtruckmanager.foodtrucks.FoodTruck;
import com.oreilly.android.foodtruckmanager.receiver.FoodTruckReceiver;
import com.oreilly.android.foodtruckmanager.service.RESTService;
import com.oreilly.android.taskmanager.R;
import com.oreilly.android.foodtruckmanager.views.*;

public class GmapsViewActivity extends MapActivity {
	private FoodTruckApplication app;
	private MapView mapView;
	private RESTService itemizedoverlay;
	private Address addressToUse;
	private String locationToUse;
	private LocationOverlay locationItemizedoverlay;

	private String currentLocation = "555 Bailey Ave., San Jose, CA";
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String action = "No Action Text Set";
		setContentView(R.layout.main);
		setUpViews();
		
	    //bindService(new Intent(this, RESTService.class), mConnection, Context.BIND_AUTO_CREATE);
		
		//Check to see if intnt has new foodtruck to map
		if (getIntent().hasExtra("FoodTruck")) {
			
			/*Get serialized food truck*/
			FoodTruck ft = (FoodTruck) getIntent().getExtras().getSerializable("FoodTruck");
			/*Set action fom intent*/
			action = getIntent().getAction();
			
			Log.w("DEBUG","Action requested "+action);

			if(action.equals(FoodTruckReceiver.FOODTRUCK_GETDIRECTIONS)){
				
				/*Start direction activity */
				startActivity(
				   new Intent(android.content.Intent.ACTION_VIEW, 
				             Uri.parse("http://maps.google.com/maps?saddr="+currentLocation+"&daddr="+ft.getLocation())
				   ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}else if(action.equals(FoodTruckReceiver.FOODTRUCK_CALLIT)){
			    
			    /*Start call activity*/
			    startActivity(
			       new Intent(android.content.Intent.ACTION_CALL,
			    		   Uri.parse("tel:+"+ft.getPhone())
			       ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//			}else if(action.equals(FoodTruckReceiver.FOODTRUCK_MAPIT)){
     		}
   		    /*Always add truck to map view*/
			addFoodTruck(ft);
				
		
		}

	}


	public boolean addFoodTruck(FoodTruck ft) {
		
		Geocoder coder = new Geocoder(this);
		List<Address> address;
		String locString = ft.getLocation();

		try {
			/* for testing only */
			if (locString.trim().equals("a")) {

				locString = "2745 Seminary Ave, Oakland, CA";
			} else if (locString.trim().equals("b")) {
				locString = "9416 Wetsand Ct., Gilroy, CA";
			} else if (locString.trim().equals("c")) {
				locString = "525 West Santa Clara St., San Jose, CA";
			} else if (locString.trim().equals("234 Fake Street")) {
				locString = "555 Bailey Ave., San Jose, CA";
			}
			address = coder.getFromLocationName(locString, 1);

			if (address.size() < 0) {
				// System.err.println("Address not found");
				Toast.makeText(this, locString + " - not found.",
						Toast.LENGTH_SHORT).show();
				return false;
			} else {

				addressToUse = address.get(0);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < addressToUse.getMaxAddressLineIndex(); i++) {

					sb.append(addressToUse.getAddressLine(i)).append("\n");
				}
				locationToUse = sb.toString();

//				GeoPoint point1 = new GeoPoint(
//						(int) (addressToUse.getLatitude() /** 1E6*/),
//						(int) (addressToUse.getLongitude() /** 1E6*/));

				Log.w("DEBUG",addressToUse.getLatitude()+","+addressToUse.getLongitude());
				
				// Set up MAP View
				mapView = (MapView) findViewById(R.id.mapview);
				mapView.setBuiltInZoomControls(true);

				// get drawable task image
				Drawable drawable = this.getResources().getDrawable(
						R.drawable.foodtruck);

				// create new location overlay				
				locationItemizedoverlay = new LocationOverlay(drawable,
						ft, addressToUse, this);

				OverlayItem overlayitem1 = new OverlayItem(locationItemizedoverlay.getGeopoint(),
						ft.getName(), "");

				locationItemizedoverlay.addLocationOverlay(overlayitem1);

				// Get list of current Overlays
				List<Overlay> mapOverlays = mapView.getOverlays();

				// Add overlay to array
				mapOverlays.add(locationItemizedoverlay);

				final MapController mapController = mapView.getController();

				mapController.animateTo(locationItemizedoverlay.getGeopoint(),
						new Runnable() {
							public void run() {
								mapController.setZoom(17);
							}
						});
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void setUpViews() {
		// Set task name to add location for
		mapView = (MapView) findViewById(R.id.mapview);
     	mapView.setBuiltInZoomControls(true);

	}
	
	
	/** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

//    public void sayHello(View v) {
//        if (!mBound) return;
//        // Create and send a message to the service, using a supported 'what' value
//        Message msg = Message.obtain(null, RESTService.MSG_SAY_HELLO, 0, 0);
//        try {
//            mService.send(msg);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, RESTService.class), mConnection,
            Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
	

}