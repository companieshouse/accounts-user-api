package uk.gov.companieshouse.accounts.user.controller;

import static uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil.APPLICATION_NAMESPACE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.exceptions.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.accounts.user.exceptions.NotFoundRuntimeException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.service.rest.err.Err;
import uk.gov.companieshouse.service.rest.err.Errors;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    public static final String X_REQUEST_ID = "X-Request-Id";
    public static final String ACCOUNTS_USER_API = "accounts_user_api";
    public static final String QUERY_PARAMETERS = "query-parameters";

    private String getJsonStringFromErrors(String requestId, Errors errors) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(errors);
        }
        catch (IOException e) {
            LOG.errorContext(requestId, String.format("Fail to parse Errors object to JSON %s", e.getMessage()), e, null);
            return "";
        }
    }

    @ExceptionHandler(NotFoundRuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Errors onNotFoundRuntimeException(NotFoundRuntimeException e, HttpServletRequest r) {
        String requestId = r.getHeader(X_REQUEST_ID);

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("url", r.getRequestURL().toString());
        contextMap.put(QUERY_PARAMETERS, r.getQueryString() != null ? "?" + r.getQueryString() : "");

        LOG.errorContext(requestId, e.getMessage(), null, contextMap);

        Errors errors = new Errors();
        errors.addError(Err.invalidBodyBuilderWithLocation(e.getFieldLocation()).withError(e.getMessage()).build());
        return errors;
    }

    @ExceptionHandler(BadRequestRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Errors onBadRequestRuntimeException(BadRequestRuntimeException e, HttpServletRequest request) {
        String requestId = request.getHeader(X_REQUEST_ID);

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("url", request.getRequestURL().toString());
        contextMap.put(QUERY_PARAMETERS, request.getQueryString() != null ? "?" + request.getQueryString() : "");

        LOG.errorContext(requestId, e.getMessage(), null, contextMap);

        Errors errors = new Errors();
        errors.addError(Err.invalidBodyBuilderWithLocation(ACCOUNTS_USER_API).withError(e.getMessage()).build());
        return errors;
    }

    @ExceptionHandler(InternalServerErrorRuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Errors onInternalServerErrorRuntimeException(InternalServerErrorRuntimeException e, HttpServletRequest request) {
        String requestId = request.getHeader(X_REQUEST_ID);

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("url", request.getRequestURL().toString());
        contextMap.put(QUERY_PARAMETERS, request.getQueryString() != null ? "?" + request.getQueryString() : "");

        LOG.errorContext(requestId, e.getMessage(), null, contextMap);

        Errors errors = new Errors();
        errors.addError(Err.invalidBodyBuilderWithLocation(ACCOUNTS_USER_API).withError(e.getMessage()).build());
        return errors;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Errors onConstraintViolationException( ConstraintViolationException exception, HttpServletRequest request) {

        Errors errors = new Errors();
        for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
            var errorMessage = "Please check the request and try again";
            errors.addError(Err.invalidBodyBuilderWithLocation(ACCOUNTS_USER_API).withError(errorMessage).build());
        }

        String requestId = request.getHeader(X_REQUEST_ID);
        String errorsJsonString = getJsonStringFromErrors(requestId, errors);
        LOG.errorContext(requestId, String.format("Validation Failed with [%s]", errorsJsonString), exception, null );

        return errors;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void onException(Exception e, HttpServletRequest r) {
        String requestId = r.getHeader(X_REQUEST_ID);
        String msg = r.getRequestURL() + (r.getQueryString()!=null ? "?"+r.getQueryString() : "") + ". " + e.getMessage();
        LOG.errorContext(requestId, msg, e, null);
    }
}