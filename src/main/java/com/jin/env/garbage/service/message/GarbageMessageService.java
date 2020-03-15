package com.jin.env.garbage.service.message;

import com.jin.env.garbage.dao.message.GarbageMessageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GarbageMessageService {

    @Autowired
    private GarbageMessageDao garbageMessageDao;

}
