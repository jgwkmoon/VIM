package pnu.stemlab.vimnavi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VIMRoutingPath {
	public static class NaviParam { // Navigation Parameters
		public double minDist;
		public double advDist;
		public double autoDist; // --> 오히려 autoTime을 고려해야할 듯
		public double safetyDist;
		public double landDist;
		public double minDir;
		public double distPerCycle;
		public double distPerStep;

		public NaviParam(
				double minDist, double advDist, double autoDist,
				double safetyDist, double landDist,
				double minDir, double distPerCycle, double distPerStep) {
			this.minDist = minDist;
			this.advDist = advDist;
			this.autoDist = autoDist;
			this.safetyDist = safetyDist;
			this.landDist = landDist;
			this.minDir = minDir;
			this.distPerCycle = distPerCycle;
			this.distPerStep = distPerStep;
		}
	}

	public List<Vertex> vertices;
	public List<POI> landmarks;
	public List<POI> safeties;
	public NaviParam naviParam;

//	public final double minDist;
//	public final double advDist;
//	public final double autoDist;
//	public final double safetyDist;
//	public final double landDist;
//	public final double minDir;
//	public final double distPerCycle;
//	public final double distPerStep;

	private String getDirMsg(double curDir, double targetDir) {
		double dir = CoordSystem.dirNorm( targetDir - curDir );
		if( 15.0 <= dir && dir <=  45.0) // 11 o'clock
			return "11 o\'clock";
		if( 45.0 <= dir && dir <=  75.0) // 10 o'clock
			return "10 o\'clock";
		if( 75.0 <= dir && dir <= 105.0) //  9 o'clock
			return "left";
		if(105.0 <= dir && dir <= 135.0) //  8 o'clock
			return "8 o\'clock";
		if(135.0 <= dir && dir <= 165.0) //  7 o'clock
			return "7 o\'clock";

		if(165.0 <= dir && dir <= 195.0) //  6 o'clock
			return "back";

		if(195.0 <= dir && dir <= 225.0) //  5 o'clock
			return "5 o\'clock";
		if(225.0 <= dir && dir <= 255.0) //  4 o'clock
			return "4 o\'clock";
		if(255.0 <= dir && dir <= 285.0) //  3 o'clock
			return "right";
		if(285.0 <= dir && dir <= 315.0) //  2 o'clock
			return "2 o\'clock";
		if(315.0 <= dir && dir <= 345.0) //  1 o'clock
			return "1 o\'clock";

		return "ahead";
	}
	private String getTurnDirInst(double curDir, double targetDir) {
		double dir = CoordSystem.dirNorm( targetDir - curDir );
		if( 15.0 <= dir && dir <=165.0) // 11 o'clock ~ 7 o'clock
			return "Turn " + getDirMsg(curDir, targetDir) + ".";
		if(165.0 <= dir && dir <= 195.0) //  6 o'clock
			return "Stop and u-turn.";
		if(195.0 <= dir && dir <= 345.0) //  5 o'clock ~ 1 o'clock
			return "Turn " + getDirMsg(curDir, targetDir) + ".";
		return "Go ahead.";  			 // 12 o'clock
	}
	private String getPosDirInst(double curDir, double targetDir) {
		double dir = CoordSystem.dirNorm( targetDir - curDir );
		if( 15.0 <= dir && dir <= 165.0) // 11 o'clock ~ 7 o'clock
			return "on your " + getDirMsg(curDir, targetDir) + ".";
		if(165.0 <= dir && dir <= 195.0) //  6 o'clock
			return "on your back.";
		if(195.0 <= dir && dir <= 345.0) // 5 o'clock ~ 1 o'clock
			return "on your " + getDirMsg(curDir, targetDir) + ".";
		return "on your way.";  			 // 12 o'clock
	}

	// Constructor
	public VIMRoutingPath(NaviParam naviParam) {
		this.naviParam = naviParam;
		this.vertices = new ArrayList<Vertex>();
		this.landmarks = new ArrayList<POI>();
		this.safeties = new ArrayList<POI>();
	}
	public VIMRoutingPath(Graph indoorNet, List<String> vertexIDs, NaviParam naviParam) {
		this(naviParam);

		// constructing a vertex list and a edge segment list
		List<VIMSegment> segList = new ArrayList<VIMSegment>();
		Vertex prevVertex = null;
		for(int i=0; i<vertexIDs.size(); ++i) {
			String vertexId = vertexIDs.get(i);
			Vertex vertex = indoorNet.getVertex(vertexId);
			this.vertices.add(vertex);
			if(prevVertex!=null) {
				VIMPoint src = prevVertex.point;
				VIMPoint dst = vertex.point;
				VIMSegment seg = new VIMSegment(src, dst);
				segList.add(seg);
			}
			prevVertex = vertex;
		}

		// constructing a landmark list associated with the edge segment
		List<POI> landmarks = indoorNet.getLandmarks();
		for(int i=0; i<landmarks.size(); ++i) {
			VIMPoint poiPos = landmarks.get(i).poiPos;
			VIMPolygon poiPolygon = landmarks.get(i).boundary;
			if(poiPolygon!=null) {
				int j;
				for(j=0; j<segList.size() &&
						!(poiPolygon.getFloor()==segList.get(j).getFloor()
								&& poiPolygon.intersect(segList.get(j))); ++j)
					;
				if(j<segList.size()) {
					this.landmarks.add(landmarks.get(i));
				}
			} else {
				this.landmarks.add(landmarks.get(i));
			}
		}

		// constructing a safety list associated with the edge segment
		List<POI> safeties = indoorNet.getSafeties();
		for(int i=0; i<safeties.size(); ++i) {
			VIMPoint poiPos = safeties.get(i).poiPos;
			VIMPolygon poiPolygon = safeties.get(i).boundary;
			if(poiPolygon!=null) {
				int j;
				for(j=0; j<segList.size() &&
						!(poiPolygon.getFloor()==segList.get(j).getFloor()
								&& poiPolygon.intersect(segList.get(j))); ++j)
					;
				if(j<segList.size()) {
					this.safeties.add(safeties.get(i));
				}
			} else {
				this.safeties.add(safeties.get(i));
			}
		}
	}

	public List<VIMInst> getCLInst(VIMLocation vimLoc) {
		VIMPoint curPos = new VIMPoint( (VIMPosition)vimLoc );
		double curDir = vimLoc.dir;
		long curTime = vimLoc.time;

		List<VIMInst> insts = new ArrayList<VIMInst>();

		for(int i=0; i<vertices.size(); ++i) {
			Vertex prvVertex = i-1<0 ? null: vertices.get(i-1);
			Vertex curVertex = vertices.get(i);
			Vertex nxtVertex = i+1>=vertices.size() ? null: vertices.get(i+1);
			VIMPoint prvVertexPt = prvVertex==null ? null: prvVertex.point;
			VIMPoint curVertexPt = curVertex.point;
			VIMPoint nxtVertexPt = nxtVertex==null ? null: nxtVertex.point;

			VIMSegment srcSeg = prvVertexPt==null ?
					new VIMSegment(curVertexPt, nxtVertexPt):
					new VIMSegment(prvVertexPt, curVertexPt);
			VIMSegment dstSeg = nxtVertexPt==null ?
					null:
					new VIMSegment(curVertexPt, nxtVertexPt);

			double dist = curPos.getDist2(curVertexPt);

			if( curPos.getFloor()== curVertexPt.getFloor() &&  srcSeg.getDist2(curPos)<=naviParam.minDist ) {
				String message = "";
				if( curVertexPt.isEqual3D(new VIMPoint((VIMPosition)vimLoc)) ) {
					message = "At " + curVertex.name + ", ";
				}
				String precedingName = prvVertex!=null ? prvVertex.name : ""; // must modify
				if(precedingName.equals(""))
					precedingName = "the startint point";
				String followingName = curVertex!=null ? curVertex.name : ""; // must modify
				if(followingName.equals(""))
					followingName = "the arrival point";

				double segDir = srcSeg.getDirection();
				if( CoordSystem.dirDiff( curDir, segDir ) >= 90.0 ) {
                    // 단순하  vertex로 판단하면 방향성 판단이 힘들다.
                    // edge를 사용하여 판단하거나, vertex를 사용하더라도 방향을 고려하여
                    // source state와 destination state를 구분해야 한다.
				    String temp = precedingName;
                    precedingName = followingName;
                    followingName = temp;
                }
//				message += "On the way from " + precedingName + " to " + followingName + ".";  // 오류인듯...
				message += "On the way from " + followingName + " to " + precedingName + ".";

				String info = String.format("step=%.1f",dist/naviParam.distPerStep); // for testing
				insts.add(new VIMInst(0, "CURRENT", curVertex.id, dist,
						(nxtVertex!=null ? nxtVertex.id: "<None>"), message, info));
				return insts;
			}
		}
		return insts;
	}
	public List<VIMInst> getSafetyInst(VIMLocation vimLoc) {
		VIMPoint curPos = new VIMPoint( (VIMPosition)vimLoc );
		double curDir = vimLoc.dir;
		long curTime = vimLoc.time;

		List<VIMInst> insts = new ArrayList<VIMInst>();

		// Safety Instruction
		for(int i=0; i<safeties.size(); ++i) {
			POI safety = safeties.get(i);
			String message = "Warning, " + safety.name;
			double dist = curPos.getDist3( safety.poiPos );
			double targetDir = curPos.getDirection(safety.poiPos);  // 방향 버그 고치기 위해 수정(확실하지 않음)
//			double targetDir = safety.poiPos.getDirection(curPos);
			VIMPolygon boundary = safety.boundary;
			if( true
//				(boundary == null || boundary.isIntersect(curPos))	// same cell space
					&& vimLoc.z == safety.poiPos.z						// same floor
					&& dist <= naviParam.safetyDist								// close distance
			) {
				message += " " + getPosDirInst(curDir, targetDir);
				String info = String.format("step=%.1f, dir=%.1f", dist/naviParam.distPerStep, targetDir); // for testing
				insts.add(new VIMInst(0, "SATETY", safety.id, dist, "<None>", message, info));
			}
		}
		return insts;
	}
	public List<VIMInst> getLandmarkInst(VIMLocation vimLoc) {
		VIMPoint curPos = new VIMPoint( (VIMPosition)vimLoc );
		double curDir = vimLoc.dir;
		long curTime = vimLoc.time;

		List<VIMInst> insts = new ArrayList<VIMInst>();

		// Landmark Instruction
		for(int i=0; i<landmarks.size(); ++i) {
			POI landmark = landmarks.get(i);
			String message = landmark.name;
			double dist = curPos.getDist2( landmark.poiPos );
			double targetDir = curPos.getDirection(landmark.poiPos);  // 방향 버그 고치기 위해 수정(확실하지 않음)
//			double targetDir = landmark.poiPos.getDirection(curPos);
			VIMPolygon boundary = landmark.boundary;
			if( true
//				(boundary == null || landmark.boundary.isIntersect(curPos))	// same cell space
					&& vimLoc.z == landmark.poiPos.z							// same floor
					&& dist <= naviParam.safetyDist										// close distance
			) {
				message += " " + getPosDirInst(curDir, targetDir);
				String info = String.format("step=%.1f, dir=%.1f", dist/naviParam.distPerStep, targetDir); // for testing
				insts.add(new VIMInst(0, "LANDMK", landmark.id, dist, "<None>", message, info));
			}
		}
		return insts;
	}
	public List<VIMInst> getNaviInst(VIMLocation vimLoc) {
		VIMPoint curPos = new VIMPoint( (VIMPosition)vimLoc );
		double curDir = vimLoc.dir;
		long curTime = vimLoc.time;

		List<VIMInst> insts = new ArrayList<VIMInst>();

//		// Safety Instruction
//		insts.addAll( getSafetyInst(vimLoc) );

		for(int i=0; i<vertices.size(); ++i) {
			Vertex prvVertex = i-1<0 ? null: vertices.get(i-1);
			Vertex curVertex = vertices.get(i);
			Vertex nxtVertex = i+1>=vertices.size() ? null: vertices.get(i+1);

			VIMPoint prvVertexPt = prvVertex==null ? null: prvVertex.point;
			VIMPoint curVertexPt = curVertex.point;
			VIMPoint nxtVertexPt = nxtVertex==null ? null: nxtVertex.point;

			VIMSegment srcSeg = prvVertexPt==null ?
					new VIMSegment(curVertexPt, nxtVertexPt):
					new VIMSegment(prvVertexPt, curVertexPt);
			VIMSegment dstSeg = nxtVertexPt==null ?
					null:
					new VIMSegment(curVertexPt, nxtVertexPt);

			double srcDist = prvVertexPt==null? Double.NaN: curPos.getDist3(prvVertexPt);
			double dist = curPos.getDist3(curVertexPt);

			double srcDir = srcSeg==null ? curDir+naviParam.minDir*2.0: srcSeg.getDirection();
			double dstDir = dstSeg==null ? curDir+naviParam.minDir*2.0: dstSeg.getDirection();

			double srcDiff = CoordSystem.dirDiff(curDir, srcDir);
			double dstDiff = CoordSystem.dirDiff(curDir, dstDir);

			// Turing Instruction
			if(i>0 && srcSeg!=null && srcDiff <= naviParam.minDir
					&& dist <= naviParam.minDist) {
				String targetPlace = "."; // must modify
				String message = null;
				if(nxtVertexPt != null)
					message = getTurnDirInst(vimLoc.dir, dstSeg.getDirection());
				else
					message = "Stop, you have arrived.";

				String info = String.format("step=%.1f", dist/naviParam.distPerStep); // for testing
				insts.add(new VIMInst(0, "TURN", curVertex.id, dist,
						(nxtVertex!=null ? nxtVertex.id: "<None>"), message, info));
			}
			// Starting Instruction
			if(dstSeg!=null && dstDiff <= naviParam.minDir
					&& dist <= naviParam.minDist) {
				String targetPlace = "."; // must modify
				double nextDist = curPos.getDist3(nxtVertexPt);
				int numStep = (int)Math.round(nextDist / naviParam.distPerStep);
				String strStep = String.format("%d", numStep);
				String message = "Go ";
				message += strStep + " step"+ (numStep>1 ? "s": "");
				message += " forward" + targetPlace;
				String info = String.format("step=%.1f, distToNextState=%.1f",
						nextDist/naviParam.distPerStep, nextDist); // for testing
				insts.add(new VIMInst(0, "START", curVertex.id, dist,
						(nxtVertex!=null ? nxtVertex.id: "<None>"), message, info));
			}
//			// Auto Current Location Instruction
//			if(i>0 && srcSeg!=null && srcDiff<= naviParam.minDir
//				&& naviParam.autoDist<=srcDist
//				&& naviParam.advDist+naviParam.autoDist<=dist) {
//
//	            String precedingName = prvVertex!=null ? prvVertex.name : ""; // must modify
//	            if(precedingName.equals(""))
//	                precedingName = "the startint point";
//	            String followingName = curVertex!=null ? curVertex.name : ""; // must modify
//	            if(followingName.equals(""))
//	                followingName = "the arrival point";
//				String message = "On the way from " + precedingName + " to " + followingName + ".";
//				String info = String.format("step=%.1f",dist/naviParam.distPerStep); // for testing
//				int orderNum = (int)Math.floor(srcDist/naviParam.autoDist);
//				insts.add(new VIMInst(0, "AUTO", curVertex.id + "$" + orderNum, dist,
//						(nxtVertex!=null ? nxtVertex.id: "<None>"), message, info));
//			}
			// Ending Instruction
			if(i>0 && srcSeg!=null && srcDiff<= naviParam.minDir
					&& dist <= naviParam.advDist) {
				String targetPlace = "."; // must modify
				int numStep = (int)Math.round(dist / naviParam.distPerStep);
				String strStep = String.format("%d", numStep);
				String message = "Stop at ";
				message += strStep + " step"+ (numStep>1 ? "s": "");
				message += " ahead" + targetPlace;
				String info = String.format("step=%.1f",dist/naviParam.distPerStep); // for testing
				insts.add(new VIMInst(0, "END", curVertex.id, dist,
						(nxtVertex!=null ? nxtVertex.id: "<None>"), message, info));
			}
		}
//		// Landmark Instruction
//		insts.addAll( getLandmarkInst(vimLoc) );
		return insts;
	}

	// new navi-inst generator:
	//     inst-generator works well for jumping position problems
	enum NaviState {
		NONE,
		TURNING,
		STARTING,
		ENDING,
		ARRIVAL
	}
	private NaviState prvState = NaviState.NONE;
	private long prvStateTime = 0L;
	private long prvElevationTime = 0L;
	private int prvWpIndex = -1;

	public void initNavi() {
		prvState = NaviState.NONE;
		prvStateTime = 0L;
		prvWpIndex = -1;
	}

	private boolean haveArrived(VIMPoint curPt, VIMPoint destPt) {
		double targetDist = curPt.getDist2(destPt);
		return targetDist <= naviParam.minDist;
	}
	private int getOnptIndex(VIMPoint curPt) {
		if(vertices.size()<=0)  // not be on any point.
			return -1;
		for(int i=0; i<vertices.size(); ++i) {
			VIMPoint pt = vertices.get(i).point;

			double distToPt = pt.getDist2(curPt);
			if(curPt.getFloor() == pt.getFloor() &&
					distToPt <= naviParam.minDist) {
				return i;
			}
		}
		return -1;
	}
	private int getWayptIndex(VIMPoint curPt) {
		if(vertices.size()<=0)  // should recalculate a routing path;
			return -1;

		VIMPoint firstPt = vertices.get(0).point;
		double minDistToSeg = Double.MAX_VALUE;
		int wayptIndex = -1;    // should recalculate a routing path;

		if(curPt.getFloor() == firstPt.getFloor()) {
			minDistToSeg = curPt.getDist2( firstPt );
			wayptIndex = 0;
		}

		for(int i=1; i<vertices.size(); ++i) {
			VIMPoint srcPt = vertices.get(i-1).point;
			VIMPoint dstPt = vertices.get(i).point;
			VIMSegment seg = new VIMSegment(srcPt, dstPt);

			double distToSeg = seg.getDist2(curPt);
			if(curPt.getFloor() == seg.getFloor() &&
					distToSeg <= minDistToSeg) {
				minDistToSeg = distToSeg;
				wayptIndex = i;
			}
		}

		if(wayptIndex>=0 && haveArrived(curPt, vertices.get(wayptIndex).point)) {
			++wayptIndex;
		}
		// Here, wayptIndex == vertices.size() means you have reached your destination.
		return wayptIndex;
	}

	private VIMShortInst getArrivalInst(String wayptID) {
		String msg = "Stop, you have arrived.";
		return new VIMShortInst(VIMShortInst.InstType.ARRIVAL, wayptID, msg);
	}
	private VIMShortInst getTurningInst(String wayptID, double curDir, double targetDir) {
		String msg = getTurnDirInst(curDir, targetDir);
		return new VIMShortInst(VIMShortInst.InstType.TURNING, wayptID, msg);
	}
	private VIMShortInst getStartingInst(String wayptID, double distToWp) {
		int numStep = (int)Math.round(distToWp / naviParam.distPerStep);
		String msg = "Go " + numStep + (numStep>1 ? " steps": " step") + " forward.";
		return new VIMShortInst(VIMShortInst.InstType.STARTING, wayptID, msg);
	}
	private VIMShortInst getEndingInst(String wayptID, double distToWp) {
		int numStep = (int)Math.round(distToWp / naviParam.distPerStep);
		String msg = "Stop at " + numStep + (numStep>1 ? " steps": " step") + " ahead.";
		return new VIMShortInst(VIMShortInst.InstType.ENDING, wayptID, msg);
	}
	private VIMShortInst getElevatorInst(String wayptID, double distToWp, String targetFloor) {
		int numStep = (int)Math.round(distToWp / naviParam.distPerStep);
		String msg = "Elevator at " + numStep + (numStep>1 ? " steps": " step") + " ahead. "
				+ "Board the elevator. Press the button to " + targetFloor + " floor.";

		return new VIMShortInst(VIMShortInst.InstType.ENDING, wayptID, msg);
	}

	private boolean isChangedWP(int wpIndex) {
		return wpIndex != prvWpIndex;
	}
	private boolean isTowardsWP(VIMPoint curPt, double curDir, VIMPoint destPt) {
		double targetDir = curPt.getDirection(destPt);
		double dirDiff = CoordSystem.dirDiff(curDir, targetDir);
		return dirDiff <= naviParam.minDir;
	}
	private boolean isNearWP(VIMPoint curPt, double curDir, VIMPoint destPt) {
		double targetDir = curPt.getDirection(destPt);
		double dirDiff = CoordSystem.dirDiff(curDir, targetDir);
		double targetDist = curPt.getDist2(destPt);
		return dirDiff <= naviParam.minDir && targetDist<=naviParam.advDist;
	}
	private boolean needTurningInst(VIMLocation curLoc, int wpIndex) {
		// 1. WP가 변경되면 turning-instruction을 발생시켜 진행방향을 잡도록 해야 한다.
		//    - WP change: 측위 오류 / 빠른 이동 (position jumping)
		// 2. 진행방향을 잡는 과정에서 측위 오류 또는 급과 회전으로 인해 direction jumping이 발생할 수 있다.
		//    이 경우 다시  turning-instruction을 발생시켜 진행방향을 수정하도록 해야 한다.
		//    - excessive rotation: 측위 오류 / 빠른 회전 --> direction jumping
		/// 참고: 2번이 구현되지 못함. 2번 조건을 논리적으로 표현해야 함.

		VIMPoint curPt = new VIMPoint((VIMPosition)curLoc);
		double curDir = curLoc.dir;
		VIMPoint wayPt = vertices.get(wpIndex).point;

		boolean isChanged = isChangedWP(wpIndex);
		boolean towardsWP = isTowardsWP(curPt, curDir, wayPt);
		boolean nearWP = isNearWP(curPt, curDir, wayPt);
		boolean prvTuring = prvState==NaviState.TURNING;
		boolean prvStarting = prvState==NaviState.STARTING;
		boolean prvEnding = prvState==NaviState.ENDING;
		boolean longTime = System.currentTimeMillis() - prvStateTime >= 10000;

		return
				false
						|| isChanged						            // turning
						|| !towardsWP &&  prvTuring &&  longTime		// repeat
				// WP방향이 아니고, 이전에 turning instruction이 발생했지만 10초 이상 방향 변동이 없을 때
				// WP방향이 아니고, 이전에 turning instruction이 발생했지만 반대 방향으로 회전했을 때 (미묘한 회전은 무시해야)
				// WP방향이 아니고, 이전에 turning instruction이 발생했지만 지나치게 회전했을 때
				;
	}
	private boolean needStartingInst(VIMLocation curLoc, int wpIndex) {
		// 0. turning이 필요한 상황이라면 turning instruction이 starting instruction보다 우선 발생해야 한다.
		//    즉, turning instruction 다음에 starting instruction이 발생해야 한다.
		// 1. 이전에 starting instruction이 발생하지 않았고, 진행방향이 제대로 되었다면
		//    starting instruction이 발생한다.
		// 2. 이전에 starting instruction이 발생했더라도 시간이 10초 이상 지났을 경우
		//    starting instruction이 발생한다. 단, ending instruction이 발생할 상황이 아닐 경우에만

		VIMPoint curPt = new VIMPoint((VIMPosition)curLoc);
		double curDir = curLoc.dir;
		VIMPoint wayPt = vertices.get(wpIndex).point;

		boolean towardsWP = isTowardsWP(curPt, curDir, wayPt);
		boolean nearWP = isNearWP(curPt, curDir, wayPt);
		boolean prvStarting = prvState==NaviState.STARTING;
		boolean prvEnding = prvState==NaviState.ENDING;
		boolean longTime = System.currentTimeMillis() - prvStateTime >= 10000;

		return
				false
						|| towardsWP && !nearWP && !prvStarting && !prvEnding		// starting
						|| towardsWP && !nearWP &&  prvStarting &&  longTime		// repeat
						|| towardsWP &&  nearWP && !prvStarting && !prvEnding		// replace ending
				;
	}
	private boolean needEndingInst(VIMLocation curLoc, int wpIndex) {
		// 1. WP방향, 이전에 starting instruction 발생, WP와 가까운 거리
		//    참고) 직전에 starting instruction이 발생하지 않았다면 starting instruction 발생을 유도해야 한다.
		VIMPoint curPt = new VIMPoint((VIMPosition)curLoc);
		double curDir = curLoc.dir;
		VIMPoint wayPt = vertices.get(wpIndex).point;

		boolean towardsWP = isTowardsWP(curPt, curDir, wayPt);
		boolean nearWP = isNearWP(curPt, curDir, wayPt);
		boolean prvStarting = prvState==NaviState.STARTING;
		boolean prvEnding = prvState==NaviState.ENDING;
		boolean longTime = System.currentTimeMillis() - prvStateTime >= 10000;

		return
				false
						|| towardsWP && nearWP && prvStarting && !prvEnding			// ending
				;
	}
    public List<VIMShortInst> getNaviInst2(VIMLocation vimLoc) {
        List<VIMShortInst> insts = new ArrayList<VIMShortInst>();

        VIMPoint curPt = new VIMPoint((VIMPosition)vimLoc);
        double curDir = vimLoc.dir;

        int wpIndex = getWayptIndex(curPt);

        if(wpIndex<0)
            return null;  // should recalculate a routing path;

        if(prvState == NaviState.ARRIVAL) {
            return null; // navigation finished
        }

        if(wpIndex>=vertices.size()) { // arrival instruction
            prvState = NaviState.ARRIVAL;
            prvStateTime = System.currentTimeMillis();
            prvWpIndex = wpIndex;

            String wayptID = "DEST";
            insts.add( getArrivalInst(wayptID) );
            return insts;
        }

        // Here, 0 <= wpIndex < vertices.size()
        if(wpIndex!=prvWpIndex) {
            prvState = NaviState.NONE;
        }
        VIMPoint wayPt = vertices.get(wpIndex).point;

        if( needTurningInst(vimLoc, wpIndex) ) {
            prvState = NaviState.TURNING;
            prvStateTime = System.currentTimeMillis();
            prvWpIndex = wpIndex;

            Vertex wpVertex = vertices.get(wpIndex);
            double targetDir = curPt.getDirection(wayPt);

			int onIndex = getOnptIndex(curPt);
			Vertex onVertex = onIndex>=0 ? vertices.get(onIndex) : null;
            if(onVertex!=null && onVertex.type.equals("elevator")) {
            	String msg = "turn to the door.";
				insts.add(
					new VIMShortInst(VIMShortInst.InstType.TURNING, wpVertex.id, msg)
				);
			} else {
				insts.add(getTurningInst(wpVertex.id, curDir, targetDir));
			}
            return insts;
        }

        if( needStartingInst(vimLoc, wpIndex) ) {
            prvState = NaviState.STARTING;
            prvStateTime = System.currentTimeMillis();
            prvWpIndex = wpIndex;

            Vertex wpVertex = vertices.get(wpIndex);
            double distToWp = curPt.getDist2(wayPt);

			int onIndex = getOnptIndex(curPt);
			Vertex onVertex = onIndex>=0 ? vertices.get(onIndex) : null;
			if(onVertex!=null && onVertex.type.equals("elevator")) {
				String msg = "wait until arriving the target floor.";
				insts.add(
					new VIMShortInst(VIMShortInst.InstType.STARTING, wpVertex.id, msg)
				);
			} else {
				insts.add(getStartingInst(wpVertex.id, distToWp));
			}
            return insts;
        }

        if( needEndingInst(vimLoc, wpIndex) ) {
            prvState = NaviState.ENDING;
            prvStateTime = System.currentTimeMillis();
            prvWpIndex = wpIndex;

            Vertex wpVertex = vertices.get(wpIndex);
            double distToWp = curPt.getDist2(wayPt);
            if(wpVertex.type.equals("elevator")) {
            	if((System.currentTimeMillis() - prvElevationTime)>=30000) {
					String targetFloor = curPt.getFloor() == 3.0 ? "4th" : "3th";
					insts.add(getElevatorInst(wpVertex.id, distToWp, targetFloor));
					prvElevationTime = System.currentTimeMillis();
				}
			} else {
				insts.add(getEndingInst(wpVertex.id, distToWp));
			}
            return insts;
        }

        return insts;
    }

    public List<VIMShortInst> getCLInst2(VIMLocation vimLoc) {
        VIMPoint curPos = new VIMPoint( (VIMPosition)vimLoc );
        double curDir = vimLoc.dir;
        long curTime = vimLoc.time;

        List<VIMShortInst> insts = new ArrayList<VIMShortInst>();

        for(int i=0; i<vertices.size(); ++i) {
            Vertex prvVertex = i-1<0 ? null: vertices.get(i-1);
            Vertex curVertex = vertices.get(i);
            Vertex nxtVertex = i+1>=vertices.size() ? null: vertices.get(i+1);
            VIMPoint prvVertexPt = prvVertex==null ? null: prvVertex.point;
            VIMPoint curVertexPt = curVertex.point;
            VIMPoint nxtVertexPt = nxtVertex==null ? null: nxtVertex.point;

            VIMSegment srcSeg = prvVertexPt==null ?
                    new VIMSegment(curVertexPt, nxtVertexPt):
                    new VIMSegment(prvVertexPt, curVertexPt);
            VIMSegment dstSeg = nxtVertexPt==null ?
                    null:
                    new VIMSegment(curVertexPt, nxtVertexPt);

            double dist = curPos.getDist3(curVertexPt);

            if( srcSeg.getDist2(curPos)<=naviParam.minDist ) {
//                String message = "";
//                if( curVertexPt.isEqual3D(new VIMPoint((VIMPosition)vimLoc)) ) {
//                    message = "At " + curVertex.name + ", ";
//                }
//                String precedingName = prvVertex!=null ? prvVertex.name : ""; // must modify
//                if(precedingName.equals(""))
//                    precedingName = "the startint point";
//                String followingName = curVertex!=null ? curVertex.name : ""; // must modify
//                if(followingName.equals(""))
//                    followingName = "the arrival point";
//                message += "On the way from " + precedingName + " to " + followingName + ".";
//
//                String info = String.format("step=%.1f",dist/naviParam.distPerStep); // for testing
//                insts.add(new VIMInst(0, "CURRENT", curVertex.id, dist,
//                        (nxtVertex!=null ? nxtVertex.id: "<None>"), message, info));
                return insts;
            }
        }
        return insts;
    }

    private String getSafetyInst(String safetyName, double dist, double curDir, double targetDir) {
        // safety instruction example: "Warning, X is 5 steps away on your 10 o'clock."
        int numStep = (int)Math.round(dist / naviParam.distPerStep);
        String strStep = String.format(" %d", numStep) + (numStep>1 ? " steps" : " step");
	    String msg = "Warning, "
                + safetyName + " is"
                + strStep + " away "
                + getPosDirInst(curDir, targetDir);
	    return msg;
    }
    private VIMShortInst getSafetyInst(String safeyId, String safetyName, double dist, double curDir, double targetDir) {
        String msg = getSafetyInst(safetyName, dist, curDir, targetDir);
        return new VIMShortInst(VIMShortInst.InstType.SAFETY, safeyId, msg);
    }
    private boolean inSafetyRange(VIMLocation curLoc, POI safety) {
        VIMPoint curPos = new VIMPoint( (VIMPosition)curLoc );
        double dist = curPos.getDist2( safety.poiPos );
        VIMPolygon boundary = safety.boundary;
        return
            curPos.getFloor() == safety.poiPos.getFloor()        // same floor
//          && (boundary == null || boundary.intersect(curPos))  // same cell space
            && dist <= naviParam.safetyDist                      // in the safety range
            ;
    }
    private Set<POI> prevSafeties = new HashSet<POI>();
    public List<VIMShortInst> getSafetyInst2(VIMLocation vimLoc) {
        // safety의 범위에 진입하는 모든 순간에 instruction이 발생된다.
        // A safety instruction is generated at every moment of entering the safety range.
        VIMPoint curPos = new VIMPoint( (VIMPosition)vimLoc );
        List<VIMShortInst> insts = new ArrayList<VIMShortInst>();

        // Safety Instruction
        for(int i=0; i<safeties.size(); ++i) {
            POI safety = safeties.get(i);
            String message = "Warning, " + safety.name;
            double dist = curPos.getDist3( safety.poiPos );
            VIMPolygon boundary = safety.boundary;
            if(inSafetyRange(vimLoc, safety)) {
                if(!prevSafeties.contains(safety)) {
                    prevSafeties.add(safety);
                    double curDir = curPos.getDist2(safety.poiPos);
                    double targetDir = curPos.getDirection(safety.poiPos);
                    insts.add( getSafetyInst(safety.id, safety.name, dist, curDir, targetDir) );
                }
            } else {
                if(prevSafeties.contains(safety)) {
                    prevSafeties.remove(safety);
                }
            }
        }
        return insts;
    }

	private boolean inLandmarkRange(VIMLocation curLoc, POI land) {
		VIMPoint curPos = new VIMPoint( (VIMPosition)curLoc );
		double dist = curPos.getDist2( land.poiPos );
		VIMPolygon boundary = land.boundary;
		return
			curPos.getFloor() == land.poiPos.getFloor()        // same floor
//          && (boundary == null || boundary.intersect(curPos))  // same cell space
			&& dist <= naviParam.safetyDist                      // in the safety range
			;
	}
	private Set<POI> prevLandmarks = new HashSet<POI>();
    public List<VIMShortInst> getLandmarkInst2(VIMLocation vimLoc) {
        // landmark의 범위에 진입하는 첫 번째 순간에만 instruction이 발생된다.
        // A landmark instruction is generated only at the first moment of entering the landmark range.
        VIMPoint curPos = new VIMPoint( (VIMPosition)vimLoc );
        List<VIMShortInst> insts = new ArrayList<VIMShortInst>();

        // Landmark Instruction
        for(int i=0; i<landmarks.size(); ++i) {
        	POI landmark = landmarks.get(i);
			String message = landmark.name;
			double dist = curPos.getDist3( landmark.poiPos );
			VIMPolygon boundary = landmark.boundary;
			if(inLandmarkRange(vimLoc, landmark)) {
				if(!prevLandmarks.contains(landmark)) {
					prevLandmarks.add(landmark);
					double curDir = curPos.getDist2(landmark.poiPos);
					double targetDir = curPos.getDirection(landmark.poiPos);
					insts.add( getSafetyInst(landmark.id, landmark.name, dist, curDir, targetDir) );
				}
			} else {
//				if(prevLandmarks.contains(landmark)) {
//					prevLandmarks.remove(landmark);
//				}
			}
        }
        return insts;
    }

//	public List<VIMInst> getRestartInst(VIMLocation vimLoc) {
//		VIMPoint curPos = new VIMPoint( (VIMPosition)vimLoc );
//		double curDir = vimLoc.dir;
//		long curTime = vimLoc.time;
//
//		List<VIMInst> insts = new ArrayList<VIMInst>();
//
//		List<Vertex> tempVertices = new ArrayList<Vertex>(vertices);
//
//		double closeDist;
//		int closeIndex;
//
//		closeDist = Double.MAX_VALUE;
//		closeIndex = -1;
//
//		if( curPos.getFloor() == tempVertices.get(0).point.getFloor() ) {
//			closeDist = curPos.getDist2( tempVertices.get(0).point );
//			closeIndex = 0;
//		}
//		for(int i=1; i<tempVertices.size(); ++i) {
//			Vertex prvVertex = tempVertices.get(i-1);
//			Vertex curVertex = tempVertices.get(i);
//
//			VIMPoint prvVertexPt = prvVertex.point;
//			VIMPoint curVertexPt = curVertex.point;
//
//			VIMSegment seg = new VIMSegment(prvVertexPt, curVertexPt);
//
//			if( curPos.getFloor() == seg.getFloor() ) {
//				 double dist = seg.getDist2(curPos);
//				 if( closeDist > dist ) {
//					 closeDist = dist;
//					 closeIndex = i;
//				 }
//			}
//		}
//		if(closeIndex >= 0) {
//			if( curPos.getDist2( tempVertices.get(closeIndex).point ) > naviParam.minDist ) {
//				Vertex newVertex = new Vertex("Your Position", "Your Position", "Your Position",
//					null, curPos);
//				tempVertices.add(closeIndex, newVertex);
//			}
//
//			Vertex prvVertex = closeIndex-1<0 ? null: tempVertices.get(closeIndex-1);
//			Vertex curVertex = tempVertices.get(closeIndex);
//			Vertex nxtVertex = closeIndex+1>=tempVertices.size() ? null: tempVertices.get(closeIndex+1);
//
//			VIMPoint prvVertexPt = prvVertex==null ? null: prvVertex.point;
//			VIMPoint curVertexPt = curVertex.point;
//			VIMPoint nxtVertexPt = nxtVertex==null ? null: nxtVertex.point;
//
//			VIMSegment srcSeg = prvVertexPt==null ?
//					new VIMSegment(curVertexPt, nxtVertexPt):
//					new VIMSegment(prvVertexPt, curVertexPt);
//			VIMSegment dstSeg = nxtVertexPt==null ?
//					null:
//					new VIMSegment(curVertexPt, nxtVertexPt);
//
//			double srcDist = prvVertexPt==null? Double.NaN: curPos.getDist3(prvVertexPt);
//			double dist = curPos.getDist3(curVertexPt);
//
//			double srcDir = srcSeg==null ? curDir+naviParam.minDir*2.0: srcSeg.getDirection();
//			double dstDir = dstSeg==null ? curDir+naviParam.minDir*2.0: dstSeg.getDirection();
//
//			double srcDiff = CoordSystem.dirDiff(curDir, srcDir);
//			double dstDiff = CoordSystem.dirDiff(curDir, dstDir);
//
//			// Turing Instruction
//			if(dist <= naviParam.minDist) {
//				String targetPlace = "."; // must modify
//				String message = null;
//				if(nxtVertexPt != null) {
//					message = getTurnDirInst(vimLoc.dir, dstSeg.getDirection());
//				}
//				else {
//					message = "Stop, you have arrived.";
//				}
//				String info = String.format("step=%.1f", dist/naviParam.distPerStep); // for testing
//				insts.add(new VIMInst(0, "TURN", curVertex.id, dist,
//						(nxtVertex!=null ? nxtVertex.id: "<None>"), message, info));
//			}
//
//			// Starting Instruction
//			if(dstSeg!=null && dstDiff <= naviParam.minDir && dist <= naviParam.minDist) {
//				String targetPlace = "."; // must modify
//				double nextDist = curPos.getDist3(nxtVertexPt);
//				int numStep = (int)Math.round(nextDist / naviParam.distPerStep);
//				String strStep = String.format("%d", numStep);
//				String message = "Go ";
//				message += strStep + " step"+ (numStep>1 ? "s": "");
//				message += " forward" + targetPlace;
//				String info = String.format("step=%.1f, distToNextState=%.1f",
//						nextDist/naviParam.distPerStep, nextDist); // for testing
//				insts.add(new VIMInst(0, "START", curVertex.id, dist,
//						(nxtVertex!=null ? nxtVertex.id: "<None>"), message, info));
//			}
//		}
//		return insts;
//	}

	public String toString() {
		StringBuffer strBuff = new StringBuffer();
		Table table = new Table();

		strBuff.append("- States (" + vertices.size() + ")\n");
		table.appendNewRow();
		table.addNewField("stID");
		table.addNewField("name");
		table.addNewField("type");
		table.addNewField("coordinate");
		table.addNewField("distFromPrev");
		VIMPoint prevPt = null;
		for(int i=0; i<vertices.size(); ++i) {
			Vertex currVertex = vertices.get(i);
			table.appendNewRow();
			table.addNewField(currVertex.id);
			table.addNewField(currVertex.name);
			table.addNewField(currVertex.type);
			table.addNewField(Format.point(currVertex.point));
			double distFromPrev
					= prevPt == null ? 0.0 : currVertex.point.getDist3(prevPt);
			table.addNewField(Format.coord(distFromPrev));
			prevPt = currVertex.point;
		}
		strBuff.append(table.toString() + "\n");
		table.clear();

		strBuff.append("- Safeties (" + safeties.size() + ")\n");
		table.appendNewRow();
		table.addNewField("saID");
		table.addNewField("name");
		table.addNewField("type");
		table.addNewField("anno");
		table.addNewField("coordinate");
		table.addNewField("bounary");
		for(int i=0; i<safeties.size(); ++i) {
			POI safety = safeties.get(i);
			table.appendNewRow();
			table.addNewField(safety.id);
			table.addNewField(safety.name);
			table.addNewField(safety.type);
			table.addNewField(safety.anno);
			table.addNewField(Format.point(safety.poiPos));
			table.addNewField(safety.boundary==null ? "<unbounded>" : "<bounded>");
		}
		table.setMaxLength(1, 20);
		table.setMaxLength(3, 20);
		strBuff.append(table.toString() + "\n");
		table.clear();

		strBuff.append("- Landmarks (" + landmarks.size() + ")\n");
		table.appendNewRow();
		table.addNewField("lmID");
		table.addNewField("name");
		table.addNewField("type");
		table.addNewField("anno");
		table.addNewField("coordinate");
		table.addNewField("bounary");
		for(int i=0; i<landmarks.size(); ++i) {
			POI landmark = landmarks.get(i);
			table.appendNewRow();
			table.addNewField(landmark.id);
			table.addNewField(landmark.name);
			table.addNewField(landmark.type);
			table.addNewField(landmark.anno);
			table.addNewField(Format.point(landmark.poiPos));
			table.addNewField(landmark.boundary==null ? "<unbounded>" : "<bounded>");
		}
		table.setMaxLength(1, 20);
		table.setMaxLength(3, 20);
		strBuff.append(table.toString() + "\n");
		table.clear();
		return new String(strBuff);
	}
}
