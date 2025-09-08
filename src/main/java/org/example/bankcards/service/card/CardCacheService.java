package org.example.bankcards.service.card;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardCacheService {

    public static final String CARD_BALANCE_CACHE_KEY = "card_balance";
    public static final String CARD_CACHE_BY_ID_KEY = "card_by_id";
    public static final String CARD_CACHE_BY_EXTERNAL_CARD_ID_KEY = "card_by_external_card_id";

    @CacheEvict(value = CARD_BALANCE_CACHE_KEY, allEntries = true)
    public void evictAllCardBalance() { }

    @CacheEvict(value = CARD_BALANCE_CACHE_KEY, key = "#id")
    public void evictCardBalance(long id) { }

    @CacheEvict(value = CARD_CACHE_BY_EXTERNAL_CARD_ID_KEY, key = "#externalCardId")
    public void evictCardByExternalCardId(String externalCardId) { }

    @Caching(evict = {
            @CacheEvict(value = CARD_CACHE_BY_ID_KEY, key = "#id"),
            @CacheEvict(value = CARD_CACHE_BY_EXTERNAL_CARD_ID_KEY, key = "#externalCardId")
    })
    public void evictCard(long id, String externalCardId) { }
}
