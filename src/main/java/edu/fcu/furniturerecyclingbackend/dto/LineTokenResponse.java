package edu.fcu.furniturerecyclingbackend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * LineTokenResponse
 * 封裝 LINE OAuth2 token API 回傳的所有欄位。
 * 主要用於接收 LINE 授權流程的 access_token、id_token 等資訊。
 */
public record LineTokenResponse(
        /* LINE access_token，存取 API 用 */
        String accessToken,

        /* token 類型（通常為 Bearer） */
        String tokenType,

        /* access_token 有效秒數 */
        Long expiresIn,

        /* refresh_token，可用於換取新 access_token */
        String refreshToken,

        /* 權限範圍（scope） */
        String scope,

        /* LINE id_token，JWT 格式，包含用戶資訊 */
        String idToken
) {
    /**
     * JsonCreator 建構子，對應 LINE API 回傳的 JSON 欄位名稱。
     * 讓 Jackson 能正確映射欄位。
     */
    @JsonCreator
    public LineTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("scope") String scope,
            @JsonProperty("id_token") String idToken) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.idToken = idToken;
    }
}
