package edu.upenn.cis.cis455.webserver;

import java.io.*;
import java.net.Socket;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * The Class FileServlet.
 */
public class FileServlet extends HttpServlet{
	
	/** The Constant log. */
	static final Logger log = Logger.getLogger(FileServlet.class.getName());
	
	/** The url. */
	private String url;
	
	/** The root. */
	protected String root = "www";
	
	/** The rela url. */
	private String relaURL;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		this.relaURL = ((SocketRequest)request).relaURL;
		this.url = this.root + this.relaURL;
		File file = new File(url);
		
		if (this.relaURL.equals("/control")) {
			PrintWriter out = response.getWriter();
			try {
				controlPage(out);
			} catch (Exception e) {
				log.error(e);
			}
			return;
		} 
		
		if (badReq(url)) {
			response.sendError(400);
			return;
		} 
		
		if (file.isFile() && file.canRead()){
			try {
				response.setContentType(this.relaURL);
				response.resetBuffer();
				PrintWriter out = response.getWriter();
				filePage(out, file);
			} catch (Exception e) {
				log.error(e);
			}
			
			return;
		} 
		if (file.isFile()) {
			response.sendError(401);
			return;
		} 
		if (file.isDirectory()) {
			String[] dir = file.list();
			try {
				PrintWriter out = response.getWriter();
				fileListPage(out, dir);
			} catch (Exception e) {
				log.error(e);
			}
			return;
		} 
			response.sendError(404);
	}



	/**
	 * Control page.
	 *
	 * @param out the out
	 * @throws Exception the exception
	 */
	private void controlPage(PrintWriter out) throws Exception {
		File logFile = new File("log.out");
		FileReader fr = new FileReader(logFile);
		
		StringBuilder sb = new StringBuilder();
		int readIn;
		while((readIn = fr.read()) != -1) {
			sb.append((char)readIn);
		}
		out.write("<HTML><HEAD><TITLE>Control</TITLE></HEAD>" + "<BODY>Error Log<br>");
		out.write(sb.toString()+"</BODY><HTML>");
	}
	
	/**
	 * File page.
	 *
	 * @param out the out
	 * @param f the f
	 * @throws Exception the exception
	 */
	private void filePage(PrintWriter out, File f) throws Exception {

		FileReader fr = new FileReader(f);
		char[] buffer = new char[1024];
		int bytes = fr.read(buffer);
		out.write(buffer, 0, bytes);
	}
	
	/**
	 * File list page.
	 *
	 * @param out the out
	 * @param dir the dir
	 * @throws Exception the exception
	 */
	private void fileListPage(PrintWriter out, String[] dir) throws Exception {
		
		out.write("<HTML><HEAD><TITLE>Directory</TITLE></HEAD>" + "<BODY>Directory...<br>");
		if (dir.length != 0) {
			for(int i = 0; i < dir.length; i++)
				out.write("<a href=\"http://localhost:8080" + this.relaURL  + dir[i] + "\">" + dir[i] + "</a> <br>");
		}
		
		out.write("</BODY></HTML>");
	}
	
	/**
	 * Bad req.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	private boolean badReq(String path) {
		return !path.matches("[A-Za-z0-9_/\\.]*");
	}
	
	/**
	 * File type.
	 *
	 * @param filename the filename
	 * @return the string
	 */
	private static String fileType(String filename) {
		if(filename.endsWith(".htm") || filename.endsWith(".html"))
			return "text/html";
		if(filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
			return "image/jpeg";
		if(filename.endsWith(".gif")) 
			return "image/gif";
		return "application/octet-stream";
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#getServletName()
	 */
	public String getServletName() {
		return "FileServlet";
	}

}
