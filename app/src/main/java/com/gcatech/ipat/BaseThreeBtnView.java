package com.gcatech.ipat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJIError;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJILocationCoordinate2D;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.flightcontroller.DJIIntelligentFlightAssistant;
import dji.sdk.mobilerc.DJIMobileRemoteController;
import dji.sdk.products.DJIAircraft;
import utils.DJIDialog;
import utils.OnButtonTouchListenerJoystick;

public abstract class BaseThreeBtnView extends FragmentActivity implements View.OnClickListener, View.OnTouchListener, GoogleMap.OnMapClickListener, OnMapReadyCallback {
    // protected TextView mTexInfo;

    protected GoogleMap gMap;
    protected Marker droneMarker = null;


    protected RelativeLayout layoutRelative;
    protected Button btnRestartAngle;
    protected Button btnDespegar;
    protected Button btnAterrizar;
    protected Button btnAterrizarForzado;

    protected SeekBar barZomm;
    protected SeekBar barAngleCamera;

    protected TextView txtAnguloCamara;
    protected TextView txtZoom;
    protected TextView txtMisionName;


    protected Button JoystickLeft_Up;
    protected Button JoystickLeft_Down;
    protected Button JoystickLeft_Left;
    protected Button JoystickLeft_Right;

    protected Button JoystickRight_Up;
    protected Button JoystickRight_Down;
    protected Button JoystickRight_Left;
    protected Button JoystickRight_Right;
    protected Button btnInitiMissionCross;

    protected ImageView mSendRectIV;

    public static UtilsSharedCamera utilsCamera = new UtilsSharedCamera();

    private DJIMobileRemoteController mobileRemoteController;

    private utils.OnButtonTouchListenerJoystick ListenerJoystickLeft;
    private utils.OnButtonTouchListenerJoystick ListenerJoystickRight;


    /*public BaseThreeBtnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context, attrs);
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();

        DJIIntelligentFlightAssistant assistant = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getIntelligentFlightAssistant();


        assistant.setPrecisionLandingEnabled(false, new DJICommonCallbacks.DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });

        assistant.setActiveObstacleAvoidance(false, new DJICommonCallbacks.DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });

        assistant.setCollisionAvoidanceEnabled(false, new DJICommonCallbacks.DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });

        assistant.setLandingProtectionEnabled(false, new DJICommonCallbacks.DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }

    private void initUI() {
        // LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Service.LAYOUT_INFLATER_SERVICE);

       /* View content = layoutInflater.inflate(R.layout.view_three_btn, null, false);
        addView(content, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
*/
        setContentView(R.layout.view_three_btn);
        if (UtilsSharedCamera.InMission) {
            //Se inicializa el mapa
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        ListenerJoystickLeft = new OnButtonTouchListenerJoystick(true, this);
        ListenerJoystickRight = new OnButtonTouchListenerJoystick(false, this);

        try {

            mobileRemoteController = ((DJIAircraft) DJIApplication.getAircraftInstance()).getMobileRemoteController();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (mobileRemoteController != null) {

            mSendRectIV = (ImageView) findViewById(R.id.tracking_send_rect_iv);

            btnInitiMissionCross = (Button) findViewById(R.id.btnInitMissionCross);
            btnInitiMissionCross.setOnClickListener(this);

            //DJIDialog.showDialog(getContext(),"Dispositivo listo para usar.");
            JoystickLeft_Up = (Button) findViewById(R.id.btnUpControllerLeft);
            JoystickLeft_Up.setOnTouchListener(ListenerJoystickLeft);

            JoystickLeft_Down = (Button) findViewById(R.id.btnDownControllerLeft);
            JoystickLeft_Down.setOnTouchListener(ListenerJoystickLeft);

            /*JoystickLeft_Left = (Button) findViewById(R.id.btnLeftControllerLeft);
            JoystickLeft_Left.setOnTouchListener(ListenerJoystickLeft);

            JoystickLeft_Right = (Button) findViewById(R.id.btnRightControllerLeft);
            JoystickLeft_Right.setOnTouchListener(ListenerJoystickLeft);
            */

            JoystickRight_Up = (Button) findViewById(R.id.btnUpControllerRight);
            JoystickRight_Up.setOnTouchListener(ListenerJoystickRight);

            //JoystickRight_Down = (Button) findViewById(R.id.btnDownControllerRight);
            //JoystickRight_Down.setOnTouchListener(ListenerJoystickRight);

            JoystickRight_Left = (Button) findViewById(R.id.btnLeftControllerRight);
            JoystickRight_Left.setOnTouchListener(ListenerJoystickLeft);

            JoystickRight_Right = (Button) findViewById(R.id.btnRightControllerRight);
            JoystickRight_Right.setOnTouchListener(ListenerJoystickLeft);

            txtMisionName = (TextView) findViewById(R.id.txtNombreMision);

            txtMisionName.setText(UtilsSharedCamera.missionName);

            txtZoom = (TextView) findViewById(R.id.txtZoom);
            barZomm = (SeekBar) findViewById(R.id.barZoom);
            barZomm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            barZomm.setBackgroundColor(Color.argb(((int) (0.8 * 255.0f)), 255, 233, 221));
                            barAngleCamera.setBackgroundColor(Color.argb(((int) (0.8 * 255.0f)), 255, 233, 221));
                        }
                    });


                    getZoomMethod(i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            barAngleCamera = (SeekBar) findViewById(R.id.barAngleCamera);
            barAngleCamera.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            barZomm.setBackgroundColor(Color.argb(((int) (0.8 * 255.0f)), 255, 233, 221));
                            barAngleCamera.setBackgroundColor(Color.argb(((int) (0.8 * 255.0f)), 255, 233, 221));
                        }
                    });
                    getChangeAngleCamera(i);

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


            ((TextureView) findViewById(R.id.texture_video_previewer_surface)).setOnClickListener(this);
            ((TextureView) findViewById(R.id.texture_video_previewer_surface)).setOnTouchListener(this);

            txtAnguloCamara = (TextView) findViewById(R.id.txtAnguloCamara);
            btnRestartAngle = (Button) findViewById(R.id.btnRestartAngle);
            btnDespegar = (Button) findViewById(R.id.btnDespegar);
            btnAterrizar = (Button) findViewById(R.id.btnAterrizar);
            btnAterrizarForzado = (Button) findViewById(R.id.btnAterrizarForzado);

            //mTexInfo.setText(getString(getInfoResourceId()));

            btnRestartAngle.setText("Angulo 0");
            btnDespegar.setText("Despegar");
            btnAterrizar.setText("Aterrizar");
            btnAterrizarForzado.setText("Forzar Aterrizaje");


            btnRestartAngle.setOnClickListener(this);
            btnDespegar.setOnClickListener(this);
            btnAterrizar.setOnClickListener(this);
            btnAterrizarForzado.setOnClickListener(this);


            barZomm.setRotation(90);
            barZomm.setX(430);
            barZomm.setY(475);

            barAngleCamera.setRotation(90);
            barAngleCamera.setX(340);
            barAngleCamera.setY(475);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    barZomm.setBackgroundColor(Color.argb(((int) (0.2 * 255.0f)), 255, 233, 221));
                    barAngleCamera.setBackgroundColor(Color.argb(((int) (0.2 * 255.0f)), 255, 233, 221));
                }
            });
            /*barZomm.getBackground().setAlpha(50);
            barAngleCamera.getBackground().setAlpha(50);*/
          /*  JoystickUp = (Button)findViewById(R.id.btnUpController);
            //JoystickDown = (Button)findViewById(R.id.directionJoystickRight);*/

        } else {
            DJIDialog.showDialog(BaseThreeBtnView.this, "No se logró conectar al dispisitivo");
        }

    }

    public int convertDpToPixels(float dp, Context context) {
        Resources resources = context.getResources();
        return (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
    }

    @Override
    public void onClick(View v) {


        UtilsSharedCamera.missionName = txtMisionName.getText().toString();

        //Se verifica que el nombre de la mision no se encuentre en blanco
        if (TextUtils.isEmpty(utilsCamera.missionName)) {
            Utils.setResultToToastShort(this, "Debe asignar el nombre de la mision primero");
            return;
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                barZomm.setBackgroundColor(Color.argb(((int) (0.2 * 255.0f)), 255, 233, 221));
                barAngleCamera.setBackgroundColor(Color.argb(((int) (0.2 * 255.0f)), 255, 233, 221));
            }
        });
        switch (v.getId()) {
            case R.id.btnRestartAngle:
                getRestartBtnMethod();
                break;

            case R.id.btnDespegar:

                if (UtilsSharedCamera.InMission) {
                    Utils.setResultToToast(this, "En modo misión no se puede usar esta funcionalidad");
                    return;

                }
                DJIApplication.getAircraftInstance().getFlightController().turnOnMotors(new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {

                    }
                });
                break;
            case R.id.btnAterrizar:
                UtilsSharedCamera.InMission = false;
                DJIApplication.getAircraftInstance().getFlightController().autoLanding(new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        //Utils.showDialogBasedOnError(getContext(), djiError);
                    }
                });
                break;
            case R.id.btnAterrizarForzado:
                UtilsSharedCamera.InMission = false;
                DJIApplication.getAircraftInstance().getFlightController().confirmLanding(new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        //Utils.showDialogBasedOnError(getContext(), djiError);
                    }
                });
                break;
            case R.id.btnInitMissionCross:
                showSettingDialog();
                break;
            case R.id.texture_video_previewer_surface:

                break;


        }
    }

    private void InitMissionCross(double metersDistance) {
        btnInitiMissionCross.setText("Cancelar Misión");
        if (UtilsSharedCamera.InMission) {
            UtilsSharedCamera.InMission = false;
            btnInitiMissionCross.setText("Iniciar Misión");
            Utils.setResultToToast(this, "Se canceló la mision correctamente");
            return;

        }
        // btnInitiMissionCross.setText("Iniciar Misión");
        utilsCamera.txtProgressFetchImage = txtZoom;
        utilsCamera.txtUpdateGrades = txtAnguloCamara;
        UtilsSharedCamera.InMission = true;
        utilsCamera.metersMissionCross = metersDistance;
        utilsCamera.InitMissionCross();
    }

    private void showSettingDialog() {
        LinearLayout settingsDistance = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_configdistance, null);

        final TextView txtDistanceConfig = (TextView) settingsDistance.findViewById(R.id.altitude);


        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(settingsDistance)
                .setPositiveButton("Empezar Misión", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String distanceString = txtDistanceConfig.getText().toString();
                        InitMissionCross(Double.parseDouble(distanceString));
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

    float downX;
    float downY;
    boolean isUniqueImage = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (UtilsSharedCamera.InMission) {
            Utils.setResultToToast(this, "En modo misión no se puede usar esta funcionalidad");
            return true;

        }

        UtilsSharedCamera.missionName = txtMisionName.getText().toString();


        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                //Se verifica que el nombre de la mision no se encuentre en blanco
                if (TextUtils.isEmpty(UtilsSharedCamera.missionName)) {
                    Utils.setResultToToastShort(this, "Debe asignar el nombre de la mision primero");
                    return true;
                }

                isUniqueImage = false;
                downX = event.getX();
                downY = event.getY();


                break;
            case MotionEvent.ACTION_MOVE:
                isUniqueImage = true;
                mSendRectIV.setVisibility(View.VISIBLE);
                final int l = (int) (downX < event.getX() ? downX : event.getX());
                final int t = (int) (downY < event.getY() ? downY : event.getY());
                final int r = (int) (downX >= event.getX() ? downX : event.getX());
                final int b = (int) (downY >= event.getY() ? downY : event.getY());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mSendRectIV.setX(l);
                        mSendRectIV.setY(t);
                        mSendRectIV.getLayoutParams().width = r - l;
                        mSendRectIV.getLayoutParams().height = b - t;
                        mSendRectIV.requestLayout();

                    }
                });
                break;

            case MotionEvent.ACTION_UP:
                //Se verifica que el nombre de la mision no se encuentre en blanco
                if (TextUtils.isEmpty(UtilsSharedCamera.missionName)) {
                    Utils.setResultToToastShort(this, "Debe asignar el nombre de la mision primero");
                    return true;
                }

                if (isUniqueImage) {
                    isUniqueImage = false;
                    new AlertDialog.Builder(BaseThreeBtnView.this)
                            .setMessage("Desea recortar la imagen?")
                            .setTitle("Advertencia")
                            .setPositiveButton("Capturar Foto", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {

                                        //utilsCamera.ctxUse = BaseThreeBtnView.this;
                                        utilsCamera.widthCrop = mSendRectIV.getLayoutParams().width;// Math.round(getActiveTrackRect(mSendRectIV).width());//
                                        utilsCamera.heightCrop = mSendRectIV.getLayoutParams().height;//Math.round(getActiveTrackRect(mSendRectIV).height());//
                                        utilsCamera.downXCrop = Math.round(downX);
                                        utilsCamera.downYCrop = Math.round(downY);//Integer.parseInt(String.valueOf(downY).split(".")[0]);
                                        utilsCamera.maxWidth = ((TextureView) findViewById(R.id.texture_video_previewer_surface)).getWidth();
                                        utilsCamera.maxHeight = ((TextureView) findViewById(R.id.texture_video_previewer_surface)).getHeight();
                                        Long tsLong = System.currentTimeMillis() / 1000;
                                        String ts = tsLong.toString();
                                        UtilsSharedCamera.missionName = UtilsSharedCamera.missionName + ts;

                                        if (!utilsCamera.IsCameraBussy()) {

                                            utilsCamera.SetCameraIsBussy(true);

                                            //Shoot Photo Button

                                            DJIApplication.getProductInstance().getCamera().startShootPhoto(
                                                    DJICameraSettingsDef.CameraShootPhotoMode.Single,
                                                    new DJICommonCallbacks.DJICompletionCallback() {
                                                        @Override
                                                        public void onResult(DJIError djiError) {
                                                            try {
                                                                if (null == djiError) {

                                                                    //Se guardan las fotos en el Dispositivo celular o tablet
                                                                    utilsCamera.FetchLastPhoto();

                                                                } else {
                                                                    Utils.setResultToToast(utilsCamera.ctxUse, "Error tomando foto: " + djiError.getDescription());
                                                                }
                                                            } catch (Exception exInter) {
                                                                Utils.setResultToToast(utilsCamera.ctxUse, "Error tomando foto: " + exInter.getMessage());
                                                            }

                                                        }
                                                    }
                                            );


                                        } else {
                                            DJIDialog.showDialog(utilsCamera.ctxUse, R.string.messageIsBusy);
                                        }
                                    } catch (Exception ex) {
                                        Utils.setResultToToast(utilsCamera.ctxUse, "Error Aceptar:" + ex.getMessage());
                                    }

                                }

                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }

                            })
                            .create()
                            .show();

                } else {

                    //utilsCamera.widthCrop = -1;
                    //getClickPhotoMethod();
                }
                break;
            default:
                break;
        }

        return true;
    }

    private void PaintState() {
        final DJIFlightControllerCurrentState state = ((DJIAircraft) DJIApplication.getAircraftInstance()).getFlightController().getCurrentState();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                txtZoom.setText("Position Latitude:" + state.getAircraftLocation().getLatitude() + " Position Longitude:" + state.getAircraftLocation().getLongitude());

            }
        });
    }


    public void SetPositionVerticalDronJoystickLeft(float data) {


        if (mobileRemoteController != null) {
            mobileRemoteController.setLeftStickVertical(data);


        }
    }


    public void SetPositionHorizontalDronJoystickLeft(float data) {

        if (mobileRemoteController != null) {
            mobileRemoteController.setLeftStickHorizontal(data);

        }
    }


    public void SetPositionVerticalDronJoystickRight(float data) {


        if (mobileRemoteController != null) {
            mobileRemoteController.setRightStickVertical(data);


        }
    }

    public void SetPositionHorizontalDronJoystickRight(float data) {


        if (mobileRemoteController != null) {
            mobileRemoteController.setRightStickHorizontal(data);

        }
    }


    protected abstract int getRestartBtnTextResourceId();

    protected abstract int getDecreaseBtnTextResourceId();

    protected abstract int getIncreaseBtnTextResourceId();


    protected abstract int getInfoResourceId();

    protected abstract void getRestartBtnMethod();

    protected abstract void getChangeAngleCamera(int valueMove);

    protected abstract void getClickPhotoMethod();

    protected abstract void getZoomMethod(int valueMove);


}
