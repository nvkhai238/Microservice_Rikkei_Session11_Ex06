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


@Service
class ShipmentService {
 private final RedissonClient redisson;
 private final StockWriter writer;
 private final CacheManager cacheManager;
 private final StringRedisTemplate redis;
 private final ObjectMapper mapper;
 ShipmentService(RedissonClient redisson,StockWriter writer,CacheManager cacheManager,StringRedisTemplate redis,ObjectMapper mapper) {
  this.redisson=redisson;this.writer=writer;this.cacheManager=cacheManager;this.redis=redis;this.mapper=mapper;
 }
 ShipmentResponse export(ShipmentRequest request) throws Exception {
  RLock lock=redisson.getLock("lock:medicine:"+request.medicineId());
  if(!lock.tryLock(3,TimeUnit.SECONDS)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Kho dang xu ly, vui long thu lai");
  try {
   MedicineView medicine=writer.export(request.medicineId(),request.quantity());
   cacheManager.getCache("medicines").evict(request.medicineId());
   String json=mapper.writeValueAsString(new ShippingAlert("EXPORT",request.medicineId(),request.quantity(),"Chuẩn bị xe giao hàng"));
   redis.convertAndSend("shipping-alerts",json);
   return new ShipmentResponse(request.medicineId(),medicine.stock(),json);
  } finally { if(lock.isHeldByCurrentThread()) lock.unlock(); }
 }
}
