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


    @PostMapping("/visit")
    @ResponseBody
    public String help(@RequestBody Person person) {
        return String.format("welcome! %s", person);
    }

    public static class Person {
        public String name;
        public Integer age;
        public String gender;

        public Person() {
        }

        public Person(String name, Integer age, String gender) {
            this.name = name;
            this.age = age;
            this.gender = gender;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", gender=" + gender +
                    '}';
        }
    }
}

