package vn.com.group.insurance.api.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import vn.com.group.insurance.common.support.DbRepositoryFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Microsoft SQL Server DataSource Configuration (Primary).
 * Configures the EntityManagerFactory, TransactionManager, and scans repository interfaces
 * under {@code vn.com.group.insurance.proposal.repository.sqlserver}.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "vn.com.group.insurance.api.repository.sqlserver",
        entityManagerFactoryRef = "sqlServerEntityManagerFactory",
        transactionManagerRef = "sqlServerTransactionManager",
        repositoryFactoryBeanClass = DbRepositoryFactoryBean.class
)
public class SqlServerDataSourceConfig {

    @Primary
    @Bean(name = "sqlServerDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.sqlserver")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "sqlServerDataSource")
    public DataSource dataSource(@Qualifier("sqlServerDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "sqlServerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("sqlServerDataSource") DataSource dataSource,
            Environment env) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("vn.com.group.insurance.api.entity.sqlserver");
        em.setPersistenceUnitName("sqlserver");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.sqlserver.hibernate.ddl-auto", "none"));
        jpaProperties.put("hibernate.dialect", env.getProperty("spring.jpa.sqlserver.properties.hibernate.dialect", "org.hibernate.dialect.SQLServerDialect"));
        jpaProperties.put("hibernate.show_sql", env.getProperty("spring.jpa.sqlserver.show-sql", "false"));
        jpaProperties.put("hibernate.format_sql", env.getProperty("spring.jpa.sqlserver.properties.hibernate.format_sql", "false"));
        em.setJpaPropertyMap(jpaProperties);

        return em;
    }

    @Primary
    @Bean(name = "sqlServerTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("sqlServerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
