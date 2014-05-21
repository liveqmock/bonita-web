/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.console.client.javascript.libs;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.TextResource;

/**
 * @author Vincent Elcrin
 * 
 */
public interface JQueryRes extends ResourceClientBunble {

    JQueryRes INSTANCE = GWT.create(JQueryRes.class);

    @Source(RESOURCES_PATH + "scripts/jquery/jquery-1.6.4.js")
    TextResource core();

    @Source(RESOURCES_PATH + "scripts/jquery/jquery-ui-1.10.3.custom.min.js")
    TextResource ui();

}
