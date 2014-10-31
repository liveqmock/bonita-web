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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static java.util.Arrays.asList;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_NAME;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_VERSION;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_PROCESS_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.bpm.process.TestProcessConnector;
import org.bonitasoft.test.toolkit.bpm.process.TestProcessConnectorFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem;
import org.bonitasoft.web.rest.server.AbstractConsoleTest;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class APIProcessConnectorDependencyIntegrationTest extends AbstractConsoleTest {

    private APIProcessConnectorDependency apiProcessConnectorDependency;

    @Override
    public void consoleTestSetUp() {
        apiProcessConnectorDependency = new APIProcessConnectorDependency();
        apiProcessConnectorDependency.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/processConnectorDependency"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private Map<String, String> buildFilters(final long processId, final String connectorId, final String connectorVersion) {
        final Map<String, String> filters = new HashMap<String, String>();
        filters.put(ATTRIBUTE_PROCESS_ID, String.valueOf(processId));
        filters.put(ATTRIBUTE_CONNECTOR_NAME, connectorId);
        filters.put(ATTRIBUTE_CONNECTOR_VERSION, connectorVersion);
        return filters;
    }

    @Test
    public void testSearch() {
        final TestProcessConnector defaultConnector = TestProcessConnectorFactory.getDefaultConnector();
        final TestProcess processWithConnector = TestProcessFactory.createProcessWithConnector(defaultConnector);
        final Map<String, String> filters = buildFilters(processWithConnector.getId(), defaultConnector.getId(), defaultConnector.getVersion());

        final ItemSearchResult<ProcessConnectorDependencyItem> search = apiProcessConnectorDependency.runSearch(0, 10, "", null, filters, null, null);

        assertEquals(defaultConnector.getDependencies().get(0), search.getResults().get(0).getFilename());
        assertEquals(defaultConnector.getDependencies().get(1), search.getResults().get(1).getFilename());
        assertEquals(defaultConnector.getDependencies().size(), search.getTotal());
    }

    @Test
    public void testSearchWithDeploys() {
        final TestProcessConnector defaultConnector = TestProcessConnectorFactory.getDefaultConnector();
        final TestProcess processWithConnector = TestProcessFactory.createProcessWithConnector(defaultConnector);
        final Map<String, String> filters = buildFilters(processWithConnector.getId(), defaultConnector.getId(), defaultConnector.getVersion());
        final List<String> deploys = asList(ATTRIBUTE_PROCESS_ID);

        final ItemSearchResult<ProcessConnectorDependencyItem> search = apiProcessConnectorDependency.runSearch(0, 10, "", null, filters, deploys, null);

        for (final ProcessConnectorDependencyItem item : search.getResults()) {
            assertNotNull(item.getProcess());
            assertEquals(processWithConnector.getId(), (long) item.getProcess().getId().toLong());
        }
    }
}
