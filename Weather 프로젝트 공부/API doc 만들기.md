# API DOC
개발한 API 의 구조나 사용법을 보여주는 문서 작성은 필수적이다.

API 문서가 있어야 다른 사람들이 사용하기 쉽고 이해 할 수 있다.

API 문서 작성도 표준이 있다. (OpenAPI Specification)

API 문서 작성을 돕는 다양한 도구들이 있다.

- Swagger
- ReDoc
- GitBook

이 프로젝트에서는 Swagger 를 사용한다.

## Swagger
표준 API 문서를 따르고 문서화에 있어서 호환성이 좋고 다양한 편의를 제공한다.

설계한 API 를 테스트하는 도구도 제공하며 널리 사용되므로 방대한 커뮤니티와 지원이 존재한다.

~~`build.gradle` 에 Swagger 의존성을 추가한다.~~
```gradle
// <Swagger>
implementation 'io.springfox:springfox-boot-starter:3.0.0'
implementation 'io.springfox:springfox-swagger-ui:3.0.0'
// </>
```

~~`application.properties` 에 다음을 추가한다.~~

~~스프링 MVC 패턴과 Swagger 의 패턴을 맞춰줘서 Swagger 가 정상 동작하도록 한다.~~
```properties
spring.mvc.pathmatch.matching-strategy=ant_path_matcher
```

~~그리고 config 패키지에 `SwaggerConfig` 클래스를 만들어 Swagger 의 설정값을 지정한다.~~
```java
package zerobase.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build().apiInfo(apiInfo());
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("title")
                .description("description")
                .version("1.0")
                .build();
    }
}
```
### Springfox to Springdoc
그러나 스프링 애플리케이션을 실행하면 오류가 발생한다.

```
java.lang.TypeNotPresentException: Type javax.servlet.http.HttpServletRequest not present
```

이것은 최근 스프링이 `javax` 에서 이름이 바뀐 `jakarta`를 사용하는데 Springfox는 여전히 `javax`를 사용하기 때문이다.

Springfox 는 2020년 부터 방치되어온 라이브러리이고, 그에 비해 Springdoc 은 꾸준한 업데이트와 더 많은 기능을 제공한다.

[공식 문서](https://springdoc.org/)를 참고하여 Springdoc 을 사용한다.

`build.gradle`에 다음과 같이 추가한다.
```gradle
// <Swagger>
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0'
// </>
```

이 `springdoc-openapi`는 바로 `Swagger UI`를 받아온다.

스프링 애플리케이션을 실행하고 웹브라우저 주소에 `http://localhost:8080/swagger-ui/index.html` 를 입력하면 따로 설정해주지 않아도 `diary-controller` 에서 매핑한 API 들이 표시된다.

하나씩 살펴보면 호출할 때 필요한 인자값과 응답 예시를 볼 수 있고 API 문서임에도 테스트를 해볼 수도 있다.

## Swagger DOC 꾸미기
Configuration Class 를 만들어서 Swagger 문서를 꾸밀 수 있다.

config 패키지에 `SwaggerConfig` 를 만들고 다음과 같이 작성한다.

```java
package zerobase.weather.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Weather API")
                              .description("Zerobase Weather API Test")
                              .version("v0.0.1")
                              .contact(new Contact().email("example@google.com")
                                               .url("https://www.google.com"))
                );
    }
}
```

위 코드와 같이 제목과 설명, 버전을 수정할 수 있다.

각 Controller 의 API 에 설명을 추가할 수도 있다.

```java
@Operation(summary = "일기만들기", description = "일기를 만드는 API")
@PostMapping("/create/diary")
void createDiary(
        @RequestParam @DateTimeFormat(iso =
                DateTimeFormat.ISO.DATE) @Parameter(
                description = "yyyy-MM-dd", example = "2000-04-20") LocalDate date,
        @RequestBody String text) {
    
    diaryService.createDiary(date, text);
}
```

`@Operation` 에 summary 와 description을 추가해서 한 눈에 무슨 API 인지 알리고, `@Parameter` 에 인자의 서식과 예시를 추가해 줘서 사용하는데 도움을 줄 수 있다.