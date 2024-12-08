package com.challenge.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.challenge.domain.transaction.Transaction;
import com.challenge.domain.user.User;
import com.challenge.dtos.TransactionDTO;
import com.challenge.repositories.TransactionRepository;


@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NotificationService notificationService;

    public Transaction createTransaction(TransactionDTO transaction) throws Exception {
    
        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());
    
        
        userService.validateTransaction(sender, transaction.value());
    
        
        boolean isAuthorized = this.authorizeTransaction(sender, transaction.value());
        if (!isAuthorized) {
            throw new Exception("Transação não autorizada.");
        }
    
        
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transaction.value());
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setTimestamp(LocalDateTime.now());
    
        
        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));
    
        
        this.repository.save(newTransaction);
        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);
    
        
        try {
            this.notificationService.sendNotification(sender, "Transação realizada com sucesso!");
            this.notificationService.sendNotification(receiver, "Você recebeu uma nova transação!");
        } catch (Exception e) {
            
            logger.warn("Erro ao enviar notificações: {}", e.getMessage());
        }
    
        return newTransaction;
    }
    
    public boolean authorizeTransaction(User sender, BigDecimal value) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://util.devi.tools/api/v2/authorize",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
            );
    
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("data")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    return Boolean.TRUE.equals(data.get("authorization"));
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao chamar a API de autorização: {}", e.getMessage());
        }
    
        return false; // Caso ocorra erro, retorna false
    }
    
    
}
