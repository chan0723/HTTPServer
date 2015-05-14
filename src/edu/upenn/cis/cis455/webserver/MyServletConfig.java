package edu.upenn.cis.cis455.webserver;
import org.apache.log4j.Logger;

import javax.servlet.*;

import java.util.*;

// every servlet has one servletconfig

/**
 * The Class MyServletConfig.
 */
class MyServletConfig implements ServletConfig {
	
	/** The Constant log. */
	static final Logger log = Logger.getLogger(MyServletConfig.class.getName());
	
	/** The name. */
	private String name;
	
	/** The context. */
	private ApplicationContext context;
	
	/** The init params. */
	private HashMap<String,String> initParams;
	
	/**
	 * Instantiates a new my servlet config.
	 *
	 * @param name the name
	 * @param context the context
	 */
	public MyServletConfig (String name, ApplicationContext context) {
		this.name = name;
		this.context = context;
		initParams = new HashMap<String,String>();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
	 */
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 */
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getServletContext()
	 */
	public ServletContext getServletContext() {
		return context;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getServletName()
	 */
	public String getServletName() {
		return name;
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

}
