package tn.esprit.spring.notification;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RabbitMQConfig {

    public static final String FACTURE_QUEUE = "facture_paid_queue";

    @Bean
    public Queue factureQueue() {
        return new Queue(FACTURE_QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(3);
        return factory;
    }

    // 🔹 Workaround for "BeanCreationNotAllowedException: applicationTaskExecutor" during shutdown
    // Explicitly defining the executor ensures it's created early and available during context closed events.
    @Bean(name = "applicationTaskExecutor")
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("app-task-");
        executor.initialize();
        return executor;
    }
}
