package com.challenge.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.challenge.domain.user.User;
import com.challenge.dtos.NotificationDTO;
import com.challenge.infra.exceptions.NotificationServiceException;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private RestTemplate restTemplate;

    public void sendNotification(User user, String message) {
    String email = user.getEmail();
    NotificationDTO notificationRequest = new NotificationDTO(email, message);

    try {
        ResponseEntity<String> notificationResponse = restTemplate.postForEntity(
            "https://util.devi.tools/api/v1/notify",
            notificationRequest,
            String.class
        );

        if (!notificationResponse.getStatusCode().is2xxSuccessful()) {
            throw new NotificationServiceException("Falha ao enviar notificação. Status: " + 
                                                   notificationResponse.getStatusCode());
        }
    } catch (HttpServerErrorException | ResourceAccessException e) {
        // Log do erro
        logger.error("Erro ao enviar notificação: {}", e.getMessage());
        // Apenas logar a falha sem interromper o fluxo
    }
}

}
