package edu.berkeley.security.eventtracker.webserver;

import java.io.IOException;
import java.util.Properties;

import org.json.JSONException;

import edu.berkeley.security.eventtracker.eventdata.EventDataSerializer;

/**
 * An example of subclassing NanoHTTPD to make a custom HTTP server.
 */
public class EventDataServer extends NanoHTTPD {

	public EventDataServer(WebServerService service) throws IOException {
		super(8080);
		this.mWebService = service;
	}

	public Response serve(String uri, String method, Properties header,
			Properties parms) {
		String msg;
		if (parms.getProperty("data") == null) {
			msg = base_msg;
		} else {
			try {
				msg = EventDataSerializer
						.getAllRowsSerializedJSONaData(mWebService);
			} catch (JSONException e) {
				msg="HI";
			} // TODO fix
		}
		return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, msg);
	}

	WebServerService mWebService;
	static final String base_msg = "\n<html>\n\u0009<head>\n\u0009\u0009<style type=\"text/css\" title=\"currentStyle\"> \n\u0009\u0009\u0009@import \"http://192.168.0.105:3000/stylesheets/demo_page.css\";\n\u0009\u0009\u0009@import \"http://192.168.0.105:3000/stylesheets/demo_table.css\";\n\u0009\u0009</style> \n\u0009\u0009<script type=\"text/javascript\" src=\"http://192.168.0.105:3000/javascripts/jquery.js\"></script>\n\u0009\u0009<script type=\"text/javascript\" src=\"http://192.168.0.105:3000/javascripts/jquery.dataTables.js\"></script>\n\u0009\u0009<script type=\"text/javascript\" charset=\"utf-8\">\n\u0009\u0009\u0009$(document).ready(function(){\n\u0009\u0009\u0009\u0009$('#example').dataTable( {\n\u0009\u0009\u0009\u0009\u0009\"bProcessing\": true,\n\u0009\u0009\u0009\u0009\u0009\"sAjaxSource\": '/?data=eventData'\n\u0009\u0009\u0009\u0009} );\n\u0009\u0009\u0009});\n\u0009\u0009</script>\n\u0009</head>\n\u0009<body>\n\u0009\u0009<div id=\"dynamic\"> \n\u0009\u0009\u0009<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"display\" id=\"example\"> \n\u0009\u0009\u0009\u0009<thead>\n\u0009\u0009\u0009\u0009\u0009<tr> \n\u0009\u0009\u0009\u0009\u0009\u0009<th width=\"20%\">Event name</th> \n\u0009\u0009\u0009\u0009\u0009\u0009<th width=\"25%\">Start Time</th> \n\u0009\u0009\u0009\u0009\u0009\u0009<th width=\"25%\">End Time</th> \n\u0009\u0009\u0009\u0009\u0009\u0009<th width=\"15%\">Notes</th> \n\u0009\u0009\u0009\u0009\u0009</tr>\n\u0009\u0009\u0009\u0009</thead> \n\u0009\u0009\u0009\u0009<tbody></tbody> \n\u0009\u0009\u0009</table> \n\u0009\u0009</div> \n\u0009</body>\n</html>\n";
}