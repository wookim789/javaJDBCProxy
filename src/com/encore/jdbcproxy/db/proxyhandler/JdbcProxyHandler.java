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

//쿼리문 저장한 클래스
import com.encore.jdbcproxy.db.query.oracle.OracleQuery;

import de.simplicit.vjdbc.VirtualDriver;
import de.simplicit.vjdbc.util.Util;

/* java 어플리케이션과 DB연결을 위한 클래스
 * 
 * DB 유저가 접속을 원하는  DB
 * AR 위의 DB를 접속하기 위한 정보를 저장하는 디비 
 * 
 * @version 2 19/02/01
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/
public class JdbcProxyHandler {

	// 생성자
	public JdbcProxyHandler() {

	}

	// connection 객체
	private static Connection conn;
	// 유저에게 입력받은 정보를 저장할 propertise 객체
	private static Properties prop;
	// 프리페어스테이먼트 객체
	private static PreparedStatement pre;
	
	//로그 객체
	private static Logger logger = Logger.getLogger("A");
	private static Logger logger2 = Logger.getLogger("pack");

	// DB에 연결시도를 하는 메소드
	public static void connect() throws ClassNotFoundException, SQLException {
		logger.info("connect start ");
		logger2.info("connect start ");

		// 접속할 url 설정
		String connectUrl = setDBUrl();// URL 설정 하는 메소드
		//가상 드라이버 찾기
		try {
			DriverManager.registerDriver(new VirtualDriver());	
		}catch (SQLException e) {
			throw new SQLException("Can't find VirtualDriver.");
		}

		try {
			//conn DB연결 시도
			conn = null;
			conn = DriverManager.getConnection(connectUrl, prop);
		}catch (SQLException e) {
			throw new SQLException("Fail to connect DB. Can't make connection object. Check your DB longin Information: *datawareUserId*, serverName, schemaId, connUser, proxyNm   ");
		}
		
		logger.info(conn.getMetaData() + " ");
		logger.info(conn.getMetaData().getDriverName());
		logger.info(conn.getMetaData().getDriverVersion());
		
		logger2.info(conn.getMetaData() + " ");
		logger2.info(conn.getMetaData().getDriverName());
		logger2.info(conn.getMetaData().getDriverVersion());
	}
	/* 유저로 부터 쿼리문을 받아 db에 쿼리문을 보내는 메소드
	 * 
	 * @param String query 유저에게 받은 쿼리문 문자열 객체
	 * */
	public static void getQueryToUser(String query) throws SQLException {
		//conn 객체는 위 connect()메소드에서 생성된 객체가 유지되어야 함.
		if(conn == null) {
			throw new SQLException();
		}
		pre = null;
		pre = conn.prepareStatement(query);
		ResultSet rs = pre.executeQuery();
		//조회 결과가  없으면 리턴
		if(!rs.isBeforeFirst()) {
			System.out.println("------------------------------------------");
			System.out.println("             No search data.");
			System.out.println("------------------------------------------");
			logger.info("no search data");
			logger2.info("no search data");
			return;
		}
		//조회 결과를 출력하기 위해 메타 정보 객체를 생성
		ResultSetMetaData rsmd = rs.getMetaData();
		//열의 수
		int colCount = rsmd.getColumnCount();
		//열의 이름들 출력
		for(int i = 1; i < colCount; i ++ ) {
			System.out.print(rsmd.getColumnName(i) + " ");
		}
		System.out.println();
		//데이터 출력
		while(rs.next()) {
			for(int i = 1; i < colCount; i ++) {
				System.out.print(rs.getString(i) + "  ");
			}
			System.out.println();
		}
	}
	//DB에 접속한 이후 유저가 접속을 끊거나 새로운 접속을 시도하면 conn 객체 제거
	public static void connectionClose() throws SQLException {
		logger.info("connection close");
		logger2.info("connection close");
		pre.close();
		conn.close();
	}
	/* AR 유저가 원하는 DB의 접속을 하기 전에 거치는 대리자 역할의 데이터베이스. AR
	 * 
	 * 
	 * AR에서 유저가 입력한 정보로 접속을 하고자 하는 DB의 정보를 조회함.
	 * 
	 * @param HashMap<String, String> userInfo userInterface에서 유저가 입력한 정보를 hashmap으로 받음
	 */
	public static void connectAR(HashMap<String, String> userInfo) throws SQLException, ClassNotFoundException, Exception {
		logger.info("connect AR ");
		logger2.info("connect AR ");
		// 유저의 접속 정보를 property에 저장 하는 메소드
		setUserProp(userInfo);	
		 // 유저가 접속하는 AR서버의 주소 설정 
		String urlAR = "jdbc:oracle:thin:@" + prop.getProperty("url");// ex) 192.168.1.32:1521:orcl"; 
		//프록시 아이피 설정 ->localhost로 고정
		prop.setProperty("proxyIp", "localhost"); 		
		
		try{
			//resion 오라클 드라이버 로딩 및 예외처리
			logger.info("Search oracle driver ");
			logger2.info("Search oracle driver ");
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}catch (ClassNotFoundException e) {
			throw new ClassNotFoundException("Can't find AR Oracle Driver.");
		}
		//end
				
		try {
			//resion 유저의 입력 정보를 이용하여 AR에 접속하는 커넥션 객체 할당 및 예외처리
			logger.info("make connection instance ");
			logger2.info("make connection instance ");
			
			conn = null;                       //url, id, pw
			conn = DriverManager.getConnection(urlAR, prop.getProperty("AR_ID"), prop.getProperty("AR_PW"));	
		}catch (SQLException e) {
			throw new SQLException("Can't login to AR. Check your login information : url, AR_ID, AR_PW");
		}
		//end	
		
		//resion svr_id를 조회 
		String sql = OracleQuery.getSvr_id();
		// SVR_ID 가져오기
		pre = null;
		pre = conn.prepareStatement(sql);
		pre.setString(1, prop.getProperty("serverName"));
     	ResultSet rsAR = pre.executeQuery();
     	
		// 예외 처리
		if(!rsAR.isBeforeFirst()) {
			throw new SQLException("Check your server name.");
		}
		
		// 쿼리문 조회 결과 프로퍼티에 저장
		while (rsAR.next()) {
			// svrID저장
			prop.setProperty("svrId", rsAR.getString(1));
			logger.info("SVR ID: " + prop.getProperty("svrId") + " ");
			logger2.info("SVR ID: " + prop.getProperty("svrId") + " ");
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

		rsAR = pre.executeQuery();
		//예외처리
		if(!rsAR.isBeforeFirst()) {
			throw new SQLException("Check your login information. proxyNm, schemaId, connUser");
		}
		
		while(rsAR.next()) {
			prop.setProperty("proxyPort", rsAR.getString(1));
			prop.setProperty("password", Util.AES_Decode(rsAR.getString(2))); // 암호화된 비밀번호를 디코딩하여 저장
			logger.info("Registry Port: " + rsAR.getString(1) + " ");
			logger.info("Password: " + rsAR.getString(2)); //보안상 디코드 되지 않은 비밀번호 저장
			logger2.info("Registry Port: " + rsAR.getString(1) + " ");
			logger2.info("Password: " + rsAR.getString(2)); //보안상 디코드 되지 않은 비밀번호 저장
		}
		
		//이제 더이상 필요없는 속성은 삭제한다.
		prop.remove("url");
		prop.remove("AR_PW");
		prop.remove("AR_ID");
		prop.remove("serverName");
		//AR 사용을 마친 CONN 객체는 반납한다. -> 주의 DB CONN객체는 쿼리문을 사용하기위해 유지해야함.
		conn.close();
		pre.close();
	}
	
	// property 접속 정보 디비 설정 메소드
	private static void setUserProp(HashMap<String, String> userInfo) {
		prop = new Properties();
		
		prop.setProperty("url", userInfo.get("url"));//AR의 ip DB의 접속정보를 가져오면 해당 속성은 삭제한다.
		prop.setProperty("AR_ID", userInfo.get("AR_ID")); 
		prop.setProperty("AR_PW", userInfo.get("AR_PW"));//AR의 pw DB의 접속정보를 가져오면 해당 속성은 삭제한다.
	
		prop.setProperty("serverName", userInfo.get("serverName"));
		prop.setProperty("schemaId", userInfo.get("schemaId")); //ex) SQLSHARP_110
		prop.setProperty("user", userInfo.get("connUser"));
		
		prop.setProperty("proxyNm", userInfo.get("proxyNm"));
		prop.setProperty("datawareUserId", userInfo.get("datawareUserId")); // "admin"
	}

	//유저가 접속을 원하는 DB의 URL 설정 메소드
	private static String setDBUrl() {
		String connectUrl = String.format("jdbc:vjdbc:rmi://%s:%s/%s,%s",
											prop.getProperty("proxyIp"), // localhost 고정
											prop.getProperty("proxyPort"), // AR에서 유저가 입력한 정보  ex) 2011
											prop.getProperty("proxyNm"), // 유저 입력 정보 (objectName) HYUK
											prop.getProperty("svrId")); // AR에서 조회한 값 ex) 0001
		return connectUrl;
	}
}

