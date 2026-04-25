package com.example.mscandidat;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name="MS-job-s")
public interface JobClient {

    @RequestMapping("jobs")
    public List<JobDTO>  getAll();
    @RequestMapping("jobs/{id}")
    public JobDTO getJobById(@PathVariable int id);

}
