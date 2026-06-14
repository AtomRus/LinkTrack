package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.properties.ValkeyCacheProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class ValkeyCacheConfig {

    @Bean
    public CacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory, ValkeyCacheProperties cacheProperties) {
        Duration ttl = cacheProperties.getTtl();
        ObjectMapper cacheObjectMapper = new ObjectMapper();
        RedisSerializer<Object> serializer = new RedisSerializer<>() {
            @Override
            public byte[] serialize(Object value) throws SerializationException {
                if (value == null) {
                    return new byte[0];
                }
                try {
                    return cacheObjectMapper.writeValueAsBytes(value);
                } catch (Exception e) {
                    throw new SerializationException("Cannot serialize value for Redis cache", e);
                }
            }

            @Override
            public Object deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                try {
                    if (bytes[0] == '[') {
                        return cacheObjectMapper.readValue(bytes, new TypeReference<List<Link>>() {});
                    }
                    return cacheObjectMapper.readValue(bytes, Object.class);
                } catch (Exception primary) {
                    try {
                        return cacheObjectMapper.readValue(bytes, Object.class);
                    } catch (Exception secondary) {
                        throw new SerializationException("Cannot deserialize value from Redis cache", primary);
                    }
                } catch (Error e) {
                    throw new SerializationException("Cannot deserialize value from Redis cache", e);
                }
            }
        };

        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        if (!cacheProperties.isNullValuesEnabled()) {
            baseConfig = baseConfig.disableCachingNullValues();
        }

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(cacheProperties.getLinksListCacheName(), baseConfig);
        cacheConfigurations.put(cacheProperties.getLinksListByTagCacheName(), baseConfig);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
