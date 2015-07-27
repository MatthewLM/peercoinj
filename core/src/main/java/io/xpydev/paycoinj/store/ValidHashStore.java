/**
 * Copyright 2014 Rimbit Developers.
 * Copyright 2014 Matthew Mitchell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.xpydev.paycoinj.store;


import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.io.Files;
import com.google.common.primitives.Bytes;
import io.xpydev.paycoinj.core.AbstractBlockChain;
import io.xpydev.paycoinj.core.Sha256Hash;
import io.xpydev.paycoinj.core.StoredBlock;
import io.xpydev.paycoinj.core.Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.spongycastle.util.encoders.Hex;

public class ValidHashStore {
	
	private File filePath;
	private ArrayList<byte[]> validHashesArray = new ArrayList<byte[]>();
	int index;
	boolean initialFind = true;
	
	private static URL VALID_HASHES_URL;
	
	static {
		try {
			VALID_HASHES_URL = new URL("https://peercoinexplorer.info/chain/Paycoin/q/getvalidhashes");
		} catch (final MalformedURLException x) {
			throw new RuntimeException(x); // cannot happen
		}
	}

	private static String GENESIS_MINI_HASH = "e327cd80c8b17efda4ea08c5877e95d8"; 
	
	public ValidHashStore(File filePath) throws IOException {
		
		this.filePath = filePath;
		long len = filePath.length();
		
		index = 0;
		
		if (len == 0) {
			// Add genesis hash and that is all
			BufferedOutputStream file = getOutputStream();
			writeHash(Hex.decode(GENESIS_MINI_HASH), file);
			file.flush();
			file.close();
			return;
		}
		
		// Load valid hashes from file.
		
		FileInputStream file = new FileInputStream(filePath);
		
		byte[] data = new byte[(int)len];
		file.read(data, 0, (int)len);
		
		byte[] b = new byte[16];
		
		for (int x = 0; x < len; x += 16) {
			System.arraycopy(data, x, b, 0, 16);
			validHashesArray.add(b);
			b = new byte[16];
		}
		
		file.close();
		
	}
	
	private BufferedOutputStream getOutputStream() throws FileNotFoundException {
		return new BufferedOutputStream(new FileOutputStream(this.filePath));
	}
	
	private void writeHash(byte[] hash, BufferedOutputStream file) throws IOException {
		
		validHashesArray.add(hash);
		file.write(hash, 0, 16);
		
	}
	
	private boolean isInValidHashes(byte[] cmpHash) {
		
		for (;index < validHashesArray.size(); index++) {
			
			if (Arrays.equals(validHashesArray.get(index), cmpHash)) {
				index++;
				initialFind = false;
				return true;
			}
			
			// Else if we are finding initial index continue, else fail.
			if (!initialFind) return false;
			
		}
		
		return false;
		
	}
	
	private byte[] getHashFromInputStream(InputStream is) throws IOException {
		
		byte[] hash = new byte[16];
		int x = 0, res;
		
		while (x < 16 && (res = is.read()) != -1)
			hash[x++] = (byte) res;
		
		if (x != 16)
			return null;
		
		return hash;
		
	}

	public boolean isValidHash(Sha256Hash hash, AbstractBlockChain blockChain, boolean waitForServer) throws IOException {
		
		// Get 16 bytes only
		byte[] cmpHash = new byte[16];
		System.arraycopy(Utils.reverseBytes(hash.getBytes()), 0, cmpHash, 0, 16);
	    
		// First check the existing hashes
		if (isInValidHashes(cmpHash))
			return true;
		
		// Nope. We need to ensure the valid hashes is synchronised with the server
		
		// Create POST data locator
		
		byte[] locator = new byte[3200];
		
		BlockStore store = checkNotNull(blockChain).getBlockStore();
		StoredBlock chainHead = blockChain.getChainHead();

		StoredBlock cursor = chainHead;
		int offset = 0;

		for (int i = 100; cursor != null && i > 0; i--, offset += 32) {
			System.arraycopy(Utils.reverseBytes(cursor.getHeader().getHash().getBytes()), 0, locator, offset, 32);

		    try {
			cursor = cursor.getPrev(store);
		    } catch (BlockStoreException e) {
			throw new RuntimeException(e);
		    }
		}

		// Now download hashes from server.

		// But if waitForServer is true, first wait a while in case the server hasn't received or processed this block yet.
		// We assume the server is well connected and 30 seconds would therefore be more than enough in most cases.
		if (waitForServer)
			Utils.sleep(30000);

		HttpURLConnection connection = (HttpURLConnection) VALID_HASHES_URL.openConnection();
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(false);
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/octet-stream");
		connection.setRequestProperty( "Accept-Encoding", "" ); 
		connection.setDoOutput(true);
		java.io.OutputStream os = connection.getOutputStream();
		os.write(locator, 0, offset);
		os.flush();
		os.close();
		connection.connect();

		try {		

		    final int responseCode = connection.getResponseCode();
		    if (responseCode == HttpURLConnection.HTTP_OK) {

			InputStream is = new BufferedInputStream(connection.getInputStream());

			// We are going to replace the valid hashes with the new ones

			BufferedOutputStream file = getOutputStream();
			validHashesArray.clear();
			index = 0;
			initialFind = true;

			// Write new hashes. Ensure a limit of 50,000 hashes.

			byte[] b;
			
			for (int x = 0; (b = getHashFromInputStream(is)) != null && x < 50000; x++)
				writeHash(b, file);

			file.flush();
			file.close();

		    } else throw new IOException("Bad response code from server when downloading hashes");

		}finally{
		    connection.disconnect();
		}

		// Lastly check valid hashes again
		return isInValidHashes(cmpHash);
		
	}
	
	public void close(){
		// Dummy method in case we add/remove it.
	}
	
}
