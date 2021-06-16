package org.opengroup.osdu.crs.middleware;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.util.AppError;
import org.opengroup.osdu.crs.util.AppException;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionMapperTests {

    @Mock
    private JaxRsDpsLog log;

    @InjectMocks
    private GlobalExceptionMapper sut;

    @Test
    public void should_useValuesInAppExceptionInResponse_When_AppExceptionIsHandledByGlobalExceptionMapper() throws Exception {
        AppException exception = new AppException(409, "any reason", "any message");

        ResponseEntity<AppError> response = sut.handleAppException(exception);
        AppError body = response.getBody();
        assertNotNull(body);
        assertEquals(409, body.getCode());
        assertEquals("any message", body.getMessage());
        assertEquals("any reason", body.getReason());
        Mockito.verify(log).error(Mockito.any(), Mockito.any(AppException.class));
    }

    @Test
    public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {
        Exception exception = new Exception("any message");

        ResponseEntity<AppError> response = sut.handleGeneralException(exception);
        AppError body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getCode());
        assertEquals("An unknown error has occurred.", body.getMessage());
        assertEquals("Server error.", body.getReason());
        Mockito.verify(log).error(Mockito.any(), Mockito.any(AppException.class));
    }

    @Test
    public void shouldReturnBadRequest() {
        AppException exception = AppException.createBadRequest("Bad request");

        ResponseEntity<AppError> response = sut.handleAppException(exception);
        AppError body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getCode());
        assertEquals("Bad request", body.getMessage());
        assertEquals("Bad Request", body.getReason());
        Mockito.verify(log).error(Mockito.any(), Mockito.any(AppException.class));
    }
}
