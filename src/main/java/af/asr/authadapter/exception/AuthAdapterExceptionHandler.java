package af.asr.authadapter.exception;

import af.asr.authadapter.util.EmptyCheckUtils;
import af.asr.authadapter.util.constant.AuthAdapterErrorCode;
import af.asr.authadapter.exception.common.ExceptionUtils;
import af.asr.authadapter.exception.common.ServiceError;
import af.asr.authadapter.http.ResponseWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthAdapterExceptionHandler {

	@Autowired
	private ObjectMapper objectMapper;

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onAuthenticationException(
			final HttpServletRequest httpServletRequest, final AuthenticationException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onAccessDeniedException(
			final HttpServletRequest httpServletRequest, final AccessDeniedException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(AuthAdapterErrorCode.FORBIDDEN.getErrorCode(),
				AuthAdapterErrorCode.FORBIDDEN.getErrorMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(AuthManagerException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> authManagerException(
			final HttpServletRequest httpServletRequest, final AuthManagerException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AuthNException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> authNException(final HttpServletRequest httpServletRequest,
                                                                        final AuthNException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().addAll(e.getList());
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(AuthZException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> authZException(final HttpServletRequest httpServletRequest,
                                                                        final AuthZException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().addAll(e.getList());
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
	}

	private ResponseWrapper<ServiceError> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(LocalDateTime.now(ZoneId.of("UTC")));
		String requestBody = null;
		if (httpServletRequest instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
		}
		if (EmptyCheckUtils.isNullEmpty(requestBody)) {
			return responseWrapper;
		}
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode = objectMapper.readTree(requestBody);
		responseWrapper.setId(reqNode.path("id").asText());
		responseWrapper.setVersion(reqNode.path("version").asText());
		return responseWrapper;
	}

}
