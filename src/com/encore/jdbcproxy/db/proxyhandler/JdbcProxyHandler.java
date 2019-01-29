package com.encore.jdbcproxy.db.proxyhandler;

//jdbc 사용을 위한 라이브러리
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

//log4j 라이브러리
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//쿼리문 저장한 클래스
import com.encore.jdbcproxy.db.query.oracle.OracleQuery;

import de.simplicit.vjdbc.util.Util;

/* java 어플리케이션과 DB연결을 위한 클래스
 * 
 * DB의 종류는 2가지로 필요에 의해 설정
 * 
 * RD(Real DB) 제품의 데이터를 저장하는 실제 DB
 * PD(Proxy DB) 유저가 원하는 RD에 접속하기 위한 정보를 저장하는 DB -> 대리자 역할 
 * 
 * @version 1 19/01/28
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/
public class JdbcProxyHandler {

	/* 생성자
	 * 
	 */
	public JdbcProxyHandler() {

	}

	/* connection 객체
	 * 
	 */
	private static Connection conn;
	/* DBConnection 클래스의 로그 객체
	 * 
	 */
	private static Logger logger = Logger.getLogger(JdbcProxyHandler.class.getName());
	/* 유저에게 입력받은 정보를 저장할 propertise 객체
	 * 
	 */
	private static Properties prop;
	/* 프리페어스테이먼트 객체
	 * 
	 */
	private static PreparedStatement pre;

	/* 2번 째 DB에 연결시도를 하는 메소드
	 * 
	 * 
	 * @param Connection connInfo 유저의 정보와 접속을 원하는 DB정보를 담은 데이터
	 * @return Connection dbconn DB연결 상태 저장하는 필드 멤버
	 */
	public static void connect() throws ClassNotFoundException, SQLException {
		PropertyConfigurator.configure("log4j.properties"); // log4j.properties lo4j설정 파일 찾는 구문
		logger.info("connect start ");
		
		conn = null;
		pre = null;
		// 접속할 url 설정
		String connectUrl = setRDUrl();// URL 설정 하는 메소드

		// virtualdriver 로딩
		if (Class.forName("de.simplicit.vjdbc.VirtualDriver") == null) {
			throw new ClassNotFoundException();
		}

		// conn 객체 생성
		conn = DriverManager.getConnection(connectUrl, prop);

		if (conn == null) {
			throw new SQLException();
		}
		logger.info(conn.getMetaData() + " ");
		System.out.println(conn.getMetaData());
	}
	
	public static void getQueryToUser(String query) throws SQLException {
		if(conn ==null) {
			throw new SQLException();
		}
		pre = null;
		pre = conn.prepareStatement(query);
		ResultSet rs = pre.executeQuery();

		if(rs == null) {
			System.out.println("No search data.");
			return;
		}
		
		ResultSetMetaData rsmd =rs.getMetaData();
		
		int colCount = rsmd.getColumnCount();
		for(int i = 1; i < colCount; i ++ ) {
			System.out.print(rsmd.getColumnName(i) + " ");
		}
		System.out.println();
		while(rs.next()) {
			for(int i = 1; i < colCount; i ++) {
				
				System.out.print(rs.getString(i) + " ");
			}
			System.out.println();
		}
	}

	/* 유저가 원하는 DB의 접속을 하기 전에 거치는 대리자 역할의 데이터베이스. Proxy Database : PD = AR
	 * 
	 * 
	 * 해당 DB에서 유저가 입력한 정보로 접속을 하고자 하는 DB(RD)의 접속 정보를 조회함.
	 * 
	 * @param HashMap<String, String> userInfo userInterface에서 유저가 입력한 정보를 hashmap으로
	 * 받음
	 */
	public static void connectPD(HashMap<String, String> userInfo) throws Exception {
		logger.info("connectPD ");
		setUserProp(userInfo);								// 유저의 접속 정보를 property에 저장 하는 메소드
		 // 유저가 접속하는 PD(AR)서버의 주소 설정 proxy IP : 유저가 접속 할 PD(AR)서버의 아이피 주소 
		String urlPD = "jdbc:oracle:thin:@" + prop.getProperty("proxyIp");// 192.168.1.32:1521:orcl"; 
		
		prop.setProperty("proxyIp", "localhost"); 			//사용을 다한(위의 접속 url) 프록시 ip값을 localhost로 변환
		
		String userIdPD = prop.getProperty("user");			// PD의 유저 아이디
		String userPwPD = prop.getProperty("password");		// PD의 유저 비밃번호
		
		//resion 사용 변수들 null로 초기화
		String sql = null;									// 쿼리문을 저장할 스트링 객체
		conn = null; 										// PD의 커넥션 객체
		pre = null;											// PD의 프리페어스테이먼트
		ResultSet rsPD = null;								// 스트링 sql의 쿼리 결과문
		//end
		
		//resion 오라클 드라이버 로딩 및 예외처리
		logger.info("Search oracle driver ");
		if (Class.forName("oracle.jdbc.driver.OracleDriver") == null) {
			throw new ClassNotFoundException();
		}
		//end
		
		//resion 유저의 입력 정보를 이용하여 PD(AR)에 접속하는 커넥션 객체 할당 예외처리
		logger.info("make connection instance ");
		conn = DriverManager.getConnection(urlPD, userIdPD, userPwPD);
		if (conn == null) {
			throw new SQLException();
		}
		//end
		
		userPwPD = null; //PD 로그인으로 사용한 비밀번호 초기화 -> PD에서 RD 비밀번호 가져와서 디코딩하여 다시 저장
		
		//resion svr_id를 조회 
		sql = OracleQuery.getSvr_id();
		// SVR_ID 가져오기
		pre = conn.prepareStatement(sql);
		pre.setString(1, prop.getProperty("serverName"));
		rsPD = pre.executeQuery();
		// 예외 처리
		if (rsPD == null) {
			throw new SQLException();
		}
		// 쿼리문 조회 결과 프로퍼티에 저장
		while (rsPD.next()) {
			// svrID저장
			prop.setProperty("svrId", rsPD.getString(1));
			logger.info("SVR ID: " + prop.getProperty("svrId") + " ");
		}
		//end
		
		//resion password와 registry port 가져오기
		sql = OracleQuery.getPort_pw();
		pre = conn.prepareStatement(sql);
		pre.setString(1, prop.getProperty("proxyIp"));
		pre.setString(2, prop.getProperty("proxyNm"));
		pre.setString(3, prop.getProperty("schemaId"));
		pre.setString(4, prop.getProperty("user"));
		pre.setString(5, prop.getProperty("svrId"));
		//예외처리
		rsPD = pre.executeQuery();
		if(rsPD==null) {
			throw new SQLException();
		}
		while(rsPD.next()) {
			prop.setProperty("proxyPort", rsPD.getString(1));
			prop.setProperty("password", Util.AES_Decode(rsPD.getString(2))); // 암호화된 비밀번호를 디코딩하여 저장
			
			logger.info("Registry Port: " + rsPD.getString(1) + " ");
			logger.info("Password: " + rsPD.getString(2)); //보안상 디코드 되지 않은 비밀번호 저장
		}
		//end
		
		// resion 사용하지 않는 코드
		//		//// Registry Port 가져오는 쿼리
		//		sql = OracleQuery.getPort();
		//		// Registry Port 가져오기
		//		pre = conn.prepareStatement(sql);
		//		pre.setString(1, prop.getProperty("proxyIp")); // localhost가 아닐시 해당 값을 변경해주어야함.
		//		pre.setString(2, prop.getProperty("proxyNm"));
		//		rsPD = pre.executeQuery();
		//		// sql 조회 결과 예외 처리
		//		if (rsPD == null) {
		//			throw new SQLException();
		//		}
		//		// 쿼리문 조회 결과 프로퍼티에 저장
		//		while (rsPD.next()) {
		//			// proxyPort 저장
		//			prop.setProperty("proxyPort", rsPD.getString(1));
		//			logger.info("Registry Port: " + prop.getProperty("proxyPort") + " ");
		//		}
		//
		//		// 암호화된 비밀번호  가져와서 디코딩 하기
		//		sql = OracleQuery.getUser_pw();
		//		//암호화된 비밀번호 가져오기
		//		pre = conn.prepareStatement(sql);
		//		pre.setString(1, prop.getProperty("svrId"));
		//		pre.setString(2, prop.getProperty("schemaId"));
		//		pre.setString(3, prop.getProperty("user"));
		//		rsPD = pre.executeQuery();
		//		// sql 조회 결과 예외 처리
		//		if (rsPD == null) {
		//			throw new SQLException();
		//		}
		//		while (rsPD.next()) {
		//			prop.setProperty("password", Util.AES_Decode((rsPD.getString(1))));
		//		}
		// end
		conn = null;
	}
	

	// property 접속 정보 디비 설정 메소드
	private static void setUserProp(HashMap<String, String> userInfo) {
		prop = new Properties();
		prop.setProperty("proxyIp", userInfo.get("proxyIp"));
		prop.setProperty("proxyNm", userInfo.get("objectName"));
		prop.setProperty("serverName", userInfo.get("serverName"));
		prop.setProperty("user", userInfo.get("userID")); // .toUpperCase()); //아이디는 대문자로 받아야함.
		prop.setProperty("password", userInfo.get("userPW"));
		prop.setProperty("datawareUserId", userInfo.get("datawareUserId")); // "admin"
		prop.setProperty("schemaId", userInfo.get("schemaId")); // SQLSHARP_110
	}

	// url 유저가 접속을 원하는 RD의 URL 설정 메소드
	private static String setRDUrl() {
		String connectUrl = String.format("jdbc:vjdbc:rmi://%s:%s/%s,%s", prop.getProperty("proxyIp"), // localhost 고정
				prop.getProperty("proxyPort"), // PD에서(AR) 유저가 입력한 정보
				prop.getProperty("proxyNm"), // 유저 입력 정보
				prop.getProperty("svrId")); // PD에서(AR) 유저가 입력한 정보
		return connectUrl;
	}
}
