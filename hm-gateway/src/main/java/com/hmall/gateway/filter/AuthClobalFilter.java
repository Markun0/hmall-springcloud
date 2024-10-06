package com.hmall.gateway.filter;

import cn.hutool.core.exceptions.ValidateException;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Component
@EnableConfigurationProperties(AuthProperties.class)
public class AuthClobalFilter implements GlobalFilter , Ordered {

    private final JwtTool jwtTool;

    private final AuthProperties authProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 2. 判断是否需要进行拦截
        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange);
        }
        // 3. 获取token
        String token = null;
        List<String> headeres = request.getHeaders().get("authorization");
        if(headeres != null && headeres.size() > 0){
            token = headeres.get(0);
        }
        // 4. 校验并解析token
        Long userId = null;
        try{
            userId = jwtTool.parseToken(token);
        }catch (UnauthorizedException e){
            // 拦截，设置响应状态码
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 5. 传递用户信息
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        // 6. 放行
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        for(String excludePath : authProperties.getExcludePaths()){
            if(antPathMatcher.match(excludePath, path)){
                return true;
            }
        }
        return false;
    }
}
