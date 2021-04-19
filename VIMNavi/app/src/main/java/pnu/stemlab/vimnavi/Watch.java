package pnu.stemlab.vimnavi;

import android.util.Log;

public class Watch {  // for android
	private static String prevMessage = "";
	private static String section = "";

	public static void putln(String section, String message) {
		Watch.section = section;
		String messages[] = message.split("\n");
		for(String msg: messages)
			Log.i(section, msg);
		prevMessage = "";
	}
	public static void put(String section, String message) {
		Watch.section = section;
		prevMessage = section + message;
	}
	public static void add(String message) {
		prevMessage += message;
	}
	public static void addln(String message) {
		message = prevMessage + message;
		String messages[] = message.split("\n");
		for(String msg: messages)
			Log.i(Watch.section, msg);
		prevMessage = "";
	}
}

//public class Watch { // for non-android
//	public static void putln(String section, String message) {
//		System.out.println(message);
////		System.out.println(section + ": " + message);
//		System.out.flush();
//	}
//	public static void put(String section, String message) {
//		System.out.print(message);
////		System.out.print(section + ": " + message);
//	}
//	public static void add(String message) {
//		System.out.print(message);
//	}
//	public static void addln(String message) {
//		System.out.println(message);
//		System.out.flush();
//	}
//	public static String repeat(String str, int num) {
//		StringBuffer ret = new StringBuffer();
//		for(int i=0; i<num; ++i)
//			ret.append(str);
//		return new String(ret);
//	}
//}
