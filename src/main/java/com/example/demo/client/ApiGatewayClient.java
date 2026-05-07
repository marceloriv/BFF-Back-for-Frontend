package com.example.demo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/*archivo que hacer las llmadas a la api gataway para que los clientes no tengan que 
preocuparse por la api de cada microservicio
*/
@Component
public class ApiGatewayClient {
    // inyeción de de RestTemplate para hacer las llamadas Http con la url base de la api gataway
    private final RestTemplate restTemplate;
    private final String apiGatewayUrl;

    //creé el objeto cliente conn restemplate para hacer las llamadas http
    public ApiGatewayClient(
            RestTemplate restTemplate,
            //leé la url de la api gataway app.proerties para enrutar las llamadas a la api gataway
            @Value("${api.gateway.url}") String apiGatewayUrl
    ) {
        this.restTemplate = restTemplate;
        this.apiGatewayUrl = apiGatewayUrl;
    }

    /**Sobre cargas convenientes (devuelven `String`)
     Estas versiones son atajos para casos simples (por ejemplo, recibir un token
     o texto plano). Internamente delegan a las versiones genéricas pasando
     `String.class` como `responseType`.
    
    Usar las versiones genéricas (que aceptan `Class<T> responseType`) cuando
     se espere un JSON que deba mapearse a un DTO. *\
    */
    public String get(String ruta) {
        return get(ruta, String.class);
    }

    public String post(String ruta, String body) {
        return post(ruta, body, String.class);
    }

    public String put(String ruta, String body) {
        return put(ruta, body, String.class);
    }
    // Método genérico: T representa el tipo de respuesta esperado. Usar cuando
    // se desee que Spring convierta el JSON de respuesta a un DTO concreto.
    // Ejemplo: get(ruta, ValidarCredencialesResponse.class)
    public <T> T get(String ruta, Class<T> responseType) {
        try {
            return restTemplate.getForObject(construirUrl(ruta), responseType);
        } catch (RestClientException e) {
            throw new RestClientException(String.format("ApiGateway error calling %s: %s", construirUrl(ruta), e.getMessage()), e);
        }   // se rellena el primero % con la ruta y el segundo % con el mensaje de error de la excepción original, para dar más contexto sobre qué salió mal.
    }

    public <T> T post(String ruta, Object body, Class<T> responseType) {
        return ejecutarConBody(HttpMethod.POST, ruta, body, responseType);
    }

    public <T> T put(String ruta, Object body, Class<T> responseType) {
        return ejecutarConBody(HttpMethod.PUT, ruta, body, responseType);
    }

    //busca la ruta de la api y hace llama a delete porque se espera un json de respuesta 
    public String delete(String ruta) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    construirUrl(ruta),
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    String.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new RestClientException(String.format("ApiGateway error calling %s: %s", construirUrl(ruta), e.getMessage()), e);
        }
    }

    private <T> T ejecutarConBody(HttpMethod method, String ruta, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    construirUrl(ruta),
                    method,
                    entity,
                    responseType
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new RestClientException(String.format("ApiGateway error calling %s: %s", construirUrl(ruta), e.getMessage()), e);
        }
    }

    private String construirUrl(String ruta) {
        return apiGatewayUrl + ruta;
    }

    
}