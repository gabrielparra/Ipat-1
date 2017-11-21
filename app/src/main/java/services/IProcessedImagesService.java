package services;

import java.util.List;

import domain.ProcessedImage;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IProcessedImagesService {
    @GET("/api/File/{token}")
    Call<List<ProcessedImage>> getImages(@Path("token") String token);
}
