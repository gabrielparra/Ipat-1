package utils;

import android.view.MotionEvent;
import android.view.View;

import com.gcatech.ipat.BaseThreeBtnView;
import com.gcatech.ipat.R;
import com.gcatech.ipat.Utils;

/**
 * Created by Javier Joya on 27/2/2017.
 */

public class OnButtonTouchListenerJoystick implements View.OnTouchListener {
    private boolean IsJoystickLeft;
    private  String IdButton = "";
    private BaseThreeBtnView ViewButtons;

    public  OnButtonTouchListenerJoystick(boolean _IsJoystickLeft,BaseThreeBtnView _ViewButtons)
    {
        IsJoystickLeft = _IsJoystickLeft;
        ViewButtons = _ViewButtons;
    }

    private  float GetDataPosition(boolean isDown,MotionEvent event)
    {
        float dataPosition = isDown ? -0.5f : 0.5f;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            dataPosition = 0;
        }

        return  dataPosition;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(ViewButtons!=null) {

            float dataPosition = GetDataPosition(false,event);

            if (IsJoystickLeft) {
                switch (v.getId()) {
                    case R.id.btnUpControllerLeft:
                            ViewButtons.SetPositionVerticalDronJoystickLeft(dataPosition);
                        break;
                    case R.id.btnDownControllerLeft:
                            dataPosition = GetDataPosition(true,event);
                            ViewButtons.SetPositionVerticalDronJoystickLeft(dataPosition);
                        break;
                    case R.id.btnLeftControllerRight://R.id.btnLeftControllerLeft:
                        dataPosition = GetDataPosition(true,event);
                        ViewButtons.SetPositionHorizontalDronJoystickLeft(dataPosition);
                        break;
                    case R.id.btnRightControllerRight://R.id.btnRightControllerLeft:
                        ViewButtons.SetPositionHorizontalDronJoystickLeft(dataPosition);
                        break;

                }

            } else

            {
                switch (v.getId()) {
                    case R.id.btnUpControllerRight:
                        ViewButtons.SetPositionVerticalDronJoystickRight(dataPosition);
                        break;
                   /* case R.id.btnDownControllerRight:
                        dataPosition = GetDataPosition(true,event);
                        ViewButtons.SetPositionVerticalDronJoystickRight(dataPosition);
                        break;

                    case R.id.btnLeftControllerRight:
                        dataPosition = GetDataPosition(true,event);
                        ViewButtons.SetPositionHorizontalDronJoystickRight(dataPosition);
                        break;
                    case R.id.btnRightControllerRight:
                        ViewButtons.SetPositionHorizontalDronJoystickRight(dataPosition);
                        break;
                      */

                }
            }
        }else
        {
            Utils.setResultToToast(v.getContext(),"No se logro ver la vista.");
        }
        return true;

    }
}
