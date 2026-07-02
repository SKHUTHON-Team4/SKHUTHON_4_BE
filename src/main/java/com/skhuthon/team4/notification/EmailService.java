package com.skhuthon.team4.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 일기 미작성 알림
    public void sendDiaryReminder(String toEmail, String nickname) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[청춘잇다] 오늘 하루를 기록해보세요 ✏️");
            helper.setFrom("청춘잇다 <noreply@cheongchun.com>");

            String html = """
                <div style="background-color: #f3f0fa; padding: 40px 0; font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;">
                    <div style="max-width: 480px; margin: 0 auto; background-color: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 4px 20px rgba(150, 120, 200, 0.15);">
                        
                        <!-- 헤더 -->
                        <div style="background: linear-gradient(135deg, #c9b8e8 0%%, #b8a0d8 100%%); padding: 36px 32px; text-align: center;">
                            <p style="color: #ffffff; font-size: 13px; letter-spacing: 2px; margin: 0 0 8px 0; opacity: 0.9;">✦ 청춘잇다 ✦</p>
                            <h1 style="color: #ffffff; font-size: 22px; margin: 0; font-weight: 700;">오늘 하루를 기록해보세요</h1>
                        </div>

                        <!-- 본문 -->
                        <div style="padding: 36px 32px;">
                            <p style="color: #6b5b95; font-size: 16px; font-weight: 600; margin: 0 0 12px 0;">
                                안녕하세요, %s님 👋
                            </p>
                            <p style="color: #777777; font-size: 14px; line-height: 1.9; margin: 0 0 28px 0;">
                                오늘 하루는 어떠셨나요?<br>
                                아직 일기를 작성하지 않으셨네요.<br>
                                단 한 줄이라도 오늘을 기록해보는 건 어떨까요?
                            </p>

                            <!-- 일기장 카드 -->
                            <div style="background-color: #faf8ff; border: 1px solid #e8e0f5; border-radius: 14px; padding: 20px 24px; margin-bottom: 28px;">
                                <p style="color: #9b8ec4; font-size: 13px; margin: 0 0 8px 0;">📖 오늘의 일기</p>
                                <p style="color: #cccccc; font-size: 14px; font-style: italic; margin: 0; line-height: 1.8;">
                                    오늘 하루를 한 줄로 남겨보세요...
                                </p>
                            </div>

                            <!-- 버튼 -->
                            <div style="text-align: center;">
                                <a href="https://cheongchun-v1.vercel.app"
                                   style="display: inline-block; background: linear-gradient(135deg, #c9b8e8 0%%, #9b8ec4 100%%);
                                          color: #ffffff; padding: 14px 40px; border-radius: 50px;
                                          text-decoration: none; font-size: 15px; font-weight: 700;
                                          letter-spacing: 0.5px; box-shadow: 0 4px 15px rgba(155, 142, 196, 0.4);">
                                    ✏️ 일기 쓰러 가기
                                </a>
                            </div>
                        </div>

                        <!-- 푸터 -->
                        <div style="background-color: #faf8ff; padding: 20px 32px; text-align: center; border-top: 1px solid #f0ebff;">
                            <p style="color: #b8a8d8; font-size: 12px; margin: 0; line-height: 1.8;">
                                청춘의 하루하루를 함께 기록해요 🌸<br>
                                <span style="color: #d0c4ee;">청춘잇다 팀 드림</span>
                            </p>
                        </div>
                    </div>
                </div>
                """.formatted(nickname);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("일기 알림 이메일 발송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("일기 알림 이메일 발송 실패: {} - {}", toEmail, e.getMessage());
        }
    }

    // AI 멘트 이메일
    public void sendAiComment(String toEmail, String nickname, String aiComment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[청춘잇다] AI가 오늘 하루를 응원합니다 💌");
            helper.setFrom("청춘잇다 <noreply@cheongchun.com>");

            String html = """
                <div style="background-color: #f3f0fa; padding: 40px 0; font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;">
                    <div style="max-width: 480px; margin: 0 auto; background-color: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 4px 20px rgba(150, 120, 200, 0.15);">

                        <!-- 헤더 -->
                        <div style="background: linear-gradient(135deg, #c9b8e8 0%%, #b8a0d8 100%%); padding: 36px 32px; text-align: center;">
                            <p style="color: #ffffff; font-size: 13px; letter-spacing: 2px; margin: 0 0 8px 0; opacity: 0.9;">✦ 청춘잇다 ✦</p>
                            <h1 style="color: #ffffff; font-size: 22px; margin: 0; font-weight: 700;">오늘 하루를 응원해요 💌</h1>
                        </div>

                        <!-- 본문 -->
                        <div style="padding: 36px 32px;">
                            <p style="color: #6b5b95; font-size: 16px; font-weight: 600; margin: 0 0 12px 0;">
                                안녕하세요, %s님 🌸
                            </p>
                            <p style="color: #777777; font-size: 14px; line-height: 1.9; margin: 0 0 24px 0;">
                                오늘 작성하신 일기를 AI가 읽고<br>
                                따뜻한 한 마디를 전해드려요.
                            </p>

                            <!-- AI 멘트 카드 -->
                            <div style="background: linear-gradient(135deg, #faf8ff 0%%, #f0ebff 100%%);
                                        border-left: 4px solid #c9b8e8; border-radius: 0 14px 14px 0;
                                        padding: 20px 24px; margin-bottom: 28px;">
                                <p style="color: #9b8ec4; font-size: 12px; margin: 0 0 10px 0; letter-spacing: 1px;">
                                    💜 AI의 한 마디
                                </p>
                                <p style="color: #555555; font-size: 15px; line-height: 1.9; margin: 0; font-style: italic;">
                                    %s
                                </p>
                            </div>

                            <!-- 구분선 -->
                            <div style="border-top: 1px solid #f0ebff; margin-bottom: 24px;"></div>

                            <p style="color: #999999; font-size: 13px; line-height: 1.8; margin: 0 0 24px 0; text-align: center;">
                                오늘도 청춘잇다와 함께<br>소중한 하루를 기록해보세요 🌙
                            </p>

                            <!-- 버튼 -->
                            <div style="text-align: center;">
                                <a href="https://cheongchun-v1.vercel.app"
                                   style="display: inline-block; background: linear-gradient(135deg, #c9b8e8 0%%, #9b8ec4 100%%);
                                          color: #ffffff; padding: 14px 40px; border-radius: 50px;
                                          text-decoration: none; font-size: 15px; font-weight: 700;
                                          letter-spacing: 0.5px; box-shadow: 0 4px 15px rgba(155, 142, 196, 0.4);">
                                    오늘 일기 쓰러 가기 ✨
                                </a>
                            </div>
                        </div>

                        <!-- 푸터 -->
                        <div style="background-color: #faf8ff; padding: 20px 32px; text-align: center; border-top: 1px solid #f0ebff;">
                            <p style="color: #b8a8d8; font-size: 12px; margin: 0; line-height: 1.8;">
                                청춘의 하루하루를 함께 기록해요 🌸<br>
                                <span style="color: #d0c4ee;">청춘잇다 팀 드림</span>
                            </p>
                        </div>
                    </div>
                </div>
                """.formatted(nickname, aiComment);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("AI 멘트 이메일 발송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("AI 멘트 이메일 발송 실패: {} - {}", toEmail, e.getMessage());
        }
    }
}