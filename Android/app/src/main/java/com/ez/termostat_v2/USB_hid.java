package com.ez.termostat_v2;



import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by evan on 24.08.2017.
 */
public class USB_hid {
    private UsbManager mUsbManager;
    UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpointIntr;
    private UsbEndpoint mInEndpoint;
    private UsbRequest request_r;

    String tag = "TAG";
    String VIN, PID;
    String status_USB;

    int bufferDataLength;
    HashMap<String, UsbDevice> deviceList;
    Iterator<UsbDevice> deviceIterator;
    UsbDevice dev = null;

    USB_hid(HashMap<String, UsbDevice> _deviceList, Iterator<UsbDevice> _deviceIterator, UsbManager _mUsbManager )
    {
        deviceList =  _deviceList;
        deviceIterator = _deviceIterator;
        mUsbManager = _mUsbManager;
    }
    ////////////////////////////////////////////////////////////////////////
    public boolean search(){
        boolean rez= false;

        //заполняем контейнер списком устройств

        while (deviceIterator.hasNext()) {
            int VINint, PIDint;
            UsbDevice device = (UsbDevice) deviceIterator.next();
            dev = device;
            VINint = device.getVendorId();
            PIDint = device.getProductId();
            VIN = "0x" + Integer.toHexString(VINint);
            PID = "0x" + Integer.toHexString(PIDint); // преобразование int в HEX
            //пример определения ProductID устройства
//            txt.setText(txt.getText() + "\n" + "Device VendorID: " + VIN + "\n" + "Device ProductID: " + PID);
//            tag = "Device VendorID: " + VIN + "\n" + "Device ProductID: " + PID;
        }
        try {
            rez = setDevice(dev);
        } catch (Exception e) {
            //           status_USB = status_USB + "\n" + "Exception e";
            status_USB = "Device don't connected!";
        }
        return rez;
    }
    //////////////////////////////////////////////////
    private boolean setDevice(UsbDevice device) {
        boolean rez = false;
        //определяем доступные интерфейсы устройства
        if (device.getInterfaceCount() != 1) {
            status_USB = status_USB + "\n" + "could not find interface";
//            txt.setText( txt.getText() + "\n" + "could not find interface");
        }
        UsbInterface intf = device.getInterface(0);

        //определяем конечные точки устройства
        if (intf.getEndpointCount() == 0) {
            status_USB = status_USB + "\n" + "could not find endpoint";
//            txt.setText( txt.getText() + "\n" +  "could not find endpoint");
        } else {
//            txt.setText( txt.getText() + "\n" + "Endpoints Count: " + intf.getEndpointCount() );
            status_USB = status_USB + "\n" + "Endpoints Count: " + intf.getEndpointCount();
        }

        UsbEndpoint epIN = null;
        UsbEndpoint epOUT = null;

        //ищем конечные точки для передачи по прерываниям
        for (int i = 0; i < intf.getEndpointCount(); i++) {
            if (intf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                if (intf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                    epIN = intf.getEndpoint(i);
//                    txt.setText( txt.getText() + "\n" + "IN endpoint: " + intf.getEndpoint(i) );
                    status_USB = status_USB + "\n" + "IN endpoint: " + intf.getEndpoint(i);
                } else {
                    epOUT = intf.getEndpoint(i);
//                    txt.setText( txt.getText() + "\n" + "OUT endpoint: " + intf.getEndpoint(i) );
                    status_USB = status_USB + "\n" + "OUT endpoint: " + intf.getEndpoint(i);
                }
            } else {
//                txt.setText( txt.getText() + "\n" + "no endpoints for INTERRUPT_TRANSFER");
                status_USB = status_USB + "\n" + "no endpoints for INTERRUPT_TRANSFER";
            }
        }

        mDevice = device;
        mEndpointIntr = epOUT;
        mInEndpoint = epIN;

        bufferDataLength = mEndpointIntr.getMaxPacketSize();
        status_USB = status_USB + "\n" + "bufferDataLength" + bufferDataLength;

        //открываем устройство для передачи данных
        if (mDevice != null) {
            UsbDeviceConnection connection;
            ;
            //           mConnection = mUsbManager.openDevice(mDevice);
            //           status_USB = status_USB + "\n" + "Connection: " + mConnection;
            connection = mUsbManager.openDevice(mDevice);
            if (connection != null && connection.claimInterface(intf, true)) {

//                txt.setText( txt.getText() + "\n" + "open device SUCCESS!");
                status_USB = status_USB+"\n" + "open device SUCCESS!";
                mConnection = connection;
                rez = true;
            } else {

//                txt.setText( txt.getText() + "\n" + "open device FAIL!");
                status_USB = status_USB+"\n" + "open device FAIL!";
                mConnection = null;
            }

        }
        return rez;
    }
    //////////////////////////////////////////////////
    public boolean read(final byte[] data, final int[] intdata) {
        boolean rez = false;

        ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);

        try {

            UsbRequest request = new UsbRequest();
            request.initialize(mConnection, mInEndpoint);
            request.queue(buffer, bufferDataLength);
            status_USB = "ожидание чтения";
            if (request.equals(mConnection.requestWait()))   // здесь ждем ответ устройства на запрос чтения
            {
                status_USB = status_USB + "\n"+"чтение прошло успешно";
//                txt_dev.setText(buffer.toString());
                buffer.flip();
                buffer.get(data);
//////////////////////////////////////////
                for(int i = 0; i<data.length; i++){
                    intdata[i] = ((int)data[i])& 0xff;    //конвертирование в int и накладывание маски 0xff позволяет избавится от представления числа в отрицательной форме
                }
//////////////////////////////////////////
                rez = true;
                request.close();
            }

        } catch (Exception ex){status_USB = status_USB + "\n"+"чтение не прокатило..."; }
        return rez;
    }
    //////////////////////////////////////////////////
    public boolean send_USB(byte[] buf) {
        boolean rez = false;
        buf[0] = 1;

        try {
            ByteBuffer buffer = ByteBuffer.allocate(bufferDataLength + 1);
            UsbRequest request = new UsbRequest();
            buffer.put(buf);
            request.initialize(mConnection, mEndpointIntr);
            request.queue(buffer, bufferDataLength);
            if (request.equals(mConnection.requestWait()))
            {
                status_USB = "отправка прошла успешно";
                rez = true;
                request.close();
            }
//        Thread.sleep(100);
        } catch (Exception ex){status_USB = "отправка не прокатила..."; }
        return rez;
    }
//////////////////////////////////////////////////
}

