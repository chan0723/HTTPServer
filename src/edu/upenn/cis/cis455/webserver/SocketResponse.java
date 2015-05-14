package edu.upenn.cis.cis455.webserver;

import javax.servlet.http.*;

import java.io.*;
import java.nio.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.*;

import org.apache.log4j.Logger;


/**
 * The Class SocketResponse.
 */
public class SocketResponse implements HttpServletResponse{
	
	/** The Constant log. */
	static final Logger log = Logger.getLogger(SocketResponse.class.getName());
	
	/** The Constant chunkedLength. */
	static final int chunkedLength = 10;
	
	/** The Constant CRLF. */
	private final static String CRLF = "\r\n";
	
	/** The os. */
	private DataOutputStream os;
	
	/** The content type. */
	private String contentType = "text/html";
	
	/** The char encoding. */
	private String charEncoding = "ISO-8859-1";
	
	/** The content length. */
	private int contentLength;
	
	/** The is committed. */
	private boolean isCommitted = false;
	
	/** The locale. */
	private Locale locale;
	
	/** The buffer size. */
	private int bufferSize;
	
	/** The sc. */
	private int sc = 200;
	
	/** The headers. */
	private HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	
	/** The sw. */
	private StringWriter sw;
	
	/** The pw. */
	private PrintWriter pw;
	
	/**
	 * The Class SocketOutputStream.
	 */
	public class SocketOutputStream extends ServletOutputStream {

		/* (non-Javadoc)
		 * @see java.io.OutputStream#write(int)
		 */
		public void write(int b) throws IOException {
			os.write(b);
		}
		
	}
	
	/**
	 * Instantiates a new socket response.
	 *
	 * @param os the os
	 * @param isPersistent the is persistent
	 */
	public SocketResponse(DataOutputStream os, boolean isPersistent) {
		this.os = os;
		this.sw = new StringWriter();
		this.pw = new PrintWriter(sw);
		if(isPersistent) {
			this.headers.put("Transfer-Encoding", new ArrayList<String>());	
			this.headers.get("Transfer-Encoding").add("chunked");
			this.headers.put("Connection", new ArrayList<String>());
			this.headers.get("Connection").add("keep-alive");
		} else {
			this.headers.put("Connection", new ArrayList<String>());
			this.headers.get("Connection").add("close");
		}
	}
	
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		this.pw.flush();
		
		if(this.headers.get("Connection").get(0).equals("close"))
			this.os.writeBytes(sw.toString());
		else {
			int fileLength = sw.toString().length();
			StringReader sr = new StringReader(sw.toString());
			
			
			for(int i = 0; i < fileLength / chunkedLength; i++) {
				this.os.writeBytes(Integer.toHexString(chunkedLength) + CRLF);
				for(int j = 0; j < chunkedLength; j++)
					this.os.write(sr.read());
				this.os.writeBytes(CRLF);
			}
			
			if(fileLength % chunkedLength != 0) {
				this.os.writeBytes(Integer.toHexString(fileLength % chunkedLength) + CRLF);
				for(int j = 0; j < fileLength % chunkedLength; j++)
					this.os.write(sr.read());
				this.os.writeBytes(CRLF);
			}
			this.os.writeBytes(0 + CRLF);
			this.os.writeBytes(CRLF);
		}
		sw.close();
		pw.close();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	@Override
	public int getBufferSize() {
		return this.bufferSize;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	@Override
	public String getCharacterEncoding() {
		return this.charEncoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	@Override
	public String getContentType() {
		return this.charEncoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	@Override
	public Locale getLocale() {
		return this.locale;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if(this.isCommitted)  throw new IllegalStateException();
		os.writeBytes("HTTP/1.1 " + this.sc + CRLF);
		for(String name : this.headers.keySet()) {
			ArrayList<String> values = this.headers.get(name);
			for(String value : values)	
				os.writeBytes(name + ": " + value + CRLF);
		}
		os.writeBytes(CRLF);
		this.isCommitted = true;
		return new SocketOutputStream();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	@Override
	public PrintWriter getWriter() throws IOException {
		if(this.isCommitted)  throw new IllegalStateException();
		os.writeBytes("HTTP/1.1 200 OK" + CRLF);
		for(String name : this.headers.keySet()) {
			ArrayList<String> values = this.headers.get(name);
			for(String value : values)	{
				os.writeBytes(name + ": " + value + CRLF);
			}
			
		}
		os.writeBytes(CRLF);
		this.isCommitted = true;
		return pw;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	@Override
	public boolean isCommitted() {
		return this.isCommitted;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	@Override
	public void reset() {
		if(this.isCommitted) throw new IllegalStateException();
		headers = new HashMap<String, ArrayList<String>>();
		resetBuffer();
	}

	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		sw.getBuffer().setLength(0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	@Override
	public void setBufferSize(int n) {
		this.bufferSize = n;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	@Override
	public void setCharacterEncoding(String arg0) {
		this.charEncoding = arg0;
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	@Override
	public void setContentLength(int arg0) {
		if(this.isCommitted)  throw new IllegalStateException();
		this.contentLength = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType(String arg0) {
		if(this.isCommitted)  throw new IllegalStateException();
		this.contentType = arg0;
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale(Locale arg0) {
		if(this.isCommitted)  throw new IllegalStateException();
		this.locale = arg0;
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	@Override
	public void addCookie(Cookie cookie) {
		
		if(!this.headers.containsKey("Set-Cookie"))
			this.headers.put("Set-Cookie", new ArrayList<String>());
		
		for(String cookieInfo : this.headers.get("Set-Cookie")) {
			if(cookieInfo.startsWith(cookie.getName()))
				return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(cookie.getName() + "=" + cookie.getValue());
		if(cookie.getSecure()) sb.append("; Secure");
		if(cookie.getComment() != null) sb.append("; Comment=" + cookie.getComment());
		if(cookie.getDomain() != null) sb.append("; Domain=" + cookie.getDomain());
		if(cookie.getPath() != null) sb.append("; Path=" + cookie.getPath());
		
		SimpleDateFormat sdf = new SimpleDateFormat();
		sb.append("; Expires=" + sdf.format(new Date(cookie.getMaxAge())));
		headers.get("Set-Cookie").add(sb.toString());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	@Override
	public void addDateHeader(String arg0, long arg1) {
		if(!containsHeader(arg0)) 
			headers.put(arg0, new ArrayList<String>());
		headers.get(arg0).add(new Date(arg1*1000).toString());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader(String arg0, String arg1) {
		if(!containsHeader(arg0)) 
			headers.put(arg0, new ArrayList<String>());
		headers.get(arg0).add(arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	@Override
	public void addIntHeader(String arg0, int arg1) {
		if(!containsHeader(arg0)) 
			headers.put(arg0, new ArrayList<String>());
		headers.get(arg0).add("" + arg1);	
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	@Override
	public boolean containsHeader(String arg0) {
		return headers.containsKey(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	@Override
	public String encodeRedirectURL(String url) {
		return url;
	}

	// deprecate
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	@Override
	public String encodeURL(String url) {
		return url;
	}

	// deprecate
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	@Override
	public void sendError(int arg0) throws IOException {
		if(this.isCommitted)  throw new IllegalStateException();
		this.sc = arg0;
		os.writeBytes("HTTP/1.1 " + arg0 + CRLF);
		for(String name : this.headers.keySet()) {
			ArrayList<String> values = this.headers.get(name);
			os.writeBytes(name + ": " + values.get(0) + CRLF);
		}
		os.writeBytes(CRLF);
		this.isCommitted = true;
		this.pw.write("<HTML><HEAD><TITLE>" + arg0 + "</TITLE></HEAD>" + "<BODY>Error " + arg0 + "</BODY></HTML>");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int arg0, String arg1) throws IOException {
		if(this.isCommitted)  throw new IllegalStateException();
		this.sc = arg0;
		os.writeBytes("HTTP/1.1 " + arg0 + CRLF);
		for(String name : this.headers.keySet()) {
			ArrayList<String> values = this.headers.get(name);
			os.writeBytes(name + ": " + values.get(0) + CRLF);
		}
		os.writeBytes(CRLF);
		this.isCommitted = true;
		
		this.pw.write("<HTML><HEAD><TITLE>" + arg0 + "</TITLE></HEAD>" + "<BODY>Error " + arg1 + "</BODY></HTML>");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	@Override
	public void sendRedirect(String url) throws IOException {
		this.headers.put("Location", new ArrayList<String>());
		this.headers.get("Location").add(url);
		sendError(302);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	@Override
	public void setDateHeader(String arg0, long arg1) {
		if(this.isCommitted)  throw new IllegalStateException();
		if(!containsHeader(arg0)) 
			headers.put(arg0, new ArrayList<String>());
		headers.get(arg0).set(0, new Date(arg1*1000).toString());
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void setHeader(String arg0, String arg1) {
		if(this.isCommitted)  throw new IllegalStateException();
		if(!containsHeader(arg0)) 
			headers.put(arg0, new ArrayList<String>());
		headers.get(arg0).set(0, arg1);
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	@Override
	public void setIntHeader(String arg0, int arg1) {
		if(this.isCommitted)  throw new IllegalStateException();
		if(!containsHeader(arg0)) 
			headers.put(arg0, new ArrayList<String>());
		headers.get(arg0).set(0, "" + arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	@Override
	public void setStatus(int n) {
		if(this.isCommitted)  throw new IllegalStateException();
		this.sc = n;
	}

	// deprecate
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int n, String arg1) {
		if(this.isCommitted)  throw new IllegalStateException();
		this.sc = n;	
	}
	
}
