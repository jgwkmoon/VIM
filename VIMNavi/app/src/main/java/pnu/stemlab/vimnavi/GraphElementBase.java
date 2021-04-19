package pnu.stemlab.vimnavi;

import java.util.*;
import org.json.*;

public class GraphElementBase {
	public String id;
	public String name;
	public String type; // "stairs", "corridor", "room", "door", "lobby", "elevator",
//	public List<POI> pois;

	GraphElementBase(String id, String name, String type, List<POI> pois) {
		this.id = id;
		this.name = name;
		this.type = type;
//		this.pois = pois;
	}
	GraphElementBase(JSONObject jsonGE) {
		this.id = null;
		this.type = null;
		this.name = null;
//		this.pois = null;
		try { this.id = jsonGE.getString("id"); } catch(JSONException e) {}
		try { this.type = jsonGE.getString("type"); } catch(JSONException e) {}
		try { this.name = jsonGE.getString("name"); } catch(JSONException e) {}
//		try {
//			JSONArray jsonPOIArray = jsonGE.getJSONArray("pois");
//			this.pois = new ArrayList<POI>();
//			for (int i=0; i<jsonPOIArray.length(); ++i)
//				this.pois.add( new POI(jsonPOIArray.getJSONObject(i)) );
//		} catch(JSONException e) {}
	}
	public JSONObject toJSON() {
		JSONObject jsonGE = new JSONObject();
		if(this.id!=null) {
			try { jsonGE.put("id", this.id); } catch(JSONException e) {}
		}
		if(this.name!=null) {
			try { jsonGE.put("name", this.name); } catch(JSONException e) {}
		}
		if(this.type!=null) {
			try { jsonGE.put("type", this.type); } catch(JSONException e) {}
		}
//		if(this.pois!=null) {
//			JSONArray jsonPOIArray = new JSONArray();
//			for (POI e: pois)
//				jsonPOIArray.put(e.toJSON());
//			try { jsonGE.put("pois", jsonPOIArray); } catch(JSONException e) {}
//		}
		return jsonGE;
	}
}
