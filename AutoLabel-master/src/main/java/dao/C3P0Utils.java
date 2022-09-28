package dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0Utils {

	// 定义全局变量
	private static ComboPooledDataSource cpds;
	// 静态代码块，命名配置
	static {
		cpds = new ComboPooledDataSource("test");
	}

	// 获取数据源
	public static DataSource getDataSource() {
		return cpds;
	}

	// 获取连接
	public static Connection getConnection() throws SQLException {
		return cpds.getConnection();
	}

}
