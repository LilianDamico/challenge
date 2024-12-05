package com.challenge.domain.services;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.challenge.domain.user.User;
import com.challenge.domain.user.Usertype;
import com.challenge.repositories.UserRepository;

@Service
public class UserService {

    @SuppressWarnings("unused")
    @Autowired
    private UserRepository repository;

    public void validateTransaction(User sender, BigDecimal amount) throws Exception{

        if(sender.getUserType() == Usertype.MERCHANT){
            throw new Exception("Lojistas não estão autorizados a enviar pagamentos");
        }

        if(sender.getBalance().compareTo(amount) < 0){
            throw new Exception("Saldo insuficiente.");
        }
    }
}
