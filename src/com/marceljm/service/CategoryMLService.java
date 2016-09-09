package com.marceljm.service;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

public interface CategoryMLService {

	public Map<String, Map<String, Float>> knowledgeBase() throws UnsupportedEncodingException, FileNotFoundException;

	public String categorize(Map<String, Map<String, Float>> fullMap, String name, String brand, Map<String, Set<String>> brandCategoryMap, Map<String, String> normalizedCategoryMap);

}
