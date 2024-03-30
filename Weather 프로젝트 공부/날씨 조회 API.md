# 날씨 조회 API 작성
Controller 객체에서 Client의 Get과 필요한 파라미터들을 매핑해주고, Service 객체를 불러와서 필요한 메서드를 호출시켜준다.

---

## 1. Controller에 매핑해주기
```java
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
```

---

## 2. Service 에 메서드 만들기
```java
public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
    return diaryRepository.findAllByDateBetween(startDate, endDate);
}

public List<Diary> readDiary(LocalDate date) {
    return diaryRepository.findAllByDate(date);
}
```

---

## 3. Repository 에 쿼리 메서드 만들기
- 메서드 이름만 규격에 맞게 지어주면 Jpa가 쿼리를 대신 생성해준다.
```java
package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Diary;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer>{
    List<Diary> findAllByDate(LocalDate date);
    
    List<Diary> findAllByDateBetween(LocalDate startDate, LocalDate endDate);
}
```

---

## 4. 요청해보기
```http
### create
POST http://localhost:8080/create/diary?date=2024-03-26
Content-Type: application/json

GOOD DAY

### get
GET http://localhost:8080/read/diary?date=2024-03-27
Accept: application/json

### get between
GET http://localhost:8080/read/diaries?startDate=2024-02-27&endDate=2024-01-27
Accept: application/json
```

---