package com.interact.listen.authentication

import grails.plugin.springsecurity.SpringSecurityUtils
import org.apache.log4j.Logger

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse

/**
 * Created by cgeesey on 1/16/2017.
 */
class InternalApiFilter extends GenericFilterBean
{
	@SuppressWarnings('ReturnNullFromCatchBlock')
	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	{
		if (!SecurityContextHolder.context.authentication)
		{
			response.sendError(HttpServletResponse.SC_FORBIDDEN)
			return
		}
		chain.doFilter(request, response)
	}
}
