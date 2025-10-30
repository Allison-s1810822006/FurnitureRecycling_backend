//package edu.fcu.furniturerecyclingbackend.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nimbusds.jose.jwk.source.RemoteJWKSet;
//import com.nimbusds.jwt.proc.DefaultJWTProcessor;
//import com.nimbusds.jwt.SignedJWT;
//import com.nimbusds.jose.proc.JWSVerificationKeySelector;
//import com.nimbusds.jose.JWSAlgorithm;
//import com.nimbusds.jose.proc.BadJOSEException;
//import com.nimbusds.jose.util.DefaultResourceRetriever;
//import edu.fcu.furniturerecyclingbackend.config.LineProperties;
//import edu.fcu.furniturerecyclingbackend.dto.LineTokenResponse;
//import edu.fcu.furniturerecyclingbackend.dto.LineProfile;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestClient;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//import java.net.URI;
//import java.time.Instant;
//import java.util.Objects;
//import com.nimbusds.jose.JWSVerifier;
//import com.nimbusds.jose.crypto.MACVerifier;
//
//@Service
//@RequiredArgsConstructor
//public class LineAuthService {
//    private final LineProperties props;
//    private final RestClient http = RestClient.create();
//    private final ObjectMapper om = new ObjectMapper();
//
//    public String buildAuthorizeUrl(String state, String nonce) {
//        return UriComponentsBuilder.fromUriString(props.getAuthorizeUrl())
//                .queryParam("response_type", "code")
//                .queryParam("client_id", props.getChannelId())
//                .queryParam("redirect_uri", props.getCallbackUrl())
//                .queryParam("state", state)
//                .queryParam("scope", props.getScope())
//                .queryParam("nonce", nonce)
//                .build(true).toUriString();
//    }
//
//    public LineTokenResponse exchangeCodeForToken(String code) {
//        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
//        form.add("grant_type","authorization_code");
//        form.add("code",code);
//        form.add("redirect_uri",props.getCallbackUrl());
//        form.add("client_id",props.getChannelId());
//        form.add("client_secret",props.getChannelSecret());
//        String json = http.post().uri(props.getTokenUrl())
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
//                .retrieve().body(String.class);
//        try { return om.readValue(json, LineTokenResponse.class); }
//        catch (IOException e){ throw new RuntimeException(e); }
//    }
//
//    public LineProfile verifyIdTokenAndExtractProfile(String idToken, String expectedNonce) {
//        try {
//            var jwt = SignedJWT.parse(idToken);
//            var header = jwt.getHeader();
//            System.out.println("JWT header alg: " + header.getAlgorithm() + ", kid: " + header.getKeyID());
//            if (header.getAlgorithm().getName().equals("HS256")) {
//                // 用 channel secret 驗證 HMAC-SHA256
//                JWSVerifier verifier = new MACVerifier(props.getChannelSecret());
//                if (!jwt.verify(verifier)) throw new BadJOSEException("HS256 signature invalid");
//                var claims = jwt.getJWTClaimsSet();
//                if (!"https://access.line.me".equals(claims.getStringClaim("iss")))
//                    throw new BadJOSEException("bad iss");
//                if (!claims.getAudience().contains(props.getChannelId()))
//                    throw new BadJOSEException("bad aud");
//                if (!Objects.equals(expectedNonce, claims.getStringClaim("nonce")))
//                    throw new BadJOSEException("bad nonce");
//                if (claims.getExpirationTime().toInstant().isBefore(Instant.now()))
//                    throw new BadJOSEException("expired");
//                return LineProfile.builder()
//                        .lineUserId(claims.getSubject())
//                        .displayName(claims.getStringClaim("name"))
//                        .pictureUrl(claims.getStringClaim("picture"))
//                        .email(claims.getStringClaim("email"))
//                        .build();
//            } else {
//                // 非對稱演算法（RS256/ES256）用 JWKs 驗證
//                var retriever = new DefaultResourceRetriever(3000,3000);
//                var jwkSource = new RemoteJWKSet<>(URI.create(props.getJwksUrl()).toURL(), retriever);
//                var processor = new DefaultJWTProcessor<>();
//                var jwksJson = http.get().uri(props.getJwksUrl()).retrieve().body(String.class);
//                System.out.println("JWKs: " + jwksJson);
//                JWSAlgorithm alg = header.getAlgorithm();
//                processor.setJWSKeySelector(new JWSVerificationKeySelector<>(alg, jwkSource));
//                var claims = processor.process(jwt, null);
//                if (!"https://access.line.me".equals(claims.getStringClaim("iss")))
//                    throw new BadJOSEException("bad iss");
//                if (!claims.getAudience().contains(props.getChannelId()))
//                    throw new BadJOSEException("bad aud");
//                if (!Objects.equals(expectedNonce, claims.getStringClaim("nonce")))
//                    throw new BadJOSEException("bad nonce");
//                if (claims.getExpirationTime().toInstant().isBefore(Instant.now()))
//                    throw new BadJOSEException("expired");
//                return LineProfile.builder()
//                        .lineUserId(claims.getSubject())
//                        .displayName(claims.getStringClaim("name"))
//                        .pictureUrl(claims.getStringClaim("picture"))
//                        .email(claims.getStringClaim("email"))
//                        .build();
//            }
//        } catch (Exception e){
//            System.err.println("verify id_token failed: " + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException("verify id_token failed: " + e.getMessage(), e);
//        }
//    }
//
//    public String bindOrLogin(LineProfile p) {
//        // TODO: 改成你現有 UserRepository / JWT 發行器
//        return "DUMMY_JWT_" + p.getLineUserId();
//    }
//}




package edu.fcu.furniturerecyclingbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import edu.fcu.furniturerecyclingbackend.config.LineProperties;
import edu.fcu.furniturerecyclingbackend.dto.LineProfile;
import edu.fcu.furniturerecyclingbackend.dto.LineTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LineAuthService {

    private static final String EXPECTED_ISS = "https://access.line.me";

    private final LineProperties props;
    private final RestClient http = RestClient.create();
    private final ObjectMapper om = new ObjectMapper();

    /** 產授權網址（注意：這裡會自動 encode redirect_uri）。 */
    public String buildAuthorizeUrl(String state, String nonce) {
        return UriComponentsBuilder.fromUriString(props.getAuthorizeUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", props.getChannelId())
                .queryParam("redirect_uri", props.getCallbackUrl())
                .queryParam("state", state)
                .queryParam("scope", props.getScope()) // e.g. "openid profile email"
                .queryParam("nonce", nonce)
                 .queryParam("prompt", "consent") // 若想每次都看到同意頁
                .build(true)
                .toUriString();
    }

    /** 用 code 向 LINE 換 access_token / id_token。 */
    public LineTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        // 換 token 時 redirect_uri 要用「原樣字串」
        form.add("redirect_uri", props.getCallbackUrl());
        form.add("client_id", props.getChannelId());
        form.add("client_secret", props.getChannelSecret());

        String json = http.post()
                .uri(props.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);

        try {
            return om.readValue(json, LineTokenResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Exchange token failed: " + e.getMessage(), e);
        }
    }

    /** 驗證 LINE 的 id_token（同時支援 HS256 與 ES256），並擷取使用者資料。 */
    public LineProfile verifyIdTokenAndExtractProfile(String idToken, String expectedNonce) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);
            JWSAlgorithm alg = jwt.getHeader().getAlgorithm();
            System.out.println("LINE id_token alg=" + alg + ", kid=" + jwt.getHeader().getKeyID());

            // 先解出 claims（簽章驗完才可信）
            var claimsSet = jwt.getJWTClaimsSet();

            if (JWSAlgorithm.HS256.equals(alg)) {
                // ------ Web Login 常見：HS256（對稱）→ 用 Channel Secret 做 HMAC 驗簽 ------
                // 注意：Channel Secret 長度足夠（> 256 bits）。LINE 的 secret 足夠，不需特別處理。
                JWSVerifier verifier = new MACVerifier(props.getChannelSecret());
                if (!jwt.verify(verifier)) {
                    throw new IllegalArgumentException("HS256 signature invalid");
                }
            } else if (JWSAlgorithm.ES256.equals(alg)) {
                // ------ 原生/LIFF 常見：ES256（非對稱）→ 透過 JWK 驗簽 ------
                var retriever = new DefaultResourceRetriever(
                        (int) Duration.ofSeconds(3).toMillis(),
                        (int) Duration.ofSeconds(3).toMillis(),
                        (int) Duration.ofMinutes(10).toMillis()
                );
                JWKSource<SecurityContext> jwkSource =
                        new RemoteJWKSet<>(URI.create(props.getJwksUrl()).toURL(), retriever);
                ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
                processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, jwkSource));
                // 這行會完成簽章驗證並回傳 claims（等效於上面的 jwt.verify）
                claimsSet = processor.process(jwt, null);
            } else {
                // 其他演算法一律拒絕
                throw new IllegalArgumentException("Unsupported JWS alg: " + alg);
            }

            // ------ 通用的 OIDC 檢查（簽章通過後才做） ------
            if (!EXPECTED_ISS.equals(claimsSet.getIssuer())) {
                throw new IllegalArgumentException("Invalid iss: " + claimsSet.getIssuer());
            }
            List<String> aud = claimsSet.getAudience();
            if (aud == null || !aud.contains(props.getChannelId())) {
                throw new IllegalArgumentException("Invalid aud: " + aud);
            }
            if (expectedNonce != null) {
                String nonce = (String) claimsSet.getClaim("nonce");
                if (!Objects.equals(expectedNonce, nonce)) {
                    throw new IllegalArgumentException("Invalid nonce");
                }
            }
            Instant exp = claimsSet.getExpirationTime() == null ? null : claimsSet.getExpirationTime().toInstant();
            if (exp == null || exp.isBefore(Instant.now())) {
                throw new IllegalArgumentException("id_token expired");
            }

            return LineProfile.builder()
                    .lineUserId(claimsSet.getSubject())                     // sub
                    .displayName((String) claimsSet.getClaim("name"))        // 需 scope=profile
                    .pictureUrl((String) claimsSet.getClaim("picture"))      // 需 scope=profile
                    .email((String) claimsSet.getClaim("email"))             // 需 scope=email
                    .build();

        } catch (Exception e) {
            System.err.println("verify id_token failed: " + e.getMessage());
            throw new RuntimeException("verify id_token failed: " + e.getMessage(), e);
        }
    }

    /** 綁定或登入你系統的帳號，回傳自家 JWT（示意）。 */
    public String bindOrLogin(LineProfile p) {
        // TODO: 用你的 UserRepository 查/建，再簽發自家 JWT（HS256/RS256 皆可）
        return "DUMMY_JWT_" + p.getLineUserId();
    }
}
