package com.gactech.admindron;

import android.app.Activity;
import android.os.Handler;

import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.mobilerc.DJIMobileRemoteController;
import dji.sdk.products.DJIAircraft;

/**
 * Created by jjoya on 30/3/2017.
 */

public class MovementsDron {

    private EventExecutionListener listenerExecution;

    public MovementsDron(DJIMobileRemoteController _mobileRemoteController, DJIFlightController _flightController) {
        mobileRemoteController = _mobileRemoteController;
        flightController = _flightController;

    }

    private float quantityMeters;
    private DJIMobileRemoteController mobileRemoteController;
    private DJIFlightController flightController;
    private boolean IsRunning = false;
    public boolean IsEnabled = true;
    float metersTraveled = 0;
    float defaultVelocityMoveFront = 0.5f;

    public double initGrades = 0;
    double numberGradesForRotate = 0;

    public void RotateDronToGrades(double gradesToRotate, EventExecutionListener _listenerExecution) {
        if (!IsRunning) {
            SetPositionVerticalDronJoystickRight(0);

            listenerExecution = _listenerExecution;
            numberGradesForRotate = gradesToRotate;
            IsRunning = true;
            IsEnabled = true;
            //initGrades = flightController.getCompass().getHeading();
            handlerRotate.postDelayed(runnableRotate, 10);
        }

    }

    private Handler handlerRotate = new Handler();
    private Runnable runnableRotate = new Runnable() {
        @Override
        public void run() {
            try {
                if (IsEnabled) {

                    SetPositionHorizontalDronJoystickLeft(0);
                    final double currentGrades = flightController.getCompass().getHeading();

                    final double gradesGo = initGrades <= 0 ? initGrades + numberGradesForRotate : initGrades - numberGradesForRotate;

                    if ((((int) currentGrades) == ((int) gradesGo))) {
                        //Se avisa que ya la ejecucion terminó
                        IsRunning = false;
                        IsEnabled = false;
                        listenerExecution.EndExecute();
                    } else {
                        float multiplier = 1;
                        if (gradesGo < 0) {
                            multiplier = -1;
                        }
                        SetPositionHorizontalDronJoystickLeft((0.32f * multiplier));
                        handlerRotate.postDelayed(runnableRotate, 10);
                    }
                } else {
                    SetPositionHorizontalDronJoystickLeft(0);
                }


            } catch (Exception ex) {
                //Utils.setResultToToast(ctxUse, "Error En runnable runnableInLeftMissionCross: " + ex.getMessage());
            }

        }


    };

    public void MoveFront(float _quantityMeters, EventExecutionListener _listenerExecution) {
        if (!IsRunning) {
            SetPositionHorizontalDronJoystickLeft(0);

            listenerExecution = _listenerExecution;
            quantityMeters = _quantityMeters;
            IsRunning = true;
            IsEnabled = true;

            metersTraveled = 0;
            handlerMoveTofront.postDelayed(runnableMoveToFront, 900);
            SetPositionVerticalDronJoystickRight(defaultVelocityMoveFront);
        }
    }

    private Handler handlerMoveTofront = new Handler();
    private Runnable runnableMoveToFront = new Runnable() {
        @Override
        public void run() {
            if (IsEnabled) {
                if (metersTraveled >= quantityMeters) {
                    IsEnabled = false;
                    IsRunning = false;
                    //Se detiene el Dron
                    SetPositionVerticalDronJoystickRight(0);
                    //Se avisa que ya termino el movimiento solicitado.
                    listenerExecution.EndExecute();
                } else {
                    //Se aumenta en medio metro cada segundo
                    metersTraveled += defaultVelocityMoveFront;
                    handlerMoveTofront.postDelayed(runnableMoveToFront, 900);
                }
            } else {
                //Se detiene el dron si se solicita cancelar el movimiento
                SetPositionVerticalDronJoystickRight(0);

            }

        }
    };

    private void SetPositionVerticalDronJoystickRight(float data) {
        if (mobileRemoteController != null) {
            mobileRemoteController.setRightStickVertical(data);
        }
    }

    public void SetPositionHorizontalDronJoystickLeft(float data) {
        if (mobileRemoteController != null) {
            mobileRemoteController.setLeftStickHorizontal(data);
        }
    }

}


