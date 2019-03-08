package com.advantech.edgexdeployclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class DeployClient extends Service {

    private static final String TAG = "DeployClient";
    private static final int DATA_LEN = 2048;
    private static final byte[] buffer = new byte[DATA_LEN];
    private static final int SEND_PORT = 30001;
    private static final int RECV_PORT = 30000;
    private static final String DATA_TITLE = "###ADVDEPLOY###";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                recvMsg(DeployClient.this,SEND_PORT, RECV_PORT);
            }
        }).start();
    }

    public void recvMsg(Context context, int sendPort, int recvPort) {
        DatagramSocket server = null;
        try {
            server = new DatagramSocket(recvPort);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                try {
                    server.receive(packet);
                    String socketData = new String(packet.getData(), 0, packet.getLength(), "UTF-8");

                    String packetTitle = socketData.substring(0, 15);
                    String packetDate = socketData.substring(15);
                    Log.d(TAG, "packetTitle :" + packetTitle + ", packetDate : " + packetDate);
                    if(packetTitle.equals(DATA_TITLE)){
                        try {
                            JSONObject objectRecv;
                            objectRecv = new JSONObject(packetDate);
                            String dataType = objectRecv.getString("type");
                            JSONObject objectRecvSub = objectRecv.getJSONObject("data");

                            switch (dataType) {
                                case "detect":
                                    String mac = objectRecvSub.getString("mac");
                                    if (mac.equals(getMacAddressFromIp(context))) {
                                        JSONObject objectSendDetect = new JSONObject();
                                        JSONObject objectSendDetectSub = new JSONObject();
                                        objectSendDetect.put("type","detect");
                                        objectSendDetectSub.put("mac",getMacAddressFromIp(context));
                                        objectSendDetect.put("data",objectSendDetectSub);
                                        objectSendDetect.put("status","online");
                                        objectSendDetect.put("reason","");
                                        String sendData = DATA_TITLE + objectSendDetect.toString();
                                        Log.d(TAG,"sendData: " + sendData);
                                        DatagramPacket theOutput = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length, packet.getAddress(), sendPort);
                                        server.send(theOutput);
                                    }
                                    break;
                                case "configAIM": {
                                    JSONObject objectSendconfigAIM = new JSONObject();
                                    JSONObject objectSendconfigAIMSub = new JSONObject();
                                    objectSendconfigAIMSub.put("mac",getMacAddressFromIp(context));
                                    objectSendconfigAIM.put("data",objectSendconfigAIMSub);
                                    objectSendconfigAIM.put("type","configAIM");
                                    objectSendconfigAIM.put("status","ok");
                                    Intent intent = new Intent("com.advantech.deploy.configAIM");
                                    intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                    intent.putExtra("data",objectRecvSub.toString());
                                    sendBroadcast(intent);
                                    String sendData = DATA_TITLE + objectSendconfigAIM.toString();
                                    Log.d(TAG,"sendData: " + sendData);
                                    DatagramPacket theOutput = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length, packet.getAddress(), sendPort);
                                    server.send(theOutput);
                                    break;
                                }
                                default: {
                                    Log.e(TAG, "Unknown message： " + packetDate);
                                    break;
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();

        } finally {
            if (server != null)
                server.close();

        }
    }

    public String getMacAddressFromIp(Context context) {
        String mac_s = "";
        StringBuilder buf = new StringBuilder();
        try {
            byte[] mac;
            NetworkInterface ne = NetworkInterface.getByInetAddress(
                    InetAddress.getByName(getIP(context)));
            mac = ne.getHardwareAddress();
            for (byte b : mac) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            mac_s = buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mac_s;
    }

    public String getIP(Context context){
        String ip = null;
        ConnectivityManager conMann = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conMann != null) {
            NetworkInfo mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo ethernetNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if (mobileNetworkInfo.isConnected()) {
                ip = getIpAddress("ppp0");
                //  Log.d(TAG,"Mobile IP : " + ip);
            }

            if (wifiNetworkInfo.isConnected()) {
                ip = getIpAddress("wlan0");
                //   Log.d(TAG,"Wifi IP : " + ip);
            }

            if (ethernetNetworkInfo.isConnected()) {
                ip = getIpAddress("eth0");
                //    Log.d(TAG,"Ethernet IP : " + ip);
            }
            return ip;
        }else{
            return null;
        }
    }

    private String getIpAddress(String interfaceName) {
        try {
            Enumeration<NetworkInterface> enumerationNetworkInterface = NetworkInterface.getNetworkInterfaces();
            while (enumerationNetworkInterface.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNetworkInterface.nextElement();
                String networkInterfaceName = networkInterface.getDisplayName();
                //     Log.d(TAG, "InterfaceName " + networkInterfaceName);

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
}
