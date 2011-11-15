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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.statusReason;
import static org.springframework.test.web.server.setup.MockMvcBuilders.standaloneSetup;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.server.MockMvc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Examples of expectations on the status and the status reason found in the response. 
 * 
 * @author Rossen Stoyanchev
 */
public class StatusResultMatcherTests {

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = standaloneSetup(new StatusController()).build();
	}

	@Test
	public void testStatusInt() throws Exception {
		this.mockMvc.perform(get("/created")).andExpect(status(201));
		this.mockMvc.perform(get("/badRequest")).andExpect(status(400));
	}	

	@Test
	public void testHttpStatus() throws Exception {
		this.mockMvc.perform(get("/created")).andExpect(status().isCreated());
		this.mockMvc.perform(get("/badRequest")).andExpect(status().isBadRequest());
	}	

	@Test
	public void testMatcher() throws Exception {
		this.mockMvc.perform(get("/badRequest"))
			.andExpect(status(allOf(greaterThanOrEqualTo(400), lessThan(500))));
	}	

	@Test
	public void testReasonEqualTo() throws Exception {
		this.mockMvc.perform(get("/badRequest")).andExpect(statusReason("Expired token"));
		
		// Hamcrest matchers...
		this.mockMvc.perform(get("/badRequest")).andExpect(statusReason(equalTo("Expired token")));
	}

	@Test
	public void testReasonMatcher() throws Exception {
		this.mockMvc.perform(get("/badRequest"))
			.andExpect(statusReason(endsWith("token")));
	}
	
	
	@Controller
	@SuppressWarnings("unused")
	private static class StatusController {
		
		@RequestMapping("/created")
		@ResponseStatus(HttpStatus.CREATED)
		public @ResponseBody void created(){
		}

		@RequestMapping("/badRequest")
		@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Expired token")
		public @ResponseBody void badRequest(){
		}

		@RequestMapping("/notImplemented")
		@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
		public @ResponseBody void notImplemented(){
		}
	}
}
