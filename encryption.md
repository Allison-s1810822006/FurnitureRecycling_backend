🔒 實作會員個資加密與遮罩功能（Email、手機）
🧩 要做的事

1️⃣ 資料庫調整
在 app_users 新增「加密用欄位」與「搜尋索引用欄位」：
email_enc / email_iv / email_kid / email_hash
phone_enc / phone_iv / phone_kid / phone_hash
為 email_hash、phone_hash 建索引（等值查詢用）。
舊的明文字段（若存在）暫時保留，之後做資料搬遷。

2️⃣ 金鑰與設定（不要進版控）
以 環境變數 提供：
APP_CRYPTO_KID（目前金鑰版本，例如 v1）
APP_CRYPTO_KEY_BASE64（32 bytes 對稱加密金鑰）
APP_CRYPTO_HMAC_KEY_BASE64（32 bytes HMAC 金鑰）
規劃 金鑰輪替：未來新增 v2，讀取依 *_kid 解密。

3️⃣ 加密與索引策略
欄位級加密：使用 AES-GCM，每筆資料都有新的 12 bytes IV（含 AAD）。
等值查詢索引：使用 HMAC-SHA256，對「正規化後」的 email／phone 計算。
正規化規則：
email → 去空白 + 全小寫
phone → 僅保留數字

4️⃣ 寫入／更新資料流程
收到 email／phone：
先正規化
產生對應 HMAC 索引
使用目前 KID 的金鑰進行 AES-GCM 加密
存入 *_enc/*_iv/*_kid/*_hash
清空 email／phone：
對應加密欄位與 hash 一併清空。
不要再寫入明文字段。

5️⃣ 讀取與對外顯示
需要顯示時才解密。
API 對外一律 回遮罩字串：
Email：只顯示首字 + 網域，例如 a***@domain.com
手機：只顯示末 3–4 碼，例如 09**-***-123
絕對不要把密文或 IV 回傳到前端。

6️⃣ 以 email／phone 查詢使用者（若有需求）
對輸入做同樣正規化。
計算對應 HMAC 索引。
用 *_hash 欄位進行等值查詢。

7️⃣ API 與服務調整（不變更既有登入流程）
GET /api/me：回遮罩後的 email／phone。
PUT /api/me：接收新資料，依「寫入／更新資料流程」處理。
保持既有 LINE 登入與 upsert 流程不變，只改寫個資落盤方式。

8️⃣ 日誌與安全性
全站 HTTPS。
JWT／Session Cookie 設定：
HttpOnly
Secure
SameSite=Lax 或 Strict
日誌／例外不記錄明文 email、phone。
DB 備份同樣視為敏感資料（仍包含密文與 IV）。
僅授權的服務或人員能存取解密功能與金鑰。

9️⃣ 測試與驗證（最低限度）
同一明文多次加密 → 密文不同但可正確解密。
改 AAD 或 IV → 解密必須失敗。
正規化與 HMAC 索引一致性（大小寫、空白、符號處理）。
以 *_hash 查詢可正確命中；API 只回遮罩、不回密文。

⚙️ 你要用到的技術與工具
對稱加密演算法：AES-GCM（128-bit tag，12-byte IV）
雜湊訊息鑑別碼：HMAC-SHA256（輸出 64 位元十六進位字串）
金鑰管理：環境變數 + KID（版本）機制，支援輪替
資料庫型別：PostgreSQL bytea（密文、IV），varchar／char(64)（KID、hash）
遮罩策略：Email（首字＋網域）、手機（只露末 3–4 碼）
索引：對 email_hash、phone_hash 建索引（視需求決定是否 unique）