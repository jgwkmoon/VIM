package pnu.stemlab.vimnavi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VIMLocation extends VIMPosition {
	public String id;
	public double dir;
	public long time;

	public VIMLocation(String id, double x, double y, double z, double dir, long time) {
		super(x, y, z);
		this.id = id;
		this.dir = dir;
		this.time = time;
	}
	public VIMLocation(String id, VIMPosition loc, double dir, long time) {
		super(loc);
		this.id = id;
		this.dir = dir;
		this.time = time;
	}
	public VIMLocation(String id, GPSLocation gpsLoc,double dir) {
		super(gpsLoc);
		this.id = id;
		this.dir = dir;
		this.time = gpsLoc.time;
	}
//	public VIMLocation(Node gpxNode) {
//		super(gpxNode);
//		Element e = (Element) gpxNode;
//		NodeList nList = e.getElementsByTagName("time");
//		String str = nList.item(0).getTextContent();
//		SimpleDateFormat fmt 
//			= new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'");
//		long time = 0;
//		try { 
//			time = fmt.parse(str).getTime();
//		} catch (ParseException ex) {
//			ex.printStackTrace();
//		}
//		this.dir = Double.NaN;
//		this.time = time;
//	}
	public VIMLocation(String id, VIMLocation loc) {
		super(loc);
		this.id = id;
		this.dir = loc.dir;
		this.time = loc.time;
	}
	public VIMLocation(JSONObject jsonLoc) {
		super(jsonLoc);
		String id=null;
		double dir=Double.NaN;
		long time=Long.MIN_VALUE;
		try { id = jsonLoc.getString("id"); } catch(JSONException e) {}
		try { dir = jsonLoc.getDouble("dir"); } catch(JSONException e) {}
		try { time = jsonLoc.getLong("time"); } catch(JSONException e) {}
		this.id = id;
		this.dir = dir;
		this.time = time;
	}
	public JSONObject toJSON() {
		JSONObject jsonLoc = super.toJSON();
		if(this.id!=null) {
			try { jsonLoc.put("id", this.id); } catch(JSONException e) {}
		}
		if(CoordSystem.isValidDir(this.dir)) {
			try { jsonLoc.put("dir",this.dir); } catch(JSONException e) {}
		}
		if(this.time!=Long.MIN_VALUE) {
			try { jsonLoc.put("time",this.time); } catch(JSONException e) {}
		}
		return jsonLoc;
	}
	public boolean isValid() {
		return 
			CoordSystem.isValidCoord(x) && 
			CoordSystem.isValidCoord(y) &&
			CoordSystem.isValidCoord(z) &&
			CoordSystem.isValidCoord(dir)
			; 
	}
	// converter
	public String toString() {
		SimpleDateFormat sdForm = new SimpleDateFormat("yyy-MM-dd/hh:mm:ss.SSS");
		String strLoc = String.format("%s\t%5.1f\t(%.1f, %.1f, %.1f)\t%s",
			(this.id==null ? "NULL": this.id),
			this.dir,
			this.x, this.y, this.z, 
			sdForm.format(this.time)
		);
		return strLoc;
	}
	// for distance
	public static double getDist(VIMLocation p1, VIMLocation p2) {
		VIMPoint pt1 = new VIMPoint( (VIMPosition)p1 );
		VIMPoint pt2 = new VIMPoint( (VIMPosition)p2 );
		return pt1.getDist2(pt2);
	}
//	public static VIMLocation getLocationByDist(VIMLocation beg, VIMLocation end, 
//		int step, double minDistPerStep, long begTime, long timePerStep) {
//		double dist = getDist(end, beg);
//		double numStep = Math.floor(dist / minDistPerStep);
//		double distPerStep = dist / numStep;
//		
//		double distWeight = (double)step * distPerStep / dist;
//		VIMPoint begPt = new VIMPoint( (VIMPosition)beg );
//		VIMPoint endPt = new VIMPoint( (VIMPosition)end );
//		VIMPoint newPt = null;
//		if(step < numStep)
//			newPt = begPt.getMovingPos3DByWeight(endPt, distWeight);
//		else
//			newPt = endPt;
//		long time = begTime + timePerStep * (long)step;
//		double dir = begPt.getDirection(endPt);
//		return new VIMLocation(newPt, dir, time);
//	}
//	public static VIMLocation getLocationByTime(VIMLocation beg, VIMLocation end, 
//		long time) {
//		double timeWeight = (double)(time - beg.time) / (double)(end.time - beg.time);
//		VIMPoint begPt = new VIMPoint( (VIMPosition)beg );
//		VIMPoint endPt = new VIMPoint( (VIMPosition)end );
//		VIMPosition newPt = begPt.getMovingPos3DByWeight(endPt, timeWeight);
//		double dir = begPt.getDirection(endPt);
//		return new VIMLocation(newPt, dir, time);
//	}
}
