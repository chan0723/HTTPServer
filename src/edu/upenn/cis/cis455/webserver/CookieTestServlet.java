package edu.upenn.cis.cis455.webserver;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

public class CookieTestServlet extends HttpServlet {
	static final Logger log = Logger.getLogger(CookieTestServlet.class.getName());
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {    
               
    	Cookie[] cookies = request.getCookies();
    	String sessionID = "";
    	for(Cookie cookie : cookies) {
    		if(cookie.getName().equals("JSESSIONID"))
    			sessionID = cookie.getValue();
    	}
      
        String expSessionID = request.getParameter("JSESSIONID");
        PrintWriter out = response.getWriter();
        
        out.print("<html><head><title>Cookie Test</title></head><body>");
              
        if(request.getQueryString() == null || request.getQueryString() == "") {
        	out.print("Cookie Test <a href=\"http://localhost:8080/" + request.getContextPath() + "/cookie_test?JSESSIONID=" + sessionID + "\"><button>test</button>");
        } else if(expSessionID != null && expSessionID.equals(sessionID)){
        	out.print("<p>Succeed in Cookie Test!</p>");
        } else {
        	out.print("<p>Failed in Cookie Test!</p>");
        	out.print("<p>Expected session id: " + expSessionID +"</p>");
        	out.print("<p>Actual session id: " + sessionID + "</p>");
        }
        out.println("</body></html>");
 	 }
}
  
