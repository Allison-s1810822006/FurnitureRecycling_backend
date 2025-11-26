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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class FurnitureConsultController {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/furniture-consult")
    public ResponseEntity<?> consult(@RequestBody ImageRequest request) {
        try {
            // 🔍 這裡加印出金鑰長度
            System.out.println("Gemini key length = " +
                    (geminiApiKey == null ? "null" : geminiApiKey.length()));
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
                    你是一個大型家具回收諮詢助手，請用繁體中文回答。

                    回覆請使用三點條列（每點一行），範例格式如下：
                    1. 是否為可回收家具：是/否
                    2. 家具種類（請僅使用下列值之一：沙發、床架、其他）
                    3. 備註（例如是否含玻璃或電子元件等，以及是否符合大型家具清運條件）

                    注意：系統資料庫目前僅記錄「沙發」與「床架」兩種家具類型，請務必在第2點僅回傳這些標準詞（或回傳「其他」）。

                    請簡短且以條列式輸出，方便前端直接顯示。
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
                // 若非支援類型，加入不可回收說明並回傳提醒
                serviceMessage = "抱歉，本服務目前僅支援「沙發」與「床架」的大型家具回收；您上傳的物品不屬於支援類型，因此無法使用本回收服務。";
                finalAnswer = normalizedAnswer + "\n* 注意：" + serviceMessage + "\n";
            } else {
                serviceMessage = "此家具類型可使用本服務（請同時確認尺寸符合參考值）。";
            }

            // --- 建立頂部醒目提示 (綠色代表可回收，紅色代表不可回收)，並加入醒目的尺寸提醒（藍色粗體） ---
            String highlightedSizeHtml = "<p><strong style=\"color:blue\">請注意：請勿超過頁面上顯示的參考尺寸；超過參考尺寸可能無法使用大型家具回收服務。</strong></p>";

            String highlightHtml;
            if (recyclable) {
                highlightHtml = "<p><strong style=\"color:green\">是否為可回收家具：是</strong></p>" + highlightedSizeHtml;
            } else {
                // 不可回收：直接顯示說明 (紅色粗體)，並顯示醒目的尺寸提醒
                highlightHtml = "<p><strong style=\"color:red\">" + escapeHtml(serviceMessage) + "</strong></p>" + highlightedSizeHtml;
            }

            // 移除模型回覆中可能包含的第一行判斷（以免重複顯示）
            String[] lines = finalAnswer.split("\\r?\\n");
            StringBuilder restBuilder = new StringBuilder();
            for (String line : lines) {
                String t = line.trim();
                if (t.startsWith("1.") || t.startsWith("1 ") || t.toLowerCase().contains("是否為可回收")) {
                    continue; // 跳過原本的第一行
                }
                restBuilder.append(line).append("\n");
            }
            String restMd = restBuilder.toString().trim();

            // 把剩下的 Markdown 轉成 HTML，然後把 highlight 放在最上方
            String restHtml = markdownToSimpleHtml(restMd);
            String finalHtml = "<div>" + highlightHtml + restHtml + "</div>";

            return ResponseEntity.ok(Map.of(
                    "answer", finalHtml,
                    "detectedType", detectedType,
                    "sizeWarning", sizeWarning,
                    "recyclable", recyclable,
                    "serviceMessage", serviceMessage
            ));


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI 判斷失敗：" + e.getMessage()));
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

                // 先把 **...** 換成 <strong>...</strong>
                item = item.replace("**", "<strong>");
                // 如果出現 <strong> 兩次交錯的情況，可以簡單處理：
                item = item.replace("<strong><strong>", "</strong>");

                sb.append("<li>").append(item).append("</li>");
            } else {
                // 普通段落
                if (inList) {
                    sb.append("</ul>");
                    inList = false;
                }

                String text = trimmed.replace("**", "<strong>");
                sb.append("<p>").append(text).append("</p>");
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
            sb.append("* 系統判定家具類型：").append(detectedType).append("（系統資料庫僅登錄「沙發」與「床架」，若非屬於這兩類請選擇「其他」）\n");
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

}
