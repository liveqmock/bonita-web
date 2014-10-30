/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/


package org.bonitasoft.web.rest.security

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.session.APISession

/**
 * @author Baptiste Mesta
 */
class ProcessPermissionRule implements PermissionRule {


    @Override
    public boolean check(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();
        if (apiCallContext.getMethod().equals("GET")) {
            return checkGetMethod(apiCallContext, apiAccessor, currentUserId, logger)
        }
        return false
    }


    private boolean checkGetMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def processAPI = apiAccessor.getProcessAPI()
        def filters = apiCallContext.getFilters()
        if (apiCallContext.getResourceId() != null) {
            def processId = Long.valueOf(apiCallContext.getResourceId())
            def processDefinition = processAPI.getProcessDeploymentInfo(processId);
            def deployedByUser = processDefinition.getDeployedBy() == currentUserId
            logger.debug("deployed by the current user? " + deployedByUser)
            return deployedByUser
        } else {
            def stringUserId = String.valueOf(currentUserId)
            if (stringUserId.equals(filters.get("team_manager_id")) || stringUserId.equals(filters.get("supervisor_id")) || stringUserId.equals(filters.get("user_id"))) {
                logger.debug("allowed because searching filters contains user id")
                return true
            }
        }
        return false;
    }
}