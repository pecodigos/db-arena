package com.pecodigos.dbarena.config.auth;

import com.pecodigos.dbarena.config.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BATTLE_DESTINATION_PREFIX = "/app/battle/";

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    private static final java.util.Set<String> ALLOWED_SUBSCRIPTIONS = java.util.Set.of(
            "/user/queue/match",
            "/user/queue/match-end",
            "/user/queue/match-error",
            "/user/queue/search-status"
    );

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnection(message, accessor);
            return message;
        }

        if (StompCommand.SEND.equals(command)) {
            ensureAuthenticated(accessor, message);
            String destination = accessor.getDestination();
            if (destination == null || !destination.startsWith(BATTLE_DESTINATION_PREFIX)) {
                throw new MessageDeliveryException(message, "Unauthorized websocket destination");
            }
        }

        if (StompCommand.SUBSCRIBE.equals(command)) {
            ensureAuthenticated(accessor, message);
            String destination = accessor.getDestination();
            if (destination == null || !ALLOWED_SUBSCRIPTIONS.contains(destination)) {
                throw new MessageDeliveryException(message, "Unauthorized websocket subscription");
            }
        }

        return message;
    }

    private void authenticateConnection(Message<?> message, StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            throw new MessageDeliveryException(message, "Authorization header missing");
        }

        try {
            String username = jwtUtil.validateToken(token.substring(7));
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            accessor.setUser(authentication);
        } catch (Exception e) {
            throw new MessageDeliveryException(message, "Invalid JWT token", e);
        }
    }

    private void ensureAuthenticated(StompHeaderAccessor accessor, Message<?> message) {
        if (accessor.getUser() == null) {
            throw new MessageDeliveryException(message, "Authentication required");
        }
    }
}
