package com.gcatech.ipat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import utils.DJIDialog;

import dji.sdk.base.DJIBaseProduct;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJIBluetoothProductConnector;
import dji.thirdparty.eventbus.EventBus;

/**
 * Created by dji on 15/12/18.
 */
public class MainContent extends RelativeLayout implements DJIBaseProduct.DJIVersionCallback {

    public static final String TAG = MainContent.class.getName();

    public MainContent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private TextView mTextConnectionStatus;

    private Button mBtnOpen = new Button(getContext());
    private static boolean connected = false;

    private static DJIBluetoothProductConnector connector = null;

    private Handler mHandler;
    private Handler mHandlerUI;
    private HandlerThread mHandlerThread = new HandlerThread("Bluetooth");


    private DJIBaseProduct mProduct;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initUI();
    }

    private void initUI() {
        Log.v(TAG, "initUI");

        mTextConnectionStatus = (TextView) findViewById(R.id.text_connection_status);

        mBtnOpen = (Button) findViewById(R.id.btn_open);

        mHandlerThread.start();
        final long currentTime = System.currentTimeMillis();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg){
                switch(msg.what){
                    case 0:
                        //connected = DJISampleApplication.getBluetoothConnectStatus();
                    connector = DJIApplication.getBluetoothProductConnector();

                    if(connector != null){

                        return;
                    }else if((System.currentTimeMillis()-currentTime)>=15000){
                        DJIDialog.showDialog(getContext(),"No se logró conectar");
                        return;
                    }else if(connector == null){
                        sendEmptyMessageDelayed(0, 1000);
                    }
                    break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        };
        mHandler.sendEmptyMessage(0);


        mBtnOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

               /* Intent intent = new Intent(getContext(), MissionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);*/


                if (Utils.isFastDoubleClick()) return;
                EventBus.getDefault().post(new SetViewWrapper(R.layout.content_component_list, R.string.activity_component_list, getContext()));
            }
        });
       /* mBtnBluetooth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isFastDoubleClick()) return;
                //EventBus.getDefault().post(new SetViewWrapper(R.layout.content_bluetooth, R.string.component_listview_bluetooth,getContext()));

            }
        });*/
    }

    @Override
    protected void onAttachedToWindow() {
        Log.d(TAG, "Comes into the onAttachedToWindow");
        refreshSDKRelativeUI();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIApplication.FLAG_CONNECTION_CHANGE);
        getContext().registerReceiver(mReceiver, filter);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(mReceiver);
        super.onDetachedFromWindow();
    }



    @Override
    public void onProductVersionChange(String oldVersion, String newVersion) {
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Comes into the BroadcastReceiver");
            refreshSDKRelativeUI();
        }

    };

    int cantidadInentos = 0;
    private void refreshSDKRelativeUI() {
        mProduct = DJIApplication.getProductInstance();
        Log.d(TAG, "mProduct: " + (mProduct == null? "null" : "unnull") );
        if (null != mProduct && mProduct.isConnected()) {
            mBtnOpen.setEnabled(true);

            String str = mProduct instanceof DJIAircraft ? "DJIAircraft" : "DJIHandHeld";
            mTextConnectionStatus.setText("Estado: " + str + " conectado correctamente.");
            mProduct.setDJIVersionCallback(this);


        } else {
            mBtnOpen.setEnabled(false);

        }
    }
}