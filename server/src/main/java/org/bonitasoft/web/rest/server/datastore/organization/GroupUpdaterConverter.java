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
package org.bonitasoft.web.rest.server.datastore.organization;

import java.util.Map;

import org.bonitasoft.engine.identity.GroupUpdater;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.server.engineclient.GroupEngineClient;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;

/**
 * @author Colin PUY
 */
public class GroupUpdaterConverter {

    private final GroupEngineClient groupEngineClient;

    public GroupUpdaterConverter(final GroupEngineClient groupEngineClient) {
        this.groupEngineClient = groupEngineClient;
    }

    public GroupUpdater convert(final Map<String, String> attributes) {
        final GroupUpdater updater = new GroupUpdater();
        if (attributes.containsKey(GroupItem.ATTRIBUTE_DESCRIPTION)) {
            updater.updateDescription(attributes.get(GroupItem.ATTRIBUTE_DESCRIPTION));
        }
        if (attributes.containsKey(GroupItem.ATTRIBUTE_ICON)) {
            updater.updateIconPath(attributes.get(GroupItem.ATTRIBUTE_ICON));
        }
        if (!MapUtil.isBlank(attributes, GroupItem.ATTRIBUTE_NAME)) {
            updater.updateName(attributes.get(GroupItem.ATTRIBUTE_NAME));
        }
        if (attributes.containsKey(GroupItem.ATTRIBUTE_DISPLAY_NAME)) {
            updater.updateDisplayName(attributes.get(GroupItem.ATTRIBUTE_DISPLAY_NAME));
        }
        if (attributes.containsKey(GroupItem.ATTRIBUTE_PARENT_GROUP_ID)) {
            final String parentGroupPath = getParentGroupPath(attributes.get(GroupItem.ATTRIBUTE_PARENT_GROUP_ID));
            updater.updateParentPath(parentGroupPath);
        }
        return updater;
    }

    private String getParentGroupPath(final String groupId) {
        if (groupId.isEmpty()) {
            return "";
        }
        return groupEngineClient.getPath(groupId);
    }
}
