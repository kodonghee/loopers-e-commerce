package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Ranking V1 API", description = "상품 랭킹 관련 API 입니다.")
public interface RankingV1ApiSpec {

    @Operation(
            summary = "상품 랭킹 조회",
            description = """
                period에 따라 조회되는 기준이 달라집니다.
                - daily: 특정 날짜 기준 (date 필수)
                - weekly: 최신 주간 랭킹 (date 불필요)
                - monthly: 최신 월간 랭킹 (date 불필요)
                """
    )
    @GetMapping("")
    ApiResponse<List<RankingV1Dto.RankingResponse>> getRankings(
            @Parameter(description = "조회 기간 (daily/weekly/monthly)", example = "daily")
            @RequestParam(name = "period") String period,

            @Parameter(description = "조회 날짜 (yyyyMMdd) — daily에서만 필수", example = "20250912")
            @RequestParam(name = "date", required = false) String date,

            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "페이지당 상품 수", example = "20")
            @RequestParam(name = "size", defaultValue = "20") int size
    );
}
