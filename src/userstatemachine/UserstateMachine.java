package userstatemachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

/* 유저의 접속 정보를 입력받는 클래스
 * 
 * @version 1 19/01/24
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/

//사용자 입력을 받기위한 라이브러리
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.encore.jdbcproxy.db.proxyhandler.JdbcProxyHandler;

/* 유저의 입력정보를 받는 상태머신 클래스
 * 
 * 유저 정보를 순차적으로 받기위해  
 * 상태머신을 이용함.
 * 
 * @version 1 19/01/28 메소드 통합
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/
public class UserstateMachine {
	public UserstateMachine() {
		
	}
	// 상태머신 제어 변수
	private int userState;
	// 로그 객체
	private Logger logger = Logger.getLogger(JdbcProxyHandler.class.getName());

	/*
	 * 유저의 입력 정보에 따른 상태머신 메소드
	 */
	public void stateMachine() throws IOException {
		// 사용자 입력을 받을 버퍼 객체 생성
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		HashMap<String, String> userInfoHash = new HashMap<>();
		userState = 0;

		while (userState != 8) {
			try {
				switch (userState) {
				// Proxy IP 입력
				case 0:
					getUserInfo(userInfoHash, buffer, "proxyIp", 0);
					break;
				// object name 입력
				case 1:
					getUserInfo(userInfoHash, buffer, "objectName", 1);
					break;
				// server name 입력
				case 2:
					getUserInfo(userInfoHash, buffer, "serverName", 2);
					break;
				// 유저 아이디
				case 3:
					getUserInfo(userInfoHash, buffer, "userID", 3);
					break;
				// 유저 비밀번호
				case 4:
					getUserInfo(userInfoHash, buffer, "userPW", 4);
					break;
				//데이타웨어 유저 아이디
				case 5:
					getUserInfo(userInfoHash, buffer, "datawareUserId", 5);
					break;
				//스키마 아이디
				case 6:
					getUserInfo(userInfoHash, buffer, "schemaId", 6);
					break;
				//커넥션 객체 생성 
				case 7:
					logger.info("Start connect to PD");
					//PD(AR)서버로 접속하여 RD(2번 째 데이터 베이스)의 접속 정보 조회
					JdbcProxyHandler.connectPD(userInfoHash);
					//RD(두번 째 데이터 베이스)접속 하여 정보 조회
					JdbcProxyHandler.connect();
					userState = 9;
					break;
				//case 8 종료
				//RD로 쿼리문 보내기
				case 9 : 
					logger.info("RD connection success.");
					System.out.println("You can query to DB!");
					System.out.println("==========================================");
					System.out.println("Insert your query : ");
					String query = buffer.readLine();
					while(true) {
	
						isCommand(query);
						if(userState ==0 || userState ==8) {
							break;
						}
						else if(query.charAt(query.length()-1)==';') {
							//쿼리문 보내기
							query = query.replace(";","");
							System.out.println(query);
							JdbcProxyHandler.getQueryToUser(query);
							query = "";
							System.out.println("==========================================");
							System.out.println("Insert your query : ");
							query = buffer.readLine();
						}else {
							//쿼리문 연결하기 
							query += " " + buffer.readLine();
						}
					}
				}
			  //유저가 입력한 정보가 잘못되거나 조회한 내용이 없을 때
			} catch (SQLException e) {
				System.out.println("");
				System.out.println("==========================================");
				System.out.println("SQL Exception occured. Do you wnat see error log?\nyes : y (exit)\nno : anykey (restart)");
				System.out.println("==========================================");
				if (buffer.readLine().equals("y")) {
					e.printStackTrace();
					break;
				}
				userState = 0;
			  //오라클 드라이버를 찾지 못했을 때
			} catch (ClassNotFoundException e) {
				userState = 8;
				e.printStackTrace();
			} catch (Exception e) {
				userState = 8;
				e.printStackTrace();
			}
		}
	}

	/*
	 * 유저의 입력 정보를 받는 메소드
	 * 
	 * @param HashMap<String, String> userInfoHash 유저의 입력정보를 담을 해쉬맵 객체
	 * 
	 * @param BufferedReader buffer 유저의 입력을 받을 버퍼드 리더 객체
	 * 
	 * @param String userInfo 입력 받을 유저의 정보 ("proxyIp", "objectName", "serverName",
	 * "userID", "userPW","datawareUserId","schemaId" ) 중에 하나를 입력 받는다.
	 * 
	 * @param int userStareNum 현재 상태머신의 상태
	 */
	private void getUserInfo(HashMap<String, String> userInfoHash, BufferedReader buffer, String userInfo,
			int userStareNum) throws IOException {
		// 첫 실행시 커맨드 정보 메소드 출력
		if (userStareNum == 0) {
			// 커맨드 정보 출력 메소드
			printCommand();
		}
		System.out.println("__________________________________________");
		System.out.println(String.format("%s : ", userInfo));

		// 데이터 입력                입력정보 문자열, 사용자 입력
		userInfoHash.put(userInfo, buffer.readLine());

		// 유저에게 입력받은 값이 ""이면
		if (userInfoHash.get(userInfo).equals("")) {
			logger.debug(String.format("%s %s", userInfo, "is empty."));
			userInfoHash.remove(userInfo);
			// 현재 상태로 전이
			userState = userStareNum;
			return;
		// 입력 받은 값이 null 이면 예외 발생
		} else if (userInfoHash.get(userInfo).isEmpty()) {
			throw new IOException();
		}
		// 사용자가 입력한 값이 명령어 인지 확인하는 메소드 실행
		isCommand(userInfoHash.get(userInfo));
		logger.info(userInfo + userInfoHash.get(userInfo));
	}

	// 단순 커맨드 정보를 출력하는 메소드
	private void printCommand() {
		System.out.println("==========================================");
		System.out.println("           Command Information");
		System.out.println("__________________________________________");
		System.out.println("  When you want restart write \"restart\".");
		System.out.println("     When you want quit write \"exit\".");
		System.out.println("==========================================");
	}

	/*
	 * 사용자가 입력한 값이 커맨드인지 확인 및 상태 전이 메소드
	 * 
	 * @param userInput 사용자가 입력한 값
	 */
	private void isCommand(String userInput) {
		if (userInput.equals("restart")) {
			System.out.println("==========================================");
			System.out.println("           Restart JDBC Proxy");
			System.out.println("==========================================");
			userState = 0;
			return;
		} else if (userInput.equals("exit")) {
			System.out.println("==========================================");
			System.out.println("                  exit");
			System.out.println("==========================================");
			userState = 8;
			return;
		} else {
			userState++;
			return;
		}
	}
}
