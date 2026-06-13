package com.uhn.pmb.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitRevisionRequest {

    private String fullName;
    private String nik;
    private String birthDate;
    private String birthPlace;
    private String gender;
    private String phoneNumber;
    private String email;

    private String addressMedan;
    private String residenceInfo;
    private String subdistrict;
    private String district;
    private String city;
    private String province;
    private String religion;
    private String informationSource;

    private String fatherNik;
    private String fatherName;
    private String fatherBirthDate;
    private String fatherEducation;
    private String fatherOccupation;
    private String fatherIncome;
    private String fatherPhone;
    private String fatherStatus;

    private String motherNik;
    private String motherName;
    private String motherBirthDate;
    private String motherEducation;
    private String motherOccupation;
    private String motherIncome;
    private String motherPhone;
    private String motherStatus;

    private String parentSubdistrict;
    private String parentCity;
    private String parentProvince;
    private String parentPhone;

    private String schoolOrigin;
    private String schoolMajor;
    private String schoolYear;
    private String nisn;
    private String schoolCity;
    private String schoolProvince;

    private MultipartFile photoId;
    private MultipartFile certificate;
    private MultipartFile transcript;
}