package edu.fcu.furniturerecyclingbackend.controller.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class FurnitureConsultController {

    private static final Logger logger = LoggerFactory.getLogger(FurnitureConsultController.class);

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/furniture-consult")
    public ResponseEntity<?> consult(@RequestBody ImageRequest request) {
        try {
            // 🔍 這裡用 logger 輸出金鑰長度（不輸出金鑰本身）
            logger.debug("Gemini key length = {}", geminiApiKey == null ? "null" : geminiApiKey.length());
            if (request == null || request.getImageBase64() == null || request.getImageBase64().isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "imageBase64 is required"));
            }

            // 1. 官方 endpoint（REST）
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";


            // 2. 組出 inline_data（跟官方文件一樣的欄位名）
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", request.getImageBase64());

            Map<String, Object> imagePart = Map.of("inline_data", inlineData);

            String prompt = """
                    你是一個大型家具回收諮詢助手，請使用繁體中文、親切且簡短回答。

                    請嚴格遵守以下服務規則並以條列式回覆（每點一行）：
                    - 本服務目前僅支援「沙發」與「床架」兩種家具；其他請回「其他」。
                    - 只有當家具的尺寸符合系統資料庫內已登錄的可回收產品尺寸時，才視為「尺寸符合」。
                    - 使用者必須自行將物品放置到自己選擇的回收站；本服務不代放置。
                    - 最晚放置時間：預約日期的前一天 17:00 前完成放置。
                    - 最早可預約日期：從今天起算，最早是兩天後。
                    - 費用：目前暫時不收費（未來可能調整）。
                    - 預約制：本服務採預約制；若未預約或不符合上述規定，將不受理。
                    - 服務區域：本服務目前僅限於「台中市」；其他縣市一律不受理，若使用者不在台中市請明確回覆無法受理並告知僅受理台中市。

                    回覆格式（務必遵守，前端會解析）：
                    1. 是否為可回收家具：是/否
                    2. 家具種類（僅回傳：沙發、床架、其他）
                    3. 尺寸是否符合資料庫：是/否
                    4. 放置說明（使用者需自行放置，以及最晚放置時間）
                    5. 預約說明（最早可預約、預約制等）
                    6. 費用說明

                    若使用者問到其他非回收相關問題，請簡短說明本助理主要功能為大型家具回收諮詢。
                    """;

            Map<String, Object> textPart = Map.of("text", prompt);

            Map<String, Object> content = Map.of(
                    "parts", List.of(
                            imagePart,
                            textPart
                    )
            );

            Map<String, Object> body = Map.of(
                    "contents", List.of(content)
            );

            // 3. 設定 header（官方建議用 x-goog-api-key）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey); // API KEY 放 header


            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);


            // 4. 呼叫 Gemini API
            @SuppressWarnings("unchecked")
            Map<String, Object> geminiResponse = restTemplate.postForObject(url, entity, Map.class);

            String answer = extractTextFromGeminiResponse(geminiResponse);

            // 後處理：將模型回覆的家具類型標準化為系統內的允許值，並加入尺寸提醒
            String normalizedAnswer = normalizeAnswerAndAddSizeReminder(answer);


            // 額外回傳標準化的類型，供前端/後端直接比對資料庫
            String detectedType = detectTypeFromAnswer(answer);
            String sizeWarning = "請勿超過頁面上顯示的參考尺寸；超過參考尺寸可能無法使用大型家具回收服務。";

            boolean recyclable = "沙發".equals(detectedType) || "床架".equals(detectedType);
            String serviceMessage;
            String finalAnswer = normalizedAnswer;
            if (!recyclable) {
                // 若非支援類型，加入不可回收說明；但若模型回覆已包含相同說明就不要重複加入
                serviceMessage = "抱歉，本服務目前僅支援「沙發」與「床架」的大型家具回收；您上傳的物品不屬於支援類型，因此無法使用本回收服務。";
                String naLower = normalizedAnswer == null ? "" : normalizedAnswer.toLowerCase();
                String svcLower = serviceMessage.toLowerCase();
                if (!naLower.contains(svcLower) && !naLower.contains("抱歉") && !naLower.contains("不屬於")) {
                    finalAnswer = normalizedAnswer + "\n注意：" + serviceMessage + "\n";
                } else {
                    finalAnswer = normalizedAnswer; // already said in model reply
                }
            } else {
                serviceMessage = "此家具類型可使用本服務（請同時確認尺寸符合參考值）。";
            }

            // --- 建立頂部醒目提示 (綠色代表可回收，紅色代表不可回收)，並加入醒目的尺寸提醒（藍色粗體） ---
            // 尺寸提醒保留醒目顏色但不使用粗體，避免過度使用粗體造成視覺疲勞
            String highlightedSizeHtml = "<p style=\"color:blue\">請注意：請勿超過頁面上顯示的參考尺寸；超過參考尺寸可能無法使用大型家具回收服務。</p>";

            // 從模型回覆抓出關鍵判斷句（若有），但只在模型明確回覆時才顯示綠色亮點；紅色（不可回收）仍然顯示
            String decisionSentence = extractDecisionSentence(finalAnswer, recyclable, serviceMessage);
            // 清理 decisionSentence，移除 markdown 標記，避免粗體或 * 出現在 highlight
            String decisionSanitized = stripMarkdown(decisionSentence);
            String answerLower = (answer == null) ? "" : answer.toLowerCase();
            boolean explicitDecisionInModel = answerLower.contains("可回收") || answerLower.contains("不可回收") || answerLower.contains("不屬於") || answerLower.contains("不受理") || answerLower.contains("抱歉");

            boolean showHighlight = !recyclable || explicitDecisionInModel; // show red always; show green only if explicit
            String highlightHtml = "";
            if (showHighlight) {
                if (recyclable) {
                    highlightHtml = "<p><strong style=\"color:green\">" + escapeHtml(decisionSanitized) + "</strong></p>" + highlightedSizeHtml;
                } else {
                    highlightHtml = "<p><strong style=\"color:red\">" + escapeHtml(decisionSanitized) + "</strong></p>" + highlightedSizeHtml;
                }
            }

            // 如果我們不會在頂端顯示 highlight，就先從 finalAnswer 移除 serviceMessage / 尺寸提醒 / 常見問候，避免出現在內容最上方
            if (!showHighlight) {
                if (serviceMessage != null && !serviceMessage.isBlank()) {
                    finalAnswer = finalAnswer.replace("注意：" + serviceMessage, "");
                    finalAnswer = finalAnswer.replace("注意:" + serviceMessage, "");
                    finalAnswer = finalAnswer.replace(serviceMessage, "");
                }
                // 移除尺寸提醒文句（純文字版）
                finalAnswer = finalAnswer.replace("請勿超過所規定的參考尺寸；超過參考尺寸可能無法使用大型家具回收服務。", "");
                // 移除一些常見自我介紹句
                finalAnswer = finalAnswer.replace("嗨，我是大型家具回收小幫手。", "");
                finalAnswer = finalAnswer.replace("嗨，我是大型家具回收小幫手！", "");
                finalAnswer = finalAnswer.replace("我是大型家具回收小幫手。", "");
                finalAnswer = finalAnswer.replace("我是專門提供大型家具（沙發、床架）回收諮詢的服務。", "");
            }

            String[] lines = finalAnswer.split("\\r?\\n");
            StringBuilder restBuilder = new StringBuilder();
            // 使用已清理的 decision 文字做比對與去重
            String decisionLower = stripMarkdown(decisionSentence).toLowerCase();
            String svcLower = serviceMessage == null ? "" : serviceMessage.toLowerCase();
            String sizeKey = "請勿超過頁面上顯示的參考尺寸";
            // set to track normalized lines and avoid duplicates
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (String line : lines) {
                String t = line.trim();
                if (t.isEmpty()) continue;
                String tLower = t.toLowerCase();
                if (t.startsWith("1.") || t.startsWith("1 ") || tLower.contains("是否為可回收")) {
                    continue; // 跳過原本的第一行
                }
                // 跳過已在 highlight 顯示過的句子或尺寸提醒以及注意: 前綴
                if (tLower.contains(sizeKey) || tLower.contains("注意:") || tLower.contains("注意：") || tLower.contains(svcLower) || (!decisionLower.isBlank() && tLower.contains(decisionLower))) {
                    continue;
                }

                // skip common greetings and self-introductions (e.g., '嗨', '您好', '我是大型家具回收小幫手')
                if (tLower.startsWith("嗨") || tLower.startsWith("您好") || tLower.startsWith("你好")
                        || tLower.contains("我是大型家具回收") || tLower.contains("大型家具回收小幫手")
                        || tLower.contains("我是專門提供") || tLower.contains("我主要是提供")) {
                    continue;
                }

                // normalize line for duplication check: remove common punctuation and spaces
                String norm = tLower.replaceAll("[\\s\\p{Punct}，。！？；：、\"'()\\[\\]<>—–-]+", "");
                if (norm.isBlank()) continue;
                if (seen.contains(norm)) continue; // duplicate
                seen.add(norm);

                restBuilder.append(line).append("\n");
            }
            String restMd = restBuilder.toString().trim();

            // 清理 inline markdown（保留列表開頭 '* '）再轉 HTML
            restMd = sanitizeInlineMarkdown(restMd);
            // 再移除行內殘留的 '*' 或 '_'（但保留開頭作為列表標記的 '* '）
            restMd = removeLoneAsterisksAndUnderscores(restMd);
            String restHtml = markdownToSimpleHtml(restMd);
            // 若未顯示 highlight（代表模型未明確回覆可回收但檢測為 recyclable），則把尺寸提醒放到內容底部而非頂部
            if (!showHighlight) {
                restHtml = restHtml + highlightedSizeHtml;
            }
            String finalHtml = "<div>" + highlightHtml + restHtml + "</div>";
            // 最後再移除可能殘留的 Markdown 強調標記與孤立字元，避免出現在前端
            finalHtml = finalHtml.replaceAll("\\*\\*", "");
            finalHtml = finalHtml.replaceAll("__", "");
            finalHtml = finalHtml.replaceAll("`", "");
            finalHtml = finalHtml.replaceAll("~~", "");
            finalHtml = finalHtml.replaceAll("[*_`~]+", "");

            return ResponseEntity.ok(Map.of(
                    "answer", finalHtml,
                    "detectedType", detectedType,
                    "sizeWarning", sizeWarning,
                    "recyclable", recyclable,
                    "serviceMessage", serviceMessage
            ));


        } catch (Exception e) {
            logger.error("AI 判斷失敗", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI 判斷失敗：" + e.getMessage()));
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatHistoryRequest request) {
        try {
            if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "messages is required"));
            }

            // 👉 用你現在已經成功的這顆模型
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiApiKey);

            // 1️⃣ 先加一個系統提示，讓 AI 固定扮演「家具回收小幫手」
            String systemPrompt = """
                    你是一位友善的「大型家具回收諮詢小幫手」，請使用繁體中文、語氣親切並以條列整理回覆。

                    回答時請嚴格遵守下列服務規則：
                    - 僅支援家具類型：沙發、床架；若非屬於這兩類請回「其他」。
                    - 必須檢查並回報是否「尺寸符合資料庫中已登錄的可回收尺寸」。
                    - 使用者需自行將物品放置於其選擇的回收站，服務不代放置。
                    - 最晚放置時間：預約日期的前一天 17:00。
                    - 最早可預約日期：從今天起算兩天後。
                    - 費用：目前暫時不收費（未來可能調整）。
                    - 本服務採預約制；未預約或不符規定者不受理。
                    - 服務區域：本服務目前僅限於「台中市」；其他縣市不受理。若使用者不在台中市，請明確回覆不可受理並說明受理範圍。

                    回覆請簡短，且當被詢問到回收相關流程或規定時，務必包含：是否可回收、家具種類、尺寸是否符合、放置/預約時限、預約/費用說明。
                    若問題與家具回收無關，請簡短說明你主要是提供大型家具回收諮詢。
                    """;

            List<Map<String, Object>> contents = new java.util.ArrayList<>();

            // 系統提示當作第一則 content
            contents.add(Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
            ));

            // 2️⃣ 把前端的整個 messages[] 丟進 contents
            for (ChatMessageDto msg : request.getMessages()) {
                if (msg == null || msg.getText() == null || msg.getText().isBlank()) {
                    continue;
                }

                String roleLabel = "使用者";
                if (msg.getFrom() != null &&
                        (msg.getFrom().equalsIgnoreCase("ai")
                                || msg.getFrom().equalsIgnoreCase("assistant"))) {
                    roleLabel = "AI";
                }

                String text = roleLabel + "：" + msg.getText();

                contents.add(Map.of(
                        "parts", List.of(Map.of("text", text))
                ));
            }

            // 3️⃣ 組成 body 丟給 Gemini
            Map<String, Object> body = Map.of(
                    "contents", contents
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> geminiResponse =
                    restTemplate.postForObject(url, entity, Map.class);

            // 4️⃣ 把第一段文字取出來
            String answer = extractTextFromGeminiResponse(geminiResponse);

            // --- 後處理: 同 consult 用法，標準化、偵測類型、建立醒目提示、加入尺寸提醒 ---
            String normalizedAnswer = normalizeAnswerAndAddSizeReminder(answer);
            String detectedType = detectTypeFromAnswer(answer);
            String sizeWarning = "請勿超過頁面上顯示的參考尺寸；超過參考尺寸可能無法使用大型家具回收服務。";

            boolean recyclable = "沙發".equals(detectedType) || "床架".equals(detectedType);
            String serviceMessage;
            String finalAnswer = normalizedAnswer;
            if (!recyclable) {
                // 若非支援類型，加入不可回收說明；但若模型回覆已包含相同說明就不要重複加入
                serviceMessage = "抱歉，本服務目前僅支援「沙發」與「床架」的大型家具回收；您上傳的物品不屬於支援類型，因此無法使用本回收服務。";
                String naLower = normalizedAnswer == null ? "" : normalizedAnswer.toLowerCase();
                String svcLower = serviceMessage.toLowerCase();
                if (!naLower.contains(svcLower) && !naLower.contains("抱歉") && !naLower.contains("不屬於")) {
                    finalAnswer = normalizedAnswer + "\n注意：" + serviceMessage + "\n";
                } else {
                    finalAnswer = normalizedAnswer; // already said in model reply
                }
            } else {
                serviceMessage = "此家具類型可使用本服務（請同時確認尺寸符合參考值）。";
            }

            // 尺寸提醒保留醒目顏色但不使用粗體，避免過度使用粗體造成視覺疲勞
            String highlightedSizeHtml = "<p style=\"color:blue\">請注意：請勿超過頁面上顯示的參考尺寸；超過參考尺寸可能無法使用大型家具回收服務。</p>";
            // 從模型回覆抓出關鍵判斷句（若有），但只在模型明確回覆時才顯示綠色亮點；紅色（不可回收）仍然顯示
            String decisionSentence = extractDecisionSentence(finalAnswer, recyclable, serviceMessage);
            String decisionSanitized = stripMarkdown(decisionSentence);
            String answerLower = (answer == null) ? "" : answer.toLowerCase();
            boolean explicitDecisionInModel = answerLower.contains("可回收") || answerLower.contains("不可回收") || answerLower.contains("不屬於") || answerLower.contains("不受理") || answerLower.contains("抱歉");

            boolean showHighlight = !recyclable || explicitDecisionInModel; // show red always; show green only if explicit
            String highlightHtml = "";
            if (showHighlight) {
                if (recyclable) {
                    highlightHtml = "<p><strong style=\"color:green\">" + escapeHtml(decisionSanitized) + "</strong></p>" + highlightedSizeHtml;
                } else {
                    highlightHtml = "<p><strong style=\"color:red\">" + escapeHtml(decisionSanitized) + "</strong></p>" + highlightedSizeHtml;
                }
            }

            // 如果我們不會在頂端顯示 highlight，就先從 finalAnswer 移除 serviceMessage / 尺寸提醒 / 常見問候，避免出現在內容最上方
            if (!showHighlight) {
                if (serviceMessage != null && !serviceMessage.isBlank()) {
                    finalAnswer = finalAnswer.replace("注意：" + serviceMessage, "");
                    finalAnswer = finalAnswer.replace("注意:" + serviceMessage, "");
                    finalAnswer = finalAnswer.replace(serviceMessage, "");
                }
                finalAnswer = finalAnswer.replace("請勿超過頁面上顯示的參考尺寸；超過參考尺寸可能無法使用大型家具回收服務。", "");
                finalAnswer = finalAnswer.replace("嗨，我是大型家具回收小幫手。", "");
                finalAnswer = finalAnswer.replace("嗨，我是大型家具回收小幫手！", "");
                finalAnswer = finalAnswer.replace("我是大型家具回收小幫手。", "");
                finalAnswer = finalAnswer.replace("我是專門提供大型家具（沙發、床架）回收諮詢的服務。", "");
            }

            String[] lines = finalAnswer.split("\\r?\\n");
            StringBuilder restBuilder = new StringBuilder();
            // 使用已清理的 decision 文字做比對與去重
            String decisionLower = stripMarkdown(decisionSentence).toLowerCase();
            String svcLower = serviceMessage == null ? "" : serviceMessage.toLowerCase();
            String sizeKey = "請勿超過頁面上顯示的參考尺寸";
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (String line : lines) {
                String t = line.trim();
                if (t.isEmpty()) continue;
                String tLower = t.toLowerCase();
                if (t.startsWith("1.") || t.startsWith("1 ") || tLower.contains("是否為可回收")) {
                    continue; // 跳過原本的第一行
                }
                if (tLower.contains(sizeKey) || tLower.contains("注意:") || tLower.contains("注意：") || tLower.contains(svcLower) || (!decisionLower.isBlank() && tLower.contains(decisionLower))) {
                    continue;
                }

                // skip common greetings and self-introductions
                if (tLower.startsWith("嗨") || tLower.startsWith("您好") || tLower.startsWith("你好")
                        || tLower.contains("我是大型家具回收") || tLower.contains("大型家具回收小幫手")
                        || tLower.contains("我是專門提供") || tLower.contains("我主要是提供")) {
                    continue;
                }

                String norm = tLower.replaceAll("[\\s\\p{Punct}，。！？；：、\"'()\\[\\]<>—–-]+", "");
                if (norm.isBlank()) continue;
                if (seen.contains(norm)) continue;
                seen.add(norm);

                restBuilder.append(line).append("\n");
            }
            String restMd = restBuilder.toString().trim();

            // 清理 inline markdown（保留列表開頭 '* '）再轉 HTML
            restMd = sanitizeInlineMarkdown(restMd);
            restMd = removeLoneAsterisksAndUnderscores(restMd);
            String restHtml = markdownToSimpleHtml(restMd);
            if (!showHighlight) {
                restHtml = restHtml + highlightedSizeHtml;
            }
            String finalHtml = "<div>" + highlightHtml + restHtml + "</div>";
            // 最後再移除可能殘留的 Markdown 強調標記與孤立字元，避免出現在前端
            finalHtml = finalHtml.replaceAll("\\*\\*", "");
            finalHtml = finalHtml.replaceAll("__", "");
            finalHtml = finalHtml.replaceAll("`", "");
            finalHtml = finalHtml.replaceAll("~~", "");
            finalHtml = finalHtml.replaceAll("[*_`~]+", "");

            return ResponseEntity.ok(Map.of(
                    "answer", finalHtml,
                    "detectedType", detectedType,
                    "sizeWarning", sizeWarning,
                    "recyclable", recyclable,
                    "serviceMessage", serviceMessage
            ));

        } catch (Exception e) {
            logger.error("Chat AI 失敗", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Chat AI 失敗：" + e.getMessage()));
        }
    }

    /**
     * 從 Gemini 回傳 JSON 中把第一個文字答案抓出來
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromGeminiResponse(Map<String, Object> response) {
        if (response == null) {
            return "AI 沒有回應內容。";
        }

        Object candidatesObj = response.get("candidates");
        if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
            return "AI 沒有產生有效回覆。";
        }

        Object candidate0 = candidates.get(0);
        if (!(candidate0 instanceof Map<?, ?> candidate)) {
            return "AI 沒有產生有效回覆。";
        }

        Object contentObj = candidate.get("content");
        if (!(contentObj instanceof Map<?, ?> content)) {
            return "AI 沒有產生有效回覆。";
        }

        Object partsObj = content.get("parts");
        if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) {
            return "AI 沒有產生有效回覆。";
        }

        Object firstPart = parts.get(0);
        if (!(firstPart instanceof Map<?, ?> part)) {
            return "AI 沒有產生有效回覆。";
        }

        Object textObj = part.get("text");
        if (textObj == null) {
            return "AI 沒有文字回覆。";
        }

        return textObj.toString();
    }

    /**
     * 前端傳來的單一句對話
     * from: "user" 或 "ai"
     * text: 顯示在畫面上的內容
     */
    public static class ChatMessageDto {
        private String from;
        private String text;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    /**
     * 前端傳來的整個對話歷史：
     * {
     *   "messages": [
     *     { "from": "user", "text": "..." },
     *     { "from": "ai",   "text": "..." }
     *   ]
     * }
     */
    public static class ChatHistoryRequest {
        private List<ChatMessageDto> messages;

        public List<ChatMessageDto> getMessages() {
            return messages;
        }

        public void setMessages(List<ChatMessageDto> messages) {
            this.messages = messages;
        }
    }


    /**
     * 前端送上來的 JSON 形狀：
     * { "imageBase64": "..." }
     */
    public static class ImageRequest {
        private String imageBase64;

        public String getImageBase64() {
            return imageBase64;
        }

        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }
    }

    /**
     * 非常簡單版的 Markdown → HTML 轉換：
     * - 把 **粗體** 變成 <strong>…</strong>
     * - 把每一行開頭的 "* " 當作 <li> 放在 <ul> 裡
     * - 其他行包成 <p>...</p>
     */
    private String markdownToSimpleHtml(String md) {
        if (md == null || md.isBlank()) {
            return "";
        }

        // 移除可能的 Markdown 強調標記，避免前端再解析為粗體（我們只在決策句使用粗體）
        md = md.replace("**", "").replace("__", "").replace("`", "");

         String[] lines = md.split("\\r?\\n");
         StringBuilder sb = new StringBuilder();
         sb.append("<div>");

         boolean inList = false;

         for (String line : lines) {
             String trimmed = line.trim();
             if (trimmed.isEmpty()) {
                 continue;
             }

             // 列表項目：以 "* " 開頭
             if (trimmed.startsWith("* ")) {
                 if (!inList) {
                     sb.append("<ul>");
                     inList = true;
                 }
                 String item = trimmed.substring(2); // 去掉 "* "

                 // 不自動將 ** 轉為粗體，避免模型亂用粗體
                 sb.append("<li>").append(escapeHtml(item)).append("</li>");
             } else {
                 // 普通段落
                 if (inList) {
                     sb.append("</ul>");
                     inList = false;
                 }

                 // 不自動處理 ** 粗體標記，直接 escape HTML 保持純文字顯示
                 sb.append("<p>").append(escapeHtml(trimmed)).append("</p>");
             }
         }

         if (inList) {
             sb.append("</ul>");
         }

         sb.append("</div>");
         return sb.toString();
     }

    // 簡單的 HTML escape（避免 serviceMessage 中出現特殊字元導致 HTML 問題）
    private String escapeHtml(String s) {
         if (s == null) return "";
         return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // 移除常見的 Markdown 標記（強調符號、反引號）及把 [text](url) 轉為 text
    private String stripMarkdown(String s) {
         if (s == null) return "";
         // 把 markdown link 轉成純文字 label
         s = s.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1");
         // 移除 * _ ` ~ 等強調符號
         s = s.replaceAll("[*_`~]+", "");
         // 移除多個空白並 trim
         return s.replaceAll("\\s+", " ").trim();
     }

    // 針對 restMd 的 inline markdown 做更溫和的清理：移除 **bold**、*italic*、`code`、~~strike~~、以及 [text](url)
    // 同時保留開頭作為列表標記的 "* "，以免破壞列表轉換
    private String sanitizeInlineMarkdown(String md) {
         if (md == null || md.isBlank()) return md == null ? "" : md;
         String out = md;
         // 1) links [text](url) -> text
         out = out.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1");
         // remove bold/italic/underscore/strike/code globally (non-greedy)
         out = out.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
         out = out.replaceAll("__(.+?)__", "$1");
         out = out.replaceAll("\\*(.+?)\\*", "$1");
         out = out.replaceAll("_(.+?)_", "$1");
         out = out.replaceAll("~~(.+?)~~", "$1");
         out = out.replaceAll("`(.+?)`", "$1");
         // remove any remaining isolated inline '*' or '_' between non-space characters
         out = out.replaceAll("(?<=\\S)\\*(?=\\S)", "");
         out = out.replaceAll("(?<=\\S)_(?=\\S)", "");
         return out;
     }

    // 移除行內孤立的 '*' 或 '_'（避免破壞以 '* ' 開頭的列表標記）
    private String removeLoneAsterisksAndUnderscores(String s) {
        if (s == null || s.isBlank()) return s == null ? "" : s;
        StringBuilder out = new StringBuilder();
        String[] lines = s.split("\\r?\\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("* ")) {
                out.append(line);
            } else {
                // remove all '*' and '_' characters
                out.append(line.replace("*", "").replace("_", ""));
            }
            if (i < lines.length - 1) out.append('\n');
        }
        return out.toString();
    }

    // 將模型回覆中的家具類型標準化為允許清單（沙發、床架、其他），並在末尾加入尺寸提醒
    private String normalizeAnswerAndAddSizeReminder(String answer) {
        if (answer == null) return "";

        // 轉換為小寫以便比對英文字（例如 'sofa', 'bed'）並保留中文字檢查
        String lower = answer.toLowerCase();

        // 偵測關鍵字（偏寬鬆），優先判斷沙發，再判斷床/床架
        String detectedType = "其他";
        if (lower.contains("沙發") || lower.contains("sofa")) {
            detectedType = "沙發";
        } else if (lower.contains("床架") || lower.contains("床") || lower.contains("床組") || lower.contains("bed")) {
            detectedType = "床架";
        }

        // 如果模型已在回覆中直接使用其中一個標準詞，則不覆寫原文；否則在回覆增加一行說明標準化結果
        boolean containsStandard = lower.contains("沙發") || lower.contains("床架");

        StringBuilder sb = new StringBuilder();
        sb.append(answer.trim());
        sb.append("\n\n");

        if (!containsStandard) {
            // 改為純文字行，避免 Markdown 標記
            sb.append("系統判定家具類型：").append(detectedType).append("（系統資料庫僅登錄「沙發」與「床架」，若非屬於這兩類請選擇「其他」）\n");
        }

        // 注意：尺寸提醒改為在回應頂部以醒目樣式顯示，避免在內容中重複出現

        return sb.toString();
    }

    // 由模型回覆文字判斷並回傳系統允許的類型（沙發 / 床架 / 其他）
    private String detectTypeFromAnswer(String answer) {
        if (answer == null || answer.isBlank()) return "其他";
        String lower = answer.toLowerCase();
        if (lower.contains("沙發") || lower.contains("sofa")) return "沙發";
        if (lower.contains("床架") || lower.contains("床") || lower.contains("bed") || lower.contains("床組")) return "床架";
        return "其他";
    }

    // 從模型回覆中抽出「是否可回收」的關鍵句（若找不到則回傳 fallback）
    private String extractDecisionSentence(String answer, boolean recyclable, String fallback) {
         if (answer == null || answer.isBlank()) return fallback != null ? fallback : (recyclable ? "可回收" : "不可回收");
         String[] lines = answer.split("\\r?\\n");
         for (String line : lines) {
             if (line == null) continue;
             String t = line.trim();
             if (t.isEmpty()) continue;

             // 優先找包含關鍵詞的行
             String lower = t.toLowerCase();
             if (t.contains("是否為可回收")
                     || lower.contains("可回收")
                     || lower.contains("不可回收")
                     || lower.contains("無法回收")
                     || lower.contains("不屬於")
                     || lower.contains("不受理")
                     || lower.contains("抱歉")
                     || lower.contains("無法受理")
                     || lower.matches(".*\\b(is|yes|no)\\b.*")) {
                 // 當行可能包含前綴編號，例如 '1. 是否為可回收家具：是'，移除編號再回傳
                 String cleaned = t.replaceAll("^\\d+\\.\\s*", "").trim();
                 return cleaned;
             }
         }

         // 若沒有找到適合的句子，使用 fallback（serviceMessage 或簡短回覆）
         if (fallback != null && !fallback.isBlank()) return fallback;
         return recyclable ? "此家具類型可使用本服務。" : "抱歉，本服務目前不支援該家具類型。";
     }

}
