package com.demon.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by yhe.abcft on 2017/6/27.
 */
public class HttpClient {

    private static final Logger log = LogManager.getLogger(HttpClient.class);

    /**
     * GET 请求
     * @param urlStr 访问地址
     * @param param 访问参数，格式
     * @param charset 编码
     * @return
     */
    public static String doGet(String urlStr, String param, String charset) {
        String result = "";
        BufferedReader in = null;
        try {
            URL url = new URL(urlStr + "?" + param);
            URLConnection conn = url.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 防止 403错误
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
            conn.setRequestProperty("Referer", urlStr);
            conn.setRequestProperty("Host", url.getHost());

            conn.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * send serialize data
     * @param urlStr
     * @param jsonObject json format content
     * @return result json format data, error result null
     */
    public static JSONObject doPost(String urlStr, Object jsonObject) {
        URL url;
        String result;//要返回的结果
        try {
            url=new URL(urlStr);
            HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();

            httpURLConnection.setConnectTimeout(HttpConfig.CONNECTION_TIMEOUT_DEFAULT);//设置连接超时时间，单位ms
            httpURLConnection.setReadTimeout(HttpConfig.READ_TIMEOUT_DEFAULT);//设置读取超时时间，单位ms

            //设置是否向httpURLConnection输出，因为post请求参数要放在http正文内，所以要设置为true
            httpURLConnection.setDoOutput(true);
            //设置是否从httpURLConnection读入，默认是false
            httpURLConnection.setDoInput(true);
            //POST请求不能用缓存，设置为false
            httpURLConnection.setUseCaches(false);
            //传送的内容是可序列化的
            //如果不设置此项，传送序列化对象时，当WEB服务默认的不是这种类型时，会抛出java.io.EOFException错误
            httpURLConnection.setRequestProperty("Content-type","application/x-java-serialized-object");

            //设置请求方法是POST
            httpURLConnection.setRequestMethod("POST");

            //连接服务器
            httpURLConnection.connect();

            //getOutputStream会隐含调用connect()，所以不用写上述的httpURLConnection.connect()也行。
            //得到httpURLConnection的输出流
            OutputStream os = httpURLConnection.getOutputStream();

            //构建输出流对象，以实现输出序列化的对象
            ObjectOutputStream objOut=new ObjectOutputStream(os);

            //向对象输出流写出数据，这些数据将存到内存缓冲区中
            objOut.writeObject(jsonObject);

            //刷新对象输出流，将字节全部写入输出流中
            objOut.flush();

            //关闭流对象
            objOut.close();
            os.close();

            //将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端，并获取访问状态
            if(HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()){
                InputStream in = null;
                ObjectInputStream inObj = null;
                try {
                    //得到httpURLConnection的输入流，这里面包含服务器返回来的java对象
                    in = httpURLConnection.getInputStream();

                    //构建对象输入流，使用readObject()方法取出输入流中的java对象
                    inObj = new ObjectInputStream(in);
                    JSONObject object = (JSONObject) JSONObject.toJSON(inObj.readObject());


                    //输出日志，在控制台可以看到接收到的数据
                    log.info("RESULT===" + object.toString());

                    return object;
                } finally {
                    //关闭创建的流
                    if (in != null) {
                        in.close();
                    }
                    if (inObj != null) {
                        inObj.close();
                    }
                }
            }else{
                log.info("HTTP Connction failed" + httpURLConnection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 跨系统网页跳转
     * @param response
     * @param url
     * @throws IOException
     */
    public static void sendRedirect(HttpServletResponse response, String url) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<meta http-equiv=\"refresh\" content=\"1;url=" + url + "\">");
        out.println("</HEAD>");
        out.println("</HTML>");
        out.flush();
        out.close();
    }

    public static void main(String[] args) {
        System.out.println(doGet("http://www.baidu.com/", "", "UTF-8"));
    }
}
