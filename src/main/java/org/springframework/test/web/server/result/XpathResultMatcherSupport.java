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

import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.server.ResultMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 
 * TODO ...
 *
 * @param <T> the type of the compiled XPath
 *
 * @author Rossen Stoyanchev
 */
public abstract class XpathResultMatcherSupport<T> {

	private final String expression;

	private final T xpath;

	public XpathResultMatcherSupport(String expression, Map<String, String> namespaces, Object ... args) {
		this.expression = String.format(expression, args);
		this.xpath = compileXpath(namespaces);
	}

	protected abstract T compileXpath(Map<String, String> namespaces);
	
	protected String getExpression() {
		return expression;
	}

	protected T getXpath() {
		return xpath;
	}

	/**
	 * Apply the XPath and assert it with the given {@code Matcher<Node>}.
	 */
	public ResultMatcher node(final Matcher<? super Node> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Document document = parseXmlString(response.getContentAsString());
				Node node = evaluateAsNode(document);
				MatcherAssert.assertThat("Xpath: " + XpathResultMatcherSupport.this.expression, node, matcher);
			}
		};
	}

	private Document parseXmlString(String xml) throws Exception  {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		InputSource inputSource = new InputSource(new StringReader(xml));
		Document document = documentBuilder.parse(inputSource);
		return document;
	}
	
	/**
	 * TODO
	 */
	protected abstract Node evaluateAsNode(Document document) throws Exception;

	/**
	 * TODO
	 */
	public ResultMatcher exists() {
		return node(Matchers.notNullValue());
	}

	/**
	 * TODO
	 */
	public ResultMatcher doesNotExist() {
		return node(Matchers.nullValue());
	}
	
	/**
	 * TODO
	 */
	public ResultMatcher nodeCount(final Matcher<Integer> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Document document = parseXmlString(response.getContentAsString());
				NodeList nodeList = evaluateAsNodeSet(document);
				String reason = "nodeCount Xpath: " + XpathResultMatcherSupport.this.expression;
				MatcherAssert.assertThat(reason, nodeList.getLength(), matcher);
			}
		};
	}

	/**
	 * TODO
	 */
	protected abstract NodeList evaluateAsNodeSet(Document document) throws Exception;
	
	/**
	 * TODO
	 */
	public ResultMatcher nodeCount(int count) {
		return nodeCount(Matchers.equalTo(count));
	}
	
	/**
	 * TODO
	 */
	public ResultMatcher string(final Matcher<? super String> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Document document = parseXmlString(response.getContentAsString());
				String result = evaluateAsString(document);
				MatcherAssert.assertThat("Xpath: " + XpathResultMatcherSupport.this.expression, result, matcher);
			}
		};
	}

	/**
	 * TODO
	 */
	protected abstract String evaluateAsString(Document document) throws Exception;

	/**
	 * TODO
	 */
	public ResultMatcher string(String value) {
		return string(Matchers.equalTo(value));
	}

	/**
	 * TODO
	 */
	public ResultMatcher number(final Matcher<? super Double> matcher) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Document document = parseXmlString(response.getContentAsString());
				Double result = evaluateAsNumber(document);
				MatcherAssert.assertThat("Xpath: " + XpathResultMatcherSupport.this.expression, result, matcher);
			}
		};
	}

	/**
	 * TODO
	 */
	protected abstract Double evaluateAsNumber(Document document) throws Exception;

	/**
	 * TODO
	 */
	public ResultMatcher number(Double value) {
		return number(Matchers.equalTo(value));
	}

	/**
	 * TODO
	 */
	public ResultMatcher booleanValue(final Boolean value) {
		return new ResultMatcherAdapter() {
			
			@Override
			public void matchResponse(MockHttpServletResponse response) throws Exception {
				Document document = parseXmlString(response.getContentAsString());
				assertEquals("Xpath:", value, evaluateAsBolean(document));
			}
		};
	}

	/**
	 * TODO
	 */
	protected abstract Boolean evaluateAsBolean(Document document) throws Exception;


}