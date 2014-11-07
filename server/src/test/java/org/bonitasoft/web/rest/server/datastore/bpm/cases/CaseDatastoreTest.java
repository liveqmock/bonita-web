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
 */
package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessInstanceImpl;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class CaseDatastoreTest {

    Logger logger = LoggerFactory.getLogger(CaseDatastoreTest.class);

    private CaseDatastore caseDatastore;

    @Mock
    private ProcessAPI processAPI;

    @Before
    public void setUp() throws Exception {
        caseDatastore = spy(new CaseDatastore(mock(APISession.class)));
        doReturn(processAPI).when(caseDatastore).getProcessAPI();
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_search_process_instances_and_convert_them_to_CaseItem() throws SearchException {
        // Given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_ID;
        final Map<String, String> filters = Collections.emptyMap();

        final ProcessInstance processInstance = new ProcessInstanceImpl("name");
        doReturn(new SearchResultImpl<ProcessInstance>(1L, Arrays.asList(processInstance))).when(processAPI).searchProcessInstances(
                any(SearchOptions.class));
        final CaseItem caseItem = new CaseItem();
        doReturn(caseItem).when(caseDatastore).convertEngineToConsoleItem(processInstance);

        // When
        final ItemSearchResult<CaseItem> result = caseDatastore.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(processAPI).searchProcessInstances(any(SearchOptions.class));
        verify(caseDatastore).addCallerFilterToSearchBuilderIfNecessary(eq(filters), any(SearchOptionsBuilder.class));
        final List<CaseItem> caseItems = result.getResults();
        assertEquals(1, caseItems.size());
        assertEquals(caseItem, caseItems.get(0));
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_throw_an_exception_when_search_process_instances_failed() throws SearchException {
        // Given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = Collections.singletonMap(CaseItem.FILTER_STATE, "started");

        doThrow(new SearchException(new Exception("toto"))).when(processAPI).searchProcessInstances(any(SearchOptions.class));

        try {
            // When
            caseDatastore.search(page, resultsByPage, search, orders, filters);
        } catch (final APIException e) {
            // Then
            assertTrue(e.getCause() instanceof SearchException);
        } finally {
            // Then
            verify(processAPI).searchProcessInstances(any(SearchOptions.class));
            verify(caseDatastore).addCallerFilterToSearchBuilderIfNecessary(eq(filters), any(SearchOptionsBuilder.class));
        }
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_search_failed_process_instances_and_convert_them_to_CaseItem_when_filter_on_failed_state() throws SearchException {
        search_should_search_failed_process_instances_and_convert_them_to_CaseItem_when_filter_on_state("failed");
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_throw_an_exception_when_search_failed_process_instances_failed_and_filter_on_failed_state() throws SearchException {
        search_should_throw_an_exception_when_search_failed_process_instances_failed_and_filter_on_state("failed");
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_search_failed_process_instances_and_convert_them_to_CaseItem_when_filter_on_error_state() throws SearchException {
        search_should_search_failed_process_instances_and_convert_them_to_CaseItem_when_filter_on_state("error");
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_throw_an_exception_when_search_failed_process_instances_failed_and_filter_on_error_state() throws SearchException {
        search_should_throw_an_exception_when_search_failed_process_instances_failed_and_filter_on_state("error");
    }

    private void search_should_search_failed_process_instances_and_convert_them_to_CaseItem_when_filter_on_state(final String state) throws SearchException {
        // Given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_ID;
        final Map<String, String> filters = Collections.singletonMap(CaseItem.FILTER_STATE, state);

        final ProcessInstance processInstance = new ProcessInstanceImpl("name");
        doReturn(new SearchResultImpl<ProcessInstance>(1L, Arrays.asList(processInstance))).when(processAPI).searchFailedProcessInstances(
                any(SearchOptions.class));
        final CaseItem caseItem = new CaseItem();
        doReturn(caseItem).when(caseDatastore).convertEngineToConsoleItem(processInstance);

        // When
        final ItemSearchResult<CaseItem> result = caseDatastore.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(processAPI).searchFailedProcessInstances(any(SearchOptions.class));
        verify(caseDatastore).addCallerFilterToSearchBuilderIfNecessary(eq(filters), any(SearchOptionsBuilder.class));
        final List<CaseItem> caseItems = result.getResults();
        assertEquals(1, caseItems.size());
        assertEquals(caseItem, caseItems.get(0));
    }

    private void search_should_throw_an_exception_when_search_failed_process_instances_failed_and_filter_on_state(final String state) throws SearchException {
        // Given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = Collections.singletonMap(CaseItem.FILTER_STATE, state);

        doThrow(new SearchException(new Exception("toto"))).when(processAPI).searchFailedProcessInstances(any(SearchOptions.class));

        try {
            // When
            caseDatastore.search(page, resultsByPage, search, orders, filters);
        } catch (final APIException e) {
            // Then
            assertTrue(e.getCause() instanceof SearchException);
        } finally {
            // Then
            verify(processAPI).searchFailedProcessInstances(any(SearchOptions.class));
            verify(caseDatastore).addCallerFilterToSearchBuilderIfNecessary(eq(filters), any(SearchOptionsBuilder.class));
        }
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_search_open_process_instances_involving_user_and_convert_them_to_CaseItem_when_filter_on_user() throws SearchException {
        // Given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_ID;
        final Map<String, String> filters = new HashMap<String, String>();
        filters.put(CaseItem.FILTER_USER_ID, "9");
        filters.put(CaseItem.ATTRIBUTE_STATE, "plop");

        final ProcessInstance processInstance = new ProcessInstanceImpl("name");
        doReturn(new SearchResultImpl<ProcessInstance>(1L, Arrays.asList(processInstance))).when(processAPI).searchOpenProcessInstancesInvolvingUser(eq(9L),
                any(SearchOptions.class));
        final CaseItem caseItem = new CaseItem();
        doReturn(caseItem).when(caseDatastore).convertEngineToConsoleItem(processInstance);

        // When
        final ItemSearchResult<CaseItem> result = caseDatastore.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(processAPI).searchOpenProcessInstancesInvolvingUser(eq(9L), any(SearchOptions.class));
        verify(caseDatastore).addCallerFilterToSearchBuilderIfNecessary(eq(filters), any(SearchOptionsBuilder.class));
        final List<CaseItem> caseItems = result.getResults();
        assertEquals(1, caseItems.size());
        assertEquals(caseItem, caseItems.get(0));
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_throw_an_exception_when_search_open_process_instances_involving_user_filter_on_user() throws SearchException {
        // Given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = new HashMap<String, String>();
        filters.put(CaseItem.FILTER_USER_ID, "9");
        filters.put(CaseItem.ATTRIBUTE_STATE, "plop");

        doThrow(new SearchException(new Exception("toto"))).when(processAPI).searchOpenProcessInstancesInvolvingUser(eq(9L), any(SearchOptions.class));

        try {
            // When
            caseDatastore.search(page, resultsByPage, search, orders, filters);
        } catch (final APIException e) {
            // Then
            assertTrue(e.getCause() instanceof SearchException);
        } finally {
            // Then
            verify(processAPI).searchOpenProcessInstancesInvolvingUser(eq(9L), any(SearchOptions.class));
            verify(caseDatastore).addCallerFilterToSearchBuilderIfNecessary(eq(filters), any(SearchOptionsBuilder.class));
        }
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_search_open_process_instances_supervised_by_and_convert_them_to_CaseItem_when_filter_on_supervisor()
            throws SearchException {
        // Given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_ID;
        final Map<String, String> filters = new HashMap<String, String>();
        filters.put(CaseItem.FILTER_SUPERVISOR_ID, "9");
        filters.put(CaseItem.ATTRIBUTE_STATE, "plop");

        final ProcessInstance processInstance = new ProcessInstanceImpl("name");
        doReturn(new SearchResultImpl<ProcessInstance>(1L, Arrays.asList(processInstance))).when(processAPI).searchOpenProcessInstancesSupervisedBy(eq(9L),
                any(SearchOptions.class));
        final CaseItem caseItem = new CaseItem();
        doReturn(caseItem).when(caseDatastore).convertEngineToConsoleItem(processInstance);

        // When
        final ItemSearchResult<CaseItem> result = caseDatastore.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(processAPI).searchOpenProcessInstancesSupervisedBy(eq(9L), any(SearchOptions.class));
        verify(caseDatastore).addCallerFilterToSearchBuilderIfNecessary(eq(filters), any(SearchOptionsBuilder.class));
        final List<CaseItem> caseItems = result.getResults();
        assertEquals(1, caseItems.size());
        assertEquals(caseItem, caseItems.get(0));
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#search(int, int, String, String, Map).
     */
    @Test
    public final void search_should_throw_an_exception_when_search_open_process_instances_supervised_by_filter_on_supervisor() throws SearchException {
        // Given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = new HashMap<String, String>();
        filters.put(CaseItem.FILTER_SUPERVISOR_ID, "9");
        filters.put(CaseItem.ATTRIBUTE_STATE, "plop");

        doThrow(new SearchException(new Exception("toto"))).when(processAPI).searchOpenProcessInstancesSupervisedBy(eq(9L), any(SearchOptions.class));

        try {
            // When
            caseDatastore.search(page, resultsByPage, search, orders, filters);
        } catch (final APIException e) {
            // Then
            assertTrue(e.getCause() instanceof SearchException);
        } finally {
            // Then
            verify(processAPI).searchOpenProcessInstancesSupervisedBy(eq(9L), any(SearchOptions.class));
            verify(caseDatastore).addCallerFilterToSearchBuilderIfNecessary(eq(filters), any(SearchOptionsBuilder.class));
        }
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#addCallerFilterToSearchBuilderIfNecessary(Map, SearchOptionsBuilder).
     */
    @Test
    public final void addCallerFilterToSearchBuilderIfNecessary_should_add_caller_filter_to_builder_when_no_caller_filter() {
        // Given
        final Map<String, String> filters = Collections.emptyMap();
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);

        // When
        caseDatastore.addCallerFilterToSearchBuilderIfNecessary(filters, builder);

        // Then
        final SearchFilter searchFilter = builder.done().getFilters().get(0);
        assertEquals(ProcessInstanceSearchDescriptor.CALLER_ID, searchFilter.getField());
        assertEquals(-1, searchFilter.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#addCallerFilterToSearchBuilderIfNecessary(Map, SearchOptionsBuilder).
     */
    @Test
    public final void addCallerFilterToSearchBuilderIfNecessary_should_add_caller_filter_to_builder_when_filter_on_caller_with_value_different_of_any() {
        // Given
        final Map<String, String> filters = Collections.singletonMap(CaseItem.FILTER_CALLER, "9");
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);

        // When
        caseDatastore.addCallerFilterToSearchBuilderIfNecessary(filters, builder);

        // Then
        final SearchFilter searchFilter = builder.done().getFilters().get(0);
        assertEquals(ProcessInstanceSearchDescriptor.CALLER_ID, searchFilter.getField());
        assertEquals(9L, searchFilter.getValue());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#addCallerFilterToSearchBuilderIfNecessary(Map, SearchOptionsBuilder).
     */
    @Test
    public final void addCallerFilterToSearchBuilderIfNecessary_should_do_nothing_when_filter_on_any_caller() {
        // Given
        final Map<String, String> filters = Collections.singletonMap(CaseItem.FILTER_CALLER, "any");
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);

        // When
        caseDatastore.addCallerFilterToSearchBuilderIfNecessary(filters, builder);

        // Then
        assertTrue(builder.done().getFilters().isEmpty());
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore#count(String, String, Map).
     */
    @Test
    public final void count_should_return_total_of_search() {
        // Given
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_ID;
        final Map<String, String> filters = Collections.emptyMap();
        final long total = 7L;
        final ItemSearchResult<IItem> itemSearchResult = new ItemSearchResult<IItem>(0, 0, total, null);
        doReturn(itemSearchResult).when(caseDatastore).search(0, 0, search, orders, filters);

        // When
        final long result = caseDatastore.count(search, orders, filters);

        // Then
        assertEquals(total, result);
    }

}