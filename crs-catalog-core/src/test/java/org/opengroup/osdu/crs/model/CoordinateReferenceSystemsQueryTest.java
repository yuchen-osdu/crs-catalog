package org.opengroup.osdu.crs.model;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.search.SpatialFilter;
import org.opengroup.osdu.crs.model.request.BaseCRS;
import org.opengroup.osdu.crs.model.request.CoordinateReferenceSystemsQuery;
import org.opengroup.osdu.crs.model.request.Datum;
import org.opengroup.osdu.crs.model.request.Extent;

@RunWith(MockitoJUnitRunner.class)
public class CoordinateReferenceSystemsQueryTest {

	@InjectMocks
	private CoordinateReferenceSystemsQuery coordinateReferenceSystemsQuery;

	@Test
	public void testConstructQuery(){
		// arrange
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
		extent.setName("Mayonette");
		coordinateReferenceSystemsQuery.setExtent(extent);
		coordinateReferenceSystemsQuery.setCoordinateReferenceSystemType("GeodeticCRS");

		String expectedQuery = "(data.CodeSpace: EPSG) AND (data.Code: \"4472\") AND (data.Name: \"Cadastre\") AND (data.ID: \"Geographic3D:EPSG::4472\") AND (data.Kind: \"geographic 3D\") AND (data.CoordinateReferenceSystemType: \"GeodeticCRS\") AND (data.BaseCRS.BaseCRSID: \"osdu:reference-data--CoordinateReferenceSystem:Geocentric:EPSG::4473:\") AND (data.BaseCRS.Name: \"Cadastre\") AND (data.Datum.Name: \"Cadastre\") AND (data.Datum.AuthorityCode.Authority: \"EPSG\") AND (data.Datum.AuthorityCode.Code: \"1037\") AND (data.PreferredUsage.Extent.Name: \"Mayonette\")";

		// act
		String query = coordinateReferenceSystemsQuery.constructQuery();

		// assert
		Assert.assertEquals(expectedQuery, query);
	}

	@Test
	public void testConstructQueryBoundProjected(){
		// arrange
		coordinateReferenceSystemsQuery.setCodeSpace("EPSG");
		coordinateReferenceSystemsQuery.setReturnBoundProjectedAndProjectedBasedOnWgs84(true);
		String expectedQuery = "(data.CodeSpace: EPSG OR data.Codespace: EPSG OR data.PreferredUsage.Extent.AuthorityCode.Authority: EPSG) AND (data.Code: 4326 OR data.PreferredUsage.Extent.AuthorityCode.Authority: EPSG) AND (data.Kind: BoundProjected)";

		// act
		String query = coordinateReferenceSystemsQuery.constructQuery();

		// assert
		Assert.assertEquals(expectedQuery, query);
	}

	@Test
	public void testConstructQueryBoundGeographic(){
		// arrange
		coordinateReferenceSystemsQuery.setCodeSpace("EPSG");
		coordinateReferenceSystemsQuery.setReturnBoundGeographic2DAndWgs84(true);
		String expectedQuery = "(data.CodeSpace: EPSG OR data.Codespace: EPSG OR data.PreferredUsage.Extent.AuthorityCode.Authority: EPSG) AND (data.Code: 4326 OR data.PreferredUsage.Extent.AuthorityCode.Authority: EPSG) AND (data.Kind: BoundGeographic2d)";

		// act
		String query = coordinateReferenceSystemsQuery.constructQuery();

		// assert
		Assert.assertEquals(expectedQuery, query);
	}

	@Test
	public void testSpatialFilter(){
		// arrange
		double latitude = 10.56;
		double longitude = 88.8;
		coordinateReferenceSystemsQuery.setLatitude(latitude);
		coordinateReferenceSystemsQuery.setLongitude(longitude);

		// act
		SpatialFilter spatialFilter = coordinateReferenceSystemsQuery.constructSpatialFilter();

		// assert
		Assert.assertNotNull(spatialFilter.getByWithinPolygon());
		Assert.assertEquals(1, spatialFilter.getByWithinPolygon().getPoints().size());
		Assert.assertEquals(latitude, spatialFilter.getByWithinPolygon().getPoints().get(0).getLatitude(), 0d);
		Assert.assertEquals(longitude,spatialFilter.getByWithinPolygon().getPoints().get(0).getLongitude(), 0d);
	}
}
