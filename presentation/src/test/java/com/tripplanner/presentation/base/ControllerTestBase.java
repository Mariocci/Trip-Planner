package com.tripplanner.presentation.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Abstract base class for controller tests.
 * Provides common setup for MockMvc configuration and HTTP request helpers.
 * Includes helper methods for executing HTTP requests with authentication.
 */
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Execute a GET request with authentication.
     *
     * @param url Endpoint URL
     * @return ResultActions for further assertions
     * @throws Exception if request fails
     */
    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Execute a GET request with authentication and custom headers.
     *
     * @param url     Endpoint URL
     * @param headers Custom headers to add
     * @return ResultActions for further assertions
     * @throws Exception if request fails
     */
    protected ResultActions performGet(String url, String... headers) throws Exception {
        MockHttpServletRequestBuilder request = get(url)
                .contentType(MediaType.APPLICATION_JSON);

        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }

        return mockMvc.perform(request);
    }

    /**
     * Execute a POST request with authentication and request body.
     *
     * @param url  Endpoint URL
     * @param body Request body object
     * @return ResultActions for further assertions
     * @throws Exception if request fails
     */
    protected ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * Execute a POST request with authentication, request body, and custom headers.
     *
     * @param url     Endpoint URL
     * @param body    Request body object
     * @param headers Custom headers to add
     * @return ResultActions for further assertions
     * @throws Exception if request fails
     */
    protected ResultActions performPost(String url, Object body, String... headers) throws Exception {
        MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));

        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }

        return mockMvc.perform(request);
    }

    /**
     * Execute a PUT request with authentication and request body.
     *
     * @param url  Endpoint URL
     * @param body Request body object
     * @return ResultActions for further assertions
     * @throws Exception if request fails
     */
    protected ResultActions performPut(String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * Execute a PUT request with authentication, request body, and custom headers.
     *
     * @param url     Endpoint URL
     * @param body    Request body object
     * @param headers Custom headers to add
     * @return ResultActions for further assertions
     * @throws Exception if request fails
     */
    protected ResultActions performPut(String url, Object body, String... headers) throws Exception {
        MockHttpServletRequestBuilder request = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));

        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }

        return mockMvc.perform(request);
    }

    /**
     * Execute a DELETE request with authentication.
     *
     * @param url Endpoint URL
     * @return ResultActions for further assertions
     * @throws Exception if request fails
     */
    protected ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Execute a DELETE request with authentication and custom headers.
     *
     * @param url     Endpoint URL
     * @param headers Custom headers to add
     * @return ResultActions for further assertions
     * @throws Exception if request fails
     */
    protected ResultActions performDelete(String url, String... headers) throws Exception {
        MockHttpServletRequestBuilder request = delete(url)
                .contentType(MediaType.APPLICATION_JSON);

        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }

        return mockMvc.perform(request);
    }

    /**
     * Assert that the response status is 401 Unauthorized.
     *
     * @param resultActions ResultActions from request execution
     * @throws Exception if assertion fails
     */
    protected void expectUnauthorized(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isUnauthorized());
    }

    /**
     * Assert that the response status is 403 Forbidden.
     *
     * @param resultActions ResultActions from request execution
     * @throws Exception if assertion fails
     */
    protected void expectForbidden(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isForbidden());
    }

    /**
     * Assert that the response status is 404 Not Found.
     *
     * @param resultActions ResultActions from request execution
     * @throws Exception if assertion fails
     */
    protected void expectNotFound(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isNotFound());
    }

    /**
     * Assert that the response status is 400 Bad Request.
     *
     * @param resultActions ResultActions from request execution
     * @throws Exception if assertion fails
     */
    protected void expectBadRequest(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isBadRequest());
    }

    /**
     * Assert that the response status is 200 OK.
     *
     * @param resultActions ResultActions from request execution
     * @throws Exception if assertion fails
     */
    protected void expectOk(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isOk());
    }

    /**
     * Assert that the response status is 201 Created.
     *
     * @param resultActions ResultActions from request execution
     * @throws Exception if assertion fails
     */
    protected void expectCreated(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isCreated());
    }

    /**
     * Assert that the response status is 204 No Content.
     *
     * @param resultActions ResultActions from request execution
     * @throws Exception if assertion fails
     */
    protected void expectNoContent(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isNoContent());
    }
}
