package pnu.stemlab.vimnavi;

import java.util.*;
import org.json.*;

public class Edge extends GraphElementBase {
	public Vertex src;
	public Vertex dst;
	public double weight;

	public Edge(String id, String name, Vertex src, Vertex dst, double weight) {
		super(id, name, null, null);
		this.src = src;
		this.dst = dst;
		this.weight = weight;
	}
	public Edge(String id, String name, String type, List<POI> pois, Vertex src, Vertex dst, double weight) {
		super(id, name, type, pois);
		this.src = src;
		this.dst = dst;
		this.weight = weight;
	}
	public Edge(JSONObject jsonEdge, Vertex src, Vertex dst) {
		super(jsonEdge);
		this.src = src;
		this.dst = dst;
		this.weight = Double.NaN;
		try { this.weight = jsonEdge.getDouble("weight");	} catch(JSONException e) {}
	}
	public JSONObject toJSON() {
		JSONObject jsonEdge = super.toJSON();
		if (this.src!=null) {
			try { jsonEdge.accumulate("_ref_src_vid", this.src.id); } catch(JSONException e) {}
		}
		if (this.dst!=null) {
			try { jsonEdge.accumulate("_ref_dst_vid", this.dst.id); } catch(JSONException e) {}
		}
		if (CoordSystem.isValidCoord(this.weight)) {
			try { jsonEdge.accumulate("weight", this.weight); } catch(JSONException e) {}
		}
		return jsonEdge;
	}

	@Override
	public String toString() {  // for testing
		return id + "(" + src + "->" + dst + ")";
	}
}
