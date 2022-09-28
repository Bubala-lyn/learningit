package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class DicReader {

	public static InputStream getInputStream(String name) {
		InputStream in = DicReader.class.getResourceAsStream(name);
		return in;
	}

	public static InputStream getInputStreamByAbsolutePath(String name) {
		InputStream in = null;
		try {
			in = new FileInputStream(new File(name));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return in;
	}
}
