/*
 * Copyright (C) 2015 NuBits Developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.matthewmitchell.peercoinj.shapeshift;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import org.slf4j.LoggerFactory;

/**
 * This class provides a way of asynchronously communicating over HTTP
 *
 * @author Matthew Mitchell
 */
public class AsyncHttpClient {
	
	public static final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public static final int CONNECTION_ERROR = -1;
	public static final int PARSE_ERROR = -2;
	public static final int API_ERROR = -3;
	
    /**
     * Make an HTTP post request
     *
     * @params url A string of the URL to make an HTTP post request to
     * @params postData A string of the post data
     * @params contentType A string of the content mime type to be used with the Content-Type header
     * @params cbks Callbacks for successful or unsuccessful responses
     */
	public void post(final String url, final String postData, final String contentType, final HttpResponseCallbacks cbks) {
		
		executor.execute(new Runnable() {

			@Override
			public void run() {
				
				try {

					HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
					connection.setUseCaches(false);
					connection.setInstanceFollowRedirects(false);
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(5000);
					connection.addRequestProperty("Accept-Encoding", "gzip");

					if (postData != null) {
						connection.setRequestMethod("POST");
						connection.setRequestProperty("Content-Type", contentType);
						connection.setDoOutput(true);
						OutputStream os = connection.getOutputStream();
						os.write(postData.getBytes(Charsets.UTF_8));
						os.flush();
						os.close();
					}

					connection.connect();

					final int responseCode = connection.getResponseCode();

					if (responseCode != HttpURLConnection.HTTP_OK) {
						cbks.onFailure(responseCode, connection.getResponseMessage());
						return;
					}

					final String contentEncoding = connection.getContentEncoding();

					InputStream is = new BufferedInputStream(connection.getInputStream(), 1024);
					if ("gzip".equalsIgnoreCase(contentEncoding))
						is = new GZIPInputStream(is);

					Reader reader = new InputStreamReader(is, Charsets.UTF_8);
					String result = CharStreams.toString(reader);
					
					cbks.onSuccess(result);

				} catch (IOException ex) {
					cbks.onFailure(CONNECTION_ERROR, "");
				}
			}
			
		});
		
	}
	
    /**
     * Make an HTTP get request
     *
     * @params url A string of the URL to make an HTTP get request to
     * @params cbks Callbacks for successful or unsuccessful responses
     */
	public void get(String url, HttpResponseCallbacks cbks) {
		post(url, null, null, cbks);
	}
	
	public interface HttpResponseCallbacks {
        /**
         * Provides the string response on a 200 OK success
         */
		public void onSuccess(String response);
        /**
         * Provides the string response and the HTTP code of a non 200 OK response
         */
		public void onFailure(int responseCode, String error);
	}
	
}
