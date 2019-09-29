package com.zzm;

import com.zzm.dao.BlogMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;

/**
 * Hello world!
 *
 */
public class MbatisSpringTest
{
    public static void main( String[] args )
    {
       /* ApplicationContext ctx = new ClassPathXmlApplicationContext("application.xml");
        BlogMapper blogMapper = ctx.getBean(BlogMapper.class);
//        blogMapper.selectBlog(1);

        blogMapper.containsKey("ttt");*/

        HashMap<String,String> map = new HashMap();
        System.out.println(map.put("tt","ww"));
        String re = map.put(null,null);
        System.out.println(re);
        System.out.println(map.put(null,null));

    }
}
