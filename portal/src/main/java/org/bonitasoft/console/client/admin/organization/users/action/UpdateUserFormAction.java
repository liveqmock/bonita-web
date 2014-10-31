/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
 */
package org.bonitasoft.console.client.admin.organization.users.action;

import static org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n._;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.rest.model.identity.UserDefinition;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.toolkit.client.ui.JsId;
import org.bonitasoft.web.toolkit.client.ui.action.Action;
import org.bonitasoft.web.toolkit.client.ui.action.form.UpdateItemWithDeployFormAction;

/**
 * Update a User
 * Check before updating that passwords are matching
 *
 * @author Colin PUY
 */
public class UpdateUserFormAction extends UpdateItemWithDeployFormAction<UserItem> {

    private final List<Action> submitActions = new ArrayList<Action>();

    public UpdateUserFormAction() {
        super(UserDefinition.get(), UserItem.DEPLOY_PROFESSIONAL_DATA, UserItem.DEPLOY_PERSONNAL_DATA);
    }

    @Override
    public void execute() {
        final String password = getParameter(new JsId(UserItem.ATTRIBUTE_PASSWORD));
        final String confirmPassword = getParameter(new JsId(UserItem.ATTRIBUTE_PASSWORD + "_confirm"));

        onError(new Promise() {

            @Override
            public void apply(final String message, final Integer errorCode) {
                getForm().addError(new JsId(UserItem.ATTRIBUTE_PASSWORD), getMessageFromErrorMessage(message));
            }
        });

        if (!areEquals(password, confirmPassword)) {
            getForm().addError(new JsId(UserItem.ATTRIBUTE_PASSWORD + "_confirm"), _("Passwords don't match"));
        } else {
            super.execute();
            for (final Action action : submitActions) {
                action.execute();
            }
        }
    }

    public void onSubmit(final Action action) {
        submitActions.add(action);
    }

    private String getMessageFromErrorMessage(String message) {
        int indexStartMessage = message.indexOf("\"message\"");
        if (indexStartMessage >= 0) {
            indexStartMessage = indexStartMessage + 11;

            message = message.substring(indexStartMessage);
            final int endOfMessage = message.indexOf("\"");
            if (endOfMessage >= 0) {
                message = message.substring(0, endOfMessage);
                message = message.replace("\\r\\n", ", ");
                message = message.substring(0, message.length() - 2);
                return message;
            }
            return message;
        }
        return message;
    }

    private boolean areEquals(final String password1, final String password2) {
        if (password1 == null && password2 == null) {
            return true;
        } else if (password1 == null) {
            return false;
        }
        return password1.equals(password2);
    }
}
