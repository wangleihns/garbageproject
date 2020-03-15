package com.jin.env.garbage.task;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.jin.env.garbage.dao.message.GarbageMessageDao;
import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.entity.message.GarbageMessageEntity;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.DateFormatUtil;
import com.jin.env.garbage.utils.SmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class SendMessageTask {
    private Logger logger = LoggerFactory.getLogger(SendMessageTask.class);

    @Autowired
    private GarbageMessageDao garbageMessageDao;

    @Autowired
    private GarbageUserDao garbageUserDao;

    @Scheduled(cron = "0 30 11,20 * * ? ")
//    @Scheduled(cron = "0 0/3 * * * ? ")
    public void sendMessage(){
        Long start = DateFormatUtil.getFirstTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        Long end = DateFormatUtil.getLastTimeOfDay(DateFormatUtil.formatDate(new Date(), "yyyy-MM-dd")).getTime();
        List<GarbageMessageEntity> list = garbageMessageDao.findAll(new Specification<GarbageMessageEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageMessageEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                Predicate predicateStart = criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), start);
                Predicate predicateEnd = criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), end);
                Predicate predicateStatus = criteriaBuilder.equal(root.get("status"), 0);
                predicates.add(predicateStart);
                predicates.add(predicateEnd);
                predicates.add(predicateStatus);
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });

        list.stream().forEach(n->{
            try {
                SendSmsResponse sendSmsResponse = SmsUtil.getSmsUtil().sendGarbageNotice(n.getPhone(), n.getPlaceName(), n.getDate(), n.getQuality(), n.getType(), n.getScore(), n.getSumScore());
                if ("OK".equals(sendSmsResponse.getCode())){
                    n.setStatus(Constants.messageStatus.SEND.getCode());
                    n.setRequestId(sendSmsResponse.getRequestId());
                    n.setReturnMessage(sendSmsResponse.getMessage());
                    n.setCode(sendSmsResponse.getCode());
                    n.setBizId(sendSmsResponse.getBizId());
                }
            } catch (ClientException e) {
                e.printStackTrace();
            }
        });
        garbageMessageDao.saveAll(list);

        logger.info("定时任务已执行");
    }

    @Scheduled(cron = "0 0 8 24 1 ?")
    public void sendNewYearMessage(){
        List<GarbageUserEntity> userEntities = garbageUserDao.findAll(new Specification<GarbageUserEntity>() {
            @Override
            public Predicate toPredicate(Root<GarbageUserEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate townId = criteriaBuilder.equal(root.get("townId"), 330183002000L);
                return townId;
            }
        });
        List<String> phones = userEntities.stream().map(n -> n.getPhone()).collect(Collectors.toList());
        System.out.println(phones.size());
        phones.stream().forEach(p->{
            try {
                if (p.length() == 11){
                    SmsUtil.getSmsUtil().sendNewYearSms(p);
                }
            } catch (ClientException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
            System.out.println("13588239908".length());
    }
}
