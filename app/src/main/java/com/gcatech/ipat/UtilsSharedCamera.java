package com.gcatech.ipat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.RectF;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.gactech.admindron.EventExecutionListener;
import com.gactech.admindron.MovementsDron;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;

import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJICameraError;
import dji.common.error.DJIError;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.camera.DJIMedia;
import dji.sdk.camera.DJIMediaManager;
import dji.sdk.products.DJIAircraft;

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
                                                        djiMediasFetch = djiMedias;
                                                        indexPhotoFetch = 0;

                                                        Utils.setResultToToast(ctxUse, "Se van a descargar las fotos");
                                                        DownloadMedia();
                                                    } else {
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

    ArrayList<DJIMedia> djiMediasFetch;
    int indexPhotoFetch = 0;

    private void DownloadMedia() {

        File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Ipat/");

        if (indexPhotoFetch < djiMediasFetch.size()) {
            lastMedia = djiMediasFetch.get(indexPhotoFetch);
            indexPhotoFetch++;

            if (lastMedia != null) {

                final String fileName = missionName + "__" + indexPhotoFetch;
                final File missionFolder = new File(destDir, missionName);
                if (!missionFolder.exists()) {
                    missionFolder.mkdirs();

                }

                lastMedia.fetchMediaData(missionFolder, fileName, new DJIMediaManager.CameraDownloadListener<String>() {

                    @Override
                    public void onStart() {
                        Log.e("onStart","Entra...");
                    }

                    @Override
                    public void onRateUpdate(long l, long l1, long l2) {

                    }

                    @Override
                    public void onProgress(long l, long l1) {
                        if (IsCameraBussy() && dialogProgress != null) {
                            Utils.setTextProgressDialog(ctxUse, dialogProgress, "Transfiriendo fotos : " + indexPhotoFetch + " / " + djiMediasFetch.size());
                        }
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.e("onSuccess","Entra...");
                        Utils.setResultToToast(ctxUse,s);
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
            DJIApplication.getProductInstance().getCamera().getMediaManager().deleteMedia(djiMediasFetch, new DJICommonCallbacks.DJICompletionCallbackWithTwoParam<ArrayList<DJIMedia>, DJICameraError>() {
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
                    dialogProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    dialogProgress.show();

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
                final double currentGrades = DJIApplication.getAircraftInstance().getFlightController().getCompass().getHeading();
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
                    final double currentGrades = DJIApplication.getAircraftInstance().getFlightController().getCompass().getHeading();

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

                                                if (numberStep == 7) {
                                                    InMission = false;
                                                    Utils.setResultToToast(ctxUse, "Finalizó correctamente la misión.");
                                                    BaseThreeBtnView.utilsCamera.FetchLastPhoto();
                                                } else {
                                                    if (numberStep == 4) {
                                                        initGrades = DJIApplication.getAircraftInstance().getFlightController().getCompass().getHeading();
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

                                        if (numberStep == 7) {
                                            InMission = false;
                                            adminMovements.IsEnabled = false;
                                            Utils.setResultToToast(ctxUse, "Finalizó correctamente la misión.");
                                            BaseThreeBtnView.utilsCamera.FetchLastPhoto();
                                        } else {
                                            if (numberStep == 4) {
                                                final double currentGrades2 = DJIApplication.getAircraftInstance().getFlightController().getCompass().getHeading();
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
                        final double currentGrades = DJIApplication.getAircraftInstance().getFlightController().getCompass().getHeading();
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
                    final double currentGrades = DJIApplication.getAircraftInstance().getFlightController().getCompass().getHeading();
                    adminMovements.initGrades = currentGrades;
                    MoveFront();
                } else {
                    RotateDron(numberGradesForRotate);
                }
            }
        });

    }

    public void InitMissionCross() {
        try {

            if (InMission) {
                adminMovements = new MovementsDron(((BaseThreeBtnView) ctxUse).mobileRemoteController, ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController());
                adminMovements.initGrades = DJIApplication.getAircraftInstance().getFlightController().getCompass().getHeading();

                MoveFront();

            } else {
                Utils.setResultToToast(ctxUse, "No se logró obtener el punto inicial para la misión");
            }
        } catch (Exception ex) {
            Utils.setResultToToast(ctxUse, "Error Mision : " + ex.getMessage());
        }
    }

}
