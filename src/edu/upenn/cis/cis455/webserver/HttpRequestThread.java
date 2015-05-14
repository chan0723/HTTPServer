package edu.upenn.cis.cis455.webserver;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

/**
 * The Class HttpRequestThread.
 */
public final class HttpRequestThread extends Thread {
	
	/** The Constant log. */
	static final Logger log = Logger.getLogger(HttpRequestThread.class.getName());
	
		/** The no args. */
		protected boolean noArgs;
		
		/** The root. */
		protected String root = "";
		
		/** The port. */
		protected int port;
		
		/** The req queue. */
		protected RequestQueue reqQueue;
		
		/** The sessions. */
		protected HashMap<String,SocketSession> sessions;
		
		/** The web apps. */
		protected WebAppsInfo webApps;
		
		/**
		 * Instantiates a new http request thread.
		 *
		 * @param reqQueue the req queue
		 * @throws Exception the exception
		 */
		public HttpRequestThread(RequestQueue reqQueue) throws Exception {
			this.reqQueue = reqQueue;
			this.port = reqQueue.server.port;
			//this.noArgs = reqQueue.server.noArgs;
			this.sessions = reqQueue.server.sessions;
			this.webApps = reqQueue.server.webApps;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			
			while(!this.isInterrupted()) {
				try {
					Socket socket = reqQueue.getNextReq();
					handleRequest(socket);
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
		
		/**
		 * Handle request.
		 *
		 * @param socket the socket
		 * @throws Exception the exception
		 */
		public void handleRequest(Socket socket) throws Exception {
			System.out.println("1");
			// inputstream and outputstream
			InputStream instream = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(instream));
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());				
			
			SocketRequest request = new SocketRequest(socket, br, this);
			SocketResponse response = new SocketResponse(os, request.persistentConnection());
			if(request.getCookies() != null) {
				for(Cookie cookie : request.getCookies()) {
					response.addCookie(cookie);
				}
			}
			HttpServlet servlet = request.getServlet();
			
			if(servlet != null) {
				servlet.service(request, response);
				response.flushBuffer();
			}
			
			if(request.persistentConnection())
				socket.setSoTimeout(10*60);
			else socket.close();
		}
		

}