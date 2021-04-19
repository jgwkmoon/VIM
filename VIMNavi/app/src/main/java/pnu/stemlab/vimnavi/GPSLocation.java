package pnu.stemlab.vimnavi;

import java.util.Date;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class GPSLocation extends VIMPosition {
	public final String id;
	public final long time;

	public GPSLocation(double x,double y,double z,long time) {
		super(x, y, z);
		this.id = null;
		this.time = time;
	}
	public GPSLocation( VIMPosition pos,long time) {
		super(pos);
		this.id = null;
		this.time = time;
	}
	public GPSLocation(GPSLocation gpsLoc) {
		super(gpsLoc);
		this.id = null;
		this.time = gpsLoc.time;
	}
	public GPSLocation(String id, double x,double y,double z,long time) {
		super(x, y, z);
		this.id = id;
		this.time = time;
	}
	public GPSLocation(String id, VIMPosition pos,long time) {
		super(pos);
		this.id = id;
		this.time = time;
	}
	public GPSLocation(String id, GPSLocation gpsLoc) {
		super(gpsLoc);
		this.id = id;
		this.time = gpsLoc.time;
	}
	public GPSLocation(Node gpxNode) {
		super(gpxNode);
		Element e = (Element) gpxNode;
		NodeList nList;

		nList = e.getElementsByTagName("name");
		String id = null;
		if(nList.getLength()>0)
			id = nList.item(0).getTextContent();
		this.id = id;
		
		nList = e.getElementsByTagName("time");
		long time = Long.MIN_VALUE;
		if(nList.getLength()>0) {
			String str = nList.item(0).getTextContent();
			SimpleDateFormat fmt 
				= new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'");
			try {
				time = fmt.parse(str).getTime();
			} catch (ParseException ex) {
				ex.printStackTrace();
			}
		}
		this.time = time;
	}
	public boolean isValid() {
		return 
			CoordSystem.isValidCoord(x) && 
			CoordSystem.isValidCoord(y) &&
			CoordSystem.isValidCoord(z); 
	}
//	// getter
//	public double getLongitude() { return this.x; }
//	public double getLatitude() { return this.y; }
//	public double getElevation() { return this.z; }
//	public long getTime() { return this.time; }
	// converter
	public String toString() {
		return String.format("ID:%s X:%.6f Y:%.6f Z:%.3f T:%s",
			(this.id==null ? "NULL": this.id),
			this.x, this.y, this.z,
			new Date(this.time).toString()
		);
	}
	// for distance
	public static double getDist(GPSLocation p1, GPSLocation p2) {
		VIMPoint pt1 = new VIMPoint( (VIMPosition)p1 );
		VIMPoint pt2 = new VIMPoint( (VIMPosition)p2 );
		return pt1.getDist2(pt2);
	}
	public static GPSLocation getLocationByDist(GPSLocation beg, GPSLocation end, 
		int step, double minDistPerStep, long begTime, long timePerStep) {
		double dist = getDist(end, beg);
		double numStep = Math.floor(dist / minDistPerStep);
		double distPerStep = dist / numStep;
		
		double distWeight = (double)step * distPerStep / dist;
		VIMPoint begPt = new VIMPoint( (VIMPosition)beg );
		VIMPoint endPt = new VIMPoint( (VIMPosition)end );
		VIMPoint newPt = null;
		if(step < numStep)
			newPt = begPt.getMovingPos3DByWeight(endPt, distWeight);
		else
			newPt = endPt;
		long time = begTime + timePerStep * (long)step;
		return new GPSLocation(newPt, time);
	}
	public static GPSLocation getLocationByTime(
		GPSLocation beg, GPSLocation end, 
		long time, String coordSystem) {
		double timeWeight = (double)(time - beg.time) / (double)(end.time - beg.time);
		VIMPoint begPt = new VIMPoint( (VIMPosition)beg );
		VIMPoint endPt = new VIMPoint( (VIMPosition)end );
		VIMPosition newPt = begPt.getMovingPos3DByWeight(endPt, timeWeight);
		return new GPSLocation(newPt, time);
	}
}
