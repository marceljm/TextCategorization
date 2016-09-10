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

import com.marceljm.service.NormalizedCategoryService;
import com.marceljm.util.ConstantUtil;
import com.marceljm.util.TextUtil;
import com.marceljm.util.ValidateUtil;

public class NormalizedCategoryServiceImpl implements NormalizedCategoryService {

	@Override
	public Map<String, String> normalizedCategoryMap(int position)
			throws UnsupportedEncodingException, FileNotFoundException {

		/* brand:[category] */
		Map<String, String> fullMap = new HashMap<String, String>();

		HashSet<String> namePriceHashSet = new HashSet<String>();

		String line;
		String categoryPath;

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

				categoryPath = line.split("\";\"")[7];

				if (!ValidateUtil.isValidCategory(categoryPath.toLowerCase()))
					continue;

				// split path
				String[] keyArray = new String[3];
				String[] categories = categoryPath.split(" / ");
				int lenght = categories.length;
				if (lenght >= 1) {
					keyArray[0] = categories[0];
					if (lenght >= 2) {
						keyArray[1] = categories[1];
						if (lenght >= 3) {
							keyArray[2] = categories[2];
						}
					}
				}

				String key;
				if (position == 1 && lenght == 3)
					key = keyArray[position] + " " + keyArray[position + 1];
				else
					key = keyArray[position];
				if (key == null)
					continue;

				/* populate fullMap */
				if (fullMap.get(categoryPath) == null) {
					fullMap.put(categoryPath, TextUtil.normalize(key));
				}

				/* print progress */
				if (++rowCounter % 100000 == 0)
					System.out.println(position + "-NormalizedCategory:" + rowCounter);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fullMap;
	}

}