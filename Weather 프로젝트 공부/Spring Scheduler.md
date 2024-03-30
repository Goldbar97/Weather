# Spring Scheduler
https://api.openweathermap.org/data/2.5/weather 는 현재 날씨를 조회하는 api를 제공하고 있고, 5일 이상의 과거 날씨를 조회하는 것은 유료이다.

이를 Scheduling을 사용해서 절약해보려 한다.

## Scheduling 사용하기
- `date_weather` 테이블을 생성하고 여기에 저장하려 한다.
```sql
create table date_weather (
    date Date not null primary key,
    weather VARCHAR(50) not null,
    icon VARCHAR(50) not null,
    temperature DOUBLE not null
);
```

- DateWeather DTO 생성하기

```java
package zerobase.weather.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Entity(name = "date_weather")
@Getter
@NoArgsConstructor
@Setter
public class DateWeather {
    @Id
    private LocalDate date;
    
    private String weather;
    private String icon;
    private double temperature;
}
```

- DateWeatherRepository 생성하기
```java
package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.weather.domain.DateWeather;

import java.time.LocalDate;
import java.util.List;

public interface DateWeatherRepository extends JpaRepository<DateWeather,
        LocalDate> {
    List<DateWeather> findALlByDate(LocalDate date);
}
```

- DiaryService에 메서드 생성하기
```java
@Service
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    
    public DiaryService(
            DiaryRepository diaryRepository,
            DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherFromApi());
    }

    private DateWeather getWeatherFromApi() {
        String weatherString = getWeatherString();
        
        Map<String, Object> parsedWeather = parseWeather(weatherString);
        
        return DateWeather.builder()
                .date(LocalDate.now())
                .weather(parsedWeather.get("main").toString())
                .icon(parsedWeather.get("icon").toString())
                .temperature((Double) parsedWeather.get("temp"))
                .build();
    }
}
```

## Service의 createDiary 리팩토링 하기

이제 매일 날씨를 저장하므로 일기를 생성할 때마다 매번 API를 호출해서 날씨 정보를 받아올 필요가 없어졌다. 따라서 DB 에 저장된 날씨를 가져오기로 한다.

```java
@Transactional
public void createDiary(LocalDate date, String text) {
    // 날씨 데이터 가져오기 (DB 에 있으면 DB 에서, 없으면 API 호출해서)
    DateWeather dateWeather = getDateWeather(date);
    
    // 일기 값 우리 DB에 넣기
    Diary diary = new Diary();
    diary.setDateWeather(dateWeather);
    diary.setText(text);
    
    diaryRepository.save(diary);
}

private DateWeather getDateWeather(LocalDate date) {
    List<DateWeather> weatherList = dateWeatherRepository.findALlByDate(date);
    if (weatherList.isEmpty()) {
        // 새로 api에서 날씨 정보를 가져온다.
        // 정책상 현재 날씨를 가져오도록 하거나 날씨없이 일기를 쓴다.
        return getWeatherFromApi();
    } else {
        return weatherList.getFirst();
    }
}
```

`DateWeather` 로부터 `Diary`로 변환하는 메서드를 만든다.

```java
package zerobase.weather.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Entity
@Getter
@NoArgsConstructor
@Setter
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String weather;
    private String icon;
    private double temperature;
    private String text;
    private LocalDate date;
    
    public void setDateWeather(DateWeather dateWeather) {
        date = dateWeather.getDate();
        weather = dateWeather.getWeather();
        icon = dateWeather.getIcon();
        temperature = dateWeather.getTemperature();
    }
}
```

### 결과
이렇게 했더니 Diary를 만들때마다 불필요하게 API를 호출하는 부분을 없앴고, 5일 이전의 날씨를 호출하면 과금되는 API를 피하고 DB 에 과거의 날씨를 저장해서 요금도 아낄 수 있게 됐다.