
package servletPackage;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Servlet
 */
@WebServlet(description = "This is a sample servlet", urlPatterns = { "/Servlet" })
public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * @throws IOException 
	 * @throws ServletException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
	      // Get an array of Cookies associated with this domain
		  String value = "";
		  response.setContentType("text/html");
		  PrintWriter out = response.getWriter();
		  //SessionManage.ClearTable();
		  
		  //if no cookie there or the cookie in our request is not the cookie that we want.
	      if(request.getCookies() == null || Found(request) == null) {
			  value = SessionManage.BuildSession(response);
	    	  try {
				output(response, value);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      }
	      else {
    		  Cookie c = Found(request);	
	    	  if(request.getParameter("Logout") != null) {
	    		  c.setMaxAge(0);
	    		  //clear out the cookie from the sessiontable in the sessino management;
	      		  out.print("<b> LOG OUT</b>");
	    	  }   
	    	  else if(request.getParameter("Refresh") != null) {
	    		  if(c == null) value = SessionManage.BuildSession(response);
	    		  else {    			  
		    		  value = SessionManage.SessionControl("refresh", response, "Hello User", c.getValue());
			    	  try {
						output(response, value);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		  }
	    	  }
	    	  else if(request.getParameter("Replace") != null){
	    		  if(c == null) value = SessionManage.BuildSession(response);
	    		  else {
		    		  String message = request.getParameter("message");
		    		  value = SessionManage.SessionControl("replace", response, message, c.getValue());
			    	  try {
						output(response, value);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		  }
	    	  }
	    	  //no operation, just display the original message;
	    	  else {
	    		  value = c.getValue();
		    	  try {
					output(response, value);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	  }
	      }
	}
	
	//Find the cookie in our request;
	public Cookie Found(HttpServletRequest request) {
		for(Cookie c : request.getCookies()) {
			if(c.getName().equals("CS5300J17")) return c;
		}
		return null;
	}
	//Displaying the informatin on our website;
	public void output(HttpServletResponse response, String value) throws ServletException, IOException {
		response.setContentType("text/html");	
	    PrintWriter out = response.getWriter(); 
	    String title = "New Page";
	    String[] valarr = value.split("_");	    
	    //deal with the session;
	    Session s = SessionManage.getSession(valarr[0]);
	    
	    String time = new Date().toString();    
	    String Extime = new Date(s.getExtime().getTimeInMillis()).toString();
	    //Expire time; 
	    String doctype = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " + "Transitional//EN\">\n";
	    out.println(doctype + 
	    		   "<HTML>\n" + 
	    		   "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" + 
	    		   "<BODY>\n" + 
	    		   "NetID:gg399\t" + "<br>Session:"  + valarr[0] + "<br>" + 
	    		   " Version: " + valarr[1] + "<br>" +" Date:" + time + "<br>" + 
	    		   "<H1>" + s.getMessage() + "</H2>\n" + 
	    		   "<form id = \"form\" name = \"form\" method = \"get\">" +
	    		   "<input type = \"submit\" name = \"Replace\" value = \"Replace\" />" + 
	    		   "<input type = \"text\" name = \"message\"><br>" +
	    		   "<input type = \"submit\" name = \"Refresh\" value = \"Refresh\" /><br>" +
	    		   "<input type = \"submit\" name = \"Logout\" value = \"Logout\" /><br>" +
	    		   //Expiration time not added;
	    		   "Cookie:" + value + "<br>"+ "Expires:" + Extime +
	    		   "</BODY></HTML>");	    
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
    }
}
