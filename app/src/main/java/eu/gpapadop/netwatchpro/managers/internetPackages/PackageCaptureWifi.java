package eu.gpapadop.netwatchpro.managers.internetPackages;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.MacAddress;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Formatter;
import eu.gpapadop.netwatchpro.R;
import eu.gpapadop.netwatchpro.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.api.InternetPackagesAPI;

public class PackageCaptureWifi extends VpnService {
    private Thread packetCaptureThread;
    private InternetPackagesAPI internetPackagesAPI = new InternetPackagesAPI();
    private SharedPreferencesHandler sharedPreferencesHandler;
    private String uniqueDeviceToken;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startPacketCapture();
        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.uniqueDeviceToken = this.sharedPreferencesHandler.getDeviceID();
        return START_STICKY;
    }

    private void startPacketCapture() {
        packetCaptureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream vpnInputStream = null;
                try {
                    // Create a VPN interface and configure it
                    Builder builder = new Builder();
                    builder.setSession(getString(R.string.app_name));
                    builder.addAddress("10.0.0.2", 32);
                    builder.addRoute("0.0.0.0", 0);
                    builder.setMtu(1500);

                    // Get the underlying file descriptor of the VPN interface
                    ParcelFileDescriptor vpnInterface = builder.establish();
                    FileDescriptor vpnFileDescriptor = vpnInterface.getFileDescriptor();

                    // Create a FileInputStream to read packets from the VPN interface
                    vpnInputStream = new FileInputStream(vpnFileDescriptor);

                    // Create a packet buffer
                    byte[] packetBuffer = new byte[4096];

                    // Start capturing packets
                    while (!Thread.interrupted()) {
                        try {
                            int bytesRead = vpnInputStream.read(packetBuffer);
                            if (bytesRead > 0) {
                                // Process the captured packet
                                processPacket(packetBuffer, bytesRead);
                            }
                        } catch (IOException e) {
                            // Handle the IOException here
                            if (e.getMessage().equals("EBADF (Bad file descriptor)")) {
                                // Handle the specific error
                                // For example, restart the packet capture process or stop the capture
                                break;
                            } else {
                                // Handle other IOExceptions
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Close the FileInputStream and release associated resources
                    if (vpnInputStream != null) {
                        try {
                            vpnInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        packetCaptureThread.start();
    }



    private void processPacket(byte[] packetData, int packetLength) {
        try {
            // Parse the Ethernet frame
            EthernetPacket ethernetPacket = EthernetPacket.newPacket(packetData, 0, packetLength);
            // Parse the Ethernet Header
            EthernetPacket.EthernetHeader ethernetHeader = ethernetPacket.getHeader();
            byte[] sourceAddress = ethernetHeader.getSrcAddr().getAddress();
            byte[] destinationAddress = ethernetHeader.getDstAddr().getAddress();

            String decodedSourceAddress = this.decodeIpAddress(sourceAddress);
            String decodedDestinationAddress = this.decodeIpAddress(destinationAddress);

            MacAddress sourceMacAddress = ethernetHeader.getSrcAddr();
            MacAddress destinationMacAddress = ethernetHeader.getDstAddr();

            String headerType = ethernetHeader.getType().valueAsString();
            String headerHexString = this.getHeaderRawData(ethernetHeader.getRawData());

            Packet ethernetPayload = ethernetPacket.getPayload();
            String decodedPayloadData;

            if (ethernetPayload != null) {
                decodedPayloadData = this.getHeaderRawData(ethernetPayload.getRawData());
                if (!decodedSourceAddress.equals("83.212.59.30") && !decodedDestinationAddress.equals("83.212.59.30")){
                    //Exclude Server IP Address
                    //Save Packet to API
//                    internetPackagesAPI.addInternetPackage(
//                            this.uniqueDeviceToken,
//                            decodedSourceAddress,
//                            decodedDestinationAddress,
//                            sourceMacAddress.toString(),
//                            destinationMacAddress.toString(),
//                            headerType,
//                            headerHexString,
//                            decodedPayloadData
//                    );
                }
            }

        } catch (IllegalRawDataException e){
            return;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPacketCapture();
    }

    private void stopPacketCapture() {
        if (packetCaptureThread != null) {
            packetCaptureThread.interrupt();
        }
    }

    private String decodeIpAddress(byte[] address){
        String addressStr = "";
        for (int i = 0; i < 4; ++i)
        {
            int t = 0xFF & address[i];
            addressStr += "." + t;
        }
        addressStr = addressStr.substring(1);
        return addressStr;
    }

    private String getHeaderRawData(byte[] headerRaw){
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);

        for (byte b : headerRaw) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }
}
