package com.example.googlemap;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.PolyUtil;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Button resetButton, currLocButton;
    private ToggleButton floor1, floor2, switchButton;
    private LinearLayout floorLayout, buttonLayout;
    private GoogleMap mMap;
    private List<String[]> csv;
    private LocationManager locationManager;
    private Marker curMarker;
    private Marker currentMarkerPosition;
    private LatLng curPos, curPin;
    private boolean curLoc;
    private List<LatLng> area = new ArrayList<>();
    private Context cont;
    private boolean inArea = false;
    private GroundOverlayOptions spb1, sp1f;
    private GroundOverlay spb1GO, sp1fGO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        checkPermission();
    }

    private void initUI(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        cont = this;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, locationListener);

        // Buttons
        resetButton = findViewById(R.id.reset);
        currLocButton = findViewById(R.id.currLocation);
        switchButton = findViewById(R.id.switchb);
        floor1 = findViewById(R.id.floor1);
        floor2 = findViewById(R.id.floor2);

        // Layouts
        floorLayout = findViewById(R.id.floorLayout);
        buttonLayout = findViewById(R.id.buttonLayout);

        // Default Gone for additional Layouts
        floorLayout.setVisibility(View.GONE);
        buttonLayout.setVisibility(View.GONE);

        // Bind Onclick
        resetButton.setOnClickListener(reset);
        currLocButton.setOnClickListener(currLocation);
//        floor1.setOnClickListener(showFloor1);
//        floor2.setOnClickListener(showFloor2);
    }
    private void checkPermission() {

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initUI();
        } else {
            String[] permissionArr = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissionArr, 100);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * 스타트업파크 가로 160m 세로 72m
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.googlemap));
        area.add(new LatLng(37.387977, 126.638702));
        area.add(new LatLng(37.387867, 126.638658));
        area.add(new LatLng(37.386612, 126.639560));
        area.add(new LatLng(37.388038, 126.641357));
        area.add(new LatLng(37.388176, 126.641319));
        area.add(new LatLng(37.389047, 126.639990));
        area.add(new LatLng(37.389031, 126.639809));
        area.add(new LatLng(37.387977, 126.638702));

        LatLng startuppark = new LatLng(37.387868, 126.639962);
        //LatLng startupparkb1 = new LatLng(37.387799, 126.639937);
        LatLng startupparkb1 = new LatLng(37.387868, 126.639962);
        LatLng startuppark1f = new LatLng(37.387768, 126.639619);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startuppark, 18));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(startuppark)      // Sets the center of the map to Mountain View
                .zoom(18)                   // Sets the zoom
                .bearing(-50)                // Sets the orientation of the camera to east
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        spb1 = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.spb1))
                .bearing(-50)
                .position(startupparkb1, 166.2f);


        sp1f = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.sp1ft))
                .position(startuppark1f, 125f);


//        try {
//            loadData();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (CsvException e) {
//            throw new RuntimeException(e);
//        }

    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.d("debug","location changed");
            String provider = location.getProvider();  // 위치정보
            double longitudeD = location.getLongitude(); // 위도
            String longitude = String.format("%.6f", longitudeD);
            double latitudeD = location.getLatitude(); // 경도
            String latitude = String.format("%.6f", latitudeD);
            //double altitude = location.getAltitude(); // 고도
            //gpsText.setText("위치정보(Android) : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude);
            double speed = location.getSpeed();
            //speedText.setText("속도(m/s) : " + Math.round(speed) + "\n" + "속도(km/h) : " + Math.round(speed * 3.6) + "\n" + "Tmap속도(m/s) : " + Math.round(speed2) + "\n" + "Tmap속도(km/h) : " + Math.round(speed2 * 3.6));
            //matching(location);
            curPos = new LatLng(latitudeD, longitudeD);

            if(curLoc){
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(curPin)      // Sets the center of the map to Mountain View
                        .zoom(20)                   // Sets the zoom
                        .bearing(-50)                // Sets the orientation of the camera to east
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            if(PolyUtil.containsLocation(curPos, area, true)){
                matching(curPos);
                Log.d("area", "in area");
                if (!inArea) {
                    if(currentMarkerPosition != null){
                        currentMarkerPosition.remove();
                    }
                    floor1.setChecked(true);
                    onToggleClicked1(floor1);
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(cont, R.raw.style_json));
                    floorLayout.setVisibility(View.VISIBLE);
                    buttonLayout.setVisibility(View.VISIBLE);
                    inArea = !inArea;
                }
                //임시로 넣은 코드
                if(currentMarkerPosition != null){
                    currentMarkerPosition.remove();
                }
                currentMarkerPosition = mMap.addMarker(new MarkerOptions().position(curPos).title("current position").icon(BitmapDescriptorFactory.fromResource(R.drawable.redcircle4)));
            } else {
                if(currentMarkerPosition != null){
                    currentMarkerPosition.remove();
                }
                currentMarkerPosition = mMap.addMarker(new MarkerOptions().position(curPos).title("current position").icon(BitmapDescriptorFactory.fromResource(R.drawable.redcircle4)));
                if (inArea) {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(cont, R.raw.googlemap));
                    floorLayout.setVisibility(View.GONE);
                    buttonLayout.setVisibility(View.GONE);

                    if (spb1GO != null) {
                        spb1GO.remove();
                    }
                    if (sp1fGO != null) {
                        sp1fGO.remove();
                    }
                    if(curMarker != null){
                        curMarker.remove();
                    }
                    inArea = !inArea;
                }
            };
        }

        public void onProviderEnabled(String provider) {

        }

        public void onProviderDisabled(String provider) {

        }
    };

//    private final View.OnClickListener showFloor1 = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            sp1fGO = mMap.addGroundOverlay(sp1f);
//            if (spb1GO != null) {
//                spb1GO.remove();
//            }
//        }
//    };
//
//    private final View.OnClickListener showFloor2 = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            spb1GO = mMap.addGroundOverlay(spb1);
//            if (sp1fGO != null) {
//                sp1fGO.remove();
//            }
//        }
//    };

    private final View.OnClickListener reset = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(37.387868, 126.639962))      // Sets the center of the map to Mountain View
                    .zoom(18)                   // Sets the zoom
                    .bearing(-50)                // Sets the orientation of the camera to east
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            curLoc = false;
        }
    };

    private final View.OnClickListener currLocation = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (curPos == null) {
                return;
            }
            if (curPin == null) {
                curPin = curPos;
            }
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(curPin)      // Sets the center of the map to Mountain View
                    .zoom(20)                   // Sets the zoom
                    .bearing(-50)                // Sets the orientation of the camera to east
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            curLoc = true;
        }
    };

    public void switchcsv(View v) throws IOException, CsvException {
        boolean on = ((ToggleButton) v).isChecked();
        if(on) {
            AssetManager assetManager = this.getAssets();
            InputStream inputStream = assetManager.open("startuppark2.csv");
            CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));

            List<String[]> allContent = (List<String[]>) csvReader.readAll();
            csv = allContent;
        } else{
            AssetManager assetManager = this.getAssets();
            InputStream inputStream = assetManager.open("startuppark.csv");
            CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));

            List<String[]> allContent = (List<String[]>) csvReader.readAll();
            csv = allContent;
        }
    }

    public void onToggleClicked1(View v){
        boolean on = ((ToggleButton) v).isChecked();
        if(on) {
            floor2.setChecked(false);
            sp1fGO = mMap.addGroundOverlay(sp1f);
            if (spb1GO != null) {
                spb1GO.remove();
            }
            if(curMarker != null){
                curMarker.remove();
            }
        } else{
            if (sp1fGO != null) {
                sp1fGO.remove();
            }
        }
    }

    public void onToggleClicked2(View v){
        boolean on = ((ToggleButton) v).isChecked();
        if(on) {
            floor1.setChecked(false);
            spb1GO = mMap.addGroundOverlay(spb1);
            if (sp1fGO != null) {
                sp1fGO.remove();
            }
            if(curMarker != null){
                curMarker.remove();
            }
        } else{
            if (spb1GO != null) {
                spb1GO.remove();
            }
        }
    }
//    private void loadData() throws IOException, CsvException {
//        AssetManager assetManager = this.getAssets();
//        InputStream inputStream = assetManager.open("startuppark.csv");
//        CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));
//
//        List<String[]> allContent = (List<String[]>) csvReader.readAll();
//        csv = allContent;
//        int i = 1;
//        for (String content[] : allContent) {
//            LatLng ppos = new LatLng(Double.parseDouble(content[0]), Double.parseDouble(content[1]));
//            mMap.addMarker(new MarkerOptions().position(ppos).icon(BitmapDescriptorFactory.fromResource(R.drawable.cur3)));
//            i++;
//        }
//    }

    private void matching(LatLng location) {
        if(csv == null){
            return;
        }
        for (String[] grid : csv) {
            if (inGrid(location, grid)) {
                if(curMarker != null){
                    curMarker.remove();
                }
                curPin = location;
                curMarker = mMap.addMarker(new MarkerOptions().position(location).title("current position").icon(BitmapDescriptorFactory.fromResource(R.drawable.cur3)));
                Log.d("debug","IN GRID");
                return;
            }
        }
    }

    private boolean inGrid(LatLng location, String[] grid) {
        if (location == null) {
            return false;
        }
        double lati = location.latitude;
        double longi = location.longitude;

        boolean isIn = false;

        if (lati >= Double.parseDouble(grid[2]) && lati <= Double.parseDouble(grid[3]) && longi >= Double.parseDouble(grid[4]) && longi <= Double.parseDouble(grid[5])) {
            isIn = true;
        }

        return isIn;
    }

}