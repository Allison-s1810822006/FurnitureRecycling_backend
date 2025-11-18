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
import edu.fcu.furniturerecyclingbackend.dto.LineUserResult;
import edu.fcu.furniturerecyclingbackend.repository.AppUsersRepository;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class LineAuthService {

    private static final String EXPECTED_ISS = "https://access.line.me";
    private static final Logger logger = LoggerFactory.getLogger(LineAuthService.class);

    private final LineProperties props;
    private final RestClient http = RestClient.create();
    private final ObjectMapper om = new ObjectMapper();
    private final AppUsersRepository appUsersRepository; // 注入會員資料庫 Repository

    /** 產授權網址（注意：這裡會自動 encode redirect_uri）。 */
    public String buildAuthorizeUrl(String state, String nonce) {
        String scope = props.getScope(); // e.g. "openid profile email"
        logger.info("LINE scope = {}", scope); // log actual scope sent to LINE
        String url = UriComponentsBuilder.fromUriString(props.getAuthorizeUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", props.getChannelId())
                .queryParam("redirect_uri", props.getCallbackUrl())
                .queryParam("state", state)
                .queryParam("scope", scope) // e.g. "openid profile email"
                .queryParam("nonce", nonce)
                 .queryParam("prompt", "consent") // 若想每次都看到同意頁
                .build()
                .encode()
                .toUriString();
        logger.debug("Line authorize URL generated: {}", url);
        return url;
    }

    /** 用 code 向 LINE 換 access_token / id_token。 */
    public LineTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        // 換 token 時 redirect_uri 要用「原樣字串"
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

    /**
     * Convenience: exchange code and verify id_token, returning LineProfile.
     * This variant does not perform nonce/state session checks — caller is responsible for that if needed.
     */
    public LineProfile getProfileFromCode(String code, String state) {
        var token = exchangeCodeForToken(code);
        // pass null for expectedNonce (no nonce check here)
        return verifyIdTokenAndExtractProfile(token.idToken(), null);
    }

    /**
     * Find existing AppUsers by lineUserId or create a new one. Returns a LineUserResult
     * indicating whether a new user was created.
     */
    public LineUserResult findOrCreateUser(String lineUserId, String displayName, String email, String pictureUrl) {
        var existingOpt = appUsersRepository.findByLineUserId(lineUserId);
        if (existingOpt.isPresent()) {
            var user = existingOpt.get();
            user.setLineDisplayName(displayName);
            user.setLineEmail(email);
            user.setLinePictureUrl(pictureUrl);
            user.setUpdatedAt(OffsetDateTime.now());
            appUsersRepository.save(user);
            boolean isMember = Boolean.TRUE.equals(user.getIsMember());
            return new LineUserResult(user, isMember);
        }

        var user = new edu.fcu.furniturerecyclingbackend.model.AppUsers();
        // Do not set userId manually; let JPA generate it. But if you prefer, uncomment below:
        // user.setUserId(UUID.randomUUID());
        user.setLineUserId(lineUserId);
        user.setFullName(displayName == null ? "" : displayName);
        user.setEmail(email);
        user.setLineDisplayName(displayName);
        user.setLineEmail(email);
        user.setLinePictureUrl(pictureUrl);
        user.setIsMember(false);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        appUsersRepository.save(user);
        return new LineUserResult(user, false); // 新建立但 not a full member yet
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

    /**
     * 判斷 LINE userId 是否已綁定會員
     * @param lineUserId LINE userId
     * @return 是否已綁定
     */
    public boolean isLineUserBound(String lineUserId) {
        // 查詢 AppUsersRepository 是否有此 LINE userId
        return appUsersRepository.findByLineUserId(lineUserId).isPresent();
    }

    /**
     * 已綁定會員登入，回傳 JWT（或建立 session）
     * @param lineUserId LINE userId
     * @return JWT 字串
     */
    public String loginWithLineUser(String lineUserId) {
        // 查詢會員，簽發 JWT（此處僅示意，實際可用 JWT 工具）
        var userOpt = appUsersRepository.findByLineUserId(lineUserId);
        if (userOpt.isPresent()) {
            // TODO: 實際簽發 JWT
            return "DUMMY_JWT_" + lineUserId;
        }
        throw new RuntimeException("LINE user not bound");
    }

    /**
     * 查詢或建立/更新使用者
     */
    public edu.fcu.furniturerecyclingbackend.model.AppUsers findOrCreateOrUpdateUser(edu.fcu.furniturerecyclingbackend.dto.LineProfile profile) {
        var opt = appUsersRepository.findByLineUserId(profile.getLineUserId());
        if (opt.isPresent()) {
            // 更新 displayName, pictureUrl, email
            var user = opt.get();
            user.setLineDisplayName(profile.getDisplayName());
            user.setLinePictureUrl(profile.getPictureUrl());
            user.setLineEmail(profile.getEmail());
            return appUsersRepository.save(user);
        } else {
            var user = new edu.fcu.furniturerecyclingbackend.model.AppUsers();
            user.setLineUserId(profile.getLineUserId());
            user.setLineDisplayName(profile.getDisplayName());
            user.setLinePictureUrl(profile.getPictureUrl());
            user.setLineEmail(profile.getEmail());
            user.setLineBoundAt(java.time.OffsetDateTime.now());
            user.setFullName(profile.getDisplayName()); // 預設用 displayName
            return appUsersRepository.save(user);
        }
    }

    /**
     * 依 LINE userId 查詢 user
     */
    public java.util.Optional<edu.fcu.furniturerecyclingbackend.model.AppUsers> getUserByLineUserId(String lineUserId) {
        return appUsersRepository.findByLineUserId(lineUserId);
    }
}
