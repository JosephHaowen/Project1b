package servletPackage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionManage {
	private static ConcurrentHashMap<String, Session> SessionTable = new ConcurrentHashMap<>();
	//server info map;
	public static Map<String, String> map = new HashMap<>();
	private static final String cookieName = "CS5300J17";
	public static final int WQ = 2;
	//total number of data bricks;
	public static int N = 2;
	public static int F = 0;
	public static final int TimeOut = 30;
	
	//build new cookies, and add to the response;
	public static void updateResponse(String value, HttpServletResponse response) {
		Cookie c = new Cookie(cookieName, value);
		//set cookie domain here;
		//c.setDomain();
  	    c.setMaxAge(TimeOut);
  	    response.addCookie(c);
	}           	

	//first time connected to the website;
	public static String BuildSession(HttpServletResponse response) {
		//new a random number as Session ID;
		String ID ="";
		Session nsession = new Session(0, "HelloUser");
		//key = sessionID_Version;
		SessionTable.put(generateSessionID(ID, 0, 0) + "_" + 0, nsession);
		String value = getValue(ID);
		updateResponse(value, response);
		return value;
	}

	//get the sessionID = serverId_rebootnum_sessionnumber;
	public static String generateSessionID(String ID, int rnum, int sessnum) {
		return ID + "_" + String.valueOf(rnum) + "_" + String.valueOf(sessnum) + "_";
	}
	
	//get cookie value = sessionID + versionumber + WQserver;
	private static String getValue(String ID) {
		Session s = SessionTable.get(ID);
		return ID + "_" + s.getVnum() + "_" + getWQServer(N);
	}
	
	public static String getWQServer(int N)  {
		try {
			map = getServerInfo();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> id = new LinkedList<>();
		StringBuilder res = new StringBuilder();
		int index = 0;
		for(String k : map.keySet()) {
			id.add(k);
		}
		Collections.shuffle(id);
		for(String i : id) {
			res.append("_");
			res.append(i);
		}
		return res.substring(1);
	}
	
	//refresh, replace, would enter in this part;
	public static String SessionControl(String operation, HttpServletResponse response, String nmessage, String cvalue) {
		String[] a = cvalue.split("_");
		String sessionID = a[0];
		int vnum = 0;
		String msg = "";
		if(SessionTable.containsKey(a[0])) {
			Session s = SessionTable.get(sessionID);
			vnum = s.getVnum();
			msg = s.getMessage();
		}
		else {
			DatagramPacket recv = RPC_Client.rpcClientRead(sessionID, "operationSESSIONREAD", a[1]);
			String lastval = new String(recv.getData());
			String[] info = new String[3];
			info = getSessionInfo(lastval);
			vnum = Integer.valueOf(info[0]);
			msg = info[1];
		}	
		//decide if this session is out of time;
		//if out of time, set a new session
		//else update the information;
		vnum ++;
		if(operation == "replace") {
			msg = nmessage;
		}
		Session s = new Session(vnum, msg);		
		RPC_Client.rpcClienWrite(sessionID, "operationSESSIONWRITE", String.valueOf(vnum), msg);
		return "value";
	}
		
	//helper function
	public static String[] getSessionInfo(String lastval) {
		String[] res = new String[3];
		String[] arr = lastval.split("_");
		//vnum
		res[0] = arr[3];
		//message
		res[1] = arr[4];
		//localdata
		res[2] = arr[5];
		return res;
	}
	
	public static Session getSession(String ID) {
		return SessionTable.get(ID);
	}
	
	//retrieve the information in the sessiontable
	public static byte[] SesionRead(byte[] inBuf) {
		byte[] outBuf = new byte[512];
		String info = new String(inBuf);
		String[] arr = info.split("_");
		String sessionID = arr[2];
		int vnum = Integer.valueOf(arr[3]);
		String key = sessionID + "_" + String.valueOf(vnum);		
		StringBuilder res = new StringBuilder();
		//retrieve session data;
		if(SessionTable.containsKey(key)) {
			Session s = SessionTable.get(key);
			res.append(true + "_");
			//get callID;
			res.append(arr[0] + "_");
			res.append(s.getVnum() + "_");
			res.append(s.getMessage() + "_");
			res.append(s.getExtime() + "_");
		}
		else {
			res.append(false);
		}
		outBuf = res.toString().getBytes();
		return outBuf;
	}
	
	//write the updated information into the sessiontable;
	public static byte[] SessionWrite(byte[] inBuf) {
		byte[] outBuf = new byte[512];
		String info = new String(inBuf);
		String[] res = getSessionInfo(info);
		//retrieve the session;
		Session s = new Session(Integer.valueOf(res[0]), res[1]);
		//deal with the time out;
		s.setExtime();
		
		String ID = info.split("_")[2];
		//cant use put, we should maintain the old value;
		SessionTable.put(ID, s);
		outBuf = "OK".getBytes();
		return outBuf;
	}
	
	//the reboot number of this server;
	public static int getRebootNum() throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader("doc/reboot_num.txt"));
		return bf.read();
	}
	//get the self server id;
	public static int getSvrID() throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader("doc/ami-launch-index.txt"));
		return bf.read();
	}
	
	//get the ip address of ourself;
	public static String getIP() throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader("doc/local=ipv4.txt"));
		return bf.readLine();
	}
	
	//get instacesinfo, key is the serverID, value is the IP
	public static Map<String, String> getServerInfo() throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader("doc/instances_info.txt"));
		Map<String, String> res = new HashMap<>();
		while(bf.readLine() != null) {
			String temp = bf.readLine();
			//ID_IP address;
			String[] arr = temp.split(" ");
			res.put(arr[0], arr[1]);
		}
		return res;
	}
	
	//get the N and F parameter from the script;
	public static void getParameter() throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader("doc/parameter.txt"));
		//initialize the parameter;
		N = bf.read();
		F = bf.read();
	}
	
}
