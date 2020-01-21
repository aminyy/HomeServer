package net.casnw.home.run;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.casnw.home.runtime.Runtime;


@RestController
@EnableAutoConfiguration

@RequestMapping("/redis")
public class RedisRest {
	@Resource
	RedisUtil redisUtil;

	@RequestMapping(value = "/set", method = { RequestMethod.GET })
	public String set(@RequestParam String value) {
		String aa="E:\\workspace\\run\\prms4.hom";
        Runtime rt;
        
        try {
            System.out.println("propertiesFile==" + value);
            rt = new Runtime(aa);
            rt.setRedisUtil(redisUtil);
            rt.loadModel();
            rt.runModel();
            rt.clearModel();
        } catch (Exception ex) {
            System.out.printf("main error:" + ex.getLocalizedMessage());
        }
        
		
		redisUtil.set("testredis", value);

		return "0000";
	}

	@RequestMapping(value = "/get", method = { RequestMethod.GET })
	public String get() {
		String value = (String) redisUtil.get("testredis");

		return value;
	}
}
