package zerobase.weather.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController {
    private final DiaryService diaryService;
    
    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }
    
    @Operation(summary = "일기 만들기", description = "일기를 만드는 API")
    @PostMapping("/create/diary")
    void createDiary(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) @Parameter(
                    description = "yyyy-MM-dd", example = "2000-04-20") LocalDate date,
            @RequestBody String text) {
        
        diaryService.createDiary(date, text);
    }
    
    @Operation(summary = "일기 지우기", description = "해당 날짜 모든 일기를 지우는 API")
    @DeleteMapping("/delete/diary")
    void deleteDiary(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) @Parameter(
                    description = "yyyy-MM-dd", example = "2000-04-20") LocalDate date) {
        diaryService.deleteDiary(date);
    }
    
    @Operation(summary = "구간 일기 읽기", description = "두 날짜 사이의 모든 일기 읽는 API")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) @Parameter(
                    description = "시작 날짜 yyyy-MM-dd", example = "2000" +
                    "-04-20") LocalDate startDate,
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) @Parameter(
                    description = "종료 날짜 yyyy-MM-dd", example = "2000" +
                    "-05-20") LocalDate endDate) {
        return diaryService.readDiaries(startDate, endDate);
    }
    
    @Operation(summary = "일기 읽기", description = "해당 날짜 일기를 읽는 API")
    @GetMapping("/read/diary")
    List<Diary> readDiary(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) @Parameter(
                    description = "yyyy-MM-dd", example = "2000-04-20") LocalDate date) {
        return diaryService.readDiary(date);
    }
    
    @Operation(summary = "일기 수정하기", description = "해당 날짜 일기의 첫 번째를 수정하는 API")
    @PutMapping("/update/diary")
    void updateDiary(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) @Parameter(
                    description = "yyyy-MM-dd", example = "2000-04-20") LocalDate date,
            @RequestBody String text) {
        diaryService.updateDiary(date, text);
    }
}