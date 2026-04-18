package com.example.mcpclient.controller;

import com.example.mcpclient.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvcTest (slice) tests for DemoController.
 */
@WebMvcTest(DemoController.class)
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    // ── GET /demo/scenarios ───────────────────────────────────────────────────

    @Test
    void listScenarios_returnsAvailableScenarios() throws Exception {
        mockMvc.perform(get("/demo/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available_demos").exists())
                .andExpect(jsonPath("$.available_demos.weather").exists())
                .andExpect(jsonPath("$.available_demos.db-query").exists())
                .andExpect(jsonPath("$.available_demos.file-search").exists())
                .andExpect(jsonPath("$.available_demos.code-review").exists())
                .andExpect(jsonPath("$.available_demos.knowledge-qa").exists());
    }

    @Test
    void listScenarios_eachScenarioHasDescriptionAndCurl() throws Exception {
        mockMvc.perform(get("/demo/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available_demos.weather.description").exists())
                .andExpect(jsonPath("$.available_demos.weather.curl").exists());
    }

    // ── GET /demo/{demo} ──────────────────────────────────────────────────────

    @Test
    void fileSearchDemo_returnsDemoResponse() throws Exception {
        when(chatService.chat(anyString())).thenReturn("Found some files");
        mockMvc.perform(get("/demo/file-search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demo").value("file-search"))
                .andExpect(jsonPath("$.response").value("Found some files"));
    }

    @Test
    void dbQueryDemo_returnsDemoResponse() throws Exception {
        when(chatService.chat(anyString())).thenReturn("Here is the report");
        mockMvc.perform(get("/demo/db-query"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demo").value("db-query"))
                .andExpect(jsonPath("$.response").value("Here is the report"));
    }

    @Test
    void weatherDemo_returnsDemoResponse() throws Exception {
        when(chatService.chat(anyString())).thenReturn("Tokyo is sunny");
        mockMvc.perform(get("/demo/weather"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demo").value("weather"))
                .andExpect(jsonPath("$.response").value("Tokyo is sunny"));
    }

    @Test
    void codeReviewDemo_returnsDemoResponse() throws Exception {
        when(chatService.chat(anyString())).thenReturn("Code looks good");
        mockMvc.perform(get("/demo/code-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demo").value("code-review"));
    }

    @Test
    void knowledgeQaDemo_returnsDemoResponse() throws Exception {
        when(chatService.chat(anyString())).thenReturn("MCP is a protocol...");
        mockMvc.perform(get("/demo/knowledge-qa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demo").value("knowledge-qa"));
    }

    // ── POST /demo/{scenario} ─────────────────────────────────────────────────

    @Test
    void customDemo_withMessage_usesCustomPrompt() throws Exception {
        when(chatService.chat("my custom prompt")).thenReturn("custom response");
        mockMvc.perform(post("/demo/anything")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "my custom prompt"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demo").value("anything"))
                .andExpect(jsonPath("$.response").value("custom response"));
    }

    @Test
    void customDemo_knownScenarioWithoutMessage_delegatesToGetEndpoint() throws Exception {
        when(chatService.chat(anyString())).thenReturn("weather result");
        mockMvc.perform(post("/demo/weather")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demo").value("weather"));
    }

    @Test
    void customDemo_unknownScenarioWithoutMessage_returnsErrorResponse() throws Exception {
        mockMvc.perform(post("/demo/unknown-scenario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("Unknown scenario")));
    }

    @Test
    void customDemo_noBody_returnsErrorResponse() throws Exception {
        mockMvc.perform(post("/demo/unknown-scenario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("Unknown scenario")));
    }
}

