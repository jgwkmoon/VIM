package pnu.stemlab.vimnavi;

import java.util.*;
import org.json.*;

public class Vertex extends GraphElementBase {
	public VIMPoint point;

	public Vertex(String id, String name) {
		super(id,name,null,null);
		this.point = null;
	}
	public Vertex(String id, String name, String type, List<POI> pois, VIMPoint point) {
		super(id, name, type, pois);
		this.point = point;
	}
	public Vertex(JSONObject jsonVertex) {
		super(jsonVertex);
		this.point = null;
		try {
			JSONObject jsonPoint = jsonVertex.getJSONObject("point");
			this.point = new VIMPoint(jsonPoint);
		} catch(JSONException e) {}
	}
	public JSONObject toJSON() {
		JSONObject jsonVertex = super.toJSON();
		if (this.point!=null) {
			try { jsonVertex.put("point", this.point.toJSON()); } catch(JSONException e) {}
		}
		return jsonVertex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vertex other = (Vertex) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() { // for testing
		return id;
	}
}
