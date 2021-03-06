/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.console.common.server.login.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.session.PlatformSession;

/**
 * @author Ruiheng Fan, Haojie Yuan
 *
 */
public class PlatformLoginServlet extends HttpServlet {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5326931127638029215L;

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(PlatformLoginServlet.class.getName());

    /**
     * the request param for the username
     */
    protected static final String USERNAME_PARAM = "username";

    /**
     * the request param for the password
     */
    protected static final String PASSWORD_PARAM = "password";

    /**
     * login fail message
     */
    protected static final String LOGIN_FAIL_MESSAGE = "loginFailMessage";

    /**
     * the URL of the login page
     */
    protected String LOGIN_PAGE = "/platformLogin.jsp";

    protected String PLATFORM_PAGE = "platform/BonitaPlatform.html#?_p=Platform";

    protected static final String PLATFORMSESSION = "platformSession";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        doPost(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {

        boolean redirectAfterLogin = true;
        final String redirectAfterLoginStr = request.getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        // Do not modify this condition: the redirection should happen unless there is redirect=false in the URL
        if (Boolean.FALSE.toString().equals(redirectAfterLoginStr)) {
            redirectAfterLogin = false;
        }
        PlatformSession platformSession = null;
        PlatformLoginAPI platformLoginAPI = null;
        final String username = request.getParameter(USERNAME_PARAM);
        final String password = request.getParameter(PASSWORD_PARAM);

        try {
            platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
            platformSession = platformLoginAPI.login(username, password);
            request.getSession().setAttribute(PLATFORMSESSION, platformSession);
            if (redirectAfterLogin) {
                response.sendRedirect(PLATFORM_PAGE);
            }
        } catch (final Exception e) {
            final String errorMessage = "Error while logging in to the platform";
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, errorMessage, e);
            }
            if (redirectAfterLogin) {
                try {
                    request.setAttribute(LOGIN_FAIL_MESSAGE, LOGIN_FAIL_MESSAGE);
                    getServletContext().getRequestDispatcher(LOGIN_PAGE).forward(request, response);
                } catch (final IOException ioe) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        final String error = "Error while redirecting to login.jsp";
                        LOGGER.log(Level.SEVERE, error, ioe);
                    }
                    throw new ServletException(errorMessage, e);
                }
            } else {
                throw new ServletException(errorMessage, e);
            }
        }

    }

}
