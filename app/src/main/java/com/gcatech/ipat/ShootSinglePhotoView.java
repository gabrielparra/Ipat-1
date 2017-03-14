package com.gcatech.ipat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gcatech.ipat.DJIApplication;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;

import dji.common.gimbal.DJIGimbalAngleRotation;
import dji.common.gimbal.DJIGimbalCapabilityKey;
import dji.common.gimbal.DJIGimbalRotateAngleMode;
import dji.common.gimbal.DJIGimbalRotateDirection;
import dji.common.util.DJIParamCapability;
import dji.common.util.DJIParamMinMaxCapability;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.camera.DJICamera;
import dji.sdk.camera.DJIMediaManager;
import dji.sdk.gimbal.DJIGimbal;
import dji.sdk.sdkmanager.DJISDKManager;
import utils.DJIDialog;

import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJIError;
import dji.common.util.DJICommonCallbacks;

/**
 * Class for shooting single photo.
 */
public class ShootSinglePhotoView extends BaseThreeBtnView {
    private DJICameraSettingsDef.CameraMode mCameraMode;

    private static final int ENABLE_LEFT_BTN = 0;
    private static final int DISABLE_LEFT_BTN = 1;

    private DJIGimbalAngleRotation mPitchRotation;
    private static DJIBaseProduct mProduct = null;

    Context ctxUsage = this;

    @Override
    public void onMapClick(LatLng point) {

    }
    boolean isMarkerBlueAdded = false;
    private void markWaypoint() {
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(UtilsSharedCamera.pointMarkerMap);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        gMap.addMarker(markerOptions);
        //mMarkers.put(mMarkers.size(), marker);
    }
    private void cameraUpdate() {
        LatLng pos = new LatLng(UtilsSharedCamera.droneLocationLat, UtilsSharedCamera.droneLocationLng);
        float zoomlevel = (float) 20.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        gMap.moveCamera(cu);

        if(!isMarkerBlueAdded && UtilsSharedCamera.pointMarkerMap!=null)
        {
            isMarkerBlueAdded = true;
            markWaypoint();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (gMap == null) {
            gMap = googleMap;
            //setUpMap();
        }

        //LatLng shenzhen = new LatLng(22.5362, 113.9454);
        //gMap.addMarker(new MarkerOptions().position(shenzhen).title("Marker in Shenzhen"));
        // gMap.moveCamera(CameraUpdateFactory.newLatLng(shenzhen));
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
    public void updateDroneLocation() {
        try {
            if (gMap != null) {
                LatLng pos = new LatLng(UtilsSharedCamera.droneLocationLat, UtilsSharedCamera.droneLocationLng);
                //Create MarkerOptions object
                final MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(pos);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (droneMarker != null) {
                            droneMarker.remove();
                        }

                        if (checkGpsCoordination(UtilsSharedCamera.droneLocationLat, UtilsSharedCamera.droneLocationLng)) {
                            droneMarker = gMap.addMarker(markerOptions);
                        }
                    }
                });

                cameraUpdate();
            }
        } catch (Exception ex) {
            Utils.setResultToToast(this, "Error Map:" + ex.getMessage());
        }
    }


    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case ENABLE_LEFT_BTN:
                    //leftBtn.setEnabled(true);
                    break;

                case DISABLE_LEFT_BTN:
                    //leftBtn.setEnabled(false);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    private DJIGimbal getGimbalInstance() {
        return getProductInstance().getGimbal();
    }

    private DJICamera getCameraInstance() {
        return getProductInstance().getCamera();
    }


    public synchronized DJIBaseProduct getProductInstance() {
        while (null == mProduct) {
            if (DJISDKManager.getInstance() != null) {
                mProduct = DJISDKManager.getInstance().getDJIProduct();
            }
        }
        return mProduct;
    }


    /*
   * Check if The Gimbal Capability is supported
   */
    private boolean isFeatureSupported(DJIGimbalCapabilityKey key) {

        DJIGimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return false;
        }

        DJIParamCapability capability = gimbal.gimbalCapability().get(key);
        if (capability != null) {
            return capability.isSuppported();
        }
        return false;
    }

    private void enablePitchExtensionIfPossible() {

        DJIGimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return;
        }
        boolean ifPossible = isFeatureSupported(DJIGimbalCapabilityKey.PitchRangeExtension);
        if (ifPossible) {
            gimbal.setPitchRangeExtensionEnabled(true,
                    new DJICommonCallbacks.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                Log.d("PitchRangeExtension", "set PitchRangeExtension successfully");
                            } else {
                                Log.d("PitchRangeExtension", "set PitchRangeExtension failed");
                            }
                        }
                    }

            );
        }

    }

    private void InitBarAngleCamera() {
        DJIGimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return;
        }

        gimbal.setCompletionTimeForControlAngleAction(1);

        Object key = DJIGimbalCapabilityKey.AdjustPitch;
        Number maxValue = ((DJIParamMinMaxCapability) (gimbal.gimbalCapability().get(key))).getMax();
        Number minValue = ((DJIParamMinMaxCapability) (gimbal.gimbalCapability().get(key))).getMin();
        int valueMax = maxValue.intValue() + (minValue.intValue() * -1);
        barAngleCamera.setMax(valueMax);
        //barAngleCamera.setProgress(valueMax - maxValue.intValue());
        barAngleCamera.setProgress(minValue.intValue());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        UtilsSharedCamera.ctxUse = ShootSinglePhotoView.this;
        if (UtilsSharedCamera.InMission) {
            findViewById(R.id.gridMap).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.gridRemoteControls).setVisibility(View.VISIBLE);
        }

        enablePitchExtensionIfPossible();
        mPitchRotation = new DJIGimbalAngleRotation(false, 0, DJIGimbalRotateDirection.Clockwise);
        mPitchRotation.enable = true;
        InitBarAngleCamera();
        final Activity activity = (Activity) this;
        getCameraInstance().getDigitalZoomScale(new DJICommonCallbacks.DJICompletionCallbackWith<Float>() {
            @Override
            public void onSuccess(final Float aFloat) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtZoom.setText("Zoom actual : " + aFloat);
                    }
                });
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });


        MainActivity.dialogLoading.dismiss();
    }

    public ShootSinglePhotoView() {
        //super(context, attrs);

    }

    /**
     * Every commands relative to the shooting photos are only allowed executed in shootphoto work
     * mode.
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isModuleAvailable()) {
//            DJISampleApplication.getProductInstance().getCamera().getCameraMode(
//                new DJIBaseComponent.DJICompletionCallbackWith<DJICameraSettingsDef.CameraMode>() {
//                    @Override
//                    public void onSuccess(DJICameraSettingsDef.CameraMode cameraMode) {
//                        mCameraMode = cameraMode;
//                    }
//
//                    @Override
//                    public void onFailure(DJIError djiError) {
//
//                    }
//                }
//            );
            DJIApplication.getProductInstance().getCamera().setExposureMode(DJICameraSettingsDef.CameraExposureMode.Program, null);
            utilsCamera.InitCameraPhotoMode();

        }
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (isModuleAvailable()) {
//            DJISampleApplication.getProductInstance().getCamera().setCameraMode(mCameraMode,
//                    new DJIBaseComponent.DJICompletionCallback() {
//                        @Override
//                        public void onResult(DJIError djiError) {
//
//                        }
//                    });
        }
    }

    private boolean isModuleAvailable() {
        return (null != DJIApplication.getProductInstance())
                && (null != DJIApplication.getProductInstance().getCamera());
    }


    @Override
    protected int getInfoResourceId() {
        return R.string.shoot_single_photo_descritpion;
    }

    @Override
    protected void getChangeAngleCamera(int valueMove) {
        rotateAngleCamera(valueMove);
    }

    @Override
    protected void getRestartBtnMethod() {

        mPitchRotation.angle = 0;
        sendRotateGimbalCommand();
        InitBarAngleCamera();

    }


    protected void getClickPhotoMethod() {


        try {


            utilsCamera.txtProgressFetchImage = txtZoom;

            if (!utilsCamera.IsCameraBussy()) {

                utilsCamera.SetCameraIsBussy(true);

                //Shoot Photo Button
                if (isModuleAvailable()) {
                    DJIApplication.getProductInstance().getCamera().startShootPhoto(
                            DJICameraSettingsDef.CameraShootPhotoMode.Single,
                            new DJICommonCallbacks.DJICompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    try {
                                        if (null == djiError) {

                                            //Se guardan las fotos en el Dispositivo celular o tablet
                                            utilsCamera.FetchLastPhoto();

                                        }else
                                        {
                                            Utils.setResultToToast(ShootSinglePhotoView.this, "Error tomando foto: " + djiError.getDescription());
                                        }
                                    } catch (Exception exInter) {
                                        Utils.setResultToToast(ShootSinglePhotoView.this, "Error tomando foto: " + exInter.getMessage());
                                    }

                                }
                            }
                    );

                }
            } else {
                DJIDialog.showDialog(ShootSinglePhotoView.this, R.string.messageIsBusy);
            }
        } catch (Exception ex) {
            Utils.setResultToToast(ShootSinglePhotoView.this, "Error tomando foto: " + ex.getMessage());
        }
    }

    @Override
    protected int getIncreaseBtnTextResourceId() {
        return R.string.increaseAngleCamera;
    }

    @Override
    protected int getRestartBtnTextResourceId() {
        return R.string.restartAngleCamera;
    }

    @Override
    protected int getDecreaseBtnTextResourceId() {
        return R.string.decreaseAngleCamera;
    }

    @Override
    protected synchronized void getZoomMethod(int valueMove) {
        final DJICamera camera = getCameraInstance();


        if (camera == null) {
            return;
        }
        String valueI = "1.";

        if (valueMove < 10) {
            valueI = "1.";
        } else {
            valueMove = valueMove - 10;
            valueI = "2.";
        }


        final float zoom = Float.parseFloat(valueI + String.valueOf(valueMove));

        final Activity activity = (Activity) this;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtZoom.setText("Zoom actual : " + zoom);
            }
        });

        camera.setDigitalZoomScale(zoom, new DJICommonCallbacks.DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });

    }

    private synchronized void rotateAngleCamera(int valueMove) {

        DJIGimbal gimbal = getGimbalInstance();

        if (gimbal == null) {
            return;
        }

        gimbal.setCompletionTimeForControlAngleAction(1);

        Object key = DJIGimbalCapabilityKey.AdjustPitch;

        Number minValue = ((DJIParamMinMaxCapability) (gimbal.gimbalCapability().get(key))).getMin();
        Number maxValue = ((DJIParamMinMaxCapability) (gimbal.gimbalCapability().get(key))).getMax();

        float valueAngle = Float.parseFloat(String.valueOf(valueMove));

        valueAngle -= (minValue.intValue() * -1);

        if (maxValue.floatValue() >= valueAngle && minValue.floatValue() <= valueAngle) {
            mPitchRotation.direction = DJIGimbalRotateDirection.Clockwise;
            mPitchRotation.angle = valueAngle;
        }

        sendRotateGimbalCommand();
    }


    private void sendRotateGimbalCommand() {

        DJIGimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return;
        }

        gimbal.rotateGimbalByAngle(DJIGimbalRotateAngleMode.AbsoluteAngle, mPitchRotation, null, null,
                new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            Log.d("RotateGimbal", "RotateGimbal successfully");
                        } else {
                            Log.d("PitchRangeExtension", "RotateGimbal failed");
                        }
                    }
                }
        );
    }
}
