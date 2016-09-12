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
				String value = line.split("\";\"")[1] + ";" + line.split("\";\"")[7];
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
		Collections.sort(resultSortedList, Collections.reverseOrder());

		int size = resultSortedList.size();
		if (size == 0)
			return "";

		/* convert to array */
		Field[] resultArray = new Field[size];
		resultArray = resultSortedList.toArray(resultArray);

		String category = "";

		/* 10233: [third] category, first and second word, brand, 0.0 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && contains(first, normalizedResult)
						&& contains(second, normalizedResult) && resultArray[i].getValue() >= 0.0) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 2130: [second] category, first and second word, brand, 0.1 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedSecondCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && contains(first, normalizedResult)
						&& contains(second, normalizedResult) && resultArray[i].getValue() >= 0.1) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 411: [first] category, first and second word, brand, 0.02 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedFirstCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && contains(first, normalizedResult)
						&& contains(second, normalizedResult) && resultArray[i].getValue() >= 0.02) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 3006: [third] category, first and second word, 0.02 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null) {
				if (contains(first, normalizedResult) && contains(second, normalizedResult)
						&& resultArray[i].getValue() >= 0.02) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 704: [second] category, first and second word, 0.07 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedSecondCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null) {
				if (contains(first, normalizedResult) && contains(second, normalizedResult)
						&& resultArray[i].getValue() >= 0.07) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 35: [first] category, first and second word, 0.28 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedFirstCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null) {
				if (contains(first, normalizedResult) && contains(second, normalizedResult)
						&& resultArray[i].getValue() >= 0.28) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 827: [third] category, first and third word, brand, 0.03 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && contains(first, normalizedResult)
						&& contains(third, normalizedResult) && resultArray[i].getValue() >= 0.03) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 702: [second] category, first and third word, brand, 0.1 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedSecondCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && contains(first, normalizedResult)
						&& contains(third, normalizedResult) && resultArray[i].getValue() >= 0.1) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 158: [third] category, first and third word, 0.22 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null) {
				if (contains(first, normalizedResult) && contains(third, normalizedResult)
						&& resultArray[i].getValue() >= 0.22) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 148: [second] category, first and third word, 0.22 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedSecondCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null) {
				if (contains(first, normalizedResult) && contains(third, normalizedResult)
						&& resultArray[i].getValue() >= 0.22) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* 39320: [third] category, first word, brand, 0.136 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedThirdCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && contains(first, normalizedResult)
						&& resultArray[i].getValue() >= 0.0) {
					category = resultArray[i].getName();
					// System.out.println(resultArray[i].getValue() + ";" + name
					// + ";" + resultArray[i].getName());
					break;
				}
			}
		}
		/* ?: [second] category, first word, brand, 0.24 */
		for (int i = 0; i < size && category.equals(""); i++) {
			String normalizedResult = normalizedSecondCategoryMap.get(resultArray[i].getName());
			if (normalizedResult != null && brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName()) && contains(first, normalizedResult)
						&& resultArray[i].getValue() >= 0.24) {
					category = resultArray[i].getName();
					System.out.println(resultArray[i].getValue() + ";" + name + ";" + resultArray[i].getName());
					break;
				}
			}
		}

		return category;

	}

	private boolean contains(String word, String result) {
		return word.length() <= 3 ? Arrays.asList(result.split(" ")).contains(word)
				: result.contains(word) && containsResult(word, result);
	}

	private boolean containsResult(String word, String result) {
		for (String aux : result.split(" "))
			if (aux.contains(word) && word.contains(aux.substring(0, 4)))
				return true;
		return false;
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
				String value = line.split("\";\"")[1] + ";" + line.split("\";\"")[7];
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
