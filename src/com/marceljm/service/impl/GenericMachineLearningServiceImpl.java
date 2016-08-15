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
import com.marceljm.service.ConstantService;
import com.marceljm.service.GenericMachineLearningService;
import com.marceljm.util.CalculatorUtil;
import com.marceljm.util.TextUtil;
import com.marceljm.util.ValidateUtil;

public class GenericMachineLearningServiceImpl implements GenericMachineLearningService {

	private int KEY_COLUMN1 = -1;
	private int KEY_COLUMN2 = -1;
	private int KNOWN_DATA_COLUMN;

	public GenericMachineLearningServiceImpl(int keyColumn1, int keyColumn2, int knownDataColumn) {
		KEY_COLUMN1 = keyColumn1;
		KEY_COLUMN2 = keyColumn2;
		KNOWN_DATA_COLUMN = knownDataColumn;
	}

	@Override
	public Map<String, Map<String, Float>> knowledgeBase() throws UnsupportedEncodingException, FileNotFoundException {

		/* name:[brand:weight] */
		Map<String, Map<String, Float>> fullMap = new HashMap<String, Map<String, Float>>();

		String line;
		String key;
		String knownData;

		long rowCounter = 0;

		float weight;

		File fileDir = new File(ConstantService.INPUT_FILE);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDir), ConstantService.CHARSET));

		/* read file */
		try {
			while ((line = in.readLine()) != null) {
				if (line.contains(ConstantService.HEADER_SIGNATURE))
					continue;

				key = line.split("\";\"")[KEY_COLUMN1];
				if (KEY_COLUMN2 > 0)
					key += " " + line.split("\";\"")[KEY_COLUMN2];
				knownData = line.split("\";\"")[KNOWN_DATA_COLUMN];

				if (line.split("\";\"").length == KNOWN_DATA_COLUMN + 1) {
					knownData = knownData.substring(0, knownData.length() - 1);
				}

				if (knownData.equals(""))
					continue;

				key = TextUtil.normalize(key);
				String[] wordList = key.split(" ");
				if (!ValidateUtil.isValidNameLength(wordList))
					continue;

				String normalizedKnownData = TextUtil.normalize(knownData);

				/* populate fullMap */
				for (String word : wordList) {
					weight = CalculatorUtil.genericWeight(normalizedKnownData, word);

					if (!fullMap.containsKey(word)) {
						Map<String, Float> aux = new HashMap<String, Float>();
						aux.put(knownData, weight);
						fullMap.put(word, aux);
					} else {
						if (fullMap.get(word).get(knownData) == null)
							fullMap.get(word).put(knownData, weight);
						else
							fullMap.get(word).put(knownData, fullMap.get(word).get(knownData) + weight);
					}
				}

				/* print progress */
				if (rowCounter % 100000 == 0)
					System.out.println(rowCounter);
				rowCounter++;
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
	public String categorize(Map<String, Map<String, Float>> fullMap, String key) {
		/* brand:weight */
		Map<String, Float> resultMap = new HashMap<String, Float>();

		key = TextUtil.normalize(key);

		/* populate resultMap */
		String[] wordList = key.split(" ");
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
		List<Field> sortedList = new ArrayList<Field>();
		for (Map.Entry<String, Float> i : resultMap.entrySet()) {
			Field field = new Field();
			field.setName(i.getKey());
			field.setValue(i.getValue());
			sortedList.add(field);
		}
		Collections.sort(sortedList);

		/* categorize */
		int size = sortedList.size();
		if (size == 0)
			return "";
		Field[] field = new Field[size];
		for (int i = 0; i < size; i++) {
			field[i] = sortedList.get(size - i - 1);
		}
		String unknownData = "";

		/* based on key text, the result can change */
		System.out.println("------------------------------------------");
		if (!key.contains(field[0].getName().toLowerCase())) {
			System.out.println(field[0].getName());
			for (int i = 1; i < size && i < 4; i++) {
				if (i < 3)
					System.out.println(field[i].getName());
				if (size > i && (key.contains(" " + field[i].getName().toLowerCase() + " "))) {
					unknownData = field[i].getName();
					break;
				}
			}
		} else
			unknownData = field[0].getName();

		return unknownData;
	}

}
