package com.eebbk.bfc.download.demo.net_test;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.baseui.BaseActivity;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class NetTestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_test);
        initView();
    }

    private void initView() {
        TextView mMachineIp = findView(R.id.tv_machine_ip);
        mMachineIp.setText(getIpAddressString());
    }

    public void startPing(View view) {
        ArrayList<PingInfo> pingInfoArrayList = new ArrayList<>();

        TextView urlTextView = findView(R.id.et_ping_ip);
        TextView intervalTextView = findView(R.id.et_ping_interval_time);
        TextView timeoutTextView = findView(R.id.et_ping_timeout);

        String url = urlTextView.getText().toString();
        int pingInterval = Integer.parseInt(intervalTextView.getText().toString());
        int pingTimeout = Integer.parseInt(timeoutTextView.getText().toString());

        PingInfo pingInfo = new PingInfo(url, pingInterval, pingTimeout);

        pingInfoArrayList.add(pingInfo);

        PingService.startPing(this, pingInfoArrayList);
    }



    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface.getNetworkInterfaces();  enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }


    public void stopPing(View view) {
        PingService.stopServices(this);
    }
}
