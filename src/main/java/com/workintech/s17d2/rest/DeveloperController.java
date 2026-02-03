package com.workintech.s17d2.rest;

import com.workintech.s17d2.model.*;
import com.workintech.s17d2.tax.Taxable;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/developers")
public class DeveloperController {

    // MainTest direct access ediyor: assertNotNull(controller.developers)
    public Map<Integer, Developer> developers;

    private final Taxable taxable;

    // Constructor Injection (Test @MockBean Taxable ile burayı doldurur)
    public DeveloperController(Taxable taxable) {
        this.taxable = taxable;
    }

    // Test @PostConstruct sonrası map null olmamalı diyor
    @PostConstruct
    public void init() {
        developers = new HashMap<>();
    }

    // GET /developers
    @GetMapping
    public List<Developer> getAll() {
        return new ArrayList<>(developers.values());
    }

    // GET /developers/{id}
    @GetMapping("/{id}")
    public Developer getById(@PathVariable Integer id) {
        return developers.get(id);
    }

    // POST /developers  → Test 201 CREATED bekliyor
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Developer create(@RequestBody Developer request) {

        Developer dev;

        // Taxable 15 / 25 / 35 döndüğü için /100 şart
        if (request.getExperience() == Experience.JUNIOR) {
            double netSalary =
                    request.getSalary() * (1 - taxable.getSimpleTaxRate() / 100);
            dev = new JuniorDeveloper(request.getId(), request.getName(), netSalary);

        } else if (request.getExperience() == Experience.MID) {
            double netSalary =
                    request.getSalary() * (1 - taxable.getMiddleTaxRate() / 100);
            dev = new MidDeveloper(request.getId(), request.getName(), netSalary);

        } else {
            double netSalary =
                    request.getSalary() * (1 - taxable.getUpperTaxRate() / 100);
            dev = new SeniorDeveloper(request.getId(), request.getName(), netSalary);
        }

        developers.put(dev.getId(), dev);
        return dev;
    }

    // PUT /developers/{id}
    @PutMapping("/{id}")
    public Developer update(@PathVariable Integer id,
                            @RequestBody Developer updated) {
        developers.put(id, updated);
        return updated;
    }

    // DELETE /developers/{id}
    @DeleteMapping("/{id}")
    public Developer delete(@PathVariable Integer id) {
        return developers.remove(id);
    }
}