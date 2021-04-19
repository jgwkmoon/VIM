package pnu.stemlab.vimnavi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Coordinate;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;

public class VIMPolygon {
	public final VIMPoint[] points;

	public VIMPolygon(VIMPoint[] points) {
		this.points = points;
	}
	public VIMPolygon(VIMPolygon pg) {
		this.points = pg.points;
	}
	public VIMPolygon(JSONObject jsonPolygon) {
		JSONArray jsonArrayPoints = null;
		try { jsonArrayPoints = jsonPolygon.getJSONArray("points"); } catch(JSONException e) {}
		this.points = new VIMPoint[jsonArrayPoints.length()];
		try {
			for(int i=0; i<jsonArrayPoints.length(); ++i)
				this.points[i] = new VIMPoint(jsonArrayPoints.getJSONObject(i));
		} catch(JSONException e) {}
	}
	public JSONObject toJSON() {
		JSONObject jsonPolygon = new JSONObject();
		if(points!=null) {
			JSONArray jsonPoints = new JSONArray();
			for(int i=0; i<points.length; ++i)
				try { jsonPoints.put(i, points[i].toJSON()); } catch(JSONException e) {}
			try { jsonPolygon.put("points", jsonPoints); } catch(JSONException e) {}
		}
		return jsonPolygon;
	}
	public Polygon toJTSPolygon() {
		GeometryFactory gfactory = new GeometryFactory();
		Coordinate coordsPoly[] = new Coordinate[points.length];
		for(int i=0; i<points.length; ++i) {
			coordsPoly[i] = new Coordinate(points[i].x, points[i].y);
		}
		Polygon jtsPolygon = gfactory.createPolygon(coordsPoly);
		return jtsPolygon;
	}
	// for distance
	public double getDist2(VIMPoint pt) {
		Polygon jtsPolygon = toJTSPolygon();
		Point jstPoint = pt.toJTSPoint();
		return new DistanceOp(jtsPolygon, jstPoint).distance();
	}
	// for testing intersection
	public boolean intersect(VIMPoint pt) {
		Point jtsPoint = pt.toJTSPoint();
		Polygon jtsPolygon = toJTSPolygon();
		return jtsPolygon.intersects(jtsPoint);
	}
	public boolean intersect(VIMSegment sg) {
		LineString lineSeg = sg.toJTSLineString();
		Polygon jtsPolygon = toJTSPolygon();
		return jtsPolygon.intersects(lineSeg);
	}
	public double getFloor() {
		if(points.length<1)
			return Double.NaN; 
		for(int i=0; i+1<points.length; ++i) {
			if(points[i].z != points[i+1].z)
				return Double.NaN; 
		}
		return points[0].z;
	}
}
