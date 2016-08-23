package android.mars.photogallery;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient mClient;
    private static final String TAG = "Maps";
    private ArrayList<Bitmap> mMapImage2;
    private List<GalleryItem> items;
    private Location mCurrentLocation;
    private int mNumbList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mClient = new GoogleApiClient.Builder(mapFragment.getActivity()).addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "Connected!!!");
                        findImage();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    private void findImage() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(TAG, "Got a fix: " + location);
                        new SearchTask().execute(location);
                    }
                });
    }

    private void updateUI() {
        if (mMap == null || mMapImage2 == null) {
            return;
        }

        ArrayList<MarkerOptions> itemMarker2 = new ArrayList<>();
        ArrayList<LatLng> itemPoint2 = new ArrayList<>();
        ArrayList<BitmapDescriptor> itemBitmap2 = new ArrayList<>();

        for (int i = 0; i < mNumbList; i++) {
            itemPoint2.add(new LatLng(items.get(i).getLat(), items.get(i).getLon()));
            itemBitmap2.add(BitmapDescriptorFactory.fromBitmap(mMapImage2.get(i)));

        }

        LatLng myPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        for (int i = 0; i < mNumbList; i++) {
            itemMarker2.add(new MarkerOptions()
                    .title(items.get(i).getCaption())
                    .position(itemPoint2.get(i))
                    .icon(itemBitmap2.get(i)));

        }

        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);

        mMap.clear();

        for (int i = 0; i < mNumbList; i++) {
            mMap.addMarker(itemMarker2.get(i));
        }
        mMap.addMarker(myMarker);


        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(itemPoint2.get(0))
                .include(myPoint)
                .build();

        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);

    }


    private class SearchTask extends AsyncTask<Location, Void, Void> {
        private GalleryItem mGalleryItem;
        private Bitmap mBitmap;
        private Location mLocation;
        private ArrayList<Bitmap> mBitmap2 = new ArrayList<>();
        ProgressDialog pg;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(MapsActivity.this);
            pg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pg.setMessage("Searching...");
            pg.show();
        }

        @Override
        protected Void doInBackground(Location... params) {
            mLocation = params[0];
            FlickrFetchr fetchr = new FlickrFetchr();
            items = fetchr.searchPhotos(params[0]);
            if (items.size() <= 0) {
                return null;
            } else {
                mNumbList = items.size() <= 100 ? items.size() : 100;
            }

            mGalleryItem = items.get(0);
            try {
                for (int i = 0; i < mNumbList; i++) {
                    byte[] bytes2 = fetchr.getUrlBytes(items.get(i).getUrl());
                    mBitmap2.add(BitmapFactory.decodeByteArray(bytes2, 0, bytes2.length));
                }
                byte[] bytes = fetchr.getUrlBytes(mGalleryItem.getUrl());
                mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (IOException ioe) {
                Log.i(TAG, "Unable to download bitmap", ioe);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mMapImage2 = mBitmap2;
            mCurrentLocation = mLocation;
            pg.cancel();
            updateUI();
        }
    }

}
