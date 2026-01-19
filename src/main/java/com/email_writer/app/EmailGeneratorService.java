package com.email_writer.app;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Map;


@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private  String geminiApiUrl;
    @Value("${gemini.api.key}")
    private  String geminiApiKey;

    


    public EmailGeneratorService(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    public String generateEmailReply(EmailRequest emailRequest){
        //build the prompt

        String prompt = buildPrompt(emailRequest);

        //craft a request
        Map<String,Object> requestBody = Map.of(
                "contents",new Object[]{
                        Map.of("parts",new Object[]{
                            Map.of("text",prompt)
                })
                }
        );

        //Do request and get response
        String reponse = webClient.post()
                .uri(geminiApiUrl + "?key=" + geminiApiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();



        //Extract response and Return
        return  extractREsponseContent(reponse);


    }

    private String extractREsponseContent(String reponse) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(reponse);
            return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            return "Error processing request:" + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. Please don't add subject line");
        if(emailRequest.getTone() !=null && !emailRequest.getTone().isEmpty()){
            prompt.append("Use a").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append("\nOrginal email: \n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }

}
