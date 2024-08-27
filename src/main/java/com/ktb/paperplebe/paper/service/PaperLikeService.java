package com.ktb.paperplebe.paper.service;

import com.ktb.paperplebe.paper.entity.Paper;
import com.ktb.paperplebe.paper.entity.PaperLike;
import com.ktb.paperplebe.paper.repository.PaperLikeRepository;
import com.ktb.paperplebe.paper.repository.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class PaperLikeService {

    private final PaperLikeRepository paperLikeRepository;
    private final PaperRepository paperRepository;

    public void increaseLikeCount(Long paperId) {
        if (paperLikeRepository.existsByPaper_Id(paperId)) {
            throw new IllegalStateException("This paper has already been liked.");
        }

        Paper findPaper = findPaperOrThrow(paperId);

        PaperLike like = new PaperLike(findPaper);
        findPaper.addLike(like);

        paperLikeRepository.save(like);
    }

    public void decreaseLikeCount(Long paperId) {
        PaperLike like = paperLikeRepository.findByPaper_Id(paperId).orElseThrow(() -> new IllegalStateException("This paper was not previously liked."));
        Paper findPaper = findPaperOrThrow(paperId);

        findPaper.removeLike(like);

        paperLikeRepository.delete(like);
    }

    public Map<Long, Boolean> getLikeStatus(List<Long> paperIds) {
        List<PaperLike> paperLikes = paperLikeRepository.findByPaper_IdIn(paperIds);

        Map<Long, Boolean> result = new HashMap<>();

        paperLikes.forEach(paperLike -> result.put(paperLike.getPaper().getId(), true));
        paperIds.forEach(paperId -> result.putIfAbsent(paperId, false));
        return result;
    }

    private Paper findPaperOrThrow(Long paperId) {
        return paperRepository.findById(paperId)
                .orElseThrow(() -> new NoSuchElementException("Paper not found."));
    }
}