package com.example.lamp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;


import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class BluetoothActivity extends AppCompatActivity {

    private AnimationDrawable ad;
    private ImageView img_pgbar;
    private TextView tip;
    private TextView paired_device_textview;
    private TextView unpaired_device_textview;
    private RecyclerView paired_list_recycleview;
    private RecyclerView unpaired_list_recycleview;
    private Paired_Adpter paired_adapter;
    private Unaired_Adpter unpaired_adapter;
    private List<BluetoothDevice> paired_device_list;
    private List<BluetoothDevice> unpaired_device_list;
    private List<String> paired_device_name_list;
    private List<String> unpaired_device_name_list;
    private List<String> paired_device_addr_list;
    private List<String> unpaired_device_addr_list;
    private List<String> paired_area_show_list;
    private List<String> unpaired_area_show_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        img_pgbar = (ImageView) findViewById(R.id.imageView);
        tip = (TextView) findViewById(R.id.textView10);
        paired_device_textview = (TextView) findViewById(R.id.textView11);
        unpaired_device_textview = (TextView) findViewById(R.id.textView12);
        paired_adapter = new Paired_Adpter();
        unpaired_adapter = new Unaired_Adpter();
        paired_device_list = new ArrayList<BluetoothDevice>();
        unpaired_device_list = new ArrayList<BluetoothDevice>();
        paired_device_name_list = new ArrayList<String>();
        unpaired_device_name_list = new ArrayList<String>();
        paired_device_addr_list = new ArrayList<String>();
        unpaired_device_addr_list = new ArrayList<String>();
        paired_area_show_list = new ArrayList<String>();
        unpaired_area_show_list = new ArrayList<String>();

        BluetoothAdapter bluetoothadapter = ((SocketActivity) getApplication()).getDefaultAdapter();
        IntentFilter find_device_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter finish_find_filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(event_receiver, find_device_filter);
        registerReceiver(event_receiver, finish_find_filter);
        if (!bluetoothadapter.isEnabled()) {
            bluetoothadapter.enable();
        }
        bluetoothadapter.startDiscovery();

        ad = (AnimationDrawable) img_pgbar.getDrawable();
        img_pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothAdapter bluetoothadapter = ((SocketActivity) getApplication()).getDefaultAdapter();
        bluetoothadapter.cancelDiscovery();
        try {
            unregisterReceiver(event_receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private BroadcastReceiver event_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    paired_device_list.add(device);
                    paired_device_name_list.add(device.getName());
                    paired_device_addr_list.add(device.getAddress());
                } else if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    unpaired_device_list.add(device);
                    unpaired_device_name_list.add(device.getName());
                    unpaired_device_addr_list.add(device.getAddress());
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                img_pgbar.setVisibility(View.GONE);
                tip.setVisibility(View.GONE);
                paired_device_textview.setVisibility(View.VISIBLE);
                unpaired_device_textview.setVisibility(View.VISIBLE);

                for (int i = 0; i < paired_device_name_list.size(); i++) {
                    paired_area_show_list.add(paired_device_name_list.get(i) + "\n" + paired_device_addr_list.get(i) + "\n");
                }
                paired_list_recycleview = (RecyclerView) findViewById(R.id.recyclerView1);
                paired_list_recycleview.setLayoutManager(new LinearLayoutManager(BluetoothActivity.this));
                paired_list_recycleview.setAdapter(paired_adapter);

                for (int i = 0; i < unpaired_device_name_list.size(); i++) {
                    unpaired_area_show_list.add(unpaired_device_name_list.get(i) + "\n" + unpaired_device_addr_list.get(i) + "\n");
                }
                unpaired_list_recycleview = (RecyclerView) findViewById(R.id.recyclerView2);
                unpaired_list_recycleview.setLayoutManager(new LinearLayoutManager(BluetoothActivity.this));
                unpaired_list_recycleview.setAdapter(unpaired_adapter);
            }
        }
    };

    private void openSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    class Paired_Adpter extends RecyclerView.Adapter<Paired_Adpter.ViewHolder> {

        @Override
        public Paired_Adpter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Paired_Adpter.ViewHolder holder = new Paired_Adpter.ViewHolder(LayoutInflater.from(
                    BluetoothActivity.this).inflate(R.layout.recycle_items, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(Paired_Adpter.ViewHolder holder, final int position) {
            holder.tv.setText(paired_area_show_list.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(final View v) {
                    Toast.makeText(BluetoothActivity.this, "蓝牙连接中...", Toast.LENGTH_SHORT).show();
                    BluetoothDevice prepare_connect_device = paired_device_list.get(position);
                    if (((SocketActivity) getApplication()).getConnectedDevice() != null) {
                        if (((SocketActivity) getApplication()).getConnectedDevice().getAddress().equals(prepare_connect_device.getAddress())) {
                            finish();
                            Toast.makeText(BluetoothActivity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                        } else {
                            ((SocketActivity) getApplication()).disconnect();
                            ((SocketActivity) getApplication()).setDevice(prepare_connect_device);
                            if (((SocketActivity) getApplication()).connect()) {
                                unregisterReceiver(event_receiver);
                                finish();
                                Toast.makeText(BluetoothActivity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BluetoothActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        ((SocketActivity) getApplication()).setDevice(prepare_connect_device);
                        if (((SocketActivity) getApplication()).connect()) {
                            unregisterReceiver(event_receiver);
                            finish();
                            Toast.makeText(BluetoothActivity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BluetoothActivity.this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return paired_area_show_list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv;

            public ViewHolder(View view) {
                super(view);
                tv = (TextView) view.findViewById(R.id.id_num);
            }
        }
    }

    class Unaired_Adpter extends RecyclerView.Adapter<Unaired_Adpter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder holder = new ViewHolder(LayoutInflater.from(
                    BluetoothActivity.this).inflate(R.layout.recycle_items, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.tv.setText(unpaired_area_show_list.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                public void onClick(final View v) {
                    openSetting();
                }
            });
        }

        @Override
        public int getItemCount() {
            return unpaired_device_name_list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv;

            public ViewHolder(View view) {
                super(view);
                tv = (TextView) view.findViewById(R.id.id_num);
            }
        }
    }


}
