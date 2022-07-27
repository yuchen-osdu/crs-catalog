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
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.search.QueryRequest;
import org.opengroup.osdu.core.common.model.search.QueryResponse;
import org.opengroup.osdu.core.common.model.search.SearchException;
import org.opengroup.osdu.core.common.model.search.SpatialFilter;
import org.opengroup.osdu.core.common.search.ISearchFactory;
import org.opengroup.osdu.core.common.search.ISearchService;
import org.opengroup.osdu.crs.model.request.ISearchQuery;
import org.opengroup.osdu.crs.model.response.SearchResponse;
import org.opengroup.osdu.crs.util.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SearchWrapperService {

    @Inject
    ISearchFactory searchFactory;

    @Inject
    DpsHeaders dpsHeaders;

    @Inject
    JaxRsDpsLog logger;

    ISearchService searchService;

    public static String schemaAuthority;

    public static String getCoordinateReferenceSystemKind() {
        return String.format("%s:wks:reference-data--CoordinateReferenceSystem:1.1.0", schemaAuthority);
    }

    public static String getCoordinateTransformationKind() {
        return String.format("%s:wks:reference-data--CoordinateTransformation:1.1.0", schemaAuthority);
    }

    public static String getCtAndCrsKind() {
        return String.format("%s:wks:reference-data--*:1.1.0", schemaAuthority);
    }

    @PostConstruct
    public void postInit() {
        searchService = searchFactory.create(dpsHeaders);
    }

    @Value("${osdu.crs.catalog.schema-authority:osdu}")
    public void setSchemaAuthority(String privateName) {
        SearchWrapperService.schemaAuthority = privateName;
    }

    public SearchResponse getSingleCrsOrCt(String recordId, String dataId, String kind) {
        String query;
        if (recordId != null && dataId != null) {
            query = String.format("id: \"%s\" && data.ID: \"%s\"", recordId, dataId);
        } else if (recordId != null) {
            query = String.format("id: \"%s\"", recordId);
        } else if (dataId != null) {
            query = String.format("data.ID: \"%s\"", dataId);
        } else {
            throw AppException.createBadRequest("Must supply either recordId or dataId as request param");
        }

        QueryRequest queryRequest = new QueryRequest();

        queryRequest.setQuery(query);
        queryRequest.setKind(kind);

        return new SearchResponse(sendToSearch(queryRequest), query);
    }

    public SearchResponse search(ISearchQuery searchQuery, String kind) {
        QueryRequest queryRequest = new QueryRequest();

        String query = searchQuery.constructQuery();
        queryRequest.setQuery(query);
        queryRequest.setKind(kind);

        List<String> returnedFields = searchQuery.getReturnedFields();
        if (returnedFields.size() > 0) {
            queryRequest.setReturnedFields(returnedFields);
        }

        SpatialFilter spatialFilter = searchQuery.constructSpatialFilter();
        if (spatialFilter != null) {
            queryRequest.setSpatialFilter(searchQuery.constructSpatialFilter());
        }

        if (searchQuery.getLimit() != null) {
            queryRequest.setLimit(searchQuery.getLimit());
        }

        if (searchQuery.getOffset() != null) {
            queryRequest.setFrom(searchQuery.getOffset());
        }

        return new SearchResponse(sendToSearch(queryRequest), query);
    }

    private QueryResponse sendToSearch(QueryRequest queryRequest) {
        QueryResponse queryResponse = null;
        try {
            logger.debug(String.format("Sending query to search service: %s", queryRequest.toString()));
            queryResponse = searchService.search(queryRequest);
            logger.debug(String.format("Received response from search service: %s", queryResponse.toString()));
        } catch (SearchException e) {
            handleSearchError("Failed to call search service", e);
        }
        return queryResponse;
    }

    private void handleSearchError(String errorMsg, Exception e) {
        logger.error(errorMsg, e);
        e.printStackTrace();
        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMsg, e.getMessage());
    }
}
