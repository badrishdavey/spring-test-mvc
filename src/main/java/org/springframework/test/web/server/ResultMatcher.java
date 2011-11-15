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

package org.springframework.test.web.server;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * A contract to match the results of an executed request against some expectation.
 * 
 * <p>See static factory methods in 
 * {@code org.springframework.test.web.server.result.MockMvcResultActions}.
 * 
 * <p>Example, assuming a static import of {@code MockMvcRequestBuilders.*} and
 * {@code MockMvcResultActions.*}:
 * 
 * <pre>
 * mockMvc.perform(get("/form"))
 *   .andExpect(status.isOk())
 *   .andExpect(contentType(MediaType.APPLICATION_JSON));
 * </pre> 
 * 
 * @author Rossen Stoyanchev
 */
public interface ResultMatcher {

	/**
	 * Match the result of an executed Spring MVC request to an expectation.
	 * 
	 * @param request the input request 
	 * @param response the resulting response
	 * @param handler the selected handler, or "null" if no matching handler found
	 * @param interceptors the selected handler interceptors, or "null" if none selected
	 * @param mav the result of the handler invocation, or "null" if view resolution was not required  
	 * @param resolvedException a successfully resolved controller exception, or "null"
	 * 
	 * @throws Exception if a failure occurs while executing the expectation
	 */
	void match(MockHttpServletRequest request, 
			   MockHttpServletResponse response, 
			   Object handler,
			   HandlerInterceptor[] interceptors, 
			   ModelAndView mav, 
			   Exception resolvedException) throws Exception;

}
