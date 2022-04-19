package fr.adixon.adiposrfid

class RFIDTag constructor(val code: String, rssi: String)  {
    var createdAt: Long = System.currentTimeMillis()
        private set
    var updatedAt: Long = System.currentTimeMillis()
        private set

    private var count: Int = 1
    private val range: Int = 30

    private val lasts = CircularArray<Long>(range)
    val averageSpeed: Long
        get() {
            if (lasts.size > 0) {
                return lasts.reduce { acc, next -> acc + next } / lasts.size
            }
            return 0
        }

    private val distances = CircularArray<Int>(range)
    val averageDistance: Int
        get() {
            if (distances.size > 0) {
                return distances.reduce { acc, next -> acc + next } / distances.size
            }
            return 0
        }

//    val speed get() = totalRSSI / count
//    val duration get() = updatedAt - createdAt
//    val lastUpdate get() = System.currentTimeMillis() - updatedAt

    fun update(rssi: String) {
        count += 1

        val now = System.currentTimeMillis()

        lasts.add(now - updatedAt)
        distances.add(rssi.toInt());

        updatedAt = now;
    }



}