package vn.com.group.insurance.proposal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Main application class for the Proposal Microservice.
 *
 * <p>Supports:
 * <ul>
 *   <li>Standalone execution in IntelliJ IDEA / Eclipse via {@link #main(String[])}</li>
 *   <li>Deployment as an external WAR on Apache Tomcat 10.1.0 via {@link SpringBootServletInitializer}</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = {
        "vn.com.group.insurance.proposal",
        "vn.com.group.insurance.common"
})
public class ProposalApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ProposalApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ProposalApplication.class, args);
    }
}
