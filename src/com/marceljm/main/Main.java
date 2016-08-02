package com.marceljm.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Main {

	public static Map<String, Map<String, Float>> map = new HashMap<String, Map<String, Float>>();

	public static String[] badWords = { " - ", " de ", " para ", " com ", " e ", " p\\/ ", " em ", " \\| ", " a ",
			" \\+ ", " ç ", " do", " \\/ ", " sem ", " da ", " até ", " c\\/ ", " p ", " \\& ", " o ", " na ", " no ",
			" as ", " os ", " ou ", " c ", " que ", " s ", " 1 ", " 2", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ", " 9 ",
			" 10 ", " in ", " ref\\. ", " das ", " dos ", " nas ", " nos ", " ref ", " on ", " and ", " mod\\. ",
			" um ", " uma ", " n ", " for ", " ao ", " , ", " série ", " the " };

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

			name = name.toLowerCase();
			name = removeBadChars(name);
			name = removeBadWords(name);
			name = removeDoubleSpace(name);

			path = path.toLowerCase();

			String[] wordList = name.split(" ");
			for (String word : wordList) {
				if (map.containsKey(word)) {
					if (map.get(word).get(path) != null) {
						float count = map.get(word).get(path) + 1F;
						map.get(word).put(path, count);
					} else {
						map.get(word).put(path, 1F);
					}
				} else {
					Map<String, Float> aux = new HashMap<String, Float>();
					aux.put(path, 1F);
					map.put(word, aux);
				}
			}
			row++;
			if (row % 10000 == 0)
				System.out.println(row);
		}

		// Convert Values to %
		for (Map.Entry<String, Map<String, Float>> entry : map.entrySet()) {
			Float total = 0F;
			for (Map.Entry<String, Float> entry2 : entry.getValue().entrySet()) {
				total += entry2.getValue();
			}
			for (Map.Entry<String, Float> entry2 : entry.getValue().entrySet()) {
				entry2.setValue(entry2.getValue() / total);
			}
		}

		in.close();

		category("Memória CLP-MEM301/SEE SAMSUNG");
		category("Monitor Profissional LFD 46 Widescreen HDMI 460UTN  SAMSUNG");
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

	}

	public static String category(String name) {
		name = name.toLowerCase();
		name = removeBadChars(name);
		name = removeBadWords(name);
		name = removeDoubleSpace(name);

		Map<String, Float> resultMap = new HashMap<String, Float>();
		String[] wordList = name.split(" ");
		for (String word : wordList) {
			Map<String, Float> wordMap = map.get(word);
			if (wordMap != null) {
				for (Map.Entry<String, Float> entry : wordMap.entrySet()) {
					if (resultMap.containsKey(entry.getKey())) {
						resultMap.put(entry.getKey(), entry.getValue() + resultMap.get(entry.getKey()));
					} else {
						resultMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}

		Float fMax = 0F;
		String sMax = "";
		Float fMax2 = 0F;
		String sMax2 = "";
		for (Map.Entry<String, Float> entry : resultMap.entrySet()) {
			if (entry.getValue() > fMax) {
				fMax2 = fMax;
				sMax2 = sMax;
				fMax = entry.getValue();
				sMax = entry.getKey();
			}
		}
		System.out.println("-------------------");
		System.out.println(name);
		System.out.println(String.format("%.2f", fMax));
		System.out.println(sMax);
		System.out.println(String.format("%.2f", fMax2));
		System.out.println(sMax2);
		return null;
	}

}
