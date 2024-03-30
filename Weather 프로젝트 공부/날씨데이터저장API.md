# 날씨 일기 저장 API 구현
---

## MVC 패턴
DB와 Client 간 통신에는 다양한 패턴이 있고 여기선 MVC 패턴을 사용한다.

Client <- **DTO** -> Controller <- **DTO** -> Service <- **DTO** -> Repository <- **ENTITY** -> DB

---

## 1. OpenWeatherMap 에서 데이터 받아오기
### Controller 에서 요청 구현하기
```java
package zerobase.weather.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;

@RestController
public class DiaryController {
    private final DiaryService diaryService;
    
    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }
    
    @PostMapping("/create/diary")
    void createDiary(
            @RequestParam @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody String text) {
        diaryService.createDiary(date, text);
    }
}
```
### Service 에서 요청 확인하기
```java
package zerobase.weather.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DiaryService {
    @Value("${openweathermap.key}")
    private String apiKey;
    
    public void createDiary(LocalDate date, String text) {
        getWeatherString();
    }
    
    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2" +
                        ".5/weather?q=incheon&appid=" + apiKey;
        System.out.println(apiUrl);
        return "";
    }
}
```

---

## 2. 받아온 데이터 JSON 사용 가능하게 파싱하기
### Json을 다루기 위한 Dependency 추가하기
```gradle
// <Json>
    implementation 'com.googlecode.json-simple:json-simple:1.1.1'
// </>
```

### Service에서 Json 파싱 테스트해보기
```java
package zerobase.weather.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

@Service
public class DiaryService {
    @Value("${openweathermap.key}")
    private String apiKey;
    
    public void createDiary(LocalDate date, String text) {
        System.out.println(getWeatherString());
    }
    
    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2" +
                ".5/weather?q=incheon&appid=" + apiKey;
        
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection httpURLConnection =
                    (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            BufferedReader br;
            
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(
                        httpURLConnection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(
                        httpURLConnection.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            
            return response.toString();
        } catch (Exception e) {
            return "failed to get response";
        }
    }
}
```

### 받아온 Json을 자바에서 쓸 수 있도록 parse해주는 메서드를 만든다.
```java
private Map<String, Object> parseWeather(String jsonString) {
    // weather.main, weather.icon, main.temp 필요함
    JSONParser jsonParser = new JSONParser();
    JSONObject jsonObject;
    
    try {
        jsonObject = (JSONObject) jsonParser.parse(jsonString);
    } catch (ParseException e) {
        throw new RuntimeException(e);
    }
    Map<String, Object> map = new HashMap<>();
    
    JSONObject mainData = (JSONObject) jsonObject.get("main");
    map.put("temp", mainData.get("temp"));
    
    // Json의 weather가 []으로 감싸진 JsonArray 형식이고, 단 하나의 Object만 있다.
    JSONArray jsonArray = (JSONArray) jsonObject.get("weather");
    JSONObject weatherData = (JSONObject) jsonArray.getFirst();
    map.put("main", weatherData.get("main"));
    map.put("icon", weatherData.get("icon"));
    return map;
}
```
```java
public void createDiary(LocalDate date, String text) {
    // open weather map에서 날씨 데이터 가져오기
    String weatherString = getWeatherString();
    
    // 받아온 날씨 json 파싱하기
    Map<String, Object> parsedWeather = parseWeather(weatherString);
    
    // 파싱된 데이터 + 일기 값 우리 DB에 넣기
}
```

---

## 3. 우리 DB에 저장하기
### DB에 SQL을 입력하여 테이블을 생성한다.
```sql
create table diary(
    id int not null primary key auto_increment,
    weather varchar(50) not null,
    icon varchar(50) not null,
    temperature double not null,
    text varchar(500) not null,
    date date not null
);
```

### 동일한 DTO 객체를 생성한다.
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
}
```

### DB와 연결해줄 Repository를 만든다.
```java
package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Diary;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer>{
}
```

### Service 객체에 DiaryRepository를 불러오고, 수정한다.
```java
private final DiaryRepository diaryRepository;
    
public DiaryService(DiaryRepository diaryRepository) {
    this.diaryRepository = diaryRepository;
}

public void createDiary(LocalDate date, String text) {
    // open weather map에서 날씨 데이터 가져오기
    String weatherString = getWeatherString();
    
    // 받아온 날씨 json 파싱하기
    Map<String, Object> parsedWeather = parseWeather(weatherString);
    
    // 파싱된 데이터 + 일기 값 우리 DB에 넣기
    Diary diary = Diary.builder()
            .weather(parsedWeather.get("main").toString())
            .icon(parsedWeather.get("icon").toString())
            .temperature((Double) parsedWeather.get("temp"))
            .text(text)
            .date(date)
            .build();
    
    diaryRepository.save(diary);
}
```

---