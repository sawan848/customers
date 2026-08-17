package com.example.app.customers.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletResponse;
import com.example.app.customers.dto.Error;
import com.example.app.customers.dto.ErrorDetails;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.UnknownHttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
*Nov 4, 20237:25:44 PM
* 
*/
@ControllerAdvice
public class GlobalExceptionalHandler extends ResponseEntityExceptionHandler {


	private Function<WebRequest, HttpHeaders> addCommonHeaders=(request)->{
		HttpHeaders responseHeader=new HttpHeaders();
		responseHeader.set("Correlation-ID",request.getHeader("Correlation-ID"));
		responseHeader.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		responseHeader.set("DATE", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

		return responseHeader;
	};



	private BiFunction<String,String,Error> createError=(code, messaage)-> new Error(code,messaage,"Contact api@helpdesk.com for more information");




	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = {HttpServerErrorException.class,
			UnknownHttpStatusCodeException.class,
			HttpStatusCodeException.class,
			RestClientException.class,
			Exception.class})
	public ResponseEntity<Object>handleException(HttpServletResponse response,
												 WebRequest request, Exception ex)throws IOException {
		HttpHeaders responseHeaders=addCommonHeaders.apply(request);
		return new ResponseEntity<>(
				createError.apply(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), ex.getMessage( )),
				responseHeaders,
				HttpStatus.INTERNAL_SERVER_ERROR
		);


	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(value = {CustomerNotFoundException.class,
			 JsonParseException.class, JsonMappingException.class})
	public ResponseEntity<Object>handleApiException(HttpServletResponse response,
													WebRequest request,Exception ex)throws IOException {
		HttpHeaders responseHeaders=addCommonHeaders.apply(request);
		return new ResponseEntity<>(
				createError.apply(String.valueOf(HttpStatus.BAD_REQUEST.value()), ex.getMessage( )),
				responseHeaders,
				HttpStatus.BAD_REQUEST
		);


	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
																  HttpHeaders headers,
																  HttpStatusCode status,
																  WebRequest request) {




		HttpHeaders responseHeaders=addCommonHeaders.apply(request);
		Error error=createError.apply(String.valueOf(HttpStatus.BAD_REQUEST.value()),
				ex.getMessage( )
				);


		List<ObjectError>globalErrors =ex.getBindingResult().getGlobalErrors();

		ex.getBindingResult().getFieldErrors().forEach(
				fieldError -> {
					ErrorDetails errorDetails=new ErrorDetails();
					errorDetails.setAttributeName(fieldError.getField());
					errorDetails.setReason(fieldError.getDefaultMessage());

					error.setErrorDetail(errorDetails);
				}
		);
		ex.getBindingResult().getGlobalErrors().forEach(objectError -> {
			ErrorDetails errorDetails=new ErrorDetails();
			errorDetails.setAttributeName(objectError.getObjectName());
			errorDetails.setReason(objectError.getDefaultMessage());


			error.setErrorDetail(errorDetails);
		});

		return new ResponseEntity<>(error,responseHeaders,HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
																   HttpHeaders headers,
																   HttpStatusCode status,
																   WebRequest request) {

		HttpHeaders responseHeaders=addCommonHeaders.apply(request);



		return new ResponseEntity<>(
				createError.apply(String.valueOf(HttpStatus.NOT_FOUND.value()),
						"No handler found for "+ex.getHttpMethod()+" "+ex.getRequestURL()),
				responseHeaders,HttpStatus.NOT_FOUND
		);

	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
																	 HttpHeaders headers,
																	 HttpStatusCode status,
																	 WebRequest request) {


		String unsupported="Unsupported content type: "+ex.getContentType();
		String supported="Supported content type: "+MediaType.toString(ex.getSupportedMediaTypes());
		HttpHeaders responseHeader=addCommonHeaders.apply(request);




		return new ResponseEntity<>(
				createError.apply(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()),
						unsupported+" , "+supported),responseHeader,
				HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
																  HttpHeaders headers,
																  HttpStatusCode status,
																  WebRequest request) {

		Throwable mostSpecific=ex.getMostSpecificCause();
		HttpHeaders responseHeaders=addCommonHeaders.apply(request);
		String message;
		if (mostSpecific!=null){
			message= mostSpecific.getMessage( );
		}else{
			message=ex.getMessage();
		}


		return new ResponseEntity<>(createError.
				apply(String.valueOf(HttpStatus.BAD_REQUEST.value()),message),
				responseHeaders,HttpStatus.BAD_REQUEST);
	}



}
