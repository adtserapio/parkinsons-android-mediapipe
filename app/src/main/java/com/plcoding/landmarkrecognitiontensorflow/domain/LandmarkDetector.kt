import android.graphics.Bitmap
import com.plcoding.landmarkrecognitiontensorflow.domain.Landmark
interface LandmarkDetector {
    fun detect(bitmap: Bitmap)
}