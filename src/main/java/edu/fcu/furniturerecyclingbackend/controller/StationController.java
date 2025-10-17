package edu.fcu.furniturerecyclingbackend.controller;

import edu.fcu.furniturerecyclingbackend.model.Station;
import edu.fcu.furniturerecyclingbackend.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    // 根據 stationId 查找回收站
    @GetMapping("/{stationId}")
    public ResponseEntity<Station> getStationById(@PathVariable UUID stationId) {
        Optional<Station> station = stationService.getStationById(stationId);
        return station.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 創建新回收站
    @PostMapping
    public ResponseEntity<Station> createStation(@RequestBody Station station) {
        Station createdStation = stationService.createStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStation);
    }

    // 更新回收站資料
    @PutMapping("/{stationId}")
    public ResponseEntity<Station> updateStation(@PathVariable UUID stationId, @RequestBody Station updatedStation) {
        Station updated = stationService.updateStation(stationId, updatedStation);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // 刪除回收站
    @DeleteMapping("/{stationId}")
    public ResponseEntity<Void> deleteStation(@PathVariable UUID stationId) {
        stationService.deleteStation(stationId);
        return ResponseEntity.noContent().build();  // 返回 204 No Content
    }
}

