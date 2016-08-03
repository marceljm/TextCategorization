package com.marceljm.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marceljm.entity.Category;
import com.marceljm.service.ConstantService;
import com.marceljm.util.CalculatorUtil;
import com.marceljm.util.TextUtil;
import com.marceljm.util.ValidateUtil;

public class Main {

	public static void main(String[] args) throws IOException {
		/* name:category:weight */
		Map<String, Map<String, Float>> fullMap = new HashMap<String, Map<String, Float>>();

		String line;
		String name;
		String path;

		long rowCounter = 0;

		float weight;

		File fileDir = new File(ConstantService.INPUT_FILE);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDir), ConstantService.CHARSET));

		/* read lines */
		while ((line = in.readLine()) != null) {
			if (line.contains(ConstantService.HEADER_SIGNATURE))
				continue;

			name = line.split("\";\"")[1];
			path = line.split("\";\"")[7];

			if (!ValidateUtil.isValidCategory(path.toLowerCase()))
				continue;

			name = TextUtil.normalize(name);
			String[] wordList = name.split(" ");
			if (!ValidateUtil.isValidNameLength(wordList))
				continue;

			/* fill map name:category:weight */
			int wordCounter = 0;
			for (String word : wordList) {
				weight = CalculatorUtil.weight(wordCounter, path.toLowerCase(), word, wordList);

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

			if (rowCounter % 10000 == 0)
				System.out.println(rowCounter);
			rowCounter++;
		}

		/* convert to percentage */
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

		in.close();

		test(fullMap);

		// fillFile(fullMap);
	}

	private static void test(Map<String, Map<String, Float>> fullMap) {
		category(fullMap, "Memória CLP-MEM301/SEE SAMSUNG");
		category(fullMap, "Monitor Profissional LFD 46 Widescreen HDMI 460UTN SAMSUNG");
		category(fullMap, "Placa de Fax SCX-FAX101 para SCX-6345ND SAMSUNG");
		category(fullMap, "Tracionador Bandeja Manual JC96-02682A SAMSUNG");
		category(fullMap, "HD 40GB Para Série ML-4550/ML-HDK210/S SAMSUNG");
		category(fullMap, "Bolsa para Câmera Digital Pouch SQ NOC-222BK SUMDEX");
		category(fullMap, "Capa para Notebook 7-9 G618 Cinza GOLLA");
		category(fullMap, "Suporte de Teto para TV de 15 até 32 N05V2B ELG");
		category(fullMap, "Memória para Impressora 128 MB DDR1 CLP-MEM101/SEE SAMSUNG");
		category(fullMap, "Mouse e Teclado Microsoft 2LF-00023 Wireless");
		category(fullMap, "Teclado Microsoft Multimídia Curve Keyboard Comfort CV3000 - Preto");
		category(fullMap, "Teclado Microsoft All in One Media Wireless - Preto");
		category(fullMap, "Mouse Óptico Microsoft Mobile 1850 Wireless USB 2.0 - Rosa");
		category(fullMap, "Cabo HDMI 2 Metros - Xbox 360");
		category(fullMap, "Jogo Castlevania: Lords Of Shadows Collection - Xbox 360");
		category(fullMap, "Jogo BioShock - Xbox 360");
		category(fullMap, "Jogo The Wolf Among Us - Xbox 360");
		category(fullMap, "Jogo The Evil Within - Xbox 360");
		category(fullMap, "Jogo: Devil May Cry 4 - Xbox 360");
		category(fullMap, "tablet 4gb android 4.2 wi-fi orion small branco spacebr");
	}

	private static void fillFile(Map<String, Map<String, Float>> fullMap) {
		// File fileDir = new File(ConstantService.INPUT_FILE);
		// BufferedReader in = new BufferedReader(
		// new InputStreamReader(new FileInputStream(fileDir),
		// ConstantService.CHARSET));

		// fileDir = new File("resources/megamamute.csv");
		// in = new BufferedReader(new InputStreamReader(new
		// FileInputStream(fileDir), "UTF8"));
		// in.readLine();
		// while ((line = in.readLine()) != null) {
		// name = line.split("\";\"")[1];
		// category(fullMap, name);
		// }
	}

	public static void category(Map<String, Map<String, Float>> completeMap, String name) {
		name = TextUtil.normalize(name);

		Map<String, Float> resultMap = new HashMap<String, Float>();

		String[] wordList = name.split(" ");
		for (String word : wordList) {
			Map<String, Float> subMap = completeMap.get(word);
			if (subMap != null) {
				for (Map.Entry<String, Float> i : subMap.entrySet()) {
					if (resultMap.containsKey(i.getKey())) {
						resultMap.put(i.getKey(), i.getValue() + resultMap.get(i.getKey()));
					} else {
						resultMap.put(i.getKey(), i.getValue());
					}
				}
			}
		}

		List<Category> categoryList = new ArrayList<Category>();
		for (Map.Entry<String, Float> i : resultMap.entrySet()) {
			Category cat = new Category();
			cat.setName(i.getKey());
			cat.setValue(i.getValue());
			categoryList.add(cat);
		}
		Collections.sort(categoryList);
		int size = categoryList.size();
		Category category1 = categoryList.get(size - 1);
		Category category2 = categoryList.get(size - 2);
		Category category3 = categoryList.get(size - 3);

		String finalCategory = category1.getName();
		// if (category1.getValue() - category2.getValue() < 1) {
		if (!category1.getName().toLowerCase().contains(wordList[0])) {
			if (category2.getName().toLowerCase().contains(wordList[0])) {
				finalCategory = category2.getName();
			} else if (category3.getName().toLowerCase().contains(wordList[0])) {
				finalCategory = category3.getName();
			}
			// }
		}
		System.out.println(name + " [" + finalCategory + "]");

		// for (int i = categoryList.size() - 1; i > categoryList.size() - 4;
		// i--) {
		// System.out.println(
		// String.format("%.2f", categoryList.get(i).getValue()) + ":\t" +
		// categoryList.get(i).getName());
		// }

	}

}
