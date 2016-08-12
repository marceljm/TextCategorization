package com.marceljm.service;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface CategoryMachineLearningService {

	public Map<String, Map<String, Float>> categoryKnowledgeBase()
			throws UnsupportedEncodingException, FileNotFoundException;

	public String categorize(Map<String, Map<String, Float>> fullMap, String name);

}
