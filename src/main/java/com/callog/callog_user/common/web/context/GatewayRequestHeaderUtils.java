package com.callog.callog_user.common.web.context;

import com.callog.callog_user.common.exception.NotFound;
import org.springframework.security.core.parameters.P;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class GatewayRequestHeaderUtils {

    public static String getRequestHeaderParamAsString(String key) {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest().getHeader(key);
    }

    public static String getUsername() {
        return getRequestHeaderParamAsString("X-Auth-Username");
    }

    public static String getClientDevice() {
        return getRequestHeaderParamAsString("X-Client-Device");
    }

    public static String getClientAddress() {
        return getRequestHeaderParamAsString("X-Client-Address");
    }

    public static String getUsernameOrThrowException() {
        String username = getUsername();
        if(username == null) {
            throw new NotFound("헤더에 username정보가 없습니다.");
        }
        return username;
    }

    public static String getClientDeviceOrThrowException() {
        String clientDevice = getClientDevice();
        if (clientDevice == null) {
            throw new NotFound("헤더에 사용자 디바이스 정보가 없습니다.");
        }
        return clientDevice;
    }
    public static String getClientAddressOrThrowException() {
        String clientAddress = getClientAddress();
        if (clientAddress == null) {
            throw new NotFound("헤더에 사용자 IP 주소 정보가 없습니다.");
        }
        return clientAddress;
    }
}



