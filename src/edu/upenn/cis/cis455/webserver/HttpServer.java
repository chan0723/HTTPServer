package edu.upenn.cis.cis455.webserver;


import java.io.*;
import java.net.*;
import java.util.*;

import javax.net.ServerSocketFactory;
import javax.servlet.http.*;

import org.apache.log4j.Logger;




public class HttpServer extends Thread {
	static final Logger log = Logger.getLogger(HttpServer.class.getName());
	
	protected ServerSocket serverSocket; 
    protected RequestQueue requestQueue;
    
    protected static final int backlog = 5;
    protected String root;
    protected int port;
    protected WebAppsInfo webApps;
    protected HashMap<String,SocketSession> sessions = new HashMap<String,SocketSession>();
    protected HttpServlet defaultServlet;
    protected Scanner input;
    protected Thread daemonThread;
    
    
    public HttpServer(int port, String root, WebAppsInfo webApps) throws Exception {
  
    	this.root = root;
    	this.port = port;
    	this.webApps = webApps;
    	// create ServerSocket
    	try {
    		ServerSocketFactory ssf = ServerSocketFactory.getDefault();
	        serverSocket = ssf.createServerSocket(port, backlog);
	     } catch( Exception e ) {
	    	 log.error(e);
	     }

    	// create thread pool
    	this.requestQueue = new RequestQueue(this);
		
		// init default servlet
		Class defaultServletClass = Class.forName("edu.upenn.cis.cis455.webserver.FileServlet");
		this.defaultServlet = (HttpServlet) defaultServletClass.newInstance();
		this.defaultServlet.init();
	
    }


	 public void startServer() {
	     try {
	         this.start();         
	     } catch( Exception e ) {
	    	 log.error(e);
	     }
	 }
	
	 

	 public void stopServer() {
	     try {
	    	 
	         this.serverSocket.close();
	         
	     } catch( Exception e ) {
	    	 log.error(e);
	     }
	 }
	

	 public void run() {

	     while(true){
	         try {
	             
	             Socket s = serverSocket.accept();
	             this.requestQueue.add(s);
	             
	         } catch(SocketException se) {
	        	 break;
	         } catch(Exception e) {
	        	 log.error(e);
	         }
	     }
	     
	     for(HttpRequestThread rt : requestQueue.threadPool)
	         rt.interrupt();     
	     this.webApps.destroyAll();
	     this.daemonThread.interrupt();
	 }
	

	 public boolean hasNextLine() throws IOException{
		 while(System.in.available() == 0) {
			 try {
				 Thread.currentThread().sleep(10);
			 } catch (InterruptedException e) {
				 return false;
			 }
		 }
		 return this.input.hasNextLine();
	 }
	
	public static void main(String[] args) throws Exception {
		int port = 8080;
		String root = "www";
		if(args.length >= 2) {
			port = Integer.parseInt(args[0]);
			root = args[1];
		}
		
		File logFile = new File("log.out");
		logFile.delete();
		logFile.createNewFile();
		
		WebAppsInfo webApps = new WebAppsInfo();
		HttpServer server = new HttpServer(port, root, webApps);
		server.startServer();
		
		Scanner input = new Scanner(System.in);
		server.input = input;
		server.daemonThread = Thread.currentThread();
		
		webApps.helpText();
		while(server.hasNextLine()) {
			String command = input.nextLine();
			if(command == null) return;
			if(command.equals("1"))
				webApps.listAll();
			else if(command.equals("2"))
				webApps.listInstalled();
			else if(command.startsWith("install "))
				webApps.installNew(command);
			else if(command.startsWith("remove "))
				webApps.remove(command);
			else webApps.helpText();
			webApps.helpText();
		}
		
	}
}
