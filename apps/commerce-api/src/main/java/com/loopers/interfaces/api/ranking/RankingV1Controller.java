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
            @RequestParam("date") String date,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<ProductResult> products = rankingService.getTopProducts(parsedDate, page, size);
        List<RankingV1Dto.RankingResponse> response = products.stream()
                .map(RankingV1Dto.RankingResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

}
