package edu.fcu.furniturerecyclingbackend.service;

import edu.fcu.furniturerecyclingbackend.model.Station;
import edu.fcu.furniturerecyclingbackend.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StationService {

    private final StationRepository stationRepository;

    @Autowired
    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    // 根據 stationId 查找回收站
    public Optional<Station> getStationById(String stationId) {
        return stationRepository.findById(stationId);
    }

    // 創建新回收站
    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    // 更新回收站資料
    public Station updateStation(String stationId, Station updatedStation) {
        if (stationRepository.existsById(stationId)) {
            updatedStation.setStationId(stationId);
            return stationRepository.save(updatedStation);
        }
        return null;
    }

    // 刪除回收站
    public void deleteStation(String stationId) {
        stationRepository.deleteById(stationId);
    }
}
