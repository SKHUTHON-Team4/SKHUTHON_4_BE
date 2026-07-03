package com.skhuthon.team4.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    AGE_REQUIRED(HttpStatus.BAD_REQUEST, "나이를 입력해주세요."),

    // Diary
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 일기입니다."),
    DIARY_ALREADY_EXISTS(HttpStatus.CONFLICT, "오늘 이미 일기를 작성했습니다."),
    DIARY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 일기만 삭제할 수 있습니다."),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 댓글만 삭제할 수 있습니다."),

    // Empathy
    EMPATHY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 공감한 일기입니다."),
    EMPATHY_NOT_FOUND(HttpStatus.NOT_FOUND, "공감한 적이 없는 일기입니다."),

    // Filter
    BAD_WORD_DETECTED(HttpStatus.BAD_REQUEST, "부적절한 내용이 포함되어 있습니다."),
    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 신고한 일기/댓글입니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 내역이 없습니다."),
    CANNOT_REPORT_OWN(HttpStatus.BAD_REQUEST, "본인의 글은 신고할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}