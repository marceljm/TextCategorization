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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.marceljm.entity.Field;
import com.marceljm.service.BrandMLService;
import com.marceljm.util.ConstantUtil;
import com.marceljm.util.TextUtil;
import com.marceljm.util.ValidateUtil;

public class BrandMLServiceImpl implements BrandMLService {

	private static String[] stores = ConstantUtil.STORES;

	private int NAME_COLUMN = 1;
	private int BRAND_COLUMN = 11;

	public BrandMLServiceImpl() {
	}

	@Override
	public Map<String, Map<String, Float>> knowledgeBase() throws UnsupportedEncodingException, FileNotFoundException {

		/* name:[brand:weight] */
		Map<String, Map<String, Float>> fullMap = new HashMap<String, Map<String, Float>>();

		HashSet<String> namePriceHashSet = new HashSet<String>();

		String line;
		String name;
		String brand;

		long rowCounter = 0;

		for (String store : stores) {

			File fileDir = new File("resources/" + store.split(";")[0] + ".csv");
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

					name = line.split("\";\"")[NAME_COLUMN];
					brand = line.split("\";\"")[BRAND_COLUMN];

					if (line.split("\";\"").length == BRAND_COLUMN + 1)
						brand = brand.substring(0, brand.length() - 1);

					if (brand.equals(""))
						continue;

					name = TextUtil.normalize(name);
					String[] nameWordList = name.split(" ");
					if (!ValidateUtil.isValidNameLength(nameWordList))
						continue;

					/* populate fullMap */
					for (String word : nameWordList) {
						if (!fullMap.containsKey(word)) {
							Map<String, Float> brandValueMap = new HashMap<String, Float>();
							brandValueMap.put(brand, 1F);
							fullMap.put(word, brandValueMap);
						} else {
							if (fullMap.get(word).get(brand) == null)
								fullMap.get(word).put(brand, 1F);
							else
								fullMap.get(word).put(brand, fullMap.get(word).get(brand) + 1F);
						}
					}

					/* print progress */
					if (++rowCounter % 100000 == 0)
						System.out.println("Brand ML:" + rowCounter);
				}
			} catch (IOException e) {
				e.printStackTrace();
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
		}
		return fullMap;
	}

	@Override
	public String categorize(Map<String, Map<String, Float>> fullMap, String name) {
		/* brand:weight */
		Map<String, Float> resultMap = new HashMap<String, Float>();

		name = TextUtil.normalize(name);

		/* populate resultMap */
		String[] nameWordList = name.split(" ");
		for (String word : nameWordList) {
			Map<String, Float> brandValueMap = fullMap.get(word);
			if (brandValueMap != null) {
				for (Map.Entry<String, Float> i : brandValueMap.entrySet()) {
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
		String brand = "";

		/* categorization: name should contain brand */
		for (int i = 0; i < size * 0.1; i++) {
			String normalizedBrand = TextUtil.normalize(resultArray[i].getName());
			if (TextUtil.containsAllWords(name, normalizedBrand)) {
				brand = resultArray[i].getName();
				break;
			}
		}

		return brand;
	}

}
