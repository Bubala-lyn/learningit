package test;

import java.util.Properties;

import org.python.util.PythonInterpreter;

public class jythonTest {

	public static void main(String[] args) {
		// System.setProperty("python.home", "D:\\software\\python");
		// System.setProperty("python.console.encoding", "utf-8");
		// System.setProperty("python.import.site", "false");
		// PythonInterpreter interp = new PythonInterpreter();
		// interp.exec("from sklearn import svm");
		// interp.exec("x=[[2.,0.,1.],[1.,1.,2.],[2.,3.,3.]]");
		// interp.exec("y=[110.,110.,130.]");
		// interp.exec("clf=svm.SVC()");
		// interp.exec("clf.fit(x,y)");
		// interp.exec("z=clf.predict([[2.,0.,3.]])");
		// PyObject x = interp.get("z");
		// int[] res = (int[]) x.__tojava__(Object.class);
		// interp.close();
		// System.out.println("z: " + res.toString());

		Properties props = new Properties();
		props.put("python.console.encoding", "UTF-8");
		props.put("python.security.respectJavaAccessibility", "false"); 
		props.put("python.import.site", "false");
		Properties preprops = System.getProperties();
		PythonInterpreter.initialize(preprops, props, new String[0]);
		PythonInterpreter interp = new PythonInterpreter();
		interp.exec("import sys");
		interp.exec("sys.path.append('D:/Anaconda3/lib')");// jython自己的
		interp.exec("sys.path.append('D:/Anaconda3/lib/site-packages')");// jython自己的
		interp.exec("sys.path.append('D:/python/svm/')");// 我们自己写的
		interp.execfile("D:\\python\\svm\\MySvm.py");
		System.out.println(interp);
	}
}
