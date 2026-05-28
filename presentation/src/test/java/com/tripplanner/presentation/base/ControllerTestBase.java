package com.tripplanner.presentation.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    
    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    
    protected ResultActions performGet(String url, String... headers) throws Exception {
        MockHttpServletRequestBuilder request = get(url)
                .contentType(MediaType.APPLICATION_JSON);

        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }

        return mockMvc.perform(request);
    }

    
    protected ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    
    protected ResultActions performPost(String url, Object body, String... headers) throws Exception {
        MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));

        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }

        return mockMvc.perform(request);
    }

    
    protected ResultActions performPut(String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    
    protected ResultActions performPut(String url, Object body, String... headers) throws Exception {
        MockHttpServletRequestBuilder request = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));

        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }

        return mockMvc.perform(request);
    }

    
    protected ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    
    protected ResultActions performDelete(String url, String... headers) throws Exception {
        MockHttpServletRequestBuilder request = delete(url)
                .contentType(MediaType.APPLICATION_JSON);

        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }

        return mockMvc.perform(request);
    }

    
    protected void expectUnauthorized(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isUnauthorized());
    }

    
    protected void expectForbidden(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isForbidden());
    }

    
    protected void expectNotFound(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isNotFound());
    }

    
    protected void expectBadRequest(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isBadRequest());
    }

    
    protected void expectOk(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isOk());
    }

    
    protected void expectCreated(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isCreated());
    }

    
    protected void expectNoContent(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isNoContent());
    }
}
