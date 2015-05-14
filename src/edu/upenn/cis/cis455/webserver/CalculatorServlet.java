package edu.upenn.cis.cis455.webserver;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

public class CalculatorServlet extends HttpServlet {
	static final Logger log = Logger.getLogger(CalculatorServlet.class.getName());
	public void service(HttpServletRequest request, HttpServletResponse response) 
	       throws java.io.IOException
	  {
	    if(request.getMethod().equals("GET"))
	    	doGet(request, response);
	    else doPost(request,response);
	  }
	
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
  {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    int v1 = Integer.valueOf(request.getParameter("num1")).intValue();
    int v2 = Integer.valueOf(request.getParameter("num2")).intValue();
    out.println("<html><head><title>Foo</title></head>");
    out.println("<body>"+v1+"+"+v2+"="+(v1+v2)+"</body></html>");
    out.close();
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
	       throws java.io.IOException
	  {
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    int v1 = Integer.valueOf(request.getParameter("num1")).intValue();
	    int v2 = Integer.valueOf(request.getParameter("num2")).intValue();
	    out.println("<html><head><title>Foo</title></head>");
	    out.println("<body>"+v1+"+"+v2+"="+(v1+v2)+"</body></html>");
	    out.close();
	  }
}
  
