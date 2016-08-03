package com.marceljm.util;

public class CalculatorUtil {

	public static float weight(int wordCounter, String path, String word, String[] wordList) {
		switch (wordCounter) {
		case 0:
			return path.contains(word) ? 1.5F : 1F;
		case 1:
			if (path.contains(word))
				return path.contains(wordList[0]) ? 10F : 1.5F;
			break;
		case 2:
			if (path.contains(word)) {
				if (path.contains(wordList[0]) && path.contains(wordList[1]))
					return 20F;
				else if (path.contains(wordList[0]) || path.contains(wordList[1]))
					return 10F;
				else
					return 1.5F;
			}
			break;
		default:
			return path.contains(word) ? 1.5F : 1F;
		}
		return 1F;
	}

}
