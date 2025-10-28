package edu.fcu.furniturerecyclingbackend.config;

import edu.fcu.furniturerecyclingbackend.model.Station;
import edu.fcu.furniturerecyclingbackend.repository.StationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.Arrays;
import java.util.List;

/**
 * 建立station表初始化資料
 * 啟動時自動插入五個定點資料（stationId: DP001~DP005, name: 一號定點~五號定點）
 * 若資料已存在則不重複插入
 */
@Component
@RequiredArgsConstructor
public class StationDataLoader {
    private final StationRepository stationRepository;

    @PostConstruct
    public void initStations() {
        List<Station> stations = Arrays.asList(
            createStation("DP001", "一號定點", "台中市西區英才路1號"),
            createStation("DP002", "二號定點", "台中市西區英才路2號"),
            createStation("DP003", "三號定點", "台中市西區英才路3號"),
            createStation("DP004", "四號定點", "台中市西區英才路4號"),
            createStation("DP005", "五號定點", "台中市西區英才路5號")
        );
        for (Station s : stations) {
            if (!stationRepository.existsById(s.getStationId())) {
                stationRepository.save(s);
            }
        }
    }

    private Station createStation(String id, String name, String address) {
        Station s = new Station();
        s.setStationId(id);
        s.setName(name);
        s.setAddress(address);
        s.setServiceStart(Time.valueOf("09:00:00"));
        s.setServiceEnd(Time.valueOf("18:00:00"));
        s.setServiceWeekday(new Integer[]{1,2,3,4,5});
        s.setAmount((short)5);
        return s;
    }
}

