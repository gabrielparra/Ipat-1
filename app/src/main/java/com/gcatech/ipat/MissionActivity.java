package com.gcatech.ipat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dji.common.camera.DJICameraSettingsDef;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.missionmanager.DJIMission;
import dji.sdk.missionmanager.DJIMissionManager;
import dji.sdk.missionmanager.DJIWaypoint;
import dji.sdk.missionmanager.DJIWaypointMission;
import dji.sdk.products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;
import dji.common.error.DJIError;
import dji.sdk.remotecontroller.DJIRemoteController;
import utils.DJIDialog;

public class MissionActivity extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, DJIMissionManager.MissionProgressStatusCallback, DJICommonCallbacks.DJICompletionCallback {

    protected static final String TAG = "GSDemoActivity";

    private GoogleMap gMap;

    private Button locate, add, clear;
    private Button config, prepare, start, stop, manualMode;

    UtilsSharedCamera utilsCamera = new UtilsSharedCamera();

    private boolean isAdd = false;

    private double droneLocationLat = 181, droneLocationLng = 181;
    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();
    private Marker droneMarker = null;

    private float altitude = 100.0f;
    private float mSpeed = 10.0f;

    private DJIWaypointMission mWaypointMission;
    private DJIMissionManager mMissionManager;
    private DJIFlightController mFlightController;
    private DJIRemoteController mRemoteController;

    private DJIWaypointMission.DJIWaypointMissionFinishedAction mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.NoAction;
    private DJIWaypointMission.DJIWaypointMissionHeadingMode mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.Auto;

    @Override
    protected void onResume() {
        super.onResume();
        initFlightController();
        initMissionManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mReceiver);
    }

    /**
     * @Description : RETURN Button RESPONSE FUNCTION
     */
    public void onReturn(View view) {
        Log.d(TAG, "onReturn");
        this.finish();
    }

    private void setResultToToast(final String string) {
        MissionActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MissionActivity.this, string, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initUI() {

        locate = (Button) findViewById(R.id.locate);
        add = (Button) findViewById(R.id.add);
        clear = (Button) findViewById(R.id.clear);
        config = (Button) findViewById(R.id.config);
        prepare = (Button) findViewById(R.id.prepare);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);


        locate.setOnClickListener(this);
        add.setOnClickListener(this);
        clear.setOnClickListener(this);
        config.setOnClickListener(this);
        prepare.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        UtilsSharedCamera.InMission = false;

        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.configmission);


        /*IntentFilter filter = new IntentFilter();
        filter.addAction(DJIApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
*/
        initUI();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initFlightController();
        initMissionManager();

    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange() {
        initMissionManager();
        initFlightController();
    }

    private void initMissionManager() {
        DJIBaseProduct product = DJIApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            mMissionManager = null;
            return;
        } else {

            mMissionManager = product.getMissionManager();
            mMissionManager.setMissionProgressStatusCallback(this);
            mMissionManager.setMissionExecutionFinishedCallback(this);
        }

        mWaypointMission = new DJIWaypointMission();
    }

    private void initFlightController() {

        DJIBaseProduct product = DJIApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof DJIAircraft) {
                mFlightController = ((DJIAircraft) product).getFlightController();
                mRemoteController = ((DJIAircraft) product).getRemoteController();
            }
        }

        if (mRemoteController == null) {
            ShowMessageRCNotConnected();
        } else {
            if (!mRemoteController.isConnected()) {
                ShowMessageRCNotConnected();
            }
        }


        if (mFlightController != null) {
            if (mFlightController.isConnected()) {
                mFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {

                    @Override
                    public void onResult(DJIFlightControllerCurrentState state) {
                        droneLocationLat = state.getAircraftLocation().getLatitude();
                        droneLocationLng = state.getAircraftLocation().getLongitude();
                        UtilsSharedCamera.droneLocationLat = droneLocationLat;
                        UtilsSharedCamera.droneLocationLng = droneLocationLng;

                        updateDroneLocation();
                        UtilsSharedCamera.UpdatePositionMap();
                    }
                });
            } else {
                ShowMessageRCNotConnected();
            }
        } else {

            ShowMessageRCNotConnected();

        }
    }

    private void ShowMessageRCNotConnected() {

        new AlertDialog.Builder(this)
                .setTitle("Advertencia")
                .setMessage("Para esta misión el control remoto debe estar conectado.")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onBackPressed();
                    }

                })
                .create()
                .show();

    }

    /**
     * DJIMissionManager Delegate Methods
     */
    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus progressStatus) {

    }

    /**
     * DJIMissionManager Delegate Methods
     */
    @Override
    public void onResult(DJIError error) {
        setResultToToast("Execution finished: " + (error == null ? "Success!" : error.getDescription()));
    }

    private void setUpMap() {
        gMap.setOnMapClickListener(this);// add the listener for click for amap object

    }

    @Override
    public void onMapClick(LatLng point) {
        if (isAdd == true) {
            markWaypoint(point);
            DJIWaypoint mWaypoint = new DJIWaypoint(point.latitude, point.longitude, altitude);
            //Add Waypoints to Waypoint arraylist;
            if (mWaypointMission != null) {
                mWaypointMission.addWaypoint(mWaypoint);
            }
        } else {
            setResultToToast("Cannot Add Waypoint");
        }
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation() {

        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = gMap.addMarker(markerOptions);
                }
            }
        });
    }

    private void markWaypoint(LatLng point) {
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = gMap.addMarker(markerOptions);
        mMarkers.put(mMarkers.size(), marker);

        UtilsSharedCamera.pointMarkerMap = point;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate: {
                updateDroneLocation();
                cameraUpdate(); // Locate the drone's place
                break;
            }
            case R.id.add: {
                enableDisableAdd();
                break;
            }
            case R.id.clear: {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gMap.clear();
                    }

                });
                if (mWaypointMission != null) {
                    mWaypointMission.removeAllWaypoints(); // Remove all the waypoints added to the task
                }
                break;
            }
            case R.id.config: {
                showSettingDialog();
                break;
            }
            case R.id.prepare: {
                prepareWayPointMission();
                break;
            }
            case R.id.start: {
                UtilsSharedCamera.missionName = ((TextView) findViewById(R.id.txtNombreMision)).getText().toString();
                startWaypointMission();
                break;
            }
            case R.id.stop: {
                stopWaypointMission();
                break;
            }

            default:
                break;
        }
    }

    private void cameraUpdate() {
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        gMap.moveCamera(cu);

    }

    private void enableDisableAdd() {
        if (isAdd == false) {
            isAdd = true;
            add.setText("Exit");
        } else {
            isAdd = false;
            add.setText("Add");
        }
    }

    private void showSettingDialog() {
        LinearLayout wayPointSettings = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);

        final TextView wpAltitude_TV = (TextView) wayPointSettings.findViewById(R.id.altitude);
        RadioGroup speed_RG = (RadioGroup) wayPointSettings.findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionAfterFinished);
        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.heading);

        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.lowSpeed) {
                    mSpeed = 3.0f;
                } else if (checkedId == R.id.MidSpeed) {
                    mSpeed = 5.0f;
                } else if (checkedId == R.id.HighSpeed) {
                    mSpeed = 10.0f;
                }
            }

        });

        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select finish action");
                if (checkedId == R.id.finishNone) {
                    mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.NoAction;
                } else if (checkedId == R.id.finishGoHome) {
                    mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.GoHome;
                } else if (checkedId == R.id.finishAutoLanding) {
                    mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.AutoLand;
                } else if (checkedId == R.id.finishToFirst) {
                    mFinishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.GoFirstWaypoint;
                }
            }
        });

        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select heading");

                mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.UsingWaypointHeading;
                /*if (checkedId == R.id.headingNext) {
                    mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.Auto;
                } else if (checkedId == R.id.headingInitDirec) {
                    mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.UsingInitialDirection;
                } else if (checkedId == R.id.headingRC) {
                    mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.ControlByRemoteController;
                } else if (checkedId == R.id.headingWP) {
                    mHeadingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.UsingWaypointHeading;
                }*/
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(wayPointSettings)
                .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String altitudeString = wpAltitude_TV.getText().toString();
                        altitude = Integer.parseInt(nulltoIntegerDefalt(altitudeString));
                        Log.e(TAG, "altitude " + altitude);
                        Log.e(TAG, "speed " + mSpeed);
                        Log.e(TAG, "mFinishedAction " + mFinishedAction);
                        Log.e(TAG, "mHeadingMode " + mHeadingMode);
                        configWayPointMission();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                })
                .create()
                .show();
    }

    String nulltoIntegerDefalt(String value) {
        if (!isIntValue(value)) value = "0";
        return value;
    }

    boolean isIntValue(String val) {
        try {
            val = val.replace(" ", "");
            Integer.parseInt(val);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void configWayPointMission() {

        if (mWaypointMission != null) {
            mWaypointMission.finishedAction = mFinishedAction;
            mWaypointMission.headingMode = mHeadingMode;
            mWaypointMission.autoFlightSpeed = mSpeed;

            if (mWaypointMission.waypointsList.size() > 0) {
                for (int i = 0; i < mWaypointMission.waypointsList.size(); i++) {
                    mWaypointMission.getWaypointAtIndex(i).altitude = altitude;
                }

                setResultToToast("Set Waypoint attitude successfully");

            }
        }
    }

    private void prepareWayPointMission() {

        if (mMissionManager != null && mWaypointMission != null) {

            if(mWaypointMission.getWaypointCount()>=2) {
                DJIMission.DJIMissionProgressHandler progressHandler = new DJIMission.DJIMissionProgressHandler() {
                    @Override
                    public void onProgress(DJIMission.DJIProgressType type, float progress) {
                    }
                };

                mMissionManager.prepareMission(mWaypointMission, progressHandler, new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError error) {
                        setResultToToast(error == null ? "Mission Prepare Successfully" : error.getDescription());
                    }
                });
            }else
            {
                Utils.setResultToToast(MissionActivity.this,"Debe agregar por lo menos dos puntos para la misión.");
            }
        }

    }

    private void ShowActivityCamera() {
        MissionActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(MissionActivity.this, ShootSinglePhotoView.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } catch (Exception ex) {
                    setResultToToast("Error : " + ex.getMessage());
                }

            }
        });
    }

    private void startWaypointMission() {

        if (mMissionManager != null) {


            mMissionManager.startMissionExecution(new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {


                    if (error == null) {
                        UtilsSharedCamera.InMission = true;
                        setResultToToast("Misión iniciada correctamente");
                        ShowActivityCamera();

                    } else {
                        new AlertDialog.Builder(MissionActivity.this)
                                .setMessage("Ocurrio un error : "+error.getDescription())
                                .setCancelable(false)
                                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        UtilsSharedCamera.InMission = false;
                                        ShowActivityCamera();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }


                }
            });

            mMissionManager.setMissionProgressStatusCallback(new DJIMissionManager.MissionProgressStatusCallback() {
                @Override
                public void missionProgressStatus(DJIMission.DJIMissionProgressStatus djiMissionProgressStatus) {
                    if (djiMissionProgressStatus instanceof DJIWaypointMission.DJIWaypointMissionStatus) {

                        DJIWaypointMission.DJIWaypointMissionStatus statusMission = ((DJIWaypointMission.DJIWaypointMissionStatus) djiMissionProgressStatus);

                        if (statusMission.isWaypointReached()) {
                            mMissionManager.pauseMissionExecution(new DJICommonCallbacks.DJICompletionCallback() {

                                @Override
                                public void onResult(DJIError djiError) {
                                    try {
                                        //Se debe tomar una foto
                                        DJIApplication.getProductInstance().getCamera().startShootPhoto(
                                                DJICameraSettingsDef.CameraShootPhotoMode.Single,
                                                new DJICommonCallbacks.DJICompletionCallback() {
                                                    @Override
                                                    public void onResult(DJIError djiError) {
                                                        mMissionManager.resumeMissionExecution(new DJICommonCallbacks.DJICompletionCallback() {
                                                            @Override
                                                            public void onResult(DJIError djiError) {

                                                            }
                                                        });
                                                    }
                                                }
                                        );
                                    } catch (Exception ex) {
                                        //    DJIDialog.showDialog(MissionActivity.this,ex.getMessage());
                                    }
                                }
                            });

                        }

                    }
                }
            });

        }
    }

    private void stopWaypointMission() {

        UtilsSharedCamera.ctxUse = this;
        UtilsSharedCamera.missionName = ((TextView) findViewById(R.id.txtNombreMision)).getText().toString();

        if (mMissionManager != null) {
            mMissionManager.stopMissionExecution(new DJICommonCallbacks.DJICompletionCallback() {

                @Override
                public void onResult(DJIError error) {
                    setResultToToast("Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
                    if(error==null)
                    {
                        utilsCamera.FetchLastPhoto();
                    }
                }
            });

            if (mWaypointMission != null) {
                mWaypointMission.removeAllWaypoints();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (gMap == null) {
            gMap = googleMap;
            setUpMap();
        }

        LatLng shenzhen = new LatLng(22.5362, 113.9454);
        gMap.addMarker(new MarkerOptions().position(shenzhen).title("Marker in Shenzhen"));
        gMap.moveCamera(CameraUpdateFactory.newLatLng(shenzhen));
    }

}
