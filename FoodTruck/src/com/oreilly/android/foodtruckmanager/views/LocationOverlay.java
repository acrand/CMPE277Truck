package com.oreilly.android.foodtruckmanager.views;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.Contacts.Photo;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.oreilly.android.foodtruckmanager.foodtrucks.FoodTruck;

public class LocationOverlay extends ItemizedOverlay{

	//private Context mContext;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private GeoPoint geopoint;
	private Address address;
	private FoodTruck ft;
	private Context context;

	public LocationOverlay(Drawable defaultMarker, FoodTruck ft, Address addressToUse, Context context) {
		// TODO Auto-generated constructor stub
		super(boundCenterBottom(defaultMarker));
		address = addressToUse;
		geopoint = new GeoPoint(
				(int) (address.getLatitude() * 1E6),
				(int) (address.getLongitude()  * 1E6));
				
		this.ft = ft;
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}



	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	
	@Override
	protected boolean onTap(int index) 
	{
        OverlayItem item = mOverlays.get(index);
          AlertDialog.Builder dialog = new AlertDialog.Builder(context);
          dialog.setTitle(item.getTitle());
          String message = getContactInfo(ft);
          dialog.setMessage(message);
          BitmapDrawable bd = new BitmapDrawable(getContactBitmap(ft));
          dialog.setIcon(bd);
          dialog.show();
	    return true;
	}
	
	public void addLocationOverlay(OverlayItem location){
		mOverlays.add(location);
		populate();
		
	}

	public String getContactInfo(FoodTruck ft)	
	{
	    
	    StringBuffer contactStr = new StringBuffer();
        ContentResolver cr = context.getContentResolver();
        Cursor cCur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        
        HashMap<String, String> contacts = new HashMap<String,String>();
               
        while (cCur.moveToNext()) 
        {
          String id = cCur.getString(cCur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
          String name = cCur.getString(cCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
          
          contacts.put(id, name);
        }
        
        while (pCur.moveToNext()) 
        {
            String id = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
           
            String name = (String)contacts.get(id);
            if(name.equalsIgnoreCase(ft.getName()))
            {
                String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                
              contactStr.append("Name: ");
              contactStr.append(name);
              contactStr.append("\n");
              contactStr.append("Phone: ");
              contactStr.append(phone);
              contactStr.append("\n");
            }
          }
        
        pCur.close();
        cCur.close();

        return contactStr.toString();
	}
	
	   public Bitmap getContactBitmap(FoodTruck ft)  
	    {
	        
	        StringBuffer contactStr = new StringBuffer();
	        ContentResolver cr = context.getContentResolver();
	        Cursor cCur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
	        
	        HashMap<String, String> contacts = new HashMap<String,String>();
            
	        while (cCur.moveToNext()) 
	        {
	          String id = cCur.getString(cCur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
	          String name = cCur.getString(cCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	          
	          contacts.put(id, name);
	        }
	        
	          cCur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	               
	        while (cCur.moveToNext()) 
	        {
	            String id = cCur.getString(cCur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
	            String cid = cCur.getString(cCur.getColumnIndex(ContactsContract.Contacts._ID));
	            String name = (String)contacts.get(id);
	            if(name.equalsIgnoreCase(ft.getName()))
	            {

	                Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Integer.parseInt(cid));
	                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
	                return BitmapFactory.decodeStream(input);
	                //String uri = cCur.getString(cCur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
//	                Uri photoUri = Uri.parse(uri);
//	                   return getPhoto(photoUri);
	            }
	        }
	        
	        
	        pCur.close();
	        cCur.close();

	        return null;
	    }
	
	public Uri getPhotoUri(String id) {
	    try {
	        Cursor cur = context.getContentResolver().query(
	                ContactsContract.Data.CONTENT_URI,
	                null,
	                ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
	                        + ContactsContract.Data.MIMETYPE + "='"
	                        + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
	                null);
	        if (cur != null) {
	            if (!cur.moveToFirst()) {
	                return null; // no photo
	            }
	        } else {
	            return null; // error in cursor process
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
	            .parseLong(id));
	    return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	}
	
	public Bitmap getPhoto(Uri uri){
	    Bitmap photoBitmap = null;

	    String[] projection = new String[] { ContactsContract.Contacts.PHOTO_ID };

	    Cursor cc = context.getContentResolver().query(uri, null, null, null, null);

	    if(cc.moveToFirst()) {
	        final String photoId = cc.getString(cc.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
	        if(photoId != null) {
	            final Cursor photo = context.getContentResolver().query(
	                    ContactsContract.Data.CONTENT_URI,
	                    new String[] {Photo.PHOTO},
	                    Data._ID + "=?",
	                    new String[] {photoId},
	                    null
	            );

	            // Convert photo blob to a bitmap
	            if(photo.moveToFirst()) {
	                byte[] photoBlob = photo.getBlob(photo.getColumnIndex(Photo.PHOTO));
	                photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
	            }

	            photo.close();
	        }

	    } 
	    cc.close();

	    return photoBitmap;
	}

	public GeoPoint getGeopoint() {
		return geopoint;
	}



	public void setGeopoint(GeoPoint geopoint) {
		this.geopoint = geopoint;
	}



	public Address getAddress() {
		return address;
	}



	public void setAddress(Address address) {
		this.address = address;
	}
	
	
	
}
