package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDate;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    @Value("${openweathermap.key}")
    private String apiKey;
    private static final Logger LOGGER = LoggerFactory.getLogger(
            WeatherApplication.class);
    public DiaryService(
            DiaryRepository diaryRepository,
            DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        LOGGER.info("started to create diary");
        
        // 날씨 데이터 가져오기 (DB 에 있으면 DB 에서, 없으면 API 호출해서)
        DateWeather dateWeather = getDateWeather(date);
        
        // 일기 값 우리 DB에 넣기
        Diary diary = new Diary();
        diary.setDateWeather(dateWeather);
        diary.setText(text);
        
        diaryRepository.save(diary);
        LOGGER.info("end to create diary");
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
    
    @Transactional
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
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
        
        JSONArray jsonArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) jsonArray.getFirst();
        map.put("main", weatherData.get("main"));
        map.put("icon", weatherData.get("icon"));
        return map;
    }
    
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        if (date.isAfter(LocalDate.ofYearDay(3050, 1))) {
            throw new InvalidDate();
        }
        LOGGER.debug("read diary");
        return diaryRepository.findAllByDate(date);
    }
    
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherFromApi());
    }
    
    public void updateDiary(LocalDate date, String text) {
        Diary diary = diaryRepository.getFirstByDate(date);
        diary.setText(text);
        diaryRepository.save(diary);
    }
}
