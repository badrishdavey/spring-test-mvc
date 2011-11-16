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

import static org.springframework.test.web.AssertionErrors.assertEquals;

import java.util.Map;

import org.hamcrest.Matcher;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.server.ResultMatcher;

/**
 * The main class use for access to all available {@link ResultMatcher}s.
 * 
 * @author Rossen Stoyanchev
 */
public abstract class MockMvcResultMatchers {

	/**
	 * TODO
	 */
	public static StatusResultMatchers status() {
		return new StatusResultMatchers();
	}

	/**
	 * TODO
	 */
	public static ContentResultMatchers content() {
		return new ContentResultMatchers();
	}
	
	/**
	 * TODO
	 */
	public static JsonPathResultMatcherSupport<?> jsonPath(String expression, Object ... args) {
		return new JsonPathResultMatchers(expression, args);
	}

	/**
	 * TODO
	 */
	public static <T> ResultMatcher jsonPath(String expression, Matcher<T> matcher) {
		JsonPathResultMatchers resultMatchers = new JsonPathResultMatchers(expression);
		return resultMatchers.value(matcher);
	}

	/**
	 * TODO
	 */
	public static XpathResultMatcherSupport<?> xpath(String expression, Object ... args) {
		return new XpathResultMatchers(expression, null, args);
	}

	/**
	 * TODO
	 */
	public static XpathResultMatcherSupport<?> xpath(String expression, Map<String, String> namespaces, Object ... args) {
		return new XpathResultMatchers(expression, namespaces, args);
	}
	
	/**
	 * Assert a redirect was issued to the given URL. 
	 */
	public static ResultMatcher redirectedUrl(final String expectedUrl) {
		return new ResultMatcherAdapter() {
			
			@Override
			protected void matchResponse(MockHttpServletResponse response) {
				assertEquals("Redirected URL", expectedUrl, response.getRedirectedUrl());
			}
		};
	}
	
	/**
	 * Assert the request was forwarded to the given URL.
	 */
	public static ResultMatcher forwardedUrl(final String expectedUrl) {
		return new ResultMatcherAdapter() {
			
			@Override
			protected void matchResponse(MockHttpServletResponse response) {
				assertEquals("Forwarded URL", expectedUrl, response.getForwardedUrl());
			}
		};
	}

	/**
	 * TODO
	 */
	public static HeaderResultMatchers header() {
		return new HeaderResultMatchers();
	}

	/**
	 * TODO
	 */
	public static CookieResultMatchers cookie() {
		return new CookieResultMatchers();
	}
	
	/**
	 * TODO
	 */
	public static HandlerResultMatchers handler() {
		return new HandlerResultMatchers();
	}

	/**
	 * TODO
	 */
	public static ModelResultMatchers model() {
		return new ModelResultMatchers();
	}

	/**
	 * TODO
	 */
	public static ViewResultMatchers view() {
		return new ViewResultMatchers();
	}

	/**
	 * TODO
	 */
	public static FlashAttributeResultMatchers flash() {
		return new FlashAttributeResultMatchers();
	}
	
	/**
	 * TODO
	 */
	public static RequestResultMatchers request() {
		return new RequestResultMatchers();
	}
	
}
