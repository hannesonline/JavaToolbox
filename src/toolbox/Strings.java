package toolbox;

import java.util.ArrayList;

public class Strings {

	public static String vectorToString(ArrayList<String> inhalt) {
		StringBuilder sb=new StringBuilder();
		for (String string : inhalt) {
			sb.append(string);
		}
		return sb.toString();
	}
	public static String vectorToStringLN(ArrayList<String> inhalt) {
		StringBuilder sb=new StringBuilder();
		for (String string : inhalt) {
			sb.append(string);
			sb.append(Datei.LN);
		}
		return sb.toString();
	}

}
