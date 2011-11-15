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


import java.util.Collections;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.test.web.server.ResultMatcher;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides methods for XPath-based {@link ResultMatcher} instances.
 * 
 * @author Rossen Stoyanchev
 */
public class XpathResultMatchers extends XpathResultMatcherSupport<XPath> {
	
	/**
	 * Protected constructor.
	 * @param expression the XPath expression to use
	 * @param namespaces namespaces used in the XPath expression, or {@code null}
	 * @param args TODO
	 * 
	 * @see  .. TODO
	 */
	protected XpathResultMatchers(String expression, Map<String, String> namespaces, Object ... args) {
		super(expression, namespaces, args);
	}
	
	@Override
	protected XPath compileXpath(Map<String, String> namespaces) {
		SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
		namespaceContext.setBindings((namespaces != null) ? namespaces : Collections.<String, String> emptyMap());
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(namespaceContext);
		return xpath;
	}

	protected Node evaluateAsNode(Document document) throws Exception {
		return (Node) getXpath().evaluate(getExpression(), document, XPathConstants.NODE);
	}
	
	protected String evaluateAsString(Document document) throws Exception {
		return (String) getXpath().evaluate(getExpression(), document, XPathConstants.STRING);
	}

	protected Double evaluateAsNumber(Document document) throws Exception {
		return (Double) getXpath().evaluate(getExpression(), document, XPathConstants.NUMBER);
	}
	
	protected Boolean evaluateAsBolean(Document document) throws Exception {
		return Boolean.parseBoolean(evaluateAsString(document));
	}
	
	protected NodeList evaluateAsNodeSet(Document document) throws Exception {
		return (NodeList) getXpath().evaluate(getExpression(), document, XPathConstants.NODESET);
	}
	
}
