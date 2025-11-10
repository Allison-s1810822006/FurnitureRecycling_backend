你現在是這個專題的後端負責人，專案是 Spring Boot 專案「FurnitureRecycling」，已經有初步的 LINE Login 串接，可以透過 https://<ngrok-domain>.ngrok-free.app/auth/line/login 開啟 LINE 同意畫面，並在 callback 拿到 JSON 資料。接下來請你在「目前這個 commit（還沒有 Render/雲端整合、只有 ngrok 免費版 callback 的版本）」上，幫我完成一套乾淨、可供前端直接使用的後端整合。

重要前提：

不要引入 Render / Docker / 雲端相關設定。

不要改壞現在已經可以正常跑的 LINE Login 流程。

所有設定維持在本機開發 + ngrok 的前提下運作。

程式碼要保持清楚結構，方便之後更換固定 ngrok 網址或改成正式網域。

一、環境 & 設定

確認 application.properties 使用以下結構（請根據現有檔案調整，而不是整個重砍）：

保留目前可用的 DB 設定（Supabase PostgreSQL）。

保留目前可用的 LINE Channel 設定欄位：

line.channel-id

line.channel-secret

line.callback-url

line.authorize-url

line.token-url

line.jwks-url

line.scope

確保 line.callback-url 只是一個「可被更換」的設定值，不要在程式碼裡寫死網址。

目前先假設會用 ngrok 免費版，callback URL 會手動填進 application.properties。

不要加入 Spring Profiles、不要用 ${DB_URL} 這種 env placeholder，全部維持單一 application.properties，單機開發可跑即可。

二、LINE Login 後端流程整理

在現有基礎上，請你完成並整理以下結構，使用清楚的 service / controller 分層：

Controller：LineAuthController

GET /auth/line/login

回傳 302 redirect 到 LINE 授權頁（用配置值組合 authorize url、client_id、redirect_uri、scope、state）。

GET /auth/line/callback

接收 code 和 state。

呼叫 service 交換 access token / id_token。

驗證 id_token（用 LINE 的 jwks）。

從 id_token 或 userinfo 中取得：

lineUserId (sub)

displayName

picture

email (如果 scope 有且有回傳)

呼叫 domain service 完成：

若該 lineUserId 對應的使用者不存在 → 建立一個 User（或 Member）資料。

若已存在 → 更新必要欄位（例如名稱、大頭照）。

最後回傳 JSON 給前端，格式請統一，例如：

{
"success": true,
"lineUserId": "...",
"displayName": "...",
"email": "...",
"jwt": "如果你有要簽一個簡單 token，可以在這放，沒有就先留 null",
"message": "LINE login success"
}


不要在這個階段做畫面渲染，只回 JSON，之後會交給前端處理導頁。

Service：LineAuthService

負責：

建立 LINE authorize URL。

呼叫 token API 拿 access_token / id_token。

驗證 id_token（簽章＋aud＋iss）。

回傳標準化的 LineProfile DTO 給 controller。

Repository / Entity：

建立或整理一個 User 或 Member entity（若專案已經有，就沿用）：

至少包含：

id（主鍵）

lineUserId（唯一，用來識別 LINE 帳號）

displayName

email（可為 null）

pictureUrl（可為 null）

其他跟專題有關欄位（例如角色、建立時間）

建立對應的 JPA Repository。

在 LineAuthService 中注入 repository，實作「查詢或新增使用者」的邏輯。

三、提供前端會用到的 API（後端部分先完成）

在這一階段，請新增或確認以下 API 存在，之後前端會直接打這些：

GET /api/auth/line/login-url

回傳一個 JSON：

{
"url": "實際要 redirect 去的 LINE login URL"
}


讓前端可以用 window.location = url 觸發 LINE 登入。

GET /api/auth/me

模擬之後登入狀態：

暫時可以先用簡單方式（例如把 lineUserId 放在 session 或暫時用 query 測試）。

回傳當前登入使用者（若有）的資料：

{
"isAuthenticated": true/false,
"user": { ...對應 User 資料... }
}


這部分結構好即可，不一定要做完整安全性機制，先讓前端有介面可以串。

保留既有與大型垃圾預約、用戶資料、紀錄相關的 API，不要刪除，之後前端會直接打這些。

四、程式品質要求

把與 LINE 整合的邏輯集中在清楚的 package，例如：

com.furniture.recycling.line

底下分 controller, service, dto, config

避免在 controller 裡面出現一大坨 HTTP 呼叫 + 驗證的邏輯，盡量放到 service。

所有 URL path 統一、語意清楚，避免魔法字串散落。

你修改後，專案必須：

可以編譯

可以啟動

在本機使用 ngrok 暴露後，可以完成：

/auth/line/login 導去 LINE

/auth/line/callback 正常取得資料，寫入/更新資料庫

/api/auth/line/login-url 回傳可用的登入 URL

/api/auth/me 回傳測試用登入狀態

五、限制條件（一定要遵守）

不要幫我加 Render、Docker、Kubernetes、CI/CD。

不要改成一定要用環境變數才能跑，保持 application.properties 即裝即跑。

不要移除目前已經存在的、跟專題功能有關的 REST API。

如需新增類別、DTO、service，請直接在專案適當 package 中新增，不要只丟在同一支檔案。

做完後，請列出你新增/修改的檔案清單，簡要說明每支檔案的用途，讓我可以逐一檢查與測試。