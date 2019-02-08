import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import userstatemachine.UserstateMachine;

/*
 * MAIN 
 * 
 * @version 2 19/02/01
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/
public class JdbcProxyMain {
	public static void main(String[] args) {
		//현재 실행중인 디렉토리 동적으로 받기. 
		File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
		//로그 파일을 저장할 절대 경로 설정
		String logPath = jarDir.getAbsolutePath();
		//log4j 프로퍼티 파일 경로 설정
		System.setProperty("test", logPath);

		PropertyConfigurator.configure(JdbcProxyMain.class.getResourceAsStream("log4j.properties")); 
		//text파일 로그 
		Logger logger = Logger.getLogger("A");
		//콘솔 출력 로그
		Logger logger2 = Logger.getLogger("pack");
		logger.info("JDBC Proxy start");
		logger2.info("JDBC Proxy start");
		
		//유저의 입력을 단계별로 받기위한 상태머신 클래스
		UserstateMachine userstateMachine = new UserstateMachine();
		try {
			//상태머신 실행
			userstateMachine.stateMachine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("Exiting application.");
		logger2.info("Exiting application.");
	}
}
