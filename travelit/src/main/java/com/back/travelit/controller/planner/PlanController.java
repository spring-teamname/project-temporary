package com.back.travelit.controller.planner;

import com.back.travelit.dto.request.planner.PlanCreateReq;
import com.back.travelit.dto.request.planner.ScheduleCreateReq;
import com.back.travelit.dto.request.planner.ScheduleReplaceReq;
import com.back.travelit.dto.response.planner.PlanLocCodeRes;
import com.back.travelit.dto.response.planner.PlanLocInfo;
import com.back.travelit.security.LoginUser;
import com.back.travelit.security.oauth.UserDTO;
import com.back.travelit.service.planner.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/planner")
@RequiredArgsConstructor
public class PlanController {

    @Autowired
    private PlanService planService;
    private Model model;
    private ModelAndView modelAndView;

    //플래너 만들기 페이지
    @GetMapping("/plan-first")
    public String makePlanPage(){
        return "/planner/plan-first";
    }

    //플래너 만들기
    @PostMapping("/plan-make")
    public String makePlan(@ModelAttribute("createReqDTO") PlanCreateReq createReqDTO, Model model){

        int userId = 2;

        //플래너 기본 정보 값 넣고,생성된 플래너 아이디 값 받기
        int planId = planService.setMakePlan(createReqDTO);

        log.info("planId");
        //user가 입력한 플래너 값을 리턴하는 view에 보내기
        model.addAttribute("createReqDTO", createReqDTO);

        //여행기간
        model.addAttribute("dates", dates(createReqDTO));

        //지역 코드를 이용하여 지역 정보 값 받기
        List<PlanLocCodeRes> states = planService.selectAllLocName(createReqDTO.getLocCode());
        //지역 코드 및 이름 값을 리턴하는 view에 보내기
        model.addAttribute("states", states);

        //즐겨찾기 지역 정보
        model.addAttribute("allMarks",allMarkLocation(userId));

        //전체 지역 정보
        model.addAttribute("allLocations", allLocation());

        return "planner/plan-second";
    }
    //여행기간 계산
    private List<LocalDate> dates(PlanCreateReq createReqDTO){
        List<LocalDate> dates = new ArrayList<>();
        LocalDate startDate = createReqDTO.getStartDate();
        LocalDate endDate = createReqDTO.getEndDate();

        while (!startDate.isAfter(endDate)) {
            dates.add(startDate);
            startDate = startDate.plusDays(1);
        }
        return dates;
    }
    //전체 지역 정보 불러오기
    private List<PlanLocInfo> allLocation(){
        return planService.getLocInfo();
    }
    //유저 즐겨찾기 지역 정보 불러오기
    private List<PlanLocInfo> allMarkLocation(int userId){
        return planService.getMarkLocInfo(userId);
    }


    //스케줄 생성
    @PostMapping("/make-sched")
    public String makeShed(@ModelAttribute("schedCreateReq") ScheduleCreateReq schedCreateReq, Model model){

        //상세 스케줄 값 넣기
        planService.setMakeSched(schedCreateReq);
        return "/planner/plan-detail";
    }

    //내 플래너 리스트
    @GetMapping("/main")
    public String mainList(@LoginUser UserDTO userDTO, Model model){
        int userId = 2;
        model.addAttribute("myPlanList",planService.getMyPlanList(userId));
        //지역정보 리스트(조회수순 6개)
        model.addAttribute("locList",planService.selectLocList());
        return "planner/mainList";
    }

    //스케줄 수정
    @GetMapping("/plan-edit/{plan_id}")
    public String editPlan(@ModelAttribute("scheduleReplaceReq") ScheduleReplaceReq scheduleReplaceReq, Model model){
        planService.setReschedule(scheduleReplaceReq);
        return "planner/plan-detail";
    }

    //플래너 상세보기
    @GetMapping("/plan-detail/{planId}")
    public String detailPlan(@PathVariable("planId") int planId, Model model){
        model.addAttribute("planInfos",planService.getPlanDetail(planId));
        model.addAttribute("scheds",planService.getSchedDetail(planId));
        return "planner/plan-detail";
    }
}
