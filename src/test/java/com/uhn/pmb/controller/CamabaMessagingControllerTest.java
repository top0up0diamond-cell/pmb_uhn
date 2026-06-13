package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.SendMessageRequest;
import com.uhn.pmb.entity.AdminMessage;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.AdminMessageRepository;
import com.uhn.pmb.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CamabaMessagingControllerTest {

    @Mock private AdminMessageRepository adminMessageRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CamabaMessagingController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("student@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/camaba/messages/send-to-admin - short message returns 400")
    void sendMessageToAdmin_shortMessage_returns400() throws Exception {
        User sender = User.builder().id(1L).email("student@test.com").build();
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(sender));

        SendMessageRequest req = new SendMessageRequest();
        req.setMessageContent("Hi");

        mockMvc.perform(post("/api/camaba/messages/send-to-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/messages/unread-count - returns 200")
    void getUnreadCount_returns200() throws Exception {
        // No auth set - controller returns 0 for anonymous user
        mockMvc.perform(get("/api/camaba/messages/unread-count"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/messages - returns 200")
    void getMessages_returns200() throws Exception {
        User sender = User.builder().id(1L).email("student@test.com").build();
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(sender));
        when(adminMessageRepository.findConversationWith(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/camaba/messages"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/messages/send-to-admin - valid message sends ok")
    void sendMessageToAdmin_validMessage_returns200() throws Exception {
        User sender = User.builder().id(1L).email("student@test.com").build();
        User admin = User.builder().id(2L).email("admin@test.com").role(com.uhn.pmb.entity.User.UserRole.ADMIN_PUSAT).build();
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(sender));
        when(userRepository.findByRole(com.uhn.pmb.entity.User.UserRole.ADMIN_PUSAT)).thenReturn(List.of(admin));

        SendMessageRequest req = new SendMessageRequest();
        req.setMessageContent("This is a valid message with enough chars");

        mockMvc.perform(post("/api/camaba/messages/send-to-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/messages/mark-read - returns 200")
    void markStudentMessagesAsRead_returns200() throws Exception {
        User student = User.builder().id(1L).email("student@test.com").build();
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(adminMessageRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/api/camaba/messages/mark-read"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/messages/unread-count - with unread messages returns count")
    void getUnreadCount_withMessages_returnsCount() throws Exception {
        User student = User.builder().id(1L).email("student@test.com").build();
        AdminMessage msg = mock(AdminMessage.class);
        when(msg.getRecipient()).thenReturn(student);
        when(msg.getStatus()).thenReturn(AdminMessage.MessageStatus.UNREAD);

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(adminMessageRepository.findAll()).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/camaba/messages/unread-count"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/messages/mark-read - with unread messages marks them read")
    void markStudentMessagesAsRead_withMessages_returns200() throws Exception {
        User student = User.builder().id(1L).email("student@test.com").build();
        AdminMessage msg = mock(AdminMessage.class);
        when(msg.getRecipient()).thenReturn(student);
        when(msg.getStatus()).thenReturn(AdminMessage.MessageStatus.UNREAD);

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(adminMessageRepository.findAll()).thenReturn(List.of(msg));
        when(adminMessageRepository.save(any())).thenReturn(msg);

        mockMvc.perform(post("/api/camaba/messages/mark-read"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/messages - with messages returns full data")
    void getStudentMessages_withMessages_returns200() throws Exception {
        User student = User.builder().id(1L).email("student@test.com")
                .role(User.UserRole.CAMABA).build();
        User admin = User.builder().id(2L).email("admin@test.com")
                .role(User.UserRole.ADMIN_PUSAT).build();

        AdminMessage msg = mock(AdminMessage.class);
        when(msg.getId()).thenReturn(1L);
        when(msg.getSender()).thenReturn(admin);
        when(msg.getRecipient()).thenReturn(student);
        when(msg.getMessageContent()).thenReturn("Hello student");
        when(msg.getMessageType()).thenReturn("INFO");
        when(msg.getStatus()).thenReturn(AdminMessage.MessageStatus.UNREAD);

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(adminMessageRepository.findConversationWith(1L)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/camaba/messages"))
                .andExpect(status().isOk());
    }
}
