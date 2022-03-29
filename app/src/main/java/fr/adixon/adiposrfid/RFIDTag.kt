package fr.adixon.adiposrfid

import android.os.Parcel
import android.os.Parcelable

class RFIDTag constructor(val code: String) : Parcelable {
    var count: Int = 1
        private set
    var timestamp: Long = System.currentTimeMillis()

    constructor(parcel: Parcel) : this(parcel.readString()!!) {
        timestamp = parcel.readLong()
    }

    fun increment() {
        count += 1
        timestamp = System.currentTimeMillis()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RFIDTag> {
        override fun createFromParcel(parcel: Parcel): RFIDTag {
            return RFIDTag(parcel)
        }

        override fun newArray(size: Int): Array<RFIDTag?> {
            return arrayOfNulls(size)
        }
    }
}