package fr.insee.compas;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class AppConfig {

    @Value("${fr.insee.compas.proxy.name:}")
    private String proxyName;

    @Value("${fr.insee.compas.proxy.port:}")
    private int proxyPort;

    @Value("${fr.insee.compas.proxy.non-proxy-hosts:}")
    private List<String> nonProxyHosts;

    @Bean
    public RestTemplate restTemplate() {

        SimpleClientHttpRequestFactory defaultFactory = new SimpleClientHttpRequestFactory();
        SimpleClientHttpRequestFactory proxyFactory = new SimpleClientHttpRequestFactory();
        proxyFactory.setProxy(
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyName, proxyPort)));

        return new RestTemplate(
                (uri, httpMethod) -> {
                    String host = uri.getHost();
                    boolean bypass =
                            nonProxyHosts.stream()
                                    .anyMatch(
                                            excluded ->
                                                    excluded.startsWith(".")
                                                            ? host.endsWith(excluded)
                                                            : host.equals(excluded));
                    return (bypass ? defaultFactory : proxyFactory).createRequest(uri, httpMethod);
                });
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorsFilter(source));
        registrationBean.setOrder(0);
        return registrationBean;
    }
}
