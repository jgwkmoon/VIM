package pnu.stemlab.vimnavi;

import java.text.SimpleDateFormat;

public class Format {
	public static String base(GraphElementBase bs, String sep) {
		return String.format("%s%s%s%s%s", bs.id, sep, bs.name, sep, bs.type);
	}
	public static String poi(POI p, String sep) {
		return String.format("%-10s%s%-40s%s%-10s%s%-40s", 
			p.id, sep, 
			p.name, sep, 
			(p.type.equals("") ? "<none>" : p.type), sep, 
			p.anno
		);
	}
	public static String coord(double crd) {
		return String.format("%7.1f", crd);
	}
	public static String direction(double dir) {
		return String.format("%5.1f", dir);
	}
	public static String dateTime(long tm) {
		SimpleDateFormat sdForm 
			= new SimpleDateFormat("yyy-MM-dd/hh:mm:ss.SSS");
		return sdForm.format(tm);
	}
	public static String time(long tm) {
		SimpleDateFormat sdForm 
			= new SimpleDateFormat("hh:mm:ss.SSS");
		return sdForm.format(tm);
	}
	public static String point(VIMPoint vimPt) {
		return String.format("(%8.1f, %8.1f, %8.1f)", vimPt.x, vimPt.y, vimPt.z);
	}
}
