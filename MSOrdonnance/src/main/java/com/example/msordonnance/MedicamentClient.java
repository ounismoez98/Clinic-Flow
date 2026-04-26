package com.example.msordonnance;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name="MS-medicament-s")
public interface MedicamentClient {

    @RequestMapping("medicaments")
    public List<MedicamentDTO> getAll();

    @RequestMapping("medicaments/{id}")
    public MedicamentDTO getMedicamentById(@PathVariable int id);
}
