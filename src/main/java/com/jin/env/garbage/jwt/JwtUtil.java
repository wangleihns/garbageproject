package com.jin.env.garbage.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by abc on 2018/5/25.
 */

@Configuration
//@ConfigurationProperties(prefix="spring.jwt")
public class JwtUtil {
    private Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static JwtUtil jwtUtil = null;
    @Value("${spring.jwt.header.typ}")
    private String typ;
    @Value("${spring.jwt.header.zip}")
    private String alg;
    @Value("${spring.jwt.claim.aud}")
    private String aud;
    @Value("${spring.jwt.claim.exp}")
    private int exp;
    @Value("${spring.jwt.claim.secret}")
    private String secret;
    @Value("${spring.jwt.claim.refreshDate}")
    private int refreshExp;


    public static JwtUtil getJwtUtil(){
        if (jwtUtil == null){
            jwtUtil = new JwtUtil();
        }
        return jwtUtil;
    }

    /**
     * 获取header
     * @return
     */
    public  Map<String, Object> getHeader(){
            Map<String,Object> map = new HashMap<>();
            map.put("typ",typ);
            map.put("alg",alg);
        return map;
    }

    /**
     *
     * @param subject
     * @param issuer
     * @param expTime
     * @return
     */
    public Claims getClaim(String subject,String issuer,int expTime){
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + expTime);
        Claims claims = Jwts.claims()
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(iat)
                .setExpiration(exp)
                .setAudience(aud);
        return claims;
    }

    /**
     *  加密等级
     * @param signType
     * @return
     */
    public SignatureAlgorithm getSignatureAlgorithm(String signType){
        switch (signType){
            case "HS256":return SignatureAlgorithm.HS256;
            case "HS384":return SignatureAlgorithm.HS384;
            case "HS512":return SignatureAlgorithm.HS512;
            case "RS256":return SignatureAlgorithm.RS256;
            case "RS384":return SignatureAlgorithm.RS384;
            case "RS512":return SignatureAlgorithm.RS512;
            case "ES256":return SignatureAlgorithm.ES256;
            case "ES384":return SignatureAlgorithm.ES384;
            case "ES512":return SignatureAlgorithm.ES512;
            case "PS256":return SignatureAlgorithm.PS256;
            case "PS384":return SignatureAlgorithm.PS384;
            case "PS512":return SignatureAlgorithm.PS512;
            default:return SignatureAlgorithm.NONE;
        }
    }

    /**
     * 创建jwt
     * @param subject
     * @param issuer
     * @return
     */
    public String generateJwtToken(String subject,String issuer, Integer expTime){
        return Jwts.builder()
                .setHeader(getHeader())
                .setClaims(getClaim(subject, issuer, (expTime == null ?exp:expTime)))
                .signWith(getSignatureAlgorithm(alg), secret)
                .compact();
    }

    /**
     * 解析jwt
     * @param jwt
     * @param type
     * @return
     */
    public  Map<String, Object> parserJavaWebToken(String jwt,String type) {
        try {
            Assert.hasText(jwt,"this String jwt must have text; it must not be null, empty, or blank");
            if ("body".equals(type)){
                Map<String, Object> jwtClaims =
                        Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt).getBody();
                return jwtClaims;
            } else if ("header".equals(type)) {
                Map<String, Object> jwtClaims =
                        Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt).getHeader();
                return jwtClaims;
            }else {
                Map map = new HashMap();
                String sign = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt).getSignature();
                map.put("sign",sign);
                return map;
            }
        } catch (Exception e) {
            logger.error("json web token verify failed");
            throw  e;
        }
    }


    /**
     * 获取过期时间
     * @param jwt
     * @return
     */
    public Date getExpireDate(String jwt) throws Exception{
        Map<String, Object> map = this.parserJavaWebToken(jwt,"body");
        Date date = new Date((Long) map.get("exp")*1000);
        return date;
    }


    public String getRefresh(String jwt){
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + refreshExp);
        Map<String, Object> claims = parserJavaWebToken(jwt,"body");
        claims.put("exp",exp);

        return Jwts.builder()
                .setHeader(getHeader())
                .setClaims(claims)
                .signWith(getSignatureAlgorithm(alg), secret)
                .compact();
    }


    public Integer getSubject(String jwt){
        Map<String, Object> claims = null;
        Integer sub = null;
        try {
            claims = parserJavaWebToken(jwt,"body");
            sub = Integer.valueOf(claims.get("sub").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sub;
    }



}
