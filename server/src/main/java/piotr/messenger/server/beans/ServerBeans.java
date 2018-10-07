package piotr.messenger.server.beans;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

@Configuration
public class ServerBeans {

    @Bean
    public Selector getSelector() {
        Selector selector;
        try {
            selector = SelectorProvider.provider().openSelector(); // throws SomeException
        } catch (IOException se) {
            throw new BeanCreationException("selectorBean", "Failed to create a Selector", se);
        }
        return selector;
    }

}
