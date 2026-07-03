package com.skhuthon.team4.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DiaryRequestDto(

        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 50, message = "제목은 50자 이내로 입력해주세요.")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(min = 100, max = 500, message = "내용은 100자 이상 500자 이하로 입력해주세요.")
        @Pattern(regexp = "^(?!\\s{10,}).*$", message = "공백을 10자 이상 연속으로 입력할 수 없어요.")
        String content,

        Boolean isPublic,

        Integer emotion
) {}