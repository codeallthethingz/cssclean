package com.gbi.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

/**
 * Utility class with the fastest (at time of writing) way of getting bytes off
 * disk and into a JVM. Splits the input at the specified separator
 * 
 * Remember to call close after you're finished with this object.
 * 
 * @author will
 * 
 */
public class FastUtf8Reader implements Iterator<String> {

	private String separator = null;
	private BufferedReader in = null;
	private StringBuffer text = new StringBuffer();
	private int bufferSize = 128; // This shouldn't be changed (I don't think);
	private char[] cs = new char[bufferSize];
	private File file = null;
	private boolean zipped;

	public FastUtf8Reader(File pFile, String pSeparator)
			throws FileNotFoundException {
		this(pFile, pSeparator, false);
	}

	public FastUtf8Reader(File pFile, String pSeparator, boolean pZipped)
			throws FileNotFoundException {
		super();
		file = pFile;
		zipped = pZipped;
		separator = pSeparator;
		open();
	}

	public void reopen() throws FileNotFoundException {
		open();
	}

	private void open() throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		try {
			if (zipped) {
				in = new BufferedReader(
						new InputStreamReader(new GZIPInputStream(
								new FileInputStream(file)), "utf8"), 128);
			} else {
				in = new BufferedReader(new InputStreamReader(
						new FileInputStream(file), "UTF-8"), 128);
			}
		} catch (IOException e) {
			throw new IllegalStateException("UTF8 is no longer supported!", e);
		}
	}

	public boolean hasNext() {
		boolean hasNext = text != null
				&& (text.length() > 0 || text.indexOf(separator) != -1 || read() != -1);
		if (!hasNext) {
			close();
		}
		return hasNext;
	}

	private int read() {
		try {
			int read = in.read(cs, 0, cs.length);
			if (read != -1) {
				text.append(cs, 0, read);
			}
			return read;
		} catch (IOException e) {
			IOUtils.closeQuietly(in);
			throw new IllegalStateException("file error: "
					+ file.getAbsolutePath(), e);
		}
	}

	public String next() {
		if (text.indexOf(separator) == -1) {
			while (text.indexOf(separator) == -1 && read() != -1) {
				// loop until file is read or separator is found.
			}
		}
		return getSubSequenceAndTruncate();
	}

	private String getSubSequenceAndTruncate() {
		int indexOf = text.indexOf(separator);
		if (indexOf == -1) {
			String result = text.toString();
			text = new StringBuffer();
			return result;
		}
		String result = text.substring(0, indexOf);
		text = new StringBuffer(text.substring(indexOf + separator.length()));
		return result;
	}

	public void remove() {
		throw new IllegalStateException("Not implemented");
	}

	public void close() {
		text = null;
		IOUtils.closeQuietly(in);
	}

}
