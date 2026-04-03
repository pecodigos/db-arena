package com.pecodigos.dbarena.config.auth;

import com.pecodigos.dbarena.config.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtChannelInterceptorTests {

    @Test
    void connectShouldAuthenticateUserFromBearerToken() {
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        CustomUserDetailsService userDetailsService = Mockito.mock(CustomUserDetailsService.class);
        JwtChannelInterceptor interceptor = new JwtChannelInterceptor(jwtUtil, userDetailsService);
        MessageChannel channel = Mockito.mock(MessageChannel.class);

        Mockito.when(jwtUtil.validateToken("valid-token")).thenReturn("goku");
        Mockito.when(userDetailsService.loadUserByUsername("goku")).thenReturn(userDetails("goku"));

        Message<byte[]> message = message(StompCommand.CONNECT, null, "Bearer valid-token", false);

        Message<?> authenticatedMessage = interceptor.preSend(message, channel);
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(authenticatedMessage);

        assertNotNull(accessor.getUser());
        assertEquals("goku", accessor.getUser().getName());
    }

    @Test
    void sendShouldRejectUnauthenticatedFrames() {
        JwtChannelInterceptor interceptor = new JwtChannelInterceptor(
                Mockito.mock(JwtUtil.class),
                Mockito.mock(CustomUserDetailsService.class)
        );
        MessageChannel channel = Mockito.mock(MessageChannel.class);
        Message<byte[]> message = message(StompCommand.SEND, "/app/battle/search", null, false);

        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(message, channel));
    }

    @Test
    void subscribeShouldRejectUnexpectedDestinationsEvenForAuthenticatedUsers() {
        JwtChannelInterceptor interceptor = new JwtChannelInterceptor(
                Mockito.mock(JwtUtil.class),
                Mockito.mock(CustomUserDetailsService.class)
        );
        MessageChannel channel = Mockito.mock(MessageChannel.class);
        Message<byte[]> message = message(StompCommand.SUBSCRIBE, "/topic/public", null, true);

        assertThrows(MessageDeliveryException.class, () -> interceptor.preSend(message, channel));
    }

    @Test
    void allowedBattleFramesShouldPassForAuthenticatedUsers() {
        JwtChannelInterceptor interceptor = new JwtChannelInterceptor(
                Mockito.mock(JwtUtil.class),
                Mockito.mock(CustomUserDetailsService.class)
        );
        MessageChannel channel = Mockito.mock(MessageChannel.class);

        assertDoesNotThrow(() -> interceptor.preSend(
                message(StompCommand.SEND, "/app/battle/end-turn", null, true),
                channel
        ));
        assertDoesNotThrow(() -> interceptor.preSend(
                message(StompCommand.SUBSCRIBE, "/user/queue/match", null, true),
                channel
        ));
    }

    private static Message<byte[]> message(StompCommand command,
                                           String destination,
                                           String authorizationHeader,
                                           boolean authenticated) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);

        if (destination != null) {
            accessor.setDestination(destination);
        }

        if (authorizationHeader != null) {
            accessor.setNativeHeader("Authorization", authorizationHeader);
        }

        if (authenticated) {
            UserDetails userDetails = userDetails("goku");
            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            ));
        }

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static UserDetails userDetails(String username) {
        return User.withUsername(username)
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }
}
