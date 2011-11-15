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

package org.springframework.test.web.server.samples.standalone.resultmatchers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.characterEncoding;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.contentType;
import static org.springframework.test.web.server.setup.MockMvcBuilders.standaloneSetup;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.server.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Examples of expectations on the content, content type, and the character encoding of the response. 
 * 
 * @author Rossen Stoyanchev
 * 
 * @see JsonPathResultMatcherTests
 * @see XpathResultMatcherTests
 * @see XmlContentResultMatcherTests
 */
public class ContentResultMatcherTests {

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = standaloneSetup(new SimpleController()).build();
	}
	
	@Test
	public void testContentType() throws Exception {
		this.mockMvc.perform(get("/handle"))
			.andExpect(contentType(MediaType.TEXT_PLAIN))
			.andExpect(contentType("text/plain"));
		
		this.mockMvc.perform(get("/handleUtf8"))
			.andExpect(contentType(MediaType.valueOf("text/plain")))
			.andExpect(contentType("text/plain"));
	}
	
	@Test
	public void testContentEqualTo() throws Exception {
		this.mockMvc.perform(get("/handle")).andExpect(content("Hello world!"));
		this.mockMvc.perform(get("/handleUtf8")).andExpect(content("こんにちは世界！"));
		
		// Hamcrest matchers...
		this.mockMvc.perform(get("/handle")).andExpect(content(equalTo("Hello world!")));
		this.mockMvc.perform(get("/handleUtf8")).andExpect(content(equalTo("こんにちは世界！")));
	}

	@Test
	public void testContentMatcher() throws Exception {
		this.mockMvc.perform(get("/handle")).andExpect(content(containsString("world")));
	}

	@Test
	public void testCharacterEncodingEqualTo() throws Exception {
		this.mockMvc.perform(get("/handle")).andExpect(characterEncoding("ISO-8859-1"));
		this.mockMvc.perform(get("/handleUtf8")).andExpect(characterEncoding("UTF-8"));
	}
	
	
	@Controller
	@SuppressWarnings("unused")
	private static class SimpleController {

		@RequestMapping(value="/handle", produces="text/plain")
		@ResponseBody
		public String handle() {
			return "Hello world!";
		}

		@RequestMapping(value="/handleUtf8", produces="text/plain;charset=UTF-8")
		@ResponseBody
		public String handleWithCharset() {
			return "こんにちは世界！";	// "Hello world! (Japanese)
		}
	}
}
