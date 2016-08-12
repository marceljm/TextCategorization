package com.marceljm.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

import com.marceljm.service.CategoryMachineLearningService;
import com.marceljm.service.ConstantService;
import com.marceljm.service.impl.CategoryMachineLearningServiceImpl;
import com.marceljm.util.ValidateUtil;

public class Export {

	public static void main(String[] args) throws IOException {
		generateOutput();
	}

	public static void generateOutput() throws IOException {
		PrintWriter writer = new PrintWriter(new File("resources/output.csv"), "UTF-8");

		File fileDir = new File(ConstantService.INPUT_FILE);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDir), ConstantService.CHARSET));

		String line;
		StringBuilder stringBuilder = new StringBuilder();

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
		String[] field = new String[11];

		CategoryMachineLearningService machineLearningService = new CategoryMachineLearningServiceImpl();
		Map<String, Map<String, Float>> categoryBase = machineLearningService.categoryKnowledgeBase();

		while ((line = in.readLine()) != null) {
			// split line
			field = line.split("\";\"");

			// remove first and last quotation marks
			field[0] = field[0].split("\"")[1];
			field[11] = field[11].substring(0, field[11].length() - 1);

			// cateorize
			if (field[7].isEmpty())
				field[7] = machineLearningService.categorize(categoryBase, field[1]);

			// skip
			if (field[7].equals("") || !ValidateUtil.isValidCategory(field[7].toLowerCase()))
				continue;

			// split path
			if (!field[0].equals("id")) {
				String[] categories = field[7].split(" / ");
				int lenght = categories.length;
				if (lenght >= 1) {
					field[8] = categories[0];
					if (lenght >= 2) {
						field[9] = categories[1];
						if (lenght >= 3) {
							field[10] = categories[2];
						}
					}
				}
			}

			// fill image field
			if (field[4].isEmpty()) {
				if (!field[3].isEmpty())
					field[4] = field[3];
				else if (!field[5].isEmpty())
					field[4] = field[5];
				field[3] = "";
				field[5] = "";
			}

			// fill main, sub and third category
			String[] category = field[7].split(" / ");
			for (int i = 0; i < category.length; i++)
				field[8 + i] = category[i];

			// write line
			// stringBuilder.append("\"" + field[0] + "\";");
			stringBuilder.append("\"" + field[1] + "\";");
			// stringBuilder.append("\"" + field[2] + "\";");
			// stringBuilder.append("\"" + field[3] + "\";");
			// stringBuilder.append("\"" + field[4] + "\";");
			// stringBuilder.append("\"" + field[5] + "\";");
			// stringBuilder.append("\"" + field[6] + "\";");
			stringBuilder.append("\"" + field[7] + "\"\n");
			// stringBuilder.append("\"" + field[8] + "\";");
			// stringBuilder.append("\"" + field[9] + "\";");
			// stringBuilder.append("\"" + field[10] + "\";");
			// stringBuilder.append("\"" + field[11] + "\"\n");
		}

		writer.write(stringBuilder.toString());
		writer.close();
		System.out.println("done!");
	}

}
