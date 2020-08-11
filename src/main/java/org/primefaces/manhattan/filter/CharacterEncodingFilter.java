/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.manhattan.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class CharacterEncodingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");

		/* PROBLEMA DE CACHE RESOLVIDO */
		HttpServletResponse httpResponse = (HttpServletResponse) resp;
		//httpResponse.setHeader("Cache-Control", "no-cache,no-store,must-revalidate"); // HTTP
																						// 1.1
		//httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0
		//httpResponse.setDateHeader("Expires", 0); // Proxies
		httpResponse.setHeader("Content-Security-Policy", "frame-ancestors 'self'");
		httpResponse.setHeader("X-XSS-Protection", "1; 'mode=block' always");

		chain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void destroy() {

	}

}
