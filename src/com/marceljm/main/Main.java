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

public class Main {

	public static Map<String, Map<String, Float>> completeMap = new HashMap<String, Map<String, Float>>();

	public static String[] badWords = { " - ", " de ", " para ", " com ", " e ", " p\\/ ", " em ", " \\| ", " a ",
			" \\+ ", " ç ", " do ", " \\/ ", " sem ", " da ", " até ", " c\\/ ", " p ", " \\& ", " o ", " na ", " no ",
			" as ", " os ", " ou ", " c ", " que ", " s ", " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ",
			" 9 ", " 10 ", " in ", " ref\\. ", " das ", " dos ", " nas ", " nos ", " ref ", " on ", " and ", " mod\\. ",
			" um ", " uma ", " n ", " for ", " ao ", " , " };

	public static String[] badChars = { "- ", " -", "\\. ", " \\.", ", ", " ," };

	public static String removeBadWords(String name) {
		for (String i : badWords) {
			name = name.replaceAll(i, " ");
		}
		return name;
	}

	public static String removeBadChars(String name) {
		for (String i : badChars)
			name = name.replaceAll(i, " ");
		return name;
	}

	public static String removeDoubleSpace(String name) {
		while (name.contains("  "))
			name = name.replace("  ", " ");
		return name;
	}

	public static String addPluralWords(String name) {
		return name.replaceAll("multifuncional", "multifuncionais");
	}

	public static void main(String[] args) throws IOException {
		File fileDir = new File("resources/product.csv");
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF8"));

		String line;
		String name;
		String path;

		long row = 0;
		in.readLine();
		// Built Map
		while ((line = in.readLine()) != null) {
			// System.out.println(line);

			name = line.split("\";\"")[1];
			path = line.split("\";\"")[7];
			if (!path.contains("/") || path.toLowerCase().contains("inativ") || path.contains("ativar"))
				continue;

			name = name.toLowerCase();
			name = removeBadChars(name);
			name = removeBadWords(name);
			name = removeDoubleSpace(name);
			name = addPluralWords(name);

			float value;
			String[] wordList = name.split(" ");
			if (wordList.length < 3)
				continue;
			int count = 0;
			for (String word : wordList) {
				// define value
				switch (count) {
				case 0:
					if (path.toLowerCase().contains(word))
						value = 2F;
					else
						value = 1F;
					break;
				case 1:
					if (path.toLowerCase().contains(word))
						if (path.toLowerCase().contains(wordList[0])) {
							value = 10F; // bolsa câmera 10
						} else
							value = 1.75F;
					else
						value = 1F;
					break;
				case 2:
					if (path.toLowerCase().contains(word))
						if (path.toLowerCase().contains(wordList[0]) && path.toLowerCase().contains(wordList[1]))
							value = 20F;
						else if (path.toLowerCase().contains(wordList[0]) || path.toLowerCase().contains(wordList[1]))
							value = 10F;
						else
							value = 1.5F;
					else
						value = 1F;
					break;
				default:
					if (path.toLowerCase().contains(word))
						value = 1.25F;
					else
						value = 1F;
				}
				if (completeMap.containsKey(word)) {
					if (completeMap.get(word).get(path) != null) {
						completeMap.get(word).put(path, completeMap.get(word).get(path) + value);
					} else {
						completeMap.get(word).put(path, value);
					}
				} else {
					Map<String, Float> aux = new HashMap<String, Float>();
					aux.put(path, value);
					completeMap.put(word, aux);
				}
				count++;
			}
			row++;
			if (row % 10000 == 0)
				System.out.println(row);
		}

		// Convert Values to %
		for (Map.Entry<String, Map<String, Float>> mainMap : completeMap.entrySet()) {
			Float total = 0F;
			for (Map.Entry<String, Float> subMap : mainMap.getValue().entrySet()) {
				total += subMap.getValue();
			}
			for (Map.Entry<String, Float> subMap : mainMap.getValue().entrySet()) {
				subMap.setValue(subMap.getValue() / total);
			}
		}

		in.close();

		fileDir = new File("resources/megamamute.csv");
		in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF8"));
		in.readLine();
		while ((line = in.readLine()) != null) {
			name = line.split("\";\"")[1];
			category(name);
		}

		category("Memória CLP-MEM301/SEE SAMSUNG");
		category("Monitor Profissional LFD 46 Widescreen HDMI 460UTN SAMSUNG");
		category("Placa de Fax SCX-FAX101 para SCX-6345ND SAMSUNG");
		category("Tracionador Bandeja Manual JC96-02682A SAMSUNG");
		category("HD 40GB Para Série ML-4550/ML-HDK210/S SAMSUNG");
		category("Bolsa para Câmera Digital Pouch SQ NOC-222BK SUMDEX");
		category("Capa para Notebook 7-9 G618 Cinza GOLLA");
		category("Suporte de Teto para TV de 15 até 32 N05V2B ELG");
		category("Memória para Impressora 128 MB DDR1 CLP-MEM101/SEE SAMSUNG");
		category("Mouse e Teclado Microsoft 2LF-00023 Wireless");
		category("Teclado Microsoft Multimídia Curve Keyboard Comfort CV3000 - Preto");
		category("Teclado Microsoft All in One Media Wireless - Preto");
		category("Mouse Óptico Microsoft Mobile 1850 Wireless USB 2.0 - Rosa");
		category("Cabo HDMI 2 Metros - Xbox 360");
		category("Jogo Castlevania: Lords Of Shadows Collection - Xbox 360");
		category("Jogo BioShock - Xbox 360");
		category("Jogo The Wolf Among Us - Xbox 360");
		category("Jogo The Evil Within - Xbox 360");
		category("Jogo: Devil May Cry 4 - Xbox 360");
		category("tablet 4gb android 4.2 wi-fi orion small branco spacebr");
	}

	public static void category(String name) {
		name = name.toLowerCase();
		name = removeBadChars(name);
		name = removeBadWords(name);
		name = removeDoubleSpace(name);
		name = addPluralWords(name);

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
