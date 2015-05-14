package edu.upenn.cis.cis455.webserver;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;


/**
 * A Request Queue accepts new requests and processes them with its associated
 * thread pool.
 */
public class RequestQueue {
	
	/** The Constant log. */
	static final Logger log = Logger.getLogger(RequestQueue.class.getName());
	
	/** The Constant maxQueueLength. */
	protected static final int maxQueueLength = 100000;
    
    /** The Constant poolCapacity. */
    protected static final int poolCapacity = 2000;
    
    /** The queue. */
    protected LinkedList<Socket> queue = new LinkedList<Socket>(); 
	
	/** The thread pool. */
	protected List<HttpRequestThread> threadPool = new ArrayList<HttpRequestThread>();	
	
	/** The server. */
	protected HttpServer server;
	

	
    /**
     * Instantiates a new request queue.
     *
     * @param server the server
     * @throws Exception the exception
     */
    public RequestQueue(HttpServer server) throws Exception {
	  
	     this.server = server;
	 
	     for( int i=0; i < poolCapacity; i++ ) {
	         HttpRequestThread thread = new HttpRequestThread(this);
	         thread.start();
	        this.threadPool.add(thread);
	     }
 }

 
	 /**
 	 * Adds the.
 	 *
 	 * @param socket the socket
 	 * @throws Exception the exception
 	 */
 	public void add(Socket socket) throws Exception {
		 synchronized(queue) {
		     if(queue.size() > maxQueueLength)
		    	 throw new Exception( "The Request Queue is full. Max size = " + maxQueueLength );
		
		     queue.addLast(socket);
		     queue.notifyAll();
		 }     
	  }
	
	
	 /**
 	 * Gets the next req.
 	 *
 	 * @return the next req
 	 * @throws InterruptedException the interrupted exception
 	 */
 	public Socket getNextReq() throws InterruptedException {
		 synchronized(queue) {
			 while(queue.isEmpty()){
		    	 queue.wait();
		     }
		     return queue.removeFirst();
		    }
	 }
 

}                         
