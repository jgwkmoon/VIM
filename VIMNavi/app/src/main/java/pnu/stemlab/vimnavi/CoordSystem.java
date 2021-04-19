package pnu.stemlab.vimnavi;

public class CoordSystem {
	public static boolean isValidCoord(double v) {
		return !(Double.isInfinite(v) || Double.isNaN(v));
	}
	public static boolean isValidDir(double v) {
		return !(Double.isInfinite(v) || Double.isNaN(v)) && 0.0<=v && v<360.0;
	}
	public static double dirNorm(double v) {
		if(v>=0.0) {
			return v - Math.floor(v/360.0)*360.0;
		} else {
			return v + Math.ceil(-v/360.0)*360.0;
		}
	}
	public static double dirDiff(double v1, double v2) {
		double absDiff = dirNorm(Math.abs(v1 - v2));
		return absDiff>180 ? 360.0-absDiff : absDiff;
	}
}
