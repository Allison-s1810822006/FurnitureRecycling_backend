package edu.fcu.furniturerecyclingbackend.monitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 獨立的命令列監控工具，用來輪詢現有的 /api/applications REST API，顯示目前申請案件及其狀態。
 *
 * 此類別為獨立檔案，不會修改任何現有的 API 或 Controller。
 *
 * 使用方式：
 *  - java -cp target/* edu.fcu.furniturerecyclingbackend.monitor.ApplicationStatusMonitor [baseUrl]
 *  - 預設 baseUrl: http://localhost:8080
 *  powershell 指令: .\mvnw.cmd --% org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=edu.fcu.furniturerecyclingbackend.monitor.ApplicationStatusMonitor -Dexec.args="http://localhost:8080"
 *
 * 互動指令：
 *  - list               : 立即抓取並顯示摘要
 *  - raw                : 顯示 API 回傳的完整 JSON（方便 debug 欄位）
 *  - watch <秒數>       : 每 <秒數> 秒輪詢一次，當有變更時列印
 *  - stop               : 停止正在進行的 watch
 *  - exit               : 離開監控程式
 */
public class ApplicationStatusMonitor {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        String baseUrl = args.length > 0 ? args[0] : "http://localhost:8080";
        String endpoint = baseUrl.replaceAll("/+$", "") + "/api/applications";

        System.out.println("申請案件狀態監控器");
        System.out.println("使用的端點: " + endpoint);
        System.out.println("指令: list | raw | watch <秒數> | stop | exit");

        Scanner scanner = new Scanner(System.in);
        AtomicBoolean watching = new AtomicBoolean(false);
        Thread watcherThread = null;

        while (true) {
            System.out.print("監控> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            try {
                if (cmd.equals("list")) {
                    fetchAndPrint(endpoint);

                } else if (cmd.equals("raw")) {
                    // 直接印出完整 JSON（漂亮列印）
                    JsonNode root = fetchRaw(endpoint);
                    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));

                } else if (cmd.equals("watch")) {
                    if (parts.length < 2) {
                        System.out.println("用法: watch <秒數>");
                        continue;
                    }
                    int seconds = Integer.parseInt(parts[1]);
                    if (seconds <= 0) {
                        System.out.println("秒數必須大於 0");
                        continue;
                    }
                    if (watching.get()) {
                        System.out.println("已在監控中。請使用 'stop' 停止。");
                        continue;
                    }
                    watching.set(true);
                    watcherThread = new Thread(() -> {
                        JsonNode last = null;
                        while (watching.get()) {
                            try {
                                JsonNode current = fetchRaw(endpoint);
                                if (!current.equals(last)) {
                                    System.out.println("\n[" + now() + "] 偵測到變更 / 目前狀態:");
                                    printSummary(current);
                                    last = current;
                                }
                                Thread.sleep(seconds * 1000L);
                            } catch (InterruptedException ignored) {
                                Thread.currentThread().interrupt();
                                break;
                            } catch (Exception e) {
                                System.out.println("監控時發生錯誤: " + e.getMessage());
                                try { Thread.sleep(seconds * 1000L); } catch (InterruptedException ie) { break; }
                            }
                        }
                    });
                    watcherThread.setDaemon(true);
                    watcherThread.start();
                    System.out.println("已開始每 " + seconds + " 秒監控一次。輸入 'stop' 停止。");

                } else if (cmd.equals("stop")) {
                    if (!watching.get()) {
                        System.out.println("目前未在監控中。");
                        continue;
                    }
                    watching.set(false);
                    if (watcherThread != null) {
                        watcherThread.interrupt();
                        watcherThread = null;
                    }
                    System.out.println("已停止監控。");

                } else if (cmd.equals("exit")) {
                    if (watching.get()) {
                        watching.set(false);
                        if (watcherThread != null) watcherThread.interrupt();
                    }
                    System.out.println("離開監控程式。");
                    break;
                } else {
                    System.out.println("未知的指令: " + cmd + "（可用: list, raw, watch, stop, exit）");
                }
            } catch (NumberFormatException nfe) {
                System.out.println("無效的數字: " + (parts.length > 1 ? parts[1] : ""));
            } catch (Exception e) {
                System.out.println("錯誤: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static JsonNode fetchRaw(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .GET()
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return mapper.readTree(response.body());
        } else {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private static void fetchAndPrint(String endpoint) {
        try {
            JsonNode root = fetchRaw(endpoint);
            printSummary(root);
        } catch (Exception e) {
            System.out.println("擷取申請資料失敗: " + e.getMessage());
        }
    }

    private static void printSummary(JsonNode root) {
        if (root == null || !root.isArray()) {
            System.out.println("沒有申請資料或回傳格式非預期: " + root);
            return;
        }
        System.out.printf("%s %-40s %-12s %-30s %-15s\n", "編號", "案件ID", "狀態", "清運地點", "預定日期");
        int i = 1;
        Iterator<JsonNode> it = root.elements();
        while (it.hasNext()) {
            JsonNode node = it.next();
            // 優先使用後端 DTO 的欄位名稱（ApplicationResponseDto）
            String id = node.path("applicationId").asText(node.path("id").asText(node.path("uuid").asText("<no-id>")));
            String status = node.path("status").asText("<no-status>");
            String collectionPoint = node.path("dropPointCode").asText(node.path("collectionPoint").asText(node.path("collection_point").asText("<no-location>")));
            String scheduledDate = node.path("requestedDate").asText(node.path("scheduledDate").asText(node.path("scheduled_date").asText("<no-date>")));
            System.out.printf("%2d %-40s %-12s %-30s %-15s\n", i++, id, status, collectionPoint, scheduledDate);

            // 若未能取得 id 或預定日期，印出原始 JSON 物件以供 debug
            if ("<no-id>".equals(id) || "<no-date>".equals(scheduledDate)) {
                try {
                    System.out.println("  >> 原始回傳（此筆可能缺欄位或為 null）：");
                    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
                } catch (Exception e) {
                    System.out.println("  >> 無法列印原始 JSON: " + e.getMessage());
                }
            }
        }
    }

    private static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}