import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import userstatemachine.UserstateMachine;

/*
 * MAIN �Լ�
 * 
 * @version 1 19/01/28
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/
public class JdbcProxyMain {
	public static void main(String[] args) {
		
		File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		String logPath = jarDir.getAbsolutePath();
		System.out.println(logPath);
		System.setProperty("test", logPath);

		PropertyConfigurator.configure(JdbcProxyMain.class.getResourceAsStream("log4j.properties")); 
		Logger mainLogger = Logger.getLogger("prac");
		mainLogger.info("JDBC Proxy start");
		
		//������ ������ �Է¹ް� ó���ϴ� ���¸ӽ� �޼ҵ�
		UserstateMachine userInterface = new UserstateMachine();
		try {
			userInterface.stateMachine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mainLogger.info("Exiting application.");
	}

}
