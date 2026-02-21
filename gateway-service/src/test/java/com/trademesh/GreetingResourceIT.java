package com.trademesh;

import io.quarkus.test.junit.QuarkusIntegrationTest;

/**
 * Integration tests for GreetingResource.
 * Executes the same tests in a real container/environment.
 */
@QuarkusIntegrationTest
class GreetingResourceIT extends GreetingResourceTest {
    // Execute the same tests but in packaged mode.
}
