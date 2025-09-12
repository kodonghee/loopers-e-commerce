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
            summary = "일간 상품 랭킹 조회",
            description = "날짜, 페이지 번호, 페이지당 상품 수에 따라 일간 상품 랭킹을 조회합니다."
    )
    @GetMapping("")
    ApiResponse<List<RankingV1Dto.RankingResponse>> getRankings(
            @Parameter(description = "조회 날짜 (yyyyMMdd)", example = "20250912")
            @RequestParam(name = "date") String date,

            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "페이지당 상품 수", example = "20")
            @RequestParam(name = "size", defaultValue = "20") int size
    );
}
