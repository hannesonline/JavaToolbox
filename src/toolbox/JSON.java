package toolbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.zip.DataFormatException;

/**
JSON_object json=new JSON_object("...");
 */
public interface JSON {
	
	public static class String_mit_Iterator{
		String string;
		int iterator;
		int length;
		public String_mit_Iterator(String string){
			this.string=string;
			iterator=0;
			length=string.length();
		}
		public char next() throws DataFormatException{
			if (iterator<length){
				return string.charAt(iterator++);
			}
			throw new DataFormatException();
		}
		public String next(int i) {
			int ende=iterator+i;
			String r=string.subSequence(iterator, ende).toString();
			iterator=ende;
			return r;
		}
		public boolean isValid() {
			return (iterator<length);
		}
		public void zurueck() {
			iterator--;
		}
	}
	
	public String tofile(int level);

	/**
	 * JSON-Objekt.
	 */
	public static class JSON_object implements JSON{
		
		/**
		 * Liste von Pairs
		 */
		Vector<Pair> members;

		/**
		 * Erstellt ein JSON-Objekt aus einer JSON-Datei
		 */
		public JSON_object(String datei_name){
				members=new Vector<JSON.Pair>();
				if (!new File(datei_name).exists()){
				return;
			}
			Datei datei=new Datei(datei_name, Datei.LESEN);
			String_mit_Iterator file=new String_mit_Iterator(lesenAlles2(datei));
			datei.schliessen();
			members=fromfile(file);
		}
		
		private static String lesenAlles2(Datei datei) {
			return Strings.vectorToString(datei.lesenAlles());
		}

		public JSON_object(String_mit_Iterator file){
			members=fromfile(file);
		}
		public JSON_object() {
			members=new Vector<JSON.Pair>();
		}
		
		private Vector<Pair> fromfile(String_mit_Iterator file) {
			/* v
			 * { "name": value, ... }
			 */
			Vector<Pair> members=new Vector<Pair>();
			char zeichen='}';
			try { zeichen=file.next(); } catch (DataFormatException e) { new Error("Datei falsch formatiert!"); }
			/*  v
			 * { "name": value, ... }
			 */
			
			while (zeichen!='}' && file.isValid()){
				if (zeichen=='"'){
					/*   v
					 * { "name": value, ... }
					 */
					String string=Value_string.import_string(file);
					/*        v
					 * { "name": value, ... }
					 */
					Pair pair=new Pair(string,file);
					/*               v
					 * { "name": value, ... }
					 */
					members.add(pair);
				}
				try { zeichen=file.next(); } catch (DataFormatException e) {  }
				/*                v
				 * { "name": value, ... }
				 */
			}
			/*                      v
			 * { "name": value, ... }
			 */
			return members;
		}

		@Override
		public String tofile(int level) {
			StringBuilder string=new StringBuilder();
			string.append("{"+Datei.LN);
			for (Pair member : members) {
				string.append(Datei.level_tabs(level+1)+member.tofile(level+1));
				string.append(","+Datei.LN);
			}
			if (!members.isEmpty()) string.delete(string.length()-3, string.length());
			string.append(Datei.LN+Datei.level_tabs(level)+"}");
			return string.toString();
		}

		public Vector<Pair> getMembers() {
			return members;
		}

		public Pair getMember(int i) {
			return members.get(i);
		}

		public JSON getMember(String string) {
			return getMember(string, false); }
		public JSON getMember(String string, boolean ifExists) {
			for (Pair member : members) {
				if (member.string.equals(string)) return member.value;
			}
			if (!ifExists) new Error("Member \"" + string+ "\" nicht gefunden!");
			return null;
		}
		public Pair getPair(String name) {
			for (Pair member : members) {
				if (member.string.equals(name)) return member;
			}
			return null;
		}
		public ArrayList<JSON> getMembers(String string) {
			ArrayList<JSON> all=new ArrayList<JSON>(); 
			for (Pair member : members) {
				if (member.string.equals(string)) all.add(member.value);
			}
			return all;
		}
		public int getMember_index(String name) {
			for (int i=0;i<members.size();i++){
				if (members.get(i).string.equals(name)) return i;
			}
			return -1;
		}

		public JSON_object getMember_Object_orNew(String string) {
			JSON_object m=getMember_Object(string,false);
			if (m==null){
				m=new JSON_object();
				members.add(new Pair(string, m));
			}
			return m;
		}
		public JSON_object getMember_Object(String string,boolean meldung) {
			JSON member=getMember(string,!meldung);
			if (member!=null && member instanceof JSON_object) return (JSON_object)member;
			if (meldung){
				new Error("Member \"" + string
						+ "\" ist kein JSON-Objekt!");
			}
			return null;
		}

		public Value_string getMember_String(String string) {
			JSON member=getMember(string);
			if (member!=null && member instanceof Value_string) return (Value_string)member;
			new Error("Member \"" + string
					+ "\" ist kein String!");
			return null;
		}

		public String getMemberValue_String(String string) {
			return getMemberValue_String(string, true, null); }
		public String getMemberValue_String(String string, String sonst) {
			return getMemberValue_String(string, false, sonst); }
		public String getMemberValue_String(String string, boolean meldung, String sonst) {
			JSON member=getMember(string,!meldung);
			if (member!=null && member instanceof Value_string){
				return ((Value_string)member).getString();
			}
			if (meldung) new Error("Member \"" + string
					+ "\" ist kein String!");
			return sonst;
		}
		
		public double getMemberValue_Double(String string) {
			return getMemberValue_Double(string, true, Double.NaN); }
		public double getMemberValue_Double(String string, double sonst) {
			return getMemberValue_Double(string, false, sonst); }
		public double getMemberValue_Double(String string, boolean meldung, double sonst) {
			JSON member=getMember(string,!meldung);
			if (member!=null && member instanceof Value_double){
				return ((Value_double)member).number;
			}
			if (meldung) new Error("Member \"" + string
					+ "\" ist kein Double!");
			return sonst;
		}

		public boolean getMemberValue_Bool(String string) {
			return getMemberValue_Bool(string, true, false); }
		public boolean getMemberValue_Bool(String string, boolean sonst) {
			return getMemberValue_Bool(string, false, sonst); }
		public boolean getMemberValue_Bool(String string, boolean meldung, boolean sonst) {
			JSON member=getMember(string,!meldung);
			if (member!=null && member instanceof Value_bool){
				return ((Value_bool)member).bool;
			}
			if (meldung) new Error("Member \"" + string + "\" ist kein Bool!");
			return sonst;
		}

		
		public JSON_array getMember_Array(String string) {
			return getMember_Array(string, true); }
		public JSON_array getMember_Array(String string, boolean meldung) {
			JSON member=getMember(string,!meldung);
			if (member!=null && member instanceof JSON_array) return (JSON_array)member;
			if(meldung)new Error("Member \"" + string
					+ "\" ist kein Array!");
			return null;
		}

		public Pair getPair(int i) {
			return members.get(i);
		}

		public void tofile(String file) {
			Datei datei=new Datei(file, Datei.SCHREIBEN);
			datei.schreibenln(tofile(0));
			datei.schliessen();
		}

		public void addPair_override(String name, Object value) {
			Pair member=getPair(name);
			if (member!=null){
				if (value==null) member.value=new Value_null();
				else if (value instanceof JSON) member.value=((JSON)value);
				else if (value instanceof Boolean) member.value=new Value_bool((Boolean)value);
				else if (value instanceof Double) member.value=new Value_double((Double)value);
				else if (value instanceof Integer) member.value=new Value_double((Integer)value);
				else if (value instanceof String) member.value=new Value_string((String)value);
				else if (value instanceof Double[]){
					member.value=new JSON_array((Double[])value);
				}
				else if (value instanceof String[][]){
					String[][] v=(String[][])value;
					JSON_array[] arrays=new JSON_array[v.length];
					for (int i = 0; i < arrays.length; i++) {
						arrays[i]=new JSON_array(v[i]);
					}
					member.value=new JSON_array(arrays);
				}
				else{
					new Error("Unbekannter Wertetyp: "+value.getClass().getName());
				}
			}else{
				addPair_add(name, value);
			}
		}

		public void addPair_add(String name, Object value) {
			members.add(new Pair(name,value));
		}

		public void removeChild(Pair pair) {
			members.remove(pair);
		}

	}

	/**
	 * Liste von Values
	 */
	public static class JSON_array implements JSON {
		Vector<JSON> elements;
		public JSON_array(String_mit_Iterator file) {
			fromfile(file);
		}
		public JSON_array(Double[] value) {
			elements=new Vector<JSON>();
			for (Double d : value) {
				elements.add(new Value_double(d));
			}
		}
		public JSON_array(JSON[] value) {
			elements=new Vector<JSON>();
			for (JSON d : value) {
				elements.add(d);
			}
		}
		public JSON_array(Vector<JSON> values) {
			elements=values;
		}
		public JSON_array(String[][] value) {
			elements=new Vector<JSON>();
			for (String[] d : value) {
				elements.add(new JSON_array(d));
			}
		}
		public JSON_array(String[] value) {
			elements=new Vector<JSON>();
			for (String d : value) {
				elements.add(new Value_string(d));
			}
		}
		public JSON_array(ArrayList<String> value) {
			elements=new Vector<JSON>();
			for (String d : value) {
				elements.add(new Value_string(d));
			}
		}
		public JSON_array(double[] value) {
			elements=new Vector<JSON>();
			for (Double d : value) {
				elements.add(new Value_double(d));
			}
		}
		private void fromfile(String_mit_Iterator file) {
			/* v
			 * [value, ... ]
			 */
			char zeichen=']';
			try { zeichen=file.next(); } catch (DataFormatException e) { new Error("Datei falsch formatiert!"); }
			/*  v
			 * [value, ... ]
			 */
			elements=new Vector<JSON>();
			while (zeichen!=']' && file.isValid()){
				JSON value=Pair.import_value(zeichen, file);
				if (value!=null) elements.add(value);
				try { zeichen=file.next(); } catch (DataFormatException e) {  }
			}
			/*             v
			 * [value, ... ]
			 */
		}
		@Override
		public String tofile(int level) {
			if (elements==null||elements.isEmpty()) return "[ ]";
			StringBuilder string=new StringBuilder();
			boolean isArray=(elements.firstElement() instanceof JSON_array||elements.firstElement() instanceof JSON_object);
			
			if (isArray){
				string.append("["+Datei.LN);
				for (JSON element : elements) {
					string.append(Datei.level_tabs(level+1)+element.tofile(level+1));
					string.append(","+Datei.LN);
				}
				if (!elements.isEmpty()) string.delete(string.length()-3, string.length());
				string.append(Datei.LN+Datei.level_tabs(level)+"]");
			}else{
				string.append("["+(isArray?Datei.LN:" "));
				for (JSON element : elements) {
					string.append(element.tofile(level));
					string.append(", ");
				}
				if (!elements.isEmpty()) string.delete(string.length()-2, string.length());
				string.append(" ]");
			}
			
			return string.toString();
		}

		public double[] getDoubles(String claim) {
			int size=elements.size();
			double[] r=new double[size];
			boolean ok=true;
			for (int i=0;i<size;i++){
				JSON element=elements.get(i);
				if (element!=null && element instanceof Value_double){
					r[i]=((Value_double)element).number;
				}else{
					ok=false;
				}
			}
			if (!ok){
				if (claim==null) claim="Array enthält fehlerhafte Einträge!";
				new Error(claim+tofile(1));
			}
			return r;
		}
		public double[] getDoubles() {
			return getDoubles(null);
		}

		public JSON_object[] getObjects() {
			int size=elements.size();
			JSON_object[] r=new JSON_object[size];
			boolean ok=true;
			for (int i=0;i<size;i++){
				JSON element=elements.get(i);
				if (element!=null && element instanceof JSON_object){
					r[i]=((JSON_object)element);
				}else{
					ok=false;
				}
			}
			if (!ok) new Error("Array enthält fehlerhafte Einträge!"+tofile(1));
			return r;
		}

		public String[] getStrings() {
			int size=elements.size();
			String[] r=new String[size];
			boolean ok=true;
			for (int i=0;i<size;i++){
				JSON element=elements.get(i);
				if (element!=null && element instanceof Value_string){
					r[i]=((Value_string)element).string;
				}else{
					ok=false;
				}
			}
			if (!ok) new Error("Array enthält fehlerhafte Einträge!"+tofile(1));
			return r;
		}

		public JSON_array[] getArrays() {
			int size=elements.size();
			JSON_array[] r=new JSON_array[size];
			boolean ok=true;
			for (int i=0;i<size;i++){
				JSON element=elements.get(i);
				if (element!=null && element instanceof JSON_array){
					r[i]=((JSON_array)element);
				}else{
					ok=false;
				}
			}
			if (!ok) new Error("Array enthält fehlerhafte Einträge!");
			return r;
		}

		public double[][] getDoublesDoubles() {
			int size=elements.size();
			double[][] r=new double[size][];
			JSON_array[] e=getArrays();
			for (int i=0;i<size;i++){
				JSON_array y=e[i];
				r[i]=y.getDoubles();
			}
			return r;
		}
	}

	public static class Value_bool implements JSON {
		public boolean bool;
		public Value_bool(boolean b) {
			this.bool=b;
		}
		@Override
		public String tofile(int level) {
			return (bool?"true":"false");
		}
	}

	public static class Value_double implements JSON {
		public double number;
		public Value_double(double n) {
			this.number=n;
		}
		private static double import_number(String_mit_Iterator file, char zeichen) {
			/* v
			 * -123.4e5
			 */
			StringBuilder string=new StringBuilder();
			while (file.isValid()&&(
					(zeichen>='0'&&zeichen<='9')
					||zeichen=='-'
					||zeichen=='+'
					||zeichen=='.'
					||zeichen=='e'
					||zeichen=='E'
				)){
				string.append(zeichen);
				try { zeichen=file.next(); } catch (DataFormatException e) { new Error("Datei falsch formatiert!"); }
			}
			/*         v
			 * -123.4e5
			 */
			file.zurueck();
			/*        v
			 * -123.4e5
			 */
			double r=Double.NaN;
			try {
				r=Double.parseDouble(string.toString());
			} catch (Exception e) {
				new Error("Fehlerhafte Werte in JSON-Datei!");
			}
			return r;
		}
		@Override
		public String tofile(int level) {
			return Double.toString(number);
		}
	}

	public static class Value_string implements JSON {
		String string;
		public Value_string(String_mit_Iterator file) {
			this.string=import_string(file);
		}
		public Value_string(String value) {
			this.string=value;
		}
		private static String import_string(String_mit_Iterator file) {
			/* v
			 * "string"
			 */
			StringBuilder string=new StringBuilder();
			char zeichen='"';
			try { zeichen=file.next(); } catch (DataFormatException e) { new Error("Datei falsch formatiert!"); }
			/*  v
			 * "string"
			 */
			while (zeichen!='"' && file.isValid()){
				if (zeichen=='\\'){
					try { zeichen=file.next(); } catch (DataFormatException e) { new Error("Datei falsch formatiert!"); }
					switch (zeichen) {
						case '"': string.append('"'); break;
						case '\\': string.append('\\'); break;
						case '/': string.append('/'); break;
						case 'b': string.append('\b'); break;
						case 'f': string.append('\f'); break;
						case 'n': string.append('\n'); break;
						case 'r': string.append('\r'); break;
						case 't': string.append('\t'); break;
						case 'u': string.append((char)Integer.parseInt(file.next(4), 16)); break;
						default:
							new Error("Ungültiges Sonderzeichen: \"\\"+zeichen+"\"!");
							break;
					}
				}else{
					string.append(zeichen);
				}
				try { zeichen=file.next(); } catch (DataFormatException e) { new Error("Datei falsch formatiert!"); }
			}
			/*        v
			 * "string"
			 */
			return string.toString();
		}
		@Override
		public String tofile(int level) {
			StringBuilder string=new StringBuilder();
			string.append("\"");
			maskString(string,this.string);
			string.append("\"");
			return string.toString();
		}
		public String getString() {
			return string;
		}
		public static void maskString(StringBuilder string, String s) {
			char[] allezeichen=s.toCharArray();
			for (char zeichen : allezeichen) {
				switch (zeichen) {
					case '"': string.append("\\\""); break;
					case '\\': string.append("\\\\"); break;
					case '/': string.append("\\/"); break;
					case '\b': string.append("\\b"); break;
					case '\f': string.append("\\f"); break;
					case '\n': string.append("\\n"); break;
					case '\r': string.append("\\r"); break;
					case '\t': string.append("\\t"); break;
					//TODO:case 'u': string.append((char)Integer.parseInt(file.next(4), 16)); break;
					default:
						string.append(zeichen);
						break;
				}
			}
		}
	}

	public static class Value_null implements JSON {
		@Override
		public String tofile(int level) {
			return "null";
		}
	}

	/**
	 * Name und Wert
	 */
	public class Pair {
		String string;
		public JSON value;
		public Pair(String string, String_mit_Iterator file) {
			this.string=string;
			try {
				this.value=import_value(file.next(), file);
			} catch (DataFormatException e) {
				new Error("Datei falsch formatiert!");
				this.value=null;
			}
		}

		public Pair(String name, Object value) {
			this.string=name;
			if (value==null) this.value=new Value_null();
			else if (value instanceof JSON) this.value=((JSON)value);
			else if (value instanceof String) this.value=new Value_string((String)value);
			else if (value instanceof Double) this.value=new Value_double((Double)value);
			else if (value instanceof Double[]) this.value=new JSON_array((Double[])value);
			else if (value instanceof String[][]) this.value=new JSON_array((String[][])value);
			else if (value instanceof Integer) this.value=new Value_double((Integer)value);
			else if (value instanceof Boolean) this.value=new Value_bool((Boolean)value);
			else{
				new Error("Ungültiger Datentyp: "+value.getClass().getName());
			}
		}
		private static JSON import_value(char zeichen, String_mit_Iterator file) {
			/* v
			 * "string"
			 * 123
			 * { object }
			 * [ array ]
			 * true
			 * false
			 * null
			 */
			JSON value=null;
			while (value==null && file.isValid()){
				if (zeichen=='"'){
					/* v
					 * "string"
					 */
					value=new Value_string(file);
					/*        v
					 * "string"
					 */
				}else if ((zeichen>='0'&&zeichen<='9')||zeichen=='-'||zeichen=='+'||zeichen=='.'){
					/* v
					 * -123.4e5
					 */
					double n=Value_double.import_number(file,zeichen);
					/*        v
					 * -123.4e5
					 */
//					if (Toolbox.ganzzahl(n)){
//						value=new Value_integer((int)n);
//					}else
					{
						value=new Value_double(n);
					}
				}else if (zeichen=='{'){
					/* v
					 * { "name": value, ... }
					 */
					value=new JSON_object(file);
					/*                      v
					 * { "name": value, ... }
					 */
				}else if (zeichen=='['){
					/* v
					 * [ value, ... ]
					 */
					value=new JSON_array(file);
					/*              v
					 * [ value, ... ]
					 */
				}else if (zeichen=='t'||zeichen=='T'){
					value=new Value_bool(true);
				}else if (zeichen=='f'||zeichen=='F'){
					value=new Value_bool(false);
				}else if (zeichen=='n'||zeichen=='N'){
					value=new Value_null();
				}else if (zeichen==']'||zeichen=='}'){
					file.zurueck();
					//System.err.println("Datei falsch formatiert! 401");
					return null;
				}else{
					try { zeichen=file.next(); } catch (DataFormatException e) { new Error("Datei falsch formatiert!"); }
				}
			}
			/*     v
			 * value...
			 */
			return value;
		}
		
		public String tofile(int level) {
			StringBuilder string=new StringBuilder();
			string.append("\"");
			Value_string.maskString(string, this.string);
			string.append("\": ");
			if (value!=null){
				string.append(value.tofile(level));
			}else{
				new Error("Wert darf nicht null sein!");
			}
			return string.toString();
		}
		
		public JSON_array getValue_Array() {
			if (value!=null && value instanceof JSON_array) return (JSON_array)value;
			new Error("Member \"" + string
					+ "\" ist kein Array!");
			return null;
		}

		public Value_bool getValue_Boolean() {
			if (value!=null && value instanceof Value_bool) return (Value_bool)value;
			new Error("Member \"" + string
					+ "\" ist kein Boolean!");
			return null;
		}

		public Value_double getValue_Double() {
			return getValue_Double(true); }
		public Value_double getValue_Double(boolean meldung) {
			if (value!=null && value instanceof Value_double) return (Value_double)value;
			if (meldung){
				new Error("Member \"" + string + "\" ist kein Zahlenwert!");
			}
			return null;
		}

		public Value_string getValue_String() {
			return getValue_String(true);
		}
		public Value_string getValue_String(boolean meldung) {
			if (value!=null && value instanceof Value_string) return (Value_string)value;
			if (meldung) new Error("Member \"" + string
					+ "\" ist keine Zeichenkette!");
			return null;
		}

		public JSON_object getValue_Object() {
			if (value!=null && value instanceof JSON_object) return (JSON_object)value;
			new Error("Member \"" + string
					+ "\" ist kein Object!");
			return null;
		}

		public String getString() {
			return string;
		}

		public String getName() {
			return string;
		}

	}

}
