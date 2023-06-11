package eu.gpapadop.netwatchpro.managers.internetPackages;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.IpV4Packet;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;

import eu.gpapadop.netwatchpro.R;

public class PackageCaptureWifi extends VpnService {
    private Thread packetCaptureThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startPacketCapture();
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
        // Parse the Ethernet frame
        try {
            EthernetPacket ethernetPacket = EthernetPacket.newPacket(packetData, 0, packetLength);
            Log.d("george", String.valueOf(ethernetPacket.getHeader()));
            Log.d("george", "======");
        } catch (IllegalRawDataException e){
            return;
        }

//        if (ethernetPacket instanceof IpV4Packet) {
//            // Extract information from IPv4 packet
//            IpV4Packet ipV4Packet = (IpV4Packet) ethernetPacket;
//            Inet4Address sourceIp = ipV4Packet.getHeader().getSrcAddr();
//            Inet4Address destinationIp = ipV4Packet.getHeader().getDstAddr();
//
//            // ... Extract other information from the IPv4 packet
//
//        } else if (ethernetPacket instanceof IpV6Packet) {
//            // Extract information from IPv6 packet
//            IpV6Packet ipV6Packet = (IpV6Packet) ethernetPacket;
//            Inet6Address sourceIp = ipV6Packet.getHeader().getSrcAddr();
//            Inet6Address destinationIp = ipV6Packet.getHeader().getDstAddr();
//
//            // ... Extract other information from the IPv6 packet
//
//        } else if (ethernetPacket instanceof ArpPacket) {
//            // Extract information from ARP packet
//            ArpPacket arpPacket = (ArpPacket) ethernetPacket;
//
//            // ... Extract ARP information
//
//        } else {
//            // Other packet types can be handled here
//        }
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
}
