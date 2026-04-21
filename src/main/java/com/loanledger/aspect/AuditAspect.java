package com.loanledger.aspect;

import com.loanledger.entity.AuditLog;
import com.loanledger.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(audit)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String action = audit.action().isEmpty() ? methodName : audit.action();
        String details = "Arguments: " + Arrays.toString(joinPoint.getArgs());
        String userId = getCurrentUserId();
        String clientIp = getClientIp();

        Object result;
        try {
            result = joinPoint.proceed();
            saveLog(userId, action, details, "SUCCESS", null, clientIp);
            return result;
        } catch (Exception e) {
            saveLog(userId, action, details, "FAILED", e.getMessage(), clientIp);
            throw e;
        }
    }

    private void saveLog(String userId, String action, String details, String status, String error, String clientIp) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .details(details)
                    .status(status)
                    .error(error)
                    .timestamp(LocalDateTime.now())
                    .clientIp(clientIp)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) auth.getPrincipal()).getUsername();
        }
        return (auth != null) ? auth.getName() : "SYSTEM";
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader == null) {
                return request.getRemoteAddr();
            }
            return xfHeader.split(",")[0];
        }
        return "UNKNOWN";
    }
}
