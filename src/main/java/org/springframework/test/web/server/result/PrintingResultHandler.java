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

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.server.ResultHandler;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * Prints the results of an executed request to an {@link OutputStream}.
 * 
 * @author Rossen Stoyanchev
 */
public class PrintingResultHandler implements ResultHandler {

	private static final int LABEL_WIDTH = 20;
	
	private final OutputStream out;
	
	private PrintStream printer;

	/**
	 * Protected constructor. See {@link MockMvcResultHandlers#print()}.
	 */
	protected PrintingResultHandler(OutputStream outputStream) {
		this.out = outputStream;
	}

	public void handle(MockHttpServletRequest request, 
					  MockHttpServletResponse response, 
					  Object handler,
					  HandlerInterceptor[] interceptors, 
					  ModelAndView mav, 
					  Exception exception) throws Exception {

		String encoding = response.getCharacterEncoding();
		
		this.printer = new PrintStream(this.out, true, 
				(encoding != null) ? encoding : WebUtils.DEFAULT_CHARACTER_ENCODING);

		this.printer.println("-----------------------------------------");
		
		printRequest(request);
		printHandler(handler);
		printResolvedException(exception);
		printModelAndView(mav);
		printResponse(response);

		this.printer.println();
	}

	protected void printRequest(MockHttpServletRequest request) {
		printHeading("HttpServletRequest");
		printValue("HTTP Method", request.getMethod());
		printValue("Request URI", request.getRequestURI());
		printValue("Params", getRequestParams(request));
		printValue("Headers", getRequestHeaders(request));
	}

	private Map<String, Object> getRequestParams(MockHttpServletRequest request) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement(); 
			String[] values = request.getParameterValues(name);
			result.put(name, (values != null) ? Arrays.asList(values) : null);
		}
		return result;
	}
	
	private Map<String, Object> getRequestHeaders(MockHttpServletRequest request) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		Enumeration<?> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			map.put(name, request.getHeader(name));
		}
		return map;
	}
	
	protected void printHeading(String text) {
		this.printer.println();
		this.printer.println(formatLabel(text, LABEL_WIDTH).append(":"));
	}

	protected void printValue(String label, Object value) {
		this.printer.println(formatLabel(label, LABEL_WIDTH).append(" = ").append(value).toString());
	}
	
	private StringBuilder formatLabel(String label, int width) {
		StringBuilder sb = new StringBuilder(label);
		while (sb.length() < width) {
			sb.insert(0, " ");
		}
		return sb;
	}

	/**
	 * Print the selected handler (likely an annotated controller method).
	 */
	protected void printHandler(Object handler) {
		printHeading("Handler");
		if (handler == null) {
			printValue("Type", "null (no matching handler found)");
			printValue("Method", null);
		}
		else {
			if (handler instanceof HandlerMethod) {
				HandlerMethod handlerMethod = (HandlerMethod) handler;
				printValue("Type", handlerMethod.getBeanType().getName());
				printValue("Method", handlerMethod);
			}
			else {
				printValue("Type", handler.getClass().getName());
				printValue("Method", "Unknown");
			}
		}
	}
	
	/**
	 * Print an exception raised in a controller and handled with a HandlerExceptionResolver, if any.
	 */
	protected void printResolvedException(Exception resolvedException) {
		printHeading("Resolved Exception");
		if (resolvedException == null) {
			printValue("Type", "null (not raised)");
		}
		else {
			printValue("Type", resolvedException.getClass().getName());
		}
	}

	/**
	 * Print the Model and view selection, or a brief message if view resolution was not required.
	 */
	protected void printModelAndView(ModelAndView mav) {
		printHeading("ModelAndView");
		if (mav == null) {
			printValue("View", "null (view resolution was not required)");
			printValue("Attributes", "null (view resolution was not required)");
		}
		else {
			printValue("View", mav.isReference() ? mav.getViewName() : mav.getView());
			if (mav.getModel().size() == 0) {
				printValue("Attributes", null);
			}
			for (String name : mav.getModel().keySet()) {
				if (name.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
					continue;
				}
				Object value = mav.getModel().get(name);
				Errors errors = (Errors) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + name);
				printValue("Attribute", name);
				printValue("value", value);
				if (errors != null) {
					printValue("errors", errors.getAllErrors());
				}
			}
		}
	}

	/**
	 * Print the HttpServletResponse.
	 */
	protected void printResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		printHeading("HttpServletResponse");
		printValue("status", response.getStatus());
		printValue("error message", response.getErrorMessage());
		printValue("headers", getHeaders(response));
		printValue("content type", response.getContentType());
		printValue("body", response.getContentAsString());
		printValue("forwarded URL", response.getForwardedUrl());
		printValue("redirected URL", response.getRedirectedUrl());
		printValue("included URLs", response.getIncludedUrls());
		printValue("cookies", getCookies(response));
	}

	private Map<String, String> getHeaders(MockHttpServletResponse response) {
		Map<String, String> headers = new LinkedHashMap<String, String>();
		for (String name : response.getHeaderNames()) {
			headers.put(name, response.getHeader(name));
		}
		return headers;
	}

	private Map<String, String> getCookies(MockHttpServletResponse response) {
		Map<String, String> cookies = new LinkedHashMap<String, String>();
		for (Cookie cookie : response.getCookies()) {
			cookies.put(cookie.getName(), cookie.getValue());
		}
		return cookies;
	}
	
}
