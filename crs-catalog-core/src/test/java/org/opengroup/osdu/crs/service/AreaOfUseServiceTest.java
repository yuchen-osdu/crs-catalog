package org.opengroup.osdu.crs.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.search.Point;
import org.opengroup.osdu.core.common.model.search.QueryResponse;
import org.opengroup.osdu.crs.model.response.AreaOfUseSearchResult;
import org.opengroup.osdu.crs.model.request.InPolygonQuery;
import org.opengroup.osdu.crs.model.response.SearchResponse;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class AreaOfUseServiceTest {

	@InjectMocks
	private AreaOfUseService areaOfUseService;

	@Mock
	private SearchWrapperService searchWrapperService;

	@Mock
	private JaxRsDpsLog logger;

	@Test
	public void testSimpleDistanceCalculation(){
		// arrange
		Point test = new Point();
		test.setLatitude(7.40);
		test.setLongitude(10.23);

		InPolygonQuery inPolygonQuery = new InPolygonQuery();
		inPolygonQuery.setPoints(Arrays.asList(test));
		inPolygonQuery.setDataId("test-data-id");
		inPolygonQuery.setRecordId("test-record-id");

		List<Double> coor1 = Arrays.asList(1.00, 2.78);
		List<Double> coor2 = Arrays.asList(9.11, 2.78);
		List<Double> coor3 = Arrays.asList(9.11, 6.01);
		List<Double> coor4 = Arrays.asList(1.00, 6.01);
		List<Double> coor5 = Arrays.asList(1.00, 2.78);
		List<List<Double>> coordinates = Arrays.asList(coor1, coor2, coor3, coor4, coor5);
		List<List<List<Double>>> nestedCoordinates = Arrays.asList(coordinates);
		Map<String, Object> geometry = new HashMap<>();
		geometry.put("coordinates", nestedCoordinates);
		List<Object> geometries = Arrays.asList(geometry);
		Map<String, Object> wgs84Coordinates = new HashMap<>();
		wgs84Coordinates.put("geometries", geometries);
		Map<String, Object> data = new HashMap<>();
		data.put("Wgs84Coordinates", wgs84Coordinates);
		Map<String, Object> d = new HashMap<>();
		d.put("data", data);
		List<Map<String, Object>> results = Arrays.asList(d);
		QueryResponse queryResponse = new QueryResponse();
		queryResponse.setResults(results);
		SearchResponse searchResponse = new SearchResponse(queryResponse, "");
		Mockito.when(searchWrapperService.search(Mockito.any(), Mockito.any()))
				.thenReturn(searchResponse);

		// act
		AreaOfUseSearchResult result = areaOfUseService.searchAou(inPolygonQuery);

		// assert
		Assert.assertEquals(1, result.getFailedPoints().size());
		Assert.assertEquals(1.785, result.getFailedPoints().get(0).getDegreeDistanceOutside().doubleValue(), 0d);
		Assert.assertEquals(197.383, result.getFailedPoints().get(0).getApproximateKmDistanceOutside().doubleValue(), 0d);
		Assert.assertEquals(197.383, result.getMaxDistKmOutside(), 0d);
	}

	@Test
	public void testMultipleDistanceCalculations(){
		// arrange
		Point test1 = new Point();
		test1.setLatitude(7.40);
		test1.setLongitude(10.23);

		Point test2 = new Point();
		test2.setLatitude(89);
		test2.setLongitude(-179);

		Point test3 = new Point();
		test3.setLatitude(0);
		test3.setLongitude(0);

		Point test4 = new Point();
		test4.setLatitude(-77.55465765761);
		test4.setLongitude(-100.1);

		Point test5 = new Point();
		test5.setLatitude(20.78);
		test5.setLongitude(100.00000001);

		InPolygonQuery inPolygonQuery = new InPolygonQuery();
		inPolygonQuery.setPoints(Arrays.asList(test1, test2, test3, test4, test5));
		inPolygonQuery.setDataId("test-data-id");
		inPolygonQuery.setRecordId("test-record-id");

		List<Double> coor1 = Arrays.asList(100.00, 20.78);
		List<Double> coor2 = Arrays.asList(-9.11, 20.78);
		List<Double> coor3 = Arrays.asList(-9.11, -6.01);
		List<Double> coor4 = Arrays.asList(100.00, -6.01);
		List<Double> coor5 = Arrays.asList(100.00, 20.78);
		List<List<Double>> coordinates = Arrays.asList(coor1, coor2, coor3, coor4, coor5);
		List<List<List<Double>>> nestedCoordinates = Arrays.asList(coordinates);
		Map<String, Object> geometry = new HashMap<>();
		geometry.put("coordinates", nestedCoordinates);
		List<Object> geometries = Arrays.asList(geometry);
		Map<String, Object> wgs84Coordinates = new HashMap<>();
		wgs84Coordinates.put("geometries", geometries);
		Map<String, Object> data = new HashMap<>();
		data.put("Wgs84Coordinates", wgs84Coordinates);
		Map<String, Object> d = new HashMap<>();
		d.put("data", data);
		List<Map<String, Object>> results = Arrays.asList(d);
		QueryResponse queryResponse = new QueryResponse();
		queryResponse.setResults(results);
		SearchResponse searchResponse = new SearchResponse(queryResponse, "");
		Mockito.when(searchWrapperService.search(Mockito.any(), Mockito.any()))
				.thenReturn(searchResponse);

		// act
		AreaOfUseSearchResult result = areaOfUseService.searchAou(inPolygonQuery);

		// assert
		Assert.assertEquals(3, result.getFailedPoints().size());
		Assert.assertEquals(115.749, result.getFailedPoints().get(1).getDegreeDistanceOutside().doubleValue(), 0d);
		Assert.assertEquals(12798.824, result.getFailedPoints().get(1).getApproximateKmDistanceOutside().doubleValue(), 0d);
		// technically out of bounds but just barely, not enough to register in rounded distance outside vars
		Assert.assertEquals(0.0, result.getFailedPoints().get(2).getDegreeDistanceOutside().doubleValue(), 0d);
		Assert.assertEquals(0.0, result.getFailedPoints().get(2).getApproximateKmDistanceOutside().doubleValue(), 0d);
		Assert.assertEquals(20243.373, result.getMaxDistKmOutside(), 0d);
	}
}
