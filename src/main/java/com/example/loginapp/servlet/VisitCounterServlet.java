package com.example.loginapp.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A plain Java Servlet (javax/jakarta Servlet API), registered directly with
 * the embedded Tomcat container via @WebServlet, running side by side with
 * the Spring MVC DispatcherServlet. Visit it at GET /visits.
 *
 * This demonstrates using the raw Servlet API in the same project as
 * Spring MVC, as requested.
 */
@WebServlet(name = "VisitCounterServlet", urlPatterns = "/visits")
public class VisitCounterServlet extends HttpServlet {

    private final AtomicInteger totalVisits = new AtomicInteger(0);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int count = totalVisits.incrementAndGet();
        resp.setContentType("text/plain");
        resp.getWriter().write("This endpoint has been hit " + count + " time(s) since the app started.\n"
                + "(Served by a raw java.servlet.http.HttpServlet, not Spring MVC.)");
    }
}
