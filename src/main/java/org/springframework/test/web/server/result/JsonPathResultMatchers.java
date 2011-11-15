/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.web.server.result;

import com.jayway.jsonpath.JsonPath;

/**
 * TODO
 * 
 * <a href="http://code.google.com/p/json-path"/>JsonPath</a>
 * 
 * @author Rossen Stoyanchev
 */
public class JsonPathResultMatchers extends JsonPathResultMatcherSupport<JsonPath> {

	/**
	 * TODO
	 * 
	 * @param expression the JSON path to use in result matchers
	 * @param args TODO
	 */
	protected JsonPathResultMatchers(String expression, Object ... args) {
		super(expression, args);
	}

	/**
	 * 
	 */
	protected JsonPath compileJsonPath(String expression) {
		return com.jayway.jsonpath.JsonPath.compile(expression);
	}
	
	/**
	 * Evaluate the JSON path against the given content.
	 */
	protected Object evaluateJsonPath(String content) throws Exception {
		return getJsonPath().read(content);
	}

}
