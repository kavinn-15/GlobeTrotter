package com.example.loginapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * Main entry point.
 *
 * @ServletComponentScan tells Spring Boot to scan for classes annotated with
 * @WebServlet / @WebFilter / @WebListener (see servlet.VisitCounterServlet),
 * so that raw Java Servlets can live alongside Spring MVC controllers in the
 * same embedded servlet container (Tomcat).
 */
@SpringBootApplication
@ServletComponentScan
public class LoginRegisterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginRegisterApplication.class, args);
    }
}
