package org.opengroup.osdu.crs.middleware;

import org.opengroup.osdu.crs.util.AppError;
import org.opengroup.osdu.crs.util.AppException;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GlobalExceptionMapperTests {

    @Test
    public void should_useValuesInAppExceptionInResponse_When_AppExceptionIsHandledByGlobalExceptionMapper() throws Exception {
        GlobalExceptionMapper sut = new GlobalExceptionMapper();
        AppException exception = new AppException(409, "any reason", "any message");

        ResponseEntity<AppError> response = sut.handleAppException(exception);
        AppError body = response.getBody();
        assertNotNull(body);
        assertEquals(409, body.getCode());
        assertEquals("any message", body.getMessage());
        assertEquals("any reason", body.getReason());
    }

    @Test
    public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {
        GlobalExceptionMapper sut = new GlobalExceptionMapper();
        Exception exception = new Exception("any message");

        ResponseEntity<AppError> response = sut.handleGeneralException(exception);
        AppError body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getCode());
        assertEquals("An unknown error has occurred.", body.getMessage());
        assertEquals("Server error.", body.getReason());
    }

    @Test
    public void shouldReturnBadRequest() {
        GlobalExceptionMapper sut = new GlobalExceptionMapper();
        AppException exception = AppException.createBadRequest("Bad request");

        ResponseEntity<AppError> response = sut.handleAppException(exception);
        AppError body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getCode());
        assertEquals("Bad request", body.getMessage());
        assertEquals("Bad Request", body.getReason());
    }
}
