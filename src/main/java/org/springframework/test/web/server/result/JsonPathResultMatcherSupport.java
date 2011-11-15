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

import static org.springframework.test.web.AssertionErrors.assertTrue;

import java.util.List;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.server.ResultMatcher;


/**
 * TODO ...
 * 
 * @param <J> the type of the compiled JsonPath
 *
 * @author Rossen Stoyanchev
 */
public abstract class JsonPathResultMatcherSupport<J> {

	private final String expression;
	
	private final J jsonPath;

	/**
	 * TODO
	 */
	protected JsonPathResultMatcherSupport(String expression, Object ... args) {
		this.expression = String.format(expression, args);
		this.jsonPath = compileJsonPath(this.expression);
	}

	/**
	 * TODO
	 */
	protected String getExpression() {
		return expression;
	}

	/**
	 * TODO
	 */
	protected J getJsonPath() {
		return jsonPath;
	}

	/**
	 * TODO
	 */
	protected abstract J compileJsonPath(String expression);
	
	/**
	 * TODO
	 */
	public <T> ResultMatcher value(final Matcher<T> matcher) {
		return new ResultMatcherAdapter() {
			
			@SuppressWarnings("unchecked")
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				T value = (T) evaluateJsonPath(response.getContentAsString());
				MatcherAssert.assertThat("JSON path: " + expression, value, matcher);
			}
		};
	}
	
	/**
	 * TODO
	 */
	protected abstract Object evaluateJsonPath(String content) throws Exception;

	/**
	 * TODO
	 */
	public ResultMatcher value(Object value) {
		return value(Matchers.equalTo(value));
	}
	
	/**
	 * TODO
	 */
	public ResultMatcher exists() {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Object value = evaluateJsonPath(response.getContentAsString());
				String reason = "No value for JSON path: " + expression;
				assertTrue(reason, value != null);
				if (List.class.isInstance(value)) {
					assertTrue(reason, !((List<?>) value).isEmpty());
				}
			}
		};
	}

	/**
	 * TODO
	 */
	public ResultMatcher doesNotExist() {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Object value = evaluateJsonPath(response.getContentAsString());
				String reason = String.format("Expected no value for JSON path: %s but found: %s", expression, value);
				if (List.class.isInstance(value)) {
					assertTrue(reason, ((List<?>) value).isEmpty());
				}
				else {
					assertTrue(reason, value == null);
				}
			}
		};
	}

}
