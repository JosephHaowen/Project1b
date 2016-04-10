package servletPackage;
import java.io.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class RPC_Server implements ServletContextListener {
	static private final int portProj1bRPC = 5300;
	
	public void rpcServer() throws Exception {				
		DatagramSocket rpcSocket = new DatagramSocket(portProj1bRPC);
		while(true) {
			byte[] inBuf = new byte[512];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			rpcSocket.receive(recvPkt);
			InetAddress returnAddr = recvPkt.getAddress();
			int returnPort = recvPkt.getPort();
			String operationCode = getoperationCode(inBuf);
			byte[] outBuf = null;
			switch(operationCode) {
			case "operationSESSIONREAD":
				//sessionread accept call args and returns call results;
				outBuf = SessionManage.SesionRead(recvPkt.getData());
				break;
			case "operationSESSIONWRITE":
				outBuf = SessionManage.SessionWrite(recvPkt.getData());
				break;
			}
			//DatagramPacket sendPkt;
			//rpcSocket.send(sendPkt);
		}
	}
	
	public String getoperationCode(byte[] inBuf) {
		String rec= new String(inBuf);
		String[] arr = rec.split("_");
		return arr[1];
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
