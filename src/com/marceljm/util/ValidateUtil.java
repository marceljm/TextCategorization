package com.marceljm.util;

import com.marceljm.service.ConstantService;

public class ValidateUtil {

	private static int MINIMUM_WORDS_REQUIRED = 3;

	private static boolean hasValidCategoryMark(String category) {
		for (String i : ConstantService.VALID_CATEGORY_MARK) {
			if (!category.contains(i)) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasInvalidCategoryMark(String category) {
		for (String i : ConstantService.INVALID_CATEGORY_MARK) {
			if (category.contains(i)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isValidCategory(String category) {
		return hasValidCategoryMark(category) && !hasInvalidCategoryMark(category);
	}

	public static boolean isValidNameLength(String[] wordList) {
		return wordList.length >= MINIMUM_WORDS_REQUIRED;
	}
}
