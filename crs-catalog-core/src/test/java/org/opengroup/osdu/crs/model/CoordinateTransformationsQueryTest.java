package org.opengroup.osdu.crs.model;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.search.SpatialFilter;
import org.opengroup.osdu.crs.model.request.CoordinateTransformationsQuery;

@RunWith(MockitoJUnitRunner.class)
public class CoordinateTransformationsQueryTest {

    @InjectMocks
    private CoordinateTransformationsQuery coordinateTransformationsQuery;

    @Test
    public void testConstructQuery() {
        // arrange
        coordinateTransformationsQuery.setCodeSpace("EPSG");
        coordinateTransformationsQuery.setName("Ain el");
        coordinateTransformationsQuery.setSourceCRS("osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG");
        coordinateTransformationsQuery.setTargetCRS("osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:");

        String expectedQuery = "(NOT data.InactiveIndicator: true) AND (NOT data.Kind: \"VerticalTransformation\") AND (data.CodeSpace: \"EPSG\") AND (data.Name: \"Ain el\") AND ((data.SourceCRS.SourceCRSID: \"osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG\") OR (data.TargetCRS.TargetCRSID: \"osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG\")) AND ((data.SourceCRS.SourceCRSID: \"osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:\") OR (data.TargetCRS.TargetCRSID: \"osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:\"))";
        // act
        String query = coordinateTransformationsQuery.constructQuery();

        // assert
        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void testConstructQueryVertical() {
        // arrange
        coordinateTransformationsQuery.setCodeSpace("EPSG");
        coordinateTransformationsQuery.setKind("VerticalTransformation");

        String expectedQuery = "(NOT data.InactiveIndicator: true) AND (data.Kind: \"VerticalTransformation\") AND (data.CodeSpace: \"EPSG\")";

        // act
        String query = coordinateTransformationsQuery.constructQuery();

        // assert
        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void testConstructQueryAll() {
        // arrange
        coordinateTransformationsQuery.setCodeSpace("EPSG");
        coordinateTransformationsQuery.setKind("All");

        String expectedQuery = "(NOT data.InactiveIndicator: true) AND (data.CodeSpace: \"EPSG\")";

        // act
        String query = coordinateTransformationsQuery.constructQuery();

        // assert
        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void testSpatialFilter() {
        // arrange
        double latitude = 10.56;
        double longitude = 88.8;
        coordinateTransformationsQuery.setLatitude(latitude);
        coordinateTransformationsQuery.setLongitude(longitude);

        // act
        SpatialFilter spatialFilter = coordinateTransformationsQuery.constructSpatialFilter();

        // assert
        Assert.assertNotNull(spatialFilter.getByWithinPolygon());
        Assert.assertEquals(1, spatialFilter.getByWithinPolygon().getPoints().size());
        Assert.assertEquals(latitude, spatialFilter.getByWithinPolygon().getPoints().get(0).getLatitude(), 0d);
        Assert.assertEquals(longitude, spatialFilter.getByWithinPolygon().getPoints().get(0).getLongitude(), 0d);
    }

}
