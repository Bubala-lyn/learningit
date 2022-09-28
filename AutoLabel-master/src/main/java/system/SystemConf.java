package system;

import java.util.Properties;

import utils.CommonUtils;

public class SystemConf{
	private static Properties pro = new Properties();
	private static boolean loaded = false;

	public static void loadSystemParams(String fileName) {
		// 自动读取配置
		ClassLoader classLoader = SystemConf.class.getClassLoader();
		try {
			pro.load(classLoader.getResourceAsStream(fileName));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("自动标注配置文件加载失败");
		}
		CommonUtils.loadCache();// 资源预加载
		
		loaded = true;
	}

	public static boolean hasLoaded(){return loaded;}
	
	public static String getValueByCode(String code) {
		return pro.getProperty(code);
	}
}
