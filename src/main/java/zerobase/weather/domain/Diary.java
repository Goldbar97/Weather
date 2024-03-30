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