package my.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import my.example.entity.CountriesGraph;
import my.example.entity.CountryJSON;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexandr Kuznetsov (alexandr@power.lv)
 */
@Component
public class CountryGraphService {
    private final static Log log = LogFactory.getLog(CountryGraphService.class);


    @Autowired
    @Qualifier("VER_1")
    private PathFinder pathFinderImpl;

    @Value("${my.sourceUrl}")
    private String sourceUrl;

    private CountriesGraph graph;


    @PostConstruct
    public void _init() throws InterruptedException {
        while (true) {
            try {
                List<CountryJSON> data = downloadRemoteData();
                graph = calculate(data);
                break;
            } catch (Throwable e) {
                log.warn("Can not initialize Service, sleep for 5 seconds", e);
                TimeUnit.SECONDS.sleep(5);
            }
        }
    }

    private CountriesGraph calculate(List<CountryJSON> inputData) {


        long start = System.nanoTime();
        CountriesGraph graph3 = pathFinderImpl.parseData(inputData);
        log.info("Pathfinder: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms");
        log.info("Pathfinder found " + graph3.getRouting().size() + " connections");


        return graph3;
    }


    private List<CountryJSON> downloadRemoteData() throws JsonProcessingException {
        log.info("Source: " + sourceUrl);

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ResponseEntity<String> response = restTemplate.getForEntity(sourceUrl, String.class);

        List<CountryJSON> countries = objectMapper.readValue(response.getBody(), new TypeReference<List<CountryJSON>>() {
        });

        return countries;


    }


    public List<String> findRoute(String ccOrigin, String ccDestination) {
        if (!graph.getCcIndexMap().containsKey(ccOrigin)) {
            throw new IllegalArgumentException("Invalid country code: " + ccOrigin);
        }
        if (!graph.getCcIndexMap().containsKey(ccDestination)) {
            throw new IllegalArgumentException("Invalid country code: " + ccDestination);
        }
        short index1 = graph.getCcIndexMap().get(ccOrigin);
        short index2 = graph.getCcIndexMap().get(ccDestination);


        final short[] route = pathFinderImpl.getDirectionalRoute(graph.getRouting(), index1, index2);
        if (null == route) return null;
        final List<String> routeCodes = new ArrayList<>();
        routeCodes.add(ccOrigin);
        for (short index : route) {
            String code = graph.getRevCcIndex().get(index);
            routeCodes.add(code);
        }
        routeCodes.add(ccDestination);


        return routeCodes;
    }

    public List<String> toRoute(short[] route, CountriesGraph graph) {
        final List<String> routeCodes = new ArrayList<>();

        for (short index : route) {
            String code = graph.getRevCcIndex().get(index);
            routeCodes.add(code);
        }

        return routeCodes;
    }
}
