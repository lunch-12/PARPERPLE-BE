package com.ktb.paperplebe.search.controller;

import com.ktb.paperplebe.paper.dto.PaperRequest;
import com.ktb.paperplebe.paper.dto.PaperResponse;
import com.ktb.paperplebe.paper.service.PaperService;
import com.ktb.paperplebe.search.dto.SearchRequest;
import com.ktb.paperplebe.search.dto.SearchResponse;
import com.ktb.paperplebe.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/search")
@RestController
public class SearchController {
    private final SearchService searchService;

    @PostMapping
    public ResponseEntity<?> search(@RequestBody final SearchRequest searchRequest) {
        final SearchResponse searchResponse = searchService.search(searchRequest);
        return ResponseEntity.ok(searchResponse);
    }
}