package com.marceljm.service;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

public interface BrandCategoryMLService {

	public Map<String, Set<String>> knowledgeBase() throws UnsupportedEncodingException, FileNotFoundException;

}
