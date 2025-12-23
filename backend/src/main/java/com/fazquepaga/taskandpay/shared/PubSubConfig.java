package com.fazquepaga.taskandpay.shared;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

import com.google.cloud.spring.pubsub.support.AckMode;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class PubSubConfig {

    @Value("${pubsub.task-reset-subscription}")
    private String taskResetSubscription;

    @Bean
    public MessageChannel proofsChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter messageChannelAdapter(
            @Qualifier("proofsChannel") MessageChannel channel, PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "proofs-subscription");
        adapter.setOutputChannel(channel);
        return adapter;
    }

    @Bean
    public MessageChannel taskResetChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter taskResetChannelAdapter(
            @Qualifier("taskResetChannel") MessageChannel channel, PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, taskResetSubscription);
        adapter.setOutputChannel(channel);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }
}
