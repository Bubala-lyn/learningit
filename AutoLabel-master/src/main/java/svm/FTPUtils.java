package svm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import system.SystemConf;

/**
 * FTP服务器工具类
 * 
 */
public class FTPUtils {

	final static String URL = "59.78.194.7";
	final static String USER = "root";
	final static String PWD = "1234567891";
	final static int PORT = 21;

	/**
	 * 上传文件至FTP服务器
	 */
	public static boolean storeFile(String fileName) {
		boolean result = false;
		FTPClient ftp = new FTPClient();
		FileInputStream fis = null;
		try {
			// 连接至服务器，端口默认为21时，可直接通过URL连接
			ftp.connect(URL, PORT);
			// 登录服务器
			ftp.login(USER, PWD);
			ftp.enterLocalPassiveMode();
			// 判断返回码是否合法
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				// 不合法时断开连接
				ftp.disconnect();
				// 结束程序
				return result;
			}
			// 设置文件操作目录
			ftp.changeWorkingDirectory(SystemConf.getValueByCode("remotePath"));
			// 设置缓冲区大小
			ftp.setBufferSize(1024);
			// 上传文件
			fis = new FileInputStream(new File(SystemConf.getValueByCode("localPath"), fileName));

			result = ftp.storeFile(fileName, fis);
			// 登出服务器
			ftp.logout();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 判断输入流是否存在
				if (null != fis) {
					// 关闭输入流
					fis.close();
				}
				// 判断连接是否存在
				if (ftp.isConnected()) {
					// 断开连接
					ftp.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 从FTP服务器下载文件至本地
	 */
	public static boolean retrieveFile(String fileName) {
		boolean result = false;
		FTPClient ftp = new FTPClient();
		OutputStream os = null;
		try {
			// 连接至服务器，端口默认为21时，可直接通过URL连接
			ftp.connect(URL, PORT);
			// 登录服务器
			ftp.login(USER, PWD);
			ftp.enterLocalPassiveMode();
			// 判断返回码是否合法
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				// 不合法时断开连接
				ftp.disconnect();
				// 结束程序
				return result;
			}
			// 设置文件操作目录
			ftp.changeWorkingDirectory(SystemConf.getValueByCode("remotePath"));
			// 设置缓冲区大小
			ftp.setBufferSize(1024);
			// 设置字符编码
			ftp.setControlEncoding("UTF-8");
			// 构造本地文件对象
			os = new FileOutputStream(new File(SystemConf.getValueByCode("localPath"), fileName));
			// 下载文件
			result = ftp.retrieveFile(fileName, os);
			// 关闭输出流
			os.close();
			// 登出服务器
			ftp.logout();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 判断输出流是否存在
				if (null != os) {
					// 关闭输出流
					os.close();
				}
				// 判断连接是否存在
				if (ftp.isConnected()) {
					// 断开连接
					ftp.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 从FTP服务器删除文件
	 */
	public static boolean deleteFile(String fileName) {
		boolean result = false;
		FTPClient ftp = new FTPClient();
		try {
			// 连接至服务器，端口默认为21时，可直接通过URL连接
			ftp.connect(URL, PORT);
			// 登录服务器
			ftp.login(USER, PWD);
			// 判断返回码是否合法
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				// 不合法时断开连接
				ftp.disconnect();
				// 结束程序
				return result;
			}
			// 设置文件操作目录
			ftp.changeWorkingDirectory(SystemConf.getValueByCode("remotePath"));
			// 设置缓冲区大小
			ftp.setBufferSize(1024);
			// 设置字符编码
			ftp.setControlEncoding("UTF-8");
			// 获取文件操作目录下所有文件名称
			String[] remoteNames = ftp.listNames();
			// 循环比对文件名称，判断是否含有当前要下载的文件名
			for (String remoteName : remoteNames) {
				if (fileName.equals(remoteName)) {
					result = true;
				}
			}
			// 文件名称比对成功时，进入删除流程
			if (result) {
				// 删除文件
				result = ftp.deleteFile(fileName);
			}
			// 登出服务器
			ftp.logout();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 判断连接是否存在
				if (ftp.isConnected()) {
					// 断开连接
					ftp.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static void main(String[] args) throws FileNotFoundException {
		// storeFile("/usr/local/python/data", "x.txt", "D://x.txt");
		// retrieveFile("/usr/local/python/data", "x.txt", "D://aa");
	}
}
