package piotr.messenger.client.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("piotr.messenger.client.gui")
@ComponentScan("piotr.messenger.client.gui.*")
@ComponentScan("piotr.messenger.client.core")
@ComponentScan("piotr.messenger.client.util")
@ComponentScan("piotr.messenger.client.service")
@Import({MainWindowBeans.class, LoginWindowBeans.class})
public class AutoConfig {
}

