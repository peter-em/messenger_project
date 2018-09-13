package piotr.messenger.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("piotr.messenger.server.service")
@ComponentScan("piotr.messenger.server.util")
@ComponentScan("piotr.messenger.server.core")
@Import(ServerBeans.class)
public class AutoConfig {
}
