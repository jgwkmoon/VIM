package pnu.stemlab.vimnavi;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class VIMSegment {
	public final VIMPoint src;
	public final VIMPoint dst;
	
	public VIMSegment(VIMPoint src, VIMPoint dst) {
		this.src = src;
		this.dst = dst;
	}
	public VIMSegment(VIMSegment sg) {
		this.src = sg.src;
		this.dst = sg.dst;
	}
	public LineString toJTSLineString() {
		GeometryFactory gfactory = new GeometryFactory();
		Coordinate coordsSeg[] = {
			new Coordinate(src.x, src.y), 
			new Coordinate(dst.x, dst.y)
		};
		LineString jtsLineSeg = gfactory.createLineString(coordsSeg);
		return jtsLineSeg;
	}
	// for testing point overlap
	public boolean isPointOverlap() {
		return this.src.isPointOverlap(dst);
	}
	public boolean isPointOverlap(VIMPoint pt) {
		return isPointOverlap() || src.isPointOverlap(pt) || dst.isPointOverlap(pt);
	}
	// for direction
	public double getDirection() {
		return src.getDirection(dst);
	}
	public double getDirection(VIMPoint pt) {
		double cosineValue = getCosVal(pt);
		double radianAngle = Math.acos(cosineValue) ;
		double degreeAngle = Math.toDegrees( radianAngle );
		double side = getPointSide(pt);
		return side>=0.0 ? degreeAngle : 360.0-degreeAngle;
	}
	public double getDirection(VIMSegment seg) {
		VIMPoint targetPt = new VIMPoint(
			this.src.x + seg.dst.x - seg.src.x,
			this.src.y + seg.dst.y - seg.src.y
		);
		return getDirection(targetPt);
	}
	public double getPointSide(VIMPoint pt) {
		// return_value  > 0: pt is on the left side of this line segment.
		// return_value  < 0: pt is on the right side of this line segment.
		// return_value == 0: pt is on this line segment.
		return (dst.x-src.x)*(pt.y-src.y)-(pt.x-src.x)*(dst.y-src.y);
	}
	public double getCosVal(VIMPoint pt) {
		// Cosine value between VIMSegment(src,dst) and VIMSegment(src,pt).
		// return_value ==  1.0: the angle is 0.0 degree
		// return_value ==  0.0: the angle is 90.0 degrees
		// return_value == -1.0: the angle is 180.0 degrees
		double vx = pt.x - src.x;
		double vy = pt.y - src.y;
		double wx = dst.x - src.x;
		double wy = dst.y - src.y;
		double innerProd = vx*wx + vy*wy;
		double d1 = src.getDist2(pt);
		double d2 = src.getDist2(dst);
		double retv = innerProd / ( d1 * d2 );
		return retv;
	}
	public double getCosVal(VIMSegment sg) {
		// Cosine value between VIMSegment(this) and VIMSegment(sg).
		// return_value ==  1.0: the angle is 0.0 degree
		// return_value ==  0.0: the angle is 90.0 degrees
		// return_value == -1.0: the angle is 180.0 degrees
		double vx = sg.dst.x - sg.src.x;
		double vy = sg.dst.y - sg.src.y;
		double wx = dst.x - src.x;
		double wy = dst.y - src.y;
		double innerProd = vx*wx + vy*wy;
		double d1 = sg.src.getDist2(sg.dst);
		double d2 = src.getDist2(dst);
		double retv = innerProd / ( d1 * d2 );
		return retv;
	}
	// for distance
	public double getDist2() {
		return src.getDist2(dst);
	}
	public double getDist2(VIMPoint pt) {
		LineString jtsLineString = toJTSLineString();
		Point jstPoint = pt.toJTSPoint();
		return new DistanceOp(jtsLineString, jstPoint).distance();
	}
	// for testing intersection
	public boolean intersect(VIMPoint pt) {
		Point jtsPoint = pt.toJTSPoint();
		LineString jtsLineString = this.toJTSLineString();
		return jtsLineString.intersects(jtsPoint);
	}
	public boolean intersect(VIMSegment sg) {
		LineString jtsLineString1 = sg.toJTSLineString();
		LineString jtsLineString2 = this.toJTSLineString();
		return jtsLineString2.intersects(jtsLineString1);
	}
	public double getFloor() {
		return src.z == dst.z ? src.z : Double.NaN;
	}
}
