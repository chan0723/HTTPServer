package edu.upenn.cis.cis455.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

/**
 * The Class SocketRequest.
 */
public class SocketRequest implements HttpServletRequest{
	
	/** The Constant log. */
	static final Logger log = Logger.getLogger(SocketRequest.class.getName());
	
	/** The content type. */
	private String contentType = "text/html";
	
	/** The char encoding. */
	private String charEncoding = "ISO-8859-1";
	
	/** The local port. */
	private int localPort;
	
	/** The rela url. */
	protected String relaURL="";
	
	/** The path info. */
	private String pathInfo=""; //the remainder of the URL request after the portion matched by the url-pattern in web.xml. It starts with a “/”
	
	/** The query string. */
	private String queryString=""; //return the HTTP GET query string, i.e., the portion after the “?” when a GET form is posted.
	
	/** The servlet path. */
	private String servletPath="";
	
	/** The context path. */
	private String contextPath="";
	
	/** The method. */
	private String method="GET";
	
	/** The headers. */
	private HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	
	/** The params. */
	private HashMap<String, ArrayList<String>> params = new HashMap<String, ArrayList<String>>();
	
	/** The attrs. */
	private HashMap<String, Object> attrs = new HashMap<String, Object>();
	
	/** The servlet. */
	private HttpServlet servlet = null;
	
	/** The locale. */
	private Locale locale;
	
	/** The br. */
	private BufferedReader br;
	
	/** The session. */
	private SocketSession session;
	
	/** The context. */
	private ApplicationContext context;
	
	/** The sessions. */
	private HashMap<String,SocketSession> sessions;
	
	/** The requested session from cookie. */
	private boolean requestedSessionFromCookie = false;
	
	/** The socket. */
	private Socket socket;
	
	/** The protocal. */
	private String protocal = "HTTP/1.0";
	
	/** The web app name. */
	private String webAppName;
	
	/** The web apps. */
	private WebAppsInfo webApps;
	
	/** The left url. */
	private String leftURL;
	
	/** The is persistent. */
	private boolean isPersistent;
	
	/**
	 * Instantiates a new socket request.
	 *
	 * @param socket the socket
	 * @param br the br
	 * @param requestThread the request thread
	 * @throws Exception the exception
	 */
	public SocketRequest(Socket socket, BufferedReader br, HttpRequestThread requestThread) throws Exception {
		this.localPort = requestThread.port;
		this.sessions = requestThread.sessions;
		this.socket = socket;
		this.webApps = requestThread.reqQueue.server.webApps;
		
		// parse request line
		String firstLine = br.readLine();
		String[] reqLine = firstLine.split("\\s");	
		if(reqLine[1].endsWith("/")) reqLine[1] = reqLine[1].substring(0, reqLine[1].length() - 1);
		this.method = reqLine[0];
		this.relaURL = reqLine[1];
		if(reqLine.length > 2) this.protocal = reqLine[2];
		
		System.out.println(this.protocal);
		
		if(this.relaURL.equals("/shutdown")) {
			requestThread.reqQueue.server.stopServer();
			return;
		}
				
		this.leftURL = reqLine[1];
		// find web app, parse context path
		parseContextPath();
		// parse servletPath;
		parseServletPath();
		// path info and query string
		parsePathInfoAndQueryString();
				
		if(this.servlet == null || this.relaURL.equals("/control")) 
			this.servlet = requestThread.reqQueue.server.defaultServlet;
		System.out.println("2");
		// parse headers
		parseHeaders(br);
		
		// read content
		if(this.method.equals("POST"))
			readContent(br);
		
		// parse params
		parseParams();
//		System.out.println("servlet=" + this.servlet.getServletName() + "; contextPath=" + this.contextPath + "; servletPath=" + this.servletPath);
	}
	
	
	/**
	 * Parses the params.
	 *
	 * @throws Exception the exception
	 */
	private void parseParams() throws Exception {
		if(!this.queryString.equals("")) {
			String[] paramsLine = this.queryString.split("&|=");
			if(paramsLine.length % 2 != 0) throw new Exception("wrong query params");
			for(int i = 0; i < paramsLine.length; i += 2) {
				if(!this.params.containsKey(paramsLine[i]))
					this.params.put(paramsLine[i], new ArrayList<String>());
				this.params.get(paramsLine[i]).add(paramsLine[i+1]);
			}
		}
	}
	
	/**
	 * Parses the headers.
	 *
	 * @param br the br
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void parseHeaders(BufferedReader br) throws IOException {
		String newHeaderLine;
		String headerName = null;
		while((newHeaderLine = br.readLine()) != null && newHeaderLine.length() != 0) {
			System.out.println(newHeaderLine);
			String[] line = newHeaderLine.split(":");
			if(!newHeaderLine.startsWith("\\s")) {
				headerName = line[0].trim();				
				if(headers.containsKey(headerName)) {
					headers.get(headerName).add(line[1].trim());
				} else {
					ArrayList<String> value = new ArrayList<String>();
					for(int i = 1; i < line.length; i++)
						value.add(line[i].trim());
					headers.put(headerName, value);
				}
			} else {
				headers.get(headerName).add(line[0].trim());
			}
		}
		
		if(this.protocal.equals("HTTP/1.1") && this.headers.get("Connection")!= null && this.headers.get("Connection").equals("keep-alive"))
			this.isPersistent = true;
		else this.isPersistent = false;
	}
	
	/* find web app, set context path */
	/**
	 * Parses the context path.
	 */
	private void parseContextPath() {
		HashMap<String, String> matches = new HashMap<String, String>();

		for(String appName : this.webApps.installedAppNames) {
			String contextPath = this.webApps.contextPaths.get(appName);
			if(leftURL.startsWith(contextPath)) {
				matches.put(appName, contextPath);
			}
		}
		
		if(matches.size() == 0) return;
		
		int maxLength = 0;
		String webAppName = "";
		for(String appName : matches.keySet()) {
			if(maxLength < matches.get(appName).length())
				webAppName = appName;
		}
		
		this.webAppName = webAppName;
		this.contextPath = matches.get(webAppName);
		leftURL = leftURL.substring(this.contextPath.length());
	}
	
	
	/* when method is post, read content */
	/**
	 * Read content.
	 *
	 * @param br the br
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void readContent(BufferedReader br) throws IOException {
		StringBuilder content = new StringBuilder();
		
		// http/1.0 and connection: close
		if(this.protocal.equals("HTTP/1.0") || this.headers.get("Connection").equals("close")) {
			int readChar;
			while((readChar = br.read()) != -1) {
				content.append((char)readChar);
			}
			this.queryString = content.toString();
			this.br = new BufferedReader(new StringReader(content.toString()));
			return;
		}
		
		// http/1.1; connection: keep-alive; transfer-encoding: chunked
		if(this.headers.get("Transfer-Encoding").equals("chunked")) {
			content = new StringBuilder();
			String length;
			while((length = br.readLine()) != null && !length.equals("0")) {
				int readLength = Integer.parseInt(br.readLine().split(";")[0], 16);
				for(int i = 0; i < readLength; i++)
					content.append((char)br.read());
				br.readLine();
			}
			this.queryString = content.toString();
			this.br = new BufferedReader(new StringReader(content.toString()));
			return;
		}
		
		// http/1.1; connection: keep-alive; content-length:xxx
		int contentLength = Integer.parseInt(this.headers.get("Content-Length").get(0));
		content = new StringBuilder();
		for(int i = 0; i < contentLength; i++)
			content.append((char)br.read());
		this.queryString = content.toString();
		this.br = new BufferedReader(new StringReader(content.toString()));
		
	}
	
	
	/**
	 * Parses the servlet path.
	 */
	private void parseServletPath() {
		if(this.webAppName == null) {
			this.servletPath = leftURL;
			leftURL = "";
			return;
		}
		
		HashMap<HttpServlet, String> matches = new HashMap<HttpServlet, String>();
		
		for(String servletName : this.webApps.appServletURLs.get(this.webAppName).keySet()) {	
			String pattern = this.webApps.appServletURLs.get(this.webAppName).get(servletName);
			
			if(pattern.endsWith("/*") && leftURL.startsWith(pattern.substring(0, pattern.length()-2))) {
				HttpServlet servlet = this.webApps.appServlets.get(this.webAppName).get(servletName);
				String servletPath = pattern.substring(0, pattern.length()-2);
				matches.put(servlet, servletPath);
			} else if (pattern.startsWith("*.") && leftURL.endsWith(pattern.substring(1))) {
				HttpServlet servlet = this.webApps.appServlets.get(this.webAppName).get(servletName);
				String servletPath = leftURL;
				matches.put(servlet, servletPath);
			} else if (pattern.endsWith("*") && leftURL.startsWith(pattern.substring(0, pattern.length()-1))) {
				HttpServlet servlet = this.webApps.appServlets.get(this.webAppName).get(servletName);
				String servletPath = pattern.substring(0, pattern.length()-1);
				matches.put(servlet, servletPath);
			} else if (leftURL.startsWith(pattern)) {
				HttpServlet servlet = this.webApps.appServlets.get(this.webAppName).get(servletName);
				String servletPath = pattern;
				matches.put(servlet, servletPath);
			}
		}
		
		if(matches.size() == 0) return;
		
		int maxLength = 0;
		HttpServlet servlet = null;
		for(HttpServlet findServlet : matches.keySet()) {
			if(matches.get(findServlet).length() > maxLength)
				servlet = findServlet;
		}
		
		this.servlet = servlet;	
		this.servletPath = matches.get(servlet);
		leftURL = leftURL.substring(this.servletPath.length());
	}
		
		
	/**
	 * Parses the path info and query string.
	 */
	private void parsePathInfoAndQueryString() {
		if(leftURL.length() == 0) return;
		if(leftURL.indexOf("#") != -1) {
			leftURL = leftURL.substring(0, leftURL.indexOf("#"));
		}
		if(leftURL.indexOf("?") != -1 && !leftURL.endsWith("?")) {
			this.queryString = leftURL.substring(leftURL.indexOf("?")+1, leftURL.length());
			leftURL = leftURL.substring(0, leftURL.indexOf("?"));
		}
		this.pathInfo = leftURL;
	}
			
	
	/**
	 * Gets the servlet.
	 *
	 * @return the servlet
	 */
	public HttpServlet getServlet() {
		return this.servlet;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String arg0) {
		return this.attrs.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(this.attrs.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	@Override
	public String getCharacterEncoding() {
		return this.charEncoding;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	@Override
	public int getContentLength() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	@Override
	public String getContentType() {
		return this.contentType;
	}

	// no need
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	@Override
	public String getLocalAddr() {
		return this.socket.getLocalAddress().toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	@Override
	public String getLocalName() {
		return "localhost";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	@Override
	public int getLocalPort() {
		return this.socket.getLocalPort();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	@Override
	public Locale getLocale() {
		return this.locale;
	}
	
	/**
	 * Sets the locale.
	 *
	 * @param locale the new locale
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	@Override
	public Enumeration getLocales() {
		ArrayList<Locale> locales = new ArrayList<Locale>();
		locales.add(this.locale);
		return Collections.enumeration(locales);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter(String arg0) {
		String params="";
		try {
			params = this.params.get(arg0).get(0);
		}catch(Exception e) {
			log.error("Bad request: no parmas", e);
		}
		return params;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	@Override
	public Map getParameterMap() {
		return params;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(this.params.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	@Override
	public String[] getParameterValues(String arg0) {
		return (String[]) this.params.get(arg0).toArray();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	@Override
	public String getProtocol() {
		return this.protocal;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		return this.br;
	}

	// deprecate
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	@Override
	public String getRemoteAddr() {
		return this.socket.getRemoteSocketAddress().toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	@Override
	public String getRemoteHost() {
		return this.socket.getRemoteSocketAddress().toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	@Override
	public int getRemotePort() {
		return this.socket.getPort();
	}

	// no need
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	@Override
	public String getScheme() {
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	@Override
	public String getServerName() {
		return "localhost";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	@Override
	public int getServerPort() {
		return this.localPort;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	@Override
	public boolean isSecure() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	@Override
	public void removeAttribute(String arg0) {
		this.attrs.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String arg0, Object arg1) {
		this.attrs.put(arg0, arg1);	
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	@Override
	public void setCharacterEncoding(String charEncoding)
			throws UnsupportedEncodingException {
		this.charEncoding = charEncoding;
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	@Override
	public String getAuthType() {
		return "BASIC";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	@Override
	public String getContextPath() {
		return contextPath;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	@Override
	public Cookie[] getCookies() {
		if(this.headers.get("Cookie") == null || this.headers.get("Cookie").size() == 0)
			return null;
		String cookieList[] = this.headers.get("Cookie").get(0).split(";");
		ArrayList<Cookie> cookies = new ArrayList<Cookie>();
		for(int i = 0; i < cookieList.length; i++) {
			String[] nameAndValue = cookieList[i].trim().split("=");
			if(nameAndValue.length == 2) {
				cookies.add(new Cookie(nameAndValue[0], nameAndValue[1]));
			}
		}
		if(cookies.size() == 0) return null;
		Cookie[] cookieArray = new Cookie[cookies.size()];
		for(int i = 0; i < cookies.size(); i++)
			cookieArray[i] = cookies.get(i);
		return cookieArray;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	@Override
	public long getDateHeader(String arg0) {
		SimpleDateFormat df = new SimpleDateFormat();
		Date date = null;
		try {
			date = df.parse(headers.get(arg0).get(1));
		} catch (ParseException e) {
			log.error(e);
		}
	
		return date.getTime();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	@Override
	public String getHeader(String arg0) {
		return headers.get(arg0).get(0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	@Override
	public Enumeration<String> getHeaderNames() {
		Enumeration<String> e = Collections.enumeration(headers.keySet());
		return e;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration<String> getHeaders(String arg0) {
		Enumeration<String> e = Collections.enumeration(headers.get(arg0));
		return e;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	@Override
	public int getIntHeader(String arg0) {
		return Integer.parseInt(this.headers.get(arg0).get(0));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	@Override
	public String getMethod() {
		return this.method;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	@Override
	public String getPathInfo() {
		return this.pathInfo;
	}

	
	// no need to imply
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	@Override
	public String getQueryString() {
		if(this.method.equals("GET")) return this.queryString;
		try {
			BufferedReader br = getReader();
		} catch (IOException e) {
			log.error(e);
		}
		return this.queryString;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	@Override
	public String getRemoteUser() {
		return this.socket.getRemoteSocketAddress().toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	@Override
	public String getRequestURI() {
		int queryIndex = this.relaURL.indexOf('?');
		if (queryIndex == -1) return relaURL;
		return relaURL.substring(0, queryIndex);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	@Override
	public StringBuffer getRequestURL() {
		return new StringBuffer("http://localhost:" + this.localPort + getRequestURI());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	@Override
	public String getRequestedSessionId() {
		return getSession().getId();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	@Override
	public String getServletPath() {
		return this.servletPath;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	@Override
	public HttpSession getSession() {
		
		if(!this.headers.containsKey("Cookie"))
			this.headers.put("Cookie", new ArrayList<String>());
			
		ArrayList<String> cookies = this.headers.get("Cookie");
		for(String cookieInfo : cookies) {
			String[] nameAndValue = cookieInfo.split("=|;|,");
			if(nameAndValue[0].equals("JSESSIONID")) {
				this.session = sessions.get(nameAndValue[1]);
				if(this.session != null) {
					this.requestedSessionFromCookie = true;
					return this.session;
				}
			}
		}
		
		this.session = new SocketSession(this.context);
		this.sessions.put(this.session.getId(), this.session);
		this.headers.get("Cookie").add("JSESSIONID="+ this.session.getId());
		
		return this.session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	@Override
	public HttpSession getSession(boolean create) {
		if(!create) return getSession();
		
		if(!this.headers.containsKey("Cookie"))
			this.headers.put("Cookie", new ArrayList<String>());
			
		ArrayList<String> cookies = this.headers.get("Cookie");
		
		Iterator<String> ite = cookies.iterator();
		while(ite.hasNext()) {
			String cookieInfo = ite.next();
			String[] nameAndValue = cookieInfo.split("=|;|,");
			if(nameAndValue[0].equals("JSESSIONID")) {
				this.sessions.remove(nameAndValue[1]);
				cookies.remove(cookieInfo);
			}
		}
		
		this.session = new SocketSession(this.context);
		this.sessions.put(this.session.getId(), this.session);
		this.headers.get("Cookie").add("JSESSIONID="+ this.session.getId());
		
		return this.session;
		
	}

	// no need
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return this.requestedSessionFromCookie;
	}

	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	// deprecated
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	@Override
	public boolean isRequestedSessionIdValid() {
		return this.requestedSessionFromCookie;
	}

	// no need
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		return false;
	}

	/**
	 * Persistent connection.
	 *
	 * @return true, if successful
	 */
	public boolean persistentConnection() {
		return this.isPersistent;
	}
}
