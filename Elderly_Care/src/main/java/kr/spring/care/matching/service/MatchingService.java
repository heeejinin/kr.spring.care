package kr.spring.care.matching.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import kr.spring.care.matching.constant.MatchingStatus;
import kr.spring.care.matching.dto.MatchingRequestDto;
import kr.spring.care.matching.entity.Matching;
import kr.spring.care.matching.repository.MatchingRepository;
import lombok.RequiredArgsConstructor;
@Service
@Transactional
@RequiredArgsConstructor
public class MatchingService {

    private final MatchingRepository matchingRepository;

    // 매칭 생성
    public Matching createMatching(@Valid MatchingRequestDto matchingRequestDto) {
        Matching matching = new Matching();
        matching.setSeniorId(matchingRequestDto.getSeniorId());
        matching.setCaregiverId(matchingRequestDto.getCaregiverId());
        matching.setStartDate(matchingRequestDto.getStartDate());
        matching.setEndDate(matchingRequestDto.getEndDate());
        matching.setStatus(MatchingStatus.REQUESTED); // 초기 상태 설정
        return matchingRepository.save(matching);
    }

    // 매칭 ID로 매칭 정보 조회
    public Matching getMatchingById(Long matchingId) {
        return matchingRepository.findById(matchingId)
                .orElseThrow(() -> new EntityNotFoundException("Matching not found: " + matchingId));
    }

    // 매칭 정보 업데이트
    public Matching updateMatching(Matching matching) {
        // 데이터베이스에 이미 존재하는 매칭인지 확인
        Matching existingMatching = matchingRepository.findById(matching.getId())
                .orElseThrow(() -> new EntityNotFoundException("Matching not found: " + matching.getId()));
        // 필요한 속성 업데이트
        existingMatching.setStartDate(matching.getStartDate());
        existingMatching.setEndDate(matching.getEndDate());
        existingMatching.setStatus(matching.getStatus());
        return matchingRepository.save(existingMatching);
    }

    // 매칭 취소
    public void cancelMatching(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new EntityNotFoundException("Matching not found: " + matchingId));
        matching.setStatus(MatchingStatus.CANCELLED); // 상태를 취소로 변경
        matchingRepository.save(matching);
    }


    // 기타 필요한 메소드들을 여기에 추가
}