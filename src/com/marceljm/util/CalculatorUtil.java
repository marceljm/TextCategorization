package com.marceljm.util;

public class CalculatorUtil {

	public static float categoryWeight(int wordCounter, String knownData, String word, String[] wordList) {
		switch (wordCounter) {
		case 0:
			return knownData.contains(word) ? 5F : 1F;
		case 1:
			if (knownData.contains(word))
				return knownData.contains(wordList[0]) ? 10F : 5F;
			break;
		case 2:
			if (knownData.contains(word)) {
				if (knownData.contains(wordList[0]) && knownData.contains(wordList[1]))
					return 20F;
				else if (knownData.contains(wordList[0]) || knownData.contains(wordList[1]))
					return 10F;
				else
					return 5F;
			}
			break;
		default:
			return knownData.contains(word) ? 5F : 1F;
		}
		return 1F;
	}

}
