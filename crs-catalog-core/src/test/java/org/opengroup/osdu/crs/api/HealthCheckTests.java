package org.opengroup.osdu.crs.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class HealthCheckTests {

	private HealthCheck sut;

	@Before
	public void setup() {
		this.sut = new HealthCheck();
	}

	@Test
	public void should_returnHttp200_when_checkLiveness() {
		assertEquals(HttpStatus.OK, this.sut.livenessCheck().getStatusCode());
	}

	@Test
	public void should_returnHttp200_when_checkReadiness() {
		assertEquals(HttpStatus.OK, this.sut.readinessCheck().getStatusCode());
	}
}