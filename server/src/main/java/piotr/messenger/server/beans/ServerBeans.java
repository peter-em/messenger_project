package piotr.messenger.server.beans;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

@Configuration
public class ServerBeans {

    @Bean
    @Qualifier("mainSelector")
    public Selector getMainSelector() {
        Selector selector;
        try {
            selector = SelectorProvider.provider().openSelector();
        } catch (IOException se) {
            throw new BeanCreationException("selectorBean", "Failed to create a MainServer Selector", se);
        }
        return selector;
    }

    @Bean
    @Qualifier("convSelector")
    public Selector getConvSelector() {
        Selector selector;
        try {
            selector = SelectorProvider.provider().openSelector();
        } catch (IOException se) {
            throw new BeanCreationException("selectorBean", "Failed to create a Conversation Selector", se);
        }
        return selector;
    }

}
