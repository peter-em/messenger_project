package piotr.messenger.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("piotr.messenger.server.service")
@ComponentScan("piotr.messenger.server.util")
@ComponentScan("piotr.messenger.server.core")
@ComponentScan("piotr.messenger.server.database")
@Import(ServerBeans.class)
@PropertySource("classpath:/application.properties")
public class AutoConfig {
}
