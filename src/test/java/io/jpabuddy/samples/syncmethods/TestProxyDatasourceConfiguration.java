package io.jpabuddy.samples.syncmethods;

import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestProxyDatasourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public ProxyTestDataSource dataSource(DataSourceProperties properties) {
        return new ProxyTestDataSource(properties.
                initializeDataSourceBuilder().type(HikariDataSource.class)
                .build());
    }

}