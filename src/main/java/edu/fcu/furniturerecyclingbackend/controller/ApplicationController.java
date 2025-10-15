package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.Application;
import edu.fcu.furniturerecyclingbackend.repository.ApplicationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationRepository repo;

    public ApplicationController(ApplicationRepository repo) {
        this.repo = repo;
    }

    // 這支會觸發 JPA 查詢 → 能驗證 entity/repo 有沒有被掃描到
    @GetMapping
    public List<Application> all() {
        return repo.findAll();
    }

    // 額外：看資料庫目前有幾筆
    @GetMapping("/count")
    public long count() {
        return repo.count();
    }
}
