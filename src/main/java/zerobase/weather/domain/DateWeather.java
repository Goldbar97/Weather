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