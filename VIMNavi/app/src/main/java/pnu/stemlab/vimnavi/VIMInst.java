package pnu.stemlab.vimnavi;

import java.util.List;

public class VIMInst {
	public int id;
	public final String type;
	public final String currTarget;
	public final double distToCurrTarget;
	public final String nextTarget;
	public final String message;
	public final String info;
	public VIMInst(int id, String type, String currTarget, double distToCurrTarget, String nextTarget, String message, String info) {
		this.id = id;
		this.type = type;
		this.currTarget = currTarget;
		this.distToCurrTarget = distToCurrTarget;
		this.nextTarget = nextTarget;
		this.message = message;
		this.info = info;
	}
	public String getHeader() {
		return "[" + type + " on " + currTarget + "]";
	}
	public String getInfo() {
		return "(" + info + ")";
	}
	public String getLongMessage() {
		return getHeader() + " " + message + " " + getInfo();
	}
	public String getShortMessage() {
		return message;
	}
	public String toString() {
		return getShortMessage();
	}
	public boolean isSameInst(VIMInst inst) {
		return type.equals(inst.type) && currTarget.equals(inst.currTarget);
//		return this.getHeader().equals(inst.getHeader());
	}
	public boolean isDupInst(List<VIMInst> prevInsts) {
		for(VIMInst inst: prevInsts)
			if(isSameInst(inst))
				return true;
		return false;
	}
//	public boolean isValidEnd(List<VIMInst> prevInsts) {
//		if( !this.type.equals("END") )
//			return false;
//		for(VIMInst inst: prevInsts)
//			if(inst.type.equals("START") && inst.nextTarget.equals(this.currTarget))
//				return true;
//		return false;
//	}
	public boolean isInvalidEnd(List<VIMInst> prevInsts) {
		if( !this.type.equals("END") )
			return false;
		for(VIMInst inst: prevInsts) {
			if(inst.type.equals("TURN") && inst.currTarget.equals(this.currTarget))
				return true;
		}
		for(VIMInst inst: prevInsts) {
			if(inst.type.equals("START") && inst.nextTarget.equals(this.currTarget))
				return false;
		}
		return true;
	}
	public boolean isInvalidAuto(List<VIMInst> prevInsts) {
		if( !this.type.equals("AUTO") )
			return false;
		String currTarget = this.currTarget.substring(0, this.currTarget.lastIndexOf('$'));
		for(VIMInst inst: prevInsts) {
			if(inst.type.equals("TURN") && inst.currTarget.equals(currTarget))
				return true;
		}
		for(VIMInst inst: prevInsts) {
			if(inst.type.equals("START") && inst.nextTarget.equals(currTarget))
				return false;
		}
		return true;
	}
}
