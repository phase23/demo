package com.szzcs.smartpos.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * Http网络请求Get,Put方法
 */
public class HttpUtil {
	/**
	 * 
	 * @param url
	 * @return HttpPost
	 */
	private static HttpPost getHttpPost(String url) {
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Content-Type", "text/html"); // 这行很重要
		httpPost.addHeader("charset", HTTP.UTF_8); // 这行很重要
		return httpPost;
	}

	/**
	 * 
	 * @param httpPost
	 * @return httpPost
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static HttpResponse getHttpResponse(HttpPost httpPost)
			throws ClientProtocolException, IOException {
		HttpResponse response = new DefaultHttpClient().execute(httpPost);
		return response;
	}

	/**
	 * 
	 * @param url
	 * @return resultString
	 */
	public static String getHttpPostResultForUrl(String url) {
		HttpPost httpPost = getHttpPost(url);
		String resultString = null;
		try {
			HttpResponse response = getHttpResponse(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity());
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			resultString = Contants.NET_CONN_ERROR;
			e.printStackTrace();
		} catch (IOException e) {
			resultString = Contants.NET_CONN_ERROR;
			e.printStackTrace();
		}
		return resultString;
	}

	public static String postHttpResponseText(String url, String post) {
		Log.d("Send ==> ", post);
		BufferedReader reader = null;
		HttpURLConnection conn = null;
		try {
			URL httpUrl = new URL(url);
			HttpURLConnection httpConn = (HttpURLConnection) httpUrl
					.openConnection(); // //设置连接属性
			httpConn.setDoOutput(true);// 使用URL 连接进行输出
			httpConn.setDoInput(true);// 使用 URL 连接进行输入
			httpConn.setUseCaches(false);// 忽略缓存
			httpConn.setRequestMethod("POST");// 设置URL请求方法
			String requestString = post; // 设置请求属性
			// 获得数据字节数据，请求数据流的编码，必须和下面服务器端处理请求流的编码一致
			byte[] requestStringBytes = requestString.getBytes("UTF-8");
			httpConn.setRequestProperty("Content-length", ""
					+ requestStringBytes.length);
			httpConn.setRequestProperty("Content-Type", "application/json");
			httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
			httpConn.setRequestProperty("Charset", "UTF-8");
			// 建立输出流，并写入数据
			OutputStream outputStream = httpConn.getOutputStream();
			outputStream.write(requestStringBytes);
			outputStream.close(); // 获得响应状态
			int responseCode = httpConn.getResponseCode();
			Log.e("DEVIL", "http状态码:" + responseCode);
			if (HttpURLConnection.HTTP_OK == responseCode) {// 连接成功 //
															// 当正确响应时处理数据
				StringBuffer buffer = new StringBuffer();
				String readLine = null;
				// 处理响应流，必须与服务器响应流输出的编码一致
				reader = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream(), "UTF-8"));
				while ((readLine = reader.readLine()) != null) {
					// buffer.append(readLine).append("\n");
					buffer.append(readLine);
				}
				reader.close();
				Log.d("Response <== ", buffer.toString());
				return buffer.toString();
			} else {
				return Contants.NET_CONN_ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Contants.SERVER_CONN_ERROR;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		// return null;

	}

	/**
	 * 得到网络请求字符串
	 * 
	 * @param url
	 *            请求的网络地址
	 * @return 返回网络返回的字符串 网络有误返回null
	 * 
	 */
	public static String getHttpResponseText(String url) {
		BufferedReader reader = null;
		HttpURLConnection conn = null;

		try {
			URL httpUrl = new URL(url);

			conn = (HttpURLConnection) httpUrl.openConnection();
			// conn.setDoInput(true);
			// conn.setDoOutput(true);
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(30000);
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Charset", HTTP.UTF_8);
			// conn.setRequestProperty("Content-Type", "application/json");
			// conn.set

			// 获得响应状态
			int responseCode = conn.getResponseCode();

			if (HttpURLConnection.HTTP_OK == responseCode) {// 连接成功
				reader = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), HTTP.UTF_8));
				StringBuffer buffer = new StringBuffer();
				String temp = null;
				while ((temp = reader.readLine()) != null) {
					buffer.append(temp);
				}
				reader.close();
				return buffer.toString();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}

		return null;

	}
}
