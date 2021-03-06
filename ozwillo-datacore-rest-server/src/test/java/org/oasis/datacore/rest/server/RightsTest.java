package org.oasis.datacore.rest.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;

import org.apache.cxf.common.util.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis.datacore.common.context.SimpleRequestContextProvider;
import org.oasis.datacore.core.meta.DataModelServiceImpl;
import org.oasis.datacore.core.meta.model.DCModelBase;
import org.oasis.datacore.core.meta.model.DCSecurity;
import org.oasis.datacore.core.meta.pov.DCProject;
import org.oasis.datacore.core.security.mock.LocalAuthenticationService;
import org.oasis.datacore.historization.exception.HistorizationException;
import org.oasis.datacore.historization.service.HistorizationService;
import org.oasis.datacore.rest.api.DCResource;
import org.oasis.datacore.rest.api.DatacoreApi;
import org.oasis.datacore.rest.client.DatacoreCachedClient;
import org.oasis.datacore.rights.rest.api.DCRights;
import org.oasis.datacore.rights.rest.api.RightsApi;
import org.oasis.datacore.sample.MarkaInvestData;
import org.oasis.datacore.sample.MarkaInvestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableMap;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:oasis-datacore-rest-server-test-context.xml" })

public class RightsTest {

	@Autowired
	private RightsApi rightsApi;
	
	@Autowired
	@Qualifier("datacoreApiCachedJsonClient")
	private DatacoreCachedClient datacoreApi;

	@Value("${datacoreApiClient.containerUrl}")
   private String containerUrlString;
   @Value("#{new java.net.URI('${datacoreApiClient.containerUrl}')}")
   //@Value("#{uriService.getContainerUrl()}")
   private URI containerUrl;

	@Autowired
	private DataModelServiceImpl modelAdminService;

	@Autowired
	private MongoOperations mongoOperations;

	@Autowired
	private LocalAuthenticationService mockAuthenticationService;

	@Autowired
	private MarkaInvestData markaInvestData;
	
	@Autowired
	private HistorizationService historizationService;

	@Before
	public void flushDataAndSetProject() {
		/*
		truncateModel(MarkaInvestModel.CITY_MODEL_NAME);
		truncateModel(MarkaInvestModel.COMPANY_MODEL_NAME);
		truncateHistorizationModel(MarkaInvestModel.COMPANY_MODEL_NAME);
		truncateModel(MarkaInvestModel.COST_TYPE_MODEL_NAME);
		truncateModel(MarkaInvestModel.COUNTRY_MODEL_NAME);
		truncateModel(MarkaInvestModel.FIELD_MODEL_NAME);
		truncateModel(MarkaInvestModel.INVESTMENT_ASSISTANCE_REQUEST_MODEL_NAME);
		truncateModel(MarkaInvestModel.INVESTOR_MODEL_NAME);
		truncateModel(MarkaInvestModel.INVESTOR_TYPE_MODEL_NAME);
		truncateModel(MarkaInvestModel.SECTOR_MODEL_NAME);
		truncateModel(MarkaInvestModel.USER_MODEL_NAME);*/

      /*markaInvestData.initModels();
		mockAuthenticationService.loginAs("admin");
		markaInvestData.createDataSample();
		mockAuthenticationService.logout();*/
      markaInvestData.initData(); // cleans data first

      SimpleRequestContextProvider.setSimpleRequestContext(new ImmutableMap.Builder<String, Object>()
            .put(DatacoreApi.PROJECT_HEADER, DCProject.OASIS_SAMPLE).build());
	}
	
	/**
	 * Logout after tests to restore default unlogged state.
	 * This is required in tests that use authentication,
	 * else if last test fails, tests that don't login will use logged in user
	 * rather than default one, which may trigger a different behaviour and
	 * make some tests fail (ex. DatacoreApiServerTest.test3clientCache asserting
	 * that creator is admin or guest).
	 */
	@After
	public void logoutAfter() {
      mockAuthenticationService.logout();
	}

	@Test
	public void testCreateResourceWhenResourceAdminForModelType() {
		
		mockAuthenticationService.loginAs("admin");
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.FIELD_MODEL_NAME), MarkaInvestModel.FIELD_MODEL_NAME);
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.COUNTRY_MODEL_NAME), MarkaInvestModel.COUNTRY_MODEL_NAME);
		mockAuthenticationService.logout();
		
		mockAuthenticationService.loginAs("bob");
		
		List<DCResource> listCity = markaInvestData.getData().get(MarkaInvestModel.CITY_MODEL_NAME);
		Assert.assertNotNull(listCity);
		
		List<DCResource> listCityCreated = datacoreApi.postAllDataInType(listCity, MarkaInvestModel.CITY_MODEL_NAME);
		Assert.assertNotNull(listCityCreated);
		Assert.assertFalse(listCityCreated.isEmpty());
		
		mockAuthenticationService.logout();
	
	}
	
	@Test
	public void testCreateAndModifyResourceWhenModelOwner() {
		
		mockAuthenticationService.loginAs("admin");
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.FIELD_MODEL_NAME), MarkaInvestModel.FIELD_MODEL_NAME);
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.COUNTRY_MODEL_NAME), MarkaInvestModel.COUNTRY_MODEL_NAME);
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.CITY_MODEL_NAME), MarkaInvestModel.CITY_MODEL_NAME);
		mockAuthenticationService.logout();
		
		mockAuthenticationService.loginAs("bob");
		
		List<DCResource> listCompany = markaInvestData.getData().get(MarkaInvestModel.COMPANY_MODEL_NAME);
		Assert.assertNotNull(listCompany);
		
		List<DCResource> listCompanyCreated = datacoreApi.postAllDataInType(listCompany, MarkaInvestModel.COMPANY_MODEL_NAME);
		Assert.assertNotNull(listCompanyCreated);
		Assert.assertFalse(listCompanyCreated.isEmpty());
		
		DCResource company = listCompanyCreated.get(0);
		Assert.assertNotNull(company);
		
		company.getProperties().put("name", "Openwide");
		company.getProperties().put("lastAnnualRevenue", 1500000f);
		List<DCResource> listNewCompany = new ArrayList<>();
		listNewCompany.add(company);
				
		datacoreApi.putAllDataInType(listNewCompany, MarkaInvestModel.COMPANY_MODEL_NAME);
		
		mockAuthenticationService.logout();
		
	}
	
	@Test
	public void testReadingWithoutRightsOnResource() {
		
		mockAuthenticationService.loginAs("admin");
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.FIELD_MODEL_NAME), MarkaInvestModel.FIELD_MODEL_NAME);
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.COUNTRY_MODEL_NAME), MarkaInvestModel.COUNTRY_MODEL_NAME);
		mockAuthenticationService.logout();
		
		mockAuthenticationService.loginAs("bob");
		DCModelBase countryModel = modelAdminService.getModelBase(MarkaInvestModel.COUNTRY_MODEL_NAME);
		Assert.assertNotNull(countryModel);
      countryModel.setSecurity(new DCSecurity()); // most probably null (default)
		countryModel.getSecurity().setAuthentifiedReadable(false);
		try {
			datacoreApi.getData(MarkaInvestModel.COUNTRY_MODEL_NAME, "1");
		} catch (ClientErrorException e) {
			Assert.assertTrue(e.getResponse() != null && e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN);
		}
		mockAuthenticationService.logout();
		
	}
	
	@Test
	public void testAddRightAndVerify() {
		
		mockAuthenticationService.loginAs("admin");
		
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.FIELD_MODEL_NAME), MarkaInvestModel.FIELD_MODEL_NAME);
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.COUNTRY_MODEL_NAME), MarkaInvestModel.COUNTRY_MODEL_NAME);
		mockAuthenticationService.logout();
		
		mockAuthenticationService.loginAs("jim");
		
		DCModelBase countryModel = modelAdminService.getModelBase(MarkaInvestModel.COUNTRY_MODEL_NAME);
		Assert.assertNotNull(countryModel);
      countryModel.setSecurity(new DCSecurity()); // most probably null (default)
		countryModel.getSecurity().setAuthentifiedReadable(false);
		countryModel.getSecurity().setAuthentifiedWritable(false);
		countryModel.getSecurity().setAuthentifiedCreatable(false);	
		try {
			datacoreApi.getData(MarkaInvestModel.COUNTRY_MODEL_NAME, "1");
		} catch (ClientErrorException e) {
			Assert.assertTrue(e.getResponse() != null && e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN);
		}
		mockAuthenticationService.logout();
		
		mockAuthenticationService.loginAs("admin");
		
		DCRights rights = new DCRights();
		List<String> readers = new ArrayList<>();
		readers.add("sample.marka.country.readers");
		rights.setReaders(readers);
		try {
			rightsApi.addRightsOnResource(MarkaInvestModel.COUNTRY_MODEL_NAME, "1", 0, rights);
		} catch (WebApplicationException e) {
			Assert.assertTrue(e.getResponse() != null && e.getResponse().getStatus() == HttpStatus.SC_OK);
		}
		
		DCRights retrievedRights = null;
		
		try {
			rightsApi.getRightsOnResource(MarkaInvestModel.COUNTRY_MODEL_NAME, "1", 0);
		} catch (WebApplicationException e) {
			Assert.assertTrue(e.getResponse() != null);
			Assert.assertTrue(e.getResponse().getStatus() == HttpStatus.SC_OK);
			Assert.assertTrue(e.getResponse().getEntity() instanceof DCRights);
			retrievedRights = (DCRights) e.getResponse().getEntity();
		}
		
		Assert.assertNotNull(retrievedRights);
		Assert.assertNotNull(retrievedRights.getReaders());
		Assert.assertFalse(retrievedRights.getReaders().isEmpty());
		Assert.assertTrue(retrievedRights.getReaders().size() == rights.getReaders().size());
		Assert.assertTrue(retrievedRights.getReaders().get(0).equals(rights.getReaders().get(0)));
		
		mockAuthenticationService.logout();
		
		mockAuthenticationService.loginAs("jim");
		
		DCResource country = datacoreApi.getData(MarkaInvestModel.COUNTRY_MODEL_NAME, "1");
		Assert.assertNotNull(country);
		
		mockAuthenticationService.logout();
		
	}
	
	@Test
	public void testRemoveAndVerify() {
		
		DCModelBase countryModel = modelAdminService.getModelBase(MarkaInvestModel.COUNTRY_MODEL_NAME);
		Assert.assertNotNull(countryModel);
      countryModel.setSecurity(new DCSecurity()); // most probably null (default)
		countryModel.getSecurity().setAuthentifiedReadable(false);
		countryModel.getSecurity().setAuthentifiedWritable(false);
		countryModel.getSecurity().setAuthentifiedCreatable(false);	
		
		mockAuthenticationService.loginAs("admin");
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.FIELD_MODEL_NAME), MarkaInvestModel.FIELD_MODEL_NAME);
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.COUNTRY_MODEL_NAME), MarkaInvestModel.COUNTRY_MODEL_NAME);
		
		DCRights rights = new DCRights();
		List<String> readers = new ArrayList<>();
		readers.add("sample.marka.country.readers");
		rights.setReaders(readers);
		try {
			rightsApi.addRightsOnResource(MarkaInvestModel.COUNTRY_MODEL_NAME, "1", 0, rights);
		} catch (WebApplicationException e) {
			Assert.assertTrue(e.getResponse() != null && e.getResponse().getStatus() == HttpStatus.SC_OK);
		}
		
		mockAuthenticationService.logout();
		
		mockAuthenticationService.loginAs("jim");
	
		DCResource country = datacoreApi.getData(MarkaInvestModel.COUNTRY_MODEL_NAME, "1");
		
		Assert.assertNotNull(country);
		
		mockAuthenticationService.logout();
		
		mockAuthenticationService.loginAs("admin");
		
		rights = new DCRights();
		readers = new ArrayList<>();
		readers.add("sample.marka.country.readers");
		rights.setReaders(readers);
		try {
			rightsApi.removeRightsOnResource(MarkaInvestModel.COUNTRY_MODEL_NAME, "1", 0, rights);
		} catch (WebApplicationException e) {
			Assert.assertTrue(e.getResponse() != null && e.getResponse().getStatus() == HttpStatus.SC_OK);
		}
		
		mockAuthenticationService.logout();
		mockAuthenticationService.loginAs("jim");
		
		try {
			datacoreApi.getData(MarkaInvestModel.COUNTRY_MODEL_NAME, "1");
		} catch (ClientErrorException e) {
			Assert.assertTrue(e.getResponse() != null && e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN);
		}
		
		mockAuthenticationService.logout();
	}


	@Test
	@DirtiesContext
	public void testTokenCacheEvictOnRightsUpdate() {

		Cache cache = mock(Cache.class);
		CacheManager cacheManager = mock(CacheManager.class);
		when(cacheManager.getCache(anyString())).thenReturn(cache);

		ReflectionTestUtils.setField(rightsApi, "cacheManager", cacheManager);

		mockAuthenticationService.loginAs("admin");

		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.FIELD_MODEL_NAME), MarkaInvestModel.FIELD_MODEL_NAME);
		datacoreApi.postAllDataInType(markaInvestData.getData().get(MarkaInvestModel.COUNTRY_MODEL_NAME), MarkaInvestModel.COUNTRY_MODEL_NAME);

		DCRights rights = new DCRights();
		List<String> readers = new ArrayList<>();
		readers.add("sample.marka.country.readers");
		rights.setReaders(readers);
		try {
			rightsApi.addRightsOnResource(MarkaInvestModel.COUNTRY_MODEL_NAME, "1", 0, rights);
		} catch (WebApplicationException e) {
			Assert.assertTrue(e.getResponse() != null && e.getResponse().getStatus() == HttpStatus.SC_OK);
		}

		verify(cache).clear();
		mockAuthenticationService.logout();
	}

	private void truncateModel(String type) {
		if (type != null && !StringUtils.isEmpty(type)) {
			DCModelBase dcModel = modelAdminService.getModelBase(type);
			if (dcModel != null) { // && dcModel.isInstanciable()
				mongoOperations.remove(new Query(), dcModel.getCollectionName());
			}
		} else {
			System.out.println("Model type is null or empty, cannot truncate model");
		}
	}
	
	private void truncateHistorizationModel(String type) {
		if (type != null && !StringUtils.isEmpty(type)) {
			DCModelBase dcModel = modelAdminService.getModelBase(type);
			if (dcModel != null && dcModel.isHistorizable()) { // && dcModel.isInstanciable()
				try {
					String historizationCollectionName = historizationService.getHistorizedCollectionNameFromOriginalModel(dcModel);
					mongoOperations.remove(new Query(), historizationCollectionName);
				} catch (HistorizationException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("Model type is null or empty, cannot truncate model");
		}
	}

}