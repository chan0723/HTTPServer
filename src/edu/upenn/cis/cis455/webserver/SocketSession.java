package edu.upenn.cis.cis455.webserver;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

/**
 * The Class SocketSession.
 */
public class SocketSession implements HttpSession{

	/** The Constant log. */
	static final Logger log = Logger.getLogger(SocketSession.class.getName());
	
	/** The attributes. */
	private HashMap<String,Object> attributes = new HashMap<String, Object>();
	
	/** The creation time. */
	private long creationTime;
	
	/** The last accessed time. */
	private long lastAccessedTime;
	
	/** The max inactive interval. */
	private int maxInactiveInterval; // seconds
	
	/** The context. */
	private ServletContext context;
	
	/** The id. */
	private String id;
	
	/** The invalidated. */
	private boolean invalidated;
	
	/** The is new. */
	private boolean isNew;
	
	/**
	 * Instantiates a new socket session.
	 *
	 * @param context the context
	 */
	public SocketSession(ServletContext context) {
		this.isNew = true;
		this.invalidated = false;
		Calendar cal = Calendar.getInstance();
    	this.creationTime = cal.getTimeInMillis();
    	this.id = "" + this.creationTime;
    	this.lastAccessedTime = this.creationTime;
    	this.maxInactiveInterval = 60*30;
    	this.context = context;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String arg0) {
		return this.attributes.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(this.attributes.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 */
	@Override
	public long getCreationTime() {
		return this.creationTime;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	@Override
	public String getId() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	@Override
	public long getLastAccessedTime() {
		return this.lastAccessedTime;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	@Override
	public int getMaxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	@Override
	public ServletContext getServletContext() {
		return context;
	}

	// deprecate
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	public HttpSessionContext getSessionContext() {
		return null;
	}
	// deprecate
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	public Object getValue(String arg0) {
		return null;
	}
	// deprecate
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */
	public String[] getValueNames() {
		return null;
	}
	// deprecate
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String arg0, Object arg1) {}
	// deprecate
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	public void removeValue(String arg0) {}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	@Override
	public void invalidate() {
		this.attributes = new HashMap<String, Object>();
		this.invalidated = true;
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	@Override
	public boolean isNew() {
		return this.isNew;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	@Override
	public void removeAttribute(String arg0) {
		this.attributes.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String arg0, Object arg1) {
		this.attributes.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	@Override
	public void setMaxInactiveInterval(int arg0) {
		this.maxInactiveInterval = arg0;
	}

}
