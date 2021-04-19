package pnu.stemlab.vimnavi;

import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class VIMPosition {
	public final double x;
	public final double y;
	public final double z;
	public VIMPosition(double x, double y) {
		this.x = x;
		this.y = y;
		this.z = Double.NaN;
	}
	public VIMPosition(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public VIMPosition(VIMPosition pos) {
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
	}
	public VIMPosition(JSONObject jsonPos) {
		double x=Double.NaN;
		double y=Double.NaN;
		double z=Double.NaN;
		try { x = jsonPos.getDouble("x"); } catch(JSONException e) {}
		try { y = jsonPos.getDouble("y"); } catch(JSONException e) {}
		try { z = jsonPos.getDouble("z"); } catch(JSONException e) {}
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public VIMPosition(Node gpxNode) {
		Element e = (Element) gpxNode;
		String str;
		str = e.getAttribute("lon");
		this.x = Double.parseDouble(str);
		str = e.getAttribute("lat");
		this.y = Double.parseDouble(str);
		NodeList nodeList = e.getElementsByTagName("ele");
		double z = Double.NaN;
		if(nodeList.getLength()>0) {
			str = nodeList.item(0).getTextContent();
			z = Double.parseDouble(str);
		}
		this.z = z;
	}
	public JSONObject toJSON() {
		JSONObject jsonPos = new JSONObject();
		if(CoordSystem.isValidCoord(x)) {
			try { jsonPos.put("x",this.x); } catch(JSONException e) {}
		}
		if(CoordSystem.isValidCoord(y)) {
			try { jsonPos.put("y",this.y); } catch(JSONException e) {}
		}
		if(CoordSystem.isValidCoord(z)) {
			try { jsonPos.put("z",this.z); } catch(JSONException e) {}
		}
		return jsonPos;
	}
}
