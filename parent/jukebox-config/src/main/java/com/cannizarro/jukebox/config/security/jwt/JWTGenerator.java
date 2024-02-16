package com.cannizarro.jukebox.config.security.jwt;

import com.cannizarro.jukebox.config.constants.Constants;
import com.cannizarro.jukebox.config.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;

@Component
@Slf4j
public class JWTGenerator {

	private final SecretKey encryptionKey;

	private final int maxAgeInDays;

	public JWTGenerator(@Value("${jwt.key}") String encryptionKey, @Value("${jwt.max_age_days}") int maxAgeInDays){
		this.encryptionKey = Keys.hmacShaKeyFor(encryptionKey.getBytes(StandardCharsets.UTF_8));
		this.maxAgeInDays = maxAgeInDays;
	}
	
	public String generateToken(Authentication authentication) {
		Date currentDate = new Date();
		Date expireDate = addDays(currentDate, maxAgeInDays);

		String token = Jwts.builder()
				.subject(((User)authentication.getPrincipal()).getUsername())
				.issuedAt(currentDate)
				.expiration(expireDate)
				.encryptWith(encryptionKey, Jwts.ENC.A256CBC_HS512)
				.compact();
		log.debug("Token: {}", token);
		return token;
	}
	public String getUsernameFromJWT(String token){
		Jwe<Claims> claims = getClaims(token);
		return claims.getPayload().getSubject();
	}
	
	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (Exception ex) {
			if(!token.equals(Constants.INVALID_TOKEN))
				log.error("Malformed token received: {}", token);
			return false;
		}
	}

	private Jwe<Claims> getClaims(String token){
		return Jwts.parser()
				.decryptWith(encryptionKey)
				.build()
				.parseEncryptedClaims(token);
	}

	private Date addDays(Date date, int days)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

}
