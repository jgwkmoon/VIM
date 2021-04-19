package pnu.stemlab.vimnavi;

import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import org.w3c.dom.NodeList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class VIMTrajectory extends ArrayList<VIMLocation> {
	private static final long serialVersionUID = 1L;

	private static double[] accumulatedDist(GPXTrajectory gpx) {
		double accuDist[] = new double[gpx.size()];
		accuDist[0] = 0.0;
		for(int i=1; i<accuDist.length; ++i) {
			VIMPoint prvPt = new VIMPoint( (VIMPosition) gpx.get(i-1) );
			VIMPoint nxtPt = new VIMPoint( (VIMPosition) gpx.get(i) );
			double edgeDist = prvPt.getDist2(nxtPt);
			accuDist[i] = accuDist[i-1] + edgeDist;
		}
		return accuDist;
	}
	private static int getIndex(double accuDist[], double movDist) {
		// Calculate the index of the given accumulated distances of edges on GPX trajectory 
		//	by movDist which is given distance.
		// accuDist[i] means the distance between vertex[0] and vertex[i], so accuDist[0] is always zero.
		int i=0;
		for(i=0; i<accuDist.length && !(movDist <= accuDist[i]); ++i)
			;
		if( i==0 ) 
			i=1;
		else if( !(i<accuDist.length) )
			i = accuDist.length-1;
		return i;
	}
	private static boolean hasSubID(String id) {
		int len = id.length();
		int i;
		for(i=0; i<len && !(id.codePointAt(i)=='$'); ++i)
			;
		return i<len;
	}
	private static String getMainID(String id) {
		int len = id.length();
		int i;
		for(i=0; i<len && !(id.codePointAt(i)=='$'); ++i)
			;
		return id.substring(0,i-1);
	}
	private static boolean isSameKindOfID(String id1, String id2) {
		String mainID1 = getMainID(id1);
		String mainID2 = getMainID(id2);
		return mainID1.equals(mainID2);
	}
	private void putSubID() {
		String prevID = "";
		int subIDNum = 0;
		for(int i=0; i<this.size(); ++i) {
			if(hasSubID(this.get(i).id)) {
				if(!isSameKindOfID(prevID, this.get(i).id))
					subIDNum = 0;
				this.get(i).id += String.format("%02d", ++subIDNum);
			} else {
				subIDNum = 0;
			}
			prevID = this.get(i).id;
		}
	}
	// constructors
	public VIMTrajectory(InputStream jsonIS) {
		super();
		byte inputByteStr[];
		try {
			inputByteStr = new byte[jsonIS.available()];
			jsonIS.read(inputByteStr);
			String str = new String(inputByteStr);
			JSONArray jsonVIMTraj = new JSONArray(str);
			for(int i=0; i<jsonVIMTraj.length(); ++i) {
				VIMLocation vimLoc = new VIMLocation(jsonVIMTraj.getJSONObject(i));
				this.add( vimLoc );
			}
		} catch(JSONException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	public VIMTrajectory(JSONArray jsonVIMTraj) {
		try {
			for(int i=0; i<jsonVIMTraj.length(); ++i) {
				VIMLocation vimLoc = new VIMLocation(jsonVIMTraj.getJSONObject(i));
				this.add( vimLoc );
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	public JSONArray toJSON() {
		JSONArray jsonVIMTraj = new JSONArray();
		for(VIMLocation loc: this)
			jsonVIMTraj.put(loc.toJSON());
		return jsonVIMTraj;
	}
	public VIMTrajectory() {
		super();
	}
	public VIMTrajectory(GPXTrajectory gpx, double distPerCycle) {
		super();
		interpolateOnWholeTraj(gpx, distPerCycle, true);
//		interpolateOnEachEdge(gpx, distPerCycle);
	}
	public void interpolateOnWholeTraj(GPXTrajectory gpx, double distPerCycle, boolean endMatch) {
		//  * Equal time interval interpolation on the entire routing path
		//
		//	- Given Path:	   v ----------- v ----- v ------------------------------ v ------------ v
		//
		//	- Desired Interval: v ------ v
		//
		//	- Interpolated1:	v ------ v ------ v ------ v ------ v ------ v ------ v ------ v ---- v  (end match method)
		//	- Interpolated2:	v ------ v ------ v ------ v ------ v ------ v ------ v ------ v ------ v
		double accuDist[] = accumulatedDist(gpx);
		double trajDist = accuDist[accuDist.length-1];
		
		double movDist;
		for(movDist=0.0; movDist<trajDist+distPerCycle; movDist+=distPerCycle) {
			if(trajDist<=movDist && movDist<trajDist+distPerCycle && endMatch) 
				movDist = trajDist;
			int i = getIndex(accuDist, movDist);
			VIMPoint begPt = new VIMPoint((VIMPosition)gpx.get(i-1));
			VIMPoint endPt = new VIMPoint((VIMPosition)gpx.get(i));
			double remainDist = movDist - (i-1<0 ? 0.0: accuDist[i-1]);
			VIMPoint newPt = begPt.getMovingPos3DByDist(endPt, remainDist);
			double dir = begPt.getDirection(endPt);
			String id = newPt.isEqual2D(endPt) ? gpx.get(i).id : newPt.isEqual2D(begPt) ? gpx.get(i-1).id : gpx.get(i-1).id + "$";
			VIMLocation newLoc = new VIMLocation(id, (VIMPosition)newPt, dir, 0L);
   			this.add(newLoc);
		}
		putSubID();
	}
	public void interpolateOnEachEdge(GPXTrajectory gpx, double givenDistPerCycle, int type, boolean waitTurn) {
		// * type: 0(same distance), 1(minimal distance), 2(maximal distance)
		// * Equal time interval interpolation on each edge of routing path by same distance
		//		   - Given Path:	   v ----------- v ----- v ------------------------------ v ------------ v
		//		   - Desired Interval: v ------ v
		//											 v ------ v			
		//													 v ------ v ------ v ------ v
		//																					  v ------ v
		//		   - Interpolated:	 v ------ v -- v ----- v ------ v ------ v ------ v --- v ------ v --- v 
		//		   - split_num = maximum ( floor( edge_dist / speed ) , 1 )   (Minimal time interval)
		// * Equal time interval interpolation on each edge of routing path by minimal distance
		//		   - Given Path:	   v ----------- v ----- v ------------------------------ v ------------ v
		//		   - Desired Interval: v ------ v
		//											 v ------ v			
		//													 v ------ v ------ v ------ v
		//																					  v ------ v
		//		   - Interpolated:	 v ------ v -- v ----- v ------ v ------ v ------ v --- v ------ v --- v 
		//		   - split_num = maximum ( floor( edge_dist / speed ) , 1 )   (Minimal time interval)
		// * Equal time interval interpolation on each edge of routing path by maximal distance
		//		   - Given Path:	   v ----------- v ----- v ------------------------------ v ------------ v
		//		   - Desired Interval: v ------ v
		//											 v ------ v			
		//													 v ------ v ------ v ------ v
		//																					  v ------ v
		//		   - Interpolated:	 v ----------- v ----- v -------- v -------- v -------- v ------------ v 
		//		   - split_num = maximum ( floor( edge_dist / speed ) , 1 )   (Minimal time interval)
		if( !(0<=type && type<=2) )
			return;
		super.clear();
		
		VIMPoint begPt = new VIMPoint((VIMPosition)gpx.get(0));
		VIMPoint endPt = new VIMPoint((VIMPosition)gpx.get(1));
		VIMPoint newPt = begPt;
		double dir = begPt.getDirection(endPt);
		String id = gpx.get(0).id;
		VIMLocation newLoc = new VIMLocation(id, (VIMPosition)newPt, dir, 0L);
		super.add(newLoc);

		for(int i=0; i<gpx.size()-1; ++i) {
			begPt = new VIMPoint((VIMPosition)gpx.get(i));
			endPt = new VIMPoint((VIMPosition)gpx.get(i+1));
			double edgeDist = begPt.getDist2(endPt);
			
			double doubleNumCycle = type==1 ? 
				Math.floor(edgeDist/givenDistPerCycle) : 
				Math.ceil(edgeDist/givenDistPerCycle);
			int numCycle = (int)Math.max(doubleNumCycle, 1.0);
			double distPerCycle = type==0 ? givenDistPerCycle : edgeDist / (double)numCycle;
			
			double accuDist = 0.0;
			for(int step=1; step<=numCycle; ++step) {
				accuDist += step<numCycle ? distPerCycle : edgeDist-accuDist;
				newPt = begPt.getMovingPos3DByDist(endPt, accuDist);
				dir = begPt.getDirection(endPt);
				id = newPt.isEqual2D(endPt) ? gpx.get(i+1).id : gpx.get(i).id + "$";
				newLoc = new VIMLocation(id, (VIMPosition)newPt, dir, 0L);
				super.add(newLoc);
				// insert turing point
				if(waitTurn && newPt.isEqual2D(endPt) && i+2<gpx.size()) {
					VIMPoint nextEndPt = new VIMPoint((VIMPosition)gpx.get(i+2));
					double newDir = endPt.getDirection(nextEndPt);
					newLoc = new VIMLocation(id, (VIMPosition)newPt, newDir, 0L);
					super.add(newLoc);
				}
			}
		}
		putSubID();
	}
//	public void interpolateOnEachEdgeByDist(GPXTrajectory gpx, double givenDistPerCycle) {
//		// * Equal time interval interpolation on each edge of routing path
//		//		   - Given Path:	   v ----------- v ----- v ------------------------------ v ------------ v
//		//		   - Desired Interval: v ------ v
//		//											 v ------ v			
//		//													 v ------ v ------ v ------ v
//		//																					  v ------ v
//		//		   - Interpolated:	 v ------ v -- v ----- v ------ v ------ v ------ v --- v ------ v --- v 
//		//		   - split_num = maximum ( floor( edge_dist / speed ) , 1 )   (Minimal time interval)
//		super.clear();
//		
//		VIMPoint begPt = new VIMPoint((VIMPosition)gpx.get(0));
//		VIMPoint endPt = new VIMPoint((VIMPosition)gpx.get(1));
//		VIMPoint newPt = begPt;
//		double dir = begPt.getDirection(endPt);
//		String id = gpx.get(0).id;
//		VIMLocation newLoc = new VIMLocation(id, (VIMPosition)newPt, dir, 0L);
//		super.add(newLoc);
//
//		for(int i=0; i<gpx.size()-1; ++i) {
//			begPt = new VIMPoint((VIMPosition)gpx.get(i));
//			endPt = new VIMPoint((VIMPosition)gpx.get(i+1));
//			double edgeDist = begPt.getDist2(endPt);
//			
//			// dist
//			double doubleNumCycle = Math.ceil(edgeDist/givenDistPerCycle);
//			int numCycle = (int)Math.max(doubleNumCycle, 1.0);
//			double distPerCycle = givenDistPerCycle;
//			
//			double accuDist = 0.0;
//			for(int step=1; step<=numCycle; ++step) {
//				accuDist += step<numCycle ? distPerCycle : edgeDist-accuDist;
//				newPt = begPt.getMovingPos3DByDist(endPt, accuDist);
//				dir = begPt.getDirection(endPt);
//				id = newPt.isEqual2D(endPt) ? gpx.get(i+1).id : gpx.get(i).id + "$";
//				newLoc = new VIMLocation(id, (VIMPosition)newPt, dir, 0L);
//				super.add(newLoc);
//				// insert turing point
//				if(newPt.isEqual2D(endPt) && i+2<gpx.size()) {
//					VIMPoint nextEndPt = new VIMPoint((VIMPosition)gpx.get(i+2));
//					double newDir = endPt.getDirection(nextEndPt);
//					newLoc = new VIMLocation(id, (VIMPosition)newPt, newDir, 0L);
//					super.add(newLoc);
//				}
//			}
//		}
//		putSubID();
//	}
//	public void interpolateOnEachEdgeByMinDist(GPXTrajectory gpx, double minDistPerCycle) {
//		// * Equal time interval interpolation on each edge of routing path
//		//		   - Given Path:	   v ----------- v ----- v ------------------------------ v ------------ v
//		//		   - Desired Interval: v ------ v
//		//											 v ------ v			
//		//													 v ------ v ------ v ------ v
//		//																					  v ------ v
//		//		   - Interpolated:	 v ----------- v ----- v -------- v -------- v -------- v ------------ v 
//		//		   - split_num = maximum ( floor( edge_dist / speed ) , 1 )   (Minimal time interval)
//		super.clear();
//		
//		VIMPoint begPt = new VIMPoint((VIMPosition)gpx.get(0));
//		VIMPoint endPt = new VIMPoint((VIMPosition)gpx.get(1));
//		VIMPoint newPt = begPt;
//		double dir = begPt.getDirection(endPt);
//		String id = gpx.get(0).id;
//		VIMLocation newLoc = new VIMLocation(id, (VIMPosition)newPt, dir, 0L);
//		super.add(newLoc);
//
//		for(int i=0; i<gpx.size()-1; ++i) {
//			begPt = new VIMPoint((VIMPosition)gpx.get(i));
//			endPt = new VIMPoint((VIMPosition)gpx.get(i+1));
//			double edgeDist = begPt.getDist2(endPt);
//			
//			// minDist
//			double doubleNumCycle = Math.floor(edgeDist/minDistPerCycle);
//			int numCycle = (int)Math.max(doubleNumCycle, 1.0);
//			double distPerCycle = edgeDist / (double)numCycle;
//			
//			double accuDist = 0.0;
//			for(int step=1; step<=numCycle; ++step) {
//				accuDist += step<numCycle ? distPerCycle : edgeDist-accuDist;
//				newPt = begPt.getMovingPos3DByDist(endPt, accuDist);
//				dir = begPt.getDirection(endPt);
//				id = newPt.isEqual2D(endPt) ? gpx.get(i+1).id : gpx.get(i).id + "$";
//				newLoc = new VIMLocation(id, (VIMPosition)newPt, dir, 0L);
//				super.add(newLoc);
//				// insert turing point
//				if(newPt.isEqual2D(endPt) && i+2<gpx.size()) {
//					VIMPoint nextEndPt = new VIMPoint((VIMPosition)gpx.get(i+2));
//					double newDir = endPt.getDirection(nextEndPt);
//					newLoc = new VIMLocation(id, (VIMPosition)newPt, newDir, 0L);
//					super.add(newLoc);
//				}
//			}
//		}
//		putSubID();
//	}
//	public void interpolateOnEachEdgeByMaxDist(GPXTrajectory gpx, double maxDistPerCycle) {
//		// * Equal time interval interpolation on each edge of routing path
//		//		   - Given Path:	   v ----------- v ----- v ------------------------------ v ------------ v
//		//		   - Desired Interval: v ------ v
//		//											 v ------ v			
//		//													 v ------ v ------ v ------ v
//		//																					  v ------ v
//		//		   - Interpolated:	 v ---- v ---- v ----- v ----- v ----- v ----- v ------ v ----- v ---- v 
//		//		   - split_num = maximum ( floor( edge_dist / speed ) , 1 )   (Minimal time interval)
//		super.clear();
//		
//		VIMPoint begPt = new VIMPoint((VIMPosition)gpx.get(0));
//		VIMPoint endPt = new VIMPoint((VIMPosition)gpx.get(1));
//		VIMPoint newPt = begPt;
//		double dir = begPt.getDirection(endPt);
//		String id = gpx.get(0).id;
//		VIMLocation newLoc = new VIMLocation(id, (VIMPosition)newPt, dir, 0L);
//		super.add(newLoc);
//
//		for(int i=0; i<gpx.size()-1; ++i) {
//			begPt = new VIMPoint((VIMPosition)gpx.get(i));
//			endPt = new VIMPoint((VIMPosition)gpx.get(i+1));
//			double edgeDist = begPt.getDist2(endPt);
//			
//			// maxDist
//			double doubleNumCycle = Math.ceil(edgeDist/maxDistPerCycle);
//			int numCycle = (int)Math.max(doubleNumCycle, 1.0);
//			double distPerCycle = edgeDist / (double)numCycle;
//			
//			double accuDist = 0.0;
//			for(int step=1; step<=numCycle; ++step) {
//				accuDist += step<numCycle ? distPerCycle : edgeDist-accuDist;
//				newPt = begPt.getMovingPos3DByDist(endPt, accuDist);
//				dir = begPt.getDirection(endPt);
//				id = newPt.isEqual2D(endPt) ? gpx.get(i+1).id : gpx.get(i).id + "$";
//				newLoc = new VIMLocation(id, (VIMPosition)newPt, dir, 0L);
//				super.add(newLoc);
//				// insert turing point
//				if(newPt.isEqual2D(endPt) && i+2<gpx.size()) {
//					VIMPoint nextEndPt = new VIMPoint((VIMPosition)gpx.get(i+2));
//					double newDir = endPt.getDirection(nextEndPt);
//					newLoc = new VIMLocation(id, (VIMPosition)newPt, newDir, 0L);
//					super.add(newLoc);
//				}
//			}
//		}
//		putSubID();
//	}
	public void setTime(long begTime, long cycle) {
		long time = begTime;
		for(int i=0; i<size(); ++i) {
			get(i).time = time;
			time += cycle;
		}
	}
	public String toString() {
		StringBuffer strBuff = new StringBuffer();
		Table table = new Table();

		table.appendNewRow();
		table.addNewField("lcID");
		table.addNewField("dir");
		table.addNewField("time");
		table.addNewField("coordinate");
		table.addNewField("distFromPrev");
		VIMPoint prevPt = null;
		
		for(int i=0; i<super.size(); ++i) {
			VIMLocation curLoc = super.get(i);
			table.appendNewRow();
			table.addNewField(curLoc.id);
			table.addNewField(Format.coord(curLoc.dir));
			table.addNewField(Format.time(curLoc.time));
			VIMPoint currPt = new VIMPoint((VIMPosition)curLoc);
			table.addNewField(Format.point(currPt));
			double distFromPrev 
				= prevPt==null ? 0.0 : prevPt.getDist3(currPt);
			table.addNewField(Format.coord(distFromPrev));
			prevPt = currPt;
		}
		strBuff.append(table.toString() + "\n");
		return new String(strBuff);
	}
}
