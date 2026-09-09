package com.util;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

//for spring boot test
@RequiredArgsConstructor
public class EndPointCallUtil {

    //client
    private static RestClient restClient = RestClient.create("http://localhost:8000");

//    public static ItemResponse read(CacheStrategy cacheStrategy, Long itemId){
//        return restClient.get()
//                .uri("/cache-strategy/%s/items/%s".formatted(cacheStrategy.name(), itemId))
//                .retrieve()
//                .body(ItemResponse.class)
//                ;
//    }
//
//    public static ItemPageResponse readAll(CacheStrategy cacheStrategy, Long page, Long pageSize){
//        return restClient.get()
//                .uri("/cache-strategy/%s/items?page=%s&pageSize=%s".formatted(cacheStrategy.name(), page, pageSize))
//                .retrieve()
//                .body(ItemPageResponse.class)
//                ;
//    }
//
//    public static ItemPageResponse readAllInfiniteScroll(CacheStrategy cacheStrategy, Long lastItemId, Long pageSize){
//        return restClient.get()
//                .uri(
//                        (lastItemId) == null ?
//                            "/cache-strategy/%s/items/infinite-scroll?pageSize=%s".formatted(cacheStrategy.name(), pageSize)
//                            :
//                            "/cache-strategy/%s/items/infinite-scroll?lastItemId=%s&pageSize=%s".formatted(cacheStrategy.name(), lastItemId, pageSize))
//                .retrieve()
//                .body(ItemPageResponse.class)
//                ;
//    }
//
//    public static ItemResponse create(CacheStrategy cacheStrategy, ItemCreateRequest itemCreateRequest){
//        return restClient.post()
//                .uri("/cache-strategy/%s/items".formatted(cacheStrategy.name()))
//                .body(itemCreateRequest)
//                .retrieve()
//                .body(ItemResponse.class)
//                ;
//    }
//
//    public static ItemResponse update(CacheStrategy cacheStrategy, Long itemId, ItemUpdateRequest itemUpdateRequest) {
//        return restClient.post()
//                .uri("/cache-strategy/%s/items/%s".formatted(cacheStrategy.name(), itemId))
//                .body(itemUpdateRequest)
//                .retrieve()
//                .body(ItemResponse.class)
//                ;
//    }
//
//    public static void delete(CacheStrategy cacheStrategy, Long itemId){
//        restClient.delete()
//                .uri("/cache-strategy/%s/items/%s".formatted(cacheStrategy.name(), itemId))
//                .retrieve()
//                .toBodilessEntity()
//                ;
//    }
}
