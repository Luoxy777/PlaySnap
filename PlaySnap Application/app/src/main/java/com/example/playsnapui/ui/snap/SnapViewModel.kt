import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SnapViewModel : ViewModel() {

    private val _latestImageUri = MutableLiveData<Uri?>()
    val latestImageUri: LiveData<Uri?> = _latestImageUri
    var imageCapture: ImageCapture? = null


    fun setLatestImageUri(uri: Uri) {
        _latestImageUri.value = uri
    }
}
