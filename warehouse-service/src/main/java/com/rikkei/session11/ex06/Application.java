package com.rikkei.session11.ex06;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.*;
import org.springframework.data.redis.serializer.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@SpringBootApplication
@EnableCaching
public class Application {
 public static void main(String[] args) { SpringApplication.run(Application.class,args); }
 @Bean RedissonClient redisson(@Value("${spring.data.redis.host:localhost}") String host,@Value("${spring.data.redis.port:6379}") int port) {
  Config config=new Config();config.useSingleServer().setAddress("redis://"+host+":"+port);return Redisson.create(config);
 }
 @Bean RedisCacheManager cacheManager(RedisConnectionFactory factory) {
  RedisCacheConfiguration config=RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(60))
   .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
  return RedisCacheManager.builder(factory).cacheDefaults(config).build();
 }
 @Bean RedisMessageListenerContainer listenerContainer(RedisConnectionFactory factory,ShippingSubscriber subscriber) {
  RedisMessageListenerContainer container=new RedisMessageListenerContainer();container.setConnectionFactory(factory);
  container.addMessageListener(subscriber,new ChannelTopic("shipping-alerts"));return container;
 }
}
