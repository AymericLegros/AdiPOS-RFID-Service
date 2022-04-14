package fr.adixon.adiposrfid

class RFIDTag constructor(val code: String)  {
    var count: Int = 1
        private set
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()
    var score: Int = 0

    fun increment() {
        count += 1
        updatedAt = System.currentTimeMillis()
    }
}