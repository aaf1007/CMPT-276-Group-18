package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.entity.MpptController;
import com.group18.xantrex_calculator.model.CalculatorResult;
import com.group18.xantrex_calculator.service.CalculatorService;
import com.group18.xantrex_calculator.service.SolarPanelsService;
import com.group18.xantrex_calculator.service.WeatherService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;

@Controller
public class CalculatorController {

    private final CalculatorService calculatorService;
    private final SolarPanelsService solarPanelsService;
    private final WeatherService weatherService;

    public CalculatorController(CalculatorService calculatorService, SolarPanelsService solarPanelsService, WeatherService weatherService) {
        this.calculatorService = calculatorService;
        this.solarPanelsService = solarPanelsService;
        this.weatherService = weatherService;
    }

    @GetMapping({ "/", "/calculator" })
    public String calculator(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("panels", solarPanelsService.getAllPanels());
        return "userdashboard";
    }

    @PostMapping("/calculator")
    public String calculate(
            @RequestParam Double pmax,
            @RequestParam Double voc,
            @RequestParam Double isc,
            @RequestParam Integer series,
            @RequestParam Integer parallel,
            @RequestParam Integer battV,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Double manualTempCelsius,
            @RequestParam(required = false) String manualTemp,
            @RequestParam(required = false) String tempUnit,
            Model model) {

        model.addAttribute("pmax", pmax);
        model.addAttribute("voc", voc);
        model.addAttribute("isc", isc);
        model.addAttribute("series", series);
        model.addAttribute("parallel", parallel);
        model.addAttribute("battV", battV);
        model.addAttribute("city", city);
        model.addAttribute("country", country);
        model.addAttribute("region", region);
        model.addAttribute("manualTempCelsius", manualTempCelsius);
        model.addAttribute("manualTemp", manualTemp);
        model.addAttribute("tempUnit", tempUnit);

        // Validate required parameters
        if (pmax == null || pmax <= 0 || voc == null || voc <= 0 || isc == null || isc <= 0 ||
            series == null || series <= 0 || parallel == null || parallel <= 0 || battV == null) {
            model.addAttribute("error", "All numeric fields must have valid positive values.");
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("panels", solarPanelsService.getAllPanels());
            return "userdashboard";
        }

        // Use manual temp if provided, otherwise fetch from weather API
        double minTemp;
        if (manualTempCelsius != null) {
            minTemp = manualTempCelsius;
        } else {
            if (WeatherService.requiresRegion(country) && (region == null || region.trim().isEmpty())) {
                Authentication regionAuth = SecurityContextHolder.getContext().getAuthentication();
                boolean regionLoggedIn = regionAuth != null && regionAuth.isAuthenticated()
                        && !(regionAuth instanceof AnonymousAuthenticationToken);
                model.addAttribute("error", "State/Province/Region is required for United States and Canada locations.");
                model.addAttribute("isLoggedIn", regionLoggedIn);
                model.addAttribute("panels", solarPanelsService.getAllPanels());
                return "userdashboard";
            }
            minTemp = weatherService.getMinTemperature(city, country, region);
        }
        double tempFactor = calculateTemperatureFactor(minTemp);

        model.addAttribute("panels", solarPanelsService.getAllPanels());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("isLoggedIn", isLoggedIn);
        CalculatorResult result = calculatorService.calculate(pmax, voc, isc, series, parallel, battV, tempFactor);
        List<MpptController> compatibleControllers = calculatorService.findAllCompatibleControllers(result, String.valueOf(battV));
        model.addAttribute("result", result);
        model.addAttribute("compatibleControllers", compatibleControllers);
        model.addAttribute("recommendedController", compatibleControllers.isEmpty() ? null : compatibleControllers.get(0));
        model.addAttribute("minTemp", minTemp);
        model.addAttribute("tempFactor", tempFactor);
        return "userdashboard";
    }

    private double calculateTemperatureFactor(double minTemp) {
        if (minTemp < -10) return 1.3;
        else if (minTemp < 0) return 1.25;
        else return 1.2;
    }
}
