package id.portaluserfacebook.news.api;

import id.portaluserfacebook.news.config.Constant;
import id.portaluserfacebook.news.model.NewsHeadline;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by arrival on 5/14/18.
 */

public interface APIService {
    @GET("v2/top-headlines")
    Call<NewsHeadline> loadHeadlines(@Query("country") String country,
                                     @Query("category") String category,
                                     @Query("apiKey") String apiKey);

    @GET("v2/everything")
    Call<NewsHeadline> loadEverything(@Query("q") String country,
                                      @Query("apiKey") String apiKey);

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constant.API_HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
