package com.advantech.edgexdeploy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.advantech.edgexdeploy.zxing.android.CaptureActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

public class DeployService extends Activity {
    private TextView tvMac;
    //    private TextView tvKey;
    private IPEditText etIp;
    private EditText etPort;
    private List<DeviceItem> listItems;
    private ListAdapter mAdapter;


    private static final String TAG = "DeployService";
    private static final int REQUEST_CODE_SCAN = 0x0000;
    private static final String DECODED_CONTENT_KEY = "codedContent";
    //    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final String DATA_TITLE = "###ADVDEPLOY###";

    public static final int SEND_PORT = 30000;
    public static final int RECV_PORT = 30001;
    public static final int DATA_LEN = 2048;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deploy);

        ImageView ivScanning = findViewById(R.id.scanning);
        Button btSend = findViewById(R.id.send);
        ListView listView = findViewById(R.id.listview);
//        tvKey = findViewById(R.id.key);
        tvMac = findViewById(R.id.mac);
        etIp = findViewById(R.id.ipedit);
        etPort = findViewById(R.id.port);

        listItems = new ArrayList<>();
        mAdapter = new ListAdapter(this, listItems);
        listView.setAdapter(mAdapter);

        sharedPreferences = getSharedPreferences("privateDate", MODE_PRIVATE);
//        String key = sharedPreferences.getString("key",null);
        String mqttServer = sharedPreferences.getString("mqtt-server", null);
        String mqttPort = sharedPreferences.getString("mqtt-port", null);
        etIp.setText(mqttServer);
        etPort.setText(mqttPort);
//        tvKey.setText(key);


        ivScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(DeployService.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DeployService.this, new String[]{Manifest.permission.CAMERA}, 1);
                    return;
                }
                Intent intent = new Intent(DeployService.this,
                        CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mqttIp = etIp.getText().trim();
                String mqttPort = etPort.getText().toString().trim();
                if (listItems.isEmpty()) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(DeployService.this);
                    dialog.setTitle("Warning");
                    dialog.setMessage("Please scan your device's Mac address first!");
                    dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();

                } else if (mqttIp.isEmpty() || mqttPort.isEmpty()) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(DeployService.this);
                    dialog.setTitle("Warning");
                    dialog.setMessage("Please fill in the IP Address and Port first!");
                    dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();

                } else {
                    Log.d(TAG, mqttIp + " : " + mqttPort);
                    editor = sharedPreferences.edit();
//                    editor.putString("key",tvKey.getText().toString().trim());
                    editor.putString("mqtt-server", mqttIp);
                    editor.putString("mqtt-port", mqttPort);
                    editor.apply();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            ListIterator<DeviceItem> deviceItemListIterator = listItems.listIterator();
                            while (deviceItemListIterator.hasNext()) {
                                DeviceItem deviceItem = deviceItemListIterator.next();
                                if (deviceItem.getIpAddr().isEmpty()) {
                                    deviceItemListIterator.remove();
                                    DeviceItem newDeviceItem = new DeviceItem(deviceItem.getMacAddr(), "", "offline", "failed");
                                    deviceItemListIterator.add(newDeviceItem);
                                } else {
                                    deviceItemListIterator.remove();
                                    DeviceItem newDeviceItem = new DeviceItem(deviceItem.getMacAddr(), deviceItem.getIpAddr(), "online", "failed");
                                    deviceItemListIterator.add(newDeviceItem);
                                    configAIM(DeployService.this, deviceItem.getIpAddr(), etIp.getText(), etPort.getText().toString());
                                }
                            }
                            Message message = new Message();
                            message.what = 0;
                            mHandler.sendMessage(message);
                        }
                    }).start();
                }
            }
        });

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("Operation selection");
                menu.add(0, 0, 0, "Delete");
                menu.add(0, 1, 0, "Reconnect");
                menu.add(0, 2, 0, "Resend");
                menu.add(0, 3, 0, "Details");
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                recvMsg();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "About");
        menu.add(Menu.NONE, 2, Menu.NONE, "Exit");
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                break;
            case 2:
                DeployService.this.finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                final String content = data.getStringExtra(DECODED_CONTENT_KEY);
//                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);
//                image.setImageBitmap(bitmap);
                Log.d(TAG, "content:" + content);
                final String mac = stringToMac(content);
                if (mac != null) {
                    Log.d(TAG, "mac:" + mac);
                    tvMac.setText(mac);

                    ListIterator<DeviceItem> deviceItemListIterator = listItems.listIterator();
                    while (deviceItemListIterator.hasNext()) {
                        DeviceItem deviceItem = deviceItemListIterator.next();
                        if (deviceItem.getMacAddr().equals(mac)) {
                            deviceItemListIterator.remove();
                        }
                    }

                    DeviceItem newDeviceItem = new DeviceItem(mac, "", "offline", "");
                    deviceItemListIterator.add(newDeviceItem);

                    mAdapter.notifyDataSetChanged();


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            detectDevice(DeployService.this, mac);
                        }
                    }).start();
                } else {
                    Toast.makeText(DeployService.this, "Invalid QR code", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                switch (permissions[0]) {
                    case Manifest.permission.CAMERA://权限1
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "You granted the permission", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                }
                break;
            default:
        }
    }

    /*
    request:
    {
	    "type": "detect",
	    "data":{
	    	"mac": 11:22:33:44:55:66"
    	}
    }
    */
    public void detectDevice(Context context, String macAddr) {
        String ip = getIP(context);
        if (ip.isEmpty()) {
            Log.d(TAG, "Unable to get the IP address of the device");
            Toast.makeText(context, "Unable to get the IP address of the device", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "The device IP address is " + ip);
            String broadCastIP = ip.substring(0, ip.lastIndexOf(".")) + "." + "255";
            try {
                JSONObject object = new JSONObject();
                JSONObject object_sub = new JSONObject();
                object.put("type", "detect");
                object_sub.put("mac", macAddr);
                object.put("data", object_sub);
                String data = DATA_TITLE + object.toString();
                Log.d(TAG, "detectDevice sendData:" + data);
                sendMsg(broadCastIP, data, SEND_PORT);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /*
    req:
    {
	    "type": "configAIM",
	    "data":{
		    "mqtt-server":"172.21.73.109",
		    "mqtt-port":"1883"
	    }
    }
     */
    public void configAIM(Context context, String destIp, String mqttService, String mqttPort) {
        if (destIp.isEmpty()) {
            Log.d(TAG, "Unable to get the IP address of the device");
            Toast.makeText(context, "Unable to get the IP address of the device", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "destIp : " + destIp);
            try {
                JSONObject object = new JSONObject();
                JSONObject object_sub = new JSONObject();
                object.put("type", "configAIM");
                //               object.put("key",key);
                object_sub.put("mqtt-server", mqttService);
                object_sub.put("mqtt-port", mqttPort);
                object.put("data", object_sub);
                String data = DATA_TITLE + object.toString();
                Log.d(TAG, "data:" + data);
                sendMsg(destIp, data, SEND_PORT);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public static void sendMsg(String destip, String data, int port) {
        DatagramSocket theSocket = null;
        try {
            InetAddress server = InetAddress.getByName(destip);
            theSocket = new DatagramSocket();
            DatagramPacket theOutput = new DatagramPacket(data.getBytes(), data.getBytes().length, server, port);
            theSocket.send(theOutput);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (theSocket != null)
                theSocket.close();
        }
    }

    public void recvMsg() {
        byte[] buffer = new byte[DATA_LEN];
        DatagramSocket theSocket = null;
        try {
            theSocket = new DatagramSocket(RECV_PORT);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                try {
                    theSocket.receive(packet);
                    String socketData = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                    String packetTitle = socketData.substring(0, 15);
                    String packetDate = socketData.substring(15);
                    Log.d(TAG, "packetTitle :" + packetTitle + ", packetDate : " + packetDate);
                    if (packetTitle.equals(DATA_TITLE)) {
                        try {
                            JSONObject objectRecv;
                            objectRecv = new JSONObject(packetDate);
                            String dataType = objectRecv.getString("type");
                            switch (dataType) {
                                case "detect": {
                                    Log.d(TAG,"detect");
                                    JSONObject objectRecvSub = objectRecv.getJSONObject("data");
                                    String mac = objectRecvSub.getString("mac");
                                    String status = objectRecv.getString("status");
                                    String destIP = packet.getAddress().getHostAddress();
                                    ListIterator<DeviceItem> deviceItemListIterator = listItems.listIterator();
                                    while (deviceItemListIterator.hasNext()) {
                                        DeviceItem deviceItem = deviceItemListIterator.next();
                                        if (deviceItem.getMacAddr().equals(mac)) {
                                            deviceItemListIterator.remove();
                                        }
                                    }
                                    if (status.equals("online")) {
                                        Log.d(TAG,"online");
                                        DeviceItem newDeviceItem = new DeviceItem(mac, destIP, "online", "");
                                        deviceItemListIterator.add(newDeviceItem);
                                    } else {
                                        DeviceItem newDeviceItem = new DeviceItem(mac, destIP, "offline", "");
                                        deviceItemListIterator.add(newDeviceItem);
                                    }

                                    Message message = new Message();
                                    message.what = 0;
                                    mHandler.sendMessage(message);
                                    break;
                                }
                                case "configAIM": {
                                    JSONObject objectRecvSub = objectRecv.getJSONObject("data");
                                    String mac = objectRecvSub.getString("mac");
                                    String status = objectRecv.getString("status");
                                    String destIP = packet.getAddress().getHostAddress();
                                    if (status.equals("ok")) {
                                        ListIterator<DeviceItem> deviceItemListIterator = listItems.listIterator();
                                        while (deviceItemListIterator.hasNext()) {
                                            DeviceItem deviceItem = deviceItemListIterator.next();
                                            if (deviceItem.getMacAddr().equals(mac)) {
                                                deviceItemListIterator.remove();
                                                DeviceItem newDeviceItem = new DeviceItem(mac, destIP, "online", "succeed");
                                                deviceItemListIterator.add(newDeviceItem);
                                            }
                                        }
                                    } else {
                                        ListIterator<DeviceItem> deviceItemListIterator = listItems.listIterator();
                                        while (deviceItemListIterator.hasNext()) {
                                            DeviceItem deviceItem = deviceItemListIterator.next();
                                            if (deviceItem.getMacAddr().equals(mac)) {
                                                deviceItemListIterator.remove();
                                                DeviceItem newDeviceItem = new DeviceItem(mac, destIP, "online", "failed");
                                                deviceItemListIterator.add(newDeviceItem);
                                            }
                                        }
                                    }
                                    Message message = new Message();
                                    message.what = 0;
                                    mHandler.sendMessage(message);
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            break;
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } finally {
            if (theSocket != null)
                theSocket.close();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final DeviceItem deviceItem = (DeviceItem) mAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case 0:                             //Delete
                listItems.remove(info.position);
                mAdapter.notifyDataSetChanged();
                return true;
            case 1:                             //Reconnect
                listItems.remove(info.position);
                mAdapter.notifyDataSetChanged();
                DeviceItem newDeviceItem = new DeviceItem(deviceItem.getMacAddr(), "", "offline", "");
                listItems.add(newDeviceItem);
                mAdapter.notifyDataSetChanged();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        detectDevice(DeployService.this, deviceItem.getMacAddr());
                    }
                }).start();
                return true;
            case 2:                                //Resend
                String mqttIp = etIp.getText().trim();
                String mqttPort = etPort.getText().toString().trim();
                if (listItems.isEmpty()) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(DeployService.this);
                    dialog.setTitle("Warning");
                    dialog.setMessage("Please scan your device's Mac address first!");
                    dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                } else if (mqttIp.isEmpty() || mqttPort.isEmpty()) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(DeployService.this);
                    dialog.setTitle("Warning");
                    dialog.setMessage("Please fill in the IP Address and Port first!");
                    dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();

                } else {
                    Log.d(TAG, mqttIp + " : " + mqttPort);
                    listItems.remove(info.position);
                    mAdapter.notifyDataSetChanged();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (deviceItem.getIpAddr().isEmpty()) {
                                DeviceItem newDeviceItem = new DeviceItem(deviceItem.getMacAddr(), "", "offline", "failed");
                                listItems.add(newDeviceItem);
                            } else {
                                DeviceItem newDeviceItem = new DeviceItem(deviceItem.getMacAddr(), deviceItem.getIpAddr(), "online", "failed");
                                listItems.add(newDeviceItem);
                                configAIM(DeployService.this, deviceItem.getIpAddr(), etIp.getText(), etPort.getText().toString());
                            }

                            Message message = new Message();
                            message.what = 0;
                            mHandler.sendMessage(message);
                        }
                    }).start();
                }
                return true;
            case 3:                                //Details

                final AlertDialog.Builder dialog = new AlertDialog.Builder(DeployService.this);
                dialog.setTitle("Info");
                dialog.setMessage("Mac Address  : " + deviceItem.getMacAddr()+"\n"+
                                  "Ip Address      : " + deviceItem.getIpAddr()+"\n"+
                                  "Status       : " + deviceItem.getStatus()+"\n"+
                                  "Result       : " + deviceItem.getResult());
                dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }



    private String stringToMac(String val) {
        //"[A-Fa-f0-9][A-Fa-f0-9]-[A-Fa-f0-9][A-Fa-f0-9]-[A-Fa-f0-9][A-Fa-f0-9]-[A-Fa-f0-9][A-Fa-f0-9]-[A-Fa-f0-9][A-Fa-f0-9]-[A-Fa-f0-9][A-Fa-f0-9]"
        //"[A-Fa-f0-9]{2}-[A-Fa-f0-9]{2}-[A-Fa-f0-9]{2}-[A-Fa-f0-9]{2}-[A-Fa-f0-9]{2}-[A-Fa-f0-9]{2}"
        //"([A-Fa-f0-9]{2}-){5}[A-Fa-f0-9]{2}"
        String trueMacAddress1 = "^([A-Fa-f0-9]{2}-){5}[A-Fa-f0-9]{2}$";
        String trueMacAddress2 = "^([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}$";
        // 这是真正的MAV地址；正则表达式；
        if (val.length() == 17 && val.matches(trueMacAddress1)) {
            return val.replace("-",":").toUpperCase();
        } else if(val.length() == 17 && val.matches(trueMacAddress2)){
            return val.toUpperCase();
        }else{
            return null;
        }
    }

    public String getIP(Context context) {
        String ip = null;
        ConnectivityManager conMann = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conMann != null) {
            NetworkInfo mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo ethernetNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if (mobileNetworkInfo.isConnected()) {
                ip = getIpAddress("ppp0");
                //    Log.d(TAG,"Mobile IP : " + ip);
            }

            if (wifiNetworkInfo.isConnected()) {
                ip = getIpAddress("wlan0");
                //     Log.d(TAG,"Wifi IP : " + ip);
            }

            if (ethernetNetworkInfo.isConnected()) {
                ip = getIpAddress("eth0");
                //    Log.d(TAG,"Ethernet IP : " + ip);
            }
        }
        return ip;
    }

    private String getIpAddress(String interfaceName) {
        try {
            Enumeration<NetworkInterface> enumerationNetworkInterface = NetworkInterface.getNetworkInterfaces();
            while (enumerationNetworkInterface.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNetworkInterface.nextElement();
                String networkInterfaceName = networkInterface.getDisplayName();
                //       Log.d(TAG, "InterfaceName " + networkInterfaceName);

                if (networkInterfaceName.equals(interfaceName)) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mAdapter.notifyDataSetChanged();
                    break;
                case 2:

                    break;
                case 3:
                    break;
            }
            return false;
        }
    });

}
