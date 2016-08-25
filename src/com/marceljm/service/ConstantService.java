package com.marceljm.service;

public class ConstantService {

	public static String HEADER_SIGNATURE = "mainCategory";

	public static String CHARSET = "UTF8";

	public static String INPUT_FILE = "resources/input.csv";

	public static String OUTPUT_FILE = "resources/output.csv";

	public static String INPUT_TEST_FILE = "resources/uncategorized.csv";

	public static String[] BAD_WORDS = { " - ", " de ", " para ", " com ", " e ", " p\\/ ", " em ", " \\| ", " a ",
			" \\+ ", " ç ", " do ", " \\/ ", " sem ", " da ", " até ", " c\\/ ", " p ", " \\& ", " o ", " na ", " no ",
			" as ", " os ", " ou ", " c ", " que ", " s ", " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ",
			" 9 ", " 10 ", " in ", " ref\\. ", " das ", " dos ", " nas ", " nos ", " ref ", " on ", " and ", " mod\\. ",
			" um ", " uma ", " n ", " for ", " ao ", " , " };

	public static String[] BAD_CHARS = { "- ", " -", "\\. ", " \\.", ", ", " ," };

	public static String[][] PLURALS = { { "multifuncional" }, { "multifuncionais" } };

	public static String[] VALID_CATEGORY_MARK = { "/" };

	public static String[] INVALID_CATEGORY_MARK = { "inativ", "ativar", " ativa", "integracao sige", "loja tim",
			"reversa" };

}
