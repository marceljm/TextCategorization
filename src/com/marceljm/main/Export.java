package com.marceljm.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.marceljm.service.BrandCategoryMLService;
import com.marceljm.service.BrandMLService;
import com.marceljm.service.CategoryMLService;
import com.marceljm.service.NormalizedCategoryService;
import com.marceljm.service.impl.BrandCategoryMLServiceImpl;
import com.marceljm.service.impl.BrandMLServiceImpl;
import com.marceljm.service.impl.CategoryMLServiceImpl;
import com.marceljm.service.impl.NormalizedCategoryServiceImpl;
import com.marceljm.util.ConstantUtil;

public class Export {

	private static String[] stores = ConstantUtil.STORES;

	public static void main(String[] args) throws IOException {
		generateOutput();
	}

	public static void generateOutput() throws IOException {
		// 0:id
		// 1:name
		// 2:price
		// 3:imageSmall
		// 4:imageMedium
		// 5:imageLarge
		// 6:link
		// 7:path
		// 8:mainCategory
		// 9:subCategory
		// 10:thirdCategory
		// 11:brand
		// 12:store
		String[] field = new String[12];

		String line;

		HashSet<String> namePriceHashSet = new HashSet<String>();

		PrintWriter writer = new PrintWriter(new File(ConstantUtil.OUTPUT_FILE), ConstantUtil.CHARSET);

		NormalizedCategoryService normalizedCategoryService = new NormalizedCategoryServiceImpl();
		Map<String, String> normalizedFirstCategoryMap = normalizedCategoryService.normalizedCategoryMap(0);
		Map<String, String> normalizedSecondCategoryMap = normalizedCategoryService.normalizedCategoryMap(1);
		Map<String, String> normalizedThirdCategoryMap = normalizedCategoryService.normalizedCategoryMap(2);

		BrandMLService brandMachineLearningService = new BrandMLServiceImpl();
		Map<String, Map<String, Float>> brandBase = brandMachineLearningService.knowledgeBase();

		BrandCategoryMLService brandCategoryMLService = new BrandCategoryMLServiceImpl();
		Map<String, Set<String>> brandCategoryBase = brandCategoryMLService.knowledgeBase();

		CategoryMLService machineLearningService = new CategoryMLServiceImpl();
		Map<String, Map<String, Float>> categoryBase = machineLearningService.knowledgeBase();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ConstantUtil.HEADER);

		int bufferCounter = 0;

		for (String store : stores) {

			File fileDir = new File("resources/" + store.split(";")[0] + ".csv");
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileDir), ConstantUtil.CHARSET));

			while ((line = in.readLine()) != null) {
				if (line.contains(ConstantUtil.HEADER_SIGNATURE))
					continue;

				// split line
				field = line.split("\";\"");

				// remove first and last quotation marks
				field[0] = field[0].split("\"")[1];
				field[11] = field[11].substring(0, field[11].length() - 1);

				// skip repeated products
				String value = field[1] + ";" + field[2];
				if (namePriceHashSet.contains(value))
					continue;
				namePriceHashSet.add(value);

				// skip short names
				if (field[1].split(" ").length < 4)
					continue;

				String merge;
				if (store.equals(ConstantUtil.MAIN_STORE))
					merge = field[1] + " " + field[7] + " " + field[11];
				else
					merge = field[1] + " " + field[7] + " " + field[8] + " " + field[9] + " " + field[10] + " "
							+ field[11];

				// categorize
				if (field[11].isEmpty())
					field[11] = brandMachineLearningService.categorize(brandBase, merge);

				if ((field[7].isEmpty() || (!field[7].isEmpty() && !store.equals(ConstantUtil.MAIN_STORE)))
						&& !field[11].isEmpty()) {
					field[7] = machineLearningService.categorize(categoryBase, merge, field[11], brandCategoryBase,
							normalizedFirstCategoryMap, normalizedSecondCategoryMap, normalizedThirdCategoryMap);
				}

				// if (field[11].isEmpty())
				// field[11] = "Outras";

				// skip
				// if (field[7].equals("") ||
				// !ValidateUtil.isValidCategory(field[7].toLowerCase()))
				// continue;

				// if (field[11].equals(""))
				// continue;

				// split path
				// if (!field[0].equals("id")) {
				// String[] categories = field[7].split(" / ");
				// int lenght = categories.length;
				// if (lenght >= 1) {
				// field[8] = categories[0];
				// if (lenght >= 2) {
				// field[9] = categories[1];
				// if (lenght >= 3) {
				// field[10] = categories[2];
				// }
				// }
				// }
				// }

				// fill image field
				// if (field[4].isEmpty()) {
				// if (!field[3].isEmpty())
				// field[4] = field[3];
				// else if (!field[5].isEmpty())
				// field[4] = field[5];
				// field[3] = "";
				// field[5] = "";
				// }

				// fill main, sub and third category
				// String[] category = field[7].split(" / ");
				// for (int i = 0; i < category.length; i++)
				// field[8 + i] = category[i];

				// fill store
				// field[12] = store.split(";")[1];

				// replace ";"
				// if (field[1].contains(";"))
				// field[1] = field[1].replaceAll(";", ",");
				// if (field[11].contains(";"))
				// field[11] = field[11].replaceAll(";", " ");

				// write lines
				// for (int i = 0; i <= 11; i++)
				// stringBuilder.append("\"" + field[i] + "\";");
				// stringBuilder.append("\n");

				// bufferCounter++;
				// if (bufferCounter % 200000 == 0) {
				// writer.write(stringBuilder.toString());
				// stringBuilder.setLength(0);
				// }
			}
			// writer.write(stringBuilder.toString());
			// stringBuilder.setLength(0);
			//
			// in.close();
			//
			// System.out.println(store + ": done!");
		}

		// writer.close();
	}

}
