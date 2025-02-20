import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TutorialViewModel : ViewModel() {
    // LiveData to store the state of interactions
    private val isBookmarked = MutableLiveData(false)
    private val isLiked = MutableLiveData(false)
    private val isShared = MutableLiveData(false)

    // Methods to toggle the states of bookmark, like, and share
    fun toggleBookmark() {
        val currentBookmarkState = isBookmarked.value
        isBookmarked.value = currentBookmarkState == null || !currentBookmarkState
    }

    fun toggleLike() {
        val currentLikeState = isLiked.value
        isLiked.value = currentLikeState == null || !currentLikeState
    }

    fun toggleShare() {
        val currentShareState = isShared.value
        isShared.value = currentShareState == null || !currentShareState
    }

    // Expose LiveData so that the fragment can observe it
    fun getIsBookmarked(): LiveData<Boolean> {
        return isBookmarked
    }

    fun getIsLiked(): LiveData<Boolean> {
        return isLiked
    }

    fun getIsShared(): LiveData<Boolean> {
        return isShared
    }
}