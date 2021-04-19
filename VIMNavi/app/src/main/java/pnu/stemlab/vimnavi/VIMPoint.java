package pnu.stemlab.vimnavi;

import org.json.JSONObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class VIMPoint extends VIMPosition {
	public VIMPoint(double x,double y) {
		super(x, y);
	}
	public VIMPoint(double x,double y,double z) {
		super(x, y, z);
	}
	public VIMPoint(VIMPosition pos) {
		super(pos);
	}
	public VIMPoint(VIMPoint pt) {
		super((VIMPosition)pt);
	}
	public VIMPoint(JSONObject jsonPoint) {
		super(jsonPoint);
	}
	public Point toJTSPoint() {
		GeometryFactory gfactory = new GeometryFactory();
		Point jtsPoint = gfactory.createPoint(
			new Coordinate(x, y)
		);
		return jtsPoint;
	}
	// for testing point overlap
	public boolean isPointOverlap(VIMPoint pt) {
		return this.x == pt.x && this.y == pt.y;
	}
	// for distance
	public double getDist2(VIMPoint pt) {
		double dx = this.x - pt.x;
		double dy = this.y - pt.y;
		return Math.sqrt(dx*dx + dy*dy);
	}
	public double getDist3(VIMPoint pt) {
		double dx = this.x - pt.x;
		double dy = this.y - pt.y;
		double dz = this.z - pt.z;
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	// for direction
	public double getDirection(VIMPoint pt) {
		// 
		double dx = pt.x - this.x;
		double dist = getDist2(pt);
		double cosineValue = dx / dist;
		double radianAngle = Math.acos( cosineValue ) ;
		double degreeAngle = Math.toDegrees( radianAngle );
		double side = pt.y - this.y;
		return side>=0.0 ? degreeAngle : 360.0-degreeAngle;
	}
//	public double getDist2MilliMeterCoord(VIMPoint pt2) {
//		// the unit of x and y is millimeter
//		// the unit of return value is meter
//		double dx = this.x - pt2.x;
//		double dy = this.y - pt2.y;
//		return Math.sqrt(dx*dx + dy*dy);
//	}
//	public double getDist2MeterCoord(VIMPoint pt2) {
//		// the unit of x and y is meter
//		// the unit of return value is meter
//		double dx = this.x - pt2.x;
//		double dy = this.y - pt2.y;
//		return Math.sqrt(dx*dx + dy*dy);
//	}
//	public double getDist2GPSCoord(VIMPoint pt2) {
//		// the unit of x and y is degree (WGS84)
//		// the unit of return value is meter
//		double x1 = this.x;
//		double y1 = this.y;
//		double x2 = pt2.x;
//		double y2 = pt2.y;
//
//		if ((y1 == y2) && (x1 == x2))
//			return 0;
//		double rtheta = Math.toRadians(x1 - x2);
//		double ry1 = Math.toRadians(y1);
//		double ry2 = Math.toRadians(y2);
//		double dist = Math.sin(ry1) * Math.sin(ry2)
//				+ Math.cos(ry1) * Math.cos(ry2) * Math.cos(rtheta);
//		dist = Math.acos(dist);
//		dist = Math.toDegrees(dist);
//		dist = dist * 60 * 1853.159616;
//		return dist;
//	}
	public boolean isEqual2D(VIMPoint pt) {
		return this.x == pt.x && this.y == pt.y;
	}
	public boolean isEqual3D(VIMPoint pt) {
		return this.x == pt.x && this.y == pt.y && this.z == pt.z;
	}
	// for moving point
	public VIMPoint getMovingPos2DByWeight(VIMPoint target, double weight) {
		if(weight==0.0)
			return this;
		if(weight==1.0)
			return target;
		double x = this.x + weight*(target.x-this.x);
		double y = this.y + weight*(target.y-this.y);
		return new VIMPoint(x,y);
	}
	public VIMPoint getMovingPos3DByWeight(VIMPoint target, double weight) {
		if(weight==0.0)
			return this;
		if(weight==1.0)
			return target;
		double x = this.x + weight*(target.x-this.x);
		double y = this.y + weight*(target.y-this.y);
		double z = this.z + weight*(target.z-this.z);
		return new VIMPoint(x,y,z);
	}
	public VIMPoint getMovingPos2DByDist(VIMPoint target, double dist) {
		double targetDist = getDist2(target);
		if(dist==0.0)
			return this;
		if(dist==targetDist)
			return target;
		double weight = dist / targetDist;
		return getMovingPos2DByWeight(target, weight);
	}
	public VIMPoint getMovingPos3DByDist(VIMPoint target, double dist) {
		double targetDist = getDist2(target);
		if(dist==0.0)
			return this;
		if(dist==targetDist)
			return target;
		double weight = dist / targetDist;
		return getMovingPos3DByWeight(target, weight);
	}
//	// direction
//	public double getDirection(VIMPoint target) {
//		VIMPoint basisAxis = new VIMPoint(this.x + 1.0, this.y);
//		double radianCosValue = target.getPointCosinValue(this, basisAxis);
//		double side = target.getPointSide(this, basisAxis);
//		return Math.toDegrees(radianCosValue) + (side<0.0 ? 180.0 : 0.0);
//	}
//	public double getPointSide(VIMPoint segSrc, VIMPoint segDst) {
//		// return_value  > 0: pt is on the left side of the line segment from segSrc to segDst.
//		// return_value  < 0: pt is on the right side of the line segment from segSrc to segDst.
//		// return_value == 0: pt is on the line segment from segSrc to segDst.
//		return (segDst.x-segSrc.x)*(this.y-segSrc.y)-(this.x-segSrc.x)*(segDst.y-segSrc.y);
//	}
//	public double getPointCosinValue(VIMPoint segSrc, VIMPoint segDst) {
//		// return_value ==  1.0: the angle is 0.0 degree
//		// return_value ==  0.0: the angle is 90.0 degrees
//		// return_value == -1.0: the angle is 180.0 degrees
//		double vx = this.x - segSrc.x;
//		double vy = this.y - segSrc.y;
//		double wx = segDst.x - segSrc.y;
//		double wy = segDst.y - segSrc.y;
//		double innerProd = vx*wx + vy*wy;
//		return innerProd / ( this.getDist2(segSrc) * segDst.getDist2(segSrc) );
//	}
	public double getFloor() {
		return super.z;
	}
}
