package com.mindae.moviecatalogsvc.resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.protocol.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.mindae.moviecatalogsvc.model.CatalogItem;
import com.mindae.moviecatalogsvc.model.Movie;
import com.mindae.moviecatalogsvc.model.Rating;
import com.mindae.moviecatalogsvc.model.UserRating;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private WebClient.Builder webClientBuilder;
	
	@HystrixCommand(fallbackMethod = "getFallbackCatalog", 
			commandProperties={
			        @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="2000"),
			            @HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value="5"),
			            @HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value="50"),
			            @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds",value="20000"),
			            @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value="5000")
			    }
	)
	@GetMapping("/{userId}")
	public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
		
//		List<Rating> ratings = Arrays.asList(
//					new Rating("1234",4),
//					new Rating("5678",5)					
//		);
		UserRating ratings = restTemplate.getForObject("http://movie-ratings-data-svc/ratingsdata/users/"+userId, UserRating.class);
		return ratings.getUserRating().stream()
						.map(rating-> {
							Movie movie = restTemplate.getForObject("http://MOVIE-INFO-SVC/movies/"+rating.getMovieId(), Movie.class);
		
//							Movie movie = webClientBuilder.build()
//											.get()
//											.uri("http://localhost:8082/movies/"+rating.getMovieId())
//											.retrieve()
//											.bodyToMono(Movie.class)
//											.block();
							
							
							return new CatalogItem(movie.getName(),"desc",rating.getRating());
						})
						.collect(Collectors.toList());

	}

	//circuit breaker fallback method
	public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId){
		return Arrays.asList(new CatalogItem("no movie","",0));
	}
	//without eureka - simple URL (host name in simple, service name as hostname in eureka URL)
//	@GetMapping("/{userId}")
//	public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
//		
////		List<Rating> ratings = Arrays.asList(
////					new Rating("1234",4),
////					new Rating("5678",5)					
////		);
//		UserRating ratings = restTemplate.getForObject("http://localhost:8083/ratingsdata/users/"+userId, UserRating.class);
//		return ratings.getUserRating().stream()
//						.map(rating-> {
////							Movie movie = restTemplate.getForObject("http://localhost:8082/movies/"+rating.getMovieId(), Movie.class);
//		
//							Movie movie = webClientBuilder.build()
//											.get()
//											.uri("http://localhost:8082/movies/"+rating.getMovieId())
//											.retrieve()
//											.bodyToMono(Movie.class)
//											.block();
//							
//							
//							return new CatalogItem(movie.getName(),"desc",rating.getRating());
//						})
//						.collect(Collectors.toList());
//
//	}
//******working code**********	
//	@GetMapping("/{userId}")
//	public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
//		
//		List<Rating> ratings = Arrays.asList(
//					new Rating("1234",4),
//					new Rating("5678",5)					
//		);
//		
//		return ratings.stream()
//						.map(rating-> {
//							Movie movie = restTemplate.getForObject("http://localhost:8082/movies/"+rating.getMovieId(), Movie.class);
//							return new CatalogItem(movie.getName(),"desc",rating.getRating());
//						})
//						.collect(Collectors.toList());
//		//		return Collections.singletonList(
//		//					new CatalogItem("Transformers","test",4)					
//		//				);
//	}
}
