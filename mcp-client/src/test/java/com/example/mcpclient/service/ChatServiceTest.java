package com.example.mcpclient.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChatService using Mockito to mock the ChatClient fluent API.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    // ── Mocks for the ChatClient fluent builder chain ─────────────────────────

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callSpec;

    @Mock
    private ChatClient.StreamResponseSpec streamSpec;

    @InjectMocks
    private ChatService chatService;

    // ── chat() ────────────────────────────────────────────────────────────────

    @Test
    void chat_returnsChatClientContent() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("AI response");

        String result = chatService.chat("What is the weather?");

        assertThat(result).isEqualTo("AI response");
        verify(requestSpec).user("What is the weather?");
    }

    @Test
    void chat_emptyMessage_stillCallsClient() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("");

        String result = chatService.chat("");
        assertThat(result).isEmpty();
    }

    // ── chatWithSystem() ──────────────────────────────────────────────────────

    @Test
    void chatWithSystem_setsSystemAndUser() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("Pirate says: Ahoy!");

        String result = chatService.chatWithSystem("Be a pirate", "Hello");

        assertThat(result).isEqualTo("Pirate says: Ahoy!");
        verify(requestSpec).system("Be a pirate");
        verify(requestSpec).user("Hello");
    }

    @Test
    void chatWithSystem_emptySystemMessage_stillCalls() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("response");

        String result = chatService.chatWithSystem("", "question");
        assertThat(result).isEqualTo("response");
    }

    // ── chatStream() ──────────────────────────────────────────────────────────

    @Test
    void chatStream_returnsFluxOfTokens() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.stream()).thenReturn(streamSpec);
        when(streamSpec.content()).thenReturn(Flux.just("token1", " token2", " token3"));

        Flux<String> result = chatService.chatStream("Tell me a story");

        StepVerifier.create(result)
                .expectNext("token1")
                .expectNext(" token2")
                .expectNext(" token3")
                .verifyComplete();

        verify(requestSpec).user("Tell me a story");
    }

    @Test
    void chatStream_emptyFlux_completesImmediately() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.stream()).thenReturn(streamSpec);
        when(streamSpec.content()).thenReturn(Flux.empty());

        StepVerifier.create(chatService.chatStream(""))
                .verifyComplete();
    }
}


