package com.uhn.pmb.task;

import com.uhn.pmb.entity.PublicationSchedule;
import com.uhn.pmb.entity.RegistrationPeriod;
import com.uhn.pmb.repository.PublicationScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationScheduleTaskTest {

    @Mock
    private PublicationScheduleRepository publicationScheduleRepository;

    private PublicationScheduleTask task;

    @BeforeEach
    void setUp() {
        task = new PublicationScheduleTask();
        ReflectionTestUtils.setField(task, "publicationScheduleRepository", publicationScheduleRepository);
    }

    @Test
    @DisplayName("checkAndPublishScheduledResults - no pending publications does nothing")
    void checkAndPublishScheduledResults_noPending_doesNothing() {
        when(publicationScheduleRepository.findByIsPublishedFalseAndPublishDateTimeBefore(any()))
                .thenReturn(Collections.emptyList());

        task.checkAndPublishScheduledResults();

        verify(publicationScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkAndPublishScheduledResults - pending publications marks published")
    void checkAndPublishScheduledResults_hasPending_marksPublished() {
        RegistrationPeriod period = RegistrationPeriod.builder()
                .id(1L)
                .name("Gelombang 1")
                .build();
        PublicationSchedule schedule = PublicationSchedule.builder()
                .id(1L)
                .period(period)
                .isPublished(false)
                .publishDateTime(LocalDateTime.now().minusMinutes(5))
                .build();

        when(publicationScheduleRepository.findByIsPublishedFalseAndPublishDateTimeBefore(any()))
                .thenReturn(List.of(schedule));
        when(publicationScheduleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        task.checkAndPublishScheduledResults();

        assertThat(schedule.getIsPublished()).isTrue();
        assertThat(schedule.getPublishedAt()).isNotNull();
        verify(publicationScheduleRepository).save(schedule);
    }

    @Test
    @DisplayName("checkAndPublishScheduledResults - multiple pending publications marks all")
    void checkAndPublishScheduledResults_multiplePending_marksAll() {
        RegistrationPeriod period1 = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        RegistrationPeriod period2 = RegistrationPeriod.builder().id(2L).name("Gel 2").build();
        PublicationSchedule s1 = PublicationSchedule.builder()
                .id(1L).period(period1).isPublished(false)
                .publishDateTime(LocalDateTime.now().minusMinutes(10)).build();
        PublicationSchedule s2 = PublicationSchedule.builder()
                .id(2L).period(period2).isPublished(false)
                .publishDateTime(LocalDateTime.now().minusMinutes(5)).build();

        when(publicationScheduleRepository.findByIsPublishedFalseAndPublishDateTimeBefore(any()))
                .thenReturn(List.of(s1, s2));
        when(publicationScheduleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        task.checkAndPublishScheduledResults();

        assertThat(s1.getIsPublished()).isTrue();
        assertThat(s2.getIsPublished()).isTrue();
        verify(publicationScheduleRepository).save(s1);
        verify(publicationScheduleRepository).save(s2);
    }
}
