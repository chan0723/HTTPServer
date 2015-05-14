package edu.upenn.cis.cis455.webserver;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;
import org.apache.log4j.Logger;

/**
 * The Class WebAppsInfo.
 */
public class WebAppsInfo {
	
	/** The Constant log. */
	static final Logger log = Logger.getLogger(WebAppsInfo.class.getName());
	
	/** The Constant webAppsRoot. */
	private static final String webAppsRoot = "webapps/";
	
	/** The all app names. */
	protected String[] allAppNames;
	
	/** The app servlet urls. */
	protected HashMap<String, HashMap<String,String>> appServletURLs = new HashMap<String, HashMap<String,String>>();
    
    /** The installed app names. */
    protected ArrayList<String> installedAppNames = new ArrayList<String>();
    
    /** The web dot xml path. */
    protected HashMap<String, String> webDotXmlPath = new HashMap<String, String>(); 
    
    /** The app contexts. */
    protected HashMap<String, ApplicationContext> appContexts = new HashMap<String, ApplicationContext>();
    
    /** The app servlets. */
    protected HashMap<String,HashMap<String,HttpServlet>> appServlets = new HashMap<String,HashMap<String,HttpServlet>>();
    
    /** The context paths. */
    protected HashMap<String, String> contextPaths = new HashMap<String, String>();
  
	/**
	 * Instantiates a new web apps info.
	 */
	public WebAppsInfo() {
		File dir = new File(this.webAppsRoot);
    	this.allAppNames = dir.list();
    	for(String fileName : this.allAppNames) {
    		String webDotXmlPath = this.webAppsRoot + fileName + "/WEB-INF/web.xml";
    		this.webDotXmlPath.put(fileName, webDotXmlPath);
    	}
	}
	
	/**
	 * List installed.
	 *
	 * @throws Exception the exception
	 */
	public void listInstalled() throws Exception {
		for(String fileName : this.installedAppNames)
			System.out.println(fileName);
	}
	
	/**
	 * Install new.
	 *
	 * @param command the command
	 * @throws Exception the exception
	 */
	public void installNew(String command) throws Exception {
		String[] parse = command.split("\\s");
		loadWebApp(parse[parse.length - 1]);
	}
	
	/**
	 * Removes the.
	 *
	 * @param command the command
	 * @throws Exception the exception
	 */
	public void remove(String command) throws Exception {
		String[] parse = command.split("\\s");
		removeWebApp(parse[parse.length - 1]);
	}
	
	/**
	 * List all.
	 */
	public void listAll() {
		for(String name : this.allAppNames) {
			System.out.println(name);
		}
	}
	
	
	
	/**
	 * Load web app.
	 *
	 * @param name the name
	 * @throws Exception the exception
	 */
	public void loadWebApp(String name) throws Exception {
		boolean existName = false;
		for(String exist : this.allAppNames) {
			if(name.equals(exist))
				existName = true;
		}
		if(!existName) {
			System.out.println("No such application");
			return;
		}
		if(this.installedAppNames.contains(name))
			return;
		
		// create ServletContext
    	ParsedXML pxml = parseXml(this.webDotXmlPath.get(name));
    	this.appServletURLs.put(name, pxml.servletURLs);
    	ApplicationContext context = createContext(pxml);
    	this.appContexts.put(name, context);
    	
    	// initialize all servlets
    	HashMap<String,HttpServlet> servlets = createServlets(pxml, context, name);
    	context.setServlets(servlets);
    	this.appServlets.put(name, servlets);
    	this.contextPaths.put(name, parseContextXML(name));
    	
    	this.installedAppNames.add(name);
	}
	
	/**
	 * Removes the web app.
	 *
	 * @param name the name
	 * @throws Exception the exception
	 */
	public void removeWebApp(String name) throws Exception {
		if(!this.installedAppNames.contains(name)) 
			return;
		for(HttpServlet servlet : this.appServlets.get(name).values()) {
			servlet.destroy();
		}
		this.appServletURLs.remove(name);
		this.appContexts.remove(name);
		this.appServlets.remove(name);
		this.installedAppNames.remove(this.installedAppNames.indexOf(name));
		this.contextPaths.remove(name);
	}
	

	
	/**
	 * Parses the xml.
	 *
	 * @param xml the xml
	 * @return the parsed xml
	 * @throws Exception the exception
	 */
	private static ParsedXML parseXml(String xml) throws Exception {
		ParsedXML pXML = new ParsedXML();
		File file = new File(xml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, pXML);
		
		return pXML;
	}
	
	/**
	 * Parses the context xml.
	 *
	 * @param fileName the file name
	 * @return the string
	 * @throws Exception the exception
	 */
	private static String parseContextXML(String fileName) throws Exception {
		String contextDotXmlPath = WebAppsInfo.webAppsRoot + fileName + "/META-INF/context.xml";
		File contextDotXml = new File(contextDotXmlPath);
		FileReader fr = new FileReader(contextDotXml);
		BufferedReader br = new BufferedReader(fr);
		return br.readLine().split("\"")[1];
	}
	
	/**
	 * Creates the context.
	 *
	 * @param pxml the pxml
	 * @return the application context
	 */
	private ApplicationContext createContext(ParsedXML pxml) {
		ApplicationContext context = new ApplicationContext();
		for (String param : pxml.contextParams.keySet()) {
			context.setInitParam(param, pxml.contextParams.get(param));
		}
		return context;
	}
	 
	 
	/**
	 * Creates the servlets.
	 *
	 * @param h the h
	 * @param fc the fc
	 * @param appName the app name
	 * @return the hash map
	 * @throws Exception the exception
	 */
	private HashMap<String,HttpServlet> createServlets(ParsedXML h, ApplicationContext fc, String appName) throws Exception {
		
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.servlets.keySet()) {
			MyServletConfig config = new MyServletConfig(servletName, fc);
			String className = h.servlets.get(servletName);
			System.out.println(className);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}
	
	/**
	 * Help text.
	 */
	public void helpText() {
		System.out.println();
		System.out.println("help instruction:");
		System.out.println("input \"1\": list all applications");
		System.out.println("input \"2\": list all installed applications");
		System.out.println("input \"install <application_name>\": install <application_name> application");
		System.out.println("input \"remove <application_name>\": remove <application_name> application");
		System.out.println();
	}
	
	/**
	 * Destroy all.
	 */
	public void destroyAll() {
		for(HashMap<String,HttpServlet> servlets : this.appServlets.values())
			for(HttpServlet servlet : servlets.values())
				servlet.destroy();
	}
}
