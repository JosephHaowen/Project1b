package cs5300project1b;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Servlet
 */
@WebServlet("/Servlet")
public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//Sessions is the session table whose key is sessionID+version, value is the corresponding session object.
    private static ConcurrentHashMap<String, MySession> sessions = new ConcurrentHashMap<> ();
    private static final String cookiename = "CS5300PROJ1";
    
    //getServers() method gets all serverID and their IP addresses from a local file.
    private static Map<String,InetAddress> servers = getServers();
    
    //local-ipv4 contains the internal IP address.
    //ami-launch-index contains the internal server ID.
    //public-hostname contains the domain name.
    //reboot_num contains the reboot number.
    //instances_info contains the instances information.
    
    
    //getRebootNum() method gets the reboot number from a local file.
    //every time we reboot the instance, this number will increase by 1.
    //getSvrID() method gets the server ID from a local file.
    //Define the port number, session number and callID.
    private static int rebootNum = getRebootNum(); 
    private static String svrID = getSvrID();
    private static int socketTimeout = 50;
    private int portProj1bRPC = 5300;
    private static int sessNum = 0;
    private static int callID = 0;
    private static List<String> svrIDs = getSvrIDs(servers);
    //getServerNum() and getFailureNum() methods get N and F from a local file.
    //Then we can compute WQ, R and W according to N and F.
    private static int N = 0;
    private static int F = 0;
    private static int WQ = 0;
    private static int W = 0;
    
    private static String domain = "";
    //Define all the parameters that should be displayed in the jsp file.
    private static String sessionid = null;
    private static String expiretime = null;
    private static String versionid = null;
    private static String cookieval = null;
    private static String msg = null;
   
    private static boolean allNodeFail = false;
    private static boolean sessTimeout = false;
    /***retrieve parameters from the local file***/
    
    /****newly added code***/
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Servlet() {
        super();
        getParameters();
        WQ = F + 1;
        W = 2*F + 1;
        // TODO Auto-generated constructor stub
    }
    
    /***maybe need to change this function according to the doc***/
    private void getParameters() {
		// TODO Auto-generated method stub
    	BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader("doc/parameter.txt"));
			N = bf.read();
			F = bf.read();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//initialize the parameter;
		
		
	}

	private static List<String> getSvrIDs(Map<String, InetAddress> servers2) {
		// TODO Auto-generated method stub
    	List<String> idList = new ArrayList<> ();
    	for(String key: servers2.keySet()){
    		idList.add(key);
    	}
		return idList;
	}

	public void init() throws ServletException  
    { 	
    }
    
    private static String getSvrID() {
		// TODO Auto-generated method stub
    	String res = null;
    	BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader("doc/ami-launch-index.txt"));
			res = bf.readLine();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1){
			e1.printStackTrace();
		}
		
    	return res;
	}

	private static int getRebootNum() {
		// TODO Auto-generated method stub
		int res = 0;
    	BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader("doc/reboot_num.txt"));
			res = bf.read();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1){
			e1.printStackTrace();
		}
		
    	return res;
		
	}
	
	private static Map<String, InetAddress> getServers() {
		// TODO Auto-generated method stub
		Map<String, InetAddress> res = new HashMap<> ();
		String temp = null;
		BufferedReader bf;
		try{
			bf = new BufferedReader(new FileReader("doc/instances_info.txt"));
			while((temp = bf.readLine()) != null ){
				String[] arr = temp.split(" ");
				res.put(arr[0], InetAddress.getByName(arr[1]));
			}
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}catch(IOException e1){
			e1.printStackTrace();
		}
		return res;
	}

	/***newly added methods***/
    public static byte[] sessionRead(String callId, String sid, String version){
    	/***here we need to truncate the packet data***/
    	String key = sid + version;
    	MySession cur = sessions.get(key);
    	String result = callId;
    	
    	//if cur == null, then this session has timed out.
    	if(cur!=null){
    		result += "_true_"+cur.msg;
    		/***maybe it is beyond 512 bytes***/
    	}else{
    		result += "_false";
    	}
    	return result.getBytes();
    }
    
    
    public static byte[] sessionWrite(String callId, String sid, String version, String msg, String dis_time){
    	MySession storeSess = new MySession(sid,version,msg,dis_time);
    	sessions.put(sid+version, storeSess);
    	return (callId+"_done").getBytes();
    }
    /***newly added methods***/
    
    
    
    
    
   
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		//get the cookies from the request and use a method called "find" to find the matching cookie.
		Cookie[] cookies = request.getCookies();
		Cookie cur = find(cookies);
		
		//get the message content and presses button from the request.
		msg = request.getParameter("message");
		String button = request.getParameter("act");
		
		
		if(button==null || button.equals("refresh")){
			
			//visiting the first time: create a new session and new cookie, and set up all output parameters.
			if(cur==null){

				Cookie newCook = CreateNewSess();
				response.addCookie(newCook);
				/***newly added code***/
				//create a new session and put it into the table.
				
				
			//visiting not the first time: obtain the session object, and set up all output parameters.
			}else{
				//get session id from the cookie and get the session from the table based on the id.
				String[] curVal = cur.getValue().split("_");
				sessionid = curVal[0];
				String version = curVal[1];
				Set<String> server = new HashSet<> ();
				for(int i=2;i<curVal.length;i++){
					server.add(curVal[i]);
				}
				
				MySession newSess = null;
				if(server.contains(svrID)){
					MySession curSess = sessions.get(sessionid+version);
					versionid = Integer.parseInt(version)+1+"";
					
					//the newSess is the session that updated from the previous version.
					//update: version+1, discardtime.
					newSess = new MySession(sessionid, versionid, curSess.msg);
					Set<String> input = getWServers(svrIDs);
					
					Set<InetAddress> resWrite = sessionWriteClient(input,newSess);
					
					//how to deal with this situation ? 
					if(resWrite.size()<WQ) {}
					
					String metadata = "";
					for(String svrid: servers.keySet()){
						if(resWrite.contains(servers.get(svrid))) metadata += "_"+svrid;
					}
					cookieval = sessionid+"_"+versionid+"_"+metadata;
					Cookie newCook = new Cookie(cookiename, cookieval);
					newCook.setMaxAge(newSess.sess_timeout_secs);
					
					newCook.setDomain(domain);
					response.addCookie(newCook);
					msg = curSess.msg;
					expiretime = newSess.deadTime;
					
					
					
				}else{
					DatagramPacket resRead = sessionReadClient(server, sessionid, version);
					
					//res==null means that all nodes fail.
					if(resRead==null){
						if(cur!=null){
							cur.setMaxAge(0);
							response.addCookie(cur);
						}
						allNodeFail = true;
						//print the nodes fail and delete the cookie.
					}else{
						
						String content = new String(resRead.getData());
						String[] val = content.split("_");
						if(val[1].equals("true")){
							msg = val[2];
							versionid = Integer.parseInt(version)+1+"";
							newSess = new MySession(sessionid,versionid, msg);
							expiretime = newSess.deadTime;
							Set<String> input = getWServers(svrIDs);
							
							Set<InetAddress> resWrite = sessionWriteClient(input,newSess);
							
							//how to deal with this situation ? 
							if(resWrite.size()<WQ) {}
							
							String metadata = "";
							for(String svrid: servers.keySet()){
								if(resWrite.contains(servers.get(svrid))) metadata += "_"+svrid;
							}
							cookieval = sessionid+"_"+versionid+"_"+metadata;
							Cookie newCook = new Cookie(cookiename, cookieval);
							newCook.setMaxAge(newSess.sess_timeout_secs);
							newCook.setDomain(domain);
							response.addCookie(newCook);
						//session time out.
						}else{
							if(cur!=null){
								cur.setMaxAge(0);
								response.addCookie(cur);
							}
							sessTimeout = true;
							//print the session time out and delete the cookie
						}
					}
				}		
			}
			
		}else if(button.equals("replace")){
		
		//the replace button: if cookie is not found, then recreate a new session and set its message to be the input message.
		if(cur==null) {
			
			Cookie newCook = CreateNewSess();
			response.addCookie(newCook);
			
		//replace button: if cookie is found, then retrieve the session using cookie and set its message to be the input message.
		}else{
			
			String[] curVal = cur.getValue().split("_");
			sessionid = curVal[0];
			String version = curVal[1];
			versionid = Integer.parseInt(version)+1+"";
			MySession newSess = new MySession(sessionid, versionid, msg);
			expiretime = newSess.deadTime;
			Set<String> input = getWServers(svrIDs);
			Set<InetAddress> resWrite = sessionWriteClient(input,newSess);
			
			if(resWrite.size()<WQ) {}
			
			String metadata = "";
			for(String svrid: servers.keySet()){
				if(resWrite.contains(servers.get(svrid))) metadata += "_"+svrid;
			}
			cookieval = sessionid+"_"+versionid+"_"+metadata;
			Cookie newCook = new Cookie(cookiename, cookieval);
			newCook.setMaxAge(newSess.sess_timeout_secs);
			newCook.setDomain(domain);
			response.addCookie(newCook);
		}
			
	//logout button: if cur is found, set its max age to zero and let it expire right away; if not found, no more operations needed.
	}else if(button.equals("logout")){
		if(cur!=null){
			String[] curVal = cur.getValue().split("_");
			sessionid = curVal[0];
			String version = curVal[1];
			Set<String> input = getWServers(svrIDs);
			MySession newSess = new MySession(sessionid, version);
			Set<InetAddress> resWrite = sessionWriteClient(input,newSess);
			
			cur.setMaxAge(0);
			response.addCookie(cur);
			
		}
		
	}	
	
	//delete all timeout sessions for the session table.
	//clearTimeout();	 
	
	//if the pressed button is null or not logout, set attributes in the request and forward request and response to output.jsp.
	//else, it is logout, then forward to logout.jsp.
	if(button==null|| !button.equals("logout")){
		
		//put all output parameters into the request so that they can be retrieved in jsp file.
		if(allNodeFail==true || sessTimeout == true){
			if(allNodeFail==true){
				allNodeFail = false;
				request.getRequestDispatcher("nodefail.jsp").forward(request, response);
			}else{
				sessTimeout = false;
				request.getRequestDispatcher("sesstimeout.jsp").forward(request, response);		
			}
		}else{
			
			request.setAttribute("session", sessionid);
			request.setAttribute("version", versionid);
			request.setAttribute("expire", expiretime);
			request.setAttribute("message", msg);
			request.setAttribute("cookie",cookieval);
			request.getRequestDispatcher("output.jsp").forward(request, response);
		}
	}else{
		request.getRequestDispatcher("logout.jsp").forward(request, response); 
	}
	}

	private Cookie CreateNewSess() {
		// TODO Auto-generated method stub
		sessionid = svrID+rebootNum+""+sessNum;
		MySession newSess = new MySession(sessionid);
		sessNum++;
		
		/***get W servers from all servers***/
		Set<String> input = getWServers(svrIDs);
		/***get W servers from all servers***/
		Set<InetAddress> res = sessionWriteClient(input,newSess);
		
		//how to deal with this situation ? 
		if(res.size()<WQ) {}
		
		
		String metadata = "";
		for(String svrid: servers.keySet()){
			if(res.contains(servers.get(svrid))) metadata += "_"+svrid;
		}
		/***newly added code***/
		
		
		/***here I need to configure the metadata to be WQ server id***/
		//create a new cookie for this session and set its MaxAge equal to session.lifetime.
		cookieval = sessionid+"_"+newSess.version+metadata;
		
		
		Cookie newCook = new Cookie(cookiename, cookieval);
		newCook.setMaxAge(newSess.sess_timeout_secs);
		/***share the cookie to all domains***/
		newCook.setDomain(domain);
		
		/***here we set cookie domain to be global???***/
	//	response.addCookie(newCook);
		
		//set up all output parameters.
		msg = newSess.msg;
		expiretime = newSess.deadTime;
		versionid = ""+ newSess.version;
		return newCook;
	}

	private Set<String> getWServers(List<String> idList) {
		// TODO Auto-generated method stub
		List<String> tmp = new ArrayList<> (idList);
		Collections.shuffle(idList);
		Set<String> wIDs = new HashSet<> ();
		for(int i=0;i<W;i++){
			wIDs.add(tmp.get(i));
		}
		return wIDs;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	
	
	//find the target cookie with the cookie name; if not fount, return null.
	protected Cookie find(Cookie[] cookies){
		if(cookies==null || cookies.length==0) return null;
		
		for(Cookie c:cookies){
			if(c.getName().equals(cookiename)) return c;
		}
		return null;
	}
	
	//if it is read, then the call would be callid_operationcode_sessid_version
	//if it is write, then the call += _msg_discardTime
	protected DatagramPacket sessionReadClient(Set<String> server, String sessId, String version){
		String request = callID + "_0_"+sessId +"_"+version;
		callID++;
		byte[] outBuf = new byte[512];
		outBuf = request.getBytes();
		DatagramSocket clientSocket = null;
		DatagramPacket recvPkt = null;
		try {
			clientSocket = new DatagramSocket();
			for(String sid:server){
				InetAddress cur = servers.get(sid);
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, cur, portProj1bRPC);
				clientSocket.send(sendPkt);
			}
			
			byte[] inBuf = new byte[512];
			recvPkt = new DatagramPacket(inBuf, inBuf.length);
			String content = null;
			String[] sp = null;
			clientSocket.setSoTimeout(socketTimeout);
			do{
				recvPkt.setLength(inBuf.length);
				clientSocket.receive(recvPkt);
				content = new String(recvPkt.getData());
				if(content!=null){
					sp = content.split("_");
				}
			}while(content==null || !(sp[0].equals(callID+"")&&sp[1].equals("true")));
			
		} catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			recvPkt = null;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			/***how to re receive the packet since it will throw an exception ?? ***/
		}
		clientSocket.close();
		return recvPkt;
	}
	/***set contains W or W-1 server nodes***/
	protected Set<InetAddress> sessionWriteClient(Set<String> server, MySession curSess){
		String request = callID + "_1_" + curSess.sid + "_" +curSess.version + "_" + curSess.msg + "_" + curSess.deadTime;
		callID++;
		byte[] outBuf = new byte[512];
		outBuf = request.getBytes();
		DatagramSocket clientSocket = null;
		DatagramPacket recvPkt = null;
		Set<InetAddress> res = new HashSet<> ();
		try {
			clientSocket = new DatagramSocket();
			for(String sid:server){
				InetAddress cur = servers.get(sid);
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, cur, portProj1bRPC);
				clientSocket.send(sendPkt);
			}
			
			byte[] inBuf = new byte[512];
			recvPkt = new DatagramPacket(inBuf, inBuf.length);
			String content = null;
			String[] sp = null;
			int count = 0;
			/**should set timeout here ?? ***/
			clientSocket.setSoTimeout(socketTimeout);
			do{
				recvPkt.setLength(inBuf.length);
				clientSocket.receive(recvPkt);
				content = new String(recvPkt.getData());
				if(content!=null){
					sp = content.split("_");
					if(sp[0].equals(callID+"")&&sp[1].equals("done")) {
						res.add(recvPkt.getAddress());
						count++;
					}
				}
			}while(count<WQ);
			/***what if all nodes fail?***/
			/***how to throw an timeout exception here??***/
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clientSocket.close();
		return res;
		
	}
	
	//iterate the all sessions, and find the ones that have expired and delete them. 
	public static void clearTimeout(){
		if(sessions==null || sessions.isEmpty()) return;
		Date current = new Date();
		DateFormat time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		for(String key:sessions.keySet()){
			MySession curSess = sessions.get(key);
			Date distime;
			try {
				distime = time.parse(curSess.deadTime);
				if(current.getTime() > distime.getTime()){
					sessions.remove(key);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}