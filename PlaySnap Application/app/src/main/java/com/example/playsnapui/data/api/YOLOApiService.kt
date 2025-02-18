import com.example.playsnapui.data.api.DetectionResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface YOLOApiService {
    @Multipart
    @POST("detect/")
    fun uploadImage(@Part image: MultipartBody.Part): Call<DetectionResponse> // Response is JSON
}
