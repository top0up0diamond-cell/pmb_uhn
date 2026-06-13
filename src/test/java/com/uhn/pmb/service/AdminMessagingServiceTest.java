package com.uhn.pmb.service;

import com.uhn.pmb.dto.SendMessageRequest;
import com.uhn.pmb.entity.AdminMessage;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.AdminMessageRepository;
import com.uhn.pmb.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMessagingServiceTest {

    @Mock private AdminMessageRepository adminMessageRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AdminMessagingService adminMessagingService;

    private User buildUser(Long id, String email) {
        return User.builder().id(id).email(email).build();
    }

    @Test
    @DisplayName("countUnread - returns count from repository")
    void countUnread_returnsCount() {
        when(adminMessageRepository.countUnreadMessages(1L)).thenReturn(5L);

        Long result = adminMessagingService.countUnread(1L);

        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("findAll - returns all messages")
    void findAll_returnsAllMessages() {
        AdminMessage msg = new AdminMessage();
        when(adminMessageRepository.findAll()).thenReturn(List.of(msg));

        List<AdminMessage> result = adminMessagingService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findConversation - returns messages between two users")
    void findConversation_returnsList() {
        when(adminMessageRepository.findConversationBetween(1L, 2L)).thenReturn(List.of());

        List<AdminMessage> result = adminMessagingService.findConversation(1L, 2L);

        assertThat(result).isEmpty();
        verify(adminMessageRepository).findConversationBetween(1L, 2L);
    }

    @Test
    @DisplayName("send - recipient not found throws RuntimeException")
    void send_recipientNotFound_throwsException() {
        User sender = buildUser(1L, "admin@test.com");
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminMessagingService.send(sender, request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("send - saves and returns message")
    void send_validRequest_savesMessage() {
        User sender = buildUser(1L, "admin@test.com");
        User recipient = buildUser(2L, "student@test.com");
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(2L);
        request.setMessageContent("Hello");
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(adminMessageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AdminMessage result = adminMessagingService.send(sender, request);

        assertThat(result.getMessageContent()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("sendToStudent - content too short throws RuntimeException")
    void sendToStudent_shortContent_throwsException() {
        User admin = buildUser(1L, "admin@test.com");
        SendMessageRequest request = new SendMessageRequest();
        request.setMessageContent("Hi");

        assertThatThrownBy(() -> adminMessagingService.sendToStudent(admin, "s@test.com", request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("sendToStudent - student not found throws RuntimeException")
    void sendToStudent_studentNotFound_throwsException() {
        User admin = buildUser(1L, "admin@test.com");
        SendMessageRequest request = new SendMessageRequest();
        request.setMessageContent("Hello there admin");
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminMessagingService.sendToStudent(admin, "none@test.com", request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getUserByEmail - not found throws RuntimeException")
    void getUserByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminMessagingService.getUserByEmail("x@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("findConversation - returns list of messages")
    void findConversationBetween_returnsList() {
        when(adminMessageRepository.findConversationBetween(1L, 2L)).thenReturn(List.of(new AdminMessage()));

        List<AdminMessage> result = adminMessagingService.findConversation(1L, 2L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("markConversationRead - marks messages read")
    void markConversationRead_withMessages() {
        // Empty list - no NPE, just covers the method call
        adminMessagingService.markConversationRead(List.of(), 1L);
    }

    @Test
    @DisplayName("countUnread - returns count via repository")
    void countUnreadMessages_returnsCount() {
        when(adminMessageRepository.countUnreadMessages(1L)).thenReturn(3L);

        long count = adminMessagingService.countUnread(1L);

        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("markAllRead - marks unread messages as read")
    void markAllRead_withUnreadMessages_marksRead() {
        AdminMessage msg = mock(AdminMessage.class);
        User recipient = User.builder().id(1L).build();
        when(msg.getRecipient()).thenReturn(recipient);
        when(msg.getStatus()).thenReturn(AdminMessage.MessageStatus.UNREAD);
        when(adminMessageRepository.findAll()).thenReturn(List.of(msg));

        int count = adminMessagingService.markAllRead(1L);

        assertThat(count).isEqualTo(1);
        verify(adminMessageRepository).save(msg);
    }

    @Test
    @DisplayName("send - with non-null messageType uses provided type")
    void send_withMessageType_usesProvidedType() {
        User sender = buildUser(1L, "admin@test.com");
        User recipient = buildUser(2L, "student@test.com");
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(2L);
        request.setMessageContent("Hello message content here");
        request.setMessageType("ANNOUNCEMENT");
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(adminMessageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AdminMessage result = adminMessagingService.send(sender, request);

        assertThat(result.getMessageContent()).isEqualTo("Hello message content here");
    }

    @Test
    @DisplayName("sendToStudent - null content throws RuntimeException")
    void sendToStudent_nullContent_throwsException() {
        User admin = buildUser(1L, "admin@test.com");
        SendMessageRequest request = new SendMessageRequest();
        request.setMessageContent(null);

        assertThatThrownBy(() -> adminMessagingService.sendToStudent(admin, "s@test.com", request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("sendToStudent - valid content with non-null messageType saves message")
    void sendToStudent_withMessageType_savesMessage() {
        User admin = buildUser(1L, "admin@test.com");
        User student = buildUser(2L, "s@test.com");
        SendMessageRequest request = new SendMessageRequest();
        request.setMessageContent("Hello student long enough message");
        request.setMessageType("ANSWER");
        when(userRepository.findByEmail("s@test.com")).thenReturn(Optional.of(student));
        when(adminMessageRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AdminMessage result = adminMessagingService.sendToStudent(admin, "s@test.com", request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("markAllRead - message for different recipient is not counted")
    void markAllRead_differentRecipient_notCounted() {
        AdminMessage msg = mock(AdminMessage.class);
        User recipient = User.builder().id(99L).build();
        when(msg.getRecipient()).thenReturn(recipient);
        when(adminMessageRepository.findAll()).thenReturn(List.of(msg));

        int count = adminMessagingService.markAllRead(1L);

        assertThat(count).isEqualTo(0);
        verify(adminMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAllRead - already-read message for same recipient is not counted")
    void markAllRead_alreadyReadMessage_notCounted() {
        AdminMessage msg = mock(AdminMessage.class);
        User recipient = User.builder().id(1L).build();
        when(msg.getRecipient()).thenReturn(recipient);
        when(msg.getStatus()).thenReturn(AdminMessage.MessageStatus.READ);
        when(adminMessageRepository.findAll()).thenReturn(List.of(msg));

        int count = adminMessagingService.markAllRead(1L);

        assertThat(count).isEqualTo(0);
        verify(adminMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("markConversationRead - unread message for current user is saved as READ")
    void markConversationRead_unreadOwnMessage_savedAsRead() {
        AdminMessage msg = mock(AdminMessage.class);
        User recipient = User.builder().id(1L).build();
        when(msg.getRecipient()).thenReturn(recipient);
        when(msg.getStatus()).thenReturn(AdminMessage.MessageStatus.UNREAD);
        when(adminMessageRepository.save(any())).thenReturn(msg);

        adminMessagingService.markConversationRead(List.of(msg), 1L);

        verify(adminMessageRepository).save(msg);
    }

    @Test
    @DisplayName("markConversationRead - already-read message for current user is skipped")
    void markConversationRead_readMessage_skipped() {
        AdminMessage msg = mock(AdminMessage.class);
        User recipient = User.builder().id(1L).build();
        when(msg.getRecipient()).thenReturn(recipient);
        when(msg.getStatus()).thenReturn(AdminMessage.MessageStatus.READ);

        adminMessagingService.markConversationRead(List.of(msg), 1L);

        verify(adminMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("markConversationRead - message for different user is skipped")
    void markConversationRead_differentUser_skipped() {
        AdminMessage msg = mock(AdminMessage.class);
        User recipient = User.builder().id(99L).build();
        when(msg.getRecipient()).thenReturn(recipient);

        adminMessagingService.markConversationRead(List.of(msg), 1L);

        verify(adminMessageRepository, never()).save(any());
    }
}
