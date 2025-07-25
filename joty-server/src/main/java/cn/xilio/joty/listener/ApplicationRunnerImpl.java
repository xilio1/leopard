package cn.xilio.joty.listener;


import cn.xilio.joty.domain.dataobject.ShortUrl;
import cn.xilio.joty.service.ShortUrlService;
import cn.xilio.joty.domain.service.BloomFilterService;
import cn.xilio.joty.core.common.exception.BizException;
import cn.xilio.joty.core.common.page.PageQuery;
import cn.xilio.joty.core.common.page.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(ApplicationRunnerImpl.class);
    @Autowired
    private ShortUrlService shortUrlService;
    @Autowired
    private BloomFilterService bloomFilterService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void run(ApplicationArguments args) {
        initShortUrlCode();
    }

    private void initShortUrlCode() {
        logger.info("Starting to initialize short link codes from database to Bloom filter.");
       int count=0;
        int page = 1;
        int pageSize = 2000;
        PageResponse<ShortUrl> pageResponse;
        try {
            do {
                PageQuery pageRequest = PageQuery.of(page, pageSize);
                pageResponse = shortUrlService.getShortUrls(pageRequest);
                logger.debug("Processing page {} with {} records.", page, pageResponse.getRecords().size());

                for (ShortUrl shortUrl : pageResponse.getRecords()) {
                    LocalDate expiredAt = shortUrl.getExpiredAt();
                    String shortCode = shortUrl.getShortCode();
                    if (!StringUtils.hasText(shortCode) || (!ObjectUtils.isEmpty(expiredAt) &&expiredAt.isBefore(LocalDate.now()))) {
                        continue;
                    }
                    bloomFilterService.put(shortCode);
                    count++;
                }
                // Increasing page numbers
                page++;
            } while (pageResponse.getHasMore());
            logger.info("Finished initializing {} short link codes to Bloom filter.", count);
        } catch (Exception e) {
            logger.error("Failed to initialize short link codes to Bloom filter.", e);
            throw new BizException("1005");
        }
    }
}
