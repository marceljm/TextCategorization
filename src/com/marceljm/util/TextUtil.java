package com.marceljm.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextUtil {

	private static String removeBadWords(String text) {
		for (String i : ConstantUtil.BAD_WORDS)
			text = text.replaceAll(i, " ");
		return text;
	}

	private static String removeBadChars(String text) {
		for (String i : ConstantUtil.BAD_CHARS)
			text = text.replaceAll(i, " ");
		return text;
	}

	private static String removeDoubleSpace(String text) {
		while (text.contains("  "))
			text = text.replace("  ", " ");
		return text;
	}

	private static String addPluralWords(String text) {
		for (int i = 0; i < ConstantUtil.PLURALS[0].length; i++)
			text = text.replaceAll(ConstantUtil.PLURALS[0][i], ConstantUtil.PLURALS[1][i]);
		return text;
	}

	private static String removeInitialAndFinalSpace(String text) {
		return text.replaceAll("^ ", "").replaceAll(" $", "");
	}

	public static String normalize(String text) {
		text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		text = text.toLowerCase();
		text = removeBadChars(text);
		text = removeBadWords(text);
		text = removeDoubleSpace(text);
		text = removeRepeatedWords(text);
		text = removeInitialAndFinalSpace(text);
		text = addPluralWords(text);
		return text;
	}

	private static String removeRepeatedWords(String text) {
		List<String> list = new ArrayList<String>();
		String result = "";
		for (String word : text.split(" ")) {
			if (!list.contains(word)) {
				list.add(word);
				result += word + " ";
			}
		}
		return result;
	}

	public static boolean containsAllWords(String text1, String text2) {
		List<String> text1List = Arrays.asList(text1.split(" "));
		List<String> text2List = Arrays.asList(text2.split(" "));
		for (String word : text2List) {
			if (!text1List.contains(word))
				return false;
		}
		return true;
	}

}
