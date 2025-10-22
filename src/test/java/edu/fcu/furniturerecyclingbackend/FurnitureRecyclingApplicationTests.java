package edu.fcu.furniturerecyclingbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // ⭐ 要記得 import 這個

@SpringBootTest
@ActiveProfiles("dev") // 讓測試時使用 application-dev.properties（連你的 Supabase）
class FurnitureRecyclingApplicationTests {

    @Test
    void contextLoads() {
    }

}

