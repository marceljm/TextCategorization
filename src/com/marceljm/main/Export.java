package com.marceljm.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.marceljm.service.ConstantService;

public class Export {

	public static void main(String[] args) throws IOException {
		output();
	}

	public static void output() throws IOException {
		PrintWriter writer = new PrintWriter(new File("resources/output.csv"));

		File fileDir = new File(ConstantService.INPUT_FILE);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileDir), ConstantService.CHARSET));

		String line;
		StringBuilder stringBuilder = new StringBuilder();
		String[] field = new String[11];

		while ((line = in.readLine()) != null) {
			field = line.split("\";\"");
			
			/* remove first and last char */
			field[0] = field[0].substring(1, field[0].length());
			field[11] = field[11].substring(0, field[11].length() - 1);
		}
	}

}
