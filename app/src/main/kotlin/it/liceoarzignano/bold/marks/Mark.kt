package it.liceoarzignano.bold.marks

import it.liceoarzignano.bold.database.DBItem

class Mark : DBItem {
    var subject = ""
    var value = 0
    var date = 0L
    var description = ""
    var isFirstQuarter = false

    constructor()

    constructor(id: Long, subject: String, value: Int, date: Long,
                description: String, firstQuarter: Boolean) {
        this.id = id
        this.subject = subject
        this.value = value
        this.date = date
        this.description = description
        this.isFirstQuarter = firstQuarter
    }
}
