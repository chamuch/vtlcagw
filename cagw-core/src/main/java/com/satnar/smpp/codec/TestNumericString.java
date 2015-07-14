package com.satnar.smpp.codec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestNumericString {

	public static void main(String[] args) {
		int i = 0x80000001;
		System.out.println("int: " + i + ", hex print: " + java.lang.Integer.toHexString(i));
		
		
		String input = "adfABCDEF1234";
		Pattern pattern = Pattern.compile("[\\da-fA-F]*");
		Matcher matcher = pattern.matcher(input);
		System.out.println("Pattern test - matchInput: '" + input + "', matches: " + matcher.matches());
	}

}
