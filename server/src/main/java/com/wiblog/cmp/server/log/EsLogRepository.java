package com.wiblog.cmp.server.log;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author panweimin
 * @create 2020-10-29 17:00
 */
public interface EsLogRepository extends ElasticsearchRepository<EsLog,String> {
}
