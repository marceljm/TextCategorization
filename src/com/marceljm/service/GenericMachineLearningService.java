package com.marceljm.service;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface GenericMachineLearningService {

	public Map<String, Map<String, Float>> knowledgeBase()
			throws UnsupportedEncodingException, FileNotFoundException;

	public String categorize(Map<String, Map<String, Float>> fullMap, String name);

}
