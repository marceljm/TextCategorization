package com.marceljm.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

		String line;
		String name;
		String pathCategory;

		long rowCounter = 0;

		float weight;

		File fileDir = new File(ConstantUtil.INPUT_FILE);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDir), ConstantUtil.CHARSET));

		/* read file */
		try {
			while ((line = in.readLine()) != null) {
				if (line.contains(ConstantUtil.HEADER_SIGNATURE))
					continue;

				name = line.split("\";\"")[1];
				pathCategory = line.split("\";\"")[7];

				if (!ValidateUtil.isValidCategory(pathCategory.toLowerCase()))
					continue;

				name = TextUtil.normalize(name);
				String[] nameWordList = name.split(" ");
				if (!ValidateUtil.isValidNameLength(nameWordList))
					continue;

				String normalizedPathCategory = TextUtil.normalize(pathCategory);

				/* populate fullMap */
				// int wordCounter = 0;
				for (String word : nameWordList) {
					// weight = CalculatorUtil.categoryWeight(wordCounter,
					// normalizedPathCategory, word, nameWordList);
					if (!fullMap.containsKey(word)) {
						Map<String, Float> pathCategoryValueMap = new HashMap<String, Float>();
						pathCategoryValueMap.put(pathCategory, 1F);
						fullMap.put(word, pathCategoryValueMap);
					} else {
						if (fullMap.get(word).get(pathCategory) == null)
							fullMap.get(word).put(pathCategory, 1F);
						else
							fullMap.get(word).put(pathCategory, fullMap.get(word).get(pathCategory) + 1F);
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
			Map<String, Set<String>> brandCategoryMap) {
		/* category:weight */
		Map<String, Float> resultMap = new HashMap<String, Float>();

		name = TextUtil.normalize(name);
		brand = TextUtil.normalize(brand);

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

		/* categorization: category should be binded to brand */
		for (int i = 0; i < size * 0.1; i++) {
			if (brandCategoryMap.get(brand) != null) {
				if (brandCategoryMap.get(brand).contains(resultArray[i].getName())) {
					category = resultArray[i].getName();
					break;
				}
			}
		}

		return category;
	}

}
