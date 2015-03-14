package fr.sportes.secrets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StaticServlet extends HttpServlet {

	private static String version;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final Logger log = Logger.getLogger("fr.sportes");

	public byte[] getResource(String name) {
		try {
			// String n = name.startsWith("/x") ? name.substring(2) : name;
			InputStream is = servletConfig.getServletContext().getResourceAsStream(name);
			if (is == null)
				return null;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int l = 0;
			while ((l = is.read(buf)) > 0)
				bos.write(buf, 0, l);
			return bos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	private ServletConfig servletConfig;

	@Override public void init(ServletConfig config) throws ServletException {
		servletConfig = config;
		version = servletConfig.getInitParameter("version");
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String uri = req.getRequestURI();
		String mime = servletConfig.getServletContext().getMimeType(uri);
		String uri3;
		byte[] bytes;
		int i = uri.lastIndexOf('.');
		if (i == -1){
			log.severe("Static - echec chargement (pas de point) : " + uri);
			resp.sendError(404);
			return;
		}
		String ext = uri.substring(i+1);
		if (mime == null){
			log.severe("Static - echec chargement (mime inconnu) : " + uri);
			resp.sendError(404);
			return;
		}
		String uri2 = uri.substring(0,i);
		int j = uri2.lastIndexOf('_');
		if (j != -1)
			uri2 = uri2.substring(0,j);
		uri3 = uri2 + "."+ ext;
		bytes = getResource(uri3);
		if (bytes == null){
			log.severe("Static - echec chargement (contenu vide) : " + uri);
			resp.sendError(404);
			return;
		}
		resp.setContentType(mime);
		resp.setContentLength(bytes.length);
//		resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
//		resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
//		resp.setDateHeader("Expires", 0); // Proxies.
		resp.getOutputStream().write(bytes);
		
//		log.info("GET: " + uri + " -- " + uri3 + " [" + mime + "]");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String uri = req.getRequestURI();
		byte[] bytes;
		if (uri.equals("/version")){
			String v = "{\"version\":\"" + version + "\"}";
			bytes = v.getBytes("UTF-8");
			resp.getOutputStream().write(bytes);
			resp.setContentType("application/json");
			resp.setContentLength(bytes.length);
		} else {
			resp.sendError(404);
			return;
		}
	}

}
