src/main/java/edu....
├─ controller/        # 接 API，參數進出都用 DTO
│  └─ RequestController.java
├─ service/           # 商業邏輯：計體積、決定車型、呼叫 repository
│  └─ RequestService.java
├─ repository/        # 只負責存取資料庫
│  ├─ ApplicationsRepository.java
│  └─ FurnitureRepository.java
├─ model/             # 資料庫對應的 Entity/Record（applications、Furniture）
│  ├─ Application.java
│  └─ FurnitureItem.java
├─ dto/               # API 用的請求/回應物件與驗證
│  ├─ CreateRequestDto.java
│  ├─ ItemDto.java
│  └─ CreateResponseDto.java
├─ config/            # CORS、例外處理、Jackson 設定等
│  ├─ WebConfig.java          # 給 Vue 本機可呼叫後端
│  └─ GlobalExceptionHandler.java (可選)
└─ FurnitureRecyclingApplication.java
