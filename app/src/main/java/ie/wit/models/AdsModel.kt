package ie.wit.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class AdsModel(
    var uid: String? = "",
    var name: String = "N/A",
    var name_price: String = "N/A",
    var description: String = "Some product Description",
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
            "description" to description,
            "imageUrl" to imageUrl,
            "upvotes" to upvotes
        )
    }
}


