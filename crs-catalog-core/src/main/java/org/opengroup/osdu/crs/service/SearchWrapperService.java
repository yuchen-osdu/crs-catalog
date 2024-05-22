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
import org.opengroup.osdu.core.common.model.search.*;
import org.opengroup.osdu.core.common.search.ISearchFactory;
import org.opengroup.osdu.core.common.search.ISearchService;
import org.opengroup.osdu.crs.model.request.ISearchQuery;
import org.opengroup.osdu.crs.model.response.SearchResponse;
import org.opengroup.osdu.crs.util.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchWrapperService {

    @Inject
    ISearchFactory searchFactory;

    @Inject
    DpsHeaders dpsHeaders;

    @Inject
    JaxRsDpsLog logger;

    ISearchService searchService;

    private static String schemaAuthority;

    private int DEFAULT_QUERY_LIMIT=1000;

    public static String getCoordinateReferenceSystemKind() {
        return "%s:wks:reference-data--CoordinateReferenceSystem:1.1.0".formatted(schemaAuthority);
    }

    public static String getCoordinateTransformationKind() {
        return "%s:wks:reference-data--CoordinateTransformation:1.1.0".formatted(schemaAuthority);
    }

    public static String getCtAndCrsKind() {
        return "%s:wks:reference-data--*:1.1.0".formatted(schemaAuthority);
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
            query = "id: \"%s\" && data.ID: \"%s\"".formatted(recordId, dataId);
        } else if (recordId != null) {
            query = "id: \"%s\"".formatted(recordId);
        } else if (dataId != null) {
            query = "data.ID: \"%s\"".formatted(dataId);
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
    public SearchResponse searchWithCursor(ISearchQuery searchQuery, String kind) {
        CursorQueryRequest cursorqueryRequest = new CursorQueryRequest ();

        String query = searchQuery.constructQuery();
        cursorqueryRequest.setQuery(query);
        cursorqueryRequest.setKind(kind);

        List<String> returnedFields = searchQuery.getReturnedFields();
        if (returnedFields.size() > 0) {
            cursorqueryRequest.setReturnedFields(returnedFields);
        }

        SpatialFilter spatialFilter = searchQuery.constructSpatialFilter();
        if (spatialFilter != null) {
            cursorqueryRequest.setSpatialFilter(searchQuery.constructSpatialFilter());
        }

        if (searchQuery.getLimit() != null) {
            cursorqueryRequest.setLimit(searchQuery.getLimit());
        }

        return new SearchResponse(sendToSearchWithCursor(cursorqueryRequest), query);
    }
    private QueryResponse sendToSearch(QueryRequest queryRequest) {
        QueryResponse queryResponse = null;
        try {
            logger.debug("Sending query to search service: %s".formatted(queryRequest.toString()));
            queryResponse = searchService.search(queryRequest);
            List<Map<String, Object>> searchResultList = new ArrayList<Map<String, Object>>();
            searchResultList = queryResponse.getResults();
            if (queryRequest.getFrom() <= 0) {
                int default_Count = searchResultList.size();
                long totalCount = queryResponse.getTotalCount();
                while (default_Count < totalCount && queryRequest.getLimit() > DEFAULT_QUERY_LIMIT) {
                    queryRequest.setFrom(default_Count);
                    queryResponse = searchService.search(queryRequest);
                    searchResultList.addAll(queryResponse.getResults());
                    default_Count += default_Count;

                }
            }
            queryResponse.setResults(searchResultList);
            logger.debug("Received response from search service: %s".formatted(queryResponse.toString()));
        } catch (SearchException e) {
            handleSearchError("Failed to call search service", e);
        }
        return queryResponse;
    }
    private CursorQueryResponse sendToSearchWithCursor(CursorQueryRequest queryRequest) {
        CursorQueryResponse cursorqueryResponse = null;
        try {
            logger.debug("Sending query to search service: %s".formatted(queryRequest.toString()));
           
            cursorqueryResponse = searchService.searchCursor(queryRequest);
            List<Map<String, Object>> searchResultList = new ArrayList<Map<String, Object>>();
            searchResultList = cursorqueryResponse.getResults();
            String cursor_value = cursorqueryResponse.getCursor();
            int default_Count = searchResultList.size();
            long totalCount = cursorqueryResponse.getTotalCount();
            while (default_Count < totalCount && queryRequest.getLimit() > DEFAULT_QUERY_LIMIT) {
                queryRequest.setCursor(cursor_value);
                cursorqueryResponse = searchService.searchCursor(queryRequest);
                searchResultList.addAll(cursorqueryResponse.getResults());
                default_Count += default_Count;

            }
            cursorqueryResponse.setResults(searchResultList);
            logger.debug("Received response from search service: %s".formatted(cursorqueryResponse.toString()));
        } catch (SearchException e) {
            handleSearchError("Failed to call search service", e);
        }
        return cursorqueryResponse;
    }

    private void handleSearchError(String errorMsg, Exception e) {
        logger.error(errorMsg, e);
        e.printStackTrace();
        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMsg, e.getMessage());
    }
}
