package com.marceljm.service;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface NormalizedCategoryService {

	public Map<String, String> normalizedCategoryMap() throws UnsupportedEncodingException, FileNotFoundException;

}
