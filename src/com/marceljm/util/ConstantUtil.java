package com.marceljm.util;

public class ConstantUtil {

	public static final String HEADER_SIGNATURE = "mainCategory";

	public static final String CHARSET = "UTF8";

	public static final String INPUT_FILE = "resources/input.csv";

	public static final String OUTPUT_FILE = "resources/output.csv";

	public static final String INPUT_TEST_FILE = "resources/uncategorized.csv";

	public static final String[] BAD_WORDS = { "[^a-z][0-9]{5,}[^a-z]", " - ", " de ", " para ", " com ", " e ",
			" p\\/ ", " em ", " \\| ", " a ", " \\+ ", " � ", " do ", " \\/ ", " sem ", " da ", " at� ", " c\\/ ",
			" p ", " \\& ", " o ", " na ", " no ", " as ", " os ", " ou ", " c ", " que ", " s ", " 1 ", " 2 ", " 3 ",
			" 4 ", " 5 ", " 6 ", " 7 ", " 8 ", " 9 ", " 10 ", " in ", " ref\\. ", " das ", " dos ", " nas ", " nos ",
			" ref ", " on ", " and ", " mod\\. ", " um ", " uma ", " n ", " for ", " ao ", " , ", " [a-z] ",
			"^[a-z0-9] ", " [a-z]$", "^ac ", "^[ao]s " };

	public static final String[] BAD_CHARS = { "- ", " -", "\\. ", " \\.", ", ", " ,", ";", ":" };

	public static final String[][] PLURALS = { { "multifuncional", "bone " }, { "multifuncionais", "bones " } };

	public static final String[] VALID_CATEGORY_MARK = { "/" };

	public static final String[] INVALID_CATEGORY_MARK = { "inativ", "ativar", " ativa", "integracao sige", "loja tim",
			"reversa" };

	public static final String HEADER = "\"id\";\"name\";\"price\";\"imageSmall\";\"imageMedium\";\"imageLarge\";\"link\";\"path\";\"mainCategory\";\"subCategory\";\"thirdCategory\";\"brand\"\n";

	public static final String MAIN_STORE = "wallmart;Wallmart";

	public static final String[] STORES = { "balao;Bal�o da Inform�tica", "cintylar;Cintylar", "cissa;Cissa Magazine",
			"efacil;Efacil", "eletroshopping;Eletro Shopping", "fastshop;Fastshop", "hp;HP", "insinuante;Insinuante",
			"kangoolu;Kangoolu", "lenovo;Lenovo", "megamamute;Mega Mamute", "microsoft;Microsoft",
			"plaza;MerchandisingPlaza", "ricardoeletro;Ricardo Eletro", "salfer;Salfer", MAIN_STORE };

}
