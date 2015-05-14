package edu.upenn.cis.cis455.webserver;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.log4j.Logger;

/**
 * The Class ParsedXML.
 */
public class ParsedXML extends DefaultHandler {
	
	/** The Constant log. */
	static final Logger log = Logger.getLogger(ParsedXML.class.getName());
	
	/** The m_state. */
	private int m_state = 0;
	
	/** The servlet name. */
	private String servletName;
	
	/** The param name. */
	private String paramName;
	
	/** The servlets. */
	protected HashMap<String,String> servlets = new HashMap<String,String>();
	
	/** The servlet ur ls. */
	protected HashMap<String,String> servletURLs = new HashMap<String, String>();
	
	/** The context params. */
	protected HashMap<String,String> contextParams = new HashMap<String,String>();
	
	/** The servlet params. */
	protected HashMap<String,HashMap<String,String>> servletParams = new HashMap<String,HashMap<String,String>>();

	/**
	 * Match_m_state.
	 *
	 * @param m the m
	 * @return the string
	 */
	private String match_m_state(int m) {
		switch(m) {

		case(1): 
			return "servlet-name";
		case(2):
			return "servlet-class";
		case(3):
			return "context-param";
		case(4):
			return "init-param";
		case(5): 
			return "url-pattern";
		case(10): // context params name
			return "param-name"; 
		case(20): // servlet params name
			return "param-name";
		case(11): // context params value;
			return "param-value";
		case(21): // servlet params value;
			return "param-value";
		default:
			return null;
		
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.compareTo("servlet-name") == 0) {
			m_state = 1;
		} else if (qName.compareTo("servlet-class") == 0) {
			m_state = 2;
		} else if (qName.compareTo("context-param") == 0) {
			m_state = 3;
		} else if (qName.compareTo("init-param") == 0) {
			m_state = 4;
		} else if (qName.compareTo("param-name") == 0) {
			m_state = (m_state == 3) ? 10 : 20;
		} else if (qName.compareTo("param-value") == 0) {
			m_state = (m_state == 10) ? 11 : 21;
		} else if (qName.compareTo("url-pattern") == 0) {
			m_state = 5;
		} 
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) {
		
		String value = new String(ch, start, length);
		
		switch(m_state) {
		case 1 :
			servletName = value;
			break;
		case 2:
			servlets.put(servletName, value);
			break;
		case 5:
			if (servletName == null) {
				System.err.println("url pattern value '" + value + "' without servlet name");
				System.exit(-1);
			}
			servletURLs.put(servletName, value);
			break;
		case 10:
			paramName = value;
			break;
		case 11:
			if (paramName == null) {
				System.err.println("Context parameter value '" + value + "' without name");
				System.exit(-1);
			}
			contextParams.put(paramName, value);
			paramName = null;
			break;
		case 20:
			paramName = value;
			break;
		case 21:
			if (paramName == null) {
				System.err.println("Servlet parameter value '" + value + "' without name");
				System.exit(-1);
			}
			HashMap<String,String> p = servletParams.get(servletName);
			if (p == null) {
				p = new HashMap<String,String>();
				servletParams.put(servletName, p);
			}
			p.put(paramName, value);
			paramName = null;
			break;
			
		default:
			break;	
		}
	}
	
}