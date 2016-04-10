package cs5300project1b;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class MySession {
	String sid; //session id
	String deadTime; // expire time
	int version; // version number
	String msg; // message
	Date birthTime;
	int sess_timeout_secs = 300;
	int incre = 20;
	
	public MySession(String sid){
		msg = "Hello Users!";
		version = 1;
	    //please note that the lifetime is set to be 5 minutes.
		birthTime = new Date();
		this.sid = sid;
		DateFormat time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		deadTime = time.format(birthTime.getTime()+(sess_timeout_secs+incre)*1000);
	}
	public MySession(String sid, String version){
		this.sid = sid;
		this.version = Integer.parseInt(version);
		this.msg = "";
		this.birthTime = new Date();
		DateFormat time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		deadTime = time.format(birthTime.getTime());
	}
	
	public MySession(String sid, String version, String data){
		this.sid = sid;
		this.version = Integer.parseInt(version);
		this.msg = data;
		this.birthTime = new Date();
		DateFormat time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		deadTime = time.format(birthTime.getTime()+(sess_timeout_secs+incre)*1000);
	}
	
	
	public MySession(String sid, String version, String data, String dis_time){
		this.sid = sid;
		this.version = Integer.parseInt(version);
		this.msg = data;
		this.birthTime = new Date();
		this.deadTime = dis_time;
	}
}
