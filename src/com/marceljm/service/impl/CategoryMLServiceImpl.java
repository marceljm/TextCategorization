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

import com.marceljm.entity.Field;
import com.marceljm.service.MLService;
import com.marceljm.util.CalculatorUtil;
import com.marceljm.util.ConstantUtil;
import com.marceljm.util.TextUtil;
import com.marceljm.util.ValidateUtil;

public class CategoryMLServiceImpl implements MLService {

	@Override
	public Map<String, Map<String, Float>> knowledgeBase() throws UnsupportedEncodingException, FileNotFoundException {

		/* name:[category:weight] */
		Map<String, Map<String, Float>> fullMap = new HashMap<String, Map<String, Float>>();

		String line;
		String name;
		String path;

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
				path = line.split("\";\"")[7];

				if (!ValidateUtil.isValidCategory(path.toLowerCase()))
					continue;

				name = TextUtil.normalize(name);
				String[] wordList = name.split(" ");
				if (!ValidateUtil.isValidNameLength(wordList))
					continue;

				String normalizedPath = TextUtil.normalize(path);

				/* populate fullMap */
				int wordCounter = 0;
				for (String word : wordList) {
					weight = CalculatorUtil.categoryWeight(wordCounter, normalizedPath, word, wordList);

					if (!fullMap.containsKey(word)) {
						Map<String, Float> aux = new HashMap<String, Float>();
						aux.put(path, weight);
						fullMap.put(word, aux);
					} else {
						if (fullMap.get(word).get(path) == null)
							fullMap.get(word).put(path, weight);
						else
							fullMap.get(word).put(path, fullMap.get(word).get(path) + weight);
					}
					wordCounter++;
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
			for (Map.Entry<String, Float> subMap : mainMap.getValue().entrySet()) {
				total += subMap.getValue();
			}
			for (Map.Entry<String, Float> subMap : mainMap.getValue().entrySet()) {
				subMap.setValue(subMap.getValue() / total);
			}
		}

		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fullMap;
	}

	@Override
	public String categorize(Map<String, Map<String, Float>> fullMap, String name) {
		/* category:weight */
		Map<String, Float> resultMap = new HashMap<String, Float>();

		name = TextUtil.normalize(name);

		/* populate resultMap */
		String[] wordList = name.split(" ");
		for (String word : wordList) {
			Map<String, Float> subMap = fullMap.get(word);
			if (subMap != null) {
				for (Map.Entry<String, Float> i : subMap.entrySet()) {
					if (!resultMap.containsKey(i.getKey())) {
						resultMap.put(i.getKey(), i.getValue());
					} else {
						resultMap.put(i.getKey(), i.getValue() + resultMap.get(i.getKey()));
					}
				}
			}
		}

		/* convert resultMap to sorted list */
		List<Field> categoryList = new ArrayList<Field>();
		for (Map.Entry<String, Float> i : resultMap.entrySet()) {
			Field cat = new Field();
			cat.setName(i.getKey());
			cat.setValue(i.getValue());
			categoryList.add(cat);
		}
		Collections.sort(categoryList);

		/* categorize */
		int size = categoryList.size();
		if (size == 0)
			return "";
		Field[] category = new Field[size];
		for (int i = 0; i < size; i++) {
			category[i] = categoryList.get(size - i - 1);
		}
		String finalCategory = category[0].getName();

		/* based on category text, the result can change */
		if (!category[0].getName().toLowerCase().contains(wordList[0])) {
			if (size > 1 && category[0].getValue() - category[1].getValue() < 1.5F
					&& category[1].getName().toLowerCase().contains(wordList[0]))
				finalCategory = category[1].getName();
			else if (size > 2 && category[0].getValue() - category[2].getValue() < 1.5F
					&& category[2].getName().toLowerCase().contains(wordList[0]))
				finalCategory = category[2].getName();
		}

		return finalCategory;
	}

}
