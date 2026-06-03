package com.example.mscandidat;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "MSOrdonnance")
public interface OrdonnanceClient {

    @GetMapping("/ordonnances")
    List<OrdonnanceDTO> getAll();

    @GetMapping("/ordonnances/{id}")
    OrdonnanceDTO getById(@PathVariable("id") int id);
}
