package com.gbi.web;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class RemoveCss {

	public static void main(String[] args) throws IOException {
		if (args == null || args.length < 3) {
			System.err
					.println("Usage:\nRemoveCss unusedcss.csv outputfile.css input1.css input2.css ... inputN.css");
			System.exit(-1);
		}
		FastUtf8Reader reader = new FastUtf8Reader(new File(args[0]), ",");
		String inputCss = "";
		for (int i = 2; i < args.length; i++) {
			String file = args[i];
			inputCss += FileUtils.readFileToString(new File(file), "UTF-8")
					+ "\n";
		}
		int count = 0;
		int found = 0;
		String processed = inputCss;
		while (reader.hasNext()) {
			String cssPattern = reader.next();
			if (StringUtils.isNotBlank(cssPattern)) {
				String pattern = cssPattern.trim();
				pattern = regexEscape(pattern);
				int size = processed.length();
				processed = processed.replaceAll(pattern + "\\s*\\{[^\\}]*}",
						"");
				if (processed.length() == size) {
					pattern = regexEscapeForCompound(cssPattern.trim(), false);
					processed = processed.replaceAll(pattern, "");
				}
				if (processed.length() == size) {
					pattern = regexEscapeForCompound(cssPattern.trim(), true);
					processed = processed.replaceAll(pattern, "");
				}
				if (processed.length() != size) {
					found++;
				} else {
					System.out.println("no match: " + count + ": "
							+ pattern.trim());
				}
				count++;
			}
		}
		FileUtils.write(new File(args[1]), processed, "UTF-8");
		System.out.println("removed: " + found + " of " + count
				+ " bad patterns, total css patterns "
				+ inputCss.split("\\{", -1).length + " now "
				+ processed.split("\\{", -1).length + " patterns");
	}

	private static String regexEscapeForCompound(String pTrim, boolean before) {
		return before ? "," + regex(pTrim) : regex(pTrim) + ",";
	}

	private static String regexEscape(String pPattern) {
		return "\n" + regex(pPattern);
	}

	private static String regex(String pPattern) {
		return pPattern.replaceAll("\\*", "\\\\*").replaceAll("\\.", "\\\\.")
				.replace(" > ", ">").replace(" + ", "+");
	}
}
