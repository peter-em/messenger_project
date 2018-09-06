package piotr.messenger.springclient.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("piotr.messenger.springclient.gui")
@ComponentScan("piotr.messenger.springclient.gui.*")
@ComponentScan("piotr.messenger.springclient.core")
@ComponentScan("piotr.messenger.springclient.util")
@Import({MainWindowConfig.class, LoginWindowConfig.class})
public class AutoConfig {
}

