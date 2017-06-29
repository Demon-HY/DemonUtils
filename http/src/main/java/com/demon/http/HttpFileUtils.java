package com.demon.http;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * http file operation utils, simple
 *
 * Created by yhe.abcft on 2017/6/27.
 */
public class HttpFileUtils {

    private static final Logger log = LogManager.getLogger(HttpFileUtils.class);

    /**
     * simulation from upload file,
     * default encode is UTF-8
     *
     * @param url 请求地址 form表单url地址
     * @param filePath 文件路径
     * @param  size 文件大小
     *
     * @return String url的响应信息返回值
     * @throws IOException
     */
    public static String simulationFormUploadSimpleFile(String url, String filePath, int size) throws IOException {
        return simulationFormUploadSimpleFile(url, filePath, size, HttpConfig.CHARSET_UTF_8);
    }

    /**
     * simulation from upload file
     *
     * @param url request url
     * @param filePath file local path
     * @param size file size
     * @param charset file encode
     *
     * @return String url的响应信息返回值
     * @throws IOException
     */
    public static  String simulationFormUploadSimpleFile(String url, String filePath, int size, String charset) throws IOException {
        URL urlObj = new URL(url);

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("文件不存在");
        }

        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        con.setRequestMethod("POST"); // POST request
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false); // post not using cache
        // set request head information
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", charset);
        // set boundary
        String BOUNDARY = "----------" + System.currentTimeMillis();
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        // request
        StringBuilder sb = new StringBuilder();
        // package content
        sb.append("--");
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data;name=\"size\"");
        sb.append("\r\n\r\n");
        sb.append(size);
        sb.append("\r\n");
        sb.append("--"); // must parameter
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data;name=\"file\";filename=\"").append(file.getName()).append("\"\r\n");
        sb.append("Content-Type:application/octet-stream\r\n\r\n");

        byte[] head = sb.toString().getBytes(charset);

        // 获得输出流
        OutputStream out = new DataOutputStream(con.getOutputStream());
        // 输出表头
        out.write(head);

        // 把文件以流文件的方式 推入到url中
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        int bytes;
        byte[] bufferOut = new byte[1024];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        in.close();

        byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes(charset);// 定义最后数据分隔线
        out.write(foot);
        out.flush();
        out.close();

        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = null;
        String result;

        try {
            // 定义BufferedReader输入流来读取URL的响应
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            result = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("数据读取异常");
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return result;
    }

    /**
     * adopt url download file and save file,
     * default timeout is 30seconds
     *
     * @param urlStr download file url
     * @param fileName file name
     * @param savePath file save path
     *
     * @throws IOException
     */
    public static void downloadFileFromUrl(String urlStr, String fileName, String savePath) throws IOException{
        downloadFileFromUrl(urlStr, fileName, savePath, HttpConfig.TIMEOUT_THIRTY_SECONDS);
    }

    /**
     * adopt url download file and save file
     * @param urlStr download file url
     * @param fileName file name
     * @param savePath file save path
     * @param timeout request timeout time
     *
     * @throws IOException
     */
    public static void downloadFileFromUrl(String urlStr, String fileName, String savePath, Integer timeout)
            throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(timeout);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36" +
                " (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
        // referer 设置
        conn.setRequestProperty("Referer", url.getPath());

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);

        File saveDir = new File(savePath);
        boolean isDirectoryCreated = saveDir.exists();
        if (!isDirectoryCreated) {
            isDirectoryCreated = saveDir.mkdirs();
        }
        if (isDirectoryCreated) {
            File file = new File(saveDir+File.separator+fileName);
            FileOutputStream fos = new FileOutputStream(file);
            log.info("starting write file, file path is '" + file.getPath() + "'.");
            fos.write(getData);
            fos.close();
            if(inputStream!=null){
                inputStream.close();
            }


            log.info("info:" + url + " download success");
        }
    }

    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}
