package com.skhuthon.team4.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    // 단건 푸시 알림 발송
    public void sendPushNotification(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 푸시 알림 발송 성공: {}", response);
        } catch (Exception e) {
            log.error("FCM 푸시 알림 발송 실패: {}", e.getMessage());
        }
    }
}