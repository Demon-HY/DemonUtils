package com.demon.http;

/**
 * http config
 * Created by yhe.abcft on 2017/6/28.
 */
public class HttpConfig {

    public static final String CHARSET_UTF_8 = "UTF-8";
    public static final String CHARSET_GB2312 = "GB2312";
    public static final String CHARSET_GBK = "GBK";

    public static final int TIMEOUT_THIRTY_SECONDS = 30 * 1000;
    public static final int TIMEOUT_THIRTY_SIXTY = 60 * 1000;

    public static final int CONNECTION_TIMEOUT_DEFAULT = 3 * 1000;
    public static final int READ_TIMEOUT_DEFAULT = 60 * 1000;

    public static final int MAX_CONNECTIONS_TOTAL = 200;
    public static final int MAX_CONNECTIONS_PER_ROUTE = 50;

}
