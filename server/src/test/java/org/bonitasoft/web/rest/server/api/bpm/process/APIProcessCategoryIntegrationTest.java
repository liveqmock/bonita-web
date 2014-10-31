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

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.test.toolkit.bpm.TestCategory;
import org.bonitasoft.test.toolkit.bpm.TestCategoryFactory;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.process.ProcessCategoryItem;
import org.bonitasoft.web.rest.server.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Séverin Moussel
 */
public class APIProcessCategoryIntegrationTest extends AbstractConsoleTest {

    private APIProcessCategory api;

    @Override
    public void consoleTestSetUp() throws Exception {
        api = new APIProcessCategory();
        api.setCaller(getAPICaller(getInitiator().getSession(),
                "API/bpm/processCategory"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void addProcessCategoryTest() {
        // Init
        final TestProcess process = TestProcessFactory.getRandomHumanTaskProcess();
        final TestCategory category = TestCategoryFactory.getRandomCategory();

        // API call
        final ProcessCategoryItem processCategory = new ProcessCategoryItem();
        processCategory.setProcessId(process.getId());
        processCategory.setCategoryId(category.getId());

        api.runAdd(processCategory);

        // Check
        final List<TestCategory> categories = process.getCategories();
        Assert.assertEquals("No categories added", 1, categories.size());

        final Category resultCategory = categories.get(0).getCategory();
        Assert.assertEquals("Wrong category found", category.getCategory().getName(), resultCategory.getName());
        Assert.assertEquals("Wrong category found", category.getCategory().getDescription(), resultCategory.getDescription());
    }

    @Test
    public void deleteProcessCategoryTest() {
        // Init
        final TestProcess process = TestProcessFactory.getRandomHumanTaskProcess();
        final TestCategory category = TestCategoryFactory.getRandomCategory();

        process.addCategory(category.getId());

        // API call
        api.runDelete(Arrays.asList(APIID.makeAPIID(process.getId(), category.getId())));

        // Check
        final List<TestCategory> categories = process.getCategories();
        Assert.assertEquals("No categories deleted", 0, categories.size());

    }

}
