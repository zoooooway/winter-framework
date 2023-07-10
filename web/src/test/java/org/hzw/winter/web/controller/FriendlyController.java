package org.hzw.winter.web.controller;

import org.hzw.winter.web.mvc.annotation.*;

import java.util.Date;

/**
 * @author hzw
 */
@Controller
@RequestMapping("/friendly")
public class FriendlyController {


    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "sir") String name) {
        return "greeting! " + name;
    }

    @GetMapping("/farewell")
    @ResponseBody
    public String farewell(@RequestParam(value = "name", defaultValue = "sir") String name) {
        return "goodbye! " + name;
    }

    @GetMapping("/time")
    public Date time() {
        return new Date();
    }
}
