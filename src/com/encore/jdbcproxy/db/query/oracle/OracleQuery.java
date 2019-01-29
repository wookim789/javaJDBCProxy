package com.encore.jdbcproxy.db.query.oracle;
/*
 * PD(AR)의 테이블에서 RD에 접속을 위한 정보를 가져오는 쿼리문 클래스
 * 
 * @version 1 19/01/28
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/
public class OracleQuery {
	public OracleQuery(){
		
	}
	//포트 정보를 가져오는 쿼리문
	private static String PORT = " SELECT REGISTRY_PORT " +
					 		     " FROM SQL_PROXY " + 
					 			 " WHERE PROXY_IP = ? " +
					 			 " AND OBJECT_NAME = ? ";
	//SVR_ID를 가져오는 쿼리문
	private static String SVR_ID = " SELECT SVR_ID " + 
					 			   " FROM MD_SVR " +
					 			   " WHERE SVR_NM = ?";
	//유저의 비밀번호를 가져오는 쿼리문
	private static String USER_PW = " SELECT PASSWORD " +
					 			    " FROM SQL_PROXY_DBMS_CONN " +
					 			    " WHERE SVR_ID= ? " +
					 			    " AND SCHEMA = ? " +
					 			    " AND CONN_USER = ? ";
			
	
	public static String getPort() {
		return PORT;
	}
	
	public static String getSvr_id() {
		return SVR_ID;
	}
	
	public static String getUser_pw() {
		return USER_PW;
	}
}
