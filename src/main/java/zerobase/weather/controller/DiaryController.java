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
    
    @Operation(summary = "일기만들기", description = "일기를 만드는 API")
    @PostMapping("/create/diary")
    void createDiary(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) @Parameter(
                    description = "yyyy-MM-dd", example = "2000-04-20") LocalDate date,
            @RequestBody String text) {
        
        diaryService.createDiary(date, text);
    }
    
    @DeleteMapping("/delete/diary")
    void deleteDiary(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) LocalDate date) {
        diaryService.deleteDiary(date);
    }
    
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return diaryService.readDiaries(startDate, endDate);
    }
    
    @GetMapping("/read/diary")
    List<Diary> readDiary(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) LocalDate date) {
        return diaryService.readDiary(date);
    }
    
    @PutMapping("/update/diary")
    void updateDiary(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody String text) {
        diaryService.updateDiary(date, text);
    }
}