package com.example.lamp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.lamp.R.layout.timepicker;
import static com.example.lamp.R.layout.brightnesspicker;

public class MainActivity extends AppCompatActivity {

    private TextView showInfoArea;
    private EditText timing_hour;
    private EditText timing_minute;
    private EditText timing_second;
    private EditText brightness;
    private CheckBox is_Enable_Physical_Switch;
    private CheckBox is_Enable_Time_Switch;
    private CheckBox is_Enable_Adjust_Brightness;
    private CheckBox is_Enable_Fun_Function;
    private Button apply_settings;
    private Button change_default_bluetooth;
    private Button open_or_close;
    private Button change_color;
    private Button increase_brightness;
    private Button decrease_brightness;
    private Button change_fun_mode;
    private Button close_fun;
    private String connectedDeviceName;
    private String show_lampstate;
    private String show_brightness;
    private String show_switchstate;
    private String show_funstate;
    private String show_timing;
    private String show_color;
    private String brightness_cache;
    private String saved_mac_addr;
    private String saved_time;
    private boolean timing_state;
    private boolean sync_flag;
    private boolean adjust_brightness_flag;
    private int open_or_close_button_pressed_times;
    private int change_color_button_pressed_times;
    private int increase_brightness_button_pressed_times;
    private int decrease_brightness_button_pressed_times;
    private Menu menu;
    Thread rThread = new BluetoothReceiveThread();
    Fileoperation fileoperation = new Fileoperation();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        adjust_brightness_flag = false;

        showInfoArea = (TextView) findViewById(R.id.textView1);
        timing_hour = (EditText) findViewById(R.id.editText1);
        timing_minute = (EditText) findViewById(R.id.editText2);
        timing_second = (EditText) findViewById(R.id.editText3);
        brightness = (EditText) findViewById(R.id.editText4);

        is_Enable_Physical_Switch = (CheckBox) findViewById(R.id.checkBox1);
        is_Enable_Time_Switch = (CheckBox) findViewById(R.id.checkBox2);
        is_Enable_Adjust_Brightness = (CheckBox) findViewById(R.id.checkBox3);
        is_Enable_Fun_Function = (CheckBox) findViewById(R.id.checkBox4);

        apply_settings = (Button) findViewById(R.id.button1);
        change_default_bluetooth = (Button) findViewById(R.id.button2);
        open_or_close = (Button) findViewById(R.id.button3);
        change_color = (Button) findViewById(R.id.button4);
        increase_brightness = (Button) findViewById(R.id.button5);
        decrease_brightness = (Button) findViewById(R.id.button6);
        change_fun_mode = (Button) findViewById(R.id.button7);
        close_fun = (Button) findViewById(R.id.button8);

        fileoperation.setFilePath(getFilesDir() + "/config.json");
        saved_mac_addr = "null";
        saved_time = "null";
        if (fileoperation.checkFileExists()) {
            if (fileoperation.updateContent()) {
                saved_mac_addr = fileoperation.getJsonValue("mac");
                saved_time = fileoperation.getJsonValue("time");
            } else {
                fileoperation.createEmptyFileContent();
            }
        } else {
            fileoperation.createEmptyFileContent();
        }
        if (!saved_mac_addr.equals("null")) {
            BluetoothAdapter bluetoothadapter = ((SocketActivity) getApplication()).getDefaultAdapter();
            if (!bluetoothadapter.isEnabled()) {
                bluetoothadapter.enable();
            }
            ((SocketActivity) getApplication()).setDevice(bluetoothadapter.getRemoteDevice(saved_mac_addr));
            if (((SocketActivity) getApplication()).connect()) {
                Toast.makeText(MainActivity.this, "已自动连接蓝牙", Toast.LENGTH_SHORT).show();
                sync_flag = false;
                open_or_close_button_pressed_times = 0;
                change_color_button_pressed_times = 0;
                increase_brightness_button_pressed_times = 0;
                decrease_brightness_button_pressed_times = 0;
                ((SocketActivity) getApplication()).BluetoothSendMessage("01010101");
                connectedDeviceName = ((SocketActivity) getApplication()).getConnectedDevice().getName();
                reloadShowInfo();
                rThread.start();
            } else {
                Toast.makeText(MainActivity.this, "自动连接蓝牙失败", Toast.LENGTH_SHORT).show();
            }
        }

        timing_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popDialog(1);
            }
        });

        timing_minute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popDialog(1);
            }
        });

        timing_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popDialog(1);
            }
        });

        brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popDialog(2);
            }
        });

        apply_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SocketActivity) getApplication()).getConnectState()) {
                    String sendsettingmessage = "1";
                    String sendtimingmessage = "";
                    String brightness_content = brightness.getText().toString();
                    String hour_content = timing_hour.getText().toString();
                    String minute_content = timing_minute.getText().toString();
                    String second_content = timing_second.getText().toString();

                    timing_hour.setCursorVisible(false);
                    timing_minute.setCursorVisible(false);
                    timing_second.setCursorVisible(false);
                    brightness.setCursorVisible(false);
                    if ((is_Enable_Adjust_Brightness.isChecked() && Integer.parseInt(brightness_content) > 100) || (is_Enable_Time_Switch.isChecked() && Integer.parseInt(hour_content) >= 100 || Integer.parseInt(minute_content) >= 60 || Integer.parseInt(second_content) >= 60)) {
                        Toast.makeText(MainActivity.this, "输入格式有误!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (is_Enable_Physical_Switch.isChecked()) {
                            sendsettingmessage += "1";
                        } else {
                            sendsettingmessage += "0";
                        }
                        if (is_Enable_Adjust_Brightness.isChecked()) {
                            sendsettingmessage += '1';
                            if (brightness_content.equals("100")) {
                                sendsettingmessage += "100";
                            } else {
                                sendsettingmessage += '0' + brightness_content;
                            }
                        } else {
                            sendsettingmessage += '0';
                            sendsettingmessage += "000";
                        }
                        if (is_Enable_Fun_Function.isChecked()) {
                            sendsettingmessage += '1';
                        } else {
                            sendsettingmessage += '0';
                        }
                        if (is_Enable_Time_Switch.isChecked()) {
                            sendsettingmessage += "1";
                            sendtimingmessage += '3';
                            sendtimingmessage += hour_content;
                            sendtimingmessage += minute_content;
                            sendtimingmessage += second_content;
                            sendtimingmessage += '1';
                            if (!((SocketActivity) getApplication()).BluetoothSendMessage(sendtimingmessage)) {
                                Toast.makeText(MainActivity.this, "设置失败,请重试!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            sendsettingmessage += '0';
                        }
                        ((BluetoothReceiveThread) rThread).setTask(sendsettingmessage);
                        if (!((SocketActivity) getApplication()).BluetoothSendMessage(sendsettingmessage)) {
                            Toast.makeText(MainActivity.this, "设置失败,请重试!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "未连接到蓝牙", Toast.LENGTH_SHORT).show();
                }
            }

        });
        change_default_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_activity = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivityForResult(intent_activity, 1);
            }
        });
        open_or_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SocketActivity) getApplication()).getConnectState()) {
                    ((BluetoothReceiveThread) rThread).setTask("21000007");
                    if (!((SocketActivity) getApplication()).BluetoothSendMessage("21000007")) {
                        Toast.makeText(MainActivity.this, "操作失败,请重试!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "未连接到蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });
        change_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SocketActivity) getApplication()).getConnectState()) {
                    ((BluetoothReceiveThread) rThread).setTask("20100007");
                    if (!((SocketActivity) getApplication()).BluetoothSendMessage("20100007")) {
                        Toast.makeText(MainActivity.this, "操作失败,请重试!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "未连接到蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });
        increase_brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SocketActivity) getApplication()).getConnectState()) {
                    ((BluetoothReceiveThread) rThread).setTask("20010007");
                    if (!((SocketActivity) getApplication()).BluetoothSendMessage("20010007")) {
                        Toast.makeText(MainActivity.this, "操作失败,请重试!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "未连接到蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });
        decrease_brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SocketActivity) getApplication()).getConnectState()) {
                    ((BluetoothReceiveThread) rThread).setTask("20001007");
                    if (!((SocketActivity) getApplication()).BluetoothSendMessage("20001007")) {
                        Toast.makeText(MainActivity.this, "操作失败,请重试!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "未连接到蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });
        change_fun_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SocketActivity) getApplication()).getConnectState()) {
                    ((BluetoothReceiveThread) rThread).setTask("20000107");
                    if (!((SocketActivity) getApplication()).BluetoothSendMessage("20000107")) {
                        Toast.makeText(MainActivity.this, "操作失败,请重试!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "未连接到蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });
        close_fun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((SocketActivity) getApplication()).getConnectState()) {
                    ((BluetoothReceiveThread) rThread).setTask("20000017");
                    if (!((SocketActivity) getApplication()).BluetoothSendMessage("20000017")) {
                        Toast.makeText(MainActivity.this, "操作失败,请重试!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "未连接到蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (((SocketActivity) getApplication()).getConnectedDevice() != null) {
                    sync_flag = false;
                    open_or_close_button_pressed_times = 0;
                    change_color_button_pressed_times = 0;
                    increase_brightness_button_pressed_times = 0;
                    decrease_brightness_button_pressed_times = 0;
                    ((SocketActivity) getApplication()).BluetoothSendMessage("01010101");
                    connectedDeviceName = ((SocketActivity) getApplication()).getConnectedDevice().getName();
                    reloadShowInfo();
                    if (!rThread.isAlive()) {
                        rThread.start();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void reloadShowInfo() {
        showInfoArea.setText("蓝牙:" + connectedDeviceName + "\n状态:" + show_lampstate + "\n亮度:" + show_brightness + "%\n色调:" + show_color + "\n开关:" + show_switchstate + "物理开关\n定时:" + show_timing + "\n风扇:" + show_funstate);
    }

    class BluetoothReceiveThread extends Thread {
        int buff_length = 9;
        private byte[] buff = new byte[buff_length];
        private int pos = 0;
        private String task;

        @Override
        public void run() {
            BluetoothSocket btSocket = ((SocketActivity) getApplication()).getSocket();
            InputStream inStream = null;
            byte[] recbuff = new byte[buff.length];
            while (btSocket != null) {
                btSocket = ((SocketActivity) getApplication()).getSocket();
                try {
                    inStream = btSocket.getInputStream();
                    inStream.read(recbuff);
                    for (int i = 0; i < buff.length; i++) {
                        if (recbuff[i] > '\0') {
                            if (addtobuff(recbuff[i]) == buff.length) {
                                dealData();
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public int addtobuff(byte data) {
            buff[pos] = data;
            if (buff[0] == '4' || buff[0] == '5') {
                pos++;
            } else {
                pos = 0;
                for (int i = 0; i < buff.length; i++) {
                    buff[i] = 0;
                }
            }
            return pos;
        }

        public void dealData() {
            pos = 0;
            if (buff[0] == '4') {
                switch (task.charAt(0)) {
                    case '1':
                        if (task.charAt(1) == '1') {
                            show_switchstate = "启用";
                        } else {
                            show_switchstate = "禁用";
                        }
                        if (task.charAt(2) == '1') {
                            adjust_brightness_flag = true;
                            brightness_cache = brightness.getText().toString();
                        } else {
                            adjust_brightness_flag = false;
                        }
                        if (task.charAt(6) == '1') {
                            show_funstate = "启用";
                        } else {
                            show_funstate = "禁用";
                        }
                        if (task.charAt(7) == '1') {
                            mHandler.sendEmptyMessage(1);
                        }
                        mHandler.sendEmptyMessage(0);
                        break;
                    case '2':
                        switch (this.task) {
                            case "21000007":
                                if (sync_flag == false) {
                                    open_or_close_button_pressed_times += 1;
                                } else {
                                    if (timing_state) {
                                        timing_state = false;
                                        show_timing = "未定时";
                                        mHandler.sendEmptyMessage(2);
                                    }
                                    if (show_lampstate.equals("开")) {
                                        show_lampstate = "关";
                                        show_brightness = "0";
                                    } else {
                                        show_lampstate = "开";
                                        if (adjust_brightness_flag) {
                                            show_brightness = brightness_cache;
                                        } else {
                                            show_brightness = "100";
                                        }
                                    }
                                    mHandler.sendEmptyMessage(0);
                                }
                                break;
                            case "20100007":
                                if (sync_flag == false) {
                                    change_color_button_pressed_times += 1;
                                } else {
                                    if (show_color.equals("冷色")) {
                                        show_color = "暖色";
                                    } else {
                                        show_color = "冷色";
                                    }
                                    mHandler.sendEmptyMessage(0);
                                }
                                break;
                            case "20010007":
                                if (sync_flag == false) {
                                    increase_brightness_button_pressed_times += 1;
                                } else {
                                    if ((Integer.parseInt(show_brightness) + 10) < 100) {
                                        show_brightness = String.valueOf(Integer.parseInt(show_brightness) + 10);
                                    } else {
                                        show_brightness = "100";
                                    }
                                    mHandler.sendEmptyMessage(0);
                                }
                                break;
                            case "20001007":
                                if (sync_flag == false) {
                                    decrease_brightness_button_pressed_times += 1;
                                } else {
                                    if ((Integer.parseInt(show_brightness) - 10) > 0) {
                                        show_brightness = String.valueOf(Integer.parseInt(show_brightness) - 10);
                                    } else {
                                        show_brightness = "0";
                                    }
                                    mHandler.sendEmptyMessage(0);
                                }
                                break;
                            case "20000107":
                            case "20000017":
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
            } else if (buff[0] == '5') {
                sync_flag = true;
                if ((buff[1] - '0' + open_or_close_button_pressed_times) % 2 == 1) {
                    show_lampstate = "开";
                } else {
                    show_lampstate = "关";
                }
                show_brightness = "" + (buff[2] - '0') + (buff[3] - '0') + (buff[4] - '0');
                if ((Integer.parseInt(show_brightness) + 10 * (increase_brightness_button_pressed_times - decrease_brightness_button_pressed_times)) < 100) {
                    show_brightness = String.valueOf(Integer.parseInt(show_brightness) + 10 * (increase_brightness_button_pressed_times - decrease_brightness_button_pressed_times));
                }
                if (buff[5] == '1') {
                    show_switchstate = "启用";
                } else {
                    show_switchstate = "禁用";
                }
                if (buff[6] == '1') {
                    show_funstate = "启用";
                } else {
                    show_funstate = "禁用";
                }
                if ((buff[7] - '0' + change_color_button_pressed_times) % 2 == 1) {
                    show_color = "冷色";
                } else {
                    show_color = "暖色";
                }
                if (buff[8] == '1') {
                    timing_state = true;
                    mHandler.sendEmptyMessage(3);
                } else {
                    timing_state = false;
                    show_timing = "未定时";
                }
                mHandler.sendEmptyMessage(0);
            }
        }

        public void setTask(String task) {
            this.task = task;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msg_case = msg.what;
            if (msg_case == 0) {
                reloadShowInfo();
            } else {
                AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intent_alarm = new Intent("com.example.lamp.ALARM_BROADCAST");
                PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent_alarm, 0);
                IntentFilter filter = new IntentFilter("com.example.lamp.ALARM_BROADCAST");
                if (msg_case == 1) {
                    intent_alarm.putExtra("msg", "");
                    registerReceiver(alarmreceiver, filter);
                    long timing_time = Integer.parseInt(timing_hour.getText().toString()) * 3600 + Integer.parseInt(timing_minute.getText().toString()) * 60 + Integer.parseInt(timing_second.getText().toString());
                    timing_time = System.currentTimeMillis() + timing_time * 1000;
                    Date date = new Date(timing_time);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    show_timing = format.format(date);
                    if (show_lampstate == "开") {
                        show_timing += "关";
                    } else if (show_lampstate == "关") {
                        show_timing += "开";
                    }
                    alarm.set(AlarmManager.RTC_WAKEUP, timing_time, sender);
                    timing_state = true;
                    format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    try {
                        fileoperation.modifyJsonContent("time", format.format(date));
                        fileoperation.saveInfo();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (msg_case == 2) {
                    alarm.cancel(sender);
                    unregisterReceiver(alarmreceiver);
                    timing_state = false;
                } else if (msg_case == 3) {
                    AlarmManager reload_alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent reload_intent_alarm = new Intent("com.example.lamp.ALARM_BROADCAST");
                    PendingIntent reload_sender = PendingIntent.getBroadcast(MainActivity.this, 0, reload_intent_alarm, 0);
                    IntentFilter reload_filter = new IntentFilter("com.example.lamp.ALARM_BROADCAST");
                    registerReceiver(alarmreceiver, reload_filter);
                    String load_time = fileoperation.getJsonValue("time");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date reload_date = new Date();
                    timing_state = true;
                    try {
                        reload_date = dateFormat.parse(load_time);
                        reload_alarm.set(AlarmManager.RTC_WAKEUP, reload_date.getTime(), reload_sender);
                        show_timing = load_time;
                        if (show_lampstate == "开") {
                            show_timing += "关";
                        } else if (show_lampstate == "关") {
                            show_timing += "开";
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public void popDialog(int mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        switch (mode) {
            case 1:
                View timepicker_view = LayoutInflater.from(MainActivity.this).inflate(timepicker, null);
                final NumberPicker hourpicker = timepicker_view.findViewById(R.id.hour_picker);
                final NumberPicker minutepicker = timepicker_view.findViewById(R.id.minute_picker);
                final NumberPicker secondpicker = timepicker_view.findViewById(R.id.second_picker);
                builder.setView(timepicker_view);
                builder.setTitle("设置时间");
                hourpicker.setMaxValue(99);
                hourpicker.setMinValue(0);
                hourpicker.setValue(0);
                minutepicker.setMaxValue(59);
                minutepicker.setMinValue(0);
                minutepicker.setValue(0);
                secondpicker.setMaxValue(59);
                secondpicker.setMinValue(0);
                secondpicker.setValue(0);
                builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (hourpicker.getValue() < 10) {
                            timing_hour.setText('0' + String.valueOf(hourpicker.getValue()));
                        } else {
                            timing_hour.setText(String.valueOf(hourpicker.getValue()));
                        }
                        if (minutepicker.getValue() < 10) {
                            timing_minute.setText('0' + String.valueOf(minutepicker.getValue()));
                        } else {
                            timing_minute.setText(String.valueOf(minutepicker.getValue()));
                        }
                        if (secondpicker.getValue() < 10) {
                            timing_second.setText('0' + String.valueOf(secondpicker.getValue()));
                        } else {
                            timing_second.setText(String.valueOf(secondpicker.getValue()));
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                break;
            case 2:
                View brightnesspicker_view = LayoutInflater.from(MainActivity.this).inflate(brightnesspicker, null);
                final NumberPicker _brightnesspicker = brightnesspicker_view.findViewById(R.id.brightness_picker);
                builder.setView(brightnesspicker_view);
                builder.setTitle("设置亮度");
                _brightnesspicker.setMaxValue(9);
                _brightnesspicker.setMinValue(0);
                _brightnesspicker.setValue(9);
                _brightnesspicker.setDisplayedValues(new String[]{"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"});
                _brightnesspicker.setWrapSelectorWheel(false);
                builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        brightness.setText(String.valueOf(10 * (_brightnesspicker.getValue() + 1)));
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                break;
        }
    }

    private BroadcastReceiver alarmreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.example.lamp.ALARM_BROADCAST")) {
                String msg = intent.getStringExtra("msg");
                timing_state = false;
                if (show_lampstate.equals("开")) {
                    show_lampstate = "关";
                    show_brightness = "0";
                } else {
                    show_lampstate = "开";
                    if (adjust_brightness_flag) {
                        show_brightness = brightness_cache;
                    } else {
                        show_brightness = "100";
                    }
                }
                show_timing = "未定时";
                mHandler.sendEmptyMessage(0);
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        if (saved_mac_addr.equals("null")) {
            menu.findItem(R.id.setAutoConnect).setTitle("默认连接此蓝牙");
        } else {
            menu.findItem(R.id.setAutoConnect).setTitle("取消绑定此蓝牙");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        MenuItem menu_item = menu.findItem(R.id.setAutoConnect);
        String menu_item_text = menu_item.toString();
        //noinspection SimplifiableIfStatement
        if (id == R.id.setAutoConnect) {
            if (menu_item_text.equals("默认连接此蓝牙")) {
                if (((SocketActivity) getApplication()).getConnectedDevice() != null) {
                    saved_mac_addr = ((SocketActivity) getApplication()).getConnectedDevice().getAddress();
                    try {
                        fileoperation.modifyJsonContent("mac", saved_mac_addr);
                        fileoperation.saveInfo();
                        menu_item.setTitle("取消绑定此蓝牙");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "设置失败,请重试!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "蓝牙未连接！", Toast.LENGTH_SHORT).show();
                }
            } else if (menu_item_text.equals("取消绑定此蓝牙")) {
                try {
                    fileoperation.modifyJsonContent("mac", "null");
                    fileoperation.saveInfo();
                    menu_item.setTitle("默认连接此蓝牙");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "设置失败,请重试!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (id == R.id.help) {
            Intent intent_help = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent_help);
        }
        return super.onOptionsItemSelected(item);
    }
}
