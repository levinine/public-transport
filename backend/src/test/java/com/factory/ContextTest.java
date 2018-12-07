package com.factory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApplication.class)
public class ContextTest {

    @Test
    public void testApplictionContextStarts() {
        System.out.println("Application context started");
    }
}
