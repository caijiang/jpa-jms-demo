/*
 * 版权所有:杭州火图科技有限公司
 * 地址:浙江省杭州市滨江区西兴街道阡陌路智慧E谷B幢4楼
 *
 * (c) Copyright Hangzhou Hot Technology Co., Ltd.
 * Floor 4,Block B,Wisdom E Valley,Qianmo Road,Binjiang District
 * 2013-2017. All rights reserved.
 *
 */

package me.jiangcai.demo.jms.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.List;
import java.util.Set;

@Configuration
@EnableTransactionManagement(mode = AdviceMode.PROXY)
@EnableAspectJAutoProxy
@ComponentScan({
        "me.jiangcai.demo.jms.controller"
})
@EnableJpaRepositories("me.jiangcai.demo.jms.repository")
@EnableWebMvc
@ImportResource("classpath:/datasource_runtime.xml")
@Import({MVCConfig.MVCConfigLoader.class})
public class MVCConfig extends WebMvcConfigurerAdapter {
    private static final String UTF8 = "UTF-8";
    private static final String PAGE_PARAMETER_NAME = "pageNumber";
    private static final String SIZE_PARAMETER_NAME = "pageSize";
    private static String[] STATIC_RESOURCE_PATHS = new String[]{
            "platform/css", "platform/images", "platform/js", "platform/weui", "assets", "_resources", "portal/css", "portal/js", "portal/images",
    };
    private final ThymeleafViewResolver htmlViewResolver;
    private final Environment environment;
    @Autowired
    public MVCConfig(ThymeleafViewResolver htmlViewResolver, Environment environment) {
        this.htmlViewResolver = htmlViewResolver;
        this.environment = environment;
    }


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        PageableHandlerMethodArgumentResolver resolver =
                new PageableHandlerMethodArgumentResolver(new SortHandlerMethodArgumentResolver()) {
                    @Override
                    public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        Pageable result = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
                        if (result == null)
                            return null;
                        return new PageRequest(result.getPageNumber() - 1, result.getPageSize(), result.getSort());
                    }
                };
        resolver.setFallbackPageable(new PageRequest(1, 10));
        resolver.setPageParameterName(PAGE_PARAMETER_NAME);
        resolver.setSizeParameterName(SIZE_PARAMETER_NAME);
        argumentResolvers.add(resolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        for (String path : STATIC_RESOURCE_PATHS) {
            registry.addResourceHandler("/" + path + "/**").addResourceLocations("/" + path + "/");
        }
    }

//    private ViewResolver redirectViewResolver() {
//        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
//        resolver.setViewNames(ArrayUtil.array("redirect:*"));
//        return resolver;
//    }
//
//    private ViewResolver forwardViewResolver() {
//        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
//        resolver.setViewNames(ArrayUtil.array("forward:*"));
//        return resolver;
//    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(htmlViewResolver);
//        registry.viewResolver(redirectViewResolver());
//        registry.viewResolver(forwardViewResolver());

    }

    String[] staticResourceAntPatterns() {
        String[] ignoring;
        int startIndex = 0;
        if (environment.acceptsProfiles("development")) {
            ignoring = new String[MVCConfig.STATIC_RESOURCE_PATHS.length + 2];
            ignoring[startIndex++] = "/**/*.html";
            ignoring[startIndex++] = "/mock/**/*";
        } else {
            ignoring = new String[MVCConfig.STATIC_RESOURCE_PATHS.length];
        }
        for (String path : MVCConfig.STATIC_RESOURCE_PATHS) {
            ignoring[startIndex++] = "/" + path + "/**/*";
        }
        return ignoring;
    }

    @Import(MVCConfigLoader.EngineLoader.class)
    static class MVCConfigLoader {

        private final SpringTemplateEngine htmlViewTemplateEngine;

        @Autowired
        public MVCConfigLoader(SpringTemplateEngine htmlViewTemplateEngine) {
            this.htmlViewTemplateEngine = htmlViewTemplateEngine;
        }

        @Autowired
        public void setTemplateEngineSet(Set<SpringTemplateEngine> templateEngineSet) {
            // 所有都增加安全方言
            templateEngineSet.forEach(engine -> engine.addDialect(new SpringSecurityDialect()));
        }

        @Bean
        public ThymeleafViewResolver htmlViewResolver() {
            ThymeleafViewResolver resolver = new ThymeleafViewResolver();
            resolver.setTemplateEngine(htmlViewTemplateEngine);
            resolver.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
            resolver.setCharacterEncoding(UTF8);
            resolver.setCache(false);
            resolver.setViewNames(new String[]{"*.html"});
            return resolver;
        }

        static class EngineLoader {
            private final ApplicationContext applicationContext;
            private final Environment environment;

            @Autowired
            public EngineLoader(ApplicationContext applicationContext, Environment environment) {
                this.applicationContext = applicationContext;
                this.environment = environment;
            }

            SpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
                SpringTemplateEngine engine = new SpringTemplateEngine();
                engine.setTemplateResolver(templateResolver);
                engine.addDialect(new Java8TimeDialect());
                engine.addDialect(new SpringSecurityDialect());
                return engine;
            }

            private ITemplateResolver htmlTemplateResolver() {
                SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
                resolver.setCacheable(!environment.acceptsProfiles("development")
                        && !environment.acceptsProfiles("test"));
                resolver.setCharacterEncoding(UTF8);
                resolver.setApplicationContext(applicationContext);
                resolver.setTemplateMode(TemplateMode.HTML);
                return resolver;
            }

            @Bean
            public SpringTemplateEngine htmlViewTemplateEngine() {
                return templateEngine(htmlTemplateResolver());
            }

        }
    }

}
