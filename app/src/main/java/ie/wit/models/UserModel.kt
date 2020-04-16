package ie.wit.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class UserModel(
    var uid: String? = "",
    var email: String? = "joe@bloggs.com",
    var name: String = "Dummy Name",
    var imageUrl: String = "someTestingImageUrl") : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "name" to name,
            "imageUrl" to imageUrl
        )
    }
}


