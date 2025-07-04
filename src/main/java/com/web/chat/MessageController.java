package com.web.chat;

import com.web.entity.Chatting;
import com.web.entity.User;
import com.web.repository.ChatRepository;
import com.web.repository.UserRepository;
import com.web.utils.Contains;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.sql.Timestamp;
import java.util.*;

@Controller

//Controller nhận và gửi tin nhắn
public class MessageController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @MessageMapping("/hello/{id}")
    public void send(SimpMessageHeaderAccessor sha, @Payload String message,@DestinationVariable String id) {
        System.out.println("sha: "+sha.getUser().getName());
        System.out.println("payload: "+message);
        User reciver = null;
        if(Long.valueOf(id) > 0){
            reciver = userRepository.findById(Long.valueOf(id)).get();
            System.out.println("userss === : "+reciver);
        }
        User sender = userRepository.findByUsername(sha.getUser().getName()).get();
        Chatting chatting = new Chatting();
        chatting.setContent(message);
        chatting.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        chatting.setReceiver(reciver);
        chatting.setSender(sender);
        chatRepository.save(chatting);
        Map<String, Object> map = new HashMap<>();
        map.put("sender", sender.getId());
        if(reciver == null){
            List<User> list = userRepository.getUserByRole(Contains.ROLE_ADMIN);
            List<User> listemp = userRepository.getUserByRole(Contains.ROLE_EMPLOYEE);
            for (User user : list) {
                simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/messages", message,map);
            }
            for (User user : listemp) {
                simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/messages", message,map);
            }
        }
        else{
            simpMessagingTemplate.convertAndSendToUser(reciver.getUsername(), "/queue/messages", message,map);
        }
    }

}
