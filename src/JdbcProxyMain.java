import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.simplicit.vjdbc.util.Util;
import userstatemachine.UserstateMachine;

/*
 * MAIN 함수
 * 
 * @version 1 19/01/28
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/
public class JdbcProxyMain {

	static Logger mainLogger = Logger.getLogger(JdbcProxyMain.class.getName());
	
	public static void main(String[] args) {
		//log4j properties 파일 불러오기
		PropertyConfigurator.configure("log4j.properties"); 
		mainLogger.info("JDBC Proxy start");
		
		//유저의 정보를 입력받고 처리하는 상태머신 메소드
		UserstateMachine userInterface = new UserstateMachine();
		try {
			userInterface.stateMachine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mainLogger.info("Exiting application.");
	}

}
