package toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class Datei {
	/*
	 * Datei-Toolbox
	 */

	int modus=0;
	public static final int SCHREIBEN                   =1;
	public static final int LESEN                       =3;
	
	public String zeichensatz=null;
	
	public static final String LF=String.valueOf((char)10);// (L)ine (F)eed (Zeilenvorschub)
	public static final String CR=String.valueOf((char)13);// (C)arriage (R)eturn (Wagenrücklauf)
	public static final String LN=CR+LF;                   // Zeilenumbruch unter Windows
	
	File datei;
	FileWriter writer;
	BufferedReader reader;

	public Datei() {}
	public Datei(String datei) {
		this.datei=new File(datei);
	}
	public Datei(String datei, int modus) {
		this.datei=new File(datei);
		oeffnen(modus);
	}

	public void schreiben(String string) {
		try {
			writer.write(string);
		} catch (IOException e) {} catch (NullPointerException e) {}
	}
	public void schreibenln(String string) { schreiben(string+LN); }

	public String lesenln() {
		/*
		 * Einlesen einer Zeile
		 */
		{
			String zeile = null;
			
			try { zeile=reader.readLine(); }
			catch (IOException e) {}
			catch (NullPointerException e) {}
			
			return zeile;
		}
	}

	public void oeffnen(int modus) {
		this.modus=modus;
		try {
			if (modus==SCHREIBEN) writer=new FileWriter(datei);
			else if (modus==LESEN){
				if (zeichensatz==null){
					reader=new BufferedReader(new FileReader(datei));
				}else{
					InputStream stream=new FileInputStream(datei);
					reader=new BufferedReader(new InputStreamReader(stream,zeichensatz));
				}
			}
		} catch (IOException e) {}
	}
	public void oeffnen(String datei, int modus) {
		this.datei=new File(datei);
		oeffnen(modus);
	}

	public void schliessen() {
		try {
			if (writer!=null) writer.close();
			if (reader!=null) reader.close();
		} catch (IOException e) {}
	}

	public ArrayList<String> lesenAlles() {
		ArrayList<String> alles=new ArrayList<String>();
		String zeile;
		while ((zeile=lesenln())!=null) alles.add((zeile));
		return alles;
	}
	public String lesenAllesLN() {
		return Strings.vectorToStringLN(lesenAlles());
	}
	
	public static class MyFileFilter extends FileFilter{
		String[] exts;
		String desc;
		public MyFileFilter(final String[] extensions, final String description){
			exts=extensions;
			desc=description;
		}
		public boolean accept(File arg) {
			if (arg!=null){
				String ext=arg.toString();
				if (ext!=null){
					ext=ext.substring(ext.lastIndexOf('.')+1);
					for (String ex:exts) if (ext.equalsIgnoreCase(ex)) return true;
					if (arg.isDirectory()) return true;
				}
			}
			return false;
		}
		public String getDescription() {
			String extList="*."+exts[0];
			for (int i=1;i<exts.length;i++) extList+=", *."+exts[i];
			return desc+" ("+extList+")";
		}
	}


	public static File fromDialog(MyFileFilter[] ffs) {
		JFileChooser fc=new JFileChooser();

		for (MyFileFilter ff : ffs) {
			fc.addChoosableFileFilter(ff);
		}

		fc.showSaveDialog(null);
		return fc.getSelectedFile();
	}
	public static String level_tabs(int level){
		String r="";
		for (int i=0;i<level;i++){
			r+=("\t");
		}
		return r;
	}

}
