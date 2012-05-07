package blue_crab;

import java.net.*;
import java.util.Enumeration;

public class BlueCrabIPDetector 
{
	public static InetAddress findBootAddr() throws UnknownHostException, SocketException {
		//THIS IS A BIT OF A HACK TO START A NEW RING CORRECTLY
		/*
		 * MAKE THIS WORK ON MULTIPLE OPERATING SYSTEMS
		 */
		String addrStringWired = null;
		String addrStringWireless = null;
		for (Enumeration<NetworkInterface> net_interfaces = NetworkInterface.getNetworkInterfaces(); net_interfaces.hasMoreElements();) {
			NetworkInterface curr = net_interfaces.nextElement();
			System.out.println(curr.getName());
			int addr_count = 0;
			for (Enumeration<InetAddress> addresses = curr.getInetAddresses(); addresses.hasMoreElements();){
				InetAddress curr_addr = addresses.nextElement();
				System.out.println(curr_addr.toString());
				if (curr.getName().equals("eth0") && addr_count == 2) {
					addrStringWired = curr_addr.toString();
				}
				if (curr.getName().equals("wlan0") && addr_count == 1) {
					addrStringWireless = curr_addr.toString();
				}
				addr_count += 1;
			}
		}
		System.out.println("Wired: "+addrStringWired);
		System.out.println("Wireless: "+addrStringWireless);
		if (addrStringWired != null) {
			return InetAddress.getByName(addrStringWired.substring(1));
		} else if (addrStringWireless != null){
			return InetAddress.getByName(addrStringWireless.substring(1));
		} else {
			return null;
		}
	}
}
