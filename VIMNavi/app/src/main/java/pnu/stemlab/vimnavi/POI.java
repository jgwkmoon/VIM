package pnu.stemlab.vimnavi;

import org.json.*;

public class POI {
	// POI type: "landmark", "safety"
	// - safety: "stairs", "escalator", "security checkpoiPos", "revolving door"
	// - landmark: ??
	public final String id;
	public final String type;
	public final String name;
	public final String anno; // annotation
	
	public final VIMPoint poiPos;
	public final double begDist; // distance in millimeter
	public final double endDist; // distance in millimeter
	public VIMPolygon boundary;

	public POI(String id,String type,String name,String anno,
		VIMPoint poiPos,double begDist,double endDist,VIMPolygon boundary) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.anno = anno;
		this.poiPos = poiPos;
		this.begDist = begDist;
		this.endDist = endDist;
		this.boundary = boundary;
	}
	public POI(JSONObject jsonPOI) {
		String id = null;
		String type = null;
		String name = null;
		String anno = null;
		JSONObject jsonPoiPos = null;
		double begDist = Double.NaN;
		double endDist = Double.NaN;
		JSONObject jsonBounary = null;
		try { id = jsonPOI.getString("id"); } catch(JSONException e) {}
		try { type = jsonPOI.getString("type"); } catch(JSONException e) {}
		try { name = jsonPOI.getString("name"); } catch(JSONException e) {}
		try { anno = jsonPOI.getString("anno");	} catch(JSONException e) {}
		try { jsonPoiPos = jsonPOI.getJSONObject("poiPos"); } catch(JSONException e) {}
		try { begDist = jsonPOI.getDouble("begDist"); } catch(JSONException e) {}
		try { endDist = jsonPOI.getDouble("endDist"); } catch(JSONException e) {}
		try { jsonBounary = jsonPOI.getJSONObject("bounary"); } catch(JSONException e) {}
		this.id = id;
		this.type = type;
		this.name = name;
		this.anno = anno;
		this.poiPos = new VIMPoint(jsonPoiPos);
		this.begDist = begDist;
		this.endDist = endDist;
		this.boundary = new VIMPolygon(jsonBounary);
	}
	public JSONObject toJSON() {
		JSONObject jsonPOI = new JSONObject();
		if(this.id!=null) {
			try { jsonPOI.put("id", this.id); } catch(JSONException e) {}
		}
		if(this.type!=null) {
			try { jsonPOI.put("type", this.type); } catch(JSONException e) {}
		}
		if(this.name!=null) {
			try { jsonPOI.put("name", this.name); } catch(JSONException e) {}
		}
		if(this.anno!=null) {
			try { jsonPOI.put("anno", this.anno); } catch(JSONException e) {}
		}
		if(this.poiPos!=null) {
			try { jsonPOI.put("poiPos", this.poiPos.toJSON()); } catch(JSONException e) {}
		}
		if(CoordSystem.isValidCoord(this.begDist)) {
			try { jsonPOI.put("begDist", this.begDist); } catch(JSONException e) {}
		}
		if(CoordSystem.isValidCoord(this.endDist)) {
			try { jsonPOI.put("endDist", this.endDist); } catch(JSONException e) {}
		}
		if(this.boundary!=null) {
			try { jsonPOI.put("boundary", this.boundary.toJSON()); } catch(JSONException e) {}
		}
		return jsonPOI;
	}
	@Override
	public boolean equals(Object obj) {
		return
			true
			&& (obj instanceof POI)
			&& this.id == ((POI)obj).id
			;
	}
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
}
