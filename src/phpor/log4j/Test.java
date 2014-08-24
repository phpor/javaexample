package phpor.log4j;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Created by phpor on 15/8/20.
 */
public class Test {
    public   static   void  main(String[] args)  {
//        PropertyConfigurator.configure("/Users/phpor/Workspace/github.com/phpor/java/javaexample/conf/log4j2.xml");
        Configurator.initialize(Test.class.getName(), "/Users/phpor/Workspace/github.com/phpor/java/javaexample/conf/log4j2.xml");
        Logger logger = LogManager.getLogger();
        logger.error("hello");
//        System.out.print("hello");
    }
}
