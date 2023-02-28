package my.example;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

/**
 * @author Alexandr Kuznetsov (alexandr@power.lv)
 */
@SpringBootApplication
        //(exclude = {ErrorMvcAutoConfiguration.class})
public class ApplicationMain {


    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApplicationMain.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }


}
