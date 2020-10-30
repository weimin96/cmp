package com.wiblog.cmp.server.log;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author panweimin
 */
public interface EsLogRepository extends ElasticsearchRepository<EsLog,String> {
}
