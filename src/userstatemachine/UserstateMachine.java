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
 * @version 2 19/02/01 메소드 통합
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/
public class UserstateMachine {
	public UserstateMachine() {
		
	}
	// 상태머신 제어 변수
	private int userState;
	// 로그 객체
	private static Logger logger = Logger.getLogger("A");
	private static Logger logger2 = Logger.getLogger("pack");

	// 유저의 입력 정보를 단계별로 받도록 하는 상태머신 메소드
	public void stateMachine() throws IOException {
		// 사용자 입력을 받을 버퍼 객체 생성
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		HashMap<String, String> userInfoHash = new HashMap<>();
		userState = 0;
		//유저의 상태가 100이 아니면 실행
		while (userState != 100) {
			try {
				switch (userState) {
				////======================================AR============================================
				//======================================================================================
				// AR의 url 입력
				case 0:
					getUserInfo(userInfoHash, buffer, "url", userState);
					break;
				// AR 유저 아이디
				case 1:
					getUserInfo(userInfoHash, buffer, "AR_ID", userState);
					break;
				// AR 유저 비밀번호
				case 2:
					getUserInfo(userInfoHash, buffer, "AR_PW", userState);
					break;
				//======================================================================================
				////======================================DB============================================
				// server name 입력
				case 3:
					getUserInfo(userInfoHash, buffer, "serverName", userState);
					break;
				//스키마 아이디
				case 4:
					getUserInfo(userInfoHash, buffer, "schemaId", userState);
					break;
				//DB 접속 아이디
				case 5:
					getUserInfo(userInfoHash, buffer, "connUser", userState);
					break;
				//======================================================================================
				////====================================기타 설정정보=======================================
				// proxy name 입력(ojectname)
				case 6:
					getUserInfo(userInfoHash, buffer, "proxyNm", userState);
					break;
				//데이터웨어 유저 아이디
				case 7:
					getUserInfo(userInfoHash, buffer, "datawareUserId", userState);
					System.out.println("dataware id end");
					break;
				//======================================================================================
					
				//커넥션 객체 생성 
				case 8:
					logger.info("Start connect to AR");
					logger2.info("Start connect to AR");
					System.out.println("==========================================");
					System.out.println("            start connect ar");
					System.out.println("==========================================");
					//AR로 접속하여 DB(2번 째 데이터 베이스)의 접속 정보 조회
					JdbcProxyHandler.connectAR(userInfoHash);
					System.out.println("            connect ar end");
					System.out.println("==========================================");
					//DB(두번 째 데이터 베이스)접속 하여 정보 조회
					System.out.println("            start connect to db");
					System.out.println("==========================================");
					JdbcProxyHandler.connect();
					System.out.println("            connect db end");
					System.out.println("==========================================");
					//위 2가지 메소드로는 상태 제어가 되지 않기 때문에 직접 다음 단계로 전이
					userState =9;
					break;
				//접속에 성공 했을 시 DB로 쿼리문 보내기
				case 9 : 
					//유저에게 쿼리문을 받아 DB로 쿼리문을 보내는 메소드
					getQuery(buffer);
				}
			  //유저가 입력한 정보가 잘못되거나 조회한 내용이 없을 때
			} catch (SQLException e) {
				System.out.println("");
				System.out.println("==========================================");
				System.out.println("SQL Exception occured. Do you wnat see error log?\nyes : y (exit)\nno : anykey (restart)");
				System.out.println("==========================================");
				//소문자 y를 입력 시 에로로그를 출력하고 프로그램 종료
				if (buffer.readLine().equals("y")) { 
					e.printStackTrace();
					break;
				}
				//재시작
				if(userState != 9) {
					userState = 0;
				}
			  //오라클 드라이버를 찾지 못했을 때
			} catch (ClassNotFoundException e) {
				//종료
				userState = 100;
				e.printStackTrace();
			} catch (Exception e) {
				//종료
				userState = 100;
				e.printStackTrace();
			}
		}
	}
/*resion 쿼리문 입력받는 메소드
 * 유저에게 쿼리문을 입력받아 
 * 원하는 테이블을 조회 하는 메소드
 * 
 * 확인된 사항
 * select * from MS_CODE; 
 * 처럼 MS_CODE, SQL_PROXY 테이블도 조회됨.
 * 
 * 확인 해야 할 사항
 * INSERT, UPDATE, DELETE가 정상 작동 하는지 확인해야 함.
 * 
 * @param BufferedReader buffer 유저에게 입력을 받기위한 buffer객체 인자
*/
	private void getQuery(BufferedReader buffer) throws IOException, SQLException {
		logger.info("AR connection success.");
		logger2.info("AR connection success.");
		System.out.println("          You can query to DB.");
		System.out.println("==========================================");
		System.out.println("Query : ");
		//사용자에게 쿼리문 입력받기
		String query = buffer.readLine();
		
		//값을 입력하지 않았으면 다시 입력받음
		if (query.equals("")) {		
			System.out.println("          Please write your query");
			logger.info("Query is \"\"");
			logger2.info("Query is \"\"");
			userState = 9;
			return;
		//예외처리
		}else if(query.isEmpty()) {
			logger.error("query is null.");
			logger2.error("query is null.");
			throw new IOException();
		}
		//쿼리문이 정상 작동 했다면, 유저에게 계속 쿼리문을 받을 수 있도록 반복함.
		while(true) {
			isCommand(query); //restart = 0 , exit = 100
			if(userState == 0 || userState ==100) {
				//conn 객체 제거
				JdbcProxyHandler.connectionClose();
				break;
			}else if (userState == 10) {// 둘다 아니면 값을 증가시킴. 따라서 9에서 10이 되기 때문에 9로 초기화
				userState = 9;
			}
			
			if(query.charAt(query.length()-1)==';') { //쿼리문에 ;가 있는지 없는지 검사
				//;있으면 쿼리문이 완성됬다 관주하여 쿼리문 전달
				query = query.replace(";",""); //preparestatedment에 넣을 문자열 이기에 ;는 제거해야한다.
				logger.info(query);
				logger2.info(query);
				JdbcProxyHandler.getQueryToUser(query); //쿼리문을 받아 조회하는 역할을 함.
				query = "";
				//쿼리문이 정상 작동 했다면 바로 다음 쿼리문을 받음.
				System.out.println("=========================================="); 
				System.out.println("Insert your query : ");
				query = buffer.readLine();
			}else { //; 없으면 쿼리문을 계속 입력받음
				//쿼리문 연결하기 
				query += " " + buffer.readLine();
			}
		}
	}
	
	/*
	 * 유저의 입력 정보를 받는 메소드
	 * 
	 * @param HashMap<String, String> userInfoHash 유저의 입력정보를 담을 해쉬맵 객체
	 * @param BufferedReader buffer 유저의 입력을 받을 버퍼드 리더 객체
	 * @param String userInfo 입력 받을 유저의 정보 
	 * (url, AR_ID, AR_PW, serverName, schemaId, connUser, proxyNm, datawareUserId) 중에 하나를 입력 받는다.
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
		// 사용자가 입력한 값이 명령어 인지 확인하는 메소드 실행
		isCommand(userInfoHash.get(userInfo));
		logger.info(userInfo + userInfoHash.get(userInfo));
		logger2.info(userInfo + userInfoHash.get(userInfo));
		
		// 유저에게 입력받은 값이 ""이면
		if (userInfoHash.get(userInfo).equals("")) {
			logger.debug(String.format("%s %s", userInfo, "is empty."));
			logger2.debug(String.format("%s %s", userInfo, "is empty."));
			userInfoHash.remove(userInfo);
			// 현재 상태로 전이
			userState = userStareNum;
			return;
		// 입력 받은 값이 null 이면 예외 발생
		} else if (userInfoHash.get(userInfo).isEmpty()) {
			throw new IOException();
		}
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
			userState = 100;
			return;
		} else {
			//명령어가 아닐시 다음 상태로 전이
			userState++;
			return;
		}
	}
}
