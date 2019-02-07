package com.encore.jdbcproxy.db.query.oracle;
/*
 * PD(AR)의 테이블에서 RD에 접속을 위한 정보를 가져오는 쿼리문 클래스
 * 
 * @version 2 19/02/01
 * @author intern Kim Gyeongwoo
 * @since JDK1.8
*/
public class OracleQuery {
	public OracleQuery(){
		
	}
	//SVR_ID를 가져오는 쿼리문
	private final static String SVR_ID = " SELECT SVR_ID " + 
										 " FROM MD_SVR " +
										 " WHERE SVR_NM = ?";
	//REGISTRY_PORT, 암호화된 PASSWORD 가져오기
	private final static String PORT_PW = " SELECT DISTINCT A.REGISTRY_PORT, B.PASSWORD " + 
										  " FROM SQL_PROXY A " + 
										  " INNER JOIN SQL_PROXY_DBMS_CONN B ON A.PROXY_ID = B.PROXY_ID " + 
										  " WHERE A.PROXY_IP = ? " +
										  " AND A.OBJECT_NAME = ? " + 
										  " AND B.SCHEMA = ? " + 
										  " AND B.CONN_USER = ? " +
										  " AND B.SVR_ID = ?";
	public static String getPort_pw() {
		return PORT_PW;
	}
	
	public static String getSvr_id() {
		return SVR_ID;
	}
}

