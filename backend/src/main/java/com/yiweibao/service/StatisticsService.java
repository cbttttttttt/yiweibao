package com.yiweibao.service;

import com.yiweibao.dto.EquipmentFaultRank;
import com.yiweibao.dto.FaultTypeAvgTime;
import com.yiweibao.dto.FaultTypeStat;
import com.yiweibao.dto.StatisticsOverview;
import com.yiweibao.repository.WorkOrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StatisticsService {

    private final WorkOrderRepository workOrderRepository;

    public StatisticsService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    public StatisticsOverview getOverview() {
        long total = workOrderRepository.count();
        long thisMonth = workOrderRepository.countSince(
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0));
        long pending = workOrderRepository.countByStatusIn(List.of(0));
        double avgHours = calculateAvgRepairHours();

        return new StatisticsOverview(total, thisMonth, pending, avgHours);
    }

    public List<FaultTypeStat> getFaultTypes() {
        List<Object[]> results = workOrderRepository.countByFaultCategory();
        List<FaultTypeStat> stats = new ArrayList<>();
        for (Object[] row : results) {
            String category = (String) row[0];
            long count = ((Number) row[1]).longValue();
            stats.add(new FaultTypeStat(category != null ? category : "(未分类)", count));
        }
        stats.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return stats;
    }

    public List<FaultTypeAvgTime> getFaultAvgTime() {
        List<Object[]> results = workOrderRepository.avgTimeByFaultCategory();
        List<FaultTypeAvgTime> list = new ArrayList<>();
        for (Object[] row : results) {
            String category = (String) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal avgVal = (BigDecimal) row[2];
            double avgHours = avgVal != null ? avgVal.doubleValue() : 0.0;
            list.add(new FaultTypeAvgTime(
                    category != null ? category : "(未分类)",
                    count,
                    Math.round(avgHours * 10.0) / 10.0
            ));
        }
        list.sort(Comparator.comparingDouble(FaultTypeAvgTime::getAvgHours).reversed());
        return list;
    }

    public List<EquipmentFaultRank> getTopEquipment(int limit) {
        List<Object[]> results = workOrderRepository.countByEquipment(PageRequest.of(0, limit));
        List<EquipmentFaultRank> ranks = new ArrayList<>();
        for (Object[] row : results) {
            ranks.add(new EquipmentFaultRank(
                    (Long) row[0], (String) row[1], (String) row[2], (Long) row[3]));
        }
        return ranks;
    }

    private double calculateAvgRepairHours() {
        Object result = workOrderRepository.avgRepairHours();
        if (result == null) return 0.0;
        return Math.round(((Number) result).doubleValue() * 10.0) / 10.0;
    }
}
