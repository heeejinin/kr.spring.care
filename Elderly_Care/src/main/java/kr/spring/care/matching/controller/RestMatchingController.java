package kr.spring.care.matching.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.spring.care.matching.constant.MatchingStatus;
import kr.spring.care.matching.dto.CaregiverDetail;
import kr.spring.care.matching.dto.MatchingDetail;
import kr.spring.care.matching.dto.MatchingRequestDto;
import kr.spring.care.matching.entity.Matching;
import kr.spring.care.matching.service.MatchingService;
import kr.spring.care.user.entity.Guardian;
import kr.spring.care.user.entity.Senior;
import kr.spring.care.user.service.CaregiverService;

@RestController
@RequestMapping("/m/matching")
public class RestMatchingController {

    @Autowired
    private MatchingService matchingService;
    @Autowired
    private CaregiverService caregiverService;

    @GetMapping("/caregivers")
    public ResponseEntity<Page<CaregiverDetail>> getCaregivers(
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String word,
            Pageable pageable) {
        Page<CaregiverDetail> caregiversPage;

        if (field != null && word != null && !field.isEmpty() && !word.isEmpty()) {
            caregiversPage = caregiverService.searchCaregivers(field, word, pageable);
        } else {
            caregiversPage = caregiverService.findCaregiversPageable(pageable);
        }

        return ResponseEntity.ok(caregiversPage);
    }

    @GetMapping("/caregivers/{caregiverId}")
    public ResponseEntity<CaregiverDetail> getCaregiverDetail(@PathVariable Long caregiverId) {
        CaregiverDetail caregiverDetail = caregiverService.findCaregiverById(caregiverId);
        return ResponseEntity.ok(caregiverDetail);
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<Matching>> getJobs(Pageable pageable) {
        Page<Matching> matchingsPage = matchingService.findMatchingsPageable(pageable);
        return ResponseEntity.ok(matchingsPage);
    }

    @GetMapping("/jobs/{matchingId}")
    public ResponseEntity<MatchingDetail> getJobDetail(@PathVariable Long matchingId) {
        MatchingDetail matchingDetail = matchingService.getMatchingDetailById(matchingId);
        return ResponseEntity.ok(matchingDetail);
    }

    // 매칭 생성 페이지
    @GetMapping("/request")
    public ResponseEntity<CaregiverDetail> requestMatching(@RequestParam(required = false) Long caregiverId) {
        if (caregiverId != null) {
            // 요양보호사 ID가 주어진 경우 요양보호사 정보를 가져옵니다.
            CaregiverDetail caregiverDetail = caregiverService.findCaregiverById(caregiverId);
            return ResponseEntity.ok(caregiverDetail);
        } else {
            return ResponseEntity.ok().build();
        }
    }

	 // 매칭 요청 처리
	 @PostMapping("/request")
	 public ResponseEntity<Void> processRequestMatching(@RequestBody MatchingRequestDto matchingRequestDto, BindingResult bindingResult) {
		    if (bindingResult.hasErrors()) {
		        return ResponseEntity.badRequest().build();
		    }

		    // 사용자 역할에 따른 처리
		    String userRole = matchingRequestDto.getUserRole();
		    if (userRole.equals("elderly")) {
		        Senior senior = matchingService.createSenior(matchingRequestDto);
		        matchingRequestDto.setSeniorId(senior.getSeniorId());
		    } else if (userRole.equals("guardian")) {
		        Guardian guardian = matchingService.createGuardian(matchingRequestDto);
		        matchingRequestDto.setSeniorId(guardian.getSenior().getSeniorId());
		    }

		    // 매칭 상태 설정: CaregiverId가 양의 정수인 경우 REQUESTED, 그렇지 않은 경우 POSTED
		    boolean isCaregiverIdValid = matchingRequestDto.getCaregiverId() != null && matchingRequestDto.getCaregiverId() > 0;
		    MatchingStatus status = isCaregiverIdValid ? MatchingStatus.REQUESTED : MatchingStatus.POSTED;
		    matchingRequestDto.setStatus(status);

		    matchingService.createMatching(matchingRequestDto);

		    return ResponseEntity.created(null).build();
		}
}
