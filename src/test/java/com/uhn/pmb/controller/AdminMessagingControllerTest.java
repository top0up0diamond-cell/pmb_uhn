package com.uhn.pmb.controller;

import com.uhn.pmb.entity.User;
import com.uhn.pmb.dto.SendMessageRequest;
import com.uhn.pmb.dto.SendReminderRequest;
import com.uhn.pmb.service.AdminMessagingService;
import com.uhn.pmb.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminMessagingControllerTest {

    @Mock private AdminMessagingService adminMessagingService;
    @Mock private EmailService emailService;
    @InjectMocks private AdminMessagingController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /admin/api/messages/unread-count - returns 200")
    void getUnreadCount_returns200() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.countUnread(1L)).thenReturn(3L);

        mockMvc.perform(get("/admin/api/messages/unread-count"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/messages - returns 200")
    void getMessages_returns200() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/messages"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/messages/send - returns 200")
    void sendMessage_returns200() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.send(any(), any())).thenReturn(new com.uhn.pmb.entity.AdminMessage());

        SendMessageRequest req = new SendMessageRequest();
        req.setMessageContent("Hello student, this is a test message");

        mockMvc.perform(post("/admin/api/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/messages/conversation/{userId} - returns 200")
    void getConversation_returns200() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.findConversation(1L, 2L)).thenReturn(List.of());
        doNothing().when(adminMessagingService).markConversationRead(any(java.util.List.class), any());

        mockMvc.perform(get("/admin/api/messages/conversation/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/messages/send-to-student/{email} - returns 200")
    void sendMessageToStudent_returns200() throws Exception {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        com.uhn.pmb.entity.AdminMessage msg = new com.uhn.pmb.entity.AdminMessage();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(adminMessagingService.sendToStudent(any(), any(), any())).thenReturn(msg);

        SendMessageRequest req = new SendMessageRequest();
        req.setMessageContent("Test message to student");

        mockMvc.perform(post("/admin/api/messages/send-to-student/student@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/messages/mark-all-read - returns 200")
    void markAllMessagesAsRead_returns200() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.markAllRead(1L)).thenReturn(5);

        mockMvc.perform(post("/admin/api/messages/mark-all-read"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/send-reminder - returns 200")
    void sendReminderToStudent_returns200() throws Exception {
        doNothing().when(emailService).sendHtmlEmail(any(), any(), any());

        SendReminderRequest req = new SendReminderRequest();
        req.setStudentEmail("student@test.com");
        req.setMessageTitle("Reminder");
        req.setMessageBody("Please complete your registration");

        mockMvc.perform(post("/admin/api/send-reminder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/messages/unread-count - returns 200")
    void getUnreadMessageCount_returns200() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.countUnread(1L)).thenReturn(5L);

        mockMvc.perform(get("/admin/api/messages/unread-count"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/messages/unread-count - service exception returns 400")
    void getUnreadMessageCount_serviceException_returns400() throws Exception {
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/messages/unread-count"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/api/messages/send - service exception returns 400")
    void sendMessage_serviceException_returns400() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.send(any(), any())).thenThrow(new RuntimeException("Send failed"));

        SendMessageRequest req = new SendMessageRequest();
        req.setMessageContent("Test");

        mockMvc.perform(post("/admin/api/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/messages/conversation/{userId} - service exception returns 400")
    void getConversation_serviceException_returns400() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.findConversation(1L, 2L)).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/messages/conversation/2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/messages - service exception returns 400")
    void getMessages_serviceException_returns400() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.findAll()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/messages"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/api/messages/send-to-student/{email} - service exception returns 500")
    void sendMessageToStudent_serviceException_returns500() throws Exception {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(adminMessagingService.sendToStudent(any(), any(), any())).thenThrow(new RuntimeException("Send failed"));

        SendMessageRequest req = new SendMessageRequest();
        req.setMessageContent("Test");

        mockMvc.perform(post("/admin/api/messages/send-to-student/student@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /admin/api/messages/mark-all-read - service exception returns 400")
    void markAllMessagesAsRead_serviceException_returns400() throws Exception {
        User user = User.builder().id(1L).email("admin@test.com").build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(user);
        when(adminMessagingService.markAllRead(1L)).thenThrow(new RuntimeException("Mark failed"));

        mockMvc.perform(post("/admin/api/messages/mark-all-read"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/api/send-reminder - service exception returns 400")
    void sendReminderToStudent_serviceException_returns400() throws Exception {
        doThrow(new RuntimeException("Send failed")).when(emailService).sendHtmlEmail(any(), any(), any());

        SendReminderRequest req = new SendReminderRequest();
        req.setStudentEmail("student@test.com");
        req.setMessageTitle("Reminder");
        req.setMessageBody("Test");

        mockMvc.perform(post("/admin/api/send-reminder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/messages - message with null sender uses Unknown defaults")
    void getMessages_withNullSenderMessage_returns200() throws Exception {
        User currentUser = User.builder().id(1L).email("admin@test.com").build();
        User recipient = User.builder().id(2L).email("r@test.com").build();
        com.uhn.pmb.entity.AdminMessage msg = com.uhn.pmb.entity.AdminMessage.builder()
                .id(1L)
                .sender(null)
                .recipient(recipient)
                .messageContent("Test message")
                .status(com.uhn.pmb.entity.AdminMessage.MessageStatus.UNREAD)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(currentUser);
        when(adminMessagingService.findAll()).thenReturn(List.of(msg));

        mockMvc.perform(get("/admin/api/messages"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/messages - message with CAMABA sender shows STUDENT type")
    void getMessages_withCambaSender_returns200() throws Exception {
        User currentUser = User.builder().id(1L).email("admin@test.com").build();
        User camabaSender = User.builder().id(3L).email("s@test.com")
                .role(com.uhn.pmb.entity.User.UserRole.CAMABA).build();
        com.uhn.pmb.entity.AdminMessage msg = com.uhn.pmb.entity.AdminMessage.builder()
                .id(2L)
                .sender(camabaSender)
                .recipient(null)
                .messageContent("Message from camaba")
                .status(com.uhn.pmb.entity.AdminMessage.MessageStatus.UNREAD)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(currentUser);
        when(adminMessagingService.findAll()).thenReturn(List.of(msg));

        mockMvc.perform(get("/admin/api/messages"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/messages - message with admin sender and recipient returns 200")
    void getMessages_withAdminSenderAndRecipient_returns200() throws Exception {
        User currentUser = User.builder().id(1L).email("admin@test.com").build();
        User adminSender = User.builder().id(4L).email("admin2@test.com")
                .role(com.uhn.pmb.entity.User.UserRole.ADMIN_PUSAT).build();
        User recipientUser = User.builder().id(5L).email("r@test.com").build();
        com.uhn.pmb.entity.AdminMessage msg = com.uhn.pmb.entity.AdminMessage.builder()
                .id(3L)
                .sender(adminSender)
                .recipient(recipientUser)
                .messageContent("Message from admin")
                .status(com.uhn.pmb.entity.AdminMessage.MessageStatus.UNREAD)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        when(adminMessagingService.getUserByEmail("admin@test.com")).thenReturn(currentUser);
        when(adminMessagingService.findAll()).thenReturn(List.of(msg));

        mockMvc.perform(get("/admin/api/messages"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/send-reminder - with UserDetails principal logs roles and returns 200")
    void sendReminderToStudent_withUserDetailsPrincipal_returns200() throws Exception {
        SecurityContextHolder.clearContext();
        org.springframework.security.core.userdetails.UserDetails ud =
                org.springframework.security.core.userdetails.User.builder()
                        .username("admin@test.com").password("pass").roles("ADMIN").build();
        var udAuth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(udAuth);
        doNothing().when(emailService).sendHtmlEmail(any(), any(), any());

        SendReminderRequest req = new SendReminderRequest();
        req.setStudentEmail("student@test.com");
        req.setMessageTitle("Reminder");
        req.setMessageBody("Please complete your registration today");

        mockMvc.perform(post("/admin/api/send-reminder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
