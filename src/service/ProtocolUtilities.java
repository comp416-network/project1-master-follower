package service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class ProtocolUtilities {
	
	/**
	 * Forwards the bytes from one stream to another
	 * @param source - the input stream from which we are sending
	 * @param destination - the output stream that we are sending to
	 * @throws IOException
	 */
	public static void sendBytes(InputStream source, OutputStream destination) throws IOException {
		byte[] buffer = new byte[1024];
		while(true) {
			int readAmount = source.read(buffer);
			if (readAmount == -1) break;
			destination.write(buffer,0,readAmount);
		}
	}
	/**
	 * Attempts to forward <code>len</code> bytes from one stream to another
	 * @param source - the input stream from which we are sending
	 * @param destination - the output stream that we are sending to
	 * @param len - the maximum number of bytes to send from source to destination
	 * @throws IOException
	 */
	public static void sendBytes(InputStream source, OutputStream destination,long len) throws IOException {
		byte[] buffer = new byte[1024];
		long remaining = len;
		while(true) {
			if (remaining == 0) break;
			int readAmount = source.read(buffer,0,(int) remaining);
			if (readAmount == -1) break;
			destination.write(buffer,0,readAmount);
			remaining -= readAmount;
		}
	}

}
