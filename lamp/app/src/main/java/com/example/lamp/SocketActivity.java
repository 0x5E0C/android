package com.example.lamp;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class SocketActivity extends Application {

    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket = null;
    private BluetoothDevice device = null;
    private OutputStream outStream = null;
    private BluetoothAdapter bluetoothadapter = BluetoothAdapter.getDefaultAdapter();
    private boolean connect_flag = false;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean connect() {
        try {
            device.createBond();
            socket = this.device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
            connect_flag = true;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            connect_flag = false;
            return false;
        }
    }

    public boolean BluetoothSendMessage(String message) {
        try {
            outStream = socket.getOutputStream();
            if (message.length() > 0) {
                byte[] send = message.getBytes();
                outStream.write(send);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        try {
            socket.close();
            socket = null;
            connect_flag = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothAdapter getDefaultAdapter() {
        return this.bluetoothadapter;
    }

    public BluetoothDevice getConnectedDevice() {
        return this.device;
    }

    public BluetoothSocket getSocket() {
        return this.socket;
    }

    public boolean getConnectState() {
        return connect_flag;
    }
}
