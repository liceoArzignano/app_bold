package it.liceoarzignano.bold.news

import it.liceoarzignano.bold.database.DBItem

class News : DBItem {
    var title = ""
    var date = 0L
    var description = ""
    var url = ""

    constructor(title: String, date: Long, description: String, url: String) {
        this.title = title
        this.date = date
        this.description = description
        this.url = url
    }

    constructor(id: Long, title: String, date: Long, description: String, url: String) {
        this.id = id
        this.title = title
        this.date = date
        this.description = description
        this.url = url
    }
}
