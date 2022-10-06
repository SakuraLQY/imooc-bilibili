package com.imooc.bilibili.service.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.domain.UserFollowing;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.constants.UserMomentsConstant;

import com.imooc.bilibili.service.UserFollowingService;
import com.imooc.bilibili.service.websocket.WebSocketService;
import com.mysql.jdbc.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RocketMqConfig {

    @Value("${rocketmq.name.server.address}")
    private String nameServerAddr;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private UserFollowingService userFollowingService;
    //创建生产者
    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws Exception{
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_MOMENTS);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws Exception{
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(UserMomentsConstant.TOPIC_MOMENTS,"*");
        //设置监听器
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msg, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                MessageExt message = msg.get(0);
                if(message == null){
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                String bodyStr = new String(message.getBody());
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);
                //获取到发布者的userId->获得粉丝ID->推送到一个userMoments列表->
                Long userId = userMoment.getUserId();
                List<UserFollowing> fanList = userFollowingService.getUserFans(userId);
                for (UserFollowing fan : fanList) {
                    //用redis进行缓存
                    String key = "subscribe"+fan.getUserId();
                    String subscribedListStr = redisTemplate.opsForValue().get(key);//获取的值
                    List<UserMoment>subscribedList;
                    if(StringUtils.isNullOrEmpty(subscribedListStr)){
                        subscribedList = new ArrayList<>();
                    }else{
                        subscribedList = JSONArray.parseArray(subscribedListStr, UserMoment.class);
                    }
                    subscribedList.add(userMoment);
                    //推送到redis
                    redisTemplate.opsForValue().set(key,JSONObject.toJSONString(subscribedList));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();;
        return consumer;
    }

    //创建弹幕生产者
    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws Exception{
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_DANMUS);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer defaultMQPushConsumer() throws MQClientException {
        //实例化消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_DANMUS);
        //设置NameServer的地址
        consumer.setNamesrvAddr(nameServerAddr);
        //订阅一个或多个topic 以及tag来过滤消费、
        consumer.subscribe(UserMomentsConstant.TOPIC_DANMUS,"*");
        //注册回调实现类处理broker节点
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                //拿到监听的信息
                MessageExt msg = msgs.get(0);
                byte[] msgByte = msg.getBody();
                String bodyStr = new String(msgByte);
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                String sessionId = jsonObject.getString("sessionId");
                String message = jsonObject.getString("message");
                //websocket
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
                if(webSocketService.getSession().isOpen()){
                    try{
                        webSocketService.sendMessage(message);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                //标记该消费已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }
}
