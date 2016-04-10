package servletPackage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;

//not consider the synchronization;
public class RPC_Client {
	static private final int portProj1bRPC = 5300;
	static public Map<String, String> SvrInfo = SessionManage.map;
	static int count = 1;
	static int WQ = 2;
	
	
	public static DatagramPacket rpcClientRead(String sessionID, String operation, String vnum)  {
		int callID = count ++;
		String str = callID + "" + "_" + operation + "_" + sessionID + "_" + vnum;
		//fill outbuf;
		byte[] outBuf = str.getBytes();
		//****//
		byte[] inBuf = new byte[512];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		int recvID = 0;
		try {
			DatagramSocket rpcSocket = new DatagramSocket();	
			
			//modify the file name, which downloads from the EC2;
			BufferedReader br = new BufferedReader(new FileReader("Test.txt"));
			//read the IP address;
			//for(each destaddr) {
			//}
			//when having multiple up addres, the following should be contained into the loop;
			//only need to be send to the corresponding server ID, which can be read from the cookie info;
			InetAddress ip = InetAddress.getByName("localhost");	
			DatagramPacket p = new DatagramPacket(outBuf, outBuf.length, ip, portProj1bRPC);
			rpcSocket.send(p);
			do {
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
				recvID = getcallID(inBuf);			
			}while(found(inBuf) && recvID != callID);
		} catch(SocketTimeoutException stoe) {
			//time out
			recvPkt = null;
		} catch(IOException ioe) {
			//other error
		}
		//condier when not getting this request;
		
		return recvPkt;
	}
	
	public static void rpcClienWrite(String sessionID, String operation, String vnum, String message) {
		int callID = count ++;
		String str = callID + "" + "_" + operation + "_" + sessionID + "_" + vnum + "_" + message + "_";
		//****//
		byte[] inBuf = new byte[512];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		int cnt = 0;
		try {
			//fill outbuf;
			byte[] outBuf = str.getBytes();
			DatagramSocket rpcSocket = new DatagramSocket();	
			
			//modify the file name, which downloads from the EC2;
			BufferedReader br = new BufferedReader(new FileReader("Test.txt"));
			//read the IP address;
			//write to the #W server;
			//for(each destaddr) {
			//}
			//when having multiple up addres, the following should be contained into the loop;
			//only need to be send to the corresponding server ID, which can be read from the cookie info;
			InetAddress ip = InetAddress.getByName("localhost");	
			DatagramPacket p = new DatagramPacket(outBuf, outBuf.length, ip, portProj1bRPC);
			rpcSocket.send(p);
			do {
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
				if(getack(inBuf)) {
					cnt ++;
				}
			}while(cnt < WQ);
		} catch(SocketTimeoutException stoe) {
			//time out
			recvPkt = null;
		} catch(IOException ioe) {
			//other error
		}
	}
	
	//helper function;
	public static boolean getack(byte[] inBuf) {
		String s = new String(inBuf);
		return s == "OK";
	}
	
	public static boolean found(byte[] inBuf) {
		String res = new String(inBuf);
		String[] arr = res.split("_");
		return arr[0] == "true";
	}
	
	public static int getcallID(byte[] inBuf) {
		String rec = new String(inBuf);
		String[] arr = rec.split("_");
		return Integer.valueOf(arr[0]);
	}	
}
