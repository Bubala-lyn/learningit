package others;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyClassLoader extends ClassLoader {

	private String classPath;

	public MyClassLoader(String classPath) {
		this.classPath = classPath;
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if ("".startsWith(name)) {
			byte[] classData = getData(name);
			if (classData == null)
				throw new ClassNotFoundException();
			else
				return defineClass(name, classData, 0, classData.length);
		} else
			return super.loadClass(name);
	}

	private byte[] getData(String className) {
		String path = classPath + File.pathSeparatorChar + className.replace('.', File.separatorChar) + ".class";
		try {
			InputStream in = new FileInputStream(path);
			ByteArrayOutputStream st = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int num = 0;
			while ((num = in.read(buffer)) != -1) {
				st.write(buffer, 0, num);
			}
			in.close();
			return st.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
