package com.marceljm.util;

public class CalculatorUtil {

	public static float categoryWeight(int wordCounter, String categoryPath, String word, String[] wordList) {
		switch (wordCounter) {
		case 0:
			return categoryPath.contains(word) ? 5F : 1F;
		case 1:
			if (categoryPath.contains(word))
				return categoryPath.contains(wordList[0]) ? 10F : 5F;
			break;
		case 2:
			if (categoryPath.contains(word)) {
				if (categoryPath.contains(wordList[0]) && categoryPath.contains(wordList[1]))
					return 20F;
				else if (categoryPath.contains(wordList[0]) || categoryPath.contains(wordList[1]))
					return 10F;
				else
					return 5F;
			}
			break;
		default:
			return categoryPath.contains(word) ? 5F : 1F;
		}
		return 1F;
	}

}
