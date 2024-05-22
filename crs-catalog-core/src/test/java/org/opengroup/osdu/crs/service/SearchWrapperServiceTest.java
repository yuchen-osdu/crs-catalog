package org.opengroup.osdu.crs.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.search.*;
import org.opengroup.osdu.core.common.search.ISearchFactory;
import org.opengroup.osdu.core.common.search.ISearchService;
import org.opengroup.osdu.crs.model.request.BaseCRS;
import org.opengroup.osdu.crs.model.request.CoordinateReferenceSystemsQuery;
import org.opengroup.osdu.crs.model.request.Datum;
import org.opengroup.osdu.crs.model.request.Extent;
import org.opengroup.osdu.crs.model.response.SearchResponse;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
public class SearchWrapperServiceTest {

	@InjectMocks
	private SearchWrapperService searchWrapperService;

	@Mock
	ISearchFactory searchFactory;

	@Mock
	DpsHeaders dpsHeaders;

	@Mock
	JaxRsDpsLog logger;

	@Mock
	ISearchService searchService;

	@Test
	public void testSearch() throws SearchException {
		// arrange
		CoordinateReferenceSystemsQuery coordinateReferenceSystemsQuery = new CoordinateReferenceSystemsQuery();
		coordinateReferenceSystemsQuery.setCodeSpace("EPSG");
		coordinateReferenceSystemsQuery.setName("Cadastre");
		coordinateReferenceSystemsQuery.setId("Geographic3D:EPSG::4472");
		coordinateReferenceSystemsQuery.setCode("4472");
		coordinateReferenceSystemsQuery.setKind("geographic 3D");
		BaseCRS baseCRS = new BaseCRS();
		baseCRS.setId("osdu:reference-data--CoordinateReferenceSystem:Geocentric:EPSG::4473:");
		baseCRS.setName("Cadastre");
		coordinateReferenceSystemsQuery.setBaseCRS(baseCRS);
		Datum datum = new Datum();
		datum.setCode("1037");
		datum.setName("Cadastre");
		datum.setCodeSpace("EPSG");
		coordinateReferenceSystemsQuery.setDatum(datum);
		Extent extent = new Extent();
		extent.setDescription("Mayonette");
		coordinateReferenceSystemsQuery.setExtent(extent);
		coordinateReferenceSystemsQuery.setCoordinateReferenceSystemType("GeodeticCRS");
		coordinateReferenceSystemsQuery.setLongitude(10.2);
		coordinateReferenceSystemsQuery.setLatitude(10.1);

		String expectedQuery = "(NOT data.InactiveIndicator: true) AND (data.CodeSpace: \"EPSG\") AND (data.Code: \"4472\") AND (data.Name: \"Cadastre\") AND (data.ID: \"Geographic3D:EPSG::4472\") AND (data.Kind: \"geographic 3D\") AND (data.CoordinateReferenceSystemType: \"GeodeticCRS\") AND (data.BaseCRS.BaseCRSID: \"osdu:reference-data--CoordinateReferenceSystem:Geocentric:EPSG::4473:\") AND (data.BaseCRS.Name: \"Cadastre\") AND (data.Datum.Name: \"Cadastre\") AND (data.Datum.AuthorityCode.Authority: \"EPSG\") AND (data.Datum.AuthorityCode.Code: \"1037\") AND (data.PreferredUsage.Extent.Description: \"Mayonette\")";

		SpatialFilter expectedSpatialFilter = new SpatialFilter();
		SpatialFilter.ByWithinPolygon byWithinPolygon = new SpatialFilter.ByWithinPolygon();
		byWithinPolygon.setPoints(Arrays.asList(new Point(10.1, 10.2)));
		expectedSpatialFilter.setByWithinPolygon(byWithinPolygon);

		QueryRequest expectedQueryRequest = new QueryRequest();
		expectedQueryRequest.setQuery(expectedQuery);
		expectedQueryRequest.setSpatialFilter(expectedSpatialFilter);

		Mockito.when(searchService.search(Mockito.any()))
				.thenReturn(new QueryResponse());

		// act
		SearchResponse searchResponse = searchWrapperService.search(coordinateReferenceSystemsQuery, SearchWrapperService.getCoordinateReferenceSystemKind());

		// assert
		ArgumentCaptor<QueryRequest> queryRequestArg = ArgumentCaptor.forClass(QueryRequest.class);
		Mockito.verify(searchService).search(queryRequestArg.capture());
		QueryRequest actualQueryRequest = queryRequestArg.getValue();
		Assertions.assertEquals(expectedQueryRequest.getQuery(), actualQueryRequest.getQuery());
		Assertions.assertEquals(expectedQueryRequest.getSpatialFilter().getByWithinPolygon().getPoints().get(0).getLongitude(),
				actualQueryRequest.getSpatialFilter().getByWithinPolygon().getPoints().get(0).getLongitude(), 0d);
		Assertions.assertEquals(expectedQueryRequest.getSpatialFilter().getByWithinPolygon().getPoints().get(0).getLongitude(),
				actualQueryRequest.getSpatialFilter().getByWithinPolygon().getPoints().get(0).getLongitude(), 0d);
	}

}
