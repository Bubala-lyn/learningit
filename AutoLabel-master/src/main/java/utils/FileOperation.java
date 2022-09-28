package utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileOperation {
	private String character;
	public FileOperation() {
		character = "UTF-8";
	}
	public FileOperation(String character) {
		this.character=character;
	}
	public String[] read(String path) {
		StringBuffer sBuffer = new StringBuffer();
		try {
			BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(path),character));
			String line;
			
			while ((line = bReader.readLine()) != null) {
				sBuffer.append(line+"\n");
			}
			bReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sBuffer.toString().split("\n");
	}
	public void write(String path,String word) {
		try {
			checkExists(path, true);
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(new File(path)));
			bWriter.write(word);
			bWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void copyFile2Resources(String source, String destFile) {
		String dest = new File("").getAbsolutePath() + "/src/main/resources/" + destFile;
		InputStream input = null;
		OutputStream output = null;
		
		try {
			checkExists(dest, true);
			input = new FileInputStream(new File(source));
			output = new FileOutputStream(new File(dest));
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
			input.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static void checkExists(String path, boolean autoCreate) throws IOException {
		File file = new File(path);
		if (file.exists())
			file.delete();
		if (autoCreate)
			file.createNewFile();
	}
}
