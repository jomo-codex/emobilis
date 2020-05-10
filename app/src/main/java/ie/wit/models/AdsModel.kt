package ie.wit.models

import android.icu.text.SimpleDateFormat
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import java.util.*

@IgnoreExtraProperties
@Parcelize
data class AdsModel(
    var uid: String? = "",
    var name: String = "N/A",
    var name_price: String = "N/A",
    var short_description: String = "Some short product Description",
    var description: String = "Some product Description",
    var posted_on: String = "Some date",
    var userId: String =  "",
    var imageUrl: String = "N/A",
    var upvotes: Int = 0
): Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "name_price" to name_price,
            "short_description" to short_description,
            "description" to description,
            "userId" to userId,
            "posted_on" to posted_on,
            "imageUrl" to imageUrl,
            "upvotes" to upvotes
        )
    }
}


