package org.pg6100.quizImp;


import io.swagger.jaxrs.config.BeanConfig;
import org.pg6100.quizImp.api.QuizRestImpl;
import org.pg6100.quizImp.api.RootCategoryRestImpl;
import org.pg6100.quizImp.api.SubCategoryRestImpl;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


//this defines the entry point of REST definitions. Can be only one.
@ApplicationPath("/api")
public class ApplicationConfig extends Application {


  private final Set<Class<?>> classes;


  public ApplicationConfig() {

    /*
      We use SWAGGER to create automatically create documentation
      for the REST service.
      This documentation will be served as a swagger.json file by
      the REST service itself.
      The web page under "webapp" are copied&pasted from the "dist"
      folder in
      https://github.com/swagger-api/swagger-ui
     */
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1.0");
    beanConfig.setSchemes(new String[]{"http"});
    beanConfig.setHost("localhost:8080");
    beanConfig.setBasePath("/quiz/api");
    beanConfig.setResourcePackage("org.pg6100.quizApi");

    //AWFUL NAME: this "set" is the one does actually init Swagger...
    beanConfig.setScan(true);

    /*
      Here we define which classes provide REST APIs
     */
    HashSet<Class<?>> c = new HashSet<>();
    c.add(QuizRestImpl.class);
    c.add(RootCategoryRestImpl.class);
    c.add(SubCategoryRestImpl.class);

    //add further configuration to activate SWAGGER
    c.add(io.swagger.jaxrs.listing.ApiListingResource.class);
    c.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

    //needed to handle Java 8 dates
    c.add(ObjectMapperContextResolver.class);

    classes = Collections.unmodifiableSet(c);
  }

  @Override
  public Set<Class<?>> getClasses() {
    return classes;
  }

}