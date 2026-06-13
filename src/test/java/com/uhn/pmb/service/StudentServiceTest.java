package com.uhn.pmb.service;

import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student buildStudent(Long id, String name) {
        User user = User.builder().email(name.toLowerCase() + "@test.com").build();
        return Student.builder()
                .id(id)
                .fullName(name)
                .nik("1234567890" + id)
                .phoneNumber("08" + id)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("getAllStudents - returns mapped list")
    void getAllStudents_returnsAll() {
        when(studentRepository.findAll())
                .thenReturn(Arrays.asList(buildStudent(1L, "Alice"), buildStudent(2L, "Bob")));

        List<Map<String, Object>> result = studentService.getAllStudents();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsKey("id");
        assertThat(result.get(0)).containsKey("fullName");
        verify(studentRepository).findAll();
    }

    @Test
    @DisplayName("getAllStudents - empty list returns empty")
    void getAllStudents_emptyRepo_returnsEmpty() {
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = studentService.getAllStudents();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getStudentById - found student returns data map")
    void getStudentById_found_returnsData() {
        Student s = buildStudent(1L, "Alice");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(s));

        Map<String, Object> result = studentService.getStudentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.get("fullName")).isEqualTo("Alice");
        assertThat(result.get("id")).isEqualTo(1L);
    }

    @Test
    @DisplayName("getStudentById - student has null user returns empty email")
    void getStudentById_nullUser_returnsEmptyEmail() {
        Student s = Student.builder().id(2L).fullName("Bob").nik("999").build();
        when(studentRepository.findById(2L)).thenReturn(Optional.of(s));

        Map<String, Object> result = studentService.getStudentById(2L);

        assertThat(result.get("email")).isEqualTo("");
    }

    @Test
    @DisplayName("getStudentById - not found throws RuntimeException")
    void getStudentById_notFound_throwsException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }
}
