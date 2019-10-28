package com.szzcs.smartpos.update;

/**
 * 配置一些常量，静态变量
 * 
 */
public class Contants {
	/* 请求服务端 */

	// public static final String BASE_URL =
	// "http://121.46.8.125:7090/pay/tms/";//测试
	public static final String BASE_URL = "http://tms.szzcs.com:7099/pay/";
	public static final String UPDATE_URL = "tms/getUpgradeInf.json";// 检查更新
	public static final String UPDATE_APP_URL = "tms/getAppUpgradeInf.json";// 检查APP更新
	/* 服务端返回的结果 */
	public static final String RES_OK = "000000";
	public static final String RES_ERR = "999999";

	public static final String NET_CONN_ERROR = "netException";
	public static final String SERVER_CONN_ERROR = "webException";
	public static final String NET_EXCEPTION = "exception";
	public static final String REQUEST_YES = "yes";
	public static final String REQUEST_NO = "no";
	public static final String REQUEST_NULL = "null";
	public static final String ERROR = "ERROR";
	public static final String SUCCESS = "SUCCESS";
	public static final String EXIST = "EXIST";// 用户已存在
	/* inten跳转状态码 */
	public static final String REQUEST_CODE = "requstCode";
	public static final int SEARCH_TO_RESULT = 1;
	public static final int LIST_TO_RESULT = 2;
	public static final int COLLECT_TO_RESULT = 3;
	public static final int PUBLISH_TO_RESULT = 7;
}
