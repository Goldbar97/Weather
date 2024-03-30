# 날씨 일기 수정 API
날짜를 기준으로 찾고, 여러 개의 일기 중 첫번 째 일기를 수정한다.

---

## 1. Controller에 Put Mapping하기
```java
@PutMapping("/update/diary")
void updateDiary(
        @RequestParam @DateTimeFormat(iso =
                DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestBody String text) {
    diaryService.updateDiary(date, text);
}
```

---

## 2. Service에 메서드 만들기
- Repository의 save 메서드는 저장뿐만 아니라 덮어쓰기 기능도 수행한다.
```java
public void updateDiary(LocalDate date, String text) {
    Diary diary = diaryRepository.getFirstByDate(date);
    diary.setText(text);
    diaryRepository.save(diary);
}
```

---

## 3. JPA Repository에 쿼리 메서드 만들기
```java
Diary getFirstByDate(LocalDate date);
```

---