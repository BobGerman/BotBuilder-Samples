// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.simplerootbot;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.builder.BotAdapter;
import com.microsoft.bot.builder.ChannelServiceHandler;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.MemoryStorage;
import com.microsoft.bot.builder.skills.SkillConversationIdFactory;
import com.microsoft.bot.builder.skills.SkillConversationIdFactoryBase;
import com.microsoft.bot.builder.skills.SkillHandler;
import com.microsoft.bot.connector.authentication.AuthenticationConfiguration;
import com.microsoft.bot.connector.authentication.ChannelProvider;
import com.microsoft.bot.connector.authentication.CredentialProvider;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.SkillHttpClient;
import com.microsoft.bot.integration.spring.BotController;
import com.microsoft.bot.integration.spring.BotDependencyConfiguration;
import com.microsoft.bot.sample.simplerootbot.authentication.AllowedSkillsClaimsValidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

//
// This is the starting point of the Sprint Boot Bot application.
//
@SpringBootApplication

// Use the default BotController to receive incoming Channel messages. A custom
// controller could be used by eliminating this import and creating a new
// org.springframework.web.bind.annotation.RestController.
// The default controller is created by the Spring Boot container using
// dependency injection. The default route is /api/messages.
@Import({BotController.class})

/**
 * This class extends the BotDependencyConfiguration which provides the default
 * implementations for a Bot application.  The Application class should
 * override methods in order to provide custom implementations.
 */
public class Application extends BotDependencyConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Returns the Bot for this application.
     *
     * <p>
     *     The @Component annotation could be used on the Bot class instead of this method
     *     with the @Bean annotation.
     * </p>
     *
     * @return The Bot implementation for this application.
     */
    @Bean
    public Bot getBot(
        ConversationState conversationState,
        SkillsConfiguration skillsConfig,
        SkillHttpClient skillClient,
        Configuration configuration
    ) {
        return new RootBot(conversationState, skillsConfig, skillClient, configuration);
    }

    @Override
    public AuthenticationConfiguration getAuthenticationConfiguration(Configuration configuration) {
        AuthenticationConfiguration authenticationConfiguration = new AuthenticationConfiguration();
        authenticationConfiguration.setClaimsValidator(
            new AllowedSkillsClaimsValidator(getSkillsConfiguration(configuration)));
        return authenticationConfiguration;
    }

    /**
     * Returns a custom Adapter that provides error handling.
     *
     * @param configuration The Configuration object to use.
     * @return An error handling BotFrameworkHttpAdapter.
     */
    @Override
    public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        return new SkillAdapterWithErrorHandler(
                                configuration,
                                getConversationState(new MemoryStorage()),
                                getSkillHttpClient(
                                    getCredentialProvider(configuration),
                                    getSkillConversationIdFactoryBase(),
                                    getChannelProvider(configuration)),
                                getSkillsConfiguration(configuration));
    }

    @Bean
    public SkillsConfiguration getSkillsConfiguration(Configuration configuration) {
        return new SkillsConfiguration(configuration);
    }

    @Bean
    public SkillHttpClient getSkillHttpClient(
        CredentialProvider credentialProvider,
        SkillConversationIdFactoryBase conversationIdFactory,
        ChannelProvider channelProvider
    ) {
        return new SkillHttpClient(credentialProvider, conversationIdFactory, channelProvider);
    }

    @Bean
    public SkillConversationIdFactoryBase getSkillConversationIdFactoryBase() {
        return new SkillConversationIdFactory(getStorage());
    }

    @Bean public ChannelServiceHandler getChannelServiceHandler(
        BotAdapter botAdapter,
        Bot bot,
        SkillConversationIdFactoryBase conversationIdFactory,
        CredentialProvider credentialProvider,
        AuthenticationConfiguration authConfig,
        ChannelProvider channelProvider
    ) {
        return new SkillHandler(
            botAdapter,
            bot,
            conversationIdFactory,
            credentialProvider,
            authConfig,
            channelProvider);
    }
}
