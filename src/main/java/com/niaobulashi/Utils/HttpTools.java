package com.niaobulashi.Utils;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tengyunyang
 * http工具类
 * @date 2019/7/15 13:54
 */
public class HttpTools {
    public static void main(String[] args) {
        String post = "http://192.168.3.109:50269/auto_generate?";
        String get = "http://192.168.3.109:5000/Date";

///dataInterface/findById
        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("input_data", "大理欧普智能科技有限公司公司公司%23/Users/ture/BU/work/专利/大理/大理欧普智能科技有限公司规划表.xls,/Users/ture/BU/work/专利/大理/大理_2021序时账.xls,/Users/ture/BU/work/专利/大理/大理2020序时账.xls,/Users/ture/BU/work/专利/大理/大理_2019序时账.xls");
//        params.put("input_data", "12312312312");

//        String post1 = post(post, params);
//        System.out.println(post1);

        params.put("file_name","大理欧普智能科技有限公司公司公司#/Users/ture/BU/work/专利/大理/大理欧普智能科技有限公司规划表.xls,/Users/ture/BU/work/专利/大理/大理_2021序时账.xls,/Users/ture/BU/work/专利/大理/大理2020序时账.xls,/Users/ture/BU/work/专利/大理/大理_2019序时账.xls");
        String s = get(get, params);
        System.out.println(s);

//        System.out.println(get("http://localhost:8133/dataInterface/findById", params));
//        System.out.println(HttpTools.getRawBody(post, "{}", "GET"));
//        System.out.println(GETNoneParam("http://localhost:8133/dataInterface/findAll", "POST"));

    }

    public final static int REQUEST_TIMEOUT = 3000 * 1000;
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * get key-value
     *
     * @param urlStr
     * @param params
     * @return
     */
    public static String get(String urlStr, Map<String, Object> params) {

        String result = "";
        HttpURLConnection conn = null;
        try {
            String para = "";// new String("username=admin&password=admin");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                para += entry.getKey() + "=" + URLEncoder.encode(String.valueOf(entry.getValue()), DEFAULT_CHARSET) + "&";
            }
            if (!"".equals(para)) {
                para = para.substring(0, para.length() - 1);
            }
            urlStr += "?" + para;
            URL url = new URL(urlStr);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(GET);
            conn.setReadTimeout(REQUEST_TIMEOUT);
            conn.setRequestProperty("encoding", DEFAULT_CHARSET); // 可以指定编码
            conn.setConnectTimeout(REQUEST_TIMEOUT);
            conn.setUseCaches(false);
            conn.connect();

            if (conn.getResponseCode() == 200) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String line = "";
                StringBuilder builder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET));
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                in.close();
                bufferedReader.close();
                result = builder.toString();
            } else {
                System.err.println(conn.getResponseCode());
                result = "ERROR";
            }
        } catch (SocketTimeoutException e) {
            result = "Read timed out";
        } catch (Exception e) {
            result = "ERROR";
            e.printStackTrace();
            System.err.println("http  连接失败 HttpTools line 90");
            return result;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }

    /**
     * post key-value
     *
     * @param urlStr
     * @param params
     * @return
     */
    public static String post(String urlStr, Map<String, String> params) {

        String result = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            String para = "";// new String("username=admin&password=admin");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                para += entry.getKey() + "=" + URLEncoder.encode((String) entry.getValue(), DEFAULT_CHARSET) + "&";
            }
            if (!"".equals(para)) {
                para = para.substring(0, para.length() - 1);
            }
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(POST);
            conn.setReadTimeout(REQUEST_TIMEOUT);
            conn.setConnectTimeout(REQUEST_TIMEOUT);
            conn.setRequestProperty("contentType", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(para.getBytes().length));
            conn.setDoOutput(true);
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(para.getBytes(DEFAULT_CHARSET));
            outputStream.close();
            conn.connect();
            System.out.println(conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                InputStream in = new BufferedInputStream(conn.getInputStream());

                String line = "";
                StringBuilder builder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET));
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                in.close();
                bufferedReader.close();
                result = builder.toString();
            } else {
                result = "ERROR";
                System.err.println("http  失败");
            }
        } catch (Exception e) {
            result = "ERROR";
            System.err.println("http   连接失败");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }


    /**
     * rawBody
     *
     * @param url
     * @param rawBody
     * @param requestMethod
     * @return
     */
    public static String getRawBody(String url, String rawBody, String requestMethod) {
        HttpURLConnection conn = null;
        PrintWriter pw = null;
        BufferedReader rd = null;
        StringBuilder sb = new StringBuilder();
        String line = null;
        String response = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(requestMethod);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(REQUEST_TIMEOUT);
            conn.setConnectTimeout(REQUEST_TIMEOUT);
            conn.setUseCaches(false);
            conn.connect();
            pw = new PrintWriter(conn.getOutputStream());
            pw.print(rawBody);
            pw.flush();
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            response = sb.toString();
        } catch (Exception e) {
            response = "ERROR";
            e.printStackTrace();
            System.err.println("http   连接失败");
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (rd != null) {
                    rd.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    /**
     * NoneParam
     *
     * @param urlStr
     * @param requestMethod
     * @return
     */
    public static String getNoneParam(String urlStr, String requestMethod) {

        String result = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(REQUEST_TIMEOUT);
            conn.setConnectTimeout(REQUEST_TIMEOUT);
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("contentType", "application/x-www-form-urlencoded");
            conn.connect();
            if (conn.getResponseCode() == 200) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String line = "";
                StringBuilder builder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET));
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                in.close();
                bufferedReader.close();
                result = builder.toString();
            } else {
                result = "ERROR";
            }
        } catch (Exception e) {
            result = "ERROR";
            System.err.println("http   连接失败");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }
}
