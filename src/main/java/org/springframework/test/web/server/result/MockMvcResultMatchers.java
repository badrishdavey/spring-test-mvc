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
import static org.springframework.test.web.AssertionErrors.assertTrue;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.AssertionErrors;
import org.springframework.test.web.server.ResultMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


/**
 * A central class for access to all available actions such expectations that 
 * can be defined for an executed Spring MVC request. 
 * 
 * @author Rossen Stoyanchev
 */
public abstract class MockMvcResultMatchers {

	/**
	 * Assert the response status code with the given matcher.
	 */
	public static ResultMatcher status(final Matcher<Integer> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				MatcherAssert.assertThat("Status: ", response.getStatus(), matcher);
			}
		};
	}

	/**
	 * Assert the response status code with the given matcher.
	 */
	public static ResultMatcher status(int status) {
		return status(Matchers.equalTo(status));
	}

	/**
	 * TODO
	 */
	public static StatusResultMatchers status() {
		return new StatusResultMatchers();
	}
	
	/**
	 * Assert the response status code with the given matcher.
	 */
	public static ResultMatcher statusReason(final Matcher<? super String> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				MatcherAssert.assertThat("Status reason: ", response.getErrorMessage(), matcher);
			}
		};
	}
	
	/**
	 * Assert the response reason with the given matcher.
	 * @see HttpServletResponse#sendError(int, String)
	 */
	public static ResultMatcher statusReason(String reason) {
		return statusReason(Matchers.equalTo(reason));
	}
	
	/**
	 * Assert the content type of the response.
	 */
	public static ResultMatcher contentType(final String contentType) {
		return contentType(MediaType.parseMediaType(contentType));
	}
	
	/**
	 * Assert the content type of the response with the given MediaType. 
	 */
	public static ResultMatcher contentType(final MediaType contentType) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				String actual = response.getContentType();
				assertTrue("Content type not set", actual != null);
				assertEquals("Content type", contentType, MediaType.parseMediaType(actual));
			}
		};
	}

	/**
	 * Assert the character encoding of the response.
	 * @see HttpServletResponse#getCharacterEncoding()
	 */
	public static ResultMatcher characterEncoding(final String characterEncoding) {
		return new ResultMatcherAdapter() {
			public void matchResponse(MockHttpServletResponse response) {
				String actual = response.getCharacterEncoding();
				assertEquals("Character encoding", characterEncoding, actual);
			}
		};
	}
	
	/**
	 * Apply a {@link Matcher} to the response content. For example:
	 * <pre>
	 * mockMvc.perform(get("/path"))
	 *   .andExpect(content(containsString("text")));
	 * </pre>
	 */
	public static ResultMatcher content(final Matcher<? super String> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				MatcherAssert.assertThat("Response content", response.getContentAsString(), matcher);
			}
		};
	}

	/**
	 * TODO
	 */
	public static ResultMatcher content(String content) {
		return content(Matchers.equalTo(content));
	}

	/**
	 * Parse the response content and the given string as XML and assert the 
	 * two are "similar" - i.e. they contain the same elements and attributes
	 * regardless of order.
	 * <p>Use of this matcher requires the 
	 * <a href="http://xmlunit.sourceforge.net/">XMLUnit<a/> library.
	 * 
	 * @see #xpath(String, Object...)
	 * @see #xpath(String, Map, Object...)
	 */
	public static ResultMatcher contentXml(final String xmlContent) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Document control = XMLUnit.buildControlDocument(xmlContent);
				Document test = XMLUnit.buildTestDocument(response.getContentAsString());
				Diff diff = new Diff(control, test);
				if (!diff.similar()) {
					AssertionErrors.fail("Response content, " + diff.toString());
		        }				
			}
		};
	}
	
	// TODO: XML validation
	
	/**
	 * Parse the content as {@link Node} and apply a {@link Matcher}.
	 * @see org.hamcrest.Matchers#hasXPath
	 */
	public static ResultMatcher contentNode(final Matcher<? super Node> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Document document = parseXmlString(response.getContentAsString());
				MatcherAssert.assertThat("Response content", document, matcher);
			}
		};
	}

	private static Document parseXmlString(String xml) throws Exception  {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		InputSource inputSource = new InputSource(new StringReader(xml));
		Document document = documentBuilder.parse(inputSource);
		return document;
	}
	
	/**
	 * Parse the content as {@link DOMSource} and apply a {@link Matcher}.
	 * @see <a href="http://code.google.com/p/xml-matchers/">xml-matchers</a> 
	 */
	public static ResultMatcher contentSource(final Matcher<? super Source> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Document document = parseXmlString(response.getContentAsString());
				MatcherAssert.assertThat("Response content", new DOMSource(document), matcher);
			}
		};
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
	 * Assert a response header with the given {@link Matcher}.
	 */
	public static ResultMatcher header(final String name, final Matcher<? super String> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			protected void matchResponse(MockHttpServletResponse response) {
				MatcherAssert.assertThat("Response header", response.getHeader(name), matcher);
			}
		};
	}
	
	/**
	 * TODO
	 */
	public static ResultMatcher header(final String name, final String value) {
		return header(name, Matchers.equalTo(value));
	}

	/**
	 * Assert a cookie value with a {@link Matcher}.
	 */
	public static ResultMatcher cookieValue(final String name, final Matcher<? super String> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			protected void matchResponse(MockHttpServletResponse response) {
				Cookie cookie = response.getCookie(name);
				assertTrue("Response cookie not found: " + name, cookie != null);
				MatcherAssert.assertThat("Response cookie", cookie.getValue(), matcher);
			}
		};
	}

	/**
	 * TODO
	 */
	public static ResultMatcher cookieValue(final String name, final String value) {
		return cookieValue(name, Matchers.equalTo(value));
	}
	
	/**
	 * TODO
	 */
	public static ResultMatcher handlerType(final Class<?> type) {
		return new ResultMatcherAdapter() {

			@Override
			protected void matchHandler(Object handler) throws Exception {
				assertTrue("No handler: ", handler != null);
				Class<?> actual = handler.getClass();
				if (HandlerMethod.class.isInstance(handler)) {
					actual = ((HandlerMethod) handler).getBeanType();
				}
				assertEquals("Handler type", type, ClassUtils.getUserClass(actual));
			}
		};
	}
	
	/**
	 * TODO
	 */
	public static ResultMatcher handlerMethodName(final Matcher<? super String> matcher) {
		return new ResultMatcherAdapter() {

			@Override
			protected void matchHandler(Object handler) throws Exception {
				assertTrue("No handler: ", handler != null);
				assertTrue("Not a HandlerMethod: " + handler, HandlerMethod.class.isInstance(handler));
				MatcherAssert.assertThat("HandlerMethod", ((HandlerMethod) handler).getMethod().getName(), matcher);
			}
		};
	}
	
	/**
	 * TODO
	 */
	public static ResultMatcher handlerMethodName(final String name) {
		return handlerMethodName(Matchers.equalTo(name));
	}

	/**
	 * TODO
	 */
	public static ResultMatcher handlerMethod(final Method method) {
		return new ResultMatcherAdapter() {

			@Override
			protected void matchHandler(Object handler) throws Exception {
				assertTrue("No handler: ", handler != null);
				assertTrue("Not a HandlerMethod: " + handler, HandlerMethod.class.isInstance(handler));
				assertEquals("HandlerMethod", method, ((HandlerMethod) handler).getMethod());
			}
		};
	}
	
	/**
	 * TODO
	 */
	public static <T> ResultMatcher modelAttribute(final String name, final Matcher<T> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override 
			@SuppressWarnings("unchecked")
			protected void matchModelAndView(ModelAndView mav) throws Exception {
				assertTrue("No ModelAndView found", mav != null);
				MatcherAssert.assertThat("Model attribute", (T) mav.getModel().get(name), matcher);
			}
		};
	}
	
	/**
	 * Syntactic sugar, equivalent to:
	 * <pre>
	 * modelAttribute("attrName", equalTo("attrValue"))
	 * </pre>
	 */
	public static ResultMatcher modelAttribute(String name, Object value) {
		return modelAttribute(name, Matchers.equalTo(value));
	}
	
	/**
	 * Syntactic sugar, equivalent to:
	 * <pre>
	 * modelAttribute("attrName", notNullValue())
	 * </pre>
	 */
	public static ResultMatcher modelAttributeExists(final String... names) {
		return new ResultMatcherAdapter() {
			
			@Override 
			protected void matchModelAndView(ModelAndView mav) throws Exception {
				assertTrue("No ModelAndView found", mav != null);
				for (String name : names) {
					modelAttribute(name, Matchers.notNullValue());
				}
			}
		};
	}

	/**
	 * TODO
	 */
	public static <T> ResultMatcher modelAttributeHasErrors(final String... names) {
		return new ResultMatcherAdapter() {
			
			@Override 
			protected void matchModelAndView(ModelAndView mav) throws Exception {
				assertTrue("No ModelAndView found", mav != null);
				for (String name : names) {
					BindingResult result = (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + name);
					assertTrue("No BindingResult for attribute: " + name, result != null);
					assertTrue("No errors for attribute: " + name, result.hasErrors());
				}
			}
		};
	}

	/**
	 * TODO
	 */
	public static <T> ResultMatcher modelHasNoErrors() {
		return new ResultMatcherAdapter() {
			
			@Override 
			protected void matchModelAndView(ModelAndView mav) throws Exception {
				assertTrue("No ModelAndView found", mav != null);
				for (Object value : mav.getModel().values()) {
					if (value instanceof BindingResult) {
						assertTrue("Unexpected binding error(s): " + value, !((BindingResult) value).hasErrors());
					}
				}
			}
		};
	}
	
	/**
	 * Assert the number of attributes excluding BindingResult instances.
	 */
	public static <T> ResultMatcher modelSize(final int size) {
		return new ResultMatcherAdapter() {
			
			@Override 
			protected void matchModelAndView(ModelAndView mav) throws Exception {
				AssertionErrors.assertTrue("No ModelAndView found", mav != null);
				int actual = 0;
				for (String key : mav.getModel().keySet()) {
					if (!key.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
						actual++;
					}
				}
				assertEquals("Model size", size, actual);
			}
		};
	}

	/**
	 * TODO
	 */
	public static ResultMatcher viewName(final Matcher<? super String> matcher) {
		return new ResultMatcherAdapter() {

			@Override
			protected void matchModelAndView(ModelAndView mav) throws Exception {
				assertTrue("No ModelAndView found", mav != null);
				MatcherAssert.assertThat("View name", mav.getViewName(), matcher);
			}
		};
	}

	/**
	 * TODO
	 */
	public static ResultMatcher viewName(final String name) {
		return viewName(Matchers.equalTo(name));
	}

	/**
	 * TODO
	 */
	public static <T> ResultMatcher flashAttribute(final String name, final Matcher<T> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			@SuppressWarnings("unchecked")
			protected void matchFlashMap(FlashMap flashMap) throws Exception {
				MatcherAssert.assertThat("Flash attribute", (T) flashMap.get(name), matcher);
			}
		};
	}
	
	/**
	 * Syntactic sugar, equivalent to:
	 * <pre>
	 * flashAttribute("attrName", equalTo("attrValue"))
	 * </pre>
	 */
	public static <T> ResultMatcher flashAttribute(final String name, final Object value) {
		return new ResultMatcherAdapter() {
			
			@Override
			protected void matchFlashMap(FlashMap flashMap) throws Exception {
				flashAttribute(name, Matchers.equalTo(value));
			}
		};
	}
	
	/**
	 * Syntactic sugar, equivalent to:
	 * <pre>
	 * flashAttribute("attrName", notNullValue())
	 * </pre>
	 */
	public static <T> ResultMatcher flashAttributeExists(final String... names) {
		return new ResultMatcherAdapter() {
			
			@Override
			protected void matchFlashMap(FlashMap flashMap) throws Exception {
				for (String name : names) {
					flashAttribute(name, Matchers.notNullValue());
				}
			}
		};
	}	

	/**
	 * TODO
	 */
	public static <T> ResultMatcher flashAttributeCount(final int count) {
		return new ResultMatcherAdapter() {
			
			@Override
			protected void matchFlashMap(FlashMap flashMap) throws Exception {
				assertEquals("FlashMap size", count, flashMap.size());
			}
		};
	}	
	
	/**
	 * TODO
	 */
	public static <T> ResultMatcher requestAttribute(final String name, final Matcher<T> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			@SuppressWarnings("unchecked")
			public void matchRequest(MockHttpServletRequest request) {
				T value = (T) request.getAttribute(name);
				MatcherAssert.assertThat("Request attribute: ", value, matcher);
			}
		};
	}

	/**
	 * TODO
	 */
	public static <T> ResultMatcher requestAttribute(String name, Object value) {
		return requestAttribute(name, Matchers.equalTo(value));
	}
	
	/**
	 * TODO
	 */
	public static <T> ResultMatcher sessionAttribute(final String name, final Matcher<T> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			@SuppressWarnings("unchecked")
			public void matchRequest(MockHttpServletRequest request) {
				T value = (T) request.getSession().getAttribute(name);
				MatcherAssert.assertThat("Request attribute: ", value, matcher);
			}
		};
	}

	/**
	 * TODO
	 */
	public static <T> ResultMatcher sessionAttribute(String name, Object value) {
		return sessionAttribute(name, Matchers.equalTo(value));
	}
	
}
