// Copyright © Amazon
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.crs.service;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.search.Point;
import org.opengroup.osdu.core.common.model.search.Polygon;
import org.opengroup.osdu.crs.model.request.InPolygonQuery;
import org.opengroup.osdu.crs.model.response.AreaOfUseSearchPoint;
import org.opengroup.osdu.crs.model.response.AreaOfUseSearchResult;
import org.opengroup.osdu.crs.model.response.SearchResponse;
import org.opengroup.osdu.crs.model.geometry.*;
import org.opengroup.osdu.crs.util.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AreaOfUseService {

	@Inject
	private SearchWrapperService searchWrapperService;

	@Inject
	private JaxRsDpsLog logger;

	// approximation taken from here: https://en.wikipedia.org/wiki/Latitude
	private final static double DEGREE_TO_KM_RATIO = 110.574;

	private final static int DECIMAL_PLACES = 3;

	public AreaOfUseSearchResult searchAou(InPolygonQuery inPolygonQuery) {
		Polygon polygon = getRecordPolygon(inPolygonQuery);

		double maxDistanceOutDegrees = 0;
		AreaOfUseSearchResult areaOfUseSearchResult = new AreaOfUseSearchResult();
		for(Point testPoint : inPolygonQuery.getPoints()){
			Double degreeDistance = determineDistance(testPoint, polygon.getPoints());
			if (degreeDistance > 0){
				logger.debug(String.format("Point outside polygon: %s, %s", testPoint.toString(), degreeDistance));
				addFailedPoint(testPoint, degreeDistance, areaOfUseSearchResult);
				if(degreeDistance > maxDistanceOutDegrees){
					maxDistanceOutDegrees = degreeDistance;
				}
			} else {
				logger.debug(String.format("Point inside polygon: %s", testPoint.toString()));
				areaOfUseSearchResult.addSuccessfulPoint(testPoint);
			}
		}

		setMaxDistanceOutKm(maxDistanceOutDegrees, areaOfUseSearchResult);

		return areaOfUseSearchResult;
	}

	// Found here: https://stackoverflow.com/questions/5254838/calculating-distance-between-a-point-and-a-rectangular-box-nearest-point
	// Could be extended to work with polygons with these considerations: calculate distance to edge (if v=xy then n=(-y,x)), calculate distances to vertices,
	// and then do it for each edge since a polygon's points could make complex shapes.
	// Keeping rectangle assumption for now because it's a bit faster and no requirement for polygon distance yet.
	private Double determineDistance(Point test, List<Point> polygon) {
		Rectangle rectangle = new Rectangle(polygon);

		double dx = Collections.max(Arrays.asList(rectangle.getMin().getLongitude() - test.getLongitude(), 0d, test.getLongitude() - rectangle.getMax().getLongitude()));
		double dy = Collections.max(Arrays.asList(rectangle.getMin().getLatitude() - test.getLatitude(), 0d, test.getLatitude() - rectangle.getMax().getLatitude()));
		double distance = Math.sqrt(dx * dx + dy * dy);

		return distance;
	}

	private void setMaxDistanceOutKm(double maxDistanceOutDegrees, AreaOfUseSearchResult areaOfUseSearchResult){
		areaOfUseSearchResult.setMaxDistKmOutside(roundDouble(maxDistanceOutDegrees * DEGREE_TO_KM_RATIO));
	}

	private void addFailedPoint(Point failedPoint, Double degreeDistance, AreaOfUseSearchResult areaOfUseSearchResult){
		AreaOfUseSearchPoint areaOfUseSearchPoint = new AreaOfUseSearchPoint();
		areaOfUseSearchPoint.setPoint(failedPoint);
		areaOfUseSearchPoint.setDegreeDistanceOutside(roundDouble(degreeDistance));
		areaOfUseSearchPoint.setApproximateKmDistanceOutside(convertDegreeToKm(degreeDistance));
		areaOfUseSearchResult.addFailedPoint(areaOfUseSearchPoint);
	}

	private Double convertDegreeToKm(Double degree){
		return roundDouble(degree * DEGREE_TO_KM_RATIO);
	}

	private Double roundDouble(double value){
		BigDecimal bd = new BigDecimal(value).setScale(DECIMAL_PLACES, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}

	private Polygon getRecordPolygon(InPolygonQuery inPolygonQuery){
		SearchResponse searchResponse = searchWrapperService.search(inPolygonQuery, SearchWrapperService.CT_AND_CRS_KIND);
		List<Map<String, Object>> searchResults = searchResponse.getSearchResults().getResults();
		if(searchResults.size() != 1) {
			throw AppException.createBadRequest(String.format(
					"Received %s results from search, should only get one. Query: %s"
					, searchResults.size(), searchResponse.getQuery()));
		}

		List<List<List<Double>>> coordinates;
		try {
			Map<String, Object> searchResult = searchResults.get(0);
			Map<String, Object> searchResultData = (Map) searchResult.get("data");
			Map<String, Object> wgs84Coordinates = (Map) searchResultData.get("Wgs84Coordinates");
			List<Object> geometries = (List) wgs84Coordinates.get("geometries");
			if (geometries.size() != 1) {
				throw AppException.createBadRequest(String.format(
						"Record Wgs84Coordinates should only have one feature for polygon. Query: %s"
						, searchResponse.getQuery()));
			}
			Map<String, Object> geometry = (Map) geometries.get(0);
			coordinates = (List) geometry.get("coordinates");
		} catch(ClassCastException e){
			logger.error("Failed to deserialize search result", e);
			throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to deserialize search result, see logs", e.getMessage());
		}

		// manifests store coordinates in nested lists, for our purpose there is no reason for it
		List<List<Double>> coordinatesList = coordinates.stream()
												.flatMap(List::stream).collect(Collectors.toList());

		logger.debug(String.format("Successfully retrieved polygon"));

		List<Point> points = new ArrayList<>();
		for(List<Double> coordinate : coordinatesList){
			if(coordinate.size() != 2){
				throw AppException.createBadRequest(String.format(
						"Record has coordinate value with invalid amount of points. Query: %s"
						, searchResponse.getQuery()));
			}
			logger.debug(String.format("Polygon coordinate: %s, %s", coordinate.get(1), coordinate.get(0)));

			points.add(new Point(coordinate.get(1), coordinate.get(0)));
		}

		return new Polygon(points);
	}

}
