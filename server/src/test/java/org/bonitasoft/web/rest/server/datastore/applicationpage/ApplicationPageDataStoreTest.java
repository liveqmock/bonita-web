package org.bonitasoft.web.rest.server.datastore.applicationpage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.bonitasoft.console.common.server.registration.BonitaRegistration;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageDefinition;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationPageDataStoreTest extends APITestWithMock {

    @Mock
    private APISession session;

    @Mock
    private ApplicationAPI applicationAPI;

    @Mock
    private PageAPI pageAPI;

    @Mock
    private ApplicationPageItemConverter converter;

    @Spy
    @InjectMocks
    private ApplicationPageDataStore dataStore;

    @Before
    public void setUp() throws Exception {
        ItemDefinitionFactory.setDefaultFactory(new ItemDefinitionFactory() {

            @Override
            public ItemDefinition<?> defineItemDefinitions(final String token) {
                return new ApplicationPageDefinition();
            }
        });
    }

    @Test
    public void should_return_result_of_engine_call_converted_to_item_on_add() throws Exception {
        //given
        final ApplicationPageItem itemToCreate = new ApplicationPageItem();
        itemToCreate.setToken("firstPage");
        itemToCreate.setApplicationId(14L);
        itemToCreate.setPageId(28L);
        final ApplicationPageImpl applicationPage = new ApplicationPageImpl(14L, 28L, "firstPage");

        given(applicationAPI.createApplicationPage(14L, 28L, "firstPage")).willReturn(applicationPage);

        //when
        final ApplicationPageItem createdItem = dataStore.add(itemToCreate);

        //then
        assertThat(createdItem).isEqualTo(createdItem);
    }

    @Test
    public void should_register_app_usage_on_add_when_system_property_set() throws Exception {
        //given
        final ApplicationPageItem itemToCreate = new ApplicationPageItem();
        itemToCreate.setToken("firstPage");
        itemToCreate.setApplicationId(14L);
        itemToCreate.setPageId(28L);
        final ApplicationPageImpl applicationPage = new ApplicationPageImpl(14L, 28L, "firstPage");
        final Page page = mock(Page.class);

        given(applicationAPI.createApplicationPage(14L, 28L, "firstPage")).willReturn(applicationPage);
        given(page.isProvided()).willReturn(false);
        given(pageAPI.getPage(28L)).willReturn(page);
        doNothing().when(dataStore).registerApplicationUsage();

        try {
            System.setProperty(BonitaRegistration.BONITA_REGISTER_SYSTEM_PROPERTY, "1");

            //when
            dataStore.add(itemToCreate);

            verify(dataStore).registerApplicationUsageIfNeeded(applicationPage);
            verify(dataStore).registerApplicationUsage();

        } finally {
            System.clearProperty(BonitaRegistration.BONITA_REGISTER_SYSTEM_PROPERTY);
        }
    }

    @Test
    public void should_not_register_app_usage_on_add_when_system_property_not_set() throws Exception {
        //given
        final ApplicationPageItem itemToCreate = new ApplicationPageItem();
        itemToCreate.setToken("firstPage");
        itemToCreate.setApplicationId(14L);
        itemToCreate.setPageId(28L);
        final ApplicationPageImpl applicationPage = new ApplicationPageImpl(14L, 28L, "firstPage");

        given(applicationAPI.createApplicationPage(14L, 28L, "firstPage")).willReturn(applicationPage);

        //when
        dataStore.add(itemToCreate);

        verify(dataStore).registerApplicationUsageIfNeeded(applicationPage);
        verify(dataStore, never()).registerApplicationUsage();
    }

    @Test
    public void should_not_register_app_usage_on_add_when_page_is_provided() throws Exception {
        //given
        final ApplicationPageItem itemToCreate = new ApplicationPageItem();
        itemToCreate.setToken("firstPage");
        itemToCreate.setApplicationId(14L);
        itemToCreate.setPageId(28L);
        final ApplicationPageImpl applicationPage = new ApplicationPageImpl(14L, 28L, "firstPage");
        final Page page = mock(Page.class);

        given(applicationAPI.createApplicationPage(14L, 28L, "firstPage")).willReturn(applicationPage);
        given(page.isProvided()).willReturn(true);
        given(pageAPI.getPage(28L)).willReturn(page);

        try {
            System.setProperty(BonitaRegistration.BONITA_REGISTER_SYSTEM_PROPERTY, "1");

            //when
            dataStore.add(itemToCreate);

            verify(dataStore).registerApplicationUsageIfNeeded(applicationPage);
            verify(dataStore, never()).registerApplicationUsage();

        } finally {
            System.clearProperty(BonitaRegistration.BONITA_REGISTER_SYSTEM_PROPERTY);
        }
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_engine_throws_CreationException_on_add() throws Exception {
        //given
        given(applicationAPI.createApplicationPage(anyLong(), anyLong(), anyString())).willThrow(new CreationException(""));

        //when
        dataStore.add(new ApplicationPageItem());

        //then exception
    }

    @Test
    public void should_return_the_applicationPage_supplied_by_the_engine_converted_to_item_on_get() throws Exception {
        //given
        final ApplicationPage applicationPage = mock(ApplicationPage.class);
        final ApplicationPageItem item = mock(ApplicationPageItem.class);
        given(applicationAPI.getApplicationPage(1)).willReturn(applicationPage);
        given(converter.toApplicationPageItem(applicationPage)).willReturn(item);

        //when
        final ApplicationPageItem retrivedItem = dataStore.get(APIID.makeAPIID("1"));

        //then
        assertThat(retrivedItem).isNotNull();
        assertThat(retrivedItem).isEqualTo(item);
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_the_engine_throw_NotFoundException_on_get() throws Exception {
        //given
        given(applicationAPI.getApplicationPage(1)).willThrow(new ApplicationPageNotFoundException(""));

        //when
        dataStore.get(APIID.makeAPIID("1"));

        //then exception
    }

    @Test
    public void should_delete_the_good_Application_Page_on_delete() throws Exception {
        //given

        //when
        dataStore.delete(Arrays.<APIID> asList(APIID.makeAPIID("1"), APIID.makeAPIID("2")));

        //then
        verify(applicationAPI, times(1)).deleteApplicationPage(1);
        verify(applicationAPI, times(1)).deleteApplicationPage(2);
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_on_delete_when_engine_throws_exception() throws Exception {
        doThrow(new DeletionException("")).when(applicationAPI).deleteApplicationPage(1);

        //when
        dataStore.delete(Arrays.<APIID> asList(APIID.makeAPIID("1")));

        //then exception
    }

    @Test
    public void should_return_a_valid_ItemSearchResult_on_search() throws Exception {
        //given
        final String orders = ApplicationPageItem.ATTRIBUTE_TOKEN + " DESC";
        final ApplicationPageImpl appPage = new ApplicationPageImpl(1, 11, "MyAppPage");
        appPage.setId(1);
        final ApplicationPageItem item = new ApplicationPageItem();
        given(converter.toApplicationPageItem(appPage)).willReturn(item);

        given(applicationAPI.searchApplicationPages(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<ApplicationPage>(2, Arrays.<ApplicationPage> asList(appPage)));

        //when
        final ItemSearchResult<ApplicationPageItem> retrievedItems = dataStore.search(0, 1, null, orders,
                Collections.<String, String> emptyMap());

        //then
        assertThat(retrievedItems).isNotNull();
        assertThat(retrievedItems.getLength()).isEqualTo(1);
        assertThat(retrievedItems.getPage()).isEqualTo(0);
        assertThat(retrievedItems.getTotal()).isEqualTo(2);
        assertThat(retrievedItems.getResults().get(0).getToken()).isEqualTo("MyAppPage");

    }

    @Test
    public void should_call_engine_with_good_parameters_on_search() throws Exception {
        //given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "string to Match";
        final String orders = ApplicationPageItem.ATTRIBUTE_TOKEN + " DESC";
        final HashMap<String, String> filters = new HashMap<String, String>();
        filters.put(ApplicationPageItem.ATTRIBUTE_APPLICATION_ID, "1");
        final ApplicationPageImpl appPage = new ApplicationPageImpl(1, 11, "MyAppPage");
        appPage.setId(1);
        final ApplicationPageItem item = new ApplicationPageItem();
        given(converter.toApplicationPageItem(appPage)).willReturn(item);

        given(applicationAPI.searchApplicationPages(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<ApplicationPage>(2, Arrays.<ApplicationPage> asList(appPage)));

        //when
        dataStore.search(page, resultsByPage, search, orders, filters);

        //then
        final ArgumentCaptor<SearchOptions> captor = ArgumentCaptor.forClass(SearchOptions.class);
        verify(applicationAPI, times(1)).searchApplicationPages(captor.capture());

        final SearchOptions searchOption = captor.getValue();
        assertThat(searchOption.getFilters()).hasSize(1);
        assertThat(searchOption.getFilters().get(0).getField()).isEqualTo(ApplicationPageItem.ATTRIBUTE_APPLICATION_ID);
        assertThat(searchOption.getFilters().get(0).getValue()).isEqualTo("1");
        assertThat(searchOption.getSearchTerm()).isEqualTo(search);
        assertThat(searchOption.getMaxResults()).isEqualTo(1);
        assertThat(searchOption.getStartIndex()).isEqualTo(0);
        assertThat(searchOption.getSorts()).hasSize(1);
        assertThat(searchOption.getSorts().get(0).getField()).isEqualTo(ApplicationPageItem.ATTRIBUTE_TOKEN);
        assertThat(searchOption.getSorts().get(0).getOrder()).isEqualTo(Order.DESC);

    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_engine_throws_SearchException_on_search() throws Exception {
        //given
        final String orders = ApplicationPageItem.ATTRIBUTE_TOKEN + " DESC";
        given(applicationAPI.searchApplicationPages(any(SearchOptions.class))).willThrow(new SearchException(null));

        //when
        dataStore.search(0, 1, null, orders, Collections.<String, String> emptyMap());

        //then exception

    }

}
