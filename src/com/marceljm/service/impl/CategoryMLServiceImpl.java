package com.marceljm.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.marceljm.entity.Field;
import com.marceljm.service.CategoryMLService;
import com.marceljm.util.ConstantUtil;
import com.marceljm.util.TextUtil;
import com.marceljm.util.ValidateUtil;

public class CategoryMLServiceImpl implements CategoryMLService {

	@Override
	public Map<String, Map<String, Float>> knowledgeBase() throws UnsupportedEncodingException, FileNotFoundException {
		/* name:[category:weight] */
		Map<String, Map<String, Float>> fullMap = new HashMap<String, Map<String, Float>>();

		HashSet<String> namePriceHashSet = new HashSet<String>();

		Map<String, Long> categoryAmountMap = categoryAmountMap();

		String line;
		String name;
		String pathCategory;

		long rowCounter = 0;

		// float weight;

		File fileDir = new File(ConstantUtil.INPUT_FILE);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDir), ConstantUtil.CHARSET));

		/* read file */
		try {
			while ((line = in.readLine()) != null) {
				if (line.contains(ConstantUtil.HEADER_SIGNATURE))
					continue;

				// skip repeated products
				String value = line.split("\";\"")[1] + ";" + line.split("\";\"")[2];
				if (namePriceHashSet.contains(value))
					continue;
				namePriceHashSet.add(value);

				name = line.split("\";\"")[1] + " " + line.split("\";\"")[7] + " " + line.split("\";\"")[11];
				name = name.substring(0, name.length() - 1);

				pathCategory = line.split("\";\"")[7];

				if (!ValidateUtil.isValidCategory(pathCategory.toLowerCase()))
					continue;

				name = TextUtil.normalize(name);
				String[] nameWordList = name.split(" ");
				if (!ValidateUtil.isValidNameLength(nameWordList))
					continue;

				// String normalizedPathCategory =
				// TextUtil.normalize(pathCategory);

				/* populate fullMap */
				// int wordCounter = 0;
				for (String word : nameWordList) {
					// float weight = CalculatorUtil.categoryWeight(wordCounter,
					// normalizedPathCategory, word,
					// nameWordList);
					if (!fullMap.containsKey(word)) {
						Map<String, Float> pathCategoryValueMap = new HashMap<String, Float>();
						pathCategoryValueMap.put(pathCategory, 1F / categoryAmountMap.get(pathCategory));
						fullMap.put(word, pathCategoryValueMap);
					} else {
						if (fullMap.get(word).get(pathCategory) == null)
							fullMap.get(word).put(pathCategory, 1F / categoryAmountMap.get(pathCategory));
						else
							fullMap.get(word).put(pathCategory,
									fullMap.get(word).get(pathCategory) + 1F / categoryAmountMap.get(pathCategory));
					}
					// wordCounter++;
				}

				/* print progress */
				if (++rowCounter % 100000 == 0)
					System.out.println("Category ML:" + rowCounter);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		/* convert fullMap values to percentage */
		Float total;
		for (Map.Entry<String, Map<String, Float>> mainMap : fullMap.entrySet()) {
			total = 0F;
			for (Map.Entry<String, Float> subMap : mainMap.getValue().entrySet())
				total += subMap.getValue();
			for (Map.Entry<String, Float> subMap : mainMap.getValue().entrySet())
				subMap.setValue(subMap.getValue() / total);
		}

		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fullMap;
	}

	@Override
	public String categorize(Map<String, Map<String, Float>> fullMap, String name, String brand,
			Map<String, Set<String>> brandCategoryMap, Map<String, String> normalizedFirstCategoryMap,
			Map<String, String> normalizedSecondCategoryMap, Map<String, String> normalizedThirdCategoryMap) {
		/* category:weight */
		Map<String, Float> resultMap = new HashMap<String, Float>();

		name = TextUtil.normalize(name);
		brand = TextUtil.normalize(brand);

		if (name.split(" ").length < 3)
			return "";
		String first = name.split(" ")[0];
		String second = name.split(" ")[1];
		String third = name.split(" ")[2];
		if (name.split(" ").length > 3) {
			if (first.equals(second) || second.equals(third))
				second = name.split(" ")[3];
			if (first.equals(third))
				third = name.split(" ")[3];
		}

		/* populate resultMap */
		String[] nameWordList = name.split(" ");
		for (String word : nameWordList) {
			Map<String, Float> categoryValueMap = fullMap.get(word);
			if (categoryValueMap != null) {
				for (Map.Entry<String, Float> i : categoryValueMap.entrySet()) {
					if (!resultMap.containsKey(i.getKey())) {
						resultMap.put(i.getKey(), i.getValue());
					} else
						resultMap.put(i.getKey(), i.getValue() + resultMap.get(i.getKey()));
				}
			}
		}

		/* convert resultMap to sorted list */
		List<Field> resultSortedList = new ArrayList<Field>();
		for (Map.Entry<String, Float> i : resultMap.entrySet()) {
			Field field = new Field();
			field.setName(i.getKey());
			field.setValue(i.getValue());
			resultSortedList.add(field);
		}
		Collections.sort(resultSortedList);

		/* invert sorted list and convert to array */
		int size = resultSortedList.size();
		if (size == 0)
			return "";
		Field[] resultArray = new Field[size];
		for (int i = 0; i < size; i++)
			resultArray[i] = resultSortedList.get(size - i - 1);
		String category = "";

		/*
		 * categorization: 10%, Third Category, first word, second word, brand,
		 * >=0.2
		 */
		for (int i = 0; i < size * 0.1; i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && normalizedResult.contains(first)
						&& normalizedResult.contains(second) && resultArray[i].getValue() >= 0.2) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
				}
			}
		}

		/*
		 * categorization: 10%, Third Category, first word, third word, brand,
		 * >=0.2
		 */
		for (int i = 0; i < size * 0.1 && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && normalizedResult.contains(first)
						&& normalizedResult.contains(third) && resultArray[i].getValue() >= 0.2) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
				}
			}
		}

		/*
		 * categorization: 10%, Third Category, second word, third word, brand,
		 * >=0.2
		 */
		for (int i = 0; i < size * 0.1 && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && normalizedResult.contains(second)
						&& normalizedResult.contains(third) && resultArray[i].getValue() >= 0.2) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
				}
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////

		/* categorization: 10%, Third Category, first word, brand, >=0.4 */
		for (int i = 0; i < size * 0.1 && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && normalizedResult.contains(first)
						&& resultArray[i].getValue() >= 0.4) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
				}
			}
		}

		/* categorization: 10%, Third Category, second word, brand, >=0.4 */
		for (int i = 0; i < size * 0.1 && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && resultArray[i].getValue() >= 0.4
						&& (second.length() <= 3 ? Arrays.asList(normalizedResult.split(" ")).contains(second)
								: normalizedResult.contains(second)
										&& second.contains(normalizedResult.substring(0, 4)))) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
				}
			}
		}

		/* categorization: 10%, Third Category, third word, brand, >=0.4 */
		for (int i = 0; i < size * 0.1 && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && resultArray[i].getValue() >= 0.4
						&& (third.length() <= 3 ? Arrays.asList(normalizedResult.split(" ")).contains(third)
								: normalizedResult.contains(third)
										&& third.contains(normalizedResult.substring(0, 4)))) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
				}
			}
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////////

		/*
		 * categorization: 10%, Second Category, first word, second word, brand
		 */
		for (int i = 0; i < size * 0.1 && category.equals(""); i++) {
			String normalizedResult = normalizedSecondCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && resultArray[i].getValue() >= 0.13
						&& normalizedResult.contains(first) && normalizedResult.contains(second)) {
					category = resultArray[i].getName();
					System.out.println(resultArray[i].getValue() + ";" + name + ";" + resultArray[i].getName());
				}
			}
		}

		/*
		 * categorization: 10%, Second Category, first word, third word, brand
		 */
		for (int i = 0; i < size * 0.1 && category.equals(""); i++) {
			String normalizedResult = normalizedSecondCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && resultArray[i].getValue() >= 0.13
						&& normalizedResult.contains(first) && normalizedResult.contains(third)) {
					category = resultArray[i].getName();
					System.out.println(resultArray[i].getValue() + ";" + name + ";" + resultArray[i].getName());
				}
			}
		}

		/*
		 * categorization: 10%, Second Category, second word, third word, brand
		 */
		for (int i = 0; i < size * 0.1 && category.equals(""); i++) {
			String normalizedResult = normalizedSecondCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && resultArray[i].getValue() >= 0.13
						&& normalizedResult.contains(second) && normalizedResult.contains(third)) {
					category = resultArray[i].getName();
					System.out.println(resultArray[i].getValue() + ";" + name + ";" + resultArray[i].getName());
				}
			}
		}

		return category;

	}

	private Map<String, Long> categoryAmountMap() throws UnsupportedEncodingException, FileNotFoundException {

		/* category:amount */
		Map<String, Long> categoryAmountMap = new HashMap<String, Long>();

		HashSet<String> namePriceHashSet = new HashSet<String>();

		String line;
		String pathCategory;

		long rowCounter = 0;

		File fileDir = new File(ConstantUtil.INPUT_FILE);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDir), ConstantUtil.CHARSET));

		/* read file */
		try {
			while ((line = in.readLine()) != null) {
				if (line.contains(ConstantUtil.HEADER_SIGNATURE))
					continue;

				// skip repeated products
				String value = line.split("\";\"")[1] + ";" + line.split("\";\"")[2];
				if (namePriceHashSet.contains(value))
					continue;
				namePriceHashSet.add(value);

				pathCategory = line.split("\";\"")[7];

				if (!ValidateUtil.isValidCategory(pathCategory.toLowerCase()))
					continue;

				/* populate categoryAmountMap */
				if (!categoryAmountMap.containsKey(pathCategory)) {
					categoryAmountMap.put(pathCategory, 1L);
				} else {
					if (categoryAmountMap.get(pathCategory) == null)
						categoryAmountMap.put(pathCategory, 1L);
					else
						categoryAmountMap.put(pathCategory, 1L + categoryAmountMap.get(pathCategory));
				}

				/* print progress */
				if (++rowCounter % 100000 == 0)
					System.out.println("Category ML:" + rowCounter);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return categoryAmountMap;
	}
}
