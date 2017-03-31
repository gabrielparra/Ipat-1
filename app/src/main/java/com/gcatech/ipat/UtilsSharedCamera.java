package com.gcatech.ipat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import com.gactech.admindron.EventExecutionListener;
import com.gactech.admindron.MovementsDron;
import com.google.android.gms.maps.model.LatLng;

import java.io.*;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJICameraError;
import dji.common.error.DJIError;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJILocationCoordinate2D;
import dji.common.flightcontroller.DJILocationCoordinate3D;
import dji.common.flightcontroller.DJIVirtualStickYawControlMode;
import dji.common.gimbal.DJIGimbalAngleRotation;
import dji.common.gimbal.DJIGimbalCapabilityKey;
import dji.common.gimbal.DJIGimbalControllerMode;
import dji.common.gimbal.DJIGimbalRotateAngleMode;
import dji.common.gimbal.DJIGimbalRotateDirection;
import dji.common.util.DJICommonCallbacks;
import dji.common.util.DJIParamMinMaxCapability;
import dji.log.DJILogHelper;
import dji.sdk.camera.DJIMedia;
import dji.sdk.camera.DJIMediaManager;
import dji.sdk.gimbal.DJIGimbal;
import dji.sdk.products.DJIAircraft;
import dji.sdk.util.Util;
import utils.DJIDialog;

/**
 * Created by jjoya on 4/3/2017.
 */

public class UtilsSharedCamera extends Activity {
    public static boolean InMission = false;
    public static Context ctxUse;
    private boolean cameraIsBussy;

    public static double droneLocationLat;
    public static double droneLocationLng;
    public static LatLng pointMarkerMap;

    public boolean IsCameraBussy() {
        return cameraIsBussy;
    }

    public void SetCameraIsBussy(boolean _cameraIsBussy) {
        cameraIsBussy = _cameraIsBussy;
    }

    public static void UpdatePositionMap() {
        if (ctxUse != null) {
            if (ctxUse instanceof ShootSinglePhotoView) {
                //Utils.setResultToToast(ctxUse,"Si esta ingresando...");
                ((ShootSinglePhotoView) ctxUse).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ShootSinglePhotoView) ctxUse).updateDroneLocation();
                    }
                });


            }
        }
    }

    public synchronized void InitCameraPhotoMode() {
        DJIApplication.getProductInstance().getCamera().setCameraMode(
                DJICameraSettingsDef.CameraMode.ShootPhoto,
                new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {

                    }
                }
        );
    }

    public synchronized void InitCameraFecthInfo() {

    }

    private void CleanResources() {
        widthCrop = 0;
        heightCrop = 0;
        missionName = null;
        lastMedia = null;
        txtProgressFetchImage = null;
        downXCrop = 0;
        downYCrop = 0;
    }

    private int ElpasedTimer = 5500;
    private DJIMedia lastMedia = null;
    public TextView txtProgressFetchImage;
    public static String missionName;
    public int widthCrop;
    public int heightCrop;
    public int downXCrop;
    public int downYCrop;
    public RectF rectCrop;
    public int maxWidth;
    public int maxHeight;

    private Handler savePhotoHandler = new Handler();
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            try {

                DJIApplication.getProductInstance().getCamera().getCameraMode(new DJICommonCallbacks.DJICompletionCallbackWith<DJICameraSettingsDef.CameraMode>() {
                    @Override
                    public void onSuccess(DJICameraSettingsDef.CameraMode cameraMode) {

                        if (cameraMode == DJICameraSettingsDef.CameraMode.MediaDownload) {
                            try {

                                DJIApplication.getProductInstance().getCamera().getMediaManager().fetchMediaList(
                                        new DJIMediaManager.CameraDownloadListener<ArrayList<DJIMedia>>() {
                                            String str;

                                            @Override
                                            public void onStart() {

                                            }

                                            @Override
                                            public void onRateUpdate(long total, long current, long persize) {

                                            }

                                            @Override
                                            public void onProgress(long l, long l1) {

                                            }

                                            @Override
                                            public void onSuccess(final ArrayList<DJIMedia> djiMedias) {
                                                try {
                                                    if (null != djiMedias) {


                                                        /*for (int indexPhoto = 0; indexPhoto < djiMedias.size(); indexPhoto++) {
                                                            if (lastMedia == null)
                                                                lastMedia = djiMedias.get(indexPhoto);
                                                            else {
                                                                if (djiMedias.get(indexPhoto).mTimeCreated > lastMedia.mTimeCreated) {

                                                                    lastMedia = djiMedias.get(indexPhoto);
                                                                }
                                                            }
                                                        }*/

                                                        djiMediasFecth = djiMedias;
                                                        indexPhotoFecth = 0;

                                                        Utils.setResultToToast(ctxUse, "Se van a descargar las fotos");
                                                        //Se intenta descargar la foto
                                                        DownloadMedia();


                                                    } else

                                                    {
                                                        Utils.setResultToToast(ctxUse, "No hay archivos en la SD");
                                                        timerHandler.postDelayed(timerRunnable, ElpasedTimer);
                                                    }
                                                } catch (Exception ex) {
                                                    Utils.setResultToToast(ctxUse, "Error Guardando: " + ex.getMessage());
                                                    timerHandler.postDelayed(timerRunnable, ElpasedTimer);
                                                }
                                            }

                                            @Override
                                            public void onFailure(DJIError djiError) {
                                                timerHandler.postDelayed(timerRunnable, ElpasedTimer);
                                                Utils.setResultToToast(ctxUse, "Error: " + djiError.getDescription());

                                            }
                                        }
                                );
                            } catch (Exception ex) {
                                timerHandler.postDelayed(timerRunnable, ElpasedTimer);
                                Utils.setResultToToast(ctxUse, "Error: " + ex.getMessage());

                            }
                        } else {
                            DJIApplication.getProductInstance().getCamera().getMediaManager().setCameraModeMediaDownload(
                                    new DJICommonCallbacks.DJICompletionCallback() {
                                        @Override
                                        public void onResult(DJIError djiError) {
                                            if (djiError == null) {
                                                Utils.setResultToToast(ctxUse, "Se asigno el modo descarga..");
                                            } else {

                                                Utils.setResultToToast(ctxUse, "Error Asignando estado descarga:" + djiError.getDescription());
                                            }

                                            timerHandler.postDelayed(timerRunnable, ElpasedTimer);
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        Utils.setResultToToast(ctxUse, "Error: " + djiError.getDescription());
                        timerHandler.postDelayed(timerRunnable, ElpasedTimer);
                    }
                });
            } catch (Exception ex) {
                Utils.setResultToToast(ctxUse, "Error GetCameraMode: " + ex.getMessage());
                timerHandler.postDelayed(timerRunnable, ElpasedTimer);
            }
        }
    };

    ArrayList<DJIMedia> djiMediasFecth;
    int indexPhotoFecth = 0;

    private void DownloadMedia() {

        if (indexPhotoFecth < djiMediasFecth.size()) {
            lastMedia = djiMediasFecth.get(indexPhotoFecth);
            indexPhotoFecth++;
            if (lastMedia != null) {

                File destDir = new File(Environment.getExternalStorageDirectory().
                        getPath() + "/DJI_IPAT_" + missionName + "/");

                lastMedia.fetchMediaData(destDir, null, new DJIMediaManager.CameraDownloadListener<String>() {
                    String str;

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onRateUpdate(long l, long l1, long l2) {

                    }

                    @Override
                    public void onProgress(long l, long l1) {
                        //Progreso de la foto
                        if (IsCameraBussy() && dialogProgress != null) {

                            Utils.setTextProgressDialog(ctxUse, dialogProgress, "Transfiriendo fotos : " + indexPhotoFecth + " / " + djiMediasFecth.size());
                        }
                    }

                    @Override
                    public void onSuccess(String s) {
                        //Se Inicializa la camara para que pueda seguir tomando Fotos
                        final String pathFinal = s;


                        //Se verifica si la foto se debe recortar
                        if (widthCrop > 0) {
                            Bitmap imgSource = BitmapFactory.decodeFile(pathFinal);

                            if (imgSource != null) {
                                final android.net.Uri uri = Uri.parse(pathFinal);
                                String urlNewFile = "crop_" + pathFinal.split("/")[pathFinal.split("/").length - 1];
                                Bitmap imgDest = cropBitmap(imgSource);
                                java.io.FileOutputStream out = null;
                                try {
                                    out = new java.io.FileOutputStream(Environment.getExternalStorageDirectory().
                                            getPath() + "/DJI_IPAT_" + missionName + "/" + urlNewFile);
                                    imgDest.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                                    // PNG is a lossless format, the compression factor (100) is ignored
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (out != null) {
                                            out.close();
                                        }
                                    } catch (java.io.IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                //  imgView.setImageBitmap(selectedBitmap);


                            } else {
                                Utils.setResultToToast(ctxUse, "No se encontro la imagen Source");
                            }
                            imgSource.recycle();
                        }

                        DownloadMedia();
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        timerHandler.postDelayed(timerRunnable, ElpasedTimer);
                        Utils.setResultToToast(ctxUse, djiError.getDescription());
                    }
                });


            } else {
                Utils.setResultToToast(ctxUse, "No se pudo obtener la ultima foto");
                timerHandler.postDelayed(timerRunnable, ElpasedTimer);
            }
        } else {
            UtilsSharedCamera.InMission = false;

            //Se deben borrar los archivos de la SD del dron
            DJIApplication.getProductInstance().getCamera().getMediaManager().deleteMedia(djiMediasFecth, new DJICommonCallbacks.DJICompletionCallbackWithTwoParam<ArrayList<DJIMedia>, DJICameraError>() {
                @Override
                public void onSuccess(ArrayList<DJIMedia> djiMedias, DJICameraError djiCameraError) {

                    FinishCaptureImage();
                }

                @Override
                public void onFailure(DJIError djiError) {
                    timerHandler.postDelayed(timerRunnable, 1500);
                }
            });
        }
    }

    public void FinishCaptureImage() {
        CleanResources();
        InitCameraPhotoMode();
        SetCameraIsBussy(false);
        dialogProgress.dismiss();
        dialogProgress = null;
        Utils.setResultToToast(ctxUse, "Se capturó la foto correctamente");
    }

    private Bitmap cropBitmap(Bitmap original) {
       /* Bitmap croppedImage =*/
        // original.setDensity(Bitmap.DENSITY_NONE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) ctxUse).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int heightScreen = maxHeight;//displayMetrics.heightPixels;
        int widthScreen = maxWidth;//displayMetrics.widthPixels;

        int percentageWidth = (widthCrop * 100) / widthScreen;
        int percentageHeight = (heightCrop * 100) / heightScreen;
        int percentageX = (downXCrop * 100) / widthScreen;
        int percentageY = (downYCrop * 100) / heightScreen;

        return Bitmap.createBitmap(original, (original.getWidth() * percentageX) / 100, (original.getHeight() * percentageY) / 100, (original.getWidth() * percentageWidth) / 100, (original.getHeight() * percentageHeight) / 100);
        /*
        Canvas canvas = new Canvas(croppedImage);

        Rect srcRect = new Rect(downXCrop, downYCrop, original.getWidth(), original.getHeight());
        Rect dstRect = new Rect(0, 0, widthCrop, heightCrop);

        int dx = (srcRect.width() - dstRect.width()) / 2;
        int dy = (srcRect.height() - dstRect.height()) / 2;

// If the srcRect is too big, use the center part of it.
        //srcRect.inset(Math.max(0, dx), Math.max(0, dy));

// If the dstRect is too big, use the center part of it.
        //dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

// Draw the cropped bitmap in the center
        canvas.drawBitmap(original, srcRect, dstRect, null);

        original.recycle();

        return croppedImage;
 */
    }

    private ProgressDialog dialogProgress = null;

    public void FetchLastPhoto() {
        ((Activity) ctxUse).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Utils.setResultToToast(ctxUse, "Por favor espere...");
                try {
                    if (dialogProgress != null) {
                        dialogProgress.dismiss();

                    }
                    SetCameraIsBussy(true);


                    dialogProgress = new ProgressDialog(ctxUse);
                    dialogProgress.setTitle("Transferencia");
                    dialogProgress.setMessage("Transfiriendo fotos por favor espere..");
                    dialogProgress.setCancelable(true); // disable dismiss by tapping outside of the dialog
                    dialogProgress.show();


                    //timerRunnable.run();

                } catch (Exception ex) {
                    SetCameraIsBussy(false);
                    Utils.setResultToToast(ctxUse, "Error Fetch: " + ex.getMessage());
                }

            }
        });

        timerHandler.postDelayed(timerRunnable, ElpasedTimer);
    }

    public TextView txtUpdateGrades;
    Handler handlerUpdateGrades = new Handler();
    Runnable runnableUpdateHandler = new Runnable() {
        @Override
        public void run() {
            if (txtUpdateGrades != null) {
                final double currentGrades = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getCompass().getHeading();
                ((Activity) ctxUse).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtUpdateGrades.setText("Grados actuales: " + currentGrades);
                        handlerUpdateGrades.postDelayed(runnableUpdateHandler, 250);
                    }
                });
            }
        }
    };

    private int countRotates = 0;
    private int numberStep = 0;
    private int numberGradesForRotate = 0;

    Handler handlerInLeftMisionCross = new Handler();
    Runnable runnableInLeftMissionCross = new Runnable() {
        @Override
        public void run() {
            try {
                if (InMission) {

                    ((BaseThreeBtnView) ctxUse).SetPositionHorizontalDronJoystickLeft(0);
                    final double currentGrades = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getCompass().getHeading();

                    final double gradesGo = initGrades <= 0 ? initGrades + numberGradesForRotate : initGrades - numberGradesForRotate;


                    ((Activity) ctxUse).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtProgressFetchImage.setText("Act:" + ((int) currentGrades) + " Meta:" + ((int) gradesGo) + " Count Step:" + numberStep);
                        }
                    });

                    if ((((int) currentGrades) == ((int) gradesGo))) {
                        DJIApplication.getProductInstance().getCamera().startShootPhoto(
                                DJICameraSettingsDef.CameraShootPhotoMode.Single,
                                new DJICommonCallbacks.DJICompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        try {
                                            if (null == djiError) {

                                                //((BaseThreeBtnView) ctxUse).SetPositionHorizontalDronJoystickLeft(0);
                                                if (numberStep == 7) {
                                                    InMission = false;
                                                    Utils.setResultToToast(ctxUse, "Finalizó correctamente la misión.");
                                                    BaseThreeBtnView.utilsCamera.FetchLastPhoto();
                                                } else {
                                                    if (numberStep == 4) {
                                                        final double currentGrades2 = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getCompass().getHeading();
                                                        initGrades = currentGrades2;
                                                    }
                                                    MoveFront();
                                                }

                                            } else {
                                                Utils.setResultToToast(ctxUse, "Error tomando foto: " + djiError.getDescription());
                                            }
                                        } catch (Exception exInter) {
                                            Utils.setResultToToast(ctxUse, "Error tomando foto: " + exInter.getMessage());
                                        }

                                    }
                                }
                        );


                    } else {

                        ((BaseThreeBtnView) ctxUse).SetPositionHorizontalDronJoystickLeft(0.3f);
                        handlerInLeftMisionCross.postDelayed(runnableInLeftMissionCross, 10);
                    }
                } else {
                    ((BaseThreeBtnView) ctxUse).SetPositionHorizontalDronJoystickLeft(0);
                }


            } catch (Exception ex) {
                Utils.setResultToToast(ctxUse, "Error En runnable runnableInLeftMissionCross: " + ex.getMessage());
            }

        }


    };

    ArrayList<Integer> listGradesMoveRotate = new ArrayList<Integer>();

    private void RotateDron(int _numberGradesForRotate) {

        adminMovements.RotateDronToGrades(_numberGradesForRotate, new EventExecutionListener() {
            @Override
            public void EndExecute() {
                DJIApplication.getProductInstance().getCamera().startShootPhoto(
                        DJICameraSettingsDef.CameraShootPhotoMode.Single,
                        new DJICommonCallbacks.DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                try {
                                    if (null == djiError) {

                                        //((BaseThreeBtnView) ctxUse).SetPositionHorizontalDronJoystickLeft(0);
                                        if (numberStep == 7) {
                                            InMission = false;
                                            adminMovements.IsEnabled = false;
                                            Utils.setResultToToast(ctxUse, "Finalizó correctamente la misión.");
                                            BaseThreeBtnView.utilsCamera.FetchLastPhoto();
                                        } else {
                                            if (numberStep == 4) {
                                                final double currentGrades2 = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getCompass().getHeading();
                                                adminMovements.initGrades = currentGrades2;
                                            }
                                            MoveFront();
                                        }

                                    } else {
                                        Utils.setResultToToast(ctxUse, "Error tomando foto: " + djiError.getDescription());
                                    }
                                } catch (Exception exInter) {
                                    Utils.setResultToToast(ctxUse, "Error tomando foto: " + exInter.getMessage());
                                }

                            }
                        }
                );
            }
        });

        //((BaseThreeBtnView) ctxUse).SetPositionHorizontalDronJoystickLeft(0.3f);
        //handlerInLeftMisionCross.postDelayed(runnableInLeftMissionCross, 10);


       /* DJIGimbal gimbal = ((DJIAircraft) DJIApplication.getAircraftInstance()).getGimbal();
        DJIGimbalAngleRotation YawRotation = new DJIGimbalAngleRotation(true, _numberGradesForRotate, DJIGimbalRotateDirection.Clockwise);

        Object key = DJIGimbalCapabilityKey.AdjustYaw;

        Utils.setResultToToast(ctxUse,"Modo Yaw:" +  DJIApplication.
                getAircraftInstance().getFlightController().
                getYawControlMode().name());
        DJIApplication.getAircraftInstance().getFlightController().
                setYawControlMode(
                        DJIVirtualStickYawControlMode.Angle
                );
        Utils.setResultToToast(ctxUse,"Modo Yaw:" +  DJIApplication.
                getAircraftInstance().getFlightController().
                getYawControlMode().name());

        final Number minValue = ((DJIParamMinMaxCapability) (gimbal.gimbalCapability().get(key))).getMin();
        final Number maxValue = ((DJIParamMinMaxCapability) (gimbal.gimbalCapability().get(key))).getMax();

        gimbal.setCompletionTimeForControlAngleAction(1.2f);
        gimbal.rotateGimbalByAngle(DJIGimbalRotateAngleMode.AbsoluteAngle, null, null, YawRotation, new DJICommonCallbacks.DJICompletionCallback() {
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    Utils.setResultToToast(ctxUse, "Error Rotando Min:" + minValue + " Max:" + maxValue);
                } else {
                    Utils.setResultToToast(ctxUse, "Se completo la rotacion");
                }
            }
        });
        */

    }

    Handler handlerInfrontMisionCross = new Handler();
    Runnable runnableInFrontMissionCross = new Runnable() {
        @Override
        public void run() {
            if (InMission) {
                if (metersTraveled >= metersMissionCross) {
                    numberStep++;
                    //Se detiene el Dron
                    ((BaseThreeBtnView) ctxUse).SetPositionVerticalDronJoystickRight(0);

                    //Se debe girar el dron a la posicion para devolverse.
                    //numberStep++;
                    numberGradesForRotate = 179;

                    if (numberStep == 4) {
                        numberGradesForRotate = 89;

                    }

                    // if (countRotates == 7) {
                    //   InMission = false;
                    // Utils.setResultToToast(ctxUse, "Finalizó correctamente la misión.");
                    //} else {

                    if (numberStep == 2 || numberStep == 6) {
                        countRotates++;
                        final double currentGrades = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getCompass().getHeading();
                        initGrades = currentGrades;
                        MoveFront();
                    } else {
                        RotateDron(numberGradesForRotate);
                    }
//                    }


                } else {
                    //Se aumenta en medio metro cada segundo

                    metersTraveled += 0.5f;
                    ((Activity) ctxUse).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtProgressFetchImage.setText(metersTraveled + " Metros recorridos __ grados:" + initGrades + " Count Step: " + numberStep);
                        }
                    });
                    handlerInfrontMisionCross.postDelayed(runnableInFrontMissionCross, 900);
                }
            } else {
                ((BaseThreeBtnView) ctxUse).SetPositionVerticalDronJoystickRight(0);
            }

        }
    };


    public float metersMissionCross = 3;
    private double metersTraveled = 0;
    private double initGrades = 0;
    public MovementsDron adminMovements = null;

    private void MoveFront() {


        adminMovements.MoveFront(metersMissionCross, new EventExecutionListener() {
            @Override
            public void EndExecute() {
                numberStep++;
                numberGradesForRotate = 179;

                if (numberStep == 4) {
                    numberGradesForRotate = 89;
                }

                if (numberStep == 2 || numberStep == 6) {
                    countRotates++;
                    final double currentGrades = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getCompass().getHeading();
                    adminMovements.initGrades = currentGrades;
                    MoveFront();
                } else {
                    RotateDron(numberGradesForRotate);
                }
            }
        });


        // metersTraveled = 0;
        //handlerInfrontMisionCross.postDelayed(runnableInFrontMissionCross, 900);
        //((BaseThreeBtnView) ctxUse).SetPositionVerticalDronJoystickRight(0.5f);
    }

    public void InitMissionCross() {
        try {
            handlerUpdateGrades.postDelayed(runnableUpdateHandler, 250);
            // final DJIFlightControllerCurrentState stateDron = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getCurrentState();
            // locationInitDron = stateDron.getAircraftLocation();


            if (InMission) {
                adminMovements = new MovementsDron(((BaseThreeBtnView) ctxUse).mobileRemoteController, ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController());
                adminMovements.initGrades = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getCompass().getHeading();

                MoveFront();

            } else {
                Utils.setResultToToast(ctxUse, "No se logró obtener el punto inicial para la misión");
            }
        } catch (Exception ex) {
            Utils.setResultToToast(ctxUse, "Error Mision : " + ex.getMessage());
        }
    }

}
