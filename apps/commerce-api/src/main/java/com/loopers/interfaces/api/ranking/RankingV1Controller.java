package com.loopers.interfaces.api.ranking;

import com.loopers.application.product.ProductResult;
import com.loopers.application.ranking.RankingService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rankings")
public class RankingV1Controller implements RankingV1ApiSpec {

    private final RankingService rankingService;

    @GetMapping
    @Override
    public ApiResponse<List<RankingV1Dto.RankingResponse>> getRankings(
            @RequestParam("period") String period,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        List<ProductResult> products;

        switch (period.toLowerCase()) {
            case "daily" -> {
                if (date == null) {
                    throw new IllegalArgumentException("date is required for daily rankings");
                }
                LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
                products = rankingService.getDailyTopProducts(parsedDate, page, size);
            }
            case "weekly" -> products = rankingService.getWeeklyTopProducts(page, size);
            case "monthly" -> products = rankingService.getMonthlyTopProducts(page, size);
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        }
        List<RankingV1Dto.RankingResponse> response = products.stream()
                .map(RankingV1Dto.RankingResponse::from)
                .toList();

        return ApiResponse.success(response);
    }
}
