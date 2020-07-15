package org.opengroup.osdu.crs.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Home {
    @RequestMapping(value = {"/", "/swagger"})
    public String swagger() {
        return "redirect:swagger-ui.html";
    }
}