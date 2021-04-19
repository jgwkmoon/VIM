package pnu.stemlab.vimnavi;

import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.NodeList;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GPXTrajectory extends ArrayList<GPSLocation> {
	private static final long serialVersionUID = 1L;
	
	public GPXTrajectory(InputStream gpxIS) throws IOException {
		super();

		DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
		DocumentBuilder bd = null;
		try {
			bd = bf.newDocumentBuilder();
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		}

		try {
			Document doc = bd.parse(gpxIS);
			NodeList nodeList = doc.getElementsByTagName("trkpt");
			for(int i=0;i<nodeList.getLength(); ++i) {
				super.add( new GPSLocation( nodeList.item(i) ) );
			}
		} catch(SAXException e) {
			e.printStackTrace();
		}
	}
	public GPXTrajectory(GPXTrajectory gpx, long begTime, long cycle, double minDistPerCycle) {
		super();
//		interpolateOnEachEdge(gpx, begTime, cycle, minDistPerCycle);
		interpolateOnWholeTraj(gpx, begTime, cycle, minDistPerCycle);
	}
	public double getDistOfTraj() {
	// Calculate the distance of the trajectory.
		double accDist = 0.0;
		for(int i=0; i+1<size(); ++i) {
			VIMPoint curPt = new VIMPoint( (VIMPosition) this.get(i) );
			VIMPoint nxtPt = new VIMPoint( (VIMPosition) this.get(i+1) );
			double edgeDist = curPt.getDist2(nxtPt);
			accDist += edgeDist;
		}
		return accDist;
	}
	public VIMPoint interpolateOnWholeTraj(double movDist, boolean endMatch) {
	// Calculate the point away from the starting point 
	//	on the given trajectory 
	//	by movDist which is given distance.
	// If the calculated point is outside the range of the given trajectory,
	//	the parameter endMatch indicates whether the point to be returned is replaced 
	//	by the last point of the trajectory or not.
		
		if( !(size()>1) )			// trajectory size must be greater than 1.
			return null;
		
		if(movDist == 0.0)
			return new VIMPoint( (VIMPosition) this.get(0) );
		
		double accDist = 0.0;		// accumulated distance
		VIMPoint curPt = null;	   // current point
		VIMPoint nxtPt = null;	   // next point
		double edgeDist = 0.0;	   // edge distance from the current point to the next point
		
		for(int i=0; i+1<size(); ++i) {
			curPt = new VIMPoint( (VIMPosition) this.get(i) );
			nxtPt = new VIMPoint( (VIMPosition) this.get(i+1) );
			edgeDist = curPt.getDist2(nxtPt);
			if(accDist < movDist && movDist <= accDist+edgeDist) {
				double remainDist = movDist - accDist;
				return movDist == accDist+edgeDist ? nxtPt : curPt.getMovingPos2DByDist(nxtPt, remainDist);
			}
			accDist += edgeDist;
		}
		
		// Here, the point to be calculated is outside the given trajectory.
		//	and nxtPt is the last point of that trajectory. 
		
		if(endMatch)
			return nxtPt;
		
		double remainDist = movDist - accDist;
		return movDist == accDist+edgeDist ? nxtPt : curPt.getMovingPos2DByDist(nxtPt, edgeDist + remainDist);
	}
	public void interpolateOnWholeTraj(GPXTrajectory gpx, long begTime, long cycle, double distPerCycle) {
	//  * Equal time interval interpolation on the entire routing path
	//
	//	- Given Path:	   v ----------- v ----- v ------------------------------ v ------------ v
	//
	//	- Desired Interval: v ------ v
	//
	//	- Interpolated1:	v ------ v ------ v ------ v ------ v ------ v ------ v ------ v ---- v  (end match method)
	//	- Interpolated2:	v ------ v ------ v ------ v ------ v ------ v ------ v ------ v ------ v
		super.clear();		
		double trajDist = gpx.getDistOfTraj();
		double movDist=0.0;
		
		for(movDist=0.0; movDist<trajDist; movDist+=distPerCycle) {
			VIMPoint pt = gpx.interpolateOnWholeTraj(movDist, true); // end match method
			if(pt!=null) {
				GPSLocation loc = new GPSLocation((VIMPosition) pt, begTime);
				super.add(loc);
				begTime += cycle;
			}
		}
		VIMPoint pt = gpx.interpolateOnWholeTraj(movDist, true); // end match method
		GPSLocation loc = new GPSLocation((VIMPosition) pt, begTime);
		super.add(loc);
	}
	public void interpolateOnEachEdge(GPXTrajectory gpx, long begTime, long cycle, double minDistPerCycle) {
	// * Equal time interval interpolation on each edge of routing path
	//		   - Given Path:	   v ----------- v ----- v ------------------------------ v ------------ v
	//		   - Desired Interval: v ------ v
	//											 v ------ v			
	//													 v ------ v ------ v ------ v
	//																					  v ------ v
	//		   - Interpolated:	 v ----------- v ----- v -------- v -------- v -------- v ------------ v 
	//		   - split_num = maximum ( floor( edge_dist / speed ) , 1 )   (Minimal time interval)
		super.clear();
		GPSLocation newLoc = new GPSLocation(gpx.get(0), begTime);
		super.add(newLoc);

		for(int i=0; i<gpx.size()-1; ++i) {
			GPSLocation beg = gpx.get(i);
			GPSLocation end = gpx.get(i+1);
			double dist = GPSLocation.getDist(end, beg);
			int numStep = 
				(int)Math.max(Math.floor(dist/minDistPerCycle), 1.0);
			double distPerStep = dist / (double)numStep;
			for(int step=1; step<=numStep; ++step) {
				newLoc = GPSLocation.getLocationByDist(beg, end, step, 
						distPerStep, begTime, cycle);
				super.add(newLoc);
			}
			begTime += cycle * (long)numStep;
		}
	}
	public long getTimeGap(int index) {
		if(index>=super.size())
			return 0;
		long begTrajTime = super.get(0).time;
		long ithTrajTime = super.get(index).time;
		return ithTrajTime - begTrajTime;
	}
	public String toString() {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < super.size(); ++i) {
			str.append( i + ": " + super.get(i).toString() +"\n");
		}
		return str.toString();
	}
}
