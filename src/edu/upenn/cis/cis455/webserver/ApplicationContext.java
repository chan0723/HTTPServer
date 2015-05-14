package edu.upenn.cis.cis455.webserver;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * The Class ApplicationContext.
 */
public class ApplicationContext implements ServletContext{
	
	/** The Constant log. */
	static final Logger log = Logger.getLogger(ApplicationContext.class.getName());

	/** The attributes. */
	private HashMap<String,Object> attributes = new HashMap<String, Object>();
	
	/** The init params. */
	private HashMap<String,String> initParams = new HashMap<String, String>();
	
	/** The path to file map. */
	private HashMap<String,File> pathToFileMap = new HashMap<String, File>();
	
	/** The servlet urls. */
	protected HashMap<String,String> servletURLs;
	
	/** The servlets. */
	protected HashMap<String,HttpServlet> servlets;
	
	/** The Constant root. */
	private static final String root = "webapps/";
	
	/**
	 * Sets the servlets.
	 *
	 * @param servlets the servlets
	 */
	public void setServlets(HashMap<String,HttpServlet> servlets) {
		this.servlets = servlets;
	}
	
	/**
	 * Sets the init param.
	 *
	 * @param name the name
	 * @param value the value
	 */
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
	
	/**
	 * Sets the servlet ur ls.
	 *
	 * @param servletURLs the servlet ur ls
	 */
	void setServletURLs(HashMap<String,String> servletURLs) {
		this.servletURLs = servletURLs;
	}
	
	/**
	 * Find servlet.
	 *
	 * @param path the path
	 * @return the string
	 */
	String findServlet(String path) {
		for(String name : servletURLs.keySet()) {
			if(path.matches(servletURLs.get(name))) 
				return name;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String arg0) {
		return this.attributes.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 */
	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(this.attributes.keySet());
	}

	// in case of more than one web applications
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getContext(java.lang.String)
	 */
	public ServletContext getContext(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
	 */
	@Override
	public String getInitParameter(String arg0) {
		return this.initParams.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 */
	@Override
	public Enumeration getInitParameterNames() {
		return Collections.enumeration(this.initParams.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getMajorVersion()
	 */
	@Override
	public int getMajorVersion() {
		return 2;
	}

	

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getMinorVersion()
	 */
	@Override
	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 4;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
	 */
	@Override
	public String getRealPath(String arg0) {
		File file = new File(this.root + arg0);
		if(file.isFile()) return file.getPath();
		if(file.isDirectory()) return file.getPath()+"/";
		return null;
	}

	// no need to implement
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
	 */
	public String getMimeType(String arg0) {
		return null;
	}
	// no need to implement
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
	 */
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return null;
	}
	// no need to implement
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}
	// no need to implement
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getResource(java.lang.String)
	 */
	public URL getResource(String arg0) throws MalformedURLException {
		return null;
	}
	// no need to implement
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String arg0) {
		return null;
	}
	// no need to implement
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
	 */
	public Set getResourcePaths(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServerInfo()
	 */
	@Override
	public String getServerInfo() {
		return "My Server engine, implied Servlet 2.4";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServlet(java.lang.String)
	 */
	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		return this.servlets.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServletContextName()
	 */
	@Override
	public String getServletContextName() {
		return "Application Servlet Context";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServletNames()
	 */
	@Override
	public Enumeration getServletNames() {
		return Collections.enumeration(this.servlets.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#getServlets()
	 */
	@Override
	public Enumeration getServlets() {
		return Collections.enumeration(this.servlets.values());
	}

	// no need to implement
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#log(java.lang.String)
	 */
	public void log(String arg0) {}
	// no need to implement
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#log(java.lang.Exception, java.lang.String)
	 */
	public void log(Exception arg0, String arg1) {}
	// no need to implement
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
	 */
	public void log(String arg0, Throwable arg1) {}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
	 */
	@Override
	public void removeAttribute(String arg0) {
		this.attributes.remove(arg0);		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String arg0, Object arg1) {
		this.attributes.put(arg0, arg1);
	}

}
