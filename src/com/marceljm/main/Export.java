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
		String[] fields = new String[11];

		while ((line = in.readLine()) != null) {
			/* remove first and last char */
			line = line.replaceFirst("\"", "");
			line = line.replaceAll("\"$", "");

			fields = line.split("\";\"");
			System.out.println(fields[0]+":"+fields[11]);
		}
	}

}
