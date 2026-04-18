package com.example.mcpclient.controller;

import com.example.mcpclient.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvcTest (slice) tests for ChatController — no Spring context, no Ollama, no MCP server required.
 */
@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    // ── POST /chat ────────────────────────────────────────────────────────────

    @Test
    void chat_validRequest_returnsOk() throws Exception {
        when(chatService.chat("Hello")).thenReturn("Hi there!");

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "Hello"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Hi there!"));
    }

    @Test
    void chat_emptyMessage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void chat_blankMessage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "   "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_nullMessage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── POST /chat/stream ─────────────────────────────────────────────────────

    @Test
    void chatStream_validRequest_returnsEventStream() throws Exception {
        when(chatService.chatStream("stream me")).thenReturn(Flux.just("token1", " token2"));

        mockMvc.perform(post("/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "stream me"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    void chatStream_nullMessage_usesEmptyString() throws Exception {
        when(chatService.chatStream("")).thenReturn(Flux.just("pong"));

        mockMvc.perform(post("/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isOk());
    }

    // ── POST /chat/system ─────────────────────────────────────────────────────

    @Test
    void chatWithSystem_validRequest_returnsResponse() throws Exception {
        when(chatService.chatWithSystem(eq("Be a pirate"), eq("Hello"))).thenReturn("Ahoy!");

        mockMvc.perform(post("/chat/system")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"system": "Be a pirate", "message": "Hello"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Ahoy!"));
    }

    @Test
    void chatWithSystem_nullSystem_usesEmptyString() throws Exception {
        when(chatService.chatWithSystem(eq(""), anyString())).thenReturn("OK");

        mockMvc.perform(post("/chat/system")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "Hello"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("OK"));
    }

    @Test
    void chatWithSystem_nullMessage_usesEmptyString() throws Exception {
        when(chatService.chatWithSystem(anyString(), eq(""))).thenReturn("Got nothing");

        mockMvc.perform(post("/chat/system")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"system": "sys"}
                                """))
                .andExpect(status().isOk());
    }
}

