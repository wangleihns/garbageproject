package com.jin.env.garbage.filter;

import com.jin.env.garbage.dao.user.GarbageUserDao;
import com.jin.env.garbage.entity.user.GarbageUserEntity;
import com.jin.env.garbage.jwt.JwtUtil;
import com.jin.env.garbage.utils.CommonUtil;
import com.jin.env.garbage.utils.Constants;
import com.jin.env.garbage.utils.JSONUtil;
import com.jin.env.garbage.utils.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "tokenFilter",urlPatterns = "/api/*")
public class TokenFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GarbageUserDao garbageUserDao;

    @Autowired
    private RedisTemplate<String,String>  redisTemplate;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        logger.info(request.getRequestURI());
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers","Authorization, Content-Type");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setCharacterEncoding("utf-8");
        String url = request.getRequestURI();
        ResponseData responseData = new ResponseData();
        if (url.endsWith("/api/v1/user/login")) {
            filterChain.doFilter(request, response);
        } else {
            String header = request.getHeader("Authorization");
//            header = request.getParameter("Authorization");

            if (header == null || !header.startsWith("Bearer")) {
                responseData.setStatus(Constants.tokenStatus.TOKEN_NOT_EXIST.getStatus());
                responseData.setMsg("请在请求头中添加token ,或者token不是以Bearer开头");
                response.getWriter().write(JSONUtil.obj2json(responseData));
                return;
            }
            String jwt = header.split(" ")[1];
            Integer sub = null;
            try {
                sub = jwtUtil.getSubject(jwt);
            } catch (Exception e) {
                e.printStackTrace();
                responseData.setStatus(Constants.tokenStatus.TokenExp.getStatus());
                responseData.setMsg("token had already expired");
                response.getWriter().write(JSONUtil.obj2json(responseData));
                return;
            }
            GarbageUserEntity userEntity = garbageUserDao.findById(sub).get();
            String accessToken =  redisTemplate.opsForValue().get("accessToken:" + userEntity.getId());

//            if (!checkSignVail(request)){
//                //签名不对返回
//                responseData.setStatus(Constants.tokenStatus.SignNotRight.getStatus());
//                responseData.setMsg("sign is wrong");
//                response.getWriter().write(JSONUtil.obj2json(responseData));
//                return;
//            }
            if (StringUtils.isEmpty(accessToken)){
                responseData.setStatus(Constants.tokenStatus.TokenExp.getStatus());
                responseData.setMsg("token had already expired");
                response.getWriter().write(JSONUtil.obj2json(responseData));
            }

            if (accessToken!=null && !accessToken.equals(jwt)){
                responseData.setStatus(Constants.tokenStatus.TokenChange.getStatus());
                responseData.setMsg("用户异地登录");
                response.getWriter().write(JSONUtil.obj2json(responseData));
                return;
            }
            if (CommonUtil.checkTokenExp(request)) {
                //token 过期返回
                responseData.setStatus(Constants.tokenStatus.TokenExp.getStatus());
                responseData.setMsg("token had already expired");
                response.getWriter().write(JSONUtil.obj2json(responseData));
                return;
            }
            logger.info("接口签名与token 均符合系统要求，允许访问接口  ");
            filterChain.doFilter(request,response);
        }
//        String token = request.getHeader("Authorization");
//        logger.info(token);
    }

    @Override
    public void destroy() {

    }
}
