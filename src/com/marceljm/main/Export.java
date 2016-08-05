package com.marceljm.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

import com.marceljm.service.ConstantService;
import com.marceljm.service.MachineLearningService;
import com.marceljm.service.impl.MachineLearningServiceImpl;

public class Export {

	public static void main(String[] args) throws IOException {
		generateOutput();
	}

	public static void generateOutput() throws IOException {
		PrintWriter writer = new PrintWriter(new File("resources/output.csv"));

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

		MachineLearningService machineLearningService = new MachineLearningServiceImpl();
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

		}
	}

}
