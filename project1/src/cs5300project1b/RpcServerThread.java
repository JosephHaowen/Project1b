package cs5300project1b;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class RpcServerThread implements ServletContextListener{
	private Thread sthread = null;
	private Thread gthread = null;
	private int portProj1bRPC = 5300;
	
	/***aware that we need to add this listener to web.xml so that it will start***/
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		try{
			sthread.interrupt();
			gthread.interrupt();
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
		if((sthread==null) || (!sthread.isAlive())){
			sthread = new Thread() {
				public void run() {
					try{
						DatagramSocket rpcSocket = new DatagramSocket(portProj1bRPC);
						
						while(true){
							byte[] inBuf = new byte[512];
							DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
							rpcSocket.receive(recvPkt);
							InetAddress returnAddr = recvPkt.getAddress();
							int returnPort = recvPkt.getPort();
							//parse the inBuf date to get the callID and operationCode.
							//here inBuf contains the callID and operationCode
							String content = new String(recvPkt.getData());
							String[] callData = content.split("_");
							String callID = callData[0];
							//if it is read, then the call would be callid_operationcode_sessid_version
							//if it is write, then the call += _msg_discardTime
							int operationCode = Integer.parseInt(callData[1]);
							System.out.println("Received:" + content);
							/***here we need to parse the content to callID and operCode***/
			
							byte[] outBuf = null;
							
							switch(operationCode){
								/***should define this method in servlet class.***/
								/***recvPkt.getData() contains callID and operationCode***/
								case 0/*read*/: //outBuf = sessionRead(recvPkt.getData(), recvPkt.getLength());
												outBuf = Servlet.sessionRead(callID, callData[2], callData[3]);
												break;
								/***should define this method in servlet class.***/
								case 1/*write*/:outBuf = Servlet.sessionWrite(callID, callData[2], callData[3], callData[4], callData[5]); 
												break;
								
							}
							//here outBuf should contain the callID and results of the call
							DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
							rpcSocket.send(sendPkt);
							
							
						}
					} catch (Exception e){
						/***we also need to consider the exception situation***/
						e.printStackTrace();
						sthread.start();
					}
				}
			};
			sthread.start();
		}
		
		if((gthread==null) || (!gthread.isAlive())){
			gthread = new Thread() {
				public void run() {
					while(true){
						Servlet.clearTimeout();
						
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			};
		}
		gthread.start();
	}

}
