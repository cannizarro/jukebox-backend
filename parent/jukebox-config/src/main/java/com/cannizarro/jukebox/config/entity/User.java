package com.cannizarro.jukebox.config.entity;

import com.cannizarro.jukebox.config.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import static java.lang.String.format;

@DynamoDbBean
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails, Serializable {
    private String username;
    private String displayName;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Instant createTimeStamp;
    private Instant updateTimeStamp;
    private Set<String> roles;
    private String restaurantName;
    private Boolean online;
    private Float price;
    private Instant lastScan;

    @Override
    @DynamoDbPartitionKey
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return accessToken;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getBearerToken(){
        return format(Constants.BEARER_TOKEN_PREFIX, accessToken);
    }
}
