package piotr.messenger.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import piotr.messenger.server.util.UsersDatabase;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

@Configuration
public class ServerBeans {

//    @Bean
//    public Logger getLogger() {
//        return LoggerFactory.getLogger(ServerMain.class);
//    }

    @Bean
    public Selector getSelector() {
        Selector selector;
        try {
            selector = SelectorProvider.provider().openSelector(); // throws SomeException
        } catch (IOException se) {
            throw new BeanCreationException("someBean", "Failed to create a SomeBean", se);
        }
        return selector;
    }

    @Bean
    public UsersDatabase getDatabase() {
        return new UsersDatabase();
    }

}
