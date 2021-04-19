package pnu.stemlab.vimnavi;

import java.io.*;
import java.util.*;
import org.json.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;


public class Graph {
	private List<Vertex> vertices;
	private List<Edge> edges;
	private List<POI> safeties;
	private List<POI> landmarks;
	
	private Map<String,Vertex> vertexMap;
	private Map<String,Edge> edgeMap;

	private void makeMap() {
		if (this.vertices!=null) {
			vertexMap = new HashMap<String,Vertex>();
			for (Vertex v: this.vertices) {
				if(v!=null)
					vertexMap.put(v.id, v);
			}
			if(this.edges!=null) {
				edgeMap = new HashMap<String,Edge>();
				for (Edge e: this.edges)
					if(e!=null)
						edgeMap.put(e.id, e);
			}
		}
	}

	public Vertex getNearestVertex(VIMPoint pt) {
		double minDist = Double.MAX_VALUE;
		Vertex minV = null;
		for(Vertex v: vertices) {
			double dist = v.point.getDist2(pt);
			if(pt.getFloor() == v.point.getFloor() && dist < minDist) {
				minDist = dist;
				minV = v;
			}
		}
		return minV;
	}

	// Getting Node From Document
	private static List<Node> getNodeByTag(Document doc, String tagName) {
		NodeList ndList= doc.getElementsByTagName(tagName);
		List<Node> list = new ArrayList<Node>();
		for(int i=0; i<ndList.getLength(); ++i) {
			Node nd = ndList.item(i);
			list.add(nd);
		}
		return list; 
	}
	private static Node getNodeByTag(Document doc, String tagName, String gmlID) {
		NodeList ndList= doc.getElementsByTagName(tagName);
		for(int i=0; i<ndList.getLength(); ++i) {
			Node nd = ndList.item(i);
			NamedNodeMap attribNodes = nd.getAttributes();
			if(attribNodes!=null) {
				Node attribNode = attribNodes.getNamedItem("gml:id");
				if(attribNode!=null && attribNode.getNodeValue().equals(gmlID))
					return nd;
			}
		}
		return null; 
	}
	private static Node getNodeById(Document doc, String gmlId) {
		List<Node> descendants = getDescendants(doc);
		for(Node node: descendants) {
			String attribValue = getAttribValue(node, "gml:id");
			if(attribValue!=null && attribValue.equals(gmlId))
				return node;
		}
		return null;
	}

	// Recursive Helper
	private static void getDescendants(Node nd, List<Node> descendant) {
		if(!nd.hasChildNodes())
			return;
		NodeList childNodes = nd.getChildNodes();
		for(int i=0; i<childNodes.getLength(); ++i) {
			Node childNode = childNodes.item(i);
			descendant.add(childNode);
			getDescendants(childNode, descendant);
		}
		return;
	}
	private static void getDescendants(Node nd, String tagName, List<Node> descendant) {
		if(!nd.hasChildNodes())
			return;
		NodeList childNodes = nd.getChildNodes();
		for(int i=0; i<childNodes.getLength(); ++i) {
			Node childNode = childNodes.item(i);
			if(childNode.getNodeName().equals(tagName))
				descendant.add(childNode);
			getDescendants(childNode, tagName, descendant);
		}
	}

	// Getting Nodes From Node
	private static List<Node> getDescendants(Node nd) {
		List<Node> descendants = new ArrayList<Node>();
		NodeList childNodes = nd.getChildNodes();
		for(int i=0; i<childNodes.getLength(); ++i) {
			Node childNode = childNodes.item(i);
			descendants.add(childNode);
			getDescendants(childNode, descendants);
		}
		return descendants;
	}
	private static List<Node> getDescendants(Node nd, String tagName) {
		List<Node> descendant = new ArrayList<Node>();
		NodeList childNodes = nd.getChildNodes();
		for(int i=0; i<childNodes.getLength(); ++i) {
			Node childNode = childNodes.item(i);
			if(childNode.getNodeName().equals(tagName))
				descendant.add(childNode);
			getDescendants(childNode, tagName, descendant);
		}
		return descendant;
	}
	private static Node getDescendants(Node nd, String tagName, String gmlID) {
		return null;
	}

	// Getting String From Node
	private static String getDescendantContent(Node nd, String tagName, int order) {
		List<Node> descendants = getDescendants(nd);
		int i=0;
		for(Node n: descendants) {
			if(n.getNodeName().equals(tagName)) {
				++i;
				if(i==order)
					return n.getTextContent();
			}
		}
		return null;
	}
	private static String getDescendantAttrib(Node nd, String tagName, String attribName, int order) {
		List<Node> descendants = getDescendants(nd);
		int i=0;
		for(Node n: descendants) {
			if(n.getNodeName().equals(tagName)) {
				++i;
				if(i==order)
					return getAttribValue(n, attribName);
			}
		}
		return null;
	}
	private static List<Node> getChildren(Node nd) {
		List<Node> children = new ArrayList<Node>();
		NodeList childNodes = nd.getChildNodes();
		for(int i=0; i<childNodes.getLength(); ++i) {
			Node childNode = childNodes.item(i);
			children.add(childNode);
		}
		return children;
	}
	private static String getChildContent(Node nd, String tagName, int order) {
		List<Node> children = getChildren(nd);
		int i=0;
		for(Node n: children) {
			if(n.getNodeName().equals(tagName)) {
				++i;
				if(i==order)
					return n.getTextContent();
			}
		}
		return null;
	}
	private static String getChildAttrib(Node nd, String tagName, String attribName, int order) {
		List<Node> children = getChildren(nd);
		int i=0;
		for(Node n: children) {
			if(n.getNodeName().equals(tagName)) {
				++i;
				if(i==order)
					return getAttribValue(n, attribName);
			}
		}
		return null;
	}
	private static String getContent(Node nd) {
		return nd.getTextContent();
	}
	private static String getAttribValue(Node nd, String attribName) {
		NamedNodeMap attribNodes = nd.getAttributes();
		if(attribNodes!=null) {
			Node attribNode = attribNodes.getNamedItem(attribName);
			if(attribNode!=null)
				return attribNode.getNodeValue();
		}
		return null;
	}
	private static String getIdContains(Document doc, String gmlId) {
		NodeList nodes = doc.getElementsByTagName("core:InterLayerConnection");
		String id=null, searchId=null;
		int i;
		for(i=0;i<nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			String topoExp = getChildContent(node, "core:typeOfTopoExpression", 1);
			if(topoExp.equals("CONTAINS")) {
				id = getChildAttrib(node, "core:interConnects", "xlink:href", 1);
				searchId = getChildAttrib(node, "core:interConnects", "xlink:href", 2);
				if(searchId.equals("#" + gmlId))
					return id.substring(1);
			}
			else if(topoExp.equals("CONTAINED")) {
				searchId = getChildAttrib(node, "core:interConnects", "xlink:href", 1);
				id = getChildAttrib(node, "core:interConnects", "xlink:href", 2);
				if(searchId.equals("#" + gmlId))
					return id.substring(1);
			}
		}
		return null;
	}
	private static String getDescendantAttribute(Node nd, int index, String tagName, String attribName) {
		List<Node> descendant = getDescendants(nd);
		int count=0;
		for(Node n: descendant) {
			if(n.getNodeName().equals(tagName)) {
				if(count==index)
					return getAttribValue(n, attribName);
				++ count;
			}
		}
		return null;
	}

	// Getting Geometry From Node
	private static VIMPoint getPointFromPosNode(Node posNode) {
		String posDim = getAttribValue(posNode,"srsDimension");
		if(posDim==null || posDim.equals("2")) {
			double x=Double.NaN, y=Double.NaN;
			Scanner sc = new Scanner(posNode.getTextContent());
			if(!sc.hasNextDouble()) { sc.close(); return null; }
			x = sc.nextDouble();
			if(!sc.hasNextDouble()) { sc.close(); return null; }
			y = sc.nextDouble();
			sc.close();
			return new VIMPoint(x,y);
		}
		if(posDim!=null && posDim.equals("3")) {
			double x=Double.NaN, y=Double.NaN, z=Double.NaN;
			Scanner sc = new Scanner(posNode.getTextContent());
			if(!sc.hasNextDouble()) { sc.close(); return null; }
			x = sc.nextDouble();
			if(!sc.hasNextDouble()) { sc.close(); return null; }
			y = sc.nextDouble();
			if(!sc.hasNextDouble()) { sc.close(); return null; }
			z = sc.nextDouble();
			sc.close();
			return new VIMPoint(x,y,z);
		}
		return null;
	}
	private static VIMPoint getPoint(Node stateNode) {
		List<Node> posList = getDescendants(stateNode, "gml:pos");
		if(posList.size()==0)
			return null;
		return getPointFromPosNode(posList.get(0));
	}
	private static VIMPolygon getPolygon(Node cellExteriorNode) {
		List<Node> posList = getDescendants(cellExteriorNode, "gml:pos");
		List<VIMPoint> pointList = new ArrayList<VIMPoint>();
		for(Node posNode: posList) {
			VIMPoint pt = getPointFromPosNode(posNode);
			pointList.add(pt);
		}
		VIMPoint poiPolygonPt[] = new VIMPoint[pointList.size()];
		for(int i=0; i<poiPolygonPt.length; ++i)
			poiPolygonPt[i] = pointList.get(i);
		VIMPolygon polygon = new VIMPolygon(poiPolygonPt);
		return polygon;
	}
	private static VIMPolygon getPolygon(Document doc, String gmlId) {
		String stateId = getIdContains(doc, gmlId);
		if(stateId==null)
			return null;
		Node stateNode = getNodeById(doc, stateId);
		if(stateNode==null)
			return null;
		String cellNodeRefId = getChildAttrib(stateNode, "core:duality", "xlink:href", 1);
		String cellNodeId = cellNodeRefId.substring(1);
		Node cellspaceNode = getNodeById(doc, cellNodeId);
		List<Node> cellExteriorNode = getDescendants(cellspaceNode, "gml:exterior");
		if(cellExteriorNode.size()<1)
			return null;
		return getPolygon( cellExteriorNode.get(0) );
	}
	
	// Loading From Document
	private static void loadPOIFromIndoorGML(List<POI> pois, Document doc, String layerName) {
		NodeList spaceLayers = doc.getElementsByTagName("core:SpaceLayer");
		for(int i=0;i<spaceLayers.getLength(); ++i) {
			String attribValue = getAttribValue(spaceLayers.item(i), "gml:id");
			if(attribValue!=null && attribValue.equals(layerName)) {
				List<Node> states = getDescendants(spaceLayers.item(i), "core:State");
				if(states!=null) {
					for(Node nd: states) {
						String sid = getAttribValue(nd, "gml:id");
						String name = getDescendantContent(nd, "gml:name", 1);
						String anno = getDescendantContent(nd, "gml:description", 1);
						VIMPoint point = getPoint(nd);
						VIMPolygon polygon = getPolygon(doc, sid);

						POI poi = new POI(sid, layerName, name, anno, point, 0.0, 0.0, polygon);
						pois.add( poi );
					}
				}
			}
		}
	}
	private static void loadVertexFromIndoorGML(List<Vertex> vertices, Document doc, String layerName) {
		NodeList spaceLayers = doc.getElementsByTagName("core:SpaceLayer");
		for(int i=0;i<spaceLayers.getLength(); ++i) {
			String attribValue = getAttribValue(spaceLayers.item(i), "gml:id");
			if(attribValue!=null && attribValue.equals(layerName)) {
				List<Node> states = getDescendants(spaceLayers.item(i), "core:State");
				if(states!=null) {
					for(Node nd: states) {
						String sid = getAttribValue(nd, "gml:id");
						String name = getDescendantContent(nd, "gml:name", 1);
						String anno = getDescendantContent(nd, "gml:description", 1);
						VIMPoint pt = getPoint(nd);

						Vertex vertex = new Vertex(sid, name, anno, null, pt);
						vertices.add( vertex );
					}
				}
			}
		}
	}
	private static void loadEdgeFromIndoorGML(List<Edge> edges, Document doc, List<Vertex> vertices, String layerName) {
		NodeList spaceLayers = doc.getElementsByTagName("core:SpaceLayer");
		for(int i=0;i<spaceLayers.getLength(); ++i) {
			String attribValue = getAttribValue(spaceLayers.item(i), "gml:id");
			if(attribValue!=null && attribValue.equals(layerName)) {
				List<Node> trans = getDescendants(spaceLayers.item(i), "core:Transition");
				if(trans!=null)
					for(Node nd: trans) {
						String id = getAttribValue(nd, "gml:id");
						String name = getChildContent(nd, "gml:name", 1);
						String anno = getChildContent(nd, "gml:description", 1);
						String strWeight = getChildContent(nd, "core:weight", 1);
						String strSrc = getChildAttrib(nd, "core:connects", "xlink:href", 1);
						String strDst = getChildAttrib(nd, "core:connects", "xlink:href", 2);

						double weight=Double.NaN;
						if(strWeight!=null)
							weight = Double.parseDouble(strWeight);

						Vertex src = getVertex(vertices, strSrc.substring(1));
						Vertex dst = getVertex(vertices, strDst.substring(1));

						Edge edge1 = new Edge(id, name, anno, null, src, dst, weight);
						edges.add( edge1 );
					}
			}
		}
	}
	public Graph(
		List<Vertex> vertices, List<Edge> edges, 
   		List<POI> safeties, List<POI> landmarks) {
		this.vertices = vertices;
		this.edges = edges;
		this.safeties = safeties;
		this.landmarks = landmarks;
		this.vertexMap = null;
		this.edgeMap = null;
		makeMap();
	}
	public Graph(InputStream gmlIS) {
		this.vertices = new ArrayList<Vertex>();
		this.edges = new ArrayList<Edge>();
		this.landmarks = new ArrayList<POI>();
		this.safeties = new ArrayList<POI>();
		
		DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
		DocumentBuilder bd = null;
		try {
			bd = bf.newDocumentBuilder();
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		}
		try {
			Document doc = bd.parse(gmlIS);
			loadPOIFromIndoorGML(landmarks, doc, "landmark");
			loadPOIFromIndoorGML(safeties, doc, "safety");
			loadVertexFromIndoorGML(vertices, doc, "network");
			loadEdgeFromIndoorGML(edges, doc, vertices, "network");
		} catch (IOException e) {
			e.printStackTrace();
		} catch(SAXException e) {
			e.printStackTrace();
		}
		makeMap();
	}
//	public Graph(JSONObject jsonGraph) {
//		setFromJSON(jsonGraph);
//	}
//	public Graph(InputStream jsonIS) { // have to distinguish a JSON file and a GML file.
//		try { 
//			byte inputByteString[];
//			inputByteString = new byte[jsonIS.available()];
//			jsonIS.read(inputByteString);
//			setFromJSON(new JSONObject(new String(inputByteString)));  
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch(JSONException e) {
//			e.printStackTrace();
//		}
//	}
	// import
//	private void setFromJSON(JSONObject jsonGraph) { // have to modify
//		this.vertices = null;
//		this.edges = null;
//		this.vertexMap = null;
//		this.edgeMap = null;
//		try {
//			JSONArray jsonVertexArray = jsonGraph.getJSONArray("vertices");
//			this.vertices = new ArrayList<Vertex>();
//			this.vertexMap = new HashMap<String,Vertex>();
//			for (int i=0; i<jsonVertexArray.length(); ++i) {
//				try {
//					JSONObject jsonVertex = jsonVertexArray.getJSONObject(i);
//					Vertex v = new Vertex(jsonVertex);
//					vertices.add(v);
//					vertexMap.put(v.id, v);
//				} catch(JSONException e) {}
//			}
//		} catch(JSONException e) {}
//
//		try {
//			JSONArray jsonEdgeArray = jsonGraph.getJSONArray("edges");
//			this.edges = new ArrayList<Edge>();
//			this.edgeMap = new HashMap<String,Edge>();
//			for (int i=0; i<jsonEdgeArray.length(); ++i) {
//				try {
//					JSONObject jsonEdge = jsonEdgeArray.getJSONObject(i);
//					Vertex srcVertex=null;
//					Vertex dstVertex=null;
//					try {
//						String srcVertexId = jsonEdge.getString("_ref_src_vid");
//						srcVertex = vertexMap.get(srcVertexId);
//					} catch(JSONException e) {}
//					try {
//						String dstVertexId = jsonEdge.getString("_ref_dst_vid");
//						dstVertex = vertexMap.get(dstVertexId);
//					} catch(JSONException e) {}
//					Edge e = new Edge(jsonEdge,srcVertex,dstVertex);
//					edges.add(e);
//					edgeMap.put(e.id, e);
//				} catch(JSONException e) {}
//			}
//		} catch(JSONException e) {}
//	}
	// export
	public void saveToJSONFile(OutputStream fos) throws IOException {
		JSONObject jsonGraph = toJSON();
		byte byteString[] = jsonGraph.toString().getBytes();
		fos.write(byteString);
	}
	public JSONObject toJSON() {
		JSONObject jsonGraph = new JSONObject();
		if (vertices!=null) {
			JSONArray jsonVertexArray = new JSONArray();
			for (Vertex v: vertices)
				if (v!=null)
					jsonVertexArray.put(v.toJSON());
			try { jsonGraph.put("vertices", jsonVertexArray); } catch(JSONException e) {}
		}
		if(edges!=null) {
			JSONArray jsonEdgeArray = new JSONArray();
			for (Edge e: edges)
				if (e!=null)
					jsonEdgeArray.put(e.toJSON());
			try { jsonGraph.put("edges", jsonEdgeArray); } catch(JSONException e) {}
		}
		return jsonGraph;
	}

	private static Vertex getVertex(List<Vertex> vertices, String vertexId) {
		int i;
		for(i=0;i<vertices.size(); ++i) {
			Vertex v = vertices.get(i);
			if(v.id.equals(vertexId))
				return vertices.get(i);
		}
		return null;
	}

	public Vertex getFirstVertex() {
		if( vertices.size()<=0 )
			return null;
		return vertices.get(0);
	}
	public Vertex getLastVertex() {
		if( vertices.size()<=0 )
			return null;
		return vertices.get(vertices.size()-1);
	}
	public Vertex getVertex(String vertexId) {
		if (vertexMap==null)
			return null;
		return vertexMap.get(vertexId);
	}
	public Edge getEdge(String EdgeId) {
		if (edgeMap==null)
			return null;
		return edgeMap.get(EdgeId);
	}
	public List<Vertex> getVertices() {
		return vertices;
	}
	public List<Edge> getEdges() {
		return edges;
	}
	public List<POI> getLandmarks() {
		return landmarks;
	}
	public List<POI> getSafeties() {
		return safeties;
	}
	public List<Edge> getEdgesFrom(Vertex v) {
		List<Edge> elist = new ArrayList<Edge>();
		for(Edge e: edges)
			if(e.src == v)
				elist.add(e);
		return elist;
	}
	public List<Edge> getEdgesTo(Vertex v) {
		List<Edge> elist = new ArrayList<Edge>();
		for(Edge e: edges)
			if(e.dst == v)
				elist.add(e);
		return elist;
	}
	public boolean isIntersection(Edge IncommingEdge) {
		// Intersection:
		//	 If there are two or more ways to proceed from the target vertex except U-turn,
		//	 the vertex is an intersection.
		int count = 0;
		Vertex targetVertex = IncommingEdge.dst;
		for(Edge e: edges) {
			if(e.src == targetVertex && e.dst != IncommingEdge.src)
				++count;
			if(count >= 2)
				return true;
		}
		return false;
	}
	private String getPrecedingVertexName(Edge e) {
		if(e.src.name!=null)
			return e.src.name;
		List<Edge> srcEdges = getEdgesTo(e.src);
		if(srcEdges.size()==0)
			return "";
		return getPrecedingVertexName(srcEdges.get(0));
	}
	private String getFollowingVertexName(Edge e) {
		if(e.dst.name!=null)
			return e.dst.name;
		List<Edge> dstEdges = getEdgesFrom(e.dst);
		if(dstEdges.size()==0)
			return "";
		return getFollowingVertexName(dstEdges.get(0));
	}
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


