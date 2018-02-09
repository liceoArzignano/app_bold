package it.liceoarzignano.bold.poster

data class Post(var name: String = "", var data: MutableList<Item> = arrayListOf()) {

    class Item(val flags: String = "",
               val hour: String = "",
               val location: String = "",
               val replacer: String = "???")
}

