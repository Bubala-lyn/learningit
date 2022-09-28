package dao;

import system.SystemConf;

public abstract class DbFactory {

	public final static DbChannel getDbChannel() {
		try {
			return (DbChannel) Class.forName(SystemConf.getValueByCode("system.dao")).newInstance();
		} catch (Exception e) {
			System.out.println("加载DbChannel失败");
		}
		return null;
	}

	public final static DbChannel getDbChannel(String url) {
		try {
			// 有参数构造函数
			return (DbChannel) Class.forName(SystemConf.getValueByCode("system.dao")).getConstructor(String.class).newInstance(url);
		} catch (Exception e) {
			System.out.println("加载DbChannel失败");
		}
		return null;
	}

	public static void main(String[] args) {
		SystemConf.loadSystemParams("autolabel.properties");
		DbChannel db = getDbChannel();
		System.out.println(db);
	}
}