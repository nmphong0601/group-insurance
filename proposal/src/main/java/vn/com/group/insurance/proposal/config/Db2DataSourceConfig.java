package vn.com.group.insurance.proposal.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * IBM DB2 DataSource Configuration (Secondary).
 * Configures the EntityManagerFactory, TransactionManager, and scans repository interfaces
 * under {@code vn.com.group.insurance.proposal.repository.db2}.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "vn.com.group.insurance.proposal.repository.db2",
        entityManagerFactoryRef = "db2EntityManagerFactory",
        transactionManagerRef = "db2TransactionManager",
        repositoryFactoryBeanClass = DbRepositoryFactoryBean.class
)
public class Db2DataSourceConfig {

    @Bean(name = "db2DataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "db2DataSource")
    public DataSource dataSource(@Qualifier("db2DataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "db2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("db2DataSource") DataSource dataSource,
            Environment env) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("vn.com.group.insurance.proposal.entity.db2");
        em.setPersistenceUnitName("db2");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.db2.hibernate.ddl-auto", "none"));
        jpaProperties.put("hibernate.dialect", env.getProperty("spring.jpa.db2.properties.hibernate.dialect", "org.hibernate.dialect.DB2Dialect"));
        jpaProperties.put("hibernate.show_sql", env.getProperty("spring.jpa.db2.show-sql", "false"));
        jpaProperties.put("hibernate.format_sql", env.getProperty("spring.jpa.db2.properties.hibernate.format_sql", "false"));
        em.setJpaPropertyMap(jpaProperties);

        return em;
    }

    @Bean(name = "db2TransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("db2EntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
