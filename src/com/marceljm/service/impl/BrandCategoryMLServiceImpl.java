package com.marceljm.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.marceljm.service.BrandCategoryMLService;
import com.marceljm.util.ConstantUtil;
import com.marceljm.util.TextUtil;
import com.marceljm.util.ValidateUtil;

public class BrandCategoryMLServiceImpl implements BrandCategoryMLService {

	private static String[] stores = ConstantUtil.STORES;

	@Override
	public Map<String, Set<String>> knowledgeBase() throws UnsupportedEncodingException, FileNotFoundException {

		/* brand:[category] */
		Map<String, Set<String>> fullMap = new HashMap<String, Set<String>>();

		String line;
		String brand;
		String path;

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

					brand = line.split("\";\"")[11];
					path = line.split("\";\"")[7];

					if (!ValidateUtil.isValidCategory(path.toLowerCase()))
						continue;

					brand = TextUtil.normalize(brand);
					brand = brand.substring(0, brand.length() - 1);
					if (brand.isEmpty())
						continue;

					/* populate fullMap */
					if (fullMap.get(brand) == null) {
						Set<String> categorySet = new HashSet<String>();
						categorySet.add(path);
						fullMap.put(brand, categorySet);
					} else {
						fullMap.get(brand).add(path);
					}

					/* print progress */
					if (++rowCounter % 100000 == 0)
						System.out.println("BrandCategory ML:" + rowCounter);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fullMap;
	}

}
