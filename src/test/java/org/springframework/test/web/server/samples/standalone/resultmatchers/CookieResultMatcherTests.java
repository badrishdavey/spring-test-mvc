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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.server.setup.MockMvcBuilders.standaloneSetup;

import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.server.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Examples of expectations on response cookies values. 
 * 
 * @author Rossen Stoyanchev
 */
public class CookieResultMatcherTests {

	private MockMvc mockMvc;
	
	@Before
	public void setup() {
		this.mockMvc = standaloneSetup(new SimpleController())
				.addInterceptors(new LocaleChangeInterceptor())
				.setLocaleResolver(new CookieLocaleResolver())
				.build();
	}

	@Test
	public void testEqualTo() throws Exception {
		this.mockMvc.perform(get("/").param("locale", "en_US")).andDo(print())
			.andExpect(cookie().value(CookieLocaleResolver.DEFAULT_COOKIE_NAME, "en_US"));
		
		// Hamcrest matchers...
		this.mockMvc.perform(get("/").param("locale", "en_US"))
			.andExpect(cookie().value(CookieLocaleResolver.DEFAULT_COOKIE_NAME, equalTo("en_US")));
	}
	
	@Test
	public void testMatcher() throws Exception {
		this.mockMvc.perform(get("/").param("locale", "en_US"))
			.andExpect(cookie().value(CookieLocaleResolver.DEFAULT_COOKIE_NAME, startsWith("en")));
	}
	

	@Controller
	@SuppressWarnings("unused")
	private static class SimpleController {
		
		@RequestMapping("/")
		public String home() {
			return "home";
		}
	}
}
