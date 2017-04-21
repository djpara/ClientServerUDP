package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

class UDPPingerClient {

	private static final String CRLF = "\r\n";

	public static void main(String argv[]) throws SocketException, UnknownHostException {

		// Socket Variables
		DatagramSocket clientSocket;
		DatagramPacket sendPacket;
		DatagramPacket receivePacket;
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		InetAddress IPAddress;

		// Client variables
		ArrayList<Long> rttList = new ArrayList<Long>();
		String server;
		int port;
		int timeout = 1_000;

		// Socket data variables
		Integer sequence = 0;
		Integer pTransmitted = 0;
		Integer pLost = 0;

		// Time formatter
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");

		// Process command-line arguments
		if (argv.length < 2) {
			System.out.println("Usage: java PingServer hostname port\n");
			System.exit(-1);
		}

		// Command-line arguments
		server = argv[0];
		port = Integer.parseInt(argv[1]);
		
//		server = "localhost";
//		port = 9000;

		// Create the socket
		clientSocket = new DatagramSocket();
		clientSocket.setSoTimeout(timeout);
		IPAddress = InetAddress.getByName(server);

		while (sequence < 10) {
			++sequence;

			// Sent time
			LocalDateTime now = LocalDateTime.now();
			Long RTTb = System.currentTimeMillis();
			
			// Format the message to be sent
			sendData = ("PING seq#="+sequence.toString()+" time="+dtf.format(now)).getBytes();

			try {

				// Create a packet and send to server
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				clientSocket.send(sendPacket);
				++pTransmitted;

				// Create receiving packet and receive from server
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				clientSocket.receive(receivePacket);
				String serverSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());

				// Received time
				Long RTTa = System.currentTimeMillis();

				// Display server response
				System.out.println(serverSentence);

				// Round trip time
				Long rtt = (RTTa - RTTb);
				rttList.add(rtt);
				System.out.println("RTT: "+rtt.toString()+" ms"+CRLF);

			} catch (Exception e) {
				// Server does not respond
				++pLost;
				System.out.println("Request timed out."+CRLF);

			}


		}
		
		// Compute statistics
		Integer pReceived = pTransmitted-pLost;
		Double pPacketLoss = (((float)pLost/pTransmitted)*100.0);
		Long min = Collections.min(rttList);
		Long max = Collections.max(rttList);
		Long ave = computeAverage(rttList);
		
		// Print PING statistics
		System.out.println("--- ping statistics ---"+CRLF+CRLF
					+pTransmitted+" packets transmitted, "
					+pReceived+" received, "+pPacketLoss+"% packet loss"
					+CRLF+CRLF+"rtt min/avg/max = "+min+" / "+ave+" / "
					+max.toString()+" ms"+CRLF);
		
		// Close the client socket
		clientSocket.close();
	}
	
	private static Long computeAverage(ArrayList<Long> lst) {
		
		Long total = (long) 0;
		
		for (Long l: lst) {
			total += l;
		}
		
		return total/lst.size();
	}
	
}