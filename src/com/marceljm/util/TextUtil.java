package com.marceljm.util;

import com.marceljm.service.ConstantService;

public class TextUtil {

	private static String removeBadWords(String text) {
		for (String i : ConstantService.BAD_WORDS)
			text = text.replaceAll(i, " ");
		return text;
	}

	private static String removeBadChars(String text) {
		for (String i : ConstantService.BAD_CHARS)
			text = text.replaceAll(i, " ");
		return text;
	}

	private static String removeDoubleSpace(String text) {
		while (text.contains("  "))
			text = text.replace("  ", " ");
		return text;
	}

	private static String addPluralWords(String text) {
		for (int i = 0; i < ConstantService.PLURALS[0].length; i++)
			text = text.replaceAll(ConstantService.PLURALS[0][i], ConstantService.PLURALS[1][i]);
		return text;
	}

	public static String normalize(String text) {
		text = text.toLowerCase();
		text = removeBadChars(text);
		text = removeBadWords(text);
		text = removeDoubleSpace(text);
		text = addPluralWords(text);
		return text;
	}

}
