package edu.fcu.furniturerecyclingbackend.controller;

//共同表單設定:放置地點資訊
//定點放置區 : 總共會有五個定點供選擇，下拉式選單選擇定點放置區
//清運日期 : 下拉式選單，點選選擇年月日

import edu.fcu.furniturerecyclingbackend.model.Schedule;
import edu.fcu.furniturerecyclingbackend.model.Station;
import edu.fcu.furniturerecyclingbackend.repository.ScheduleRepository;
import edu.fcu.furniturerecyclingbackend.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PublicLookupController {

    private final StationRepository stationRepository;
    private final ScheduleRepository scheduleRepository;

    // 站點下拉
    @GetMapping("/stations")
    public List<Map<String, String>> stations() {
        List<Station> stations = stationRepository.findAll();
        return stations.stream()
                .map(s -> Map.of(
                        "value", s.getStationId().toString(),
                        "label", s.getName()
                ))
                .toList();
    }

    // 指定日期的班表下拉
    @GetMapping("/schedules")
    public List<Map<String, String>> schedules(@RequestParam LocalDate date) {
        List<Schedule> all = scheduleRepository.findAll();
        return all.stream()
                .filter(s -> s.getScheduleDate().isEqual(date))
                .sorted(Comparator.comparingInt(Schedule::getSequenceNo))
                .map(s -> Map.of(
                        "value", s.getScheduleId().toString(),
                        "label", String.format("%s（序號 %d）",
                                s.getEta().atZone(ZoneId.systemDefault()).toLocalTime(), s.getSequenceNo())
                ))
                .toList();
    }
}
