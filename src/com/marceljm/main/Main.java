package com.marceljm.main;

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
import com.marceljm.util.CalculatorUtil;
import com.marceljm.util.TextUtil;
import com.marceljm.util.ValidateUtil;

public class Main {

	public static void main(String[] args) throws IOException {
		/* name:[category:weight] */
		Map<String, Map<String, Float>> fullMap = new HashMap<String, Map<String, Float>>();

		String line;
		String name;
		String path;

		long rowCounter = 0;

		float weight;

		File fileDir = new File(ConstantService.INPUT_FILE);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDir), ConstantService.CHARSET));

		/* read file to learn */
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
			if (rowCounter % 100000 == 0)
				System.out.println(rowCounter);
			rowCounter++;
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

		in.close();

		/* test */
		testWithList(fullMap);
		// testWithFile(fullMap);
	}

	public static void categorize(Map<String, Map<String, Float>> fullMap, String name) {
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
		if (size == 0) {
			System.out.println("Cannot be categorized:" + name);
			return;
		}
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

		/* print result */
		System.out.println(name + " [" + finalCategory + "]");

		printTopRelatedCategories(categoryList);
	}

	private static void printTopRelatedCategories(List<Field> categoryList) {
		for (int i = categoryList.size() - 1; i > categoryList.size() - 4 && i >= 0; i--) {
			System.out.println(
					String.format("%.2f", categoryList.get(i).getValue()) + ":\t" + categoryList.get(i).getName());
		}
	}

	private static void testWithFile(Map<String, Map<String, Float>> fullMap)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		String line;
		String name;
		File fileDir;
		BufferedReader in;
		fileDir = new File(ConstantService.INPUT_TEST_FILE);
		in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), ConstantService.CHARSET));
		in.readLine();
		while ((line = in.readLine()) != null) {
			name = line.split("\";\"")[1];
			categorize(fullMap, name);
		}
		in.close();
	}

	private static void testWithList(Map<String, Map<String, Float>> fullMap) {
		categorize(fullMap, "Memória CLP-MEM301/SEE SAMSUNG");
		categorize(fullMap, "Monitor Profissional LFD 46 Widescreen HDMI 460UTN SAMSUNG");
		categorize(fullMap, "Placa de Fax SCX-FAX101 para SCX-6345ND SAMSUNG");
		categorize(fullMap, "Tracionador Bandeja Manual JC96-02682A SAMSUNG");
		categorize(fullMap, "HD 40GB Para Série ML-4550/ML-HDK210/S SAMSUNG");
		categorize(fullMap, "Bolsa para Câmera Digital Pouch SQ NOC-222BK SUMDEX");
		categorize(fullMap, "Capa para Notebook 7-9 G618 Cinza GOLLA");
		categorize(fullMap, "Suporte de Teto para TV de 15 até 32 N05V2B ELG");
		categorize(fullMap, "Memória para Impressora 128 MB DDR1 CLP-MEM101/SEE SAMSUNG");
		categorize(fullMap, "Mouse e Teclado Microsoft 2LF-00023 Wireless");
		categorize(fullMap, "Teclado Microsoft Multimídia Curve Keyboard Comfort CV3000 - Preto");
		categorize(fullMap, "Teclado Microsoft All in One Media Wireless - Preto");
		categorize(fullMap, "Mouse Óptico Microsoft Mobile 1850 Wireless USB 2.0 - Rosa");
		categorize(fullMap, "Cabo HDMI 2 Metros - Xbox 360");
		categorize(fullMap, "Jogo Castlevania: Lords Of Shadows Collection - Xbox 360");
		categorize(fullMap, "Jogo BioShock - Xbox 360");
		categorize(fullMap, "Jogo The Wolf Among Us - Xbox 360");
		categorize(fullMap, "Jogo The Evil Within - Xbox 360");
		categorize(fullMap, "Jogo: Devil May Cry 4 - Xbox 360");
		categorize(fullMap, "tablet 4gb android 4.2 wi-fi orion small branco spacebr");
	}

}
